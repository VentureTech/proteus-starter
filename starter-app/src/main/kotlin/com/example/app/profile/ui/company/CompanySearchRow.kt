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

package com.example.app.profile.ui.company

/**
 * DTO for company search.
 * @author Russ Tennant (russ@venturetech.net)
 */
data class CompanySearchRow(val id: Int, val name: String, val address: String, val website: String, val phone: String)