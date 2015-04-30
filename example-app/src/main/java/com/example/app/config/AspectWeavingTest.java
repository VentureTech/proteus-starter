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

package com.example.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Test if load time weaving is occuring for this package.
 * @author Russ Tennant (russ@i2rd.com)
 */
@Configurable
public class AspectWeavingTest
{
    /** Flag. */
    private boolean _configured;

    /**
     * Spring injection.
     * @param config configuration.
     */
    @Autowired
    public void setAppConfig(ExampleAppProjectConfig config)
    {
        _configured = (config != null);
    }

    /**
     * Test if configured.
     * @return true or false.
     */
    public boolean isConfigured()
    {
        return _configured;
    }
}
