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

package com.example.app.model.repository;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import com.example.app.model.AbstractAuditableSoftDeleteEntity;
import com.example.app.model.SoftDeleteEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
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
@Where(clause = SoftDeleteEntity.WHERE_CLAUSE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
@Audited
@SQLDelete(sql = "UPDATE " + ProjectConfig.PROJECT_SCHEMA + '.' + Repository.TABLE_NAME
    + " SET " + Repository.SOFT_DELETE_COLUMN_PROP + " = 'true' WHERE " + Repository.ID_COLUMN + " = ?")
public class Repository extends AbstractAuditableSoftDeleteEntity implements NamedObject
{
    private static final long serialVersionUID = -3033481916961512269L;

    /** The database table name */
    public static final String TABLE_NAME = "repository";
    /** The database ID column */
    public static final String ID_COLUMN = "repository_id";
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + '.' + ID_COLUMN + "_seq";

    /** The database column and property: name */ 
    public static final String NAME_COLUMN_PROP = "name";
    /** The database column and property: description */
    public static final String DESCRIPTION_COLUMN_PROP = "description";
    /** The property: owned */
    public static final String OWNED_PROP = "owned";
    /** The database join table for property: assigned */
    public static final String ASSIGNED_JOIN_TABLE = "repository_assigned";
    /** The inverse join column for property: assigned */
    public static final String ASSIGNED_INVERSE_JOIN = "assigned_id";
    /** The property: assigned */
    public static final String ASSIGNED_PROP = "assigned";

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

    @Column(name = NAME_COLUMN_PROP)
    @NotNull
    @Nonnull
    @Override
    public LocalizedObjectKey getName()
    {
        return _name;
    }
    /**
     *   Set the name of this Repository
     *   @param name the name of is Repository
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
     *   Set the description of this Repository
     *   @param description the description of this Repository
     */
    public void setDescription(@Nullable LocalizedObjectKey description)
    {
        _description = description;
    }
}
