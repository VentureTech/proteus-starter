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

package com.example.app.terminology;

import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.ObjectStreamException;

import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.spring.ApplicationContextUtils;

import static com.example.app.terminology.DefaultProfileTermProviderLOK.*;

/**
 * Term provider for profile API referenced in a UI.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@SuppressWarnings("unused")
@Component
@Qualifier("default")
@I18NFile(
    symbolPrefix = "com.lrlabs.terminology.DefaultProfileTermProvider",
    i18n = {
        @I18N(symbol = "attendee", l10n = @L10N("Attendee")),
        @I18N(symbol = "attendees", l10n = @L10N("Attendees")),
        @I18N(symbol = "membership", l10n = @L10N("Role")),
        @I18N(symbol = "membership-type", l10n = @L10N("Role Type")),
        @I18N(symbol = "profile", l10n = @L10N("Profile")),
        @I18N(symbol = "profile-type", l10n = @L10N("Profile Type")),
        @I18N(symbol = "repository", l10n = @L10N("Repository")),
        @I18N(symbol = "resource", l10n = @L10N("Resource")),
        @I18N(symbol = "resource all lower", l10n = @L10N("resource")),
        @I18N(symbol = "resources", l10n = @L10N("Resources")),
        @I18N(symbol = "result", l10n = @L10N("Result")),
        @I18N(symbol = "user", l10n = @L10N("User")),
        @I18N(symbol = "users", l10n = @L10N("Users")),
        @I18N(symbol = "user all lower", l10n = @L10N("user")),
    }
)
public class DefaultProfileTermProvider implements ProfileTermProvider
{
    private static final long serialVersionUID = 54911207722013603L;


    @Override
    public TextSource attendee()
    {
        return ATTENDEE();
    }

    @Override
    public TextSource attendees()
    {
        return ATTENDEES();
    }

    @Override
    public TextSource membership()
    {
        return MEMBERSHIP();
    }

    @Override
    public TextSource membershipType()
    {
        return MEMBERSHIP_TYPE();
    }

    @Override
    public TextSource profile()
    {
        return PROFILE();
    }

    @Override
    public TextSource profileType()
    {
        return PROFILE_TYPE();
    }

    @Override
    public TextSource repository()
    {
        return REPOSITORY();
    }

    @Override
    public TextSource resource()
    {
        return RESOURCE();
    }

    @Override
    public TextSource resourceAllLower()
    {
        return RESOURCE_ALL_LOWER();
    }

    @Override
    public TextSource resources()
    {
        return RESOURCES();
    }

    @Override
    public TextSource result()
    {
        return RESOURCE();
    }

    @Override
    public TextSource user()
    {
        return USER();
    }

    @Override
    public TextSource userAllLower()
    {
        return USER_ALL_LOWER();
    }

    @Override
    public TextSource users()
    {
        return USERS();
    }

    private Object readResolve() throws ObjectStreamException
    {
        ApplicationContext context = ApplicationContextUtils.getInstance().getContext();
        assert context != null;
        return context.getBean(DefaultProfileTermProvider.class);
    }

    @Contract(pure = true)
    private Object writeReplace() throws ObjectStreamException
    {
        return this;
    }
}