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

package com.example.app.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import net.proteusframework.core.config.ExecutorConfig;
import net.proteusframework.internet.http.ResponseStatus;
import net.proteusframework.users.model.PhoneNumber;

/**
 * Sms Service Integration.  Provides a consistent API for sending Sms messages programmatically.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/10/15 12:42 PM
 */
@Service(SmsService.SERVICE_NAME)
public class SmsService
{
    static final String SERVICE_NAME = "ldp.SmsService";

    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(SmsService.class);
    private static final Pattern PAT_PHONE_SEPARATORS = Pattern.compile("[\\-()]");

    @Autowired
    private ExecutorConfig _executorConfig;

    @Value("${tropo_endpoint}")
    private String _tropoEndpoint;
    @Value("${tropo_message_api_key}")
    private String _tropoMessageApiKey;

    /**
     * Send an Sms message to the specified phone number with the given content.
     *
     * @param recipient the intended recipient of the message
     * @param content the content of the message
     *
     * @return a Future representing the success of the action.
     */
    public Future<Boolean> sendSms(@Nonnull final PhoneNumber recipient, @Nonnull final String content)
    {
        return _executorConfig.executorService().submit(() -> {
            //All we want is a raw number with a '+' in front -- so we strip the formatting.
            String numberToDial = PAT_PHONE_SEPARATORS.matcher(recipient.toExternalForm()).replaceAll("");

            Gson gson = new GsonBuilder().create();

            TropoRequest request = new TropoRequest();
            request.action = "create";
            request.message = content;
            request.numberToDial = numberToDial;
            request.token = _tropoMessageApiKey;

            try
            {
                return Request.Post(_tropoEndpoint)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .bodyString(gson.toJson(request), ContentType.APPLICATION_JSON)
                    .execute().handleResponse(httpResponse -> {
                        if (httpResponse.getStatusLine().getStatusCode() != ResponseStatus.OK.getCode())
                        {
                            _logger.warn("Sms Message sending failed.  Status code: "
                                         + httpResponse.getStatusLine().getStatusCode() + " was returned.");
                            return false;
                        }
                        String responseString = EntityUtils.toString(httpResponse.getEntity());
                        TropoResponse response = gson.fromJson(responseString, TropoResponse.class);
                        if (response.success == null || !response.success)
                        {
                            _logger.warn("Sms Message sending failed. Tropo Response: " + responseString);
                            return false;
                        }
                        return true;
                    });
            }
            catch (IOException e)
            {
                _logger.error("Unable to send Sms message request to Tropo.", e);
                return false;
            }
        });
    }
}

/**
 * Json Mapping Class for a Tropo Request.
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON Object")
class TropoRequest
{
    public String action;
    public String token;
    public String message;
    public String numberToDial;
}

/**
 * Json Mapping Class for a Tropo Response
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON Object")
@SuppressWarnings("InstanceVariableNamingConvention")
class TropoResponse
{
    public Boolean success;
    public String token;
    public String id;
}