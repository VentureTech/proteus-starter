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

import net.proteusframework.email.EmailConfigType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URL
import java.util.*
import javax.annotation.PostConstruct


internal data class Hostname(val address: String, val welcomePage: Page)

class Site(id: String) : IdentifiableParent<Page>(id), ContentContainer, ResourceCapable {
    private val contentToRemoveImplementation: MutableList<Content> = mutableListOf()
    override val contentToRemove: MutableList<Content>
        get() = contentToRemoveImplementation
    override val contentList: MutableList<Content> get() = content
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()

    internal var webResources: URL? = null
    internal var libraryResources: URL? = null
    internal val roles = mutableListOf<Role>()
    internal val emailTemplates = mutableListOf<EmailTemplate<*>>()
    internal val hostnames = mutableListOf<Hostname>()
    internal val templates = mutableListOf<Template>()
    internal val layouts = mutableListOf<Layout>()
    internal val content = mutableListOf<Content>()
    internal val pagesToRemove = mutableListOf<Page>()
    internal var primaryLocale: Locale = Locale.ENGLISH
    internal var defaultTimezone: TimeZone = TimeZone.getTimeZone("US/Central")
    internal lateinit var parent: AppDefinition
    internal val siteConstructedCallbacks = mutableListOf<(Site) -> Unit>()

    internal fun getContentById(existingId: String): Content {
        val predicate = createContentIdPredicate(existingId)
        return children.flatMap { it.contentList }.filter(predicate).firstOrNull() ?:
            templates.flatMap { it.contentList }.filter(predicate).firstOrNull() ?:
            content.filter(predicate).first()
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
        this.content.add(content)
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
        this.content.add(contentById)
        return contentById
    }

    /**
     * Add a page to the site.
     * @param id the identifier. Something like "User Management".
     * @param path the page path. Page paths can contain spring environment variables like "\${folder.company}/users".
     */
    fun page(id: String, path: String, init: Page.() -> Unit) =
        Page(id = id, site = this, path = resolvePlaceholders(path)).apply(init)

    /**
     * Remove a defined Page.
     * @receiver the page to remove.
     */
    fun Page.remove() = pagesToRemove.add(this)

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
        val page = children.filter { it.id == existingWelcomePageId }.first()
        hostnames.add(Hostname(address, page))
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
        hostnames.add(Hostname(address, page))
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
    open class MyAppDefinition() : AppDefinition("My Cool Application", version = 1) {
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
abstract class AppDefinition(val definitionName: String, val version: Int, val siteId: String, val init: Site.() -> Unit){

    companion object {
        internal val registeredSites = mutableMapOf<AppDefinition, MutableList<Site>>()
    }

    data class SiteToConstruct(val id: String, val init: Site.() -> Unit = {})

    constructor(definitionName: String, version: Int): this(definitionName, version, "", {})

    @Autowired
    @Qualifier("standalone")
    lateinit var placeholderHelper: PlaceholderHelper
    private val sitesToConstruct = mutableListOf<SiteToConstruct>()


    init {
        if(siteId.isNotBlank())
            sitesToConstruct.add(SiteToConstruct(siteId, init))
    }

    @PostConstruct
    fun postConstruct() {
        sitesToConstruct.forEach {
            val site = Site(it.id)
            site.parent = this
            site.apply(it.init)
            registeredSites.getOrPut(this, { mutableListOf<Site>() }).add(site)
            for (callback in site.siteConstructedCallbacks) {
                callback.invoke(site)
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


