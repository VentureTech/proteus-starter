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

package com.example.app.profile.ui.visibility;

import com.example.app.kprofile.ui.visibility.MembershipOperationVisibilityCondition;

import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;

/**
 * Text for {@link MembershipOperationVisibilityCondition}
 *
 * @author Alan Holt (aholt@proteus.co)
 * @since 11/14/2018
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.visibility.MembershipOperationVisibilityConditionText",
    i18n = {
        @I18N(symbol = "Label Field Checker", l10n = @L10N("Membership Operation Checker")),
        @I18N(symbol = "Label Field Membership Operation", l10n = @L10N("Membership Operation"))
    }
)
public class MembershipOperationVisibilityConditionText
{
    private MembershipOperationVisibilityConditionText() {}

    /**
     * Label field checker localized object key.
     *
     * @return the localized object key
     */
    public static LocalizedObjectKey LABEL_FIELD_CHECKER()
    {
        return MembershipOperationVisibilityConditionTextLOK.LABEL_FIELD_CHECKER();
    }

    /**
     * Label membership operation localized object key.
     *
     * @return the localized object key
     */
    public static LocalizedObjectKey LABEL_FIELD_MEMBERSHIP_OPERATION()
    {
        return MembershipOperationVisibilityConditionTextLOK.LABEL_FIELD_MEMBERSHIP_OPERATION();
    }
}

