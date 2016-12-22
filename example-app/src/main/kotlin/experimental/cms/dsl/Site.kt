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

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*


data class Hostname(val name: String, val welcomePage: Page)

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
    var primaryLocale = Locale.ENGLISH
    var defaultTimezone = TimeZone.getTimeZone("US/Central")

    operator fun invoke(id:String="", body: Site.() -> Unit = {}){
        if(id.isNotBlank()) this@Site.id = id
        body()
    }

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

    fun page(id:String, path:String, init: Page.() -> Unit) = Page(id, this@Site, path = path).apply(init)

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

private val registeredSites = mutableListOf<Site>()


@Profile("automation")
@Component
open class SiteDefinition(val definitionName: String, open val site: Site? = null) {
    fun createSite(id: String, init: Site.() -> Unit = {}): Site {
        val site = Site(id).apply(init)
        registeredSites.add(site)
        return site
    }
    override fun toString(): String {
        return "SiteDefinition(definitionName='$definitionName', site=$site)"
    }
}
