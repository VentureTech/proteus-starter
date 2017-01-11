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

import com.i2rd.cms.bean.contentmodel.CmsModelDataSet
import com.i2rd.cms.bean.scripted.ScriptedPageElementFactoryType
import com.i2rd.cms.generator.ScriptGeneratorType
import com.i2rd.cms.scripts.impl.ScriptableRedirectType
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.cms.component.editor.DefaultDelegatePurpose
import net.proteusframework.cms.component.editor.DelegatePurpose

/**
 * Script Type Enum.
 */
enum class ScriptType(val modelName: String) {
//    FreeMarker(FreeMarkerType.MODEL_NAME),
    ScriptedGenerator(ScriptGeneratorType.MODEL_NAME),
    ScriptedContent(ScriptedPageElementFactoryType.MODEL_NAME),
    LoginRedirect(ScriptableRedirectType.MODEL_NAME)
}

internal data class Script(val type:ScriptType, val file: String, val dependencies: Set<Script> = emptySet())

internal fun createContentIdPredicate(existingId: String): (Content) -> Boolean = { it.id == existingId }

interface ContentContainer {
    /** Internal Use. */
    val contentList: MutableList<Content>
    /** Internal Use. */
    val contentToRemove: MutableList<Content>

    /**
     * Remove Content.
     */
    fun Content.remove() = contentToRemove.add(this)
    fun typeName(): String = javaClass.simpleName
}
internal fun _getSite(toCheck: Any?): Site {
    return when (toCheck) {
        is Site -> toCheck
        is BoxedContent -> toCheck.site
        is Content -> _getSite(toCheck.parent)
        else -> throw IllegalStateException("Couldn't determine site")
    }
}
internal fun _getPage(toCheck: Any?): Page? {
    return when (toCheck) {
        is Page -> toCheck
        is Content -> _getPage(toCheck.parent)
        else -> null
    }
}

interface DelegateContent : ContentContainer {
    /**
     * Internal Use.
     * @see content
     */
    val contentPurpose: MutableMap<Content, DelegatePurpose>
    /** Internal Use. */
    var parent: Any?

    /**
     * Add delegate content.
     * @param content the Content to add.
     * @param purpose optional purpose. Default to NONE. This is Content Container specific.
     * @param init the initialization block.
     */
    fun <T : Content> content(content: T, purpose: DelegatePurpose = DefaultDelegatePurpose.NONE, init: T.() -> Unit={}): T {
        contentList.add(content)
        contentPurpose.put(content, purpose)
        content.parent = this
        return content.apply(init)
    }

    /**
     * Add delegate content.
     * @param existingContentId an existing Content's identifier.
     * @param purpose optional purpose. Default to NONE. This is Content Container specific.
     */
    fun content(existingContentId: String, purpose: DelegatePurpose = DefaultDelegatePurpose.NONE): Content {
        val contentById = _getSite(parent).getContentById(existingContentId)
        contentList.add(contentById)
        contentPurpose.put(contentById, purpose)
        return contentById
    }
}

data class ContentInstance(val contentElement: ContentElement, val dataSet: CmsModelDataSet? = null)

interface Content : HTMLIdentifier, HTMLClass, ResourceCapable, PathCapable {
    val id: String
    var parent: Any?

    fun getSite(): Site {
        return _getSite(parent)
    }

    fun getPage(): Page? {
        return _getPage(parent)
    }

    /**
     * Create a ContentInstance. If the implementation is configurable, populate
     * the [ContentInstance.dataSet] property with a value.
     * @param helper helper class for creating content.
     * @param existing optional existing content element to update.
     */
    fun createInstance(helper: ContentHelper, existing: ContentElement? = null): ContentInstance
    fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean = false
}


