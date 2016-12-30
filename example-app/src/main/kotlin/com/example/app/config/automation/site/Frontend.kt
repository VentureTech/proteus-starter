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

package com.example.app.config.automation.site/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

import com.example.app.ui.ApplicationFunctions
import experimental.cms.dsl.ApplicationFunction
import experimental.cms.dsl.SiteDefinition
import experimental.cms.dsl.Text
import net.proteusframework.cms.component.page.PageProperties.Type.page
import net.proteusframework.cms.component.page.PageProperties.Type.page_template
import net.proteusframework.cms.component.page.layout.BoxDescriptor.*
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("automation")
@Component
open class CAPMCCloud() : SiteDefinition("CapMC Cloud", version = 1) {
    init {
        createSite("CapMC Frontend") {

            template("Login") {
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
                content("Header", Text("Logo")) {
                    htmlContent = """<h1 class="logo"><span>Capstone Mail Compliance</span>
<img src="entropy/logo.png" alt="Logo" /></h1>"""
                    htmlId = "logo"
                }
                content("Footer", Text("Copyright Us")) {
                    htmlContent = """<p>Copyright Capstone Technologies LLC.</p>"""
                    htmlClass = "copyright"
                }
            }

            template("Frontend") {
                layout("Header, Main, Footer")
                content("Header", "Logo")
                content("Footer", "Copyright Us")
            }

            hostname("capmc.russ1-vdm.vipasuite.com", "Home") {
                path = "/home"
                template("Login")
                content("Content", Text("Please Login")) {
                    htmlContent = """<p>Please Login</p><p><a href="/config/user">User Mgt</a></p>"""
                    htmlClass = "please-login"
                }
            }


            page("User Management", "/config/user") {
                template("Frontend")
                content("Content", ApplicationFunction(ApplicationFunctions.User.MANAGEMENT)) {
                    htmlClass = "user-mgt"

                }
            }

            page("Resource Management", "/config/resource") {
                template("Frontend")
                content("Content", ApplicationFunction(ApplicationFunctions.ResourceRepositoryItem.MANAGEMENT)) {
                    htmlClass = "resource-mgt"
                    css("entropy/page/page--resource-mgt.css")
                }
            }

            content(Text("Fake Web Service", "Web Service Content")) {
                path = "/ws/fake-web-service"
            }
        }
    }
}

fun main(args: Array<String>) {
    val capmcCloud = CAPMCCloud()
    println(capmcCloud)
}