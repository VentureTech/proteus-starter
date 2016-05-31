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

package com.example.app.model.resource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;

import static com.example.app.model.resource.ResourceVisibilityLOK.NAME_PRIVATE;
import static com.example.app.model.resource.ResourceVisibilityLOK.NAME_PUBLIC;

/**
 * Enum for defining visibility status of a Resource
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/24/15 9:05 AM
 */
@I18NFile(
    symbolPrefix = "com.lrlabs.model.resource.ResourceVisibility",
    i18n = {
        @I18N(symbol = "Name Public", l10n = @L10N("Public")),
        @I18N(symbol = "Name Private", l10n = @L10N("Private"))
    }
)
public enum ResourceVisibility implements NamedObject
{
    /** Public Visibility */ 
    Public(NAME_PUBLIC()),
    /** Private Visibility */
    Private(NAME_PRIVATE());

    private final TextSource _name;

    ResourceVisibility(@Nonnull TextSource name)
    {
        _name = name;
    }

    @Nonnull
    @Override
    public TextSource getName()
    {
        return _name;
    }

    @Nullable
    @Override
    public TextSource getDescription()
    {
        return null;
    }

    /**
     *   Get a list of values for a combo box. This includes a null value at the 0 index.
     *   @return a list of values for a combo box
     */
    public static List<ResourceVisibility> getValuesForCombo()
    {
        List<ResourceVisibility> values = new ArrayList<>();
        Collections.addAll(values, ResourceVisibility.values());
        values.add(0, null);
        return values;
    }


}
