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

import com.example.app.config.automation.site.profile.basic.ProfileBasicDSL.Companion.SITE_FRONTEND
import net.proteusframework.dsl.AppDefinition
import net.proteusframework.dsl.content.HTML
import net.proteusframework.internet.http.RequestError
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URL

@Profile("automation")
@Component
open class ProfileBasicErrorPagesDSL :
    AppDefinition(DEFINITION_NAME, version = 1, siteId = SITE_FRONTEND, dependency = ProfileBasicDSL.DEFINITION_NAME, init = {
        webResources(URL("https://repo.proteus.co/artifactory/simple/vt-snapshot-local/" +
            "com/example/starter-app-webdev/\${LATEST}/starter-app-webdev-\${LATEST}.zip"))

        template("Error") {
            javaScript("vendor/jquery.min.js")
            javaScript("vendor/tether.min.js")
            javaScript("vendor/bootstrap.min.js")
            javaScript("vendor/select2.min.js")
            javaScript("templates/main.min.js")
            css("vendor/select2.min.css")
            css("templates/template--base.min.css")
            css("templates/font-awesome.min.css")
            layout("Header, Body, Footer")
        }

        errorPage(RequestError.GENERAL, page("GeneralError", "/400-fallback"){
            template("Error")
            content("Body", HTML("400-Fallback Message", """
                <div class="error-page">
                    <h1>An error occurred.</h1>
                </div>
"""))
        })
        errorPage(RequestError.NOT_FOUND, page("NotFoundError", "/404"){
            template("Error")
            content("Body", HTML("404 Message", """
                <div class="error-page">
                    <h1>The resource your requested does not exist.</h1>
                </div>
"""))
        })
        errorPage(RequestError.FORBIDDEN, page("ForbiddenError", "/403"){
            template("Error")
            content("Body", HTML("401 Message", """
                <div class="error-page">
                    <h1>You don't have permission to view this page.</h1>
                </div>
"""))
        })
        errorPage(RequestError.BAD_REQUEST, page("BadRequest", "/400"){
            template("Error")
            content("Body", HTML("400 Message", """
                <div class="error-page">
                    <h1>Bad Request.</h1>
                </div>
"""))
        })
        errorPage(RequestError.CMS_ERROR, page("InternalError", "/500"){
            template("Error")
            content("Body", HTML("500 Message", """
                <div class="error-page">
                    <h1>Internal Server Error.</h1>
                </div>
"""))
        })
    }){
    companion object {
        internal const val DEFINITION_NAME = "${ProfileBasicDSL.DEFINITION_NAME} - Error Pages"
    }
}
