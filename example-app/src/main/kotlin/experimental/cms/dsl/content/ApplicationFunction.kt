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

package experimental.cms.dsl.content

import com.i2rd.cms.component.miwt.MIWTPageElementModel
import experimental.cms.dsl.Content
import experimental.cms.dsl.ContentHelper
import experimental.cms.dsl.ContentInstance
import experimental.cms.dsl.Identifiable
import net.proteusframework.cms.PageElementModelImpl
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.ui.management.ApplicationFunction

class ApplicationFunction(id: String)
    : Identifiable(id), Content {
    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null

    var registerLink: Boolean = true

    override fun createInstance(helper: ContentHelper, existing: ContentElement?): ContentInstance {
        if(existing != null)
            return ContentInstance(existing)
        val match = helper.getApplicationFunctions().filter {
            val annotation = it.javaClass.getAnnotation(ApplicationFunction::class.java)
            annotation.name == this@ApplicationFunction.id
        }.first() as MIWTPageElementModel
        val pageElementModel = helper.getMIWTPageElementModelFactory().getVirtualComponents(helper.getCmsSite()).filter {
            PageElementModelImpl.StandardIdentifier(it.identifier).virtualIdentifier == match.javaClass.name
        }.first()
        helper.assignToSite(pageElementModel.identifier)
        return ContentInstance(helper.getMIWTPageElementModelFactory().createInstance(pageElementModel))
    }

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
