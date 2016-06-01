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

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;

import com.example.app.support.AppUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Transient;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.proteusframework.core.hibernate.dao.EntityRetriever;

import static java.time.ZoneOffset.UTC;
import static net.proteusframework.core.StringFactory.isEmptyString;

/**
 * Schedule backed by ical4j.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@Audited
public class ICal4jSchedule extends Schedule
{
    private static final long serialVersionUID = -1973637805917656270L;
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ICal4jSchedule.class);

    private boolean _repeat;
    private String _eventProgrammaticIdentifier;
    private String _recurrenceRule;
    @Nonnull
    private TemporalDirection _temporalDirection = TemporalDirection.FUTURE;

    @Override
    public Schedule copy()
    {
        ICal4jSchedule copy = new ICal4jSchedule();
        copy.setRecurrenceRule(getRecurrenceRule());
        copy.setRepeat(isRepeat());
        copy.setTemporalDirection(getTemporalDirection());
        copy.setEventProgrammaticIdentifier(getEventProgrammaticIdentifier());
        return copy;
    }

    /**
     * Get the event programmatic identifier.
     *
     * @return the event programmatic identifier
     */
    public String getEventProgrammaticIdentifier()
    {
        return _eventProgrammaticIdentifier;
    }

    /**
     * Set the event programmatic identifier.
     *
     * @param eventProgrammaticIdentifier the event programmatic identifier
     */
    public void setEventProgrammaticIdentifier(String eventProgrammaticIdentifier)
    {
        _eventProgrammaticIdentifier = eventProgrammaticIdentifier;
    }

    @Transient
    @Override
    public ScheduleType getType()
    {
        return ScheduleType.internal;
    }

    @Override
    public boolean isRepeat()
    {
        return _repeat;
    }

    /**
     * Set the repeat flag.
     *
     * @param repeat the repeat flag.
     *
     * @see #isRepeat()
     */
    public void setRepeat(boolean repeat)
    {
        _repeat = repeat;
    }

    @Override
    public List<Instant> schedule(ScheduleContext scheduleContext)
    {
        if (isEmptyString(getRecurrenceRule()))
            return Collections.emptyList();


        LocalDateTime startTime = LocalDateTime.ofInstant(scheduleContext.getStartTime(), UTC)
            .truncatedTo(ChronoUnit.DAYS);
        final TemporalAmount duration = scheduleContext.getDuration();
        LocalDateTime endTimeInclusive = LocalDateTime.from(getTemporalDirection() == TemporalDirection.FUTURE
            ? startTime.plus(duration)
            : startTime.minus(duration));
        if (getTemporalDirection() == TemporalDirection.PAST)
        {
            LocalDateTime swap = startTime;
            startTime = endTimeInclusive;
            endTimeInclusive = swap;
        }

        Recur recur;
        try
        {
            recur = new Recur(getRecurrenceRule());
        }
        catch (ParseException e)
        {
            _logger.error("Bad rule: " + getRecurrenceRule(), e);
            return Collections.emptyList();
        }

        List<Instant> instantList = new ArrayList<>();

        final Date until = new Date(endTimeInclusive.toInstant(UTC).toEpochMilli());
        if (isRepeat())
            recur.setUntil(until);
        else
            recur.setCount(1);
        final Date start = new Date(startTime.toInstant(UTC).toEpochMilli());
        final DateList dateList = recur.getDates(start, new Period(new DateTime(start), new DateTime(until)), Value.DATE_TIME);
        DateFormat parser = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        parser.setTimeZone(AppUtil.UTC);
        for (Object dateObject : dateList)
        {
            try
            {
                final java.util.Date date = parser.parse(dateObject.toString());
                instantList.add(date.toInstant());
                if (!isRepeat())
                    break;
            }
            catch (ParseException e)
            {
                _logger.error("Unable to parse date: " + dateObject, e);
            }
        }

        return instantList;
    }

    /**
     * Get the recurrence rule.
     *
     * @return the recurrence rule
     */
    @Column(length = 4096, nullable = false)
    public String getRecurrenceRule()
    {
        return _recurrenceRule;
    }

    /**
     * Set the recurrence rule.
     *
     * @param recurrenceRule the recurrence rule
     */
    public void setRecurrenceRule(String recurrenceRule)
    {
        _recurrenceRule = recurrenceRule;
    }

    /**
     * Get the temporal direction.
     *
     * @return the temporal direction
     */
    @Nonnull
    public TemporalDirection getTemporalDirection()
    {
        return _temporalDirection;
    }

    /**
     * Set the temporal direction.
     *
     * @param temporalDirection the temporal direction
     */
    public void setTemporalDirection(@Nonnull TemporalDirection temporalDirection)
    {
        _temporalDirection = temporalDirection;
    }

    @Override
    public int hashCode()
    {
        if (getId() != null)
            return super.hashCode();

        return Objects.hash(isRepeat(), getEventProgrammaticIdentifier(), getRecurrenceRule(), getTemporalDirection());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        if (getId() != null)
            return super.equals(o);

        ICal4jSchedule that = EntityRetriever.getInstance().narrowProxyIfPossible(o);
        return isRepeat() == that.isRepeat()
               && Objects.equals(getEventProgrammaticIdentifier(), that.getEventProgrammaticIdentifier())
               && Objects.equals(getRecurrenceRule(), that.getRecurrenceRule())
               && getTemporalDirection() == that.getTemporalDirection();
    }
}
