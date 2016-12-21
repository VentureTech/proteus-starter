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

interface Content : HTMLIdentifier, HTMLClass, ResourceCapable {
    val id: String
}

class Text(id: String, var htmlContent: String= "")
    : Identifiable(id), Content {
    override var htmlId: String=""
    override var htmlClass: String=""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()

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

    override fun toString(): String {
        return "ApplicationFunction(" +
                "htmlId='$htmlId'," +
                "htmlClass='$htmlClass'," +
                "cssPaths=$cssPaths," +
                "javaScriptPaths=$javaScriptPaths" +
                ")"
    }
}
