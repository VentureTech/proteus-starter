/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package experimental.cms.dsl.impl

import com.i2rd.cms.SiteSSLOption
import com.i2rd.cms.backend.BackendConfig
import com.i2rd.cms.backend.layout.BoxInformation
import com.i2rd.cms.component.miwt.MIWTPageElementModelFactory
import com.i2rd.cms.dao.CmsBackendDAO
import com.i2rd.cms.dao.CmsSiteDefinitionDAO
import com.i2rd.cms.editor.CmsEditorDAO
import com.i2rd.cms.page.BeanBoxList
import com.i2rd.cms.workflow.WorkFlowFactory
import com.i2rd.hibernate.util.HibernateRunnable
import com.i2rd.hibernate.util.HibernateUtil
import com.i2rd.lib.Library
import com.i2rd.lib.LibraryDAO
import experimental.cms.dsl.*
import net.proteusframework.cms.*
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.cms.component.page.PageTemplate
import net.proteusframework.cms.controller.LinkUtil
import net.proteusframework.cms.dao.CmsFrontendDAO
import net.proteusframework.cms.dao.PageElementPathDAO
import net.proteusframework.cms.support.HTMLPageElementUtil.populateBeanBoxLists
import net.proteusframework.core.hibernate.HibernateSessionHelper
import net.proteusframework.core.hibernate.dao.DAOHelper
import net.proteusframework.core.locale.TransientLocalizedObjectKey
import net.proteusframework.core.net.ContentTypes
import net.proteusframework.data.filesystem.FileEntity
import net.proteusframework.data.filesystem.FileSystemDAO
import net.proteusframework.data.filesystem.http.FileSystemEntityResourceFactory
import net.proteusframework.internet.http.resource.html.FactoryNDE
import net.proteusframework.internet.http.resource.html.NDE
import net.proteusframework.internet.http.resource.html.NDEType
import net.proteusframework.ui.miwt.component.Component
import net.proteusframework.users.model.dao.PrincipalDAO
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment
import java.util.*
import javax.annotation.Resource

// FIXME : implement content removal

open class CmsModelApplication(val siteDefinition: SiteDefinition) : DAOHelper(), ContentHelper {

    companion object {
        val logger = LogManager.getLogger(CmsModelApplication::class.java)!!
    }

    @Autowired
    lateinit var _backendConfig: BackendConfig
    @Autowired
    lateinit var _miwtPageElementModelFactory: MIWTPageElementModelFactory
    @Autowired(required = false)
    @Qualifier("ApplicationFunction")
    lateinit var _applicationFunctionComponents: List<Component>

    @Autowired
    lateinit var environment: Environment
    @Resource(name = HibernateSessionHelper.RESOURCE_NAME)
    lateinit var hsh: HibernateSessionHelper
    @Autowired
    lateinit var fserf: FileSystemEntityResourceFactory
    @Autowired
    lateinit var siteDefinitionDAO: CmsSiteDefinitionDAO
    @Autowired
    lateinit var cmsBackendDAO: CmsBackendDAO
    @Autowired
    lateinit var cmsFrontendDAO: CmsFrontendDAO
    @Autowired
    lateinit var principalDAO: PrincipalDAO
    @Autowired
    lateinit var contentElementDAO: CmsEditorDAO
    @Autowired
    lateinit var pagePathDAO: PageElementPathDAO
    @Autowired
    lateinit var fileSystemDAO: FileSystemDAO
    @Autowired
    lateinit var libraryDAO: LibraryDAO


    private val pageModelToCmsPage = mutableMapOf<Page, net.proteusframework.cms.component.page.Page>()
    private val layoutToBoxInformation = mutableMapOf<Layout, BoxInformation>()
    private val pagePaths = mutableMapOf<String, PageElementPath>()
    var currentSite: CmsSite? = null

    fun applyDefinition() = HibernateRunnable({
        siteDefinition.getSites().forEach { siteModel ->
            doInTransaction {
                applySiteModel(siteModel)
            }
        }
        cleanup()
    }).run()

    private fun cleanup() {
        pageModelToCmsPage.clear()
        layoutToBoxInformation.clear()
    }

    private fun applySiteModel(siteModel: Site) {
        logger.info("Applying Site Model: ${siteModel.id}")
        if(siteModel.hostnames.isEmpty())
            throw IllegalArgumentException("Invalid SiteModel. No hostnames")

        val site = getOrCreateSite(siteModel)
        currentSite = site
        siteModel.children.forEach { getOrCreatePagePass1(site, it) }
        siteModel.children.forEach { getOrCreatePagePass2(site, it) }
        for(content in siteModel.content) {
            // FIXME : implement
            // FIXME : register to site if necessary
        }
    }


