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

import com.example.app.kotlin.toTextSource
import com.example.app.kprofile.ui.visibility.ProfileFetchEntryDataDomain.withMembershipTest
import com.example.app.profile.model.Profile
import com.example.app.profile.model.membership.MembershipOperation
import com.i2rd.contentmodel.def.type.DataTypeImpl
import com.i2rd.contentmodel.def.type.PhysicalType
import com.i2rd.converter.Converter
import com.i2rd.converter.ConverterContext
import com.i2rd.converter.InconvertibleException
import com.i2rd.data.AbstractDataDomain
import net.proteusframework.cms.controller.CmsRequest
import net.proteusframework.core.locale.LocaleContext
import org.eclipse.birt.core.framework.Platform.getEntry

/**
 * Defines a method of fetching profiles, as well as fetching the operations that may be performed
 *
 * @author Alan Holt (aholt@proteus.co)
 */
interface ProfileFetchEntry {
    /** The programmatic name of the fetch entry.  Do not change this unless you want to write a data conversion. */
    val programmaticName: String
    /** The display name of the fetch entry. */
    val displayName: String
        get () = programmaticName

    /**
     * Get the description of the visibility condition for the given configured membership operation.
     *
     * @param op the configured membership operation
     * @param lc the locale context
     * @return the description of the configuration
     */
    fun getDescription(op: MembershipOperation, lc: LocaleContext): String

    /**
     * Fetch the profiles to check for membership operation on.
     *
     * @param req the request to use for fetching profiles
     * @return the profiles to check for the membership operation on
     */
    fun fetchProfiles(req: CmsRequest<*>): List<Profile>

    /**
     * Fetch the membership operations that may be configured for this.
     *
     * @return possible membership operations
     */
    fun fetchOperations(): List<MembershipOperation>
}

/**
 * Defines a method of fetching profiles, as well as fetching the operations that may be performed
 *
 * @param programmaticName the programmatic name of the fetch entry.  Do not change this unless you want to write a data conversion.
 * @param displayName the display name of the fetch entry.
 * @param description function to get the description of the visibility condition for the given configured membership operation.
 * @param fetch function to fetch the profiles to check for membership operation on.
 * @param operations function to fetch the membership operations that may be configured for this.
 *
 * @author Alan Holt (aholt@proteus.co)
 */
open class ProfileFetchEntryImpl(
    /** the programmatic name of the fetch entry.  Do not change this unless you want to write a data conversion. */
    override val programmaticName: String,
    override val displayName: String = programmaticName,
    val description: (MembershipOperation, LocaleContext) -> String,
    val fetch: (CmsRequest<*>) -> List<Profile>,
    val operations: () -> List<MembershipOperation>
) : ProfileFetchEntry {
    override fun getDescription(op: MembershipOperation, lc: LocaleContext): String = description(op, lc)

    override fun fetchProfiles(req: CmsRequest<*>): List<Profile> = fetch(req)

    override fun fetchOperations(): List<MembershipOperation> = operations()
}

/**
 * Create a ProfileFetchEntryRelationship for this FetchEntry and the given fetcher
 *
 * @param fetcher the fetcher
 * @return a Relationship
 */
fun ProfileFetchEntry.toRelationship(fetcher: ProfileFetcher) = ProfileFetchEntryRelationship(
    fetcher,
    this
)

/**
 * Defines a relationship between a FetchEntry and a Fetcher.
 * Really only used for database persistence so the converter can
 * figured its life out without ending up crying under a bridge somewhere.
 */
data class ProfileFetchEntryRelationship(
    val fetcher: ProfileFetcher,
    val fetchEntry: ProfileFetchEntry
)

/**
 * [AbstractDataDomain] for a [ProfileFetchEntryRelationship]
 *
 * @author Alan Holt (aholt@proteus.co)
 */
private object ProfileFetchEntryDataDomain : AbstractDataDomain<ProfileFetchEntryRelationship>(
    ProfileFetchEntryRelationship::class.java
) {
    init {
        withMembershipTest { it is ProfileFetchEntryRelationship }
    }
}

/**
 * [Converter] for [ProfileFetchEntryRelationship]
 *
 * @author Alan Holt (aholt@proteus.co)
 */
private class ProfileFetchEntryConverter(
    private val fetchers: List<ProfileFetcher>
) : Converter {
    @Suppress("UNCHECKED_CAST")
    override fun <DT : Any?> convert(ctx: ConverterContext<DT>): DT? = when (val dt = ctx.destinationType) {
        String::class.java                        -> when (val src = ctx.source) {
            null                             -> null
            is String                        -> src as DT?
            is ProfileFetchEntryRelationship -> "${src.fetcher.programmaticName}:::${src.fetchEntry.programmaticName}" as DT?
            else                             -> throw InconvertibleException(ctx, "Unsupported source: ${src}")
        }
        ProfileFetchEntryRelationship::class.java -> when (val src = ctx.source) {
            null                             -> null
            is ProfileFetchEntryRelationship -> src as DT?
            is String                        -> src.split(":::").let { (fetcherProg, entryProg) ->
                fetchers.find { it.programmaticName == fetcherProg }?.run {
                    getEntry(entryProg)?.toRelationship(this)
                }
            } as DT?
            else                             -> throw InconvertibleException(ctx, "Unsupported source: ${src}")
        }
        else                                      -> throw InconvertibleException(ctx, "Unsupported destination type: ${dt}")
    }
}

/**
 * [DataTypeImpl] for [ProfileFetchEntryRelationship]s to be used within a ModelDataSet
 *
 * @author Alan Holt (aholt@proteus.co)
 */
class ProfileFetchEntryDataType private constructor(
    converter: ProfileFetchEntryConverter
) : DataTypeImpl<ProfileFetchEntryRelationship>(
    "ProfileFetchEntryDataType".toTextSource(),
    "ProfileFetchEntry",
    PhysicalType.String,
    ProfileFetchEntryDataDomain,
    converter,
    converter
) {
    /**
     * [DataTypeImpl] for [ProfileFetchEntryRelationship]s to be used within a ModelDataSet
     *
     * @param fetchers the list of ProfileFetchers to use for selecting the fetcher by programmatic name.
     *
     * @author Alan Holt (aholt@proteus.co)
     */
    constructor(fetchers: List<ProfileFetcher>) : this(ProfileFetchEntryConverter(fetchers))
}