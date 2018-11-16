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
import com.example.app.profile.model.Profile
import com.example.app.profile.model.ProfileDAO
import com.example.app.profile.model.membership.MembershipOperation
import com.example.app.profile.model.user.UserDAO
import com.i2rd.contentmodel.def.type.DataTypeImpl
import com.i2rd.contentmodel.def.type.PhysicalType
import com.i2rd.converter.Converter
import com.i2rd.converter.ConverterContext
import com.i2rd.converter.InconvertibleException
import com.i2rd.data.AbstractDataDomain
import net.proteusframework.cms.controller.CmsRequest
import net.proteusframework.core.locale.LocaleContext

/**
 * Defines a method of checking if the current user has a membership operation on one or more profiles
 *
 * @author Alan Holt (aholt@proteus.co)
 */
interface MembershipOperationChecker {
    /** The programmatic name of the checker.  Do not change this unless you want to write a data conversion. */
    val programmaticName: String
    /** The display name of the checker. */
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

    /**
     * Check if the current user has the given membership operation on any of the profiles returned by [fetchProfiles].
     *
     * @param req the request to use to fetch the profiles
     * @param op the operation to check on the profiles
     * @return true if the user has the membership operation on any of the profiles.  False otherwise.
     */
    fun checkPermissions(
        req: CmsRequest<*>,
        op: MembershipOperation,
        profileDAO: ProfileDAO,
        userDAO: UserDAO
    ): Boolean = profileDAO.canOperate(
        userDAO.currentUser,
        fetchProfiles(req),
        req.timeZone,
        op
    )
}

/**
 * Defines a method of checking if the current user has a membership operation on one or more profiles
 *
 * @param programmaticName the programmatic name of the checker.  Do not change this unless you want to write a data conversion.
 * @param displayName the display name of the checker.
 * @param description function to get the description of the visibility condition for the given configured membership operation.
 * @param fetch function to fetch the profiles to check for membership operation on.
 * @param operations function to fetch the membership operations that may be configured for this.
 *
 * @author Alan Holt (aholt@proteus.co)
 */
abstract class MembershipOperationCheckerImpl(
    /** the programmatic name of the checker.  Do not change this unless you want to write a data conversion. */
    override val programmaticName: String,
    override val displayName: String = programmaticName,
    val description: (MembershipOperation, LocaleContext) -> String,
    val fetch: (CmsRequest<*>) -> List<Profile>,
    val operations: () -> List<MembershipOperation>
) : MembershipOperationChecker {
    override fun getDescription(op: MembershipOperation, lc: LocaleContext): String = description(op, lc)

    override fun fetchProfiles(req: CmsRequest<*>): List<Profile> = fetch(req)

    override fun fetchOperations(): List<MembershipOperation> = operations()

}

/**
 * [AbstractDataDomain] for a [MembershipOperationChecker]
 *
 * @author Alan Holt (aholt@proteus.co)
 */
private object MembershipOperationCheckerDataDomain : AbstractDataDomain<MembershipOperationChecker>(
    MembershipOperationChecker::class.java
) {
    init {
        withMembershipTest { it is MembershipOperationChecker }
    }
}

/**
 * [Converter] for [MembershipOperationChecker]
 *
 * @author Alan Holt (aholt@proteus.co)
 */
private class MembershipOperationCheckerConverter(
    private val fetchers: List<MembershipOperationChecker>
) : Converter {
    @Suppress("UNCHECKED_CAST")
    override fun <DT : Any?> convert(ctx: ConverterContext<DT>): DT? = when (val dt = ctx.destinationType) {
        String::class.java                     -> when (val src = ctx.source) {
            null                          -> null
            is String                     -> src as DT?
            is MembershipOperationChecker -> src.programmaticName as DT?
            else                          -> throw InconvertibleException(ctx, "Unsupported source: ${src}")
        }
        MembershipOperationChecker::class.java -> when (val src = ctx.source) {
            null                          -> null
            is MembershipOperationChecker -> src as DT?
            is String                     -> fetchers.find { it.programmaticName == src } as DT?
            else                          -> throw InconvertibleException(ctx, "Unsupported source: ${src}")
        }
        else                                   -> throw InconvertibleException(ctx, "Unsupported destination type: ${dt}")
    }
}

/**
 * [DataTypeImpl] for [MembershipOperationChecker]s to be used within a ModelDataSet
 *
 * @author Alan Holt (aholt@proteus.co)
 */
class MembershipOperationCheckerDataType private constructor(
    converter: MembershipOperationCheckerConverter
) : DataTypeImpl<MembershipOperationChecker>(
    "MembershipOperationCheckerDataType".toTextSource(),
    "MembershipOperationChecker",
    PhysicalType.String,
    MembershipOperationCheckerDataDomain,
    converter,
    converter
) {
    /**
     * [DataTypeImpl] for [MembershipOperationChecker]s to be used within a ModelDataSet
     *
     * @param fetchers the list of ProfileFetchers to use for selecting the fetcher by programmatic name.
     *
     * @author Alan Holt (aholt@proteus.co)
     */
    constructor(fetchers: List<MembershipOperationChecker>) : this(MembershipOperationCheckerConverter(fetchers))
}