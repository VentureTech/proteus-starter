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

package experimental.cms.dsl.shell

import com.google.common.collect.Multimap
import com.google.common.io.BaseEncoding
import com.i2rd.cms.HostnameDestination
import com.i2rd.cms.SiteSSLOption
import com.i2rd.cms.backend.BackendConfig
import com.i2rd.cms.backend.files.FileManagerDAO
import com.i2rd.cms.backend.files.UploadRequest
import com.i2rd.cms.backend.files.ZipFileOption
import com.i2rd.cms.backend.layout.BoxInformation
import com.i2rd.cms.bean.DelegateElement
import com.i2rd.cms.bean.ScriptingBeanPageElementModelFactory
import com.i2rd.cms.bean.contentmodel.CmsModelDataSet
import com.i2rd.cms.component.miwt.MIWTPageElementModelFactory
import com.i2rd.cms.dao.CmsBackendDAO
import com.i2rd.cms.dao.CmsSiteDefinitionDAO
import com.i2rd.cms.editor.CmsEditorDAO
import com.i2rd.cms.page.BeanBoxList
import com.i2rd.cms.util.NDEUtil
import com.i2rd.cms.workflow.WorkFlowFactory
import com.i2rd.hibernate.util.HibernateUtil
import com.i2rd.lib.ILibraryType
import com.i2rd.lib.Library
import com.i2rd.lib.LibraryConfiguration
import com.i2rd.lib.LibraryDAO
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import experimental.cms.dsl.*
import net.proteusframework.cms.*
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.cms.component.page.PageTemplate
import net.proteusframework.cms.controller.LinkUtil
import net.proteusframework.cms.dao.CmsFrontendDAO
import net.proteusframework.cms.dao.PageElementPathDAO
import net.proteusframework.cms.permission.PagePermission
import net.proteusframework.cms.support.HTMLPageElementUtil.populateBeanBoxLists
import net.proteusframework.core.hibernate.HibernateSessionHelper
import net.proteusframework.core.hibernate.dao.DAOHelper
import net.proteusframework.core.io.StreamUtils
import net.proteusframework.core.locale.LocaleContext
import net.proteusframework.core.locale.LocaleSource
import net.proteusframework.core.locale.TransientLocalizedObjectKey
import net.proteusframework.core.locale.TransientLocalizedObjectKey.getTransientLocalizedObjectKey
import net.proteusframework.core.net.ContentTypes
import net.proteusframework.core.notification.Notifications
import net.proteusframework.data.filesystem.DirectoryEntity
import net.proteusframework.data.filesystem.FileEntity
import net.proteusframework.data.filesystem.FileSystemDAO
import net.proteusframework.data.filesystem.FileSystemEntityCreateMode
import net.proteusframework.data.filesystem.http.FileSystemEntityResourceFactory
import net.proteusframework.email.EmailConfig
import net.proteusframework.email.EmailConfigType
import net.proteusframework.email.EmailTemplate
import net.proteusframework.email.EmailTemplateDAO
import net.proteusframework.internet.http.Link
import net.proteusframework.internet.http.resource.html.FactoryNDE
import net.proteusframework.internet.http.resource.html.NDEType
import net.proteusframework.ui.management.ApplicationRegistry
import net.proteusframework.ui.management.link.RegisteredLink
import net.proteusframework.ui.management.link.RegisteredLinkDAO
import net.proteusframework.ui.miwt.component.Component
import net.proteusframework.ui.miwt.component.composite.Message
import net.proteusframework.users.model.Org2Role
import net.proteusframework.users.model.Organization
import net.proteusframework.users.model.Role
import net.proteusframework.users.model.dao.PrincipalDAO
import org.apache.logging.log4j.LogManager
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment
import java.io.File
import java.util.*
import javax.annotation.Resource

@Qualifier("standalone")
open class PlaceholderHelperImpl : PlaceholderHelper {
    @Autowired
    lateinit var environment: Environment

    override fun resolvePlaceholders(template: String) = environment.resolvePlaceholders(template)!!

}

open class CmsModelApplication : DAOHelper(), ContentHelper {

    companion object {
        val logger = LogManager.getLogger(CmsModelApplication::class.java)!!
        const val ARTIFACTORY_HOST = "repo.venturetech.net"
    }

