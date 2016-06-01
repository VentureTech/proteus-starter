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

import org.hibernate.Hibernate;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.proteusframework.core.hibernate.dao.EntityRetriever;

import static java.time.ZoneOffset.UTC;

/**
 * Period Schedule that is relative to a start time of another event.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@Audited
public class RelativePeriodSchedule extends Schedule
{
    private static final long serialVersionUID = 4083108267776326797L;

    private boolean _repeat;
    private String _eventProgrammaticIdentifier;
    private Period _period;
    private Time _time;
    @Nonnull
    private TemporalDirection _temporalDirection = TemporalDirection.FUTURE;

    @SuppressWarnings("ConstantConditions")
    @Override
    public RelativePeriodSchedule copy()
    {
        RelativePeriodSchedule copy = new RelativePeriodSchedule();
        copy.setRepeat(isRepeat());
        copy.setTemporalDirection(getTemporalDirection());
        copy.setEventProgrammaticIdentifier(getEventProgrammaticIdentifier());
        copy.setPeriod(getPeriod());
        copy.setTime(getTime());
        return copy;
    }

    @Override
    public ScheduleType getType()
    {
        return ScheduleType.relative;
    }

    @Override
    public boolean isRepeat()
    {
        return _repeat;
    }

    @Override
    public List<Instant> schedule(ScheduleContext scheduleContext)
    {
        final List<Instant> times = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.ofInstant(scheduleContext.getStartTime(), UTC)
            .truncatedTo(ChronoUnit.DAYS);
        final TemporalAmount duration = scheduleContext.getDuration();
        final LocalDateTime endTimeInclusive = LocalDateTime.from(
            getTemporalDirection() == TemporalDirection.FUTURE
                ? startTime.plus(duration)
                : startTime.minus(duration).minus(1, ChronoUnit.DAYS)
        );
        @Nullable
        final LocalTime time = getTime() != null ? getTime().toLocalTime() : null;
        LocalDateTime next = execute(startTime);
        while (shouldContinue(next, endTimeInclusive))
        {
            if (time != null)
                times.add(next.plus(time.toSecondOfDay(), ChronoUnit.SECONDS).toInstant(UTC));
            else
                times.add(next.toInstant(UTC));
            if (!_repeat) break;
            next = execute(next);
        }
        return times;
    }

    /**
     * Get the temporal direction.
     * Defaults to future.
     *
     * @return the temporal direction.
     */
    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    public TemporalDirection getTemporalDirection()
    {
        return _temporalDirection;
    }

    /**
     * Set the temporal direction.
     *
     * @param temporalDirection the temporal direction.
     */
    public void setTemporalDirection(@Nonnull TemporalDirection temporalDirection)
    {
        _temporalDirection = temporalDirection;
    }

    /**
     * Get the time of the day.
     *
     * @return the time of the day.
     */
    @Nullable
    public Time getTime()
    {
        return _time;
    }

    /**
     * Execute the schedule.
     *
     * @param time the time.
     *
     * @return the new time.
     */
    LocalDateTime execute(Temporal time)
    {
        return LocalDateTime.from(
            getTemporalDirection() == TemporalDirection.FUTURE
                ? _period.addTo(time)
                : _period.subtractFrom(time)
        );
    }

    /**
     * Test if we should stop looking for more dates.
     *
     * @param time the time.
     * @param endTimeInclusive the end time inclusive
     *
     * @return true or false.
     */
    boolean shouldContinue(LocalDateTime time, LocalDateTime endTimeInclusive)
    {
        final boolean result;
        final int cmp = time.compareTo(endTimeInclusive);
        if (getTemporalDirection() == TemporalDirection.FUTURE)
        {
            result = cmp <= 0;
        }
        else
        {
            result = cmp > 0;
        }
        return result;
    }

    /**
     * Set the time of the day.
     *
     * @param time the time of the day.
     */
    public void setTime(Time time)
    {
        _time = time;
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

    /**
     * Get the event name to lookup the start time.
     * Callers to {@link #schedule(ScheduleContext)} will use this to set
     * the ScheduleContext {@link ScheduleContext#getStartTime() start time}.
     *
     * @return the event name. The plan is referenced by its class name.
     */
    public String getEventProgrammaticIdentifier()
    {
        return _eventProgrammaticIdentifier;
    }

    /**
     * Set the event name used to lookup the start time.
     *
     * @param eventProgrammaticIdentifier the event name.
     */
    public void setEventProgrammaticIdentifier(String eventProgrammaticIdentifier)
    {
        _eventProgrammaticIdentifier = eventProgrammaticIdentifier;
    }

    /**
     * Get the period.
     *
     * @return the period
     */
    public Period getPeriod()
    {
        return _period;
    }

    /**
     * Set the period.
     *
     * @param period the period
     */
    public void setPeriod(Period period)
    {
        _period = period;
    }

    @Override
    public int hashCode()
    {
        if (getId() != null)
            return super.hashCode();

        return Objects.hash(isRepeat(), getEventProgrammaticIdentifier(), getPeriod(), getTime(), getTemporalDirection());
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

        RelativePeriodSchedule that = EntityRetriever.getInstance().narrowProxyIfPossible(o);
        return isRepeat() == that.isRepeat()
               && Objects.equals(getEventProgrammaticIdentifier(), that.getEventProgrammaticIdentifier())
               && Objects.equals(getPeriod(), that.getPeriod())
               && Objects.equals(getTime(), that.getTime())
               && getTemporalDirection() == that.getTemporalDirection();
    }
}
