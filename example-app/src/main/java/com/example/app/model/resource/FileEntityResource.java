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
@Where(clause = SoftDeleteEntity.WHERE_CLAUSE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
@BatchSize(size = 10)
@Audited
@SQLDelete(sql = "UPDATE " + ProjectConfig.PROJECT_SCHEMA + '.' + Resource.TABLE_NAME
    + " SET " + FileEntityResource.SOFT_DELETE_COLUMN_PROP + " = 'true' WHERE " + FileEntityResource.ID_COLUMN + " = ?")
@DiscriminatorValue("file")
public class FileEntityResource extends Resource
{
    private static final long serialVersionUID = 5061021909017268727L;

    /** The database column for property: file */
    public static final String FILE_COLUMN = "file_id";
    /** The property: file */
    public static final String FILE_PROP = "file";

    private FileEntity _file;

    /**
     *   Get the {@link FileEntity} stored by this Resource
     *   @return the File Entity
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
     *   Set the {@link FileEntity} stored by this Resource
     *   @param file the File Entity
     */
    public void setFile(@Nullable FileEntity file)
    {
        _file = file;
    }

    @Override
    public FileEntityResource clone()
    {
        FileEntityResource resource = (FileEntityResource) super.clone();
        resource.setFile(getFile());
        return resource;
    }
}
