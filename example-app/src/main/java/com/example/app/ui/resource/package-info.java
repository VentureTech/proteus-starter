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
 * UI Classes and Objects related to Resources
 */
@I18NFiles({
    @I18NFile(
        file = "ResourceText",
        classVisibility = I18NFile.Visibility.PUBLIC,
        symbolPrefix = "ResourceText",
        i18n = {
            @I18N(symbol = "Label Source", l10n = @L10N("Source")),
            @I18N(symbol = "Label Author", l10n = @L10N("Author"))
        }
    )
})
package com.example.app.ui.resource;

import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.I18NFiles;
import net.proteusframework.core.locale.annotation.L10N;