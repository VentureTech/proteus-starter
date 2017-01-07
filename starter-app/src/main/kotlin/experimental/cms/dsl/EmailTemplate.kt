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

import net.proteusframework.email.EmailConfigType


/**
 * DSL Model of an [net.proteusframework.email.EmailTemplate]
 * @author Russ Tennant (russ@venturetech.net)
 */

class EmailTemplate <T:EmailConfigType<*>>
(val type: Class<T>, val name: String, val programmaticName: String) : Identifiable (programmaticName) {
    var from: String = ""
    var replyTo: String = ""
    var to: String = ""
    var cc: String = ""
    var bcc: String = ""
    var subject: String = ""
    var htmlContent: String = ""
}