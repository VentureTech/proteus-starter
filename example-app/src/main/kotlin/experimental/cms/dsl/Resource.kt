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

interface ResourceCapable {
    val cssPaths: MutableList<String>
    val javaScriptPaths: MutableList<String>

    fun css(path: String) {
        cssPaths.add(path)
    }

    fun javascript(path: String) {
        javaScriptPaths.add(path)
    }
}