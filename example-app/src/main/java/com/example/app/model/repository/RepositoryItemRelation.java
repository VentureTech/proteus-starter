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
import com.example.app.model.AbstractAuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import net.proteusframework.core.hibernate.dao.EntityRetriever;

/**
 * Entity defining a relationship between a Repository and a RepositoryItem
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 4/13/16 3:20 PM
 */
@Entity
//Note:  We also have a unique index using the database:
// CREATE INDEX ON app.repositoryItemRelation.repositoryItem_id WHERE relationType = "owned"
@Table(name = RepositoryItemRelation.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA,
    uniqueConstraints = {
        @UniqueConstraint(name = "repoItem_repo", columnNames = {
            Repository.ID_COLUMN,
            RepositoryItem.ID_COLUMN
        })
    })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
@Audited
public class RepositoryItemRelation extends AbstractAuditableEntity<Integer>
{
    /** The database table name */
    public static final String TABLE_NAME = "repositoryItemRelation";
    /** The database id column */
    public static final String ID_COLUMN = "repositoryItemRelation_id";
    /** The property: repositoryItem */
    public static final String REPOSITORY_ITEM_PROP = "repositoryItem";
    /** The property: repository */
    public static final String REPOSITORY_PROP = "repository";
    /** The property: relationType */
    public static final String RELATION_TYPE_PROP = "relationType";
    private static final long serialVersionUID = 4502761901001997105L;
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + '.' + ID_COLUMN + "_seq";
    private RepositoryItem _repositoryItem;
    private Repository _repository;
    private RepositoryItemRelationType _relationType;

    /**
     * Get the RepositoryItem for this RepositoryItemRelation, casting it to the expected RepositoryItem subclass in the process
     *
     * @param <RI> the RepositoryItem subclass
     * @param clazz the RepositoryItem subclass
     *
     * @return the RepositoryItem subclass instance
     */
    @Nonnull
    @Transient
    public <RI extends RepositoryItem> RI getAssertedCastRepositoryItem(Class<RI> clazz)
    {
        RI ri = getCastRepositoryItem(clazz);
        assert ri != null;
        return ri;
    }

    /**
     * Get the RepositoryItem for this RepositoryItemRelation, casting it to the expected RepositoryItem subclass in the process
     *
     * @param <RI> the RepositoryItem subclass
     * @param clazz the RepositoryItem subclass
     *
     * @return the RepositoryItem subclass instance
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Transient
    public <RI extends RepositoryItem> RI getCastRepositoryItem(Class<RI> clazz)
    {
        RepositoryItem ri = getRepositoryItem();
        if (ri instanceof HibernateProxy)
        {
            EntityRetriever er = EntityRetriever.getInstance();
            ri = er.narrowProxyIfPossible(ri);
        }
        return (clazz.isAssignableFrom(ri.getClass())) ? (RI) ri : null;
    }

    /**
     * Get the RepositoryItem for this RepositoryItemRelation
     *
     * @return RepositoryItem
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = RepositoryItem.ID_COLUMN)
    @Cascade({
        CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.PERSIST,
        CascadeType.REFRESH, CascadeType.DETACH, CascadeType.LOCK, CascadeType.REPLICATE})
    @NotNull
    @Nonnull
    public RepositoryItem getRepositoryItem()
    {
        return _repositoryItem;
    }

    /**
     * Get the RepositoryItem for this RepositoryItemRelation
     *
     * @param repositoryItem RepositoryItem
     */
    public void setRepositoryItem(@Nonnull RepositoryItem repositoryItem)
    {
        _repositoryItem = repositoryItem;
    }

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

    /**
     * Get the Relation Type for this RepositoryItemRelation
     *
     * @return Relation Type
     */
    @Column(name = RELATION_TYPE_PROP)
    @Enumerated(EnumType.STRING)
    @Nonnull
    public RepositoryItemRelationType getRelationType()
    {
        return _relationType;
    }

    /**
     * Set the Relation Type for this RepositoryItemRelation
     *
     * @param relationType Relation Type
     */
    public void setRelationType(@Nonnull RepositoryItemRelationType relationType)
    {
        _relationType = relationType;
    }

    /**
     * Get the Repository for this RepositoryItemRelation
     *
     * @return Repository
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = Repository.ID_COLUMN)
    @NotNull
    @Nonnull
    public Repository getRepository()
    {
        return _repository;
    }

    /**
     * Set the Repository for this RepositoryItemRelation
     *
     * @param repository Repository
     */
    public void setRepository(@Nonnull Repository repository)
    {
        _repository = repository;
    }
}
