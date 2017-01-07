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

import net.proteusframework.users.model.AcegiPermission
import net.proteusframework.users.model.AuthenticationMethodSecurityLevel
import net.proteusframework.users.model.AuthenticationMethodSecurityLevel.SHARED_IDENTIFIER
import net.proteusframework.users.model.AuthenticationMethodSecurityLevel.SHARED_SECRET
import net.proteusframework.users.model.CredentialPolicyLevel

/**
 * Model of a Permission
 * @author Russ Tennant (russ@venturetech.net)
 */
data class Permission(val type: Class<out AcegiPermission>, val name: String, val programmaticName: String,
    val policyLevel: CredentialPolicyLevel = CredentialPolicyLevel.LOW,
    val minAuthenticationMethodSecurityLevel: AuthenticationMethodSecurityLevel = SHARED_IDENTIFIER,
    val maxAuthenticationMethodSecurityLevel: AuthenticationMethodSecurityLevel = SHARED_SECRET,
    /** Programmatic Name Of Role To Add To */
    val addToRole: String = "")
