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
import experimental.cms.dsl.AppDefinition
import experimental.cms.dsl.AppFunctionPage
import experimental.cms.dsl.content.ApplicationFunction
import experimental.cms.dsl.content.Login
import experimental.cms.dsl.content.Logout
import experimental.cms.dsl.content.ScriptedGenerator
import net.proteusframework.cms.component.page.PageProperties.Type.page
import net.proteusframework.cms.component.page.PageProperties.Type.page_template
import net.proteusframework.cms.component.page.layout.BoxDescriptor.*
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URL

private val appName = ProjectInformation.getName()
@Profile("automation")
@Component
open class ProfileBasicDSL : AppDefinition("Profile Basic", 1, "${appName} Frontend", {
    libraryResources(URL("https://repo.venturetech.net/artifactory/vt-snapshot-local/" +
        "com/example/starter-app/1.2-SNAPSHOT/starter-app-1.2-SNAPSHOT-libraries.zip"))

    template("Login") {
        javaScript("templates/main.min.js")
        css("templates/template--base.min.css")
        layout("Header, Main, Footer") {
            box("Header") {
                htmlId = "header"
                defaultContentArea = page_template
                boxType = HEADER
            }
            box("Content") {
                htmlId = "main"
                defaultContentArea = page
                boxType = MAIN
            }
            box("Footer") {
                htmlId = "footer"
                defaultContentArea = page_template
                boxType = FOOTER
            }
        }
    }

    template("Frontend") {
        javaScript("templates/main.min.js")
        css("templates/template--base.min.css")
        layout("Header, Main, Footer")
        content("Header", ScriptedGenerator("Company Selector")) {
            script("ScriptGenerator/Frontend/CompanySelectorScript.groovy")
        }
        content("Header", ScriptedGenerator("Top Menu")) {
            script("ScriptGenerator/Frontend/StarterSiteMenuScript.groovy")
        }
        content("Header", Logout("Logout"))
    }

    page("Login", "/login") {
        template("Login")
        content("Content", Login("Login")) {
            scriptedRedirect("ScriptableRedirect/StarterSiteRedirectScript.groovy")
        }
    }

    page("Home", "/home") {
        template("Frontend")
        permission("Frontend Access")
        authenticationPage("Login")
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

            content("Content", ApplicationFunction(appFunction)) {
                htmlClass = htmlClassName
            }
        }
    }

    hostname("\${proteus.install.name}.\${base.domain}", "Home")
})