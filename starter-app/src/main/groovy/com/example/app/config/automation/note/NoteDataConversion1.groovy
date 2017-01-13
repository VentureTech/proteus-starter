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

package com.example.app.config.automation.note

import net.proteusframework.core.automation.DataConversion
import net.proteusframework.core.automation.SQLDataConversion
import net.proteusframework.core.automation.TaskQualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile

/**
 * Data conversions for Note API.
 * @author Russ Tennant (russ@venturetech.net)
 */
@Profile(["automation", "development"])
@Configuration
@Lazy
class NoteDataConversion1
{
    private static final String IDENTIFIER = "starter-app-note"

    /**
     * Add Note.
     * 2017.01.06 at 22:13 UTC
     * @return Bean.
     */
    @TaskQualifier(TaskQualifier.Type.data_conversion)
    @Bean
    DataConversion dataConversion_201701062213()
    {
        def ddl = [
            $/create schema if not exists app/$,
            $/create schema if not exists audit/$,
            $/create table if not exists audit.REVINFO (REV int4 not null, REVTSTMP int8, primary key (REV))/$,
            $/create table app.Note (note_id int4 not null, createtime timestamp not null, 
lastmodtime timestamp not null, content varchar(4096) not null, createuser_id int8, lastmoduser_id int8, 
primary key (note_id))/$,
            $/create table audit.Note_AUD (note_id int4 not null, REV int4 not null, revtype int2, 
content varchar(4096), primary key (note_id, REV))/$,
            $/alter table app.Note add constraint FK_ke94el4i1mot6pii3qe6tdmf4 foreign key (createuser_id) 
references Role/$,
            $/alter table app.Note add constraint FK_6n12ahk93q6lislpl8wm13550 foreign key (lastmoduser_id) 
references Role/$,
            $/alter table audit.Note_AUD add constraint FK_svabcv1pycuqt7bcme481wuvg foreign key (REV) 
references audit.REVINFO/$,
            $/create sequence IF NOT EXISTS hibernate_sequence/$
        ]
        new SQLDataConversion(IDENTIFIER, "Add Note", 201701062213, false, null, ddl, null, null)
    }
}
