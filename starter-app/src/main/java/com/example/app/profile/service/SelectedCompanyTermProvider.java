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

import com.example.app.profile.model.terminology.ProfileTermProvider;
import com.example.app.support.ui.UIPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import net.proteusframework.core.locale.TextSource;

/**
 * Term provider for a company in an MIWT UI.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SelectedCompanyTermProvider implements ProfileTermProvider
{
    private static final long serialVersionUID = -1682061106170397120L;

    @Autowired
    private UIPreferences _uiPreferences;


    @Override
    public TextSource company()
    {
        return getDelegate().company();
    }

    @Override
    public TextSource companies()
    {
        return getDelegate().companies();
    }

    @Override
    public TextSource client()
    {
        return getDelegate().client();
    }

    @Override
    public TextSource clients()
    {
        return getDelegate().clients();
    }

    @Override
    public TextSource location()
    {
        return getDelegate().location();
    }

    @Override
    public TextSource locations()
    {
        return getDelegate().locations();
    }

    private ProfileTermProvider getDelegate()
    {
        return _uiPreferences.getSelectedCompanyTermProvider();
    }

}
