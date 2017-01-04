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

package com.example.app.model.terminology;

import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.ObjectStreamException;

import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.spring.ApplicationContextUtils;

import static com.example.app.model.terminology.DefaultProfileTermProviderLOK.COMPANIES;
import static com.example.app.model.terminology.DefaultProfileTermProviderLOK.COMPANY;

/**
 * Term provider for profile API referenced in a UI.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@Component
@Qualifier("default")
@I18NFile(
    symbolPrefix = "com.example.app.model.terminology.DefaultProfileTermProvider",
    i18n = {
        @I18N(symbol = "company", l10n = @L10N("Development Provider")),
        @I18N(symbol = "companies", l10n = @L10N("Development Providers"))
    }
)
public class DefaultProfileTermProvider implements ProfileTermProvider
{
    private static final long serialVersionUID = 54911207722013603L;

    @Override
    public TextSource company()
    {
        return COMPANY();
    }

    @Override
    public TextSource companies()
    {
        return COMPANIES();
    }

    private Object readResolve() throws ObjectStreamException
    {
        ApplicationContext context = ApplicationContextUtils.getInstance().getContext();
        assert context != null;
        return context.getBean(DefaultProfileTermProvider.class);
    }

    @Contract(pure = true)
    private Object writeReplace() throws ObjectStreamException
    {
        return this;
    }
}