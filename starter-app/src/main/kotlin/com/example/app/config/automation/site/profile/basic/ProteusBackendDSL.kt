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
import com.example.app.login.social.ui.SocialLogin
import experimental.cms.dsl.AppDefinition
import experimental.cms.dsl.content.Composite
import experimental.cms.dsl.content.HTML
import experimental.cms.dsl.content.Text
import net.proteusframework.cms.login.oneall.service.OneAllLoginService
import net.proteusframework.cms.login.social.ui.SocialLoginMode
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private val appName = ProjectInformation.APPLICATION_NAME

@Profile("automation")
@Component
open class ProteusBackendDSL :
    AppDefinition(DEFINITION_NAME, version = 1, siteId = SITE_BACKEND, init = {

    page("Dashboard", "/config/dashboard") { }

    page("Login", "/account/login") {
        template("Login")
        content("Primary Content", Text("Proteus Logo - Login Process")) {
            htmlContent = """<div><img alt="Proteus Framework"
            src="/_resources/dyn/files/1059150z219b19a5/_fn/proteus-white-text.png" width="225" /></div>"""
            htmlClass = "site-logo"
        }
        content("Primary Content", Composite("Login process container")) {
            htmlClass = "login-process"

            content(SocialLogin("Social Login")) {
                htmlClass = "social-login"
                landingPage("Dashboard")
                loginService(OneAllLoginService.SERVICE_IDENTIFIER)
                provider("google")
                mode(SocialLoginMode.Login)
                additionalProperty(OneAllLoginService.PROP_SSO_ENABLED, "true")
            }
        }
        content("Primary Content", Text("Downloads")) {
            htmlContent = """<h2>Download</h2>

<ul>
    <li><a href="/_resources/dyn/files/1062206z27680b5d/_fn/Proteus101.pdf" target="_blank">Proteus 101 Guide</a>&nbsp;(PDF)</li>
    <li><a href="/_resources/dyn/files/1062205zbe615ae7/_fn/Proteus101.epub" target="_blank">Proteus 101 Guide</a>&nbsp;(EPUB)</li>
</ul>
"""
            htmlClass = "downloadables"
        }
        content("Primary Content", HTML("Shared Copyright")) {
            htmlContent = """<div class="product">Product of
            <a href="http://www.venturetech.net" target="_blank" class="venturetech">
            <span class="text">VentureTech</span></a></div>
<div class="made">Made in <span class="nebraska" title="Nebraska">Nebraska</span></div>"""
            htmlClass = "copyright"
        }
        content("Primary Content", SocialLogin("OneAll Social Login")).remove()
    }

    page("My Preferences", "/account/preferences") {
        template("Neptune - Protected - No Primary Content Styles")
        content("Primary Content", SocialLogin("OneAll Social Link")) {
            htmlClass = "oneall-link"
            loginService(OneAllLoginService.SERVICE_IDENTIFIER)
            provider("google")
            mode(SocialLoginMode.Link)
        }
    }

    hostname("\${proteus.install.name}-admin.\${base.domain}", "Dashboard")
}) {
    companion object {
        internal const val DEFINITION_NAME = "Proteus Backend"
        internal const val SITE_BACKEND = "Administration site, backend, " +
            "for managing content for this installation of Proteus Framework."
    }
}