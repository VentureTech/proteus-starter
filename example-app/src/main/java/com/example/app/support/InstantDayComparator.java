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

package com.example.app.support;

import com.example.app.model.schedule.TemporalDirection;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

import static com.example.app.model.schedule.TemporalDirection.FUTURE;
import static com.example.app.model.schedule.TemporalDirection.PAST;

/**
 * Comparator for Instant that truncates the compared Instant instances to the nearest Day before comparing
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
public class InstantDayComparator implements Comparator<Instant>, Serializable
{
    private static final InstantDayComparator _ascInstance = new InstantDayComparator();
    private static final InstantDayComparator _descInstance = new InstantDayComparator(PAST);
    private static final long serialVersionUID = 1775027402933386444L;

    private final TemporalDirection _direction;

    /**
     * Get the singleton instance of InstantDayComparator that is used for sorting based on the given TemporalDirection
     *
     * @param direction the direction to sort.  {@link TemporalDirection#FUTURE} for ascending, {@link TemporalDirection#PAST}
     * for descending.
     *
     * @return ActivityInstantComparator
     */
    public static InstantDayComparator getFromComputedTemporalDirection(TemporalDirection direction)
    {
        return direction == FUTURE ? getAscInstance() : getDescInstance();
    }

    /**
     * Get the singleton instance of InstantDayComparator that is used for sorting in ascending order
     *
     * @return ActivityInstantComparator
     */
    public static InstantDayComparator getAscInstance()
    {
        return _ascInstance;
    }

    /**
     * Get the singleton instance of InstantDayComparator that is used for sorting in descending order
     *
     * @return ActivityInstantComparator
     */
    public static InstantDayComparator getDescInstance()
    {
        return _descInstance;
    }

    /**
     * Instantiate a new instance of InstantDayComparator for the future.
     */
    public InstantDayComparator()
    {
        this(FUTURE);
    }

    /**
     * Instantiate a new instance of InstantDayComparator
     *
     * @param direction the direction to sort
     */
    private InstantDayComparator(TemporalDirection direction)
    {
        _direction = direction;
    }

    @Override
    public int compare(Instant o1, Instant o2)
    {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return _direction == FUTURE ? -1 : 1;
        if (o2 == null) return _direction == FUTURE ? 1 : -1;
        Instant truncatedo1 = o1.truncatedTo(ChronoUnit.DAYS);
        Instant truncatedo2 = o2.truncatedTo(ChronoUnit.DAYS);
        return truncatedo1.compareTo(truncatedo2) * (_direction == FUTURE ? 1 : -1);
    }
}
