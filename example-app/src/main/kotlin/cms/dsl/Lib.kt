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

package cms.dsl

open class Identifiable(var  id: String) {
    override fun toString(): String {
        return "Identifiable(id='$id')"
    }
}

open class IdentifiableParent<C>(id: String) : Identifiable(id){
    val children = mutableListOf<C>()
    fun add(child: C): C = child.apply {children.add(this)}
    override fun toString(): String {
        return "IdentifiableParent(id='$id', children=$children)"
    }

}
