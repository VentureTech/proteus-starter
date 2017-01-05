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
import java.util.*
import javax.annotation.PostConstruct


data class Hostname(val address: String, val welcomePage: Page)

class Site(id: String) : IdentifiableParent<Page>(id), ContentContainer, ResourceCapable {
    private val contentToRemoveImplementation: MutableList<Content> = mutableListOf()
    override val contentToRemove: MutableList<Content>
        get() = contentToRemoveImplementation
    override val contentList: MutableList<Content> get() = content
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()

    val emailTemplates = mutableListOf<EmailTemplate<*>>()
    val hostnames = mutableListOf<Hostname>()
    val templates = mutableListOf<Template>()
    val layouts = mutableListOf<Layout>()
    val content = mutableListOf<Content>()
    val pagesToRemove = mutableListOf<Page>()
    var primaryLocale: Locale = Locale.ENGLISH
    var defaultTimezone: TimeZone = TimeZone.getTimeZone("US/Central")
    lateinit var parent: SiteDefinition
    internal val siteConstructedCallbacks = mutableListOf<(Site) -> Unit>()

    fun getContentById(existingId: String): Content {
        val predicate = createContentIdPredicate(existingId)
        return children.flatMap { it.contentList }.filter(predicate).firstOrNull() ?:
            templates.flatMap { it.contentList }.filter(predicate).firstOrNull() ?:
            content.filter(predicate).first()
    }

    fun <T : Content> content(content: T, init: T.() -> Unit = {}): T {
        this.content.add(content)
        content.parent = this
        content.apply(init)
        if(content.path.isNotBlank())
            content.path = resolvePlaceholders(content.path)
        return content
    }

    fun content(existingContentId: String): Content {
        return getContentById(existingContentId)
    }

    fun page(id: String, path: String, init: Page.() -> Unit) =
        Page(id = id, site = this@Site, path = resolvePlaceholders(path)).apply(init)

    fun Page.remove() = pagesToRemove.add(this)

    fun template(id: String, init: Template.() -> Unit) = Template(id, this@Site).apply(init)

    fun layout(id: String, init: Layout.() -> Unit) = Layout(id, this@Site).apply(init)

    fun hostname(name: String, welcomePageId: String) {
        val page = children.filter { it.id == welcomePageId }.first()
        hostnames.add(Hostname(name, page))
    }

    fun hostname(name: String, welcomePageId: String, init: Page.() -> Unit) {
        val page = children.filter { it.id == welcomePageId }.firstOrNull() ?: Page(welcomePageId, this)
        page.apply(init)
        if (page.path.isBlank()) throw IllegalStateException("Missing path")
        hostnames.add(Hostname(name, page))
    }

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

abstract class SiteDefinition(val definitionName: String, val version: Int) {
    companion object {
        internal val registeredSites = mutableMapOf<SiteDefinition, MutableList<Site>>()
    }
    data class SiteToConstruct(val id: String, val init: Site.() -> Unit = {})

    @Autowired
    @Qualifier("standalone")
    lateinit var placeholderHelper: PlaceholderHelper
    private val sitesToConstruct = mutableListOf<SiteToConstruct>()


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
        return "SiteDefinition(definitionName='$definitionName', sites=${getSites()})"
    }

}
