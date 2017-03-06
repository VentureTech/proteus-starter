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

package com.example.app.profile.model.user;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Optional;

import net.proteusframework.users.model.AbstractAuditableEntity;

/**
 * A User Position Entity -- It represents a position held by the user within their client.
 * This Entity is fully auditable and implements soft delete.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 10/30/15 2:40 PM
 */
@Entity
@Table(name = UserPosition.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA, indexes = {
    @Index(name = "userposition_user_idx", columnList = UserPosition.USER_COLUMN)
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
@Audited
public class UserPosition extends AbstractAuditableEntity<Integer>
{
    /** the database table name for this entity */
    public static final String TABLE_NAME = "UserPosition";
    /** the database id column for this entity */
    public static final String ID_COLUMN = "userPosition_id";
    /** the database column name for the property: user */
    public static final String USER_COLUMN = "user_id";
    /** the property: user */
    public static final String USER_PROP = "user";
    /** the database column name and property: position */
    public static final String POSITION_COLUMN_PROP = "position";
    /** the database column name and property: startDate */
    public static final String START_DATE_COLUMN_PROP = "startDate";
    /** the database column name and property: endDate */
    public static final String END_DATE_COLUMN_PROP = "endDate";
    /** The database column and property: current */
    public static final String CURRENT_COLUMN_PROP = "current";
    /** the serial version UID */
    private static final long serialVersionUID = -6468993041645511504L;
    /** the ID generator identifier for this entity */
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + ".user_position_id_seq";
    /** the user that this position belongs to */
    private User _user;
    /** the position name */
    private String _position;
    /** the position start date */
    private Date _startDate;
    /** the position end date */
    private Date _endDate;
    private boolean _current;

    @Id
    @Column(name = ID_COLUMN)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(name = GENERATOR, sequenceName = GENERATOR)
    @Override
    public Integer getId()
    {
        return super.getId();
    }

    /**
     * Get the end date of this position
     *
     * @return the end date of this position
     */
    @Nonnull
    @Transient
    public Optional<Date> getOptionalEndDate()
    {
        return Optional.ofNullable(getEndDate());
    }

    /**
     * Get the end date of this position
     *
     * @return the end date of this position
     */
    @Nullable
    @Column(name = END_DATE_COLUMN_PROP)
    public Date getEndDate()
    {
        return _endDate;
    }

    /**
     * Set the end date of this position
     *
     * @param endDate the end date of this position
     */
    public void setEndDate(@Nullable Date endDate)
    {
        _endDate = endDate;
    }

    /**
     * Get the start date of this position
     *
     * @return the start date of this position
     */
    @Transient
    @Nonnull
    public Optional<Date> getOptionalStartDate()
    {
        return Optional.ofNullable(getStartDate());
    }

    /**
     * Get the start date of this position
     *
     * @return the start date of this position
     */
    @Column(name = START_DATE_COLUMN_PROP)
    @Nullable
    public Date getStartDate()
    {
        return _startDate;
    }

    /**
     * Set the start date of this position
     *
     * @param startDate the start date of this position
     */
    public void setStartDate(@Nullable Date startDate)
    {
        _startDate = startDate;
    }

    /**
     * Get the name of this position
     *
     * @return the name of this position
     */
    @Column(name = POSITION_COLUMN_PROP, nullable = false)
    @NotNull
    public String getPosition()
    {
        return _position;
    }

    /**
     * Set the name of this position
     *
     * @param position the name of this position
     */
    public void setPosition(@Nonnull String position)
    {
        _position = position;
    }

    /**
     * Get the {@link User} that this belongs to
     *
     * @return the User that this belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = USER_COLUMN, nullable = false)
    @NotNull
    public User getUser()
    {
        return _user;
    }

    /**
     * Set the {@link User} that this belongs to
     *
     * @param user the User that this belongs to
     */
    public void setUser(@Nonnull User user)
    {
        _user = user;
    }

    /**
     * Get boolean flag. If true, this is a current position
     *
     * @return current flag
     */
    @Column(name = CURRENT_COLUMN_PROP, nullable = false)
    public boolean isCurrent()
    {
        return _current;
    }

    /**
     * Set boolean flag. If true, this is a current position
     *
     * @param current flag
     */
    public void setCurrent(boolean current)
    {
        _current = current;
    }
}
