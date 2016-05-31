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

package com.example.app.model.company;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;

import static com.example.app.model.company.LocationStatusLOK.*;

/**
 * Statuses for a {@link Location}
 *
 * @author  Ken Logan (klogan@venturetech.net)
 */
@SuppressWarnings("unused")
@I18NFile(
    symbolPrefix = "com.poultryexchange.tpe.model.LocationStatus",
    i18n = {
        @I18N(symbol = "Name Pending", l10n = @L10N("Pending")),
        @I18N(symbol = "Name Active", l10n = @L10N("Active")),
        @I18N(symbol = "Name Inactive", l10n = @L10N("Inactive")),
    }
)
public enum LocationStatus implements NamedObject
{
    /** Pending */
    PENDING(NAME_PENDING()),
    /** Active */
    ACTIVE(NAME_ACTIVE()),
    /** Inactive */
    INACTIVE(NAME_INACTIVE()),
    ;

    /** Localized name */
    private final TextSource _name;

    LocationStatus(TextSource name)
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
        return TextSources.EMPTY;
    }
}
