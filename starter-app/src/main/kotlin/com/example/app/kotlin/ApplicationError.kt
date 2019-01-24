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

package com.example.app.kotlin

import arrow.core.Either
import net.proteusframework.core.notification.Notification
import net.proteusframework.core.notification.NotificationImpl
import net.proteusframework.kotlin.miwt.toTextSource
import org.apache.logging.log4j.Logger

interface ApplicationError {
    val msg: String

    fun log(logger: Logger, prefix: String) {
        logger.error("$prefix $msg")
    }

    fun log(logger: Logger) {
        log(logger, "")
    }

    fun toNotifications(): List<Notification> = listOf(NotificationImpl.error(msg.toTextSource()))
}

interface ExceptionalApplicationError : ApplicationError {
    val e: Throwable

    override fun log(logger: Logger, prefix: String) {
        logger.error("${if (prefix.isNotEmpty()) "$prefix " else prefix}$msg", e)
    }

    override fun toNotifications(): List<Notification> = listOf(NotificationImpl.create(e, msg))
}

interface DelegatingApplicationError : ApplicationError {
    val e: ApplicationError

    override fun log(logger: Logger, prefix: String) {
        e.log(logger, prefix)
    }

    override fun toNotifications(): List<Notification> = e.toNotifications()
}

fun Throwable.toApplicationError() = object : ExceptionalApplicationError {
    override val e: Throwable
        get() = this@toApplicationError

    override val msg: String
        get() = e.message ?: ""
}

fun <V> tryRailway(theTry: () -> V): Either<ApplicationError, V> = flatTryRailway {
    Either.right(theTry())
}
fun <V> flatTryRailway(theTry: () -> Either<ApplicationError, V>) = try {
    theTry()
} catch (e: Throwable) {
    Either.left(e.toApplicationError())
}