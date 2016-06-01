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

package com.example.app.model.profile;

import com.example.app.service.MembershipOperationProvider;
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

import java.util.Locale;

import net.proteusframework.core.hibernate.HibernateSessionHandler;
import net.proteusframework.core.locale.JDBCLocaleSource;
import net.proteusframework.core.locale.LocalizedObjectKey;

/**
 * Configuration that provides MembershipTypes.
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@Configuration
@Lazy
public class MembershipTypeProvider implements ApplicationListener<ApplicationContextEvent>
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(MembershipTypeProvider.class);

    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private ProfileTypeProvider _profileTypeProvider;
    @Autowired
    private MembershipOperationProvider _mop;
    @Autowired
    private JDBCLocaleSource _localeSource;

    private boolean _initialized;

    @Override
    public void onApplicationEvent(ApplicationContextEvent event)
    {
        if (!_initialized && (event instanceof ContextRefreshedEvent || event instanceof ContextStartedEvent))
        {
            //Ensure that the profile type provider is initialized
            _profileTypeProvider.onApplicationEvent(event);
            final HibernateSessionHandler handler =
                (HibernateSessionHandler) event.getApplicationContext().getBean(HibernateSessionHandler.RESOURCE_NAME);
            try
            {
                handler.openSessions();
                initialize();
                _initialized = true;
            }
            catch (Exception e)
            {
                _logger.fatal("MembershipTypeProvider failed to initialize.", e);
            }
            finally
            {
                handler.clearSessions();
            }
        }
    }

    private void initialize()
    {
        companyAdmin();
    }

    /**
     * Get the system admin membership type
     *
     * @return membership type
     */
    @Bean
    public MembershipType companyAdmin()
    {
        return _profileDAO.getMembershipTypeOrNew(_profileTypeProvider.company(), "admin",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "System Admin For Companies"),
            () -> _mop.getOperations());
    }
}
