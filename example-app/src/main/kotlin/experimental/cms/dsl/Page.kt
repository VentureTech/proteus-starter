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

interface BoxedContent {
    val content: MutableMap<Box, Content>
    val contentToRemove: MutableList<Content>
    val parent: Site
    val layout: Layout

    fun <T : Content> content(boxId: String, content: T, init: T.() -> Unit={}): T {
        val box = layout.children.filter { it.id == boxId }.first()
        this.content[box] = content
        content.apply(init)
        return content
    }
    fun content(boxId: String, existingContentId: String): Content {
        val box = layout.children.filter { it.id == boxId }.first()
        val predicate: (Content) -> Boolean = { it.id == existingContentId }
        val contentElement = parent.children.flatMap { it.content.values }.filter(predicate).firstOrNull()?:
            parent.templates.flatMap { it.content.values }.filter(predicate).first()
        this.content[box] = contentElement
        return contentElement
    }

    fun Content.remove() = contentToRemove.add(this)
}


class Template(id: String, override val parent: Site, override var layout: Layout = Layout("", parent))
    : Identifiable(id), ResourceCapable, HTMLIdentifier, BoxedContent {
    override var htmlId: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override val content = mutableMapOf<Box, Content>()
    override val contentToRemove = mutableListOf<Content>()
    init {
        if(id.isNotBlank())
            parent.templates.add(this)
    }

    fun layout(id: String, init: Layout.() -> Unit): Unit {
        layout = if (id.isBlank() && this@Template.id.isNotBlank())
            Layout("${this@Template.id}-${id}", parent)
        else
            Layout(id, parent)
        layout.apply(init)
    }

    fun layout(existingId: String): Unit {
        layout = parent.layouts.filter({ it.id == existingId }).first()
    }

    override fun toString(): String {
        return "Template(id='$id', parent=${parent.id}, layout=$layout, htmlId='$htmlId')"
    }


}

class Page(id: String, override val parent: Site, var template: Template = Template("", parent), var path: String = "")
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
            parent.add(this)
    }
    fun template(id: String, init: Template.() -> Unit): Unit {
        template = Template(id, parent)
        template.apply(init)
    }

    fun template(existingId: String): Unit {
        template = parent.templates.filter({ it.id == existingId }).first()
    }

    fun permission(permission: String) {
        pagePermission = permission
    }
    fun authenticationPage(authenticationPageId: String) {
        val page = parent.children.filter { it.id == authenticationPageId }.first()
        authenticationPage = page
    }


    override fun toString(): String {
        return "Page(id='$id', parent=${parent.id}, template=$template, path='$path', content=$content)"
    }
}

