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

package com.example.app.model.resource;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import com.example.app.model.SoftDeleteEntity;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.net.URI;

/**
 * Implementation of {@link Resource} that holds a URI
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/18/15 3:12 PM
 */
@Entity
@Where(clause = SoftDeleteEntity.WHERE_CLAUSE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
@BatchSize(size = 10)
@Audited
@SQLDelete(sql = "UPDATE " + ProjectConfig.PROJECT_SCHEMA + '.' + Resource.TABLE_NAME
    + " SET " + URIResource.SOFT_DELETE_COLUMN_PROP + " = 'true' WHERE " + URIResource.ID_COLUMN + " = ?")
@DiscriminatorValue("uri")
public class URIResource extends Resource
{
    private static final long serialVersionUID = -4934440451406871988L;

    /** The database column and property: uri */
    public static final String URI_COLUMN_PROP = "uri";

    private URI _uri;

    /**
     *   Get the URI stored by this Resource
     *   @return the uri
     */
    @Column(name = URI_COLUMN_PROP)
    @Nullable
    public URI getUri()
    {
        return _uri;
    }
    /**
     *   Set the URI stored by this Resource
     *   @param uri the uri
     */
    public void setUri(@Nullable URI uri)
    {
        _uri = uri;
    }

    @Override
    public URIResource clone()
    {
        URIResource resource = (URIResource) super.clone();
        resource.setUri(getUri());
        return resource;
    }
}
