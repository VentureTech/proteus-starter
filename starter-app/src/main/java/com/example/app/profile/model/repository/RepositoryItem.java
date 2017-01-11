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

package com.example.app.profile.model.repository;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.users.model.AbstractAuditableEntity;

/**
 * An item in a repository.
 *
 * @author Russ Tennant (russ@i2rd.com)
 * @author Alan Holt (aholt@venturetech.net)
 */
@Entity
@Table(name = RepositoryItem.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
@Audited
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = ProjectConfig.DISCRIMINATOR_COLUMN)
public abstract class RepositoryItem extends AbstractAuditableEntity<Integer> implements NamedObject
{
    /** The database table name */
    public static final String TABLE_NAME = "repositoryItem";
    /** The database ID column */
    public static final String ID_COLUMN = "repositoryItem_id";
    /** The database column and property: source */
    public static final String SOURCE_COLUMN_PROP = "source";
    /** The database column and property: status */
    public static final String STATUS_COLUMN_PROP = "status";
    private static final long serialVersionUID = -1157448594050392156L;
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + '.' + ID_COLUMN + "_seq";
    private String _source;
    private RepositoryItemStatus _status = RepositoryItemStatus.Draft;

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

    @Transient
    @Nonnull
    @Override
    public abstract TextSource getName();

    @Transient
    @Nullable
    @Override
    public abstract TextSource getDescription();

    /**
     * Get the source or licensing entity of the item.
     *
     * @return the source.
     */
    @Column(name = SOURCE_COLUMN_PROP, nullable = false)
    @Nonnull
    @NotEmpty
    public String getSource()
    {
        return _source;
    }

    /**
     * Set the source or licensing entity of the item.
     *
     * @param source the source
     */
    public void setSource(String source)
    {
        _source = source;
    }

    /**
     * Get the status of this RepositoryItem
     *
     * @return the status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = STATUS_COLUMN_PROP)
    @NotNull
    public RepositoryItemStatus getStatus()
    {
        return _status;
    }

    /**
     * Set the status of this RepositoryItem
     *
     * @param status the status
     */
    public void setStatus(@Nonnull RepositoryItemStatus status)
    {
        _status = status;
    }
}
