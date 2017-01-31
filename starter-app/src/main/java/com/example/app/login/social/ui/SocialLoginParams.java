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

import com.example.app.login.social.service.SocialLoginProvider;
import com.example.app.login.social.service.SocialLoginService;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import net.proteusframework.core.notification.Notification;
import net.proteusframework.internet.http.ResponseURL;

/**
 * Parameters used to construct Social Login UIs
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/19/17
 */
public class SocialLoginParams
{
    private final ResponseURL _callbackURL;
    private final SocialLoginMode _mode;
    private final Consumer<Notification> _messageAcceptor;
    private final ImmutableList<SocialLoginProvider> _loginProviders;
    private final SocialLoginService _loginService;
    private final SocialLoginContentBuilder _contentBuilder;

    /**
     * Instantiates a new Social login params.
     *
     * @param callbackURI the callback uri
     * @param mode the mode
     * @param loginService the login service
     * @param messageAcceptor the message acceptor
     * @param contentBuilder the content builder
     * @param providers the providers
     */
    public SocialLoginParams(ResponseURL callbackURI, SocialLoginMode mode, SocialLoginService loginService,
        Consumer<Notification> messageAcceptor, SocialLoginContentBuilder contentBuilder, List<SocialLoginProvider> providers)
    {
        _callbackURL = callbackURI;
        _mode = mode;
        _messageAcceptor = messageAcceptor;
        _loginService = loginService;
        _contentBuilder = contentBuilder;
        if(providers != null)
            _loginProviders = ImmutableList.copyOf(providers);
        else
            _loginProviders = ImmutableList.of();
    }

    /**
     * Gets callback uri.
     *
     * @return the callback uri
     */
    public ResponseURL getCallbackURL()
    {
        return _callbackURL;
    }

    /**
     * Gets mode.
     *
     * @return the mode
     */
    public SocialLoginMode getMode()
    {
        return _mode;
    }

    /**
     * Gets login providers.
     *
     * @return the login providers
     */
    public ImmutableList<SocialLoginProvider> getLoginProviders()
    {
        return _loginProviders;
    }

    /**
     * Gets login providers string.
     *
     * @param excludes the excludes
     *
     * @return the login providers string
     */
    public String getLoginProvidersString(@Nullable List<String> excludes)
    {
        final List<String> excl = excludes != null ? excludes : Collections.emptyList();
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        final Iterator<SocialLoginProvider> lpiter = getLoginProviders().stream()
            .filter(lp -> !excl.contains(lp.getProgrammaticName()))
            .iterator();
        lpiter.forEachRemaining(lp -> {
            sb.append('\'').append(lp.getProgrammaticName()).append('\'');
            if(lpiter.hasNext())
                sb.append(',');
        });
        sb.append(']');
        return sb.toString();
    }

    /**
     * Gets login providers string.
     *
     * @return the login providers string
     */
    public String getLoginProvidersString()
    {
        return getLoginProvidersString(null);
    }

    /**
     * Gets provider for programmatic name.
     *
     * @param programmaticName the programmatic name
     *
     * @return the provider for programmatic name
     */
    public SocialLoginProvider getProviderForProgrammaticName(String programmaticName)
    {
        return getLoginProviders().stream().filter(slp -> Objects.equals(slp.getProgrammaticName(), programmaticName))
            .findFirst().orElseGet(() -> new SocialLoginProvider(programmaticName, programmaticName));
    }

    /**
     * Gets message acceptor.
     *
     * @return the message acceptor
     */
    public Consumer<Notification> getMessageAcceptor()
    {
        return _messageAcceptor;
    }

    /**
     * Gets login service.
     *
     * @return the login service
     */
    public SocialLoginService getLoginService()
    {
        return _loginService;
    }

    /**
     * Gets content builder.
     *
     * @return the content builder
     */
    public SocialLoginContentBuilder getContentBuilder()
    {
        return _contentBuilder;
    }
}
