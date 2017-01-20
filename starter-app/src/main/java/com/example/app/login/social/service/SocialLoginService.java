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

import com.example.app.login.social.ui.SocialLoginElement;
import com.example.app.login.social.ui.SocialLoginParams;

import java.util.List;

import net.proteusframework.cms.component.generator.Renderer;
import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;

/**
 * Interface for defining a Social Login Integration
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/19/17
 */
public interface SocialLoginService extends NamedObject
{
    /**
     * Create a Login renderer for this service
     *
     * @param loginParams the parameters passed from the SocialLogin UI Component
     *
     * @return the Login renderer
     */
    Renderer<SocialLoginElement> createLoginRenderer(SocialLoginParams loginParams);


    /**
     * Get a list of supported {@link SocialLoginProvider}s for this service
     * @return the supported Providers
     */
    List<SocialLoginProvider> getSupportedProviders();

    /**
     * Handle the Login Callback
     * @param request the request
     * @param response the response
     * @param loginParams the login parameters
     * @return boolean flag.  True if the callback resulted in the user being logged in.  False otherwise
     */
    boolean handleLoginCallback(Request request, Response response, SocialLoginParams loginParams);

    /**
     * Get the identifier for this service
     * @return the identifier
     */
    String getServiceIdentifier();
}
