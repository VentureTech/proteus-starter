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

package com.example.app.model.schedule;

/**
 * Defines the direction of scheduling in relation to the ScheduleContext.
 * @author Russ Tennant (russ@venturetech.net)
 */
public enum TemporalDirection
{
    /** Scheduling should occur in the future. */
    FUTURE,
    /** Scheduling should occur in the past. */
    PAST
}
