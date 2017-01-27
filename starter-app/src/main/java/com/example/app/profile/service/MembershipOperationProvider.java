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

package com.example.app.profile.service;

import com.example.app.profile.model.membership.MembershipOperation;

import java.util.List;

/**
 * Provider of common membership operations.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public interface MembershipOperationProvider
{
    /**
     * Get the "Change User Password" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation changeUserPassword();

    /**
     * Get the "Delete User Roles" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation deleteUserRoles();

    /**
     * Get a list of all MembershipOperations that are defined within this provider.
     *
     * @return membership operations
     */
    List<MembershipOperation> getOperations();

    /**
     * Get the modify compnay operation.
     *
     * @return the operation.
     */
    MembershipOperation modifyCompany();

    /**
     * Get the "Modify Repository Resources" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyRepositoryResources();

    /**
     * Get the "Manage User" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation manageUser();

    /**
     * Get the "Modify User" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyUser();

    /**
     * Get the "Modify User Roles Operations" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MemberShipOperation
     */
    MembershipOperation modifyUserRoleOperations();

    /**
     * Get the "Modify User Roles" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyUserRoles();

    /**
     * Get the "View Repository Resources" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation viewRepositoryResources();

    /**
     * Get the "View User" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation viewUser();

    /**
     * Get the "Modify Client" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyClient();

    /**
     * Get the "View Client" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation viewClient();

    /**
     * Get the "Modify Location" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyLocation();

    /**
     * Get the "View Location" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation viewLocation();
}
