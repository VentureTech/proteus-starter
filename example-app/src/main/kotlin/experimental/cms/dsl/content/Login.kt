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

import com.i2rd.cms.bean.LoginBean
import com.i2rd.cms.bean.LoginBeanContentBuilder
import com.i2rd.xml.XsdConstants
import experimental.cms.dsl.Content
import experimental.cms.dsl.ContentHelper
import experimental.cms.dsl.Identifiable
import experimental.cms.dsl.Page
import net.proteusframework.cms.component.ContentElement

class Login(id: String)
    : Identifiable(id), Content {

    var landingPage: Page? = null
    var forgotPasswordText = ""
    var resetPasswordText = ""
    var titleText = ""

    fun landingPage(landingPageId: String) {
        getSite().siteConstructedCallbacks.add({ site ->
            val page = site.children.filter { it.id == landingPageId }.first()
            landingPage = page
        })
    }

    override fun createInstance(helper: ContentHelper): ContentElement {
        val contentElement = LoginBean()
        val builder = LoginBeanContentBuilder()
        updateBuilder(builder, helper)
        val dataSet = builder.content
        dataSet.contentElement = contentElement
        contentElement.dataVersions.add(dataSet)
        return contentElement
    }

    override fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean {
        val builder = LoginBeanContentBuilder.load(contentElement.publishedData[helper.getCmsSite().primaryLocale], false)
        updateBuilder(builder, helper)
        return builder.isDirty
    }

    private fun updateBuilder(builder: LoginBeanContentBuilder, helper: ContentHelper) {
        builder.isAllowRenderOnInsecurePage = false
        val lpToCheck = landingPage
        if (lpToCheck != null)
            builder.landing = helper.getCMSLink(lpToCheck)
        builder.setData(LoginBean.ContentPurpose.forgotten_password, forgotPasswordText, XsdConstants.XHTML_FRAGMENT_XSD)
        builder.setData(LoginBean.ContentPurpose.reset_password, resetPasswordText, XsdConstants.XHTML_FRAGMENT_XSD)
        builder.setData(LoginBean.ContentPurpose.login_form_title, titleText, XsdConstants.XHTML_FRAGMENT_XSD)
    }


    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null

    override fun toString(): String {
        return "Login(" +
            "landingPage=$landingPage," +
            "forgotPasswordText='$forgotPasswordText'," +
            "resetPasswordText='$resetPasswordText'," +
            "titleText='$titleText'," +
            "path='$path'," +
            "htmlId='$htmlId'," +
            "htmlClass='$htmlClass'," +
            "cssPaths=$cssPaths," +
            "javaScriptPaths=$javaScriptPaths" +
            ")"
    }


}