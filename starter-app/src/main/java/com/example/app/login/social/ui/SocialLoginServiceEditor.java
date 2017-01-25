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

import javax.annotation.Nonnull;
import java.util.function.Function;

import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;

/**
 * Wrapper Class Defining An Additional Editor Field For {@link SocialLoginEditor}
 *
 * @param <V> the type of the value being edited
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1 /24/17
 */
public class SocialLoginServiceEditor<V>
{
    private final String _property;
    private final ValueEditor<V> _editor;
    private final Function<V, String> _valueToString;
    private final Function<String, V> _stringToValue;

    /**
     * Instantiates a new Social login service editor.
     *
     * @param property the property for the value to be added into the ContentBuilder
     * @param editor the editor
     * @param valueToString conversion function for converting the value into a string for persistence
     * @param stringToValue conversion function for converting the string back into the value from persistence
     */
    public SocialLoginServiceEditor(@Nonnull String property, @Nonnull ValueEditor<V> editor,
        @Nonnull Function<V, String> valueToString, @Nonnull Function<String, V> stringToValue)
    {
        _property = property;
        _editor = editor;
        _valueToString = valueToString;
        _stringToValue = stringToValue;
    }

    /**
     * Gets property.
     *
     * @return the property
     */
    @Nonnull
    public String getProperty()
    {
        return _property;
    }

    /**
     * Gets editor.
     *
     * @return the editor
     */
    @Nonnull
    public ValueEditor<V> getEditor()
    {
        return _editor;
    }

    /**
     * Gets value to string.
     *
     * @return the value to string
     */
    @Nonnull
    public Function<V, String> getValueToString()
    {
        return _valueToString;
    }

    /**
     * Gets string to value.
     *
     * @return the string to value
     */
    @Nonnull
    public Function<String, V> getStringToValue()
    {
        return _stringToValue;
    }
}
