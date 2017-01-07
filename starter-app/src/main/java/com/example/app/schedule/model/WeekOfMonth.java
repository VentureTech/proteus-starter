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
 * Week of month.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@SuppressWarnings("unused")
public enum WeekOfMonth
{
    /** Week. */
    FIRST(1),
    /** Week. */
    SECOND(2),
    /** Week. */
    THIRD(3),
    /** Week. May also be last. */
    FOURTH(4),
    /** Week. May also be last. */
    FIFTH(5),
    /** Last Week. */
    LAST(-1);

    private final int _offset;

    /**
     * Get the WeekOfMonth based on the offset.
     *
     * @param offset the offset.
     *
     * @return the week of month.
     *
     * @see #getOffset()
     */
    public static WeekOfMonth fromOffset(int offset)
    {
        switch (offset)
        {
            case 1:
                return FIRST;
            case 2:
                return SECOND;
            case 3:
                return THIRD;
            case 4:
                return FOURTH;
            case 5:
                return FIFTH;
            case -1:
                return LAST;
            default:
                throw new IllegalArgumentException("Invalid offset.");
        }
    }

    WeekOfMonth(int offset)
    {
        _offset = offset;
    }

    /**
     * Get the offset.
     *
     * @return the offset.
     */
    public int getOffset()
    {
        return _offset;
    }
}
