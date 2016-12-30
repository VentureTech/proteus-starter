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

import com.example.app.config.ProjectInformation;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.urlshortener.Urlshortener;
import com.google.api.services.urlshortener.UrlshortenerRequestInitializer;
import com.google.api.services.urlshortener.model.Url;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.function.Function;

import net.proteusframework.core.StringFactory;

/**
 * Service for shortening URLs.  Uses Google URL Shortener service.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/30/16
 */
@Service
public class URLShortenerService
{
    private static final Logger _logger = LogManager.getLogger(URLShortenerService.class);

    @Value("${google-url-shortener-key}") private String _urlShortenerKey;

    /**
     * Create shortened url.
     *
     * @param url the url.
     *
     * @return the shortened URL.
     * @throws IOException if the URL cannot be shortened.
     */
    public String createShortenedURL(String url) throws IOException
    {
        Url toInsert = new Url();
        toInsert.setLongUrl(url);
        return
            tryURLShortenOperation(shortener -> {
                final Urlshortener.Url.Insert insertOp;
                try
                {
                    insertOp = shortener.url().insert(toInsert);
                    return insertOp.execute().getId();
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Unable to shorten URL.", e);
                }
            }, 0.75);
    }

    @Nonnull
    Urlshortener createURLShortener()
    {
        if(StringFactory.isEmptyString(_urlShortenerKey))
            throw new RuntimeException(
                "google-url-shortener-key should be set in default.properties in order to use the url shortener service.");
        return new Urlshortener.Builder(new ApacheHttpTransport(), new GsonFactory(), null)
            .setGoogleClientRequestInitializer(new UrlshortenerRequestInitializer(_urlShortenerKey))
            .setApplicationName(ProjectInformation.getName())
            .build();
    }

    String tryURLShortenOperation(Function<Urlshortener, String> operation, double rate) throws IOException
    {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final Urlshortener shortener = createURLShortener();
        final RateLimiter rateLimiter = RateLimiter.create(rate);
        IOException lastException = null;
        int retries = 0;
        while (retries++ < 5)
        {
            try
            {
                return operation.apply(shortener);
            }
            catch (RuntimeException e)
            {
                if(e.getCause() instanceof IOException)
                    lastException = (IOException) e.getCause();
                if(e.getCause() instanceof InterruptedIOException)
                {
                    if(_logger.isDebugEnabled())
                    {
                        _logger.debug("Could not shorten URL due to connection error. Retry#" + retries
                                      + ". Time spent: " + stopwatch, e);
                    }
                    rateLimiter.acquire();
                }
                else
                    throw e;
            }
        }
        if(lastException != null)
            throw lastException;
        else
            throw new IOException("Unable to shorten URL. Retried " + retries + " times for " + stopwatch);
    }
}
