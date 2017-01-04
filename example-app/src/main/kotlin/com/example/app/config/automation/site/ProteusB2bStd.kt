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

import com.example.app.ui.ApplicationFunctions
import experimental.cms.dsl.SiteDefinition
import experimental.cms.dsl.content.*
import net.proteusframework.cms.component.page.PageProperties.Type.page
import net.proteusframework.cms.component.page.PageProperties.Type.page_template
import net.proteusframework.cms.component.page.layout.BoxDescriptor.*

open class ProteusB2bStd : SiteDefinition("Proteus B2b Standard", version = 1) {
    init {
        createSite("\${app.name} Frontend") {
            template("Login") {
                javascript("templates/main.min.js")
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
                javascript("templates/main.min.js")
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
                content("Content", Login("Login"))
            }

            page("Home", "/home") {
                template("Frontend")
                pagePermission = "Frontend Access"
                authenticationPage("Login")
            }

            page("Company Management", "/\${folder.company}/manage") {
                template("Frontend")
                pagePermission = "Frontend Access"
                authenticationPage("Login")

                content("Content", ApplicationFunction(ApplicationFunctions.Company.MANAGEMENT)) {
                    htmlClass = "company-management"
                }
            }

            page("Company Viewer", "/\${folder.company}/view/*") {
                template("Frontend")
                pagePermission = "Frontend Access"
                authenticationPage("Login")

                content("Content", ApplicationFunction(ApplicationFunctions.Company.VIEW)) {
                    htmlClass = "company-viewer"
                }
            }

            page("Company Editor", "/\${folder.company}/edit/*") {
                template("Frontend")
                pagePermission = "Frontend Access"
                authenticationPage("Login")

                content("Content", ApplicationFunction(ApplicationFunctions.Company.EDIT)) {
                    htmlClass = "company-editor"
                }
            }

            page("Site Setup", "/site/setup") {
                template("Frontend")
                pagePermission = "Frontend Access"
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

            hostname("\${proteus.install.name}.\${base.domain}", "Home")
        }
    }
}