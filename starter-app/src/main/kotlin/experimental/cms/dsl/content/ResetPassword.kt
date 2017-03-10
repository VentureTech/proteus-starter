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

import com.i2rd.cms.bean.ResetPasswordBean
import com.i2rd.cms.bean.ResetPasswordBeanContentBuilder
import com.i2rd.cms.visibility.VisibilityConditionInstance
import experimental.cms.dsl.*
import net.proteusframework.cms.PageElementModelImpl
import net.proteusframework.cms.component.ContentElement
import net.proteusframework.email.EmailTemplate

/**
 * Reset Password Content.
 * @author Russ Tennant (russ@venturetech.net)
 */
class ResetPassword(id: String): Identifiable(id), Content {

    private lateinit var loginPage: Page
    var ignoreDynamicReturnPath = false
    /** Programmatic name of EmailTemplate */
    var emailTemplate = "default-reset-password"
    var authenticationMeans = ResetPasswordBean.AuthenticationMeans.EMAIL_OR_USER_NAME

    fun loginPage(existingPageId: String) {
        getSite().siteConstructedCallbacks.add({site ->
            val page = site.getExistingPage(existingPageId)
            loginPage = page
        })
    }

    override fun createInstance(helper: ContentHelper, existing: ContentElement?): ContentInstance {
        val contentElement = existing?:ResetPasswordBean()
        val builder = ResetPasswordBeanContentBuilder()
        updateBuilder(contentElement, builder, helper)
        helper.assignToSite(PageElementModelImpl.StandardIdentifier(ResetPasswordBean::class.java).toIdentifier())
        return ContentInstance(contentElement, builder.content)
    }

    override fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean {
        val dataSet = contentElement.publishedData[helper.getCmsSite().primaryLocale] ?: return false
        val builder = ResetPasswordBeanContentBuilder.load(dataSet, false)
        updateBuilder(contentElement, builder, helper)
        return builder.isDirty
    }

    private fun updateBuilder(contentElement: ContentElement,
        builder: ResetPasswordBeanContentBuilder, helper: ContentHelper) {
        builder.authenticationMeans = authenticationMeans
        builder.isOverrideDynamicReturn = ignoreDynamicReturnPath
        builder.loginLink = helper.getCMSLink(loginPage)
        if(emailTemplate.isNotBlank()) {
            val template: EmailTemplate? = helper.getEmailTemplate(emailTemplate)
            builder.emailTemplate = template
            val emailConfig = template?.emailConfig
// PF-1625
//            if(emailConfig is ContentElementEmailConfig)
//                emailConfig.contentElement = contentElement
        }
    }

    override var visibilityCondition: VisibilityConditionInstance? = null
    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null
}
