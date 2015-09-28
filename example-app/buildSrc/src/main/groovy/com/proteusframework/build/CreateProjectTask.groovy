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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Task to create a new project.
 * @author Russ Tennant (russ@i2rd.com)
 */
class CreateProjectTask extends DefaultTask
{
    CreateProjectTask()
    {
        group = 'Build Setup'
        description = 'Create new project using this project as the template.'
    }

    @TaskAction
    def doIt() {
        new CreateProjectUI(project)
    }
}
