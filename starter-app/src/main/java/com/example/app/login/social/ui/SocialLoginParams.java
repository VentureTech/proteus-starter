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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

import java.util.List;
import java.util.function.Consumer;

import net.proteusframework.ui.miwt.component.composite.Message;

/**
 * Parameters used to construct Social Login UIs
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/19/17
 */
public class SocialLoginParams
{
    private final String _callbackURI;
    private final SocialLoginMode _mode;
    private final Consumer<Message> _messageAcceptor;
    private final ImmutableList<SocialLoginProvider> _loginProviders;

    /**
     * Instantiates a new Social login params.
     *
     * @param callbackURI the callback uri
     * @param mode the mode
     * @param messageAcceptor the message acceptor
     * @param providers the providers
     */
    public SocialLoginParams(String callbackURI, SocialLoginMode mode,
        Consumer<Message> messageAcceptor, List<SocialLoginProvider> providers)
    {
        _callbackURI = callbackURI;
        _mode = mode;
        _messageAcceptor = messageAcceptor;
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
    public String getCallbackURI()
    {
        return _callbackURI;
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
     * @return the login providers string
     */
    public String getLoginProvidersString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        final UnmodifiableIterator<SocialLoginProvider> lpiter = getLoginProviders().iterator();
        lpiter.forEachRemaining(lp -> {
            sb.append('\'').append(lp.getProgrammaticName()).append('\'');
            if(lpiter.hasNext())
                sb.append(',');
        });
        sb.append(']');
        return sb.toString();
    }

    /**
     * Gets message acceptor.
     *
     * @return the message acceptor
     */
    public Consumer<Message> getMessageAcceptor()
    {
        return _messageAcceptor;
    }
}
