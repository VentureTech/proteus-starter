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

package experimental.cms.dsl

/**
 * Model of a Principal Role
 * @author Russ Tennant (russ@venturetech.net)
 */
internal data class Role(val programmaticName: String, val name: String, val description: String="",
    val sessionTimeout: Int = 0)