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
 
package com.example.app;

import org.testng.annotations.Test;

import com.i2rd.unit.test.BaseTestAnnotationConfig;

/**
 * This example is to prevent Jenkins build from failing due to no unit tests.
 * @author Joe Developer
 */
public class ExampleTest extends BaseTestAnnotationConfig
{

    /** Example test method.  */
    @Test(groups = {"unit"})
    public void test() 
    {
        // Implement a test
    }
    
}
