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

package experimental.cms.dsl

import com.i2rd.cms.dao.CmsSiteDefinitionDAO
import net.proteusframework.core.spring.ApplicationContextUtils
import net.proteusframework.email.EmailConfigType
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URL
import java.util.*
import javax.annotation.PostConstruct


internal data class Hostname(val address: String, val welcomePage: Page)

@SiteElementMarker
class Site(id: String, private val appDefinition: AppDefinition) : IdentifiableParent<Page>(id), ContentContainer, ResourceCapable {

    companion object{
        val logger = LogManager.getLogger(Site::class.java)!!
    }
    private var siteDefinitionDAO: CmsSiteDefinitionDAO = appDefinition.siteDefinitionDAO
    private var dependency: AppDefinition? = null
        get() = getAppDefinitionDependency(appDefinition)


    private val contentToRemoveImplementation: MutableList<Content> = mutableListOf()
    override val contentToRemove: MutableList<Content>
        get() = contentToRemoveImplementation
    override val contentList: MutableList<Content> get() = content
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()

    /** Internal Use. */
    internal var sitePreferenceKey: String = ""
    /** Internal Use. */
    internal var webResources: URL? = null
    /** Internal Use. */
    internal var libraryResources: URL? = null
    /** Internal Use. */
    internal val roles = mutableListOf<Role>()
    /** Internal Use. */
    internal val emailTemplates = mutableListOf<EmailTemplate<*>>()
    /** Internal Use. */
    internal val hostnames = mutableListOf<Hostname>()
    /** Internal Use. */
    internal val templates = mutableListOf<Template>()
    /** Internal Use. */
    internal val layouts = mutableListOf<Layout>()
    /** Internal Use. */
    internal val content = mutableListOf<Content>()
    /** Internal Use. */
    internal val pagesToRemove = mutableListOf<Page>()
    /** Internal Use. */
    internal var primaryLocale: Locale = Locale.ENGLISH
    /** Internal Use. */
    internal var defaultTimezone: TimeZone = TimeZone.getTimeZone("US/Central")
    /** Internal Use. */
    internal lateinit var parent: AppDefinition
    /** Internal Use. */
    internal val siteConstructedCallbacks = mutableListOf<(Site) -> Unit>()


    /**
     * Internal Use.
     *
     * Get Content previously added to a site element by its identifier.
     */
    internal fun getContentById(existingId: String): Content {
        // Need ContentElement.class => Content::class mapping
//        val content = siteDefinitionDAO.getSiteByDescription(id)?.let {siteDefinitionDAO.getContentElementByName(it, existingId)}
//        if(content != null) return CreateContent(existingId)
        val predicate = createContentIdPredicate(existingId)
        return children.flatMap(Page::contentList).filter(predicate).firstOrNull() ?:
            templates.flatMap(Template::contentList).filter(predicate).firstOrNull() ?:
            content.filter(predicate).firstOrNull()?:throw IllegalStateException("Content '$existingId' does not exist")
    }

    internal fun _getExistingLayout(existingId: String): Layout {
        val existingLayout = layouts.filter({ it.id == existingId }).firstOrNull()?:let{
            val layout = siteDefinitionDAO.getSiteByDescription(id)?.let {
                siteDefinitionDAO.getLayoutByName(it, existingId)
            }
            if (layout != null) _createExistingLayout(layout) else null
        }
        return existingLayout?:let {
            val dep = dependency
            if(dep != null) {
                for(depSite in dep.getSites()) {
                    try{
                        return@let depSite._getExistingLayout(existingId)
                    }catch (ignore: IllegalStateException) {
                        logger.debug("Unable to find layout in site: $depSite.id", ignore)
                    }
                }
            }
            throw IllegalStateException("Layout '$existingId' does not exist")
        }
    }

    private  fun _createExistingLayout(layout: net.proteusframework.cms.component.page.layout.Layout)  : Layout {
        val existingLayout = Layout(layout.name, this)
        for(cmsBox in layout.boxes)
            _populateModelBoxes(existingLayout, cmsBox)
        return existingLayout
    }

    private fun _populateModelBoxes(layout: Layout, cmsBox: net.proteusframework.cms.component.page.layout.Box) {
        val box = Box(cmsBox.name, cmsBox.boxDescriptor, cmsBox.defaultContentArea, cmsBox.wrappingContainerCount,
            cmsBox.cssName, cmsBox.styleClass)
        layout.add(box)
        for(cmsChildBox in cmsBox.children) {
            _populateModelBoxes(box, cmsChildBox)
        }
    }
    private fun _populateModelBoxes(parent: Box, cmsBox: net.proteusframework.cms.component.page.layout.Box) {
        val box = Box(cmsBox.name, cmsBox.boxDescriptor, cmsBox.defaultContentArea, cmsBox.wrappingContainerCount,
            cmsBox.cssName, cmsBox.styleClass)
        parent.add(box)
        for(cmsChildBox in cmsBox.children) {
            _populateModelBoxes(box, cmsChildBox)
        }
    }

