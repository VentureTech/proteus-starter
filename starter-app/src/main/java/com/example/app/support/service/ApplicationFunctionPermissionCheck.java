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

package com.example.app.support.service;

import com.example.app.profile.model.user.User;
import com.example.app.profile.model.user.UserDAO;
import com.example.app.profile.ui.company.CompanyManagement;
import com.example.app.profile.ui.company.CompanyUIPermissionCheck;

import javax.annotation.Nullable;

import net.proteusframework.core.spring.ApplicationContextUtils;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.dao.PrincipalDAO;

/**
 * Defines methods for checking permission for an Application Function.
 * <br><br>
 * This interface can be used to check permissions for links
 * within a scripted menu to determine what menu items should be rendered.
 * Also, the same code should be used within the UI itself to check permission for the current user.
 * <br><br>
 * See {@link CompanyManagement} and {@link CompanyUIPermissionCheck} for an example.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/4/17
 */
public interface ApplicationFunctionPermissionCheck
{
    /**
     * Check if the given User has the required permissions to view or access the Application Function
     *
     * @param user the user
     *
     * @return the boolean
     */
    default boolean checkPermissions(@Nullable User user)
    {
        if(user == null) return false;
        return checkPermissions(user.getPrincipal());
    }

    /**
     * Check if the given Principal has the required permissions to view or access the Application Function
     *
     * @param principal the principal
     *
     * @return the boolean
     */
    boolean checkPermissions(@Nullable Principal principal);

    /**
     * Check if the current User or Principal has the required permissions to view or access the Application Function
     *
     * @return the boolean
     */
    default boolean checkPermissionsForCurrent()
    {
        @SuppressWarnings("ConstantConditions")
        UserDAO userDAO = ApplicationContextUtils.getInstance().getContext().getBean(UserDAO.class);
        PrincipalDAO principalDAO = PrincipalDAO.getInstance();
        User currentUser = userDAO.getCurrentUser();
        return currentUser != null ? checkPermissions(currentUser) : checkPermissions(principalDAO.getCurrentPrincipal());
    }

    /**
     * Check if the current User or Principal has the required permissions to view or access the Application Function
     * <br><br>
     * Unlike {@link #checkPermissionsForCurrent()}, this method will throw an {@link IllegalArgumentException}
     * if the current user lacks required permission.  The given error message will be the message set on the thrown Exception.
     *
     * @param errorMessage the error message
     * @throws IllegalArgumentException if current user lacks permission.
     */
    default void checkPermissionsForCurrent(String errorMessage) throws IllegalArgumentException
    {
        if(!checkPermissionsForCurrent())
        {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Get the ApplicationFunction name that this PermissionCheck is checking permissions for.
     * @return the ApplicationFunction name
     */
    String getApplicationFunctionName();
}
