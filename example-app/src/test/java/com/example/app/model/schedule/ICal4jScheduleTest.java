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

import org.testng.Reporter;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.testng.Assert.*;

/**
 * Tests.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public class ICal4jScheduleTest
{

    /**
     * Log.
     *
     * @param message the message
     */
    static void log(Object message)
    {
        Reporter.log(String.valueOf(message), true);
    }

    /**
     * Test schedule.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testScheduleLastDayOfMonth() throws Exception
    {
        Instant startTime = Instant.EPOCH;
        Period duration = Period.ofMonths(2);
        String rrule = "FREQ=MONTHLY;BYMONTHDAY=-1;BYHOUR=12";
        ICal4jSchedule schedule = new ICal4jSchedule();
        schedule.setRepeat(true);
        schedule.setRecurrenceRule(rrule);
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        final List<Instant> instantList = schedule.schedule(scheduleContext);
        assertFalse(instantList.isEmpty(), "Not expecting empty list.");
        assertEquals(instantList.size(), 2);
        ZonedDateTime dateTime = ZonedDateTime.of(1970, 1, 31, 12, 0, 0, 0, UTC);
        assertEquals(instantList.get(0), dateTime.toInstant());
        dateTime = ZonedDateTime.of(1970, 2, 28, 12, 0, 0, 0, UTC);
        assertEquals(instantList.get(1), dateTime.toInstant());
    }

    /**
     * Test schedule.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testScheduleDoWDoM() throws Exception
    {
        Instant startTime = Instant.EPOCH;
        Period duration = Period.ofMonths(2);
        String rrule = "FREQ=MONTHLY;BYDAY=-1MO,-1WE,2MO,2WE;BYHOUR=12";
        ICal4jSchedule schedule = new ICal4jSchedule();
        schedule.setRepeat(true);
        schedule.setRecurrenceRule(rrule);
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        final List<Instant> instantList = schedule.schedule(scheduleContext);
        assertFalse(instantList.isEmpty(), "Not expecting empty list.");
        assertEquals(instantList.size(), 8);
        ZonedDateTime dateTime = ZonedDateTime.of(1970, 1, 12, 12, 0, 0, 0, UTC);
        assertEquals(instantList.get(0), dateTime.toInstant());
        dateTime = ZonedDateTime.of(1970, 2, 25, 12, 0, 0, 0, UTC);
        assertEquals(instantList.get(7), dateTime.toInstant());
    }

    /**
     * Test schedule.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testSchedulePast() throws Exception
    {
        Duration twoDays = Duration.ofDays(2);
        Instant startTime = Instant.EPOCH;
        Duration duration = Duration.ofDays(1);
        ICal4jSchedule schedule = new ICal4jSchedule();
        schedule.setRepeat(true);
        schedule.setTemporalDirection(TemporalDirection.PAST);
        String rrule = "FREQ=DAILY;INTERVAL=3;BYHOUR=0";
        schedule.setRecurrenceRule(rrule);
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        List<Instant> instantList = schedule.schedule(scheduleContext);
        assertTrue(instantList.isEmpty());
        duration = Duration.ofDays(3);
        scheduleContext = new ScheduleContext(duration, startTime);
        instantList = schedule.schedule(scheduleContext);
        assertEquals(instantList.size(), 1);
        assertEquals(instantList.get(0), Instant.EPOCH.minus(Duration.ofDays(1)));

        duration = Duration.ofDays(30);
        scheduleContext = new ScheduleContext(duration, startTime);
        instantList = schedule.schedule(scheduleContext);
        assertEquals(instantList.size(), 10);
        assertEquals(instantList.get(9), Instant.EPOCH.minus(Duration.ofDays(1)));
    }

    /**
     * Test schedule.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testSchedulePast2() throws Exception
    {
        Period eighteenMonths = Period.ofMonths(18);
        Instant startTime = Instant.EPOCH;
        ICal4jSchedule schedule = new ICal4jSchedule();
        schedule.setRepeat(true);
        schedule.setTemporalDirection(TemporalDirection.PAST);
        String rrule = "FREQ=YEARLY;BYMONTH=6;BYMONTHDAY=1;BYHOUR=12";
        schedule.setRecurrenceRule(rrule);
        ScheduleContext scheduleContext = new ScheduleContext(eighteenMonths, startTime);
        List<Instant> instantList = schedule.schedule(scheduleContext);
        assertEquals(instantList.size(), 1);

    }

    /**
     * Test schedule repeat.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testScheduleRepeatMonth() throws Exception
    {
        TemporalAmount twelveHours = Duration.ofHours(12);
        TemporalAmount thirtyDays = Period.ofDays(30);
        Instant startTime = Instant.EPOCH;
        TemporalAmount duration = Period.ofYears(1);
        ICal4jSchedule schedule = new ICal4jSchedule();
        schedule.setRepeat(true);
        String rrule = "FREQ=MONTHLY;BYSETPOS=-1;BYHOUR=12";
        schedule.setRecurrenceRule(rrule);
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        assertEquals(schedule.schedule(scheduleContext).size(), 12);
        assertEquals(schedule.schedule(scheduleContext).get(0),
            Instant.EPOCH.plus(twelveHours).plus(thirtyDays));
        assertEquals(schedule.schedule(scheduleContext).get(11),
            LocalDateTime.of(1970, Month.DECEMBER, 31, 12, 0).toInstant(ZoneOffset.UTC));
    }


    /**
     * Test schedule repeat.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testScheduleRepeatMonthSomeWeekdays() throws Exception
    {
        Instant startTime = Instant.EPOCH;
        TemporalAmount duration = Period.ofYears(1);
        ICal4jSchedule schedule = new ICal4jSchedule();
        schedule.setRepeat(true);
        String rrule = "FREQ=MONTHLY;BYDAY=MO,TU,WE,TH,FR;BYHOUR=3;BYMINUTE=30";
        schedule.setRecurrenceRule(rrule);
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        assertEquals(schedule.schedule(scheduleContext).size(), 261);
        assertEquals(schedule.schedule(scheduleContext).get(2),
            LocalDateTime.of(1970, Month.JANUARY, 5, 3, 30).toInstant(ZoneOffset.UTC));
        assertEquals(schedule.schedule(scheduleContext).get(260),
            LocalDateTime.of(1970, Month.DECEMBER, 31, 3, 30).toInstant(ZoneOffset.UTC));
    }

    /**
     * Test schedule repeat.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testScheduleRepeatMonthNoWeekdays() throws Exception
    {
        Instant startTime = Instant.EPOCH;
        TemporalAmount duration = Period.ofYears(1);
        ICal4jSchedule schedule = new ICal4jSchedule();
        schedule.setRepeat(true);
        String rrule = "FREQ=MONTHLY;BYHOUR=3;BYMINUTE=30";
        schedule.setRecurrenceRule(rrule);
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        final List<Instant> instantList = schedule.schedule(scheduleContext);
        assertEquals(instantList.size(), 12);
        assertEquals(instantList.get(0),
            LocalDateTime.of(1970, Month.JANUARY, 31, 3, 30).toInstant(ZoneOffset.UTC));
        assertEquals(instantList.get(11),
            LocalDateTime.of(1970, Month.DECEMBER, 31, 3, 30).toInstant(ZoneOffset.UTC));
    }

    /**
     * Test schedule repeat.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testScheduleRepeatMonthSomeWeekdays2() throws Exception
    {
        Instant startTime = Instant.EPOCH;
        TemporalAmount duration = Period.ofYears(1);
        ICal4jSchedule schedule = new ICal4jSchedule();
        schedule.setRepeat(true);
        String rrule = "FREQ=MONTHLY;INTERVAL=2;BYDAY=MO,TU,WE,TH,FR;BYHOUR=3;BYMINUTE=30";
        schedule.setRecurrenceRule(rrule);
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        assertEquals(schedule.schedule(scheduleContext).size(), 130);
        assertEquals(schedule.schedule(scheduleContext).get(2),
            LocalDateTime.of(1970, Month.FEBRUARY, 4, 3, 30).toInstant(ZoneOffset.UTC));
        assertEquals(schedule.schedule(scheduleContext).get(125),
            LocalDateTime.of(1970, Month.DECEMBER, 25, 3, 30).toInstant(ZoneOffset.UTC));
    }

    /**
     * Test schedule repeat.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testScheduleRepeatYear() throws Exception
    {
        Instant startTime = Instant.EPOCH;
        TemporalAmount duration = Period.ofYears(1);
        ICal4jSchedule schedule = new ICal4jSchedule();
        schedule.setRepeat(true);
        String rrule = "FREQ=YEARLY;BYMONTH=1;BYMONTHDAY=20;BYHOUR=12";
        schedule.setRecurrenceRule(rrule);
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        final List<Instant> instantList = schedule.schedule(scheduleContext);
        assertEquals(instantList.size(), 1);
        assertEquals(instantList.get(0),
            LocalDateTime.of(1970, Month.JANUARY, 20, 12, 0).toInstant(ZoneOffset.UTC));
    }

    /**
     * Test schedule repeat.
     *
     * @throws Exception the exception
     */
    @Test(groups = "unit")
    public void testScheduleRepeatYearWeek() throws Exception
    {
        Instant startTime = Instant.EPOCH;
        TemporalAmount duration = Period.ofYears(1);
        ICal4jSchedule schedule = new ICal4jSchedule();
        schedule.setRepeat(true);
        String rrule = "FREQ=YEARLY;BYMONTH=2;BYDAY=1MO,2TU,3WE,4TH,-1FR;BYHOUR=12";
        schedule.setRecurrenceRule(rrule);
        ScheduleContext scheduleContext = new ScheduleContext(duration, startTime);
        final List<Instant> instantList = schedule.schedule(scheduleContext);
        assertEquals(instantList.size(), 5);
        assertEquals(instantList.get(0),
            LocalDateTime.of(1970, Month.FEBRUARY, 2, 12, 0).toInstant(ZoneOffset.UTC));
        assertEquals(instantList.get(4),
            LocalDateTime.of(1970, Month.FEBRUARY, 27, 12, 0).toInstant(ZoneOffset.UTC));
    }
}