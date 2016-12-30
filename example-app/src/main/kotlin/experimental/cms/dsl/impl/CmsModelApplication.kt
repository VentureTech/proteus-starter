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

import com.i2rd.cms.HostnameDestination
import com.i2rd.cms.SiteSSLOption
import com.i2rd.cms.backend.BackendConfig
import com.i2rd.cms.backend.layout.BoxInformation
import com.i2rd.cms.component.miwt.MIWTPageElementModelFactory
import com.i2rd.cms.dao.CmsBackendDAO
import com.i2rd.cms.dao.CmsSiteDefinitionDAO
import com.i2rd.cms.editor.CmsEditorDAO
import com.i2rd.cms.page.BeanBoxList
import com.i2rd.cms.util.NDEUtil
import com.i2rd.cms.workflow.WorkFlowFactory
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
import net.proteusframework.cms.permission.PagePermission
import net.proteusframework.cms.support.HTMLPageElementUtil.populateBeanBoxLists
import net.proteusframework.core.StringFactory.convertToProgrammaticName2
import net.proteusframework.core.hibernate.HibernateSessionHelper
import net.proteusframework.core.hibernate.dao.DAOHelper
import net.proteusframework.core.locale.TransientLocalizedObjectKey
import net.proteusframework.core.net.ContentTypes
import net.proteusframework.data.filesystem.FileEntity
import net.proteusframework.data.filesystem.FileSystemDAO
import net.proteusframework.data.filesystem.http.FileSystemEntityResourceFactory
import net.proteusframework.internet.http.resource.html.FactoryNDE
import net.proteusframework.internet.http.resource.html.NDEType
import net.proteusframework.ui.management.ApplicationRegistry
import net.proteusframework.ui.management.link.RegisteredLink
import net.proteusframework.ui.management.link.RegisteredLinkDAO
import net.proteusframework.ui.miwt.component.Component
import net.proteusframework.users.model.AuthenticationMethodSecurityLevel
import net.proteusframework.users.model.dao.PrincipalDAO
import org.apache.logging.log4j.LogManager
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment
import java.util.*
import javax.annotation.Resource


open class CmsModelApplication() : DAOHelper(), ContentHelper {

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
    @Autowired
    lateinit var applicationRegistry: ApplicationRegistry
    @Autowired
    lateinit var registeredLinkDAO : RegisteredLinkDAO


    private val pagePermissionCache = mutableMapOf<String, PagePermission>()
    private val pageModelToCmsPage = mutableMapOf<Page, net.proteusframework.cms.component.page.Page>()
    private val layoutToBoxInformation = mutableMapOf<Layout, BoxInformation>()
    private val pagePaths = mutableMapOf<String, PageElementPath>()
    var currentSite: CmsSite? = null

    fun applyDefinition(siteDefinition: SiteDefinition) {
        siteDefinition.getSites().forEach { siteModel ->
            cleanup()
            doInTransaction {
                applySiteModel(siteModel)
            }
        }
    }

    private fun cleanup() {
        pageModelToCmsPage.clear()
        layoutToBoxInformation.clear()
    }

    private fun applySiteModel(siteModel: Site) {
        logger.info("Applying Site Model: ${siteModel.id}")
        if (siteModel.hostnames.isEmpty())
            throw IllegalArgumentException("Invalid SiteModel. No hostnames")

        val site = getOrCreateSite(siteModel)
        currentSite = site
        val ctrList = mutableSetOf<Content>()
        ctrList.addAll(siteModel.contentToRemove)
        val pageList = mutableListOf<Page>()
        pageList.addAll(siteModel.children)
        pageList.forEach {
            ctrList.addAll(it.contentToRemove)
            ctrList.addAll(it.template.contentToRemove)
        }
        for(ctr in ctrList) {
            val ce = siteDefinitionDAO.getContentElementByName(site, ctr.id)
            if(ce != null)
            {
                cmsBackendDAO.trashContentElement(ce, false)
            }
        }
        for(pg in siteModel.pagesToRemove) {
            pageList.remove(pg)
            val page = siteDefinitionDAO.getPageByName(site, pg.id)
            if(page != null) {
                cmsBackendDAO.trashPage(page)
            }
        }
        val hostnames = getOrCreateHostnames(siteModel, site)
        if(hostnames.isNotEmpty()) {
            site.defaultHostname = hostnames[0]
            session.saveOrUpdate(site)
        }
        pageList.forEach { getOrCreatePagePass1(site, it) }
        pageList.forEach { getOrCreatePagePass2(site, it) }
        val hibernateUtil = HibernateUtil.getInstance()
        for (content in siteModel.content) {
            if(siteModel.contentToRemove.contains(content))
                continue
            val contentElementList = mutableListOf(createContentInstance(content, site))
            saveContentElements(elements = contentElementList, hibernateUtil = hibernateUtil, site = site)
            if(!content.path.isNullOrBlank()) {
                session.flush()
                updatePageElementPath(contentElementList[0], content)
            }
        }
    }