    private fun getOrCreateSite(siteModel: Site): CmsSite {
        var cmsSite = siteDefinitionDAO.getSiteByDescription(siteModel.id)
        if(cmsSite == null) {
            cmsSite = CmsSite()
            cmsSite.siteDescription = siteModel.id
            cmsSite.primaryLocale = siteModel.primaryLocale
            cmsSite.defaultTimeZone = siteModel.defaultTimezone
            cmsSite.workFlow = WorkFlowFactory.getStandard3StageWorkFlow()
            cmsSite.lastModUser = principalDAO.currentPrincipal
            cmsSite.lastModified = Date()
            cmsBackendDAO.saveSite(cmsSite)
        }
        session.flush()
        return cmsSite
    }

    private fun getOrCreateHostnames(siteModel: Site, site: CmsSite): List<CmsHostname> {
        val list = mutableListOf<CmsHostname>()
        for((hnAddress, welcomePage) in siteModel.hostnames) {
            val address = environment.resolveRequiredPlaceholders(hnAddress)
            var cmsHostname = cmsFrontendDAO.getSiteHostname(address)
            if(cmsHostname == null) {
                cmsHostname = CmsHostname()
                cmsHostname.site = site
                cmsHostname.name = address
                cmsHostname.sslOption = SiteSSLOption.no_influence
                cmsHostname.welcomePage = getOrCreatePagePass1(site, welcomePage)
                session.flush()

            } else if(cmsHostname.site != site)
                throw IllegalArgumentException("Hostname, $address, exists on other site.")
            list.add(cmsHostname)
        }
        return list
    }

    private fun getOrCreatePagePass1(site: CmsSite, page: Page): net.proteusframework.cms.component.page.Page {
        val pass1 = pageModelToCmsPage.getOrPut(page, defaultValue = {
            var cmsPage = siteDefinitionDAO.getPageByName(site, page.id)
            if(cmsPage == null) {
                cmsPage = net.proteusframework.cms.component.page.Page()
                cmsPage.site = site
                cmsPage.lastModUser = principalDAO.currentPrincipal
                cmsPage.lastModified = Date()
                cmsPage.pageTemplate = getOrCreatePageTemplate1(site, page.template)
                cmsBackendDAO.savePage(cmsPage)
                cmsPage.primaryPageElementPath = createPageElementPath(cmsPage, page)
                session.flush()
            } else {
                updatePageElementPath(cmsPage, page)
                session.flush()
            }
            return cmsPage
        })
        layoutToBoxInformation.put(page.layout, BoxInformation(pass1.pageTemplate.layout))
        return pass1
    }

    private fun getOrCreatePagePass2(site: CmsSite, page: Page): net.proteusframework.cms.component.page.Page {
        val cmsPage = getOrCreatePagePass1(site, page)
        val boxInformation = layoutToBoxInformation[page.layout]
        cmsPage.beanBoxList.forEach({save(site, it)})
        for((key, value) in page.content)
        {
            val bbl = cmsPage.getBeanBoxList(boxInformation!!.getBoxByName(key.id).orElseThrow {
                IllegalArgumentException("Missing Box: ${key.id}") })!!
            if(bbl.elements.filter { it.name == value.id }.none()) {
                val contentElement = value.createInstance(this)
                contentElement.lastModUser = principalDAO.currentPrincipal
                contentElement.lastModified = Date()
                contentElement.site = site
                bbl.elements.add(contentElement)
                // FIXME : check for and register links for ApplicationFunction content
                // FIXME register to site if necessary
            }
        }
        session.flush()
        return cmsPage
    }

    private fun createPageElementPath(pageElement: PageElement, pathInfo: PathCapable): PageElementPath {
        val pep = pagePathDAO.createPageElementPathMapping(pageElement, pathInfo.getCleanPath(), pathInfo.isWildcard())
        pagePaths.put(pep.path, pep)
        return pep
    }

    private fun updatePageElementPath(pageElement: PageElement, pathInfo: PathCapable): Unit {
        val path = pathInfo.getCleanPath()
        if(pageElement.primaryPageElementPath == null)
            pageElement.primaryPageElementPath = createPageElementPath(pageElement, pathInfo)
        else {
            val pep = pageElement.primaryPageElementPath!!
            pep.path = path
            pep.isWildcard = pathInfo.isWildcard()
            pagePathDAO.savePageElementPath(pep)
            pagePaths.put(pep.path, pep)
        }
    }

