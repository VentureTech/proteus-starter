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

package com.example.app.login.social.ui;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;

import static com.example.app.login.social.ui.SocialLoginModeLOK.LINK;
import static com.example.app.login.social.ui.SocialLoginModeLOK.LOGIN;


/**
 * Defined Social Login Modes
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/19/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.login.social.ui.SocialLoginMode",
    i18n = {
        @I18N(symbol = "Login", l10n = @L10N("Login")),
        @I18N(symbol = "Link", l10n = @L10N("Link"))
    }
)
public enum SocialLoginMode implements NamedObject
{
    /** Login Mode. User is not already logged in. */
    Login(LOGIN()),
    /** Link Mode. User is logged in already, and wishes to link a social network with their account */
    Link(LINK())
    ;

    private final TextSource _name;

    SocialLoginMode(TextSource name)
    {
        _name = name;
    }

    @Nonnull
    @Override
    public TextSource getName()
    {
        return _name;
    }

    @Nullable
    @Override
    public TextSource getDescription()
    {
        return null;
    }
}
