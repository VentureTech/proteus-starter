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

import groovy.beans.Bindable

/**
 * Project model.
 * @author Russ Tennant (russ@i2rd.com)
 */
class ProjectModel
{
    @Bindable String appGroup
    @Bindable String appName
    @Bindable boolean copyDemo
    @Bindable File destinationDirectory
    @Bindable boolean createDB
}
