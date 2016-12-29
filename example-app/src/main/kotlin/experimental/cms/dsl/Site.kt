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

import java.util.*


data class Hostname(val address: String, val welcomePage: Page)

class Site(id: String) : IdentifiableParent<Page>(id), ContentContainer {
    private val contentToRemoveImplementation: MutableList<Content> = mutableListOf()
    override val contentToRemove: MutableList<Content>
        get() = contentToRemoveImplementation
    override val contentList: List<Content> get() = content
    val hostnames = mutableListOf<Hostname>()
    val templates = mutableListOf<Template>()
    val layouts = mutableListOf<Layout>()
    val content = mutableListOf<Content>()
    val pagesToRemove = mutableListOf<Page>()
    var primaryLocale: Locale = Locale.ENGLISH
    var defaultTimezone: TimeZone = TimeZone.getTimeZone("US/Central")

    fun getContentById(existingId: String): Content {
        val predicate = createContentIdPredicate(existingId)
        return children.flatMap { it.content.values }.filter(predicate).firstOrNull()?:
                templates.flatMap { it.content.values }.filter(predicate).firstOrNull()?:
                content.filter(predicate).first()
    }

    fun <T : Content> content(content: T, init: T.() -> Unit={}): T {
        this.content.add(content)
        content.parent = this
        return content.apply(init)
    }
    fun content(existingContentId: String): Content {
        return getContentById(existingContentId)
    }

    fun page(id:String, path:String, init: Page.() -> Unit) = Page(id = id, site = this@Site, path = path).apply(init)

    fun Page.remove() = pagesToRemove.add(this)

    fun template(id:String, init: Template.() -> Unit) = Template(id, this@Site).apply(init)

    fun layout(id:String, init: Layout.() -> Unit) = Layout(id, this@Site).apply(init)

    fun hostname(name: String, welcomePageId: String)  {
        val page = children.filter { it.id == welcomePageId }.first()
        hostnames.add(Hostname(name, page))
    }
    fun hostname(name: String, welcomePageId: String, init: Page.() -> Unit)  {
        val page = children.filter { it.id == welcomePageId }.firstOrNull()?: Page(welcomePageId, this)
        page.apply(init)
        if(page.path.isBlank()) throw IllegalStateException("Missing path")
        hostnames.add(Hostname(name, page))
    }

    override fun toString(): String {
        return "Site(" +
                "id='$id', pages=$children," +
                "hostnames=$hostnames," +
                "templates=$templates," +
                "layouts=$layouts" +
                ")"
    }

}


open class SiteDefinition(val definitionName: String, val version: Int) {
    companion object {
        internal val registeredSites = mutableMapOf<SiteDefinition, MutableList<Site>>()
    }
    fun createSite(id: String, init: Site.() -> Unit = {}): Site {
        val site = Site(id).apply(init)
        registeredSites.getOrPut(this, { mutableListOf<Site>() }).add(site)
        return site
    }

    fun getSites(): List<Site> = registeredSites[this]!!.toList()

    override fun toString(): String {
        return "SiteDefinition(definitionName='$definitionName', sites=${getSites()})"
    }
}