    private fun getOrCreatePageTemplate1(site: CmsSite, template: Template): PageTemplate {
        var pageTemplate = siteDefinitionDAO.getPageTemplateByName(site, template.id)
        if(pageTemplate == null) {
            pageTemplate = PageTemplate()
            pageTemplate.name = template.id
            pageTemplate.cssName = template.htmlId
            pageTemplate.lastModified = Date()
            pageTemplate.layout = getOrCreateLayout(site, template.layout)
            pageTemplate.ndEs.add(createNDEs(site, template))
            populateBeanBoxLists(pageTemplate)
            session.flush()
        }
        return pageTemplate
    }

    private fun getOrCreatePageTemplate2(site: CmsSite, template: Template): PageTemplate {
        val pageTemplate = getOrCreatePageTemplate1(site, template)
        val boxInformation = layoutToBoxInformation[template.layout]
        pageTemplate.beanBoxList.forEach({save(site, it)})
        for((key, value) in template.content)
        {
            val bbl = pageTemplate.getBeanBoxList(boxInformation!!.getBoxByName(key.id).orElseThrow {
                IllegalArgumentException("Missing Box: ${key.id}") })
            if(bbl.elements.filter { it.name == value.id }.none()) {
                val contentElement = value.createInstance(this)
                contentElement.lastModUser = principalDAO.currentPrincipal
                contentElement.lastModified = Date()
                contentElement.site = site
                bbl.elements.add(contentElement)
                // FIXME : check for and register links for ApplicationFunction content
                // FIXME register to site if necessary
            }
        }
        session.flush()
        return pageTemplate
    }

    private fun save(site: CmsSite, beanBoxList: BeanBoxList) {
        val hibernateUtil = HibernateUtil.getInstance()
        val session = hsh.session
        session.save(beanBoxList)
        session.save(beanBoxList.box)
        val elements: MutableList<ContentElement> = beanBoxList.elements
        saveContentElements(elements, hibernateUtil, site)
    }

    private fun saveContentElements(elements: MutableList<ContentElement>,
        hibernateUtil: HibernateUtil, site: CmsSite) {
        val it = elements.listIterator()
        while (it.hasNext()) {
            val ce = it.next()
            if (hibernateUtil.isPersistent(ce))
                continue
            if (ce.publishedData.isNotEmpty() || ce.dataVersions.isNotEmpty()) {
                val data = if (ce.publishedData.isNotEmpty()) ce.publishedData.values.iterator().next() else
                    ce.dataVersions.iterator().next()
                data.lastModUser = principalDAO.currentPrincipal
                data.lastModTime = Date()
                val newCE = contentElementDAO.createNewRevision(ce, site.primaryLocale, data, site.workFlow.finalState)
                it.set(newCE)
            } else {
                cmsBackendDAO.saveBean(ce)
            }
        }
        for(ce in elements)
        {
            val dElements = mutableListOf<ContentElement>()
            for(de in ce.delegates) {
                if(hibernateUtil.isTransient(de))
                    cmsBackendDAO.saveDelegate(de)
                dElements.add(de.delegate)
            }
            saveContentElements(dElements, hibernateUtil, site)
        }
    }

    private fun getOrCreateLayout(site: CmsSite, layout: Layout): net.proteusframework.cms.component.page.layout.Layout {
        var cmsLayout = siteDefinitionDAO.getLayoutByName(site, layout.id)
        if(cmsLayout == null) {
            cmsLayout = net.proteusframework.cms.component.page.layout.Layout()
            cmsLayout.site = site
            cmsLayout.name = layout.id
            populateLayoutBoxes(site, layout, cmsLayout)
            cmsBackendDAO.saveLayout(cmsLayout)
            session.flush()
        }
        return cmsLayout
    }

    private fun populateLayoutBoxes(site: CmsSite, layout: Layout,
        cmsLayout: net.proteusframework.cms.component.page.layout.Layout) {
        for (box in layout.children) {
            val cmsBox = createCmsBox(box, site)
            cmsLayout.boxes.add(cmsBox)
            populateLayoutBoxes(site, box, cmsBox)
        }
    }

    private fun populateLayoutBoxes(site: CmsSite, parent: Box, cmsParent: net.proteusframework.cms.component.page.layout.Box) {
        for (child in parent.children) {
            val cmsBox = createCmsBox(child, site)
            cmsParent.children.add(cmsBox)
            populateLayoutBoxes(site, child, cmsBox)
        }
    }

    private fun createCmsBox(box: Box,
        site: CmsSite): net.proteusframework.cms.component.page.layout.Box {
        val cmsBox = net.proteusframework.cms.component.page.layout.Box()
        cmsBox.site = site
        cmsBox.name = box.id
        cmsBox.boxDescriptor = box.boxType
        cmsBox.defaultContentArea = box.defaultContentArea
        cmsBox.cssName = box.htmlId
        cmsBox.styleClass = box.htmlClass
        cmsBox.lastModUser = principalDAO.currentPrincipal
        cmsBox.lastModified = Date()
        return cmsBox
    }

