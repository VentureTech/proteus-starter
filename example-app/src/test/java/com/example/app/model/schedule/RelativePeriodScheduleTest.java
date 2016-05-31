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

import org.testng.annotations.Test;

import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public class RelativePeriodScheduleTest
{

    /**
     * Test schedule.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testSchedule() throws Exception
    {
        Instant startTime = Instant.EPOCH;
        Duration duration = Duration.ofDays(1);
        RelativePeriodSchedule schedule = new RelativePeriodSchedule();
        schedule.setPeriod(Period.ofDays(2));
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        assertTrue(schedule.schedule(scheduleContext).isEmpty());

        duration = Duration.ofDays(2);
        scheduleContext = new ScheduleContext(duration, startTime);
        assertEquals(schedule.schedule(scheduleContext).size(), 1);
        assertEquals(schedule.schedule(scheduleContext).get(0), Instant.EPOCH.plus(schedule.getPeriod()));

        duration = Duration.ofDays(100);
        scheduleContext = new ScheduleContext(duration, startTime);
        assertEquals(schedule.schedule(scheduleContext).size(), 1);
        assertEquals(schedule.schedule(scheduleContext).get(0), Instant.EPOCH.plus(schedule.getPeriod()));
    }

    /**
     * Test schedule repeat.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testScheduleRepeat() throws Exception
    {
        Instant startTime = Instant.EPOCH;
        Duration duration = Duration.ofDays(1);
        RelativePeriodSchedule schedule = new RelativePeriodSchedule();
        schedule.setRepeat(true);
        schedule.setPeriod(Period.ofDays(2));
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        assertTrue(schedule.schedule(scheduleContext).isEmpty());

        duration = Duration.ofDays(2);
        scheduleContext = new ScheduleContext(duration, startTime);
        assertEquals(schedule.schedule(scheduleContext).size(), 1);
        assertEquals(schedule.schedule(scheduleContext).get(0), Instant.EPOCH.plus(schedule.getPeriod()));

        duration = Duration.ofDays(30);
        scheduleContext = new ScheduleContext(duration, startTime);
        assertEquals(schedule.schedule(scheduleContext).size(), 15);
        assertEquals(schedule.schedule(scheduleContext).get(0), Instant.EPOCH.plus(schedule.getPeriod()));
        assertEquals(schedule.schedule(scheduleContext).get(14), Instant.EPOCH.plus(duration));
    }

    /**
     * Test schedule repeat.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testScheduleRepeatPast() throws Exception
    {
        Instant startTime = Instant.EPOCH;
        Duration duration = Duration.ofDays(1);
        RelativePeriodSchedule schedule = new RelativePeriodSchedule();
        schedule.setTemporalDirection(TemporalDirection.PAST);
        schedule.setRepeat(true);
        schedule.setPeriod(Period.ofDays(2));
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        assertTrue(schedule.schedule(scheduleContext).isEmpty());

        duration = Duration.ofDays(2);
        scheduleContext = new ScheduleContext(duration, startTime);
        assertEquals(schedule.schedule(scheduleContext).size(), 1);
        assertEquals(schedule.schedule(scheduleContext).get(0), Instant.EPOCH.minus(schedule.getPeriod()));

        duration = Duration.ofDays(30);
        scheduleContext = new ScheduleContext(duration, startTime);
        assertEquals(schedule.schedule(scheduleContext).size(), 15);
        assertEquals(schedule.schedule(scheduleContext).get(0), Instant.EPOCH.minus(schedule.getPeriod()));
        assertEquals(schedule.schedule(scheduleContext).get(14), Instant.EPOCH.minus(duration));
    }

    /**
     * Test schedule time.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testScheduleTime() throws Exception
    {
        Instant startTime = Instant.EPOCH;
        Duration duration = Duration.ofDays(1);
        RelativePeriodSchedule schedule = new RelativePeriodSchedule();
        schedule.setRepeat(true);
        final Time time = Time.valueOf("12:00:00");
        schedule.setTime(time);
        schedule.setPeriod(Period.ofDays(2));
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        assertTrue(schedule.schedule(scheduleContext).isEmpty());

        duration = Duration.ofDays(2);
        scheduleContext = new ScheduleContext(duration, startTime);
        assertEquals(1, schedule.schedule(scheduleContext).size());
        assertEquals(schedule.schedule(scheduleContext).get(0),
            Instant.EPOCH.plus(schedule.getPeriod()).plusSeconds(time.toLocalTime().toSecondOfDay()));

        duration = Duration.ofDays(30);
        scheduleContext = new ScheduleContext(duration, startTime);
        assertEquals(15, schedule.schedule(scheduleContext).size());
        assertEquals(schedule.schedule(scheduleContext).get(0),
            Instant.EPOCH.plus(schedule.getPeriod()).plusSeconds(time.toLocalTime().toSecondOfDay()));
        assertEquals(schedule.schedule(scheduleContext).get(14),
            Instant.EPOCH.plus(duration).plusSeconds(time.toLocalTime().toSecondOfDay()));
    }
}