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
import com.example.app.config.automation.site.profile.basic.ProfileBasicDSL.Companion.SITE_FRONTEND
import com.example.app.profile.ui.ApplicationFunctions.Company.Location
import net.proteusframework.dsl.AppDefinition
import net.proteusframework.dsl.AppFunctionPage
import net.proteusframework.dsl.content.ApplicationFunction
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private const val appName = ProjectInformation.APPLICATION_NAME

@Profile("automation")
@Component
open class ProfileBasicLocationsDSL :
    AppDefinition(DEFINITION_NAME, version = 1, siteId = SITE_FRONTEND, dependency = ProfileBasicDSL.DEFINITION_NAME, init = {

    for((appFunction, path, htmlClassName) in listOf(
        AppFunctionPage(Location.MANAGEMENT, "/location/manage", "location-management"),
        AppFunctionPage(Location.EDIT, "/location/edit/*", "location-editor"),
        AppFunctionPage(Location.VIEW, "/location/view/*", "location-viewer")
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
}) {
    companion object {
        internal const val DEFINITION_NAME = "${ProfileBasicDSL.DEFINITION_NAME} - Locations"
    }
}