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

package experimental.cms.dsl.shell

import org.jetbrains.exposed.sql.Table

object SiteDefinitionRecord : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", 512).uniqueIndex()
    val version = integer("version")
    val created = datetime("created")
    val modified = datetime("modified")
}