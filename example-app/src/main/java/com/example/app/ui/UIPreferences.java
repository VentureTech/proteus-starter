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

package com.example.app.ui;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import net.proteusframework.core.notification.Notification;

/**
 * UI Preferences that are limited to SESSION scope
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/25/16 1:51 PM
 */
@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UIPreferences
{
    private final HashMap<String, Integer> _intPrefs = new HashMap<>();
    private final HashMap<String, Object> _objPrefs = new HashMap<>();
    private final List<Notification> _messages = new ArrayList<>();

    /**
     *   Get a stored Integer within UIPreferences
     *   @param key the Key to retrieve
     *   @return the stored value.
     */
    public Optional<Integer> getStoredInteger(String key)
    {
        return Optional.ofNullable(_intPrefs.get(key));
    }

    /**
     *   Set a stored Integer within UIPreferences
     *   @param key the Key to store
     *   @param value the value to store
     */
    public void setStoredInteger(String key, @Nullable Integer value)
    {
        _intPrefs.put(key, value);
    }

    /**
     *   Get a stored Object within UIPreferences
     *   @param key the Key to retrieve
     *   @return the stored value.
     */
    public Optional<Object> getStoredObject(String key)
    {
        return Optional.ofNullable(_objPrefs.get(key));
    }

    /**
     *   Set a stored Object within UIPreferences
     *   @param key the Key to store
     *   @param value the value to store
     */
    public void setStoredObject(String key, @Nullable Object value)
    {
        _objPrefs.put(key, value);
    }

    /**
     *   Add a message to be stored within UIPreferences.  It will be consumed upon calling {@link UIPreferences#consumeMessages()}
     *   @param message the message to add
     */
    public void addMessage(Notification message)
    {
        _messages.add(message);
    }

    /**
     *   Clears the messages within UIPreferences, returning them.
     *   @return messages
     */
    public List<Notification> consumeMessages()
    {
        List<Notification> messages = new ArrayList<>(_messages);
        _messages.clear();
        return messages;
    }
}
