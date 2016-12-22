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

import net.proteusframework.cms.component.page.PageProperties
import net.proteusframework.cms.component.page.layout.BoxDescriptor


class Box(id: String, var boxType: BoxDescriptor = BoxDescriptor.COLUMN,
          var defaultContentArea: PageProperties.Type = PageProperties.Type.page,
          override var htmlId: String="", override var htmlClass: String="")
    : IdentifiableParent<Box>(id), HTMLClass, HTMLIdentifier {
    fun box(id: String, init: Box.() -> Unit) {
        add(Box(id)).apply(init)
        boxType = BoxDescriptor.ENCLOSING
    }

    override fun toString(): String {
        return "Box(id='$id', boxType=$boxType, defaultContentArea=$defaultContentArea, htmlId='$htmlId', " +
                "htmlClass='$htmlClass', children=$children)"
    }

}

class Layout(id: String, val parent: Site) : IdentifiableParent<Box>(id){
    init {
        if(id.isNotBlank())
            parent.layouts.add(this)
    }
    fun box(id: String, init: Box.() -> Unit) {
        add(Box(id)).apply(init)
    }

    override fun toString(): String {
        return "Layout(id='$id', children=$children, parent=${parent.id})"
    }

}

interface BoxedContent : ContentContainer {
    override val contentList: List<Content> get() = content.values.toList()
    val content: MutableMap<Box, Content>
    val site: Site
    val layout: Layout

    fun <T : Content> content(boxId: String, content: T, init: T.() -> Unit={}): T {
        val box = layout.children.filter { it.id == boxId }.first()
        this.content[box] = content
        content.parent = this
        content.apply(init)
        return content
    }
    fun content(boxId: String, existingContentId: String): Content {
        val box = layout.children.filter { it.id == boxId }.first()
        val contentElement = site.getContentById(existingContentId)
        this.content[box] = contentElement
        return contentElement
    }

}


class Template(id: String, override val site: Site, override var layout: Layout = Layout("", site))
    : Identifiable(id), ResourceCapable, HTMLIdentifier, BoxedContent {
    override var htmlId: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override val content = mutableMapOf<Box, Content>()
    override val contentToRemove = mutableListOf<Content>()
    init {
        if(id.isNotBlank())
            site.templates.add(this)
    }

    fun layout(id: String, init: Layout.() -> Unit): Unit {
        layout = if (id.isBlank() && this@Template.id.isNotBlank())
            Layout("${this@Template.id}-${id}", site)
        else
            Layout(id, site)
        layout.apply(init)
    }

    fun layout(existingId: String): Unit {
        layout = site.layouts.filter({ it.id == existingId }).first()
    }

    override fun toString(): String {
        return "Template(id='$id', parent=${site.id}, layout=$layout, htmlId='$htmlId')"
    }


}

class Page(id: String, override val site: Site, var template: Template = Template("", site), var path: String = "")
    : Identifiable(id), ResourceCapable, BoxedContent {
    override val layout: Layout get() = template.layout
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override val content = mutableMapOf<Box, Content>()
    override val contentToRemove = mutableListOf<Content>()
    var pagePermission: String? = null
    var authenticationPage: Page? = null

    init {
        if(id.isNotBlank())
            site.add(this)
    }
    fun template(id: String, init: Template.() -> Unit): Unit {
        template = Template(id, site)
        template.apply(init)
    }

    fun template(existingId: String): Unit {
        template = site.templates.filter({ it.id == existingId }).first()
    }

    fun permission(permission: String) {
        pagePermission = permission
    }
    fun authenticationPage(authenticationPageId: String) {
        val page = site.children.filter { it.id == authenticationPageId }.first()
        authenticationPage = page
    }


    override fun toString(): String {
        return "Page(id='$id', parent=${site.id}, template=$template, path='$path', content=$content)"
    }
}

