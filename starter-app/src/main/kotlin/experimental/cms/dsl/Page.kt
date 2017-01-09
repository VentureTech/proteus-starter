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
import net.proteusframework.cms.permission.PagePermission
import net.proteusframework.core.StringFactory.convertToProgrammaticName2
import net.proteusframework.users.model.AuthenticationMethodSecurityLevel
import net.proteusframework.users.model.AuthenticationMethodSecurityLevel.SHARED_IDENTIFIER
import net.proteusframework.users.model.AuthenticationMethodSecurityLevel.SHARED_SECRET
import net.proteusframework.users.model.CredentialPolicyLevel

/**
 * Box model
 */
class Box(id: String, var boxType: BoxDescriptor = BoxDescriptor.COLUMN,
          var defaultContentArea: PageProperties.Type = PageProperties.Type.page,
          override var htmlId: String="", override var htmlClass: String="")
    : IdentifiableParent<Box>(id), HTMLClass, HTMLIdentifier {

    /**
     * Add a box as a child to this box.
     * @param id the identifier of the child box.
     * @param init the initialization block.
     */
    fun box(id: String, init: Box.() -> Unit) {
        add(Box(id)).apply(init)
        boxType = BoxDescriptor.ENCLOSING
    }

    override fun toString(): String {
        return "Box(id='$id', boxType=$boxType, defaultContentArea=$defaultContentArea, htmlId='$htmlId', " +
                "htmlClass='$htmlClass', children=$children)"
    }

}

/**
 * Layout model.
 */
class Layout(id: String, val parent: Site) : IdentifiableParent<Box>(id){
    init {
        if(id.isNotBlank())
            parent.layouts.add(this)
    }
    /**
     * Add a Box to the layout.
     * This is only used when a new Layout is created. It will not be used to modify an existing layout
     * with new Boxes / removed Boxes.
     * @param id the identifier for the new Box.
     * @param init the initialization block.
     */
    fun box(id: String, init: Box.() -> Unit) {
        add(Box(id)).apply(init)
    }

    override fun toString(): String {
        return "Layout(id='$id', children=$children, parent=${parent.id})"
    }

}

/**
 * BoxedContent model.
 */
interface BoxedContent : ContentContainer {
    override val contentList: MutableList<Content> get() = content.values.flatMap { it }.toMutableList()
    val content: MutableMap<Box, MutableList<Content>>
    val site: Site
    val layout: Layout

    /**
     * Find the index of content in a Box.
     * @param box the box.
     * @param contentId existing content identifier.
     */
    fun indexOf(box: Box, contentId: String): Int = content[box]!!.indexOfFirst(createContentIdPredicate(contentId))

    /**
     * Add content to the specified box.
     * @param boxId the box identifier.
     * @param content the content to add to the box.
     * @param init the initialization block.
     */
    fun <T : Content> content(boxId: String, content: T, init: T.() -> Unit={}): T {
        site.siteConstructedCallbacks.add({site ->
            val box = layout.children.filter { it.id == boxId }.first()
            this.content.getOrPut(box, {mutableListOf<Content>()}).add(content)
            content.parent = this
            content.apply(init)
        })
        return content
    }

    /**
     * Add content to the specified box.
     * @param boxId the box identifier.
     * @param existingContentId the existing content's identifier.
     */
    fun content(boxId: String, existingContentId: String): Unit {
        site.siteConstructedCallbacks.add({ site ->
            val box = layout.children.filter { it.id == boxId }.first()
            val contentElement = site.getContentById(existingContentId)
            this.content.getOrPut(box, { mutableListOf<Content>() }).add(contentElement)
        })
    }

}

/**
 * Template Model.
 */
class Template(id: String, override val site: Site, override var layout: Layout = Layout("", site))
    : Identifiable(id), ResourceCapable, HTMLIdentifier, BoxedContent {
    override var htmlId: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override val content = mutableMapOf<Box, MutableList<Content>>()
    override val contentToRemove = mutableListOf<Content>()
    init {
        if(id.isNotBlank())
            site.templates.add(this)
    }

    /**
     * Set the layout on this template.
     * @param id the identifier for the layout.
     * @param init the initialization block.
     */
    fun layout(id: String, init: Layout.() -> Unit): Unit {
        layout = Layout(id, site)
        layout.apply(init)
    }

    /**
     * Set the layout on this template.
     * @param existingId existing layout's identifier.
     */
    fun layout(existingId: String): Unit {
        layout = site.layouts.filter({ it.id == existingId }).first()
    }

    override fun toString(): String {
        return "Template(id='$id', parent=${site.id}, layout=$layout, htmlId='$htmlId')"
    }


}

/** Page Model. */
class Page(id: String, override val site: Site, override var path: String = "", var template: Template = Template("", site))
    : Identifiable(id), ResourceCapable, BoxedContent, PathCapable {
    override val layout: Layout get() = template.layout
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override val content = mutableMapOf<Box, MutableList<Content>>()
    override val contentToRemove = mutableListOf<Content>()

    var title: String? = null
    /**
     * Internal Use.
     * @see permission
     */
    internal var pagePermission: Permission? = null
        private set
    /**
     * Internal Use.
     * @see authenticationPage
     */
    internal var authenticationPage: Page? = null
        private set

    init {
        if(id.isNotBlank())
            site.add(this)
    }

    /**
     * Set a template on this page.
     * @param id the template identifier.
     * @param init the initialization block.
     */
    fun template(id: String, init: Template.() -> Unit): Unit {
        template = Template(id, site)
        template.apply(init)
    }

    /**
     * Set a template on this page.
     * @param existingId the existing template's identifier.
     */
    fun template(existingId: String): Unit {
        site.siteConstructedCallbacks.add({site->
            template = site._getExistingTemplate(existingId)
        })
    }

    /**
     * Set the required page permission required to access this page.
     * @param name the permission name.
     * @param addToRole optional programmatic name of Role to add this permission to.
     * @param policyLevel password policy level.
     * @param minAuthenticationMethodSecurityLevel minimum authentication security level
     * @param maxAuthenticationMethodSecurityLevel maximum authentication security level
     */
    fun permission(name: String,
        addToRole: String = "",
        policyLevel: CredentialPolicyLevel = CredentialPolicyLevel.LOW,
        minAuthenticationMethodSecurityLevel: AuthenticationMethodSecurityLevel = SHARED_IDENTIFIER,
        maxAuthenticationMethodSecurityLevel: AuthenticationMethodSecurityLevel = SHARED_SECRET) {
        pagePermission = Permission(PagePermission::class.java, name, convertToProgrammaticName2(name)!!,
            addToRole = addToRole,
            policyLevel = policyLevel,
            minAuthenticationMethodSecurityLevel = minAuthenticationMethodSecurityLevel,
            maxAuthenticationMethodSecurityLevel =  maxAuthenticationMethodSecurityLevel)
    }

    /**
     * Set the authentication page to redirect to if the user isn't logged in.
     * @param authenticationPageId existing page's identifier.
     */
    fun authenticationPage(authenticationPageId: String) {
        site.siteConstructedCallbacks.add({ site ->
            authenticationPage = site._getExistingPage(authenticationPageId)
        })
    }


    override fun toString(): String {
        return "Page(id='$id', parent=${site.id}, template=$template, path='$path', content=$content)"
    }
}

