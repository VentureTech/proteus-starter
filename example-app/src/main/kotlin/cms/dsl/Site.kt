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

package cms.dsl

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component


data class Hostname(val name: String, val welcomePage: Page)

class Site(id: String) : IdentifiableParent<Page>(id) {
    val hostnames = mutableListOf<Hostname>()
    val templates = mutableListOf<Template>()
    val layouts = mutableListOf<Layout>()
    operator fun invoke(id:String="", body: Site.() -> Unit = {}){
        if(id.isNotBlank()) this@Site.id = id
        body()
    }
    fun page(id:String, path:String, init: Page.() -> Unit) = Page(id, this@Site, path = path).apply(init)

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
