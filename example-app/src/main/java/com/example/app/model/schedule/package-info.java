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

/**
 * Configuration classes for the Activity models.
 * Instances of these classes should not be shared.
 */
@I18NFiles({
    @I18NFile(
        file = "ScheduleText",
        classVisibility = I18NFile.Visibility.PUBLIC,
        symbolPrefix = "ScheduleText",
        i18n = {
            @I18N(symbol = "Years", l10n = @L10N("Years")),
            @I18N(symbol = "Months", l10n = @L10N("Months")),
            @I18N(symbol = "Days", l10n = @L10N("Days")),
            @I18N(symbol = "Before", l10n = @L10N("Before")),
            @I18N(symbol = "After", l10n = @L10N("After"))
        }
    )
})
package com.example.app.model.schedule;

import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.I18NFiles;
import net.proteusframework.core.locale.annotation.L10N;