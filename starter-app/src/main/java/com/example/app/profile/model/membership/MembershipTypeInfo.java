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

package com.example.app.profile.model.membership;

import com.example.app.support.service.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.EnumSet;

import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TransientLocalizedObjectKey;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;


/**
 * Defines Info for Predefined MembershipTypes
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 7/25/16 11:05 AM
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.model.membership.MembershipTypeInfo",
    i18n = {
        @I18N(symbol = "Name Company Admin", l10n = @L10N("Company Admin"))
    }
)
public enum MembershipTypeInfo implements NamedObject
{
    /** MembershipTypeInfo. */
    SystemAdmin(MembershipTypeInfoLOK.NAME_COMPANY_ADMIN(), "admin")
    //Additional Predefined MembershipTypes go here.
    ;

    private final LocalizedObjectKey _name;
    private final String _progId;

    @SuppressWarnings("NonFinalFieldInEnum") //Field is injected at runtime.
    private AppUtil _appUtil;

    MembershipTypeInfo(@Nonnull LocalizedObjectKey name, @Nonnull String progId)
    {
        _name = name;
        _progId = progId;
    }

    /**
     * Gets prog id.
     *
     * @return the prog id
     */
    @Nonnull
    public String getProgId()
    {
        return _progId;
    }

    /**
     * New name localized object key transient localized object key.
     *
     * @return the transient localized object key
     */
    public TransientLocalizedObjectKey getNewNameLocalizedObjectKey()
    {
        return _appUtil.copyLocalizedObjectKey(getName());
    }

    private void setAppUtil(AppUtil appUtil)
    {
        _appUtil = appUtil;
    }

    @Nonnull
    @Override
    public LocalizedObjectKey getName()
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
     * Injector class for MembershipTypeInfo.  Used to inject beans into the enum values.
     */
    @Component
    public static class MembershipTypeInfoInjector
    {
        @Autowired
        private AppUtil _appUtil;

        /**
         * Post construct.
         */
        @PostConstruct
        public void postConstruct()
        {
            for(MembershipTypeInfo mti : EnumSet.allOf(MembershipTypeInfo.class))
                mti.setAppUtil(_appUtil);
        }
    }
}
