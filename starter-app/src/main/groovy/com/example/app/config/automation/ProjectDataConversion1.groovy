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
@SuppressWarnings(['SE_NO_SERIALVERSIONID', 'LI_LAZY_INIT_STATIC', 'MS_SHOULD_BE_FINAL', 'LongLine'])
class ProjectDataConversion1
{
    /** IDENTIFIER constant for LDP data conversions */
    private static final String IDENTIFIER = 'starter-app'

    /**
     * Set names on some of the Proteus Backend pages.
     * This is only intended for use with projects that have been created using the proteus snapshot.  Hopefully, the site_id is
     * valid for other installs.
     * 2017.01.06 at 22:13 UTC
     * @return Bean.
     */
    @TaskQualifier(TaskQualifier.Type.data_conversion)
    @Bean
    DataConversion dataConversion_201701300931()
    {
        def stmts = [
            $/update page p set name='Dashboard' from pageelementpath pep where pep.site_id=4 AND pep.path=
'config/dashboard' AND pep.pageelementpath_id=p.pageelementpath_id/$,
            $/update page p set name='Login' from pageelementpath pep where pep.site_id=4 AND pep.path=
'account/login' AND pep.pageelementpath_id=p.pageelementpath_id/$,
            $/update page p set name='My Preferences' from pageelementpath pep where pep.site_id=4 AND pep.path=
'account/preferences' AND pep.pageelementpath_id=p.pageelementpath_id/$
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
