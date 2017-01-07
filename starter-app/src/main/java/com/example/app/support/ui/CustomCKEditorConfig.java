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

package com.example.app.support.ui;

import javax.annotation.Nonnull;

/**
 * Rich editor config types.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/16/15 9:59 AM
 */
public enum CustomCKEditorConfig
{
    /** Standard ck editor configuration */
    standard("standard-custom.js"),
    /** Minimal ck editor configuration */
    minimal("minimal-custom.js");

    private final String _jsFile;

    CustomCKEditorConfig(@Nonnull String jsFile)
    {
        _jsFile = jsFile;
    }


    @Override
    public String toString()
    {
        return _jsFile;
    }
}