    @Autowired
    lateinit var _backendConfig: BackendConfig
    @Autowired
    lateinit var _miwtPageElementModelFactory: MIWTPageElementModelFactory
    @Autowired
    lateinit var _scriptingBeanPageElementModelFactory: ScriptingBeanPageElementModelFactory
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
    lateinit var registeredLinkDAO: RegisteredLinkDAO
    @Autowired
    lateinit var emailTemplateDAO: EmailTemplateDAO
    @Autowired
    @Qualifier("localeSource")
    lateinit var localeSource: LocaleSource
    @Autowired
    lateinit var fileManagerDAO: FileManagerDAO

    private val contentElementData = mutableMapOf<ContentElement, CmsModelDataSet>()
    private val pagePermissionCache = mutableMapOf<String, PagePermission>()
    private val roleCache = mutableMapOf<String, Role>()
    private val pageModelToCmsPage = mutableMapOf<Page, net.proteusframework.cms.component.page.Page>()
    private val layoutToBoxInformation = mutableMapOf<Layout, BoxInformation>()
    private val pagePaths = mutableMapOf<String, PageElementPath>()
    private val libraries = mutableMapOf<String, Library<*>>()
    private val libraryConfigurations = mutableMapOf<Library<*>, LibraryConfiguration<*>>()
    var currentSite: CmsSite? = null
    lateinit var currentWebRoot: DirectoryEntity

    fun applyDefinition(appDefinition: AppDefinition) {
        appDefinition.getSites().forEach { siteModel ->
            cleanup()
            doInTransaction {
                applySiteModel(appDefinition, siteModel)
            }
        }
    }

    private fun cleanup() {
        pageModelToCmsPage.clear()
        layoutToBoxInformation.clear()
    }

    private fun applySiteModel(appDefinition: AppDefinition, siteModel: Site) {
        logger.info("Applying Site Model: ${siteModel.id}")

        val site = getOrCreateSite(appDefinition, siteModel)
        createRoles(siteModel, site)
        uploadResources(siteModel)
        val ctrList = mutableSetOf<Content>()
        ctrList.addAll(siteModel.contentToRemove)
        val pageList = mutableListOf<Page>()
        pageList.addAll(siteModel.children)
        pageList.forEach {
            ctrList.addAll(it.contentToRemove)
            ctrList.addAll(it.template.contentToRemove)
        }
        for (ctr in ctrList)
            addContentChildren(ctrList, ctr)
        for (ctr in ctrList) {
            val ce = siteDefinitionDAO.getContentElementByName(site, ctr.id)
            if (ce != null) {
                cmsBackendDAO.trashContentElement(ce, false)
            }
        }
        for (pg in siteModel.pagesToRemove) {
            pageList.remove(pg)
            val page = siteDefinitionDAO.getPageByName(site, pg.id)
            if (page != null) {
                cmsBackendDAO.trashPage(page)
            }
        }
        getOrCreateHostnames(siteModel, site)

        siteModel.emailTemplates.forEach { createEmailTemplate(site, it) }
        if(siteModel.emailTemplates.isNotEmpty()) session.flush()

        pageList.forEach { getOrCreatePagePass1(site, it) }
        pageList.forEach { getOrCreatePagePass2(site, it) }
        val hibernateUtil = HibernateUtil.getInstance()
        for (content in siteModel.content) {
            if (siteModel.contentToRemove.contains(content))
                continue
            logger.info("Creating Cms Content: ${content.id}. Adding To Site.")
            val contentElementList = mutableListOf(createContentInstance(content, site))
            saveContentElements(elements = contentElementList, hibernateUtil = hibernateUtil, site = site)
            if (!content.path.isNullOrBlank()) {
                session.flush()
                updatePageElementPath(contentElementList[0], content)
            }
        }
    }

