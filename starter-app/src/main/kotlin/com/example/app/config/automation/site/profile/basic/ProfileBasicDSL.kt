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
import com.example.app.profile.ui.ApplicationFunctions.*
import com.example.app.support.service.AppUtil
import com.i2rd.cms.bean.ResetPasswordBean.AuthenticationMeans.EMAIL_ONLY
import com.i2rd.cms.visibility.AuthenticationCondition
import experimental.cms.dsl.AppDefinition
import experimental.cms.dsl.AppFunctionPage
import experimental.cms.dsl.content.*
import net.proteusframework.cms.component.page.PageProperties.Type.page
import net.proteusframework.cms.component.page.PageProperties.Type.page_template
import net.proteusframework.cms.component.page.layout.BoxDescriptor.*
import net.proteusframework.email.config.ContentElementEmailConfigType
import net.proteusframework.users.model.AuthenticationMethodSecurityLevel.SHARED_IDENTIFIER
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URL

private val appName = ProjectInformation.APPLICATION_NAME

@Profile("automation")
@Component
open class ProfileBasicDSL : AppDefinition("Profile Basic", version = 1, siteId = "$appName Frontend", init = {
    libraryResources(URL("https://repo.venturetech.net/artifactory/vt-snapshot-local/" +
        "com/example/starter-app/\${LATEST}/starter-app-\${LATEST}-libraries.zip"))
    webResources(URL("https://repo.venturetech.net/artifactory/simple/vt-snapshot-local/" +
        "com/example/starter-app-webdev/\${LATEST}/starter-app-webdev-\${LATEST}.zip"))

    storeSitePreference(AppUtil.PREF_KEY_PROFILE_SITE)

    template("Login") {
        javaScript("vendor/jquery.min.js")
        javaScript("vendor/tether.min.js")
        javaScript("vendor/bootstrap.min.js")
        javaScript("vendor/select2.min.js")
        javaScript("templates/main.min.js")
        css("templates/template--base.min.css")
        css("pages/page--login.min.css")
        layout("Header, Main, Footer") {
            box("Enclosing") {
                htmlId = "e-content"
                htmlClass = "enclosing"
                box("Header") {
                    htmlId = "header"
                    defaultContentArea = page_template
                    boxType = HEADER
                }
                box("Body") {
                    htmlId = "body"
                    defaultContentArea = page
                    boxType = MAIN
                }
            }
            box("Footer") {
                htmlId = "footer"
                defaultContentArea = page_template
                boxType = FOOTER
            }
        }
        content("Header", HTML("Logo & Menu Toggle")){
            htmlContent = """<a href="/" style="width: 12rem;"><img src="application-logo.png" alt="logo" class="logo"/>
</a><div class="fa fa-bars"></div>"""
            htmlClass = "logo-con"
        }
        content("Footer", Text("Global Footer")){
            htmlContent = "<p>$appName | &copy; 2016</p>"
        }
    }

    template("Frontend") {
        javaScript("vendor/jquery.min.js")
        javaScript("vendor/tether.min.js")
        javaScript("vendor/bootstrap.min.js")
        javaScript("vendor/select2.min.js")
        javaScript("templates/main.min.js")
        css("templates/template--base.min.css")
        layout("Header, Main, Footer")
        content("Header", "Logo & Menu Toggle")
        content("Header", ScriptedGenerator("Company Selector")) {
            script("ScriptGenerator/Frontend/CompanySelectorScript.groovy")
        }
        content("Header", ScriptedGenerator("Top Menu")) {
            script("ScriptGenerator/Frontend/StarterSiteMenuScript.groovy")
        }
        content("Header", Logout("Logout")) {
            htmlClass = "menu"
            htmlId = "logout"
        }
        content("Footer", "Global Footer")
    }

    page("Login", "/login") {
        template("Login")
        content("Body", Composite("Login Group")) {
            content(Text("Already Logged-In Text")) {
                htmlContent = "<p>You're logged in. Click <a href=\"/dashboard\">HERE</a> to return to $appName.</p>"
                htmlClass = "logged-in"
                val authenticationCondition = AuthenticationCondition()
                val dataMap = authenticationCondition.configurationDataMap
                dataMap[AuthenticationCondition.FIELD_SECURITY_LEVEL] = SHARED_IDENTIFIER
                dataMap.update()
                visibilityCondition = authenticationCondition
            }
            content(Login("Login")) {
                htmlClass = "form-signin"
                titleText = "<p>Sign In Now</p>"
                forgotPasswordText = """<p><a href="/password-reset">Forgot your password?</a></p>"""
                landingPage("Dashboard")
                scriptedRedirect("ScriptableRedirect/StarterSiteRedirectScript.groovy")
            }
        }
    }

    emailTemplate(ContentElementEmailConfigType::class.java, "Reset Password", "default-reset-password") {
        to = "\${recipient.findEmailAddress()}"
        from = "\"$appName\" <noreply@venturetech.net>"
        replyTo = from
        subject = "$appName: Reset Your  Password"
        htmlContent = """<p>Dear ${"$"}{(recipient.name.first)!''} ${"$"}{(recipient.name.last)!''},</p>

<p>To reset your password for $appName, please <a href="${"$"}{action_url!validation_default}">click here</a>.</p>

<p>$appName</p>
<a href="/dashboard"><img src="application-logo.png" alt="logo" class="logo"/></a>
"""
    }

    page("Password Reset", "/password-reset") {
        template("Login")
        content("Body", Composite("Reset Password Group")) {
            htmlClass = "retrieve-password-con"
            content(HTML("Reset Password Instructions")) {
                htmlContent = """<div class="title">Reset Password</div>
<p >To reset your password, please provide the e-mail address stored for your user account. If the e-mail address
match an existing user account, then an email message will be sent with additional instructions.</p>"""
            }
            content(ResetPassword("Reset Password")){
                authenticationMeans = EMAIL_ONLY
                loginPage("Login")
                ignoreDynamicReturnPath = true
            }
        }
    }

    page("Dashboard", "/dashboard") {
        template("Frontend")
        permission("Frontend Access")
        authenticationPage("Login")
        content("Body", Text("Dashboard Heading")) {
            htmlContent = "<h1>Dashboard</h1>"
        }
    }

    for((appFunction, path, htmlClassName) in listOf(
        AppFunctionPage(Company.MANAGEMENT, "/\${folder.company}/manage",
            "company-management"),
        AppFunctionPage(Company.VIEW, "/\${folder.company}/view/*", "company-viewer"),
        AppFunctionPage(Company.EDIT, "/\${folder.company}/edit/*", "company-editor"),
        AppFunctionPage(StarterSite.SETUP, "/site/setup", "site-setup"),
        AppFunctionPage(User.MANAGEMENT, "/user/manage", "user-management"),
        AppFunctionPage(User.VIEW, "/user/view/*", "user-viewer"),
        AppFunctionPage(User.EDIT, "/user/edit/*", "user-editor")
     )) {

        page(appFunction, path) {
            template("Frontend")
            permission("Frontend Access")
            authenticationPage("Login")

            content("Body", ApplicationFunction(appFunction)) {
                htmlClass = htmlClassName
            }
        }
    }

    hostname("\${proteus.install.name}.\${base.domain}", "Dashboard")
})

