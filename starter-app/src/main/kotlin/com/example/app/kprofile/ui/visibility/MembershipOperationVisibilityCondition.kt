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
        const val FIELD_FETCHER = "ProfileFetcher"
        const val FIELD_FETCH_ENTRY = "ProfileFetchEntry"
        const val FIELD_MEMBERSHIP_OPERATION = "MembershipOperation"
    }

    // Can't put these in the constructor because apparently a VisibilityCondition needs to have a no-arg constructor.
    @Autowired
    lateinit var fetchers: List<ProfileFetcher>
    @Autowired
    lateinit var profileDAO: ProfileDAO

    val profileFetcherDataType: ProfileFetcherDataType by lazy {
        ProfileFetcherDataType(fetchers)
    }
    val profileFetchEntryDataType: ProfileFetchEntryDataType by lazy {
        ProfileFetchEntryDataType(fetchers)
    }
    val membershipOperationDataType: MembershipOperationDataType by lazy {
        MembershipOperationDataType(profileDAO)
    }

    inner class Editor : StandardVisibilityConditionEditor() {

        private val fetcherEditor: ComboBoxValueEditor<ProfileFetcher?>? by lazy {
            ComboBoxValueEditor(
                "Fetcher".toTextSource(),
                fetchers.nullFirst(),
                if (fetchers.size <= 1) fetchers.firstOrNull() else null
            ).apply {
                cellRenderer { it?.displayName ?: "Please Select" }
                isVisible = fetchers.size > 1
            }
        }

        private fun getSelectedFetcher(
            defaultFetcher: ProfileFetcher? = null
        ): ProfileFetcher? = fetcherEditor?.uiValue ?: defaultFetcher

        private fun getFetchEntryOptions(
            defaultFetcher: ProfileFetcher? = null
        ): List<ProfileFetchEntryRelationship?> =
            (getSelectedFetcher(defaultFetcher)?.run { entries.map { it.toRelationship(this) } } ?: listOf()).nullFirst()

        private val selectedFetchEntry: ProfileFetchEntryRelationship?
            get () = fetchEntryEditor.uiValue

        private val membershipOperationOptions: List<MembershipOperation?>
            get () = (selectedFetchEntry?.fetchEntry?.fetchOperations() ?: listOf()).nullFirst()

        private val fetchEntryEditor: ComboBoxValueEditor<ProfileFetchEntryRelationship?> by lazy {
            ComboBoxValueEditor(
                "Profile(s)".toTextSource(),
                getFetchEntryOptions(fetchers.firstOrNull()),
                null
            ).apply {
                cellRenderer { it?.fetchEntry?.displayName ?: "Please Select" }
                fetcherEditor?.let {
                    it.valueComponent.addActionListener { _ ->
                        val options = getFetchEntryOptions()
                        setOptions(options)
                        value = (options.find { o ->
                            o?.fetchEntry?.programmaticName == value?.fetchEntry?.programmaticName
                        } ?: if (options.size <= 1) options.firstOrNull() else null)
                        isVisible = options.size > 1
                    }
                }
            }
        }

        private val mopEditor: ComboBoxValueEditor<MembershipOperation?> by lazy {
            ComboBoxValueEditor(
                "Operation".toTextSource(),
                membershipOperationOptions,
                null
            ).apply {
                cellRenderer { it?.name ?: CommonButtonText.PLEASE_SELECT }
                fetchEntryEditor.valueComponent.addActionListener { _ ->
                    val options = membershipOperationOptions
                    setOptions(options)
                    if (value !in options) value = null
                }
            }
        }

        override fun createUIElement(field: ModelField): ValueEditor<*>? =
            when (field.programmaticName) {
                FIELD_FETCHER              -> fetcherEditor
                FIELD_FETCH_ENTRY          -> fetchEntryEditor
                FIELD_MEMBERSHIP_OPERATION -> mopEditor
                else                       -> null
            }
    }

    override fun getName(): TextSource = "Membership Operation Visibility Condition".toTextSource()

    override fun getDescription(): TextSource? =
        "Visibility Condition for checking a membership operation on one or more profiles".toTextSource()

    override fun getConfiguration() = ModelDefinition().apply {
        addField(FIELD_FETCHER, profileFetcherDataType).apply {
            label = MembershipOperationVisibilityConditionText.LABEL_FIELD_FETCHER()
        }
        addField(FIELD_FETCH_ENTRY, profileFetchEntryDataType).apply {
            label = MembershipOperationVisibilityConditionText.LABEL_FIELD_FETCHENTRY()
        }
        addField(FIELD_MEMBERSHIP_OPERATION, membershipOperationDataType).apply {
            label = MembershipOperationVisibilityConditionText.LABEL_FIELD_MEMBERSHIP_OPERATION()
        }
    }

    override fun getEditor(): VisibilityConditionEditor = Editor()

    override fun getCacheable(instance: VisibilityConditionInstance, request: CmsRequest<PageElement>): Cacheable<PageElement> =
        getCacheableBuilder(instance, request).setScope(if (request.principal == null) Scope.APPLICATION else Scope.SESSION)
            .setExpireTime(0).makeCacheable()

    private fun getConfiguredFetcher(instance: VisibilityConditionInstance) =
        when {
            fetchers.size <= 1 -> fetchers.firstOrNull()
            else               -> instance.configurationDataMap.let { map ->
                map[FIELD_FETCHER].let {
                    fetchers.find { f -> f.programmaticName == (it as ProfileFetcher?)?.programmaticName }
                }
            }
        }

    private fun getConfiguredFetchEntry(
        instance: VisibilityConditionInstance,
        fetcher: ProfileFetcher?
    ) =
        when {
            fetcher == null           -> null
            fetcher.entries.size <= 1 -> fetcher.entries.firstOrNull()
            else                      -> instance.configurationDataMap.let { map ->
                map[FIELD_FETCH_ENTRY].let {
                    fetcher.getEntry((it as ProfileFetchEntryRelationship?)?.fetchEntry?.programmaticName)
                }
            }
        }

    private fun getConfiguredMembershipOperation(
        instance: VisibilityConditionInstance,
        fetchEntry: ProfileFetchEntry?
    ) =
        when (fetchEntry) {
            null -> null
            else -> {
                val operations = fetchEntry.fetchOperations()
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
        getConfiguredFetcher(instance)?.let { fetcher ->
            getConfiguredFetchEntry(instance, fetcher)?.let { fetchEntry ->
                getConfiguredMembershipOperation(instance, fetchEntry)?.let { mo ->
                    Triple(fetcher, fetchEntry, mo)
                }
            }
        }

    override fun isVisible(instance: VisibilityConditionInstance, request: CmsRequest<PageElement>): Boolean {
        return getConfiguredInfo(instance)?.let { (fetcher, fetchEntry, mop) ->
            fetcher.checkPermissions(request, fetchEntry, mop)
        } ?: false
    }

    override fun renderConfiguredDescription(instance: VisibilityConditionInstance, chain: VisibilityConditionRenderChain) {
        chain.appendable.apply {
            append(getConfiguredInfo(instance)?.let { (_, fetchEntry, mop) ->
                fetchEntry.getDescription(mop, chain.localeContext)
            } ?: "Not Configured")
        }
    }

}