    @SuppressFBWarnings("DE_MIGHT_IGNORE")
    private fun uploadResources(siteModel: Site) {
        var username=""
        var password=""
        val home = System.getenv()["HOME"]
        val gradlePropertiesFile = File("$home/.gradle/gradle.properties")
        if(gradlePropertiesFile.canRead()) {
            val properties = Properties()
            gradlePropertiesFile.inputStream().use { properties.load(it) }
            username = properties["repo_venturetech_username"] as String
            password = properties["repo_venturetech_password"] as String
        }
        val messages = mutableListOf<Message>()
        for((dir, url) in mapOf(currentWebRoot to siteModel.webResources,
            libraryDAO.librariesDirectory to siteModel.libraryResources)) {
            if(url == null) continue
            val tempFile = createTempFile(suffix = ".zip")
            tempFile.deleteOnExit()
            logger.info("Downloading $url")
            val connection = url.openConnection()
            if(url.host == ARTIFACTORY_HOST && username.isNotBlank()) {
                val userPass = BaseEncoding.base64().encode("$username:$password".toByteArray())
                val basic = "Basic $userPass"
                connection.setRequestProperty("Authorization", basic)
            }
            connection.inputStream.use { ins ->
                tempFile.outputStream().use { outs ->
                    StreamUtils.copyStream(ins, outs)
                }
            }
            val dataSource = FileDataSource(tempFile)
            val upload = UploadRequest(dir, dataSource, FileSystemEntityCreateMode.overwrite,
                EnumSet.of(ZipFileOption.preserve_directories, ZipFileOption.unzip))
            fileManagerDAO.createFiles(dir, listOf(upload), messages)
            if(!tempFile.delete()) logger.info("Unable to delete ${tempFile.absolutePath}")
        }
        val localeContext = LocaleContext(Locale.ENGLISH)
        localeContext.localeSource = localeSource
        for(m in messages){
            Notifications.log4jNotification(logger, m, localeContext)
        }
    }

    private fun createEmailTemplate(site: CmsSite, emailTemplateModel: experimental.cms.dsl.EmailTemplate<*>) {
        val emailTemplate = getEmailTemplate(emailTemplateModel.programmaticName)?:EmailTemplate(site)
        emailTemplate.name = emailTemplateModel.name
        if(emailTemplate.emailConfig == null) {
            @Suppress("UNCHECKED_CAST")
            val etct = emailTemplateDAO.getEmailConfigTypeByType<EmailConfig,EmailConfigType<EmailConfig>>(
                emailTemplateModel.type as Class<EmailConfigType<EmailConfig>>?)
            emailTemplate.emailConfig = etct.instantiateEmailConfig()
        }
        if(emailTemplate.id == 0L) {
            emailTemplate.from = emailTemplateModel.from
            emailTemplate.replyTo = emailTemplateModel.replyTo
            emailTemplate.to = emailTemplateModel.to
            emailTemplate.cc = emailTemplateModel.cc
            emailTemplate.bcc = emailTemplateModel.bcc
            emailTemplate.subject = emailTemplateModel.subject
            emailTemplate.isAdvancedEditingMode = true
            emailTemplate.htmlBody = emailTemplateModel.htmlContent
            emailTemplate.lastModUser = principalDAO.currentPrincipal
            emailTemplate.createUser = principalDAO.currentPrincipal
            emailTemplate.isApproved = true
            emailTemplateDAO.save(emailTemplate)
        }

    }

    private fun addContentChildren(ctrList: MutableSet<Content>, ctr: Content) {
        if (ctr is ContentContainer) {
            ctrList.addAll(ctr.contentToRemove)
            for (toCheck in ctr.contentList)
                addContentChildren(ctrList, toCheck)
        }
    }


    private fun getOrCreateSite(appDefinition: AppDefinition, siteModel: Site): CmsSite {
        var cmsSite = siteDefinitionDAO.getSiteByDescription(siteModel.id)
        if (cmsSite == null && appDefinition.dependency != null) {
            cmsSite = getAppDefinitionDependency(appDefinition)?.let depDef@{depDef ->
                return@depDef depDef.getSites().firstOrNull({site -> site.id == siteModel.id})?.let site@{
                    val depSite = getOrCreateSite(depDef, it)
                    getOrCreateHostnames(it, depSite)
                    return@site depSite
                }
            }
        }
        if (cmsSite == null) {
            if (siteModel.hostnames.isEmpty())
                throw IllegalArgumentException("Invalid SiteModel. No hostnames")
            logger.info("Creating Cms Site: ${siteModel.id}")
            cmsSite = CmsSite()
            cmsSite.siteDescription = siteModel.id
            cmsSite.primaryLocale = siteModel.primaryLocale
            cmsSite.defaultTimeZone = siteModel.defaultTimezone
            cmsSite.workFlow = WorkFlowFactory.getStandard3StageWorkFlow()
            cmsSite.lastModUser = principalDAO.currentPrincipal
            cmsSite.lastModified = Date()
            cmsBackendDAO.saveSite(cmsSite)
            val client = session.createQuery("FROM Organization WHERE programmaticName = 'venturetech'")
                .uniqueResult() as Organization
            cmsBackendDAO.associate(client, cmsSite)
        }
        currentSite = cmsSite
        currentWebRoot = FileSystemDirectory.getRootDirectory(cmsSite)
        createNDEs(cmsSite, cmsSite, siteModel)
        session.flush()
        return cmsSite
    }

