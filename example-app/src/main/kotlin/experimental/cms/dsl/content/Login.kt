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
import com.i2rd.cms.scripts.impl.ScriptableRedirectType
import com.i2rd.lib.Library
import com.i2rd.lib.LibraryConfiguration
import com.i2rd.xml.XsdConstants
import experimental.cms.dsl.*
import net.proteusframework.cms.component.ContentElement

class Login(id: String) : Identifiable(id), Content {

    private var landingPage: Page? = null
    var forgotPasswordText = ""
    var resetPasswordText = ""
    var titleText = ""
    private var scriptedRedirect: Script? = null

    fun landingPage(landingPageId: String) {
        getSite().siteConstructedCallbacks.add({ site ->
            val page = site.children.filter { it.id == landingPageId }.first()
            landingPage = page
        })
    }

    fun scriptedRedirect(file: String) {
        scriptedRedirect = Script(ScriptType.LoginRedirect, file)
    }

    override fun createInstance(helper: ContentHelper, existing: ContentElement?): ContentInstance {
        val contentElement = existing ?: LoginBean()
        val builder = LoginBeanContentBuilder()
        updateBuilder(builder, helper)
        return ContentInstance(contentElement, builder.content)
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
        val redirect = scriptedRedirect
        if(redirect != null) {
            @Suppress("UNCHECKED_CAST")
            val library = helper.createLibrary(id, redirect.file, redirect.type.modelName) as Library<ScriptableRedirectType>?
            if(library != null) {
                var lc = helper.getLibraryConfiguration(library)
                if(lc == null) {
                    lc = LibraryConfiguration<ScriptableRedirectType>(library)
                    helper.saveLibraryConfiguration(lc)
                    builder.scriptInstance = lc.getId()
                }
            }
        }
        else {
            builder.scriptInstance = 0
        }
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