    internal fun _getExistingTemplate(existingId: String): Template {
        val existingTemplate = templates.filter({ it.id == existingId }).firstOrNull()?:let{
            val template = siteDefinitionDAO.getSiteByDescription(id)?.let {
                siteDefinitionDAO.getPageTemplateByName(it, existingId)
            }
            if (template != null) Template(existingId, this, _createExistingLayout(template.layout)) else null
        }
        return existingTemplate?:let {
            val dep = dependency
            if(dep != null) {
                for(depSite in dep.getSites()) {
                    try{
                        return@let depSite._getExistingTemplate(existingId)
                    }catch (ignore: IllegalStateException) {
                        logger.debug("Unable to find template in site: $depSite.id", ignore)
                    }
                }
            }
            throw IllegalStateException("Template '$existingId' does not exist")
        }
    }

    /**
     * Internal Use: Get an existing defined Page from the Site.

     * @param existingId the existing page identifier.
     * @return the existing page if it exists.
     */
    fun getExistingPage(existingId: String): Page {
        val existingPage = children.filter { it.id == existingId }.firstOrNull()?:let {
            val page = siteDefinitionDAO.getSiteByDescription(id)?.let {
                siteDefinitionDAO.getPageByName(it, existingId)
            }
            if(page != null)
                Page(existingId, this, page.primaryPath, Template(page.pageTemplate.name, this,
                    _createExistingLayout(page.pageTemplate.layout)))
            else
                null
        }
        return existingPage?:let {
            val dep = dependency
            if(dep != null) {
                for(depSite in dep.getSites()) {
                    try{
                        return@let depSite.getExistingPage(existingId)
                    }catch (ignore: IllegalStateException) {
                        logger.debug("Unable to find page in site: $depSite.id", ignore)
                    }
                }
            }
            throw IllegalStateException("Page '$existingId' does not exist")
        }
    }

    /**
     * Provide a [java.util.prefs.Preferences] key to store the [net.proteusframework.cms.CmsSite.getId].
     * @param the key. Must follow [java.util.prefs.Preferences] constraints like key length.
     */
    fun storeSitePreference(key: String) {
        if(key.isBlank()) throw IllegalArgumentException("Key must be specified")
        sitePreferenceKey = key
    }

    /**
     * Add a URL to a Zip file of Web Resources.
     *
     * If specified, this file will be unzipped and uploaded to the Site's Web docroot.
     *
     * @param url the URL
     */
    fun webResources(url: URL) {
        if(!url.path.endsWith(".zip")) throw IllegalArgumentException("Invalid URL: $url")
        webResources = url
    }

    /**
     * Add a URL to a Zip file of Library Resources.
     *
     * If specified, this file will be unzipped and uploaded to the Libraries docroot.
     *
     * @param url the URL
     */
    fun libraryResources(url: URL) {
        if(!url.path.endsWith(".zip")) throw IllegalArgumentException("Invalid URL: $url")
        libraryResources = url
    }

    /**
     * Add a user role to the site.
     * @param name the name of the role.
     * @param programmaticName the programmatic name. This is used to reference the role.
     * @param description optional description.
     * @param sessionTimeout session timeout for this role in hours.
     */
    fun role(name: String, programmaticName: String, description: String = "", sessionTimeout: Int = 0) {
        roles.add(Role(programmaticName, name, description, sessionTimeout * 60 * 60 /* Convert to seconds */))
    }

    /**
     * Add content to the site. This is usually used for web services or dynamic content.
     * @param content the Content (e.g. [experimental.cms.dsl.content.ScriptedGenerator]).
     * @param init initialization block.
     */
    fun <T : Content> content(content: T, init: T.() -> Unit = {}): T {
        siteConstructedCallbacks.add({ _ ->
            this.content.add(content)
        })
        content.parent = this
        content.apply(init)
        if(content.path.isNotBlank())
            content.path = resolvePlaceholders(content.path)
        return content
    }

    /**
     * Add content to the site. This is usually used for web services or dynamic content.
     * @param existingContentId an reference to Content defined elsewhere
     */
    fun content(existingContentId: String): Content {
        val contentById = getContentById(existingContentId)
        siteConstructedCallbacks.add({ _ ->
            this.content.add(contentById)
        })
        return contentById
    }

    /**
     * Add a page to the site.
     * @param id the identifier. Something like "User Management".
     * @param path the page path. Page paths can contain spring environment variables like "\${folder.company}/users".
     */
    fun page(id: String, path: String, init: Page.() -> Unit): Page {
        val page = Page(id = id, site = this, path = path)
        siteConstructedCallbacks.add({_ ->
            page.path = resolvePlaceholders(path)
            page.apply(init)
        })
        return page
    }


