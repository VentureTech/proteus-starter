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

import java.text.SimpleDateFormat

/**
 * Version class for build script.
 * @author Russ Tennant (russ@i2rd.com)
 * @since 12/3/13 4:02 PM
 */
class Version
{
    String originalVersion
    String thisVersion
    String status
    Date buildTime
    def project

    Version(String versionValue, def project)
    {
        this.project = project
        buildTime = new Date()
        originalVersion = versionValue
        if (originalVersion.endsWith('-SNAPSHOT'))
        {
            status = 'integration'
            def version = originalVersion.substring(0, originalVersion.length() - '-SNAPSHOT'.length())
            thisVersion = "${version}-${getTimestamp()}"
        }
        else
        {
            status = 'release'
            thisVersion = versionValue
        }
    }

    boolean isSnapshot()
    {
        originalVersion.endsWith('-SNAPSHOT')
    }

    String getTimestamp()
    {
        // Convert local file timestamp to UTC
        def format = new SimpleDateFormat('yyyyMMddHHmmss')
        format.setCalendar(Calendar.getInstance(TimeZone.getTimeZone('Etc/UTC')));
        return format.format(buildTime)
    }

    int getCommitAsNumber()
    {
        Integer.parseInt(project.gitinfo.commit.substring(0,7), 16)
    }

    String toString()
    {
        thisVersion
    }
}