    private fun createNDEs(site: CmsSite, resources: ResourceCapable): NDEList {
        val list = NDEList(ContentTypes.Text.html.contentType)
        list.ndEs.addAll(createNDEs(site, NDEType.CSS, resources.cssPaths))
        list.ndEs.addAll(createNDEs(site, NDEType.JS, resources.javaScriptPaths))
        return list
    }

    private fun createNDEs(site: CmsSite, type: NDEType, paths: List<String>): List<NDE> {
        val list = mutableListOf<NDE>()
        val root = FileSystemDirectory.getRootDirectory(site)
        val query = hsh.session.createQuery(
            "SELECT fe FROM FileEntity fe WHERE getFilePath(fe.id) LIKE :path AND fe.root = :root")
            .setParameter("root", root)
        for (path in paths) {
            @Suppress("UNCHECKED_CAST")
            val result: List<FileEntity> = query.setParameter("path", "%" + path).list() as List<FileEntity>
            if(result.size > 1) {
                throw IllegalArgumentException("Multiple files match path: $path for site: ${site.id}")
            } else if(result.isNotEmpty()) {
                val file = result[0]
                val nde = FactoryNDE()
                nde.type = type
                nde.resource = fserf.createResource(file)
                if (nde.resource == null)
                    throw IllegalStateException("Unable to get resource for file#${file.id}")
                list.add(nde)
            }
        }
        return list
    }

    override fun getInternalLink(link: String): String {
        val path = cleanPath(link)
        if(pagePaths.containsKey(path)) {
            return LinkUtil.getCMSLink(pagePaths.get(path)!!.pageElement).uriAsString
        }
        val site = currentSite!!
        val exactPath = pagePathDAO.getPageElementPathForExactPath(path, site)
        if(exactPath != null)
            return LinkUtil.getCMSLink(exactPath.pageElement).uriAsString

        val root = FileSystemDirectory.getRootDirectory(site)
        val query = hsh.session.createQuery(
            "SELECT fe FROM FileEntity fe WHERE getFilePath(fe.id) LIKE :path AND fe.root = :root")
            .setParameter("root", root)
            .setParameter("path", "%" + link)
        @Suppress("UNCHECKED_CAST")
        val results: List<FileEntity> = query.list() as List<FileEntity>
        if(results.size > 1) {
            throw IllegalArgumentException("Multiple files match file link: $link for site: ${site.id}")
        } else if(results.isNotEmpty()) {
            val file = results[0]
            return fileSystemDAO.getLocalURI(null, file).toString()
        }
        return link
    }

    override fun getBackendConfig(): BackendConfig = _backendConfig

    override fun getMIWTPageElementModelFactory() = _miwtPageElementModelFactory

    override fun getApplicationFunctions(): List<Component> = _applicationFunctionComponents

    override fun getCmsSite() = currentSite!!

    override fun assignToSite(componentIdentifier: String) {
        val siteConfiguration = cmsBackendDAO.getSiteConfiguration(currentSite)
        siteConfiguration.assignedComponentIdentifiers.add(componentIdentifier)
        cmsBackendDAO.saveSiteConfiguration(siteConfiguration)
    }

    override fun createLibrary(libraryName: String, libraryPath: String, libraryType: String): Library<*>? {
        val site = currentSite!!
        val root = libraryDAO.librariesDirectory
        val query = hsh.session.createQuery(
            "SELECT fe FROM FileEntity fe WHERE getFilePath(fe.id) LIKE :path AND fe.root = :root")
            .setParameter("root", root)
            .setParameter("path", "%" + libraryPath)
        @Suppress("UNCHECKED_CAST")
        val results: List<FileEntity> = query.list() as List<FileEntity>
        if(results.size > 1) {
            throw IllegalArgumentException("Multiple files match file link: $libraryPath for site: ${site.id}")
        } else if(results.isNotEmpty()) {
            val file = results[0]
            var library = libraryDAO.getLibraries(site, file).firstOrNull()
            if(library == null) {
                val type = libraryDAO.libraryTypes.filter { it.getModelName() == libraryType }.first()
                library = type.createLibrary()
                library.getAssignments().add(site)
                library.setFile(file)
                library.setName(TransientLocalizedObjectKey(mapOf(Locale.ENGLISH to libraryName)))
                libraryDAO.saveLibrary(library)
            }
            return library
        }
        return null
    }

    override fun saveLibrary(library: Library<*>) {
        libraryDAO.saveLibrary(library)
    }
}