    private fun createRoles(siteModel: Site, site: CmsSite) {
        if(siteModel.roles.isEmpty()) return
        val client = session.createQuery("FROM Org2Site WHERE site = :site")
            .setEntity("site", site)
            .uniqueResult() as Organization
        val query = session.createQuery("FROM Role WHERE programmaticName = :programmaticName")
        for(role in siteModel.roles) {
            var userRole = query.setParameter("programmaticName", role.programmaticName)
                .uniqueResult() as Role?
            if(userRole == null) {
                userRole = Role()
                userRole.programmaticName = role.programmaticName
                userRole.createTime = Date()
                userRole.createUser = principalDAO.currentPrincipal
                session.save(userRole)
                val org2Role = Org2Role()
                org2Role.organization = client
                org2Role.role = userRole
                session.save(org2Role)
            }
            userRole.lastModTime = Date()
            userRole.lastModUser = principalDAO.currentPrincipal
            userRole.sessionTimeout = role.sessionTimeout
            val name = getTransientLocalizedObjectKey(localeSource, userRole.name)?:
                TransientLocalizedObjectKey(mutableMapOf())
            name.text[site.primaryLocale] = role.name
            val description = getTransientLocalizedObjectKey(localeSource, userRole.description)?:
                TransientLocalizedObjectKey(mutableMapOf())
            description.text[site.primaryLocale] = role.description
            if(name.isModification(localeSource, true))
                userRole.name = name
            if(description.isModification(localeSource, true))
                userRole.description = description
            session.saveOrUpdate(userRole)

            roleCache[role.programmaticName]=userRole
        }
        session.flush()
    }

