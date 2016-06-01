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
import com.example.app.model.AbstractAuditableSoftDeleteEntity;
import com.example.app.model.SoftDeleteEntity;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import net.proteusframework.cms.label.Label;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.users.audit.FullyAuditable;

import static javax.persistence.FetchType.LAZY;
import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

/**
 * Resource Entity.  It is {@link FullyAuditable} and implements {@link SoftDeleteEntity}
 *
 * This entity represents a learning resource that can be rendered and edited within the UI based on the resource data.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/5/15 1:27 PM
 */
@Entity
@Table(name = Resource.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA)
@Where(clause = SoftDeleteEntity.WHERE_CLAUSE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
@BatchSize(size = 10)
@Audited
@SQLDelete(sql = "UPDATE " + ProjectConfig.PROJECT_SCHEMA + '.' + Resource.TABLE_NAME
                 + " SET " + Resource.SOFT_DELETE_COLUMN_PROP + " = 'true' WHERE " + Resource.ID_COLUMN + " = ?")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = ProjectConfig.DISCRIMINATOR_COLUMN)
public abstract class Resource extends AbstractAuditableSoftDeleteEntity implements NamedObject, Cloneable
{
    /** The database table name */
    public static final String TABLE_NAME = "resource";
    /** The database id column */
    public static final String ID_COLUMN = "resource_id";
    /** Where clause for a collection containing Resources */
    public static final String COLLECTION_WHERE_CLAUSE = ID_COLUMN + " in (select resource." + ID_COLUMN + " from app."
                                                         + TABLE_NAME + " resource where resource." + SOFT_DELETE_COLUMN_PROP
                                                         + "='false')";
    /** The database column and property: author */
    public static final String AUTHOR_COLUMN_PROP = "author";
    /** The database column and property: visibility */
    public static final String VISIBILITY_COLUMN_PROP = "visibility";
    /** The property: tags */
    public static final String TAGS_PROP = "tags";
    /** The database join table: categories */
    public static final String TAGS_JOIN_TABLE = "resource_tags";
    /** The database inverse join column for: categories */
    public static final String TAGS_INVERSE_JOIN = "tag_id";
    /** The database column for property: type */
    public static final String CATEGORY_COLUMN = "category_id";
    /** The property: type */
    public static final String CATEGORY_PROP = "category";
    /** The database column and property: resourceType */
    public static final String RESOURCE_TYPE_COLUMN_PROP = "resourceType";
    /** The database column and property: name */
    public static final String NAME_COLUMN_PROP = "name";
    /** The database column and property: description */
    public static final String DESCRIPTION_COLUMN_PROP = "description";
    /** The database column for property: image */
    public static final String IMAGE_COLUMN = "image_id";
    /** The property: image */
    public static final String IMAGE_PROP = "image";
    private static final long serialVersionUID = 4744893963830122039L;
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + ".resource_id_seq";
    private String _author;
    private ResourceVisibility _visibility = ResourceVisibility.Public;
    private List<Label> _tags = new ArrayList<>();
    private Label _category;
    private ResourceType _resourceType;
    private LocalizedObjectKey _name;
    private LocalizedObjectKey _description;
    private FileEntity _image;

    @Override
    @Transient
    public Resource clone()
    {
        ResourceType resourceType = getResourceType();
        Resource clone = resourceType.createInstance(resourceType);
        clone.setResourceType(resourceType);
        clone.setVisibility(getVisibility());
        clone.setAuthor(getAuthor());
        clone.setTags(getTags());
        clone.setDescription(getDescription());
        clone.setName(getName());
        clone.setCategory(getCategory());
        clone.setImage(getImage());
        return clone;
    }

    /**
     * Get the internal resource type of this resource
     *
     * @return the internal resource type of this resource
     */
    @Column(name = RESOURCE_TYPE_COLUMN_PROP)
    @NotNull
    @Nonnull
    public ResourceType getResourceType()
    {
        return _resourceType;
    }

    /**
     * Get the resource visibility
     *
     * @return the resource visibility
     */
    @Column(name = VISIBILITY_COLUMN_PROP)
    @Enumerated(EnumType.STRING)
    @NotNull
    @Nonnull
    public ResourceVisibility getVisibility()
    {
        return _visibility;
    }

    /**
     * Get the author of this resource
     *
     * @return the author of this resource
     */
    @Column(name = AUTHOR_COLUMN_PROP)
    @Nullable
    public String getAuthor()
    {
        return _author;
    }

    /**
     * Set the author of this resource
     *
     * @param author the author of this resource
     */
    public void setAuthor(@Nullable String author)
    {
        _author = author;
    }

    /**
     * Get the categories that this resource belongs to
     *
     * @return the categories that this resource belongs to
     */
    @ManyToMany
    @JoinTable(name = TAGS_JOIN_TABLE, schema = ProjectConfig.PROJECT_SCHEMA,
        joinColumns = {@JoinColumn(name = ID_COLUMN, nullable = false)},
        inverseJoinColumns = {@JoinColumn(name = TAGS_INVERSE_JOIN, nullable = false)})
    @Nonnull
    @NotNull
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
    @BatchSize(size = 10)
    @Audited(targetAuditMode = NOT_AUDITED)
    public List<Label> getTags()
    {
        return _tags;
    }

    /**
     * Set the categories that this resource belongs to
     *
     * @param tags the categories that this resource belongs to
     */
    public void setTags(@Nonnull List<Label> tags)
    {
        _tags = tags;
    }

    @Column(name = NAME_COLUMN_PROP)
    @Nonnull
    @NotNull
    @Override
    public LocalizedObjectKey getName()
    {
        return _name;
    }

    /**
     * Get the type of this resource -- used for display purposes
     *
     * @return the type of this resource
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = CATEGORY_COLUMN)
    @Nullable
    @Audited(targetAuditMode = NOT_AUDITED)
    public Label getCategory()
    {
        return _category;
    }

    /**
     * Set the type of this resource -- used for display purposes
     *
     * @param category the type of this resource
     */
    public void setCategory(@Nullable Label category)
    {
        _category = category;
    }

    /**
     * Get the Resource Image
     *
     * @return the resource Image
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = IMAGE_COLUMN)
    @Audited(targetAuditMode = NOT_AUDITED)
    @Nullable
    public FileEntity getImage()
    {
        return _image;
    }

    /**
     * Get the Resource Image
     *
     * @param image the resource Image
     */
    public void setImage(@Nullable FileEntity image)
    {
        _image = image;
    }

    /**
     * Set the name of this Resource
     *
     * @param name the name of this Resource
     */
    public void setName(@Nonnull LocalizedObjectKey name)
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
     * Set the description of this Resource
     *
     * @param description the description of this Resource
     */
    public void setDescription(@Nullable LocalizedObjectKey description)
    {
        _description = description;
    }

    /**
     * Set the resource visibility
     *
     * @param visibility the resource visibility
     */
    public void setVisibility(@Nonnull ResourceVisibility visibility)
    {
        _visibility = visibility;
    }

    /**
     * Set the internal resource type of this resource
     *
     * @param resourceType the internal resource type of this resource
     */
    public void setResourceType(ResourceType resourceType)
    {
        _resourceType = resourceType;
    }

    @Id
    @Column(name = ID_COLUMN)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(name = GENERATOR, sequenceName = GENERATOR)
    @Override
    @NotNull
    @Nonnull
    public Integer getId()
    {
        return super.getId();
    }
}
