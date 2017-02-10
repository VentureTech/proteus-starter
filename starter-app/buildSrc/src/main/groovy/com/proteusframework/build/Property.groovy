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

package com.proteusframework.build

/**
 * Build property
 * @author Russ Tennant (russ@i2rd.com)
 * @since 12/3/13 4:13 PM
 */
class Property
{
    def name
    def description
    def required = true

    boolean test(def settings)
    {
        def res = true
        if (!settings.hasProperty(name))
        {
            def envValue = System.getenv().get(name)
            if(envValue)
            {
                settings.gradle.rootProject.ext[name] = envValue
            }
            else if (required)
            {
                println "Missing property: ${toString()}"
                res = false
            }
            else
            {
                println "Defaulting non-required property: ${toString()} to NOT_SPECIFIED"
                settings.gradle.rootProject.ext[name] = 'NOT_SPECIFIED'
            }
        }
        res
    }

    String toString()
    {
        return "${name} [${description}]"
    }
}
