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
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

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
    @Override
    public boolean checkPermissions(@Nullable Principal principal)
    {
        if(principal == null) return false;
        return AppUtil.userHasAdminRole(principal);
    }

    @Override
    public String getApplicationFunctionName()
    {
        return ApplicationFunctions.Company.MANAGEMENT;
    }
}