    private fun getOrCreateHostnames(siteModel: Site, site: CmsSite): List<CmsHostname> {
        val list = mutableListOf<CmsHostname>()
        for ((hnAddress, welcomePage) in siteModel.hostnames) {
            val address = environment.resolveRequiredPlaceholders(hnAddress).let d@{
                    val pat1 = "_| ".toRegex()
                    val pat2 = "[^a-zA-Z\\-0-9.]".toRegex()

                    return@d it.replace(pat1, "-").replace(pat2, "").toLowerCase()
                }
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
        if (list.isNotEmpty()) {
            site.defaultHostname = list[0]
            cmsBackendDAO.saveSite(site, list)
            val user = principalDAO.currentPrincipal!!
            if (!user.authenticationDomains.contains(site.domain)) {
                user.authenticationDomains.add(site.domain)
                principalDAO.savePrincipal(user)
            }
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
        for ((key, valueList) in page.content) {
            val box = boxInformation.getBoxByName(key.id).orElseThrow {
                IllegalArgumentException("Missing Box: ${key.id}")
            }
            val bbl = cmsPage.getBeanBoxList().filter { it.box == box }.first()
            val contentList = valueList.filter { !page.contentToRemove.contains(it) }
            for (value in contentList) {
                if (bbl.elements.filter { it.name == value.id }.none()) {
                    logger.info("Creating Cms Content: ${value.id}. Adding To Box: ${box.name}")
                    val contentElement = createContentInstance(value, site)
                    bbl.elements.add(contentElement)
                } else {
                    createContentInstance(value, site) // Update content if needed
                }
            }
            save(site, bbl, key, page)
        }
        page.pagePermission?.let {
            val programmaticName = it.programmaticName
            var permission = pagePermissionCache[programmaticName] ?:
                siteDefinitionDAO.getPagePermissionByProgrammaticName(site, programmaticName)
            if (permission == null) {
                permission = PagePermission(programmaticName)
                permission.siteId = site.id
                permission.displayName = TransientLocalizedObjectKey(mutableMapOf(Locale.ENGLISH to it.name))
                permission.minimumSecurityLevel = it.minAuthenticationMethodSecurityLevel
                permission.maximumSecurityLevel = it.maxAuthenticationMethodSecurityLevel
                permission.credentialPolicyLevel = it.policyLevel
                session.save(permission)
                pagePermissionCache.put(programmaticName, permission)
            }
            if(it.addToRole.isNotBlank()) {
                val userRole = roleCache[it.addToRole] ?:
                    session.createQuery("FROM Role WHERE programmaticName = " + ":programmaticName")
                    .setParameter("programmaticName", it.addToRole)
                    .uniqueResult() as Role? ?: throw IllegalArgumentException("Role does not exist: ${it.addToRole}")
                if(!userRole.permissions.contains(permission)) {
                    userRole.permissions.add(permission)
                    session.saveOrUpdate(userRole)
                }

            }
            page.title.let {
                val title = getTransientLocalizedObjectKey(localeSource, cmsPage.persistedTitleKey)?:
                    TransientLocalizedObjectKey(mutableMapOf())
                title.text[site.primaryLocale] = it
                cmsPage.persistedTitleKey = title
            }
            cmsPage.authorization = permission
            cmsPage.touch()
        }
        val authenticationPageModel = page.authenticationPage
        if (authenticationPageModel != null) {
            cmsPage.authorizationPage = pageModelToCmsPage[authenticationPageModel]
            cmsPage.touch()
        }
        return cmsPage
    }

    private fun createContentInstance(content: Content, site: CmsSite): ContentElement {
        var contentElement = siteDefinitionDAO.getContentElementByName(site, content.id)
        if (contentElement == null) {
            val instance = content.createInstance(this)
            contentElement = instance.contentElement
            handleNewDataSet(instance, content, site)
        } else if (content.isModified(this, contentElement)) {
            val instance = content.createInstance(this, contentElement)
            handleNewDataSet(instance, content, site)
            Hibernate.initialize(contentElement)
            Hibernate.initialize(contentElement.dataVersions)
            for (dv in contentElement.dataVersions) {
                Hibernate.initialize(dv.modelData)
                for (md in dv.modelData) {
                    Hibernate.initialize(md)
                }
            }
            Hibernate.initialize(contentElement.publishedData)
            logger.info("Cms Content Is Modified: ${contentElement.name}")
        }
        createNDEs(site, contentElement, content)
        if (content is DelegateContent) {
            for (child in content.contentList) {
                val ceDelegates = contentElement.delegates
                if (content.contentToRemove.contains(child))
                    continue
                val existingDelegate = ceDelegates.filter { it.delegate.name == child.id }.firstOrNull()
                if (existingDelegate == null) {
                    logger.info("Creating Cms Content: ${child.id}. Adding To Content: ${content.id}")
                    val delegate = createContentInstance(child, site)
                    val delegateElement = DelegateElement(delegate, content.contentPurpose[child]!!)
                    ceDelegates.add(delegateElement)
                } else {
                    createContentInstance(child, site) // Check to see if the delegate has been modified
                }
            }
        }
        return contentElement
    }

    private fun handleNewDataSet(instance: ContentInstance, content: Content, site: CmsSite): Unit {
        val contentElement = instance.contentElement
        contentElement.lastModified = Date()
        if (contentElement.name.isNullOrBlank())
            contentElement.name = content.id
        contentElement.cssName = content.htmlId
        contentElement.styleClass = content.htmlClass
        contentElement.lastModUser = principalDAO.currentPrincipal
        contentElement.lastModified = Date()
        if (contentElement.site == null)
            contentElement.site = site
        logger.info("Saving Cms Content: ${contentElement.name}")
        //session.saveOrUpdate(contentElement)
        val dataSet = instance.dataSet ?: return
        contentElementData.put(contentElement, dataSet)
        if (dataSet.locale == null)
            dataSet.locale = currentSite!!.primaryLocale
        dataSet.lastModUser = principalDAO.currentPrincipal
        dataSet.lastModTime = Date()
        dataSet.modelData.forEach {
            dataSet.lastModUser = principalDAO.currentPrincipal
            dataSet.lastModTime = Date()
        }
    }

    private fun createPageElementPath(pageElement: PageElement, pathInfo: PathCapable): PageElementPath {
        val cleanPath = pathInfo.getCleanPath()
        if (pagePaths.containsKey(cleanPath))
            return pagePaths[cleanPath]!!
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
            if (pep.path != path) {
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
        for ((key, valueList) in template.content) {
            val contentList = valueList.filter { !template.contentToRemove.contains(it) }
            val box = boxInformation!!.getBoxByName(key.id).orElseThrow {
                IllegalArgumentException("Missing Box: ${key.id}")
            }
            val bbl = pageTemplate.getBeanBoxList().filter { it.box == box }.first()
            for (value in contentList) {
                if (bbl.elements.filter { it.name == value.id }.none()) {
                    logger.info("Creating Cms Content: ${value.id}. Adding To Box: ${bbl.box.name}")
                    val contentElement = createContentInstance(value, site)
                    bbl.elements.add(contentElement)
                } else {
                    createContentInstance(value, site) // Update content if needed
                }
            }
            save(site, bbl, key, template)
        }
        return pageTemplate
    }

    private fun save(site: CmsSite, beanBoxList: BeanBoxList, boxModel: Box, boxedContentModel: BoxedContent) {
        logger.info("Saving BeanBoxList: ${beanBoxList.box.name}")
        val hibernateUtil = HibernateUtil.getInstance()
        val session = hsh.session
        session.save(beanBoxList)
        session.save(beanBoxList.box)
        val elements: MutableList<ContentElement> = beanBoxList.elements
        saveContentElements(elements, hibernateUtil, site)
        elements.sortWith(Comparator { e1, e2 ->
            val idx1: Int = boxedContentModel.indexOf(boxModel, e1.name)
            val idx2: Int = boxedContentModel.indexOf(boxModel, e2.name)
            idx1 - idx2
        })
        session.flush()
    }

    private fun saveContentElements(elements: MutableList<ContentElement>,
        hibernateUtil: HibernateUtil = HibernateUtil.getInstance(), site: CmsSite) {
        val it = elements.listIterator()
        loop@
        while (it.hasNext()) {
            val ce = it.next()
            val dataSet = contentElementData.remove(ce.delegate)
            saveChildElements(ce, hibernateUtil, site)
            if (dataSet != null) {
                logger.info("Creating New Cms Content Revision: ${ce.name}")
                if (dataSet.id != 0)
                    throw IllegalStateException("New dataset must be new")
                val newCE = contentElementDAO.createNewRevision(ce.delegate, site.primaryLocale, dataSet,
                    site.workFlow.finalState) as ContentElement
                it.set(newCE)
                if (ce is DelegateElement)
                    ce.delegate = newCE
            } else {
                session.save(ce.delegate)
            }
        }
    }

    private fun saveChildElements(child: ContentElement,
        hibernateUtil: HibernateUtil, site: CmsSite) {
        val dElements = mutableListOf<ContentElement>()
        for (de in child.delegates) {
            dElements.add(de)
        }
        if (dElements.isNotEmpty())
            saveContentElements(dElements, hibernateUtil, site)
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

    private fun createNDEs(site: CmsSite, siteElement: Any, resources: ResourceCapable) {

        for (nde in createNDEs(site, NDEType.CSS, resources.cssPaths, siteElement)) {
            NDEUtil.addNDEToEntity(siteElement, nde,
                ContentTypes.Text.html.contentType,
                ContentTypes.Application.xhtml_xml.contentType
                                  )
        }
        for (nde in createNDEs(site, NDEType.JS, resources.javaScriptPaths, siteElement)) {
            NDEUtil.addNDEToEntity(siteElement, nde,
                ContentTypes.Text.html.contentType,
                ContentTypes.Application.xhtml_xml.contentType
                                  )
        }

    }

    private fun createNDEs(site: CmsSite, type: NDEType, paths: List<String>, siteElement: Any): List<FactoryNDE> {
        val list = mutableListOf<FactoryNDE>()
        val query = hsh.session.createQuery(
            "SELECT fe FROM FileEntity fe WHERE getFilePath(fe.id) LIKE :path AND fe.root = :root")
            .setParameter("root", currentWebRoot)
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
            return LinkUtil.getCMSLink(pagePaths[path]!!.pageElement).uriAsString
        }
        val site = currentSite!!
        val exactPath = pagePathDAO.getPageElementPathForExactPath(path, site)
        if (exactPath != null)
            return LinkUtil.getCMSLink(exactPath.pageElement).uriAsString

        val query = hsh.session.createQuery(
            "SELECT fe FROM FileEntity fe WHERE getFilePath(fe.id) LIKE :path AND fe.root = :root")
            .setParameter("root", currentWebRoot)
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

    override fun getCMSLink(page: Page): Link = LinkUtil.getCMSLink(pageModelToCmsPage[page])

    override fun getBackendConfig(): BackendConfig = _backendConfig

    override fun getMIWTPageElementModelFactory() = _miwtPageElementModelFactory

    override fun getScriptingBeanPageElementModelFactory() = _scriptingBeanPageElementModelFactory

    override fun getApplicationFunctions(): List<Component> = _applicationFunctionComponents

    override fun getCmsSite() = currentSite!!

    override fun assignToSite(componentIdentifier: String) {
        val siteConfiguration = cmsBackendDAO.getSiteConfiguration(currentSite)
        if (siteConfiguration.assignedComponentIdentifiers.add(componentIdentifier))
            cmsBackendDAO.saveSiteConfiguration(siteConfiguration)
    }

    override fun createLibrary(libraryName: String, libraryPath: String, libraryType: String): Library<*>? {
        if(libraries[libraryPath] == null) {
            val site = getCmsSite()
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
                val libraryList = libraryDAO.getLibraries(site, file)
                if (libraryList.size > 1)
                    throw IllegalArgumentException("Multiple libraries match file link: $libraryPath for site: ${site.id}")
                var library = libraryList.firstOrNull()
                if (library == null) {
                    val type = libraryDAO.libraryTypes.filter { it.getModelName() == libraryType }.first()
                    library = type.createLibrary()
                    library.getAssignments().add(site)
                    library.setFile(file)
                    library.setName(TransientLocalizedObjectKey(mapOf(Locale.ENGLISH to libraryName)))
                    libraryDAO.saveLibrary(library)
                    libraries.put(libraryPath, library)
                }
                return library
            }
        }
        return libraries[libraryPath]
    }

    override fun saveLibrary(library: Library<*>) {
        libraryDAO.saveLibrary(library)
    }

    override fun <LT : ILibraryType<LT>?> getLibraryConfiguration(library: Library<LT>): LibraryConfiguration<LT>? {
        val query = session.createQuery("SELECT lc FROM LibraryConfiguration lc WHERE lc.library = :library")
            .setParameter("library", library)
        @Suppress("UNCHECKED_CAST")
        val results = query.list() as List<LibraryConfiguration<LT>>
        if (results.size > 1) {
            throw IllegalArgumentException(
                "Multiple LibraryConfigurations match library: ${library.id} for site: ${currentSite!!.id}")
        } else if (results.isNotEmpty()) {
            return results[0]
        } else {
            @Suppress("UNCHECKED_CAST")
            return libraryConfigurations[library] as LibraryConfiguration<LT>?
        }

    }

    override fun saveLibraryConfiguration(libraryConfiguration: LibraryConfiguration<*>) {
        libraryDAO.saveLibraryConfiguration(libraryConfiguration)
        libraryConfigurations.put(libraryConfiguration.getLibrary(), libraryConfiguration)
    }

    override fun resolvePlaceholders(template: String): String = environment.resolvePlaceholders(template)

    override fun getEmailTemplate(programmaticName: String): EmailTemplate? =
        emailTemplateDAO.getEmailTemplate(programmaticName, currentSite)

    override fun getRegisteredLink(functionName: String, functionContext: String): RegisteredLink? {
        val applicationFunctionByName = applicationRegistry.getApplicationFunctionByName(functionName)
        val applicationContextProvider = applicationRegistry.getApplicationContextProvider(applicationFunctionByName)
        val fc = applicationContextProvider.getPossibleContexts(getCmsSite()).filter { it.name == functionContext }.firstOrNull()
        val registeredLink = registeredLinkDAO.getRegisteredLink(getCmsSite(), applicationFunctionByName, fc)
        return registeredLink
    }

    override fun saveRegisteredLink(registeredLink: RegisteredLink) = registeredLinkDAO.saveRegisteredLink(registeredLink)

    override fun <LT:ILibraryType<LT>> setScriptParameters(libraryConfiguration: LibraryConfiguration<LT>,
        parameters: Multimap<String, Any>) {
        libraryDAO.setParameterValues(libraryConfiguration, null, null, parameters)
    }
}

