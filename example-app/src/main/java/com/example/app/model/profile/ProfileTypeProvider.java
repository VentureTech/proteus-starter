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

import com.example.app.service.ProfileTypeKindLabelProvider;
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

import net.proteusframework.cms.label.Label;
import net.proteusframework.core.hibernate.HibernateSessionHandler;
import net.proteusframework.core.locale.JDBCLocaleSource;
import net.proteusframework.core.locale.LocalizedObjectKey;

/**
 * Configuration that provides ProfileTypes for LRSuccess
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/6/16 8:53 AM
 */
@Configuration
@Lazy
public class ProfileTypeProvider implements ApplicationListener<ApplicationContextEvent>
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ProfileTypeProvider.class);

    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private JDBCLocaleSource _localeSource;
    @Autowired
    private ProfileTypeKindLabelProvider _typeKindLabelProvider;

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
                initialize();
                _initialized = true;
            }
            catch (Exception e)
            {
                _logger.fatal("ProfileTypeProvider failed to initialize.", e);
            }
            finally
            {
                handler.clearSessions();
            }
        }
    }

    /**
     * Initializes ProfileTypes and Profile Type Kinds that are specified by this provider, ensuring they are within the database.
     */
    private void initialize()
    {
        initKinds();
        initTypes();
    }

    private void initKinds()
    {
        kindCompany();
        kindLocation();
    }

    private void initTypes()
    {
        company();
        location();
    }

    /**
     * Get the Client Profile Type Kind.
     *
     * @return the Client Profile Type Kind
     */
    @Bean
    public Label kindCompany()
    {
        String progId = "profile-type-kind-client";
        Label kind = _typeKindLabelProvider.getLabelOrNew(progId);
        if (_profileDAO.isTransient(kind))
        {
            kind.setName(LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Client"));
            _typeKindLabelProvider.addLabel(kind);
            kind = _typeKindLabelProvider.getLabel(progId).orElseThrow(() -> new IllegalStateException(
                "Profile Type Kind could not be found, even after it was created."));
        }
        return kind;
    }

    /**
     * Get the Location Profile Type Kind.
     *
     * @return the Location Profile Type Kind
     */
    @Bean
    public Label kindLocation()
    {
        String progId = "profile-type-kind-location";
        Label kind = _typeKindLabelProvider.getLabelOrNew(progId);
        if (_profileDAO.isTransient(kind))
        {
            kind.setName(LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Location"));
            _typeKindLabelProvider.addLabel(kind);
            kind = _typeKindLabelProvider.getLabel(progId).orElseThrow(() -> new IllegalStateException(
                "Profile Type Kind could not be found, even after it was created."));
        }
        return kind;
    }

    /**
     * Get the client profile type.
     *
     * @return the client profile type
     */
    @Bean
    public ProfileType company()
    {
        return _profileDAO.getProfileTypeOrNew("client",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Client"),
            this::kindCompany);
    }

    /**
     * Get the location profile type.
     *
     * @return the location profile type
     */
    @Bean
    public ProfileType location()
    {
        return _profileDAO.getProfileTypeOrNew("location",
            () -> LocalizedObjectKey.getLocalizedObjectKey(_localeSource, Locale.ENGLISH, null, "Location"),
            this::kindLocation);
    }
}
