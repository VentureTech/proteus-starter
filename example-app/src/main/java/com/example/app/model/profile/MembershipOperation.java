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

package com.example.app.model.profile;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

import net.proteusframework.core.hibernate.model.Entity;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;

/**
 * Defines a single operation that a Profile Membership may perform
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/1/15 10:24 AM
 */
@javax.persistence.Entity
@Table(name = MembershipOperation.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
public class MembershipOperation implements Entity<Integer>, Serializable, NamedObject
{
    /** The database table name */
    public static final String TABLE_NAME = "MembershipOperation";
    /** The database id column */
    public static final String ID_COLUMN = "membershipOperation_id";
    /** The database column and property: programmaticIdentifier */
    public static final String PROGRAMMATIC_IDENTIFIER_COLUMN_PROP = "programmaticIdentifier";
    /** The database column and property: name */
    public static final String NAME_COLUMN_PROP = "name";
    private static final long serialVersionUID = 219265821931910750L;
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + '.' + ID_COLUMN + "_seq";
    private Integer _id;
    private String _programmaticIdentifier;
    private LocalizedObjectKey _name;

    @Column(name = NAME_COLUMN_PROP)
    @NotNull
    @Nonnull
    @Override
    public LocalizedObjectKey getName()
    {
        return _name;
    }

    /**
     * Set the  name
     *
     * @param name the name
     */
    public void setName(@Nonnull LocalizedObjectKey name)
    {
        _name = name;
    }

    @Nullable
    @Override
    @Transient
    public TextSource getDescription()
    {
        return null;
    }

    /**
     * Get the programmatic identifier
     *
     * @return the programmatic identifier
     */
    @Column(name = PROGRAMMATIC_IDENTIFIER_COLUMN_PROP, unique = true)
    @NotNull
    @Nonnull
    @NotEmpty
    public String getProgrammaticIdentifier()
    {
        return _programmaticIdentifier;
    }

    /**
     * Set the programmatic identifier
     *
     * @param programmaticIdentifier the desired programmatic identifier
     */
    public void setProgrammaticIdentifier(@Nonnull String programmaticIdentifier)
    {
        _programmaticIdentifier = programmaticIdentifier;
    }

    @Override
    public int hashCode()
    {
        if (getId() == null)
        {
            return System.identityHashCode(this);
        }
        else
        {
            return getId().hashCode();
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof MembershipOperation))
        {
            return false;
        }
        final MembershipOperation two = (MembershipOperation) obj;
        if (getId() == null)
        {
            return this == two; // not persisted
        }
        else
        {
            return getId().equals(two.getId()); // single table inheritance, this should be ok.
        }
    }

    @Override
    @Id
    @Column(name = ID_COLUMN, unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(name = GENERATOR, sequenceName = GENERATOR)
    @NotNull
    public Integer getId()
    {
        return _id;
    }

    /**
     * Set the identifier.
     *
     * @param id the identifier.
     */
    public void setId(@Nonnull Integer id)
    {
        _id = id;
    }
}
