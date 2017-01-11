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

import com.example.app.config.ProjectInformation;

import net.proteusframework.ui.management.ApplicationRegistry;

/**
 * Application definition for {@link ApplicationRegistry} use.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public final class Application
{
    /** Application NAME. */
    public static final String NAME = ProjectInformation.APPLICATION_NAME;
    /** Application Session. */
    public static final String SESSION = "app";

    private Application()
    {
    }
}
