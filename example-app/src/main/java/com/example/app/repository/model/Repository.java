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

package com.example.app.repository.model;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import com.example.app.support.model.AbstractAuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.NamedObject;

/**
 * Represents a repository of items.
 *
 * @author Russ Tennant (russ@i2rd.com)
 * @author Alan Holt (aholt@venturetech.net)
 */
@Entity
@Table(name = Repository.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
@Audited
public class Repository extends AbstractAuditableEntity<Integer> implements NamedObject
{
    /** The database table name */
    public static final String TABLE_NAME = "repository";
    /** The database ID column */
    public static final String ID_COLUMN = "repository_id";
    /** The database column and property: name */
    public static final String NAME_COLUMN_PROP = "name";
    /** The database column and property: description */
    public static final String DESCRIPTION_COLUMN_PROP = "description";

    private static final long serialVersionUID = -3033481916961512269L;
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + '.' + ID_COLUMN + "_seq";

    private LocalizedObjectKey _name;
    private LocalizedObjectKey _description;

    @Id
    @Column(name = ID_COLUMN)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(name = GENERATOR, sequenceName = GENERATOR)
    @NotNull
    @Override
    public Integer getId()
    {
        return super.getId();
    }

    @SuppressWarnings("NullableProblems")
    @Column(name = NAME_COLUMN_PROP)
    @NotNull
    @Override
    public LocalizedObjectKey getName()
    {
        return _name;
    }

    /**
     * Set the name of this Repository
     *
     * @param name the name of is Repository
     */
    public void setName(LocalizedObjectKey name)
    {
        _name = name;
    }

    @Column(name = DESCRIPTION_COLUMN_PROP)
    @Nullable
    @Override
    public LocalizedObjectKey getDescription()
    {
        return _description;
    }

    /**
     * Set the description of this Repository
     *
     * @param description the description of this Repository
     */
    public void setDescription(@Nullable LocalizedObjectKey description)
    {
        _description = description;
    }
}
