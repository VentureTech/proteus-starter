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

package com.example.app.profile.ui.company;

import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.support.service.AppUtil;
import com.example.app.support.service.ApplicationFunctionPermissionCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.proteusframework.internet.http.Request;
import net.proteusframework.ui.management.ApplicationRegistry;
import net.proteusframework.ui.management.link.RegisteredLinkDAO;
import net.proteusframework.users.model.Principal;

/**
 * Service to check permissions for the Company UIs.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/4/17
 */
@Service
public class CompanyUIPermissionCheck implements ApplicationFunctionPermissionCheck
{
    @Autowired private RegisteredLinkDAO _registeredLinkDAO;
    @Autowired private ApplicationRegistry _applicationRegistry;
    @Autowired private AppUtil _appUtil;

    @Override
    public boolean checkPermissions(@Nonnull Request request, @Nullable Principal principal)
    {
        if(functionExists(_appUtil.getSite(), request, _applicationRegistry, _registeredLinkDAO))
        {
            if (principal == null) return false;
            return AppUtil.userHasAdminRole(principal);
        }
        return false;
    }

    @Override
    public String getApplicationFunctionName()
    {
        return ApplicationFunctions.Company.MANAGEMENT;
    }
}
