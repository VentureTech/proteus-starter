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

package com.example.app.config.automation

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.proteusframework.core.automation.DataConversion
import net.proteusframework.core.automation.SQLDataConversion
import net.proteusframework.core.automation.TaskQualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile

/**
 * Version 1 data conversions.
 * @author Russ Tennant (russ@i2rd.com)
 */
@Profile(["automation", "com.example.app.demo.1"])
@Configuration
@Lazy
@SuppressFBWarnings(['SE_NO_SERIALVERSIONID', 'LI_LAZY_INIT_STATIC', 'MS_SHOULD_BE_FINAL'])
@SuppressWarnings("LongLine")
class ProjectDataConversion1
{
    /** IDENTIFIER constant for LDP data conversions */
    private static final String IDENTIFIER = 'starter-app'

    /**
     * Add Note.
     * 2017.01.06 at 22:13 UTC
     * @return Bean.
     */
    @TaskQualifier(TaskQualifier.Type.data_conversion)
    @Bean
    DataConversion dataConversion_201701300931()
    {
        def stmts = [
            $/update page set name = 'Dashboard' where page_id = 169/$,
            $/update page set name = 'Login' where page_id = 166/$,
            $/update page set name = 'My Preferences' where page_id = 185/$
        ]
        new SQLDataConversion(IDENTIFIER, "Fix Backend Page Names", 201701300931, false, null, null, null, stmts)
    }

    /**
     * Data conversion #1
     * @return Bean.
     */
/*
    @TaskQualifier(TaskQualifier.Type.data_conversion)
    @Bean
    public DataConversion dataConversion_1()
    {
        def ddl = [
            $/CREATE TABLE FOO (foo text)/$,
        ] as String[]
        createSchemaUpdate(IDENTIFIER,
            'Describe what this data conversion does.',
            2016_09_11_13_11 *//*YYYY-MM-dd-HH-mm*//*,
            false,
            ddl);
    }
*/
}
