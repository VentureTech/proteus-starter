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

package com.example.app.ksupport

import arrow.core.Either
import arrow.syntax.function.pipe
import co.proteus.url_shortener.sdk.URLShortener
import com.example.app.kotlin.ApplicationError
import com.example.app.kotlin.flatTryRailway
import com.example.app.kotlin.lazyLogger
import com.example.app.kotlin.toApplicationError
import com.google.common.util.concurrent.RateLimiter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL

class URLShortenMaxTriesReachedError(val url: String) : ApplicationError {
    override val msg: String
        get() = "Failed to shorten url: $url.  Max tries reached."
}

/**
 * Wraps [URLShortener] in a rate limiter and a retry loop.
 *
 * @author Alan Holt (aholt@proteus.co)
 * @since 1/24/2019
 */
@Service
class URLShortenerService(
    @Value("\${proteus-url-shortener-key}") private val key: String
) {
    private val logger by lazyLogger()

    private fun createShortener(): URLShortener = URLShortener(key)

    fun createShortenedURL(url: String): Either<ApplicationError, String> = flatTryRailway { tryShorten(url) }

    @Suppress("UnstableApiUsage")
    private tailrec fun tryShorten(
        toShorten: String,
        limiter: RateLimiter = RateLimiter.create(0.75),
        shortener: URLShortener = createShortener(),
        maxTries: Int = 5,
        currentTry: Int = 0,
        lastError: ApplicationError? = null
    ): Either<ApplicationError, String> {
        if (currentTry >= maxTries) {
            return Either.left(
                lastError ?: URLShortenMaxTriesReachedError(toShorten)
            )
        } else {
            val exceptionReceived: Exception = try {
                return URL(toShorten).pipe(shortener::shorten).shortURL.pipe(Either.Companion::right)
            } catch (e: Exception) {
                logger.error("Error occurred attempting to shorten URL: $toShorten.  Shortener will attempt " +
                    "${maxTries - currentTry} more times to shorten before giving up.", e)
                e
            }

            limiter.acquire()

            return tryShorten(
                toShorten,
                limiter,
                shortener,
                maxTries,
                currentTry + 1,
                exceptionReceived.toApplicationError()
            )
        }
    }
}