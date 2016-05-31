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

import com.example.app.model.AbstractAuditableEntity;
import org.hibernate.envers.Audited;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;

import net.proteusframework.core.locale.TextSource;

/**
 * Scheduling configuration.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@Audited
public abstract class Schedule extends AbstractAuditableEntity<Integer>
{

    private static final long serialVersionUID = -766887341561166550L;

    /**
     * Test if the schedule repeats.
     * @return true or false.
     */
    public abstract boolean isRepeat();

    /**
     * Get the schedule type.
     * @return the type.
     */
    public abstract ScheduleType getType();

    /**
     * Provide a list of zero or more schedule times.
     * @param scheduleContext the context.
     * @return the schedule times.
     */
    public abstract List<Instant> schedule(ScheduleContext scheduleContext);

    /**
     *   Copy this Schedule
     *   @return a copied Schedule
     */
    public abstract Schedule copy();


}
