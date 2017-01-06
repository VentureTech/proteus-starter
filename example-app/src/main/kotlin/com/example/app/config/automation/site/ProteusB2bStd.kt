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

package com.example.app.config.automation.site

import com.example.app.config.ProjectInformation
import com.example.app.profile.ui.ApplicationFunctions
import experimental.cms.dsl.AppDefinition
import experimental.cms.dsl.content.*
import net.proteusframework.cms.component.page.PageProperties.Type.page
import net.proteusframework.cms.component.page.PageProperties.Type.page_template
import net.proteusframework.cms.component.page.layout.BoxDescriptor.*
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
private val appName = ProjectInformation.getName()
@Profile("automation")
@Component
open class ProteusB2bStd : AppDefinition("Proteus B2b Standard", 1, "$appName Frontend", {
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
        content("Content", Composite("Login-Composite")) {
            content(Text("Text1")) {
                //language=HTML
                htmlContent = """<p> Hi! </p>"""
            }
            content(Text("Text2")) {
                //language=HTML
                htmlContent = """<p> Bye! </p>"""
            }
        }
    }

    page("Home", "/home") {
        template("Frontend")
        permission("Frontend Access")
        authenticationPage("Login")
    }

    page("Company Management", "/\${folder.company}/manage") {
        template("Frontend")
        permission("Frontend Access")
        authenticationPage("Login")

        content("Content", ApplicationFunction(ApplicationFunctions.Company.MANAGEMENT)) {
            htmlClass = "company-management"
        }
    }

    page("Company Viewer", "/\${folder.company}/view/*") {
        template("Frontend")
        permission("Frontend Access")
        authenticationPage("Login")

        content("Content", ApplicationFunction(ApplicationFunctions.Company.VIEW)) {
            htmlClass = "company-viewer"
        }
    }

    page("Company Editor", "/\${folder.company}/edit/*") {
        template("Frontend")
        permission("Frontend Access")
        authenticationPage("Login")

        content("Content", ApplicationFunction(ApplicationFunctions.Company.EDIT)) {
            htmlClass = "company-editor"
        }
    }

    page("Site Setup", "/site/setup") {
        template("Frontend")
        permission("Frontend Access")
        authenticationPage("Login")

        content("Content", Text("Setup Instructions")) {
            htmlContent = """<h1 class="site-setup-instructions">
In order to use the Starter Site, a default Company must be created. Please create one now.
                </h1>"""
        }
        content("Content", ApplicationFunction(ApplicationFunctions.StarterSite.SETUP)) {
            htmlClass = "site-setup"
        }
    }

    page("User Management", "/user/manage") {
        template("Frontend")
        permission("Frontend Access")
        authenticationPage("Login")

        content("Content", ApplicationFunction(ApplicationFunctions.User.MANAGEMENT)) {
            htmlClass = "user-management"
        }
    }

    page("User Viewer", "/user/view/*") {
        template("Frontend")
        permission("Frontend Access")
        authenticationPage("Login")

        content("Content", ApplicationFunction(ApplicationFunctions.User.VIEW)) {
            htmlClass = "user-viewer"
        }
    }

    page("User Editor", "/user/edit/*") {
        template("Frontend")
        permission("Frontend Access")
        authenticationPage("Login")

        content("Content", ApplicationFunction(ApplicationFunctions.User.EDIT)) {
            htmlClass = "user-editor"
        }
    }

    hostname("\${proteus.install.name}.\${base.domain}", "Home")
})