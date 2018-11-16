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
import com.example.app.kprofile.ui.visibility.ProfileFetcherDataDomain.withMembershipTest
import com.example.app.profile.model.membership.MembershipOperation
import com.i2rd.contentmodel.def.type.DataTypeImpl
import com.i2rd.contentmodel.def.type.PhysicalType
import com.i2rd.converter.Converter
import com.i2rd.converter.ConverterContext
import com.i2rd.converter.InconvertibleException
import com.i2rd.data.AbstractDataDomain
import net.proteusframework.cms.controller.CmsRequest

/**
 * Interface for defining ways of fetching profiles for checking permissions (membership operations).
 * Instances of this should be a Spring bean, and will be autowired into the [MembershipOperationVisibilityCondition]
 * As options for selection.
 *
 * @author Alan Holt (aholt@proteus.co)
 */
interface ProfileFetcher {
    /** Programmatic Name.  Do Not Change this unless you want to create a data conversion as well to update the database data */
    val programmaticName: String

    /** The display name of the Profile Fetcher within the Visibility Condition Editor */
    val displayName: String
        get () = programmaticName

    /** The myriad ways that profiles (and their respective membership operations) can be fetched */
    val entries: List<ProfileFetchEntry>

    /**
     * Check if the current user has the given membership operation on any of the profiles returned by the fetchEntry.
     *
     * @param request the request to use to fetch the profiles from the fetchEntry
     * @param fetchEntry the fetch entry to use to fetch the profiles
     * @param operation the operation to check on the profiles
     * @return true if the user has the membership operation on any of the profiles.  False otherwise.
     */
    fun checkPermissions(request: CmsRequest<*>, fetchEntry: ProfileFetchEntry, operation: MembershipOperation): Boolean

    /**
     * Get the [ProfileFetchEntry] (if it exists) that has the given programmatic name for this ProfileFetcher
     *
     * @param programmaticName the fetch entry programmatic name
     * @return the [ProfileFetchEntry], or null.
     */
    fun getEntry(programmaticName: Any?) = entries.find { it.programmaticName == programmaticName }
}

/**
 * [AbstractDataDomain] for a [ProfileFetcher]
 *
 * @author Alan Holt (aholt@proteus.co)
 */
private object ProfileFetcherDataDomain : AbstractDataDomain<ProfileFetcher>(ProfileFetcher::class.java) {
    init {
        withMembershipTest { it is ProfileFetcher }
    }
}

/**
 * [Converter] for [ProfileFetcher]
 *
 * @author Alan Holt (aholt@proteus.co)
 */
private class ProfileFetcherConverter(
    private val fetchers: List<ProfileFetcher>
) : Converter {
    @Suppress("UNCHECKED_CAST")
    override fun <DT : Any?> convert(ctx: ConverterContext<DT>): DT? = when (val dt = ctx.destinationType) {
        String::class.java         -> when (val src = ctx.source) {
            null              -> null
            is String         -> src as DT?
            is ProfileFetcher -> src.programmaticName as DT?
            else              -> throw InconvertibleException(ctx,
                "Unsupported source: ${src}")
        }
        ProfileFetcher::class.java -> when (val src = ctx.source) {
            null              -> null
            is ProfileFetcher -> src as DT?
            is String         -> fetchers.find { it.programmaticName == src } as DT?
            else              -> throw InconvertibleException(ctx, "Unsupported source: ${src}")
        }
        else                       -> throw InconvertibleException(ctx,
            "Unsupported destination type: ${dt}")
    }
}

/**
 * [DataTypeImpl] for [ProfileFetcher]s to be used within a ModelDataSet
 *
 * @author Alan Holt (aholt@proteus.co)
 */
class ProfileFetcherDataType private constructor(
    converter: ProfileFetcherConverter
) : DataTypeImpl<ProfileFetcher>(
    "ProfileFetcherDataType".toTextSource(),
    "ProfileFetcher",
    PhysicalType.String,
    ProfileFetcherDataDomain,
    converter,
    converter
) {
    /**
     * [DataTypeImpl] for [ProfileFetcher]s to be used within a ModelDataSet
     *
     * @param fetchers the list of ProfileFetchers to use for selecting the fetcher by programmatic name.
     *
     * @author Alan Holt (aholt@proteus.co)
     */
    constructor(fetchers: List<ProfileFetcher>) : this(ProfileFetcherConverter(fetchers))
}