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

import edu.emory.mathcs.backport.java.util.Arrays;

import com.example.app.login.social.service.SocialLoginProvider;
import com.example.app.login.social.service.SocialLoginService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.i2rd.cms.bean.contentmodel.CmsModelDataSet;

import net.proteusframework.cms.component.content.ContentBuilder;
import net.proteusframework.internet.http.Link;

/**
 * {@link ContentBuilder} for {@link SocialLoginElement}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/19/17
 */
public class SocialLoginContentBuilder extends ContentBuilder<Property>
{
    private static final String PROVIDER_LIST_DELIMITER = ":;:";
    private static final Pattern PROVIDER_LIST_DELIMITER_MATCH = Pattern.compile(PROVIDER_LIST_DELIMITER);

    /**
     * Load social login content builder.
     *
     * @param data the data
     * @param useCache the use cache
     *
     * @return the social login content builder
     */
    public static SocialLoginContentBuilder load(CmsModelDataSet data, boolean useCache)
    {
        return load(data, SocialLoginContentBuilder.class, useCache);
    }

    /**
     * Gets login service identifier.
     *
     * @return the login service identifier
     */
    @Nullable
    public String getLoginServiceIdentifier()
    {
        return getProperty(Property.LoginService);
    }

    /**
     * Sets login service identifier.
     *
     * @param identifier the identifier
     */
    public void setLoginServiceIdentifier(String identifier)
    {
        setProperty(Property.LoginService, identifier);
    }

    /**
     * Sets login service.
     *
     * @param service the service
     */
    public void setLoginService(@Nonnull SocialLoginService service)
    {
        setLoginServiceIdentifier(service.getServiceIdentifier());
    }

    /**
     * Gets provider programmatic names.
     *
     * @return the provider programmatic names
     */
    @SuppressWarnings("unchecked")
    public List<String> getProviderProgrammaticNames()
    {
        return (List<String>)Arrays.asList(PROVIDER_LIST_DELIMITER_MATCH.split(getProperty(Property.Providers, "")));
    }

    /**
     * Sets providers.
     *
     * @param providers the providers
     */
    public void setProviderProgrammaticNames(List<String> providers)
    {
        StringBuilder sb = new StringBuilder();
        final Iterator<String> providerIterator = providers.iterator();
        providerIterator.forEachRemaining(p -> {
            sb.append(p);
            if(providerIterator.hasNext())
                sb.append(PROVIDER_LIST_DELIMITER);
        });

        setProperty(Property.Providers, sb.toString());
    }

    /**
     * Sets providers.
     *
     * @param providers the providers
     */
    public void setProviders(List<SocialLoginProvider> providers)
    {
        setProviderProgrammaticNames(providers.stream().map(SocialLoginProvider::getProgrammaticName).collect(Collectors.toList()));
    }

    /**
     * Gets mode.
     *
     * @return the mode
     */
    public SocialLoginMode getMode()
    {
        return getPropertyEnumValue(Property.Mode, SocialLoginMode.Login);
    }

    /**
     * Sets mode.
     *
     * @param mode the mode
     */
    public void setMode(SocialLoginMode mode)
    {
        setPropertyEnumValue(Property.Mode, mode);
    }

    /**
     * Gets landing page.
     *
     * @return the landing page
     */
    @Nullable
    public Link getLandingPage()
    {
        return getLinkPropertyValue(Property.LandingPage, null);
    }

    /**
     * Sets landing page.
     *
     * @param link the link
     */
    public void setLandingPage(@Nullable Link link)
    {
        setLinkPropertyValue(Property.LandingPage, link);
    }

    /**
     * Is override dynamic return page boolean.
     *
     * @return the boolean
     */
    public boolean isOverrideDynamicReturnPage()
    {
        return getPropertyBooleanValue(Property.OverrideDynamicReturn, false);
    }

    /**
     * Sets override dynamic return page.
     *
     * @param b the b
     */
    public void setOverrideDynamicReturnPage(boolean b)
    {
        setPropertyBooleanValue(Property.OverrideDynamicReturn, b);
    }

    /**
     * Gets scripted redirect instance.
     *
     * @return the scripted redirect instance
     */
    public long getScriptedRedirectInstance()
    {
        return getPropertyLongValue(Property.ScriptedRedirect, 0);
    }

    /**
     * Sets scripted redirect instance.
     *
     * @param id the id
     */
    public void setScriptedRedirectInstance(long id)
    {
        setPropertyLongValue(Property.ScriptedRedirect, id);
    }
}

enum Property
{
    LoginService,
    Providers,
    Mode,
    LandingPage,
    ScriptedRedirect,
    OverrideDynamicReturn
}
