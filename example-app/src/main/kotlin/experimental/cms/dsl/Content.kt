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

internal fun createContentIdPredicate(existingId: String):  (Content) -> Boolean = { it.id == existingId }

interface ContentContainer {
    val contentList: List<Content>
    val contentToRemove: MutableList<Content>

    fun Content.remove() = contentToRemove.add(this)
}

interface Content : HTMLIdentifier, HTMLClass, ResourceCapable {
    val id: String
    var parent: Any?
}

class Text(id: String, var htmlContent: String= "")
    : Identifiable(id), Content {
    override var htmlId: String=""
    override var htmlClass: String=""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null

    override fun toString(): String {
        return "Text(" +
                "htmlContent='$htmlContent'," +
                "htmlId='$htmlId'," +
                "htmlClass='$htmlClass'," +
                "cssPaths=$cssPaths," +
                "javaScriptPaths=$javaScriptPaths" +
                ")"
    }


}

class ApplicationFunction(id: String)
    : Identifiable(id), Content {
    override var htmlId: String=""
    override var htmlClass: String=""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null

    var registerLink: Boolean = true

    override fun toString(): String {
        return "ApplicationFunction(" +
                "htmlId='$htmlId'," +
                "htmlClass='$htmlClass'," +
                "cssPaths=$cssPaths," +
                "registerLink=$registerLink," +
                "javaScriptPaths=$javaScriptPaths" +
                ")"
    }
}
