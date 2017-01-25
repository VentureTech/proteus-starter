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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

import net.proteusframework.core.spring.ApplicationContextUtils;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;
import net.proteusframework.internet.http.ResponseURL;
import net.proteusframework.internet.http.Site;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ApplicationFunctionContext;
import net.proteusframework.ui.management.ApplicationFunctionContextProvider;
import net.proteusframework.ui.management.ApplicationRegistry;
import net.proteusframework.ui.management.link.RegisteredLinkDAO;
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
     * Check if the given User has the required permissions to view or access the Application Function.
     * <br><br>
     * Implementations of this method should also check if the ApplicationFunction exists
     * using {@link #functionExists(Site, Request, ApplicationRegistry, RegisteredLinkDAO)}
     *
     * @param request the Request
     * @param user the user
     *
     * @return the boolean
     */
    default boolean checkPermissions(@Nonnull Request request, @Nullable User user)
    {
        if(user == null) return false;
        return checkPermissions(request, user.getPrincipal());
    }

    /**
     * Check if the given Principal has the required permissions to view or access the Application Function
     * <br><br>
     * Implementations of this method should also check if the ApplicationFunction exists
     * using {@link #functionExists(Site, Request, ApplicationRegistry, RegisteredLinkDAO)}
     *
     * @param request the Request
     * @param principal the principal
     *
     * @return the boolean
     */
    boolean checkPermissions(@Nonnull Request request, @Nullable Principal principal);

    /**
     * Check if the current User or Principal has the required permissions to view or access the Application Function
     *
     * @param request the Request
     * @return the boolean
     */
    default boolean checkPermissionsForCurrent(@Nonnull Request request)
    {
        @SuppressWarnings("ConstantConditions")
        UserDAO userDAO = ApplicationContextUtils.getInstance().getContext().getBean(UserDAO.class);
        PrincipalDAO principalDAO = PrincipalDAO.getInstance();
        User currentUser = userDAO.getCurrentUser();
        return currentUser != null
            ? checkPermissions(request, currentUser)
            : checkPermissions(request, principalDAO.getCurrentPrincipal());
    }

    /**
     * Check if the current User or Principal has the required permissions to view or access the Application Function
     * <br><br>
     * Unlike {@link #checkPermissionsForCurrent(Request)}, this method will throw an {@link IllegalArgumentException}
     * if the current user lacks required permission.  The given error message will be the message set on the thrown Exception.
     *
     * @param request the Request
     * @param errorMessage the error message
     * @throws IllegalArgumentException if current user lacks permission.
     */
    default void checkPermissionsForCurrent(@Nonnull Request request, String errorMessage) throws IllegalArgumentException
    {
        if(!checkPermissionsForCurrent(request))
        {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Get the ApplicationFunction name that this PermissionCheck is checking permissions for.
     * @return the ApplicationFunction name
     */
    String getApplicationFunctionName();

    /**
     * Check if the ApplicationFunction that this Permission check is for actually exists on the given Site.
     * Will take ApplicationFunctionContext into consideration.
     *
     * @param site the Site to check on
     * @param request the Request used to determine ApplicationFunctionContext
     * @param applicationRegistry the ApplicationRegistry, should be provided by the caller.
     * @param registeredLinkDAO the RegisteredLinkDAO, should be provided by the caller.
     * @return boolean  True if the ApplicationFunction exists on the site, otherwise false.
     */
    default boolean functionExists(@Nonnull Site site, @Nonnull Request request,
        @Nonnull ApplicationRegistry applicationRegistry, @Nonnull RegisteredLinkDAO registeredLinkDAO)
    {
        final ApplicationFunction appFunction = applicationRegistry.getApplicationFunctionByName(getApplicationFunctionName());
        if(appFunction != null)
        {
            final ApplicationFunctionContextProvider contextProvider = applicationRegistry
                .getApplicationContextProvider(appFunction);
            final ApplicationFunctionContext functionContext = contextProvider.getContext(request);
            return registeredLinkDAO.getRegisteredLink(site, appFunction, functionContext) != null;
        }
        return false;
    }

    /**
     * Create a ResponseURL for the ApplicationFunction that this Permission Check is for.
     * By Default, will just create a URL with no URL Parameters.
     * @param request the current Request
     * @param response the current Response
     * @param applicationRegistry the ApplicationRegistry
     * @return ResponseURL
     */
    default ResponseURL createResponseURL(
        @Nonnull Request request, @Nonnull Response response, @Nonnull ApplicationRegistry applicationRegistry)
    {
        return response.createURL(applicationRegistry.createLink(request, response,
            applicationRegistry.getApplicationFunctionByName(getApplicationFunctionName()), Collections.emptyMap()));
    }
}
