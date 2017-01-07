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

import java.io.IOException;

import net.proteusframework.core.CommandUtil;

/**
 * Just an example.
 *
 * @author Russ Tennant (russ@i2rd.com)
 * @since 12/4/13 3:03 PM
 */
public class ExampleMain
{

    /** object . */
    private Object _object;

    /**
     * Constructor.
     */
    public ExampleMain()
    {
        super();
    }

    /**
     * Command line.
     *
     * @param args arguments.
     * @throws IOException on error.
     */
    public static void main(String... args) throws IOException
    {
        String result = CommandUtil.ask("How you doing? ");
        System.out.format("That's cool. Glad to hear your doing, \"%s\".%n", result);
    }
}
