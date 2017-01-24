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

package com.example.app.config.automation.site.profile.basic

import com.example.app.config.ProjectInformation
import com.example.app.login.oneall.service.OneAllLoginService
import com.example.app.login.social.ui.SocialLogin
import com.example.app.login.social.ui.SocialLoginMode
import com.example.app.profile.ui.ApplicationFunctions
import experimental.cms.dsl.AppDefinition
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private val appName = ProjectInformation.APPLICATION_NAME

@Profile("automation")
@Component
open class ProfileBasicOneAllDSL :
    AppDefinition("Profile Basic - OneAll", version = 1, siteId = "$appName Frontend", dependency = "Profile Basic", init = {
    page("Login", "/login") {
        template("Login")
        content("Body", SocialLogin("OneAll Social Login")) {
            htmlClass = "oneall-social-login"
            landingPage("Dashboard")
            scriptedRedirect("StarterSiteRedirectScript.groovy")
            scriptedRedirectParam("Default Redirect Page", "/dashboard")
            loginService(OneAllLoginService.SERVICE_IDENTIFIER)
            provider("google")
            provider("facebook")
            mode(SocialLoginMode.Login)
            //You can enable SSO by uncommenting the line below:
//            additionalProperty(OneAllLoginService.PROP_SSO_ENABLED, "true")
        }
    }

    page(ApplicationFunctions.User.MY_ACCOUNT_VIEW, "/account/my-profile") {
        template("Frontend")
        content("Body", SocialLogin("OneAll Link")) {
            htmlClass = "oneall-link"
            loginService(OneAllLoginService.SERVICE_IDENTIFIER)
            provider("google")
            provider("facebook")
            mode(SocialLoginMode.Link)
        }
    }
})