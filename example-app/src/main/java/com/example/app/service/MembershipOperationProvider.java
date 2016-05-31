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

package com.example.app.service;

import com.example.app.model.profile.MembershipOperation;

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
     * Get the "Facilitate Meeting Group" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation facilitateMeetingGroup();

    /**
     * Get a list of all MembershipOperations that are defined within this provider.
     *
     * @return membership operations
     */
    List<MembershipOperation> getOperations();

    /**
     * Get the "Manage Repository Objectives" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation manageRepositoryObjectives();

    /**
     * Get the "Modify Meeting Group" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyMeetingGroup();

    /**
     * Get the "Modify Repository Goals" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyRepositoryGoals();

    /**
     * Get the "Modify Repository Processes" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyRepositoryProcesses();

    /**
     * Get the "Modify Repository Programs" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyRepositoryPrograms();

    /**
     * Get the "Modify Repository Resources" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyRepositoryResources();

    /**
     * Get the "Modify Repository Task Configs" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyRepositoryTaskConfigs();

    /**
     * Get the "Modify User" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyUser();

    /**
     * Get the "Modify User Roles" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation modifyUserRoles();

    /**
     * Get the "Delete User Roles" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation deleteUserRoles();

    /**
     * Get the "Modify User Roles Operations" MembershipOperation, or create it, if it has not been created yet.
     * @return a MemberShipOperation
     */
    MembershipOperation modifyUserRoleOperations();

    /**
     * Get the "View Meeting Group" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation viewMeetingGroup();

    /**
     * Get the "View Repository Goals" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation viewRepositoryGoals();

    /**
     * Get the "View Repository Processes" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation viewRepositoryProcesses();

    /**
     * Get the "View Repository Programs" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation viewRepositoryPrograms();

    /**
     * Get the "View Repository Resources" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation viewRepositoryResources();

    /**
     * Get the "View Repository Task Configs" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation viewRepositoryTaskConfigs();

    /**
     * Get the "View User" MembershipOperation, or create it, if it has not been created yet.
     *
     * @return a MembershipOperation
     */
    MembershipOperation viewUser();
}
