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

package com.example.app.login.social.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TextSources;

/**
 * A Social Login Provider
 *
 * i.e. Facebook, Twitter, Google, Instagram, etc...
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/19/17
 */
public class SocialLoginProvider implements NamedObject
{
    private final String _displayName;
    private final String _programmaticName;

    /**
     * Instantiates a new Social login provider.
     *
     * @param displayName the display name
     * @param programmaticName the programmatic name
     */
    public SocialLoginProvider(String displayName, String programmaticName)
    {
        _displayName = displayName;
        _programmaticName = programmaticName;
    }

    /**
     * Gets display name.
     *
     * @return the display name
     */
    public String getDisplayName()
    {
        return _displayName;
    }

    /**
     * Gets programmatic name.
     *
     * @return the programmatic name
     */
    public String getProgrammaticName()
    {
        return _programmaticName;
    }

    @Nonnull
    @Override
    public TextSource getName()
    {
        return TextSources.createText(getDisplayName());
    }

    @Nullable
    @Override
    public TextSource getDescription()
    {
        return null;
    }
}
