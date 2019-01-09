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

package com.example.app.kprofile.ui.visibility

import com.example.app.kotlin.cellRenderer
import com.example.app.kotlin.nullFirst
import com.example.app.kotlin.toTextSource
import com.example.app.kprofile.model.membership.operation.MembershipOperationDataType
import com.example.app.profile.model.ProfileDAO
import com.example.app.profile.model.membership.MembershipOperation
import com.example.app.profile.model.user.UserDAO
import com.example.app.profile.ui.visibility.MembershipOperationVisibilityConditionText
import com.i2rd.cms.visibility.StandardVisibilityConditionEditor
import com.i2rd.cms.visibility.VisibilityConditionEditor
import com.i2rd.cms.visibility.VisibilityConditionInstance
import com.i2rd.cms.visibility.VisibilityConditionRenderChain
import com.i2rd.contentmodel.def.ModelDefinition
import com.i2rd.contentmodel.def.ModelField
import net.proteusframework.cms.PageElement
import net.proteusframework.cms.component.generator.Cacheable
import net.proteusframework.cms.controller.CmsRequest
import net.proteusframework.core.locale.TextSource
import net.proteusframework.internet.http.Scope
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor
import net.proteusframework.ui.miwt.util.CommonButtonText
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.stereotype.Component


/**
 * Membership Operation Visibility Condition
 * @author Alan Holt (aholt@proteus.co)
 * @since 11/14/2018
 */
@Component
@Configurable
class MembershipOperationVisibilityCondition : VisibilityConditionInstance() {
    companion object {
        const val FIELD_CHECKER = "MembershipOperationChecker"
        const val FIELD_MEMBERSHIP_OPERATION = "MembershipOperation"
    }

    // Can't put these in the constructor because apparently a VisibilityCondition needs to have a no-arg constructor.
    @Autowired
    lateinit var checkers: List<MembershipOperationChecker>
    @Autowired
    lateinit var profileDAO: ProfileDAO
    @Autowired
    lateinit var userDAO: UserDAO

    val membershipOperationCheckerDataType: MembershipOperationCheckerDataType by lazy {
        MembershipOperationCheckerDataType(checkers)
    }
    val membershipOperationDataType: MembershipOperationDataType by lazy {
        MembershipOperationDataType(profileDAO)
    }

    inner class Editor : StandardVisibilityConditionEditor() {

        private val selectedChecker: MembershipOperationChecker?
            get () = if (checkers.size <= 1) checkers.firstOrNull() else operationCheckerEditor.uiValue

        private val membershipOperationOptions: List<MembershipOperation?>
            get () = (selectedChecker?.fetchOperations() ?: listOf()).nullFirst()

        private val operationCheckerEditor: ComboBoxValueEditor<MembershipOperationChecker?> by lazy {
            ComboBoxValueEditor(
                "Profile(s)".toTextSource(),
                checkers.nullFirst(),
                null
            ).apply {
                cellRenderer { it?.displayName ?: "Please Select" }
                isVisible = checkers.size > 1
            }
        }

        private val mopEditor: ComboBoxValueEditor<MembershipOperation?> by lazy {
            ComboBoxValueEditor(
                "Operation".toTextSource(),
                membershipOperationOptions,
                null
            ).apply {
                cellRenderer { it?.name ?: CommonButtonText.PLEASE_SELECT }
                operationCheckerEditor.valueComponent.addActionListener {
                    val options = membershipOperationOptions
                    setOptions(options)
                    if (value !in options) value = null
                    else valueComponent.selectedObject = value
                }
            }
        }

        override fun createUIElement(field: ModelField): ValueEditor<*>? =
            when (field.programmaticName) {
                FIELD_CHECKER              -> operationCheckerEditor
                FIELD_MEMBERSHIP_OPERATION -> mopEditor
                else                       -> null
            }
    }

    override fun getName(): TextSource = "Membership Operation Visibility Condition".toTextSource()

    override fun getDescription(): TextSource? =
        "Visibility Condition for checking a membership operation on one or more profiles".toTextSource()

    override fun getConfiguration() = ModelDefinition().apply {
        addField(FIELD_CHECKER, membershipOperationCheckerDataType).apply {
            label = MembershipOperationVisibilityConditionText.LABEL_FIELD_CHECKER()
        }
        addField(FIELD_MEMBERSHIP_OPERATION, membershipOperationDataType).apply {
            label = MembershipOperationVisibilityConditionText.LABEL_FIELD_MEMBERSHIP_OPERATION()
        }
    }

    override fun getEditor(): VisibilityConditionEditor = Editor()

    override fun getCacheable(instance: VisibilityConditionInstance, request: CmsRequest<PageElement>): Cacheable<PageElement> =
        getCacheableBuilder(instance, request).setScope(if (request.principal == null) Scope.APPLICATION else Scope.SESSION)
            .setExpireTime(0).makeCacheable()

    private fun getConfiguredChecker(
        instance: VisibilityConditionInstance
    ) =
        when {
            checkers.size <= 1 -> checkers.firstOrNull()
            else               -> instance.configurationDataMap.let { map ->
                map[FIELD_CHECKER]?.let {
                    it as MembershipOperationChecker
                }
            }
        }

    private fun getConfiguredMembershipOperation(
        instance: VisibilityConditionInstance,
        checker: MembershipOperationChecker?
    ) =
        when (checker) {
            null -> null
            else -> {
                val operations = checker.fetchOperations()
                when {
                    operations.size <= 1 -> operations.firstOrNull()
                    else                 -> instance.configurationDataMap.let { map ->
                        map[FIELD_MEMBERSHIP_OPERATION].let {
                            operations.find { mo ->
                                mo.programmaticIdentifier == (it as MembershipOperation?)?.programmaticIdentifier
                            }
                        }
                    }
                }
            }
        }

    private fun getConfiguredInfo(
        instance: VisibilityConditionInstance
    ) =
        getConfiguredChecker(instance)?.let { checker ->
            getConfiguredMembershipOperation(instance, checker)?.let { mo ->
                checker to mo
            }
        }

    override fun isVisible(instance: VisibilityConditionInstance, request: CmsRequest<PageElement>): Boolean {
        return getConfiguredInfo(instance)?.let { (checker, op) ->
            checker.checkPermissions(request, op, profileDAO, userDAO)
        } ?: false
    }

    override fun renderConfiguredDescription(instance: VisibilityConditionInstance, chain: VisibilityConditionRenderChain) {
        chain.appendable.apply {
            append(getConfiguredInfo(instance)?.let { (checker, op) ->
                checker.getDescription(op, chain.localeContext)
            } ?: "Not Configured")
        }
    }

}