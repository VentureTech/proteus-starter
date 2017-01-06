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

package com.example.app.note.model;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import com.example.app.support.model.AbstractAuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TextSources;

/**
 * Note Entity.
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@Entity
@Table(name = Note.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.MEMBER_DATA)
@Audited
public class Note extends AbstractAuditableEntity<Integer> implements NamedObject
{
    /** The database table name */
    public static final String TABLE_NAME = "Note";
    /** The database id column for this entity */
    public static final String ID_COLUMN = "note_id";
    /** The database column and property: content */
    public static final String CONTENT_COLUMN_PROP = "content";
    private static final long serialVersionUID = -8659050739585920812L;
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + '.' + ID_COLUMN + "_seq";
    private String _content;

    @Id
    @Column(name = ID_COLUMN)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(name = GENERATOR, sequenceName = GENERATOR)
    @Override
    public Integer getId()
    {
        return super.getId();
    }

    @Nonnull
    @Override
    @Transient
    public TextSource getName()
    {
        return Optional.ofNullable(getContent()).map(TextSources::createText).orElse(TextSources.EMPTY);
    }

    /**
     * Get the note content
     *
     * @return content
     */
    @Column(name = CONTENT_COLUMN_PROP, length = 4096)
    @Nullable
    @NotNull
    public String getContent()
    {
        return _content;
    }

    /**
     * Set the note content
     *
     * @param content the content
     */
    public void setContent(@Nonnull String content)
    {
        _content = content;
    }

    @Nullable
    @Override
    @Transient
    public TextSource getDescription()
    {
        return null;
    }
}
