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

package com.example.app.schedule.model;

/**
 * Types of schedules.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public enum ScheduleType
{
    /** Schedule that is configured internally. */
    internal,
    /** Schedule that is configured externally. */
    external,
    /** Schedule that is configured relative to another event. */
    relative
}
