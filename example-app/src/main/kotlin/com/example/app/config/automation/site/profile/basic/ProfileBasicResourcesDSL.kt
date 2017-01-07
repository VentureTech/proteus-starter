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
import com.example.app.profile.ui.ApplicationFunctions.Company.Resource
import experimental.cms.dsl.AppDefinition
import experimental.cms.dsl.AppFunctionPage
import experimental.cms.dsl.content.ApplicationFunction
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private val appName = ProjectInformation.getName()
@Profile("automation")
@Component
open class ProfileBasicResourcesDSL : AppDefinition("Profile Basic - Resources", 1, "${appName} Frontend", {

    for((appFunction, path, htmlClassName) in listOf(
        AppFunctionPage(Resource.MANAGEMENT, "/resource/manage", "resource-management"),
        AppFunctionPage(Resource.EDIT, "/resource/edit/*", "resource-editor"),
        AppFunctionPage(Resource.VIEW, "/resource/view/*", "resource-viewer")
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
})