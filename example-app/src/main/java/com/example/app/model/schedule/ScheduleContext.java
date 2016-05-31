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

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.temporal.TemporalAmount;

/**
 * Context information for scheduling purposes.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public class ScheduleContext
{
    @Nonnull
    private final TemporalAmount _duration;
    @Nonnull
    private final Instant _startTime;


    /**
     * Instantiates a new Schedule context.
     *
     * @param duration the duration to schedule.
     * @param startTime the start time.
     */
    public ScheduleContext(@Nonnull TemporalAmount duration, @Nonnull Instant startTime)
    {
        _duration = duration;
        _startTime = startTime;
    }

    /**
     * Get the duration to schedule.
     * @return the duration to schedule.
     */
    @Nonnull
    public TemporalAmount getDuration()
    {
        return _duration;
    }

    /**
     * Get the start time.
     * @return the start time.
     */
    @Nonnull
    public Instant getStartTime()
    {
        return _startTime;
    }
}
