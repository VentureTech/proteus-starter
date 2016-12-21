import cms.dsl.ApplicationFunction
import cms.dsl.SiteDefinition
import cms.dsl.Text
import com.example.app.ui.ApplicationFunctions
import net.proteusframework.cms.component.page.PageProperties.Type.page
import net.proteusframework.cms.component.page.PageProperties.Type.page_template
import net.proteusframework.cms.component.page.layout.BoxDescriptor.*

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

open class CAPMCCloud(definitionName: String = "CapMC Cloud") : SiteDefinition(definitionName) {
    override val site = createSite("CapMC Frontend") {

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
                htmlContent = """<h1 class="logo"><span>Capstone Mail Compliance</span></h1>"""
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

        hostname("capmc-\${install}.venturetech.net", "Home") {
            path = "/home"
            template("Login")
            content("Content", Text("Please Login")) {
                htmlContent = """<p>Please Login</p>"""
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
    }
}

fun main(args: Array<String>) {
    val capmcCloud = CAPMCCloud()
    println(capmcCloud)
}