    /**
     * Add a template to the site.
     * @param id the identifier. Something like "Frontend" or "Configuration"
     * @param init the initialization block.
     */
    fun template(id: String, init: Template.() -> Unit) = Template(id, this@Site).apply(init)

    /**
     * Add a layout to the site.
     * @param id the identifier / name of the layout.
     * @param init the initialization block.
     */
    fun layout(id: String, init: Layout.() -> Unit) = Layout(id, this@Site).apply(init)

    /**
     * Add a hostname to the site. The first hostname added becomes the default hostname.
     * @param address the web address like "www.example.com".
     * @param existingWelcomePageId a reference to an existing page to use as the welcome / home page.
     */
    fun hostname(address: String, existingWelcomePageId: String) {
        siteConstructedCallbacks.add({site ->
            val page = getExistingPage(existingWelcomePageId)
            hostnames.add(Hostname(address, page))
        })
    }

    /**
     * Add a hostname to the site. The first hostname added becomes the default hostname.
     * @param address the web address like "www.example.com".
     * @param welcomePageId the page identifier for a new page to use as the welcome / home page.
     * @param init the initialization block.
     */
    fun hostname(address: String, welcomePageId: String, init: Page.() -> Unit) {
        val page = children.filter { it.id == welcomePageId }.firstOrNull() ?: Page(welcomePageId, this)
        page.apply(init)
        if (page.path.isBlank()) throw IllegalStateException("Missing path")
        siteConstructedCallbacks.add({ _ ->
            hostnames.add(Hostname(address, page))
        })
    }

    /**
     * Add an email template to the site.
     * @param type the email configuration type.
     * @param name the name of the email template.
     * @param programmaticName the programmatic name (must be unique). You will use this to assign email templates to Content.
     * @param init the initialization block.
     */
    fun <T: EmailConfigType<*>>
        emailTemplate(type: Class<T>, name: String, programmaticName: String, init: EmailTemplate<*>.() -> Unit = {}) {
        val emailTemplate = EmailTemplate<T>(type, name, programmaticName)
        emailTemplates.add(emailTemplate.apply(init))
    }

    private fun resolvePlaceholders(template: String) = parent.placeholderHelper.resolvePlaceholders(template)

    override fun toString(): String {
        return "Site(" +
            "id='$id', pages=$children," +
            "hostnames=$hostnames," +
            "templates=$templates," +
            "layouts=$layouts" +
            ")"
    }

}

/** @suppress */
private fun appDefinitionExample() {
    @Profile("automation")
    @Component
    open class MyAppDefinition : AppDefinition("My Cool Application", version = 1) {
        init {
            createSite("Cool Application") {
                // ...
            }
        }
    }
}

/**
 * Extend class to define a new site definition.
 *
 * @sample appDefinitionExample
 */
abstract class AppDefinition(val definitionName: String, val version: Int, siteId: String, var dependency: String? = null,
    val init: Site.() -> Unit){

    companion object {
        internal val registeredSites = mutableMapOf<AppDefinition, MutableList<Site>>()
    }

    data class SiteToConstruct(val id: String, val init: Site.() -> Unit = {})

    constructor(definitionName: String, version: Int): this(definitionName, version, "", null, {})

    @Autowired
    @Qualifier("standalone")
    lateinit var placeholderHelper: PlaceholderHelper
    @Autowired
    lateinit var siteDefinitionDAO: CmsSiteDefinitionDAO

    private val sitesToConstruct = mutableListOf<SiteToConstruct>()


    init {
        if(siteId.isNotBlank())
            sitesToConstruct.add(SiteToConstruct(siteId, init))
    }

    @PostConstruct
    fun postConstruct() {
        sitesToConstruct.forEach {
            val site = Site(it.id, this)
            site.parent = this
            site.apply(it.init)
            registeredSites.getOrPut(this, { mutableListOf<Site>() }).add(site)

            val traverseCallbacks = site.siteConstructedCallbacks.toMutableList()
            while(!traverseCallbacks.isEmpty()) {
                site.siteConstructedCallbacks.clear()
                val callback = traverseCallbacks.removeAt(0)
                callback.invoke(site)
                // Invoking callback can add more site constructed callbacks
                traverseCallbacks.addAll(site.siteConstructedCallbacks)
            }
        }
        sitesToConstruct.clear()
    }

    @Override
    fun createSite(id: String, init: Site.() -> Unit = {}) = sitesToConstruct.add(SiteToConstruct(id, init))


    fun getSites(): List<Site> = registeredSites[this]!!.toList()

    override fun toString(): String {
        return "AppDefinition(definitionName='$definitionName', sites=${getSites()})"
    }

}

internal fun getAppDefinitionDependency(appDefinition: AppDefinition) = if (appDefinition.dependency.isNullOrBlank())
    null else ApplicationContextUtils.getInstance().context?.getBeansOfType(AppDefinition::class.java)?.values?.filter {
        it.definitionName == appDefinition.dependency
    }?.firstOrNull()
