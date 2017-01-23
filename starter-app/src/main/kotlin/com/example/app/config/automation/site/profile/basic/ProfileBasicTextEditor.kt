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
import experimental.cms.dsl.AppDefinition
import experimental.cms.dsl.content.TextEditor
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private val packageName = ProjectInformation::class.java.name.substring(0, (ProjectInformation::class.java.name.length) -
    (".config.ProjectInformation".length))

@Profile("automation")
@Component
open class ProfileBasicTextEditor: AppDefinition(DEFINITION_NAME, 1, siteId = SITE_FRONTEND,
    dependency = ProfileBasicDSL.DEFINITION_NAME, init = {

    page("Text Management", "/\${folder.company}/manage-text") {
        template("Frontend")
        permission("Frontend Access")
        authenticationPage("Login")
        css("page--manage-text.css")

        content("Body", TextEditor("Text Editor")) {
            htmlClass = "text-management"
            symbolPrefix(packageName)
        }
    }

}) {
    companion object {
        internal const val DEFINITION_NAME = "${ProfileBasicDSL.DEFINITION_NAME} - Text Editor"
    }
}
