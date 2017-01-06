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

package com.example.app.profile.model.company;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;

/**
 * Enum defining possible status of a {@link Company}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 6/27/16 10:22 AM
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.model.company.CompanyStatus",
    i18n = {
        @I18N(symbol = "Active Name", l10n = @L10N("Active")),
        @I18N(symbol = "Inactive Name", l10n = @L10N("Inactive"))
    }
)
public enum CompanyStatus implements NamedObject
{
    /** Active */
    Active(CompanyStatusLOK.ACTIVE_NAME()),
    /** Inactive */
    Inactive(CompanyStatusLOK.INACTIVE_NAME())
    ;

    private final TextSource _name;

    CompanyStatus(@Nonnull TextSource name)
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
}
