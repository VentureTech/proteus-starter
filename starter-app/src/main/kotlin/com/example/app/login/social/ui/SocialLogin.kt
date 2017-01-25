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

package com.example.app.login.social.ui

import com.google.common.collect.ArrayListMultimap
import com.i2rd.cms.scripts.impl.ScriptableRedirectType
import com.i2rd.cms.visibility.VisibilityConditionInstance
import com.i2rd.lib.Library
import com.i2rd.lib.LibraryConfiguration
import experimental.cms.dsl.*
import net.proteusframework.cms.PageElementModelImpl
import net.proteusframework.cms.component.ContentElement

/**
 * Social Login DSL Content
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/20/17
 */
class SocialLogin(id: String) : Identifiable(id), Content {
    override var visibilityCondition: VisibilityConditionInstance? = null
    override var path: String = ""
    override var htmlId: String = ""
    override var htmlClass: String = ""
    override val cssPaths = mutableListOf<String>()
    override val javaScriptPaths = mutableListOf<String>()
    override var parent: Any? = null

    private var _landingPage: Page? = null
    private var _scriptedRedirect: Script? = null
    private var _scriptedRedirectParameters = ArrayListMultimap.create<String, Any>()
    private var _overrideDynamicReturnPage: Boolean = false
    private var _loginService: String? = null
    private var _providers = mutableListOf<String>()
    private var _mode: SocialLoginMode? = null
    private var _additionalProperties = mutableMapOf<String, String?>()

    fun landingPage(landingPageId: String) {
        getSite().siteConstructedCallbacks.add{ site ->
            _landingPage = site.getExistingPage(landingPageId)
        }
    }

    fun scriptedRedirect(file: String) {
        _scriptedRedirect = Script(ScriptType.LoginRedirect, file)
    }

    fun scriptedRedirectParam(parameterName: String, parameterValue: Any) {
        _scriptedRedirectParameters.put(parameterName, parameterValue)
    }

    fun overrideDynamicReturn(overRide: Boolean) {
        _overrideDynamicReturnPage = overRide
    }

    fun loginService(serviceIdentifier: String) {
        _loginService = serviceIdentifier
    }

    fun provider(providerIdentifier: String) {
        _providers.add(providerIdentifier)
    }

    fun mode(loginmode: SocialLoginMode) {
        _mode = loginmode
    }

    fun additionalProperty(property: String, value: String?) {
        _additionalProperties.put(property, value)
    }

    override fun createInstance(helper: ContentHelper, existing: ContentElement?): ContentInstance {
        val contentElement = existing ?: SocialLoginElement()
        helper.assignToSite(PageElementModelImpl.StandardIdentifier(SocialLoginElement::class.java).toIdentifier())
        val builder = SocialLoginContentBuilder()
        updateBuilder(builder, helper)
        return ContentInstance(contentElement, builder.content)
    }

    override fun isModified(helper: ContentHelper, contentElement: ContentElement): Boolean {
        val builder = SocialLoginContentBuilder.load(contentElement.publishedData[helper.getCmsSite().primaryLocale], false)
        updateBuilder(builder, helper)
        return builder.isDirty
    }

    private fun updateBuilder(builder: SocialLoginContentBuilder, helper: ContentHelper) {
        val lpToCheck = _landingPage
        if(lpToCheck != null)
            builder.landingPage = helper.getCMSLink(lpToCheck)
        val redirect = _scriptedRedirect
        if(redirect != null) {
            @Suppress("UNCHECKED_CAST")
            val library = helper.createLibrary(id, redirect.file, redirect.type.modelName) as Library<ScriptableRedirectType>?
            if(library != null) {
                var lc = helper.getLibraryConfiguration(library)
                if(lc == null) {
                    lc = LibraryConfiguration<ScriptableRedirectType>(library)
                    helper.saveLibraryConfiguration(lc)
                    builder.scriptedRedirectInstance = lc.id
                } else {
                    builder.scriptedRedirectInstance = lc.id
                }
                helper.setScriptParameters(lc, _scriptedRedirectParameters)
                helper.saveLibraryConfiguration(lc)
            }
        } else {
            builder.scriptedRedirectInstance = 0
        }
        builder.isOverrideDynamicReturnPage = _overrideDynamicReturnPage
        builder.loginServiceIdentifier = _loginService
        builder.providerProgrammaticNames = _providers
        builder.mode = _mode
        _additionalProperties.keys.forEach { builder.setProperty(it, _additionalProperties[it]) }
    }
}