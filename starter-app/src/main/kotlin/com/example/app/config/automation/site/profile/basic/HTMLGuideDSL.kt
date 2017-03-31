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
import com.example.app.profile.ui.ApplicationFunctions.Client
import com.example.app.guide.HTMLGuide
import experimental.cms.dsl.AppDefinition
import experimental.cms.dsl.AppFunctionPage
import experimental.cms.dsl.content.ApplicationFunction
import experimental.cms.dsl.content.MIWT
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("automation")
@Component
open class HTMLGuideDSL :
    AppDefinition(DEFINITION_NAME, version = 1, siteId = SITE_FRONTEND, dependency = ProfileBasicDSL.DEFINITION_NAME, init = {

        page("HTMLGuide", "html-guide") {
            template("Frontend")
            content("Body", MIWT("HTML Guide", HTMLGuide::class.java))
        }

}) {
    companion object {
        internal const val DEFINITION_NAME = "HTML Guide"
    }
}