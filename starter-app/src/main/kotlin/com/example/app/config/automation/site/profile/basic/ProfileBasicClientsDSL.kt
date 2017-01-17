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
import com.example.app.profile.ui.ApplicationFunctions.Client
import experimental.cms.dsl.AppDefinition
import experimental.cms.dsl.AppFunctionPage
import experimental.cms.dsl.content.ApplicationFunction
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private val appName = ProjectInformation.APPLICATION_NAME

@Profile("automation")
@Component
open class ProfileBasicClientsDSL :
    AppDefinition("Profile Basic - Clients", version = 1, siteId = "$appName Frontend", dependency = "Profile Basic", init = {
    for((appFunction, path, htmlClassName) in listOf(
        AppFunctionPage(Client.MANAGEMENT, "/\${folder.client}/manage", "client-management"),
        AppFunctionPage(Client.EDIT, "/\${folder.client}/edit/*", "client-editor"),
        AppFunctionPage(Client.VIEW, "/\${folder.client}/view/*", "client-viewer")
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
})