    private fun getOrCreateSite(siteModel: Site): CmsSite {
        var cmsSite = siteDefinitionDAO.getSiteByDescription(siteModel.id)
        if (cmsSite == null) {
            logger.info("Creating Cms Site: ${siteModel.id}")
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
        for ((hnAddress, welcomePage) in siteModel.hostnames) {
            val address = environment.resolveRequiredPlaceholders(hnAddress)
            var cmsHostname = cmsFrontendDAO.getSiteHostname(address)
            if (cmsHostname == null) {
                logger.info("Creating CmsHostname: $address")
                cmsHostname = CmsHostname()
                cmsHostname.site = site
                cmsHostname.name = address
                cmsHostname.sslOption = SiteSSLOption.no_influence
                cmsHostname.welcomePage = getOrCreatePagePass1(site, welcomePage)
                cmsHostname.destination = HostnameDestination.welcome_page
                session.save(cmsHostname)

            } else if (cmsHostname.site != site)
                throw IllegalArgumentException("Hostname, $address, exists on other site.")
            list.add(cmsHostname)
        }
        return list
    }

    private fun getOrCreatePagePass1(site: CmsSite, page: Page): net.proteusframework.cms.component.page.Page {
        val pass1 = pageModelToCmsPage.getOrPut(page, defaultValue = {
            var cmsPage = siteDefinitionDAO.getPageByName(site, page.id)
            if (cmsPage == null) {
                logger.info("Creating Cms Page (pass 1): ${page.id}")
                cmsPage = net.proteusframework.cms.component.page.Page()
                cmsPage.name = page.id
                cmsPage.site = site
                cmsPage.lastModUser = principalDAO.currentPrincipal
                cmsPage.lastModified = Date()
                cmsPage.pageTemplate = getOrCreatePageTemplatePass1(site, page.template)
                session.save(cmsPage)
                createNDEs(site, cmsPage, page)
                updatePageElementPath(cmsPage, page)
                cmsBackendDAO.savePage(cmsPage)
                session.flush()
            } else {
                logger.info("Found Existing Cms Page (pass 1): ${page.id}")
                updatePageElementPath(cmsPage, page)
                session.flush()
            }
            cmsPage
        })
        layoutToBoxInformation.put(page.layout, BoxInformation(pass1.pageTemplate.layout))
        return pass1
    }

    private fun getOrCreatePagePass2(site: CmsSite, page: Page): net.proteusframework.cms.component.page.Page {
        logger.info("Creating Cms Page (pass 2): ${page.id}")
        val cmsPage = getOrCreatePagePass1(site, page)
        getOrCreatePageTemplatePass2(site, page.template)
        val boxInformation = layoutToBoxInformation.getOrPut(page.layout,
            { BoxInformation(getOrCreateLayout(site, page.layout)) })
        for ((key, value) in page.content) {
            if(page.contentToRemove.contains(value))
                continue
            val box = boxInformation.getBoxByName(key.id).orElseThrow {
                IllegalArgumentException("Missing Box: ${key.id}")
            }
            val bbl = cmsPage.getBeanBoxList().filter { it.box == box }.first()
            if (bbl.elements.filter { it.name == value.id }.none()) {
                logger.info("Creating Cms Content: ${value.id}")
                val contentElement = createContentInstance(value, site)
                bbl.elements.add(contentElement)
                if(value is ApplicationFunction && value.registerLink) {
//                    val appFun = applicationRegistry.getApplicationFunctionByName(value.id)
                    val registeredLink = RegisteredLink()
                    registeredLink.siteId = site.id
                    registeredLink.functionName = value.id
                    registeredLink.link = LinkUtil.getCMSLink(cmsPage)
                    registeredLink.functionContext = ""
                    registeredLinkDAO.saveRegisteredLink(registeredLink)
                }
            } else {
                createContentInstance(value, site) // Update content if needed
            }
            save(site, bbl)
        }
        if(!page.pagePermission.isNullOrBlank()) {
            val programmaticName: String = convertToProgrammaticName2(page.pagePermission)!!
            var permission = pagePermissionCache[programmaticName]?:
                siteDefinitionDAO.getPagePermissionByProgrammaticName(site, programmaticName)
            if(permission == null) {
                permission = PagePermission(programmaticName)
                permission.siteId = site.id
                permission.displayName = TransientLocalizedObjectKey(mutableMapOf(Locale.ENGLISH to page.pagePermission))
                permission.minimumSecurityLevel = AuthenticationMethodSecurityLevel.SHARED_SECRET
                session.save(permission)
                pagePermissionCache.put(programmaticName, permission)
            }
            cmsPage.authorization = permission
            cmsPage.touch()
        }
        val authenticationPageModel = page.authenticationPage
        if(authenticationPageModel != null) {
            cmsPage.authorizationPage = pageModelToCmsPage[authenticationPageModel]
            cmsPage.touch()
        }
        return cmsPage
    }

    private fun createContentInstance(content: Content, site: CmsSite): ContentElement {
        var contentElement = siteDefinitionDAO.getContentElementByName(site, content.id)
        if(contentElement == null) {
            contentElement = content.createInstance(this)
            contentElement.name = content.id
            contentElement.cssName = content.htmlId
            contentElement.styleClass = content.htmlClass
            contentElement.lastModUser = principalDAO.currentPrincipal
            contentElement.lastModified = Date()
            contentElement.site = site
        } else if(content.isModified(this, contentElement)) {
            val instance = content.createInstance(this)
            Hibernate.initialize(contentElement)
            Hibernate.initialize(contentElement.dataVersions)
            for(dv in contentElement.dataVersions) {
                Hibernate.initialize(dv.modelData)
                for(md in dv.modelData) {
                    Hibernate.initialize(md)
                }
            }
            Hibernate.initialize(contentElement.publishedData)
            val dataVersions = instance.dataVersions
            for(dv in dataVersions) {
                dv.contentElement = contentElement
            }
            contentElement.dataVersions.addAll(dataVersions)
        }
        createNDEs(site, contentElement, content)
        return contentElement
    }

    private fun createPageElementPath(pageElement: PageElement, pathInfo: PathCapable): PageElementPath {
        val cleanPath = pathInfo.getCleanPath()
        if (pagePaths.containsKey(cleanPath))
            return pagePaths.get(cleanPath)!!
        logger.info("Creating path: $cleanPath")
        val pep = pagePathDAO.createPageElementPathMapping(pageElement, cleanPath, pathInfo.isWildcard())
        pagePaths.put(pep.path, pep)
        return pep
    }

    private fun updatePageElementPath(pageElement: PageElement, pathInfo: PathCapable): Unit {
        val path = pathInfo.getCleanPath()
        if (pageElement.primaryPageElementPath == null)
            pageElement.primaryPageElementPath = createPageElementPath(pageElement, pathInfo)
        else {
            val pep = pageElement.primaryPageElementPath!!
            if(pep.path != path) {
                logger.info("Changing path: ${pep.path} to ${path}")
                pep.path = path
                pep.isWildcard = pathInfo.isWildcard()
                pagePathDAO.savePageElementPath(pep)
            }
            pagePaths.put(pep.path, pep)
        }
    }

    private fun getOrCreatePageTemplatePass1(site: CmsSite, template: Template): PageTemplate {
        var pageTemplate = siteDefinitionDAO.getPageTemplateByName(site, template.id)
        if (pageTemplate == null) {
            logger.info("Creating Cms Page Template (pass 1): ${template.id}")
            pageTemplate = PageTemplate()
            pageTemplate.name = template.id
            pageTemplate.site = site
            pageTemplate.cssName = template.htmlId
            pageTemplate.lastModified = Date()
            pageTemplate.layout = getOrCreateLayout(site, template.layout)
            createNDEs(site, pageTemplate, template)
            populateBeanBoxLists(pageTemplate)
            session.flush()
        } else {
            logger.info("Found Existing Cms Page Template (pass 1): ${template.id}")
        }
        return pageTemplate
    }

    private fun getOrCreatePageTemplatePass2(site: CmsSite, template: Template): PageTemplate {
        logger.info("Creating Cms Page Template (pass 2): ${template.id}")
        val pageTemplate = getOrCreatePageTemplatePass1(site, template)
        val boxInformation = layoutToBoxInformation[template.layout]
        for ((key, value) in template.content) {
            if(template.contentToRemove.contains(value))
                continue
            val box = boxInformation!!.getBoxByName(key.id).orElseThrow {
                IllegalArgumentException("Missing Box: ${key.id}")
            }
            val bbl = pageTemplate.getBeanBoxList().filter { it.box == box }.first()
            if (bbl.elements.filter { it.name == value.id }.none()) {
                logger.info("Creating Cms Content: ${value.id}. Adding To Box: ${bbl.box.name}")
                val contentElement = createContentInstance(value, site)
                bbl.elements.add(contentElement)
            } else {
                createContentInstance(value, site) // Update content if needed
            }
            save(site, bbl)
        }
        return pageTemplate
    }

    private fun save(site: CmsSite, beanBoxList: BeanBoxList) {
        logger.info("Saving BeanBoxList: ${beanBoxList.box.name}")
        val hibernateUtil = HibernateUtil.getInstance()
        val session = hsh.session
        session.save(beanBoxList)
        session.save(beanBoxList.box)
        val elements: MutableList<ContentElement> = beanBoxList.elements
        saveContentElements(elements, hibernateUtil, site)
        session.flush()
    }

    private fun saveContentElements(elements: MutableList<ContentElement>,
        hibernateUtil: HibernateUtil = HibernateUtil.getInstance(), site: CmsSite) {
        val it = elements.listIterator()
        while (it.hasNext()) {
            var ce = it.next()
            if (hibernateUtil.isPersistent(ce) && ce.dataVersions.filter { hibernateUtil.isTransient(it) }.none()) {
                logger.info("Skipping persistent Cms Content: ${ce.name}")
                continue
            }
            val dataVersions = ce.dataVersions
            if (ce.publishedData.isNotEmpty() || dataVersions.isNotEmpty()) {
                val dataSet = if (dataVersions.isNotEmpty())  dataVersions.last() else
                    ce.publishedData.values.iterator().next()
                if(dataSet.locale == null)
                    dataSet.locale = currentSite!!.primaryLocale
                dataSet.lastModUser = principalDAO.currentPrincipal
                dataSet.lastModTime = Date()
                dataSet.modelData.forEach {
                    it.lastModUser = principalDAO.currentPrincipal
                    it.lastModTime = Date()
                }
                dataVersions.remove(dataSet)
                logger.info("Creating New Cms Content Revision: ${ce.name}")
                if(dataSet.id != 0)
                    throw IllegalStateException("New dataset must be new")
                val newCE = contentElementDAO.createNewRevision(ce, site.primaryLocale, dataSet, site.workFlow.finalState)
                it.set(newCE)
                ce = newCE
            }
            logger.info("Saving Cms Content: ${ce.name}")

            cmsBackendDAO.saveBean(ce)

        }
        for (ce in elements) {
            val dElements = mutableListOf<ContentElement>()
            for (de in ce.delegates) {
                if (hibernateUtil.isTransient(de))
                    cmsBackendDAO.saveDelegate(de)
                dElements.add(de.delegate)
            }
            saveContentElements(dElements, hibernateUtil, site)
        }
    }

    private fun getOrCreateLayout(site: CmsSite, layout: Layout): net.proteusframework.cms.component.page.layout.Layout {
        var cmsLayout = siteDefinitionDAO.getLayoutByName(site, layout.id)
        if (cmsLayout == null) {
            logger.info("Creating Cms Layout: ${layout.id}")
            cmsLayout = net.proteusframework.cms.component.page.layout.Layout()
            cmsLayout.site = site
            cmsLayout.name = layout.id
            populateLayoutBoxes(site, layout, cmsLayout)
            cmsBackendDAO.saveLayout(cmsLayout)
            session.flush()
        } else {
            logger.info("Found Existing Cms Layout: ${layout.id}")
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

    private fun createNDEs(site: CmsSite, siteElement: Any, resources: ResourceCapable) {

        for(nde in createNDEs(site, NDEType.CSS, resources.cssPaths, siteElement)) {
            NDEUtil.addNDEToEntity(siteElement, nde,
                ContentTypes.Text.html.contentType,
                ContentTypes.Application.xhtml_xml.contentType
                )
        }
        for(nde in createNDEs(site, NDEType.JS, resources.javaScriptPaths, siteElement)) {
            NDEUtil.addNDEToEntity(siteElement, nde,
                ContentTypes.Text.html.contentType,
                ContentTypes.Application.xhtml_xml.contentType
                                  )
        }

    }

    private fun createNDEs(site: CmsSite, type: NDEType, paths: List<String>, siteElement : Any): List<FactoryNDE> {
        val list = mutableListOf<FactoryNDE>()
        val root = FileSystemDirectory.getRootDirectory(site)
        val query = hsh.session.createQuery(
            "SELECT fe FROM FileEntity fe WHERE getFilePath(fe.id) LIKE :path AND fe.root = :root")
            .setParameter("root", root)
        for (path in paths) {
            @Suppress("UNCHECKED_CAST")
            val result: List<FileEntity> = query.setParameter("path", "%" + path).list() as List<FileEntity>
            if (result.size > 1) {
                throw IllegalArgumentException("Multiple files match path: $path for site: ${site.id}")
            } else if (result.isNotEmpty()) {
                val file = result[0]
                val nde = FactoryNDE()
                nde.type = type
                nde.resource = fserf.createResource(file)
                if (nde.resource == null)
                    throw IllegalStateException("Unable to get resource for file#${file.id}")
                NDEUtil.updateEntityReferences(siteElement, file)
                list.add(nde)
            }
        }
        return list
    }

    override fun getInternalLink(link: String): String {
        val path = cleanPath(link)
        if (pagePaths.containsKey(path)) {
            return LinkUtil.getCMSLink(pagePaths.get(path)!!.pageElement).uriAsString
        }
        val site = currentSite!!
        val exactPath = pagePathDAO.getPageElementPathForExactPath(path, site)
        if (exactPath != null)
            return LinkUtil.getCMSLink(exactPath.pageElement).uriAsString

        val root = FileSystemDirectory.getRootDirectory(site)
        val query = hsh.session.createQuery(
            "SELECT fe FROM FileEntity fe WHERE getFilePath(fe.id) LIKE :path AND fe.root = :root")
            .setParameter("root", root)
            .setParameter("path", "%" + link)
        @Suppress("UNCHECKED_CAST")
        val results: List<FileEntity> = query.list() as List<FileEntity>
        if (results.size > 1) {
            throw IllegalArgumentException("Multiple files match file link: $link for site: ${site.id}")
        } else if (results.isNotEmpty()) {
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
        if (results.size > 1) {
            throw IllegalArgumentException("Multiple files match file link: $libraryPath for site: ${site.id}")
        } else if (results.isNotEmpty()) {
            val file = results[0]
            var library = libraryDAO.getLibraries(site, file).firstOrNull()
            if (library == null) {
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

