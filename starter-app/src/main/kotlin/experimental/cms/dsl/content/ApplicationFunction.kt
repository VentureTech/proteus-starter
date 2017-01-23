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
import com.i2rd.cms.visibility.VisibilityConditionInstance
import experimental.cms.dsl.Content
import experimental.cms.dsl.ContentHelper
import experimental.cms.dsl.ContentInstance
import experimental.cms.dsl.Identifiable
import net.proteusframework.cms.PageElementModelImpl
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.ui.management.ApplicationFunction

internal data class RegisteredLink(val functionName: String, val functionContext: String = "",
    val pathInfoPattern: String = "")

open class ApplicationFunction(id: String, val functionName: String = id)
    : Identifiable(id), Content {
    override var visibilityCondition: VisibilityConditionInstance? = null
    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null

    internal var registeredLink = RegisteredLink(functionName)

    fun registeredLink(functionContext: String = "", pathInfoPattern: String = "") {
        registeredLink = RegisteredLink(id, functionContext, pathInfoPattern)
    }

    override fun createInstance(helper: ContentHelper, existing: ContentElement?): ContentInstance {
        var theLink = helper.getRegisteredLink(registeredLink.functionName, registeredLink.functionContext)
        val page = getPage()?:throw IllegalStateException("ApplicationFunction is not on a page")
        if(theLink == null){
            theLink = net.proteusframework.ui.management.link.RegisteredLink(
                helper.getCmsSite(),
                registeredLink.functionName,
                registeredLink.functionContext,
                helper.getCMSLink(page))
        } else {
            theLink.functionContext = registeredLink.functionContext
            theLink.pathInfoPattern = registeredLink.pathInfoPattern
            theLink.link = helper.getCMSLink(page)
        }
        helper.saveRegisteredLink(theLink)

        if (existing != null) return ContentInstance(existing)
        val match = helper.getApplicationFunctions().filter {
            val annotation = it.javaClass.getAnnotation(ApplicationFunction::class.java)
            annotation.name == functionName
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
            "registeredLink=$registeredLink," +
            "javaScriptPaths=$javaScriptPaths" +
            ")"
    }
}
