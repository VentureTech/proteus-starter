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
import com.example.app.resource.model.Resource;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import net.proteusframework.core.locale.LocalizedObjectKey;

/**
 * A {@link RepositoryItem} implementation that holds a reference to a {@link Resource}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/12/15 11:31 AM
 */
@Entity
@Table(name = ResourceRepositoryItem.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
@Audited
@DiscriminatorValue("resource")
public class ResourceRepositoryItem extends RepositoryItem
{
    /** The database table name */
    public static final String TABLE_NAME = "resourceRepositoryItem";
    /** The database column for property: resource */
    public static final String RESOURCE_COLUMN = "resource_id";
    /** The property: resource */
    public static final String RESOURCE_PROP = "resource";
    private static final long serialVersionUID = 6067966732141155565L;
    private Resource _resource;

    @Transient
    @Nonnull
    @Override
    public LocalizedObjectKey getName()
    {
        return getResource().getName();
    }

    /**
     * Get the {@link Resource} that this RepositoryItem is pointing to
     *
     * @return the Resource
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = RESOURCE_COLUMN, unique = true)
    @Cascade(CascadeType.ALL)
    @Nonnull
    @NotNull
    public Resource getResource()
    {
        return _resource;
    }

    /**
     * Set the {@link Resource} that this RepositoryItem is pointing to
     *
     * @param resource the Resource
     */
    public void setResource(@Nonnull Resource resource)
    {
        _resource = resource;
    }

    @Transient
    @Nullable
    @Override
    public LocalizedObjectKey getDescription()
    {
        return getResource().getDescription();
    }
}
