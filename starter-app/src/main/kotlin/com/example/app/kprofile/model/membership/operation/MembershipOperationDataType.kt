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

package com.example.app.kprofile.model.membership.operation

import com.example.app.kotlin.toTextSource
import com.example.app.profile.model.ProfileDAO
import com.example.app.profile.model.membership.MembershipOperation
import com.i2rd.contentmodel.def.type.DataTypeImpl
import com.i2rd.contentmodel.def.type.PhysicalType
import com.i2rd.converter.Converter
import com.i2rd.converter.ConverterContext
import com.i2rd.converter.InconvertibleException
import com.i2rd.data.AbstractDataDomain

/**
 * [AbstractDataDomain] for a [MembershipOperation]
 *
 * @author Alan Holt (aholt@proteus.co)
 */
private object MembershipOperationDataDomain : AbstractDataDomain<MembershipOperation>(MembershipOperation::class.java) {
    init {
        withMembershipTest { it is MembershipOperation }
    }
}

/**
 * [Converter] for [MembershipOperation]
 *
 * @author Alan Holt (aholt@proteus.co)
 */
private class MembershipOperationConverter(
    val profileDAO: ProfileDAO
) : Converter {
    @Suppress("UNCHECKED_CAST")
    override fun <DT : Any?> convert(ctx: ConverterContext<DT>): DT? = when (val dt = ctx.destinationType) {
        String::class.java              -> when (val src = ctx.source) {
            null                   -> null
            is String              -> src as DT?
            is MembershipOperation -> src.programmaticIdentifier as DT?
            else                   -> throw InconvertibleException(ctx, "Unsupported source: ${src}")
        }
        MembershipOperation::class.java -> when (val src = ctx.source) {
            null                   -> null
            is MembershipOperation -> src as DT?
            is String              -> profileDAO.getMembershipOperation(src).orElse(null) as DT?
            else                   -> throw InconvertibleException(ctx, "Unsupported source: ${src}")
        }
        else                            -> throw InconvertibleException(ctx, "Unsupported destination type: ${dt}")
    }
}

/**
 * [DataTypeImpl] for [MembershipOperation] to be used within a ModelDataSet
 *
 * @author Alan Holt (aholt@proteus.co)
 */
class MembershipOperationDataType private constructor(
    converter: MembershipOperationConverter
) : DataTypeImpl<MembershipOperation>(
    "MembershipOperationDataType".toTextSource(),
    "MembershipOperation",
    PhysicalType.String,
    MembershipOperationDataDomain,
    converter,
    converter
) {
    /**
     * [DataTypeImpl] for [MembershipOperation] to be used within a ModelDataSet
     *
     * @param profileDAO the Profile DAO to use for fetching the membership operation from the database.
     *
     * @author Alan Holt (aholt@proteus.co)
     */
    constructor(profileDAO: ProfileDAO) : this(MembershipOperationConverter(profileDAO))
}