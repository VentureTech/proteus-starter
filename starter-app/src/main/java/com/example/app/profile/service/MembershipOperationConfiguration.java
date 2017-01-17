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

import com.example.app.profile.model.ProfileDAO;
import com.example.app.profile.model.membership.MembershipOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.proteusframework.core.hibernate.HibernateSessionHandler;
import net.proteusframework.core.locale.JDBCLocaleSource;
import net.proteusframework.core.locale.LocalizedObjectKey;

/**
 * Configuration for Membership Capability Functions to ensure they are unique within the database
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/1/15 8:36 AM
 */
@Configuration
@Lazy
public class MembershipOperationConfiguration implements MembershipOperationProvider, ApplicationListener<ApplicationContextEvent>
{
    private static final Logger _logger = LogManager.getLogger(MembershipOperationConfiguration.class);

    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private JDBCLocaleSource _localeSource;

    private boolean _initialized;

    @Override
    public void onApplicationEvent(ApplicationContextEvent event)
    {
        if (!_initialized && (event instanceof ContextRefreshedEvent || event instanceof ContextStartedEvent))
        {
            final HibernateSessionHandler handler =
                (HibernateSessionHandler) event.getApplicationContext().getBean(HibernateSessionHandler.RESOURCE_NAME);
            try
            {
                handler.openSessions();
                getOperations();
                _initialized = true;
            }
            catch (Exception e)
            {
                _logger.fatal("MembershipOperationConfiguration failed to initialize.", e);
            }
            finally
            {
                handler.clearSessions();
            }
        }
    }

    @Override
    public List<MembershipOperation> getOperations()
    {
        List<MembershipOperation> operations = new ArrayList<>();


        operations.add(manageUser());
        operations.add(viewUser());
        operations.add(modifyUser());
        operations.add(modifyUserRoleOperations());
        operations.add(changeUserPassword());
        operations.add(modifyUserRoles());
        operations.add(deleteUserRoles());
        operations.add(viewRepositoryResources());
        operations.add(modifyRepositoryResources());
        operations.add(modifyCompany());
        operations.add(viewClient());
        operations.add(modifyClient());

        return operations;
    }

    @Override
    @Bean
    public MembershipOperation manageUser()
    {
        return _profileDAO.getMembershipOperationOrNew("manageUser",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Manage User"));
    }

    @Override
    public MembershipOperation viewUser()
    {
        return _profileDAO.getMembershipOperationOrNew("viewUser",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "View User"));
    }

    @Override
    public MembershipOperation modifyUser()
    {
        return _profileDAO.getMembershipOperationOrNew("modifyUser",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Modify User"));
    }

    @Override
    public MembershipOperation modifyUserRoleOperations()
    {
        return _profileDAO.getMembershipOperationOrNew("modifyUserRoleOperations",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Modify User Role Operations"));
    }

    @Override
    @Bean
    public MembershipOperation changeUserPassword()
    {
        return _profileDAO.getMembershipOperationOrNew("change-pw",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Change User Password"));
    }

    @Override
    @Bean
    public MembershipOperation modifyUserRoles()
    {
        return _profileDAO.getMembershipOperationOrNew("modifyUserRoles",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Modify User Roles"));
    }

    @Override
    @Bean
    public MembershipOperation deleteUserRoles()
    {
        return _profileDAO.getMembershipOperationOrNew("deleteUserRoles",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Delete User Roles"));
    }

    @Override
    public MembershipOperation viewRepositoryResources()
    {
        return _profileDAO.getMembershipOperationOrNew("viewRepositorResources",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "View Respository Resources"));
    }

    @Override
    public MembershipOperation modifyRepositoryResources()
    {
        return _profileDAO.getMembershipOperationOrNew("modifyRepositorResources",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Modify Respository Resources"));
    }

    @Override
    public MembershipOperation modifyCompany()
    {
        return _profileDAO.getMembershipOperationOrNew("modifyCompany",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Modify Client"));
    }

    @Override
    public MembershipOperation modifyClient()
    {
        return _profileDAO.getMembershipOperationOrNew("modifyClient",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Modify Client"));
    }

    @Override
    public MembershipOperation viewClient()
    {
        return _profileDAO.getMembershipOperationOrNew("viewClient",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "View Client"));
    }
}
