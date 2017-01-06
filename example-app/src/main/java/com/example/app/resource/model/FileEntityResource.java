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

package com.example.app.resource.model;

import com.example.app.config.ProjectCacheRegions;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.annotation.Nullable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.proteusframework.data.filesystem.FileEntity;

/**
 * Implementation of {@link Resource} that holds a reference to a {@link FileEntity}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/18/15 3:45 PM
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
@BatchSize(size = 10)
@Audited
@DiscriminatorValue("file")
public class FileEntityResource extends Resource
{
    /** The database column for property: file */
    public static final String FILE_COLUMN = "file_id";
    /** The property: file */
    public static final String FILE_PROP = "file";
    private static final long serialVersionUID = 5061021909017268727L;
    private FileEntity _file;

    @Override
    public FileEntityResource clone()
    {
        FileEntityResource resource = (FileEntityResource) super.clone();
        resource.setFile(getFile());
        return resource;
    }

    /**
     * Get the {@link FileEntity} stored by this Resource
     *
     * @return the File Entity
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = FILE_COLUMN)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Nullable
    public FileEntity getFile()
    {
        return _file;
    }

    /**
     * Set the {@link FileEntity} stored by this Resource
     *
     * @param file the File Entity
     */
    public void setFile(@Nullable FileEntity file)
    {
        _file = file;
    }
}
