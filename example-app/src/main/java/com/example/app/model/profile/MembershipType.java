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

package com.example.app.model.profile;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import com.example.app.model.AbstractAuditableSoftDeleteEntity;
import com.example.app.model.SoftDeleteEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

/**
 * Membership Type entity for Profiles.  It is fully auditable and implements soft delete functionality
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 10/27/15 10:34 AM
 */
@Entity
@Table(name = MembershipType.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA,
    uniqueConstraints = {
        @UniqueConstraint(name = "profileType_programmaticIdentifier", columnNames = {
            ProfileType.ID_COLUMN, MembershipType.PROGRAMMATIC_ID_COLUMN_PROP
        })
    })
@Where(clause = SoftDeleteEntity.WHERE_CLAUSE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.MEMBER_DATA)
@Audited
@SQLDelete(sql = "UPDATE " + ProjectConfig.PROJECT_SCHEMA + '.' + MembershipType.TABLE_NAME
                 + " SET " + MembershipType.SOFT_DELETE_COLUMN_PROP + " = 'true' WHERE " + MembershipType.ID_COLUMN + " = ?")
public class MembershipType extends AbstractAuditableSoftDeleteEntity implements NamedObject
{
    /** The database table name for this entity */
    public static final String TABLE_NAME = "MembershipType";
    /** The database id column for this entity */
    public static final String ID_COLUMN = "membershiptype_id";
    /** The database column name and property: name */
    public static final String NAME_COLUMN_PROP = "name";
    /** The database column name and property: defaultOperations */
    public static final String DEFAULT_OPERATIONS_PROP = "defaultOperations";
    /** The database column name and property: programmaticIdentifier */
    public static final String PROGRAMMATIC_ID_COLUMN_PROP = "programmaticIdentifier";
    /** The property: profileType */
    public static final String PROFILE_TYPE_PROP = "profileType";
    /** The serial version UID */
    private static final long serialVersionUID = -7073389536889912205L;
    /** The ID generator identifier for this entity */
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + ".membershiptype_id_seq";
    private LocalizedObjectKey _name;
    private String _programmaticIdentifier;
    private ProfileType _profileType;
    private List<MembershipOperation> _defaultOperations = new ArrayList<>();

    /**
     * Get the default operations for new Memberships.
     *
     * @return the operations.
     */
    @ManyToMany
    @JoinTable(schema = ProjectConfig.PROJECT_SCHEMA, name = "membershiptype_operations",
        joinColumns = {@JoinColumn(name = ID_COLUMN)},
        inverseJoinColumns = {@JoinColumn(name = MembershipOperation.ID_COLUMN)})
    @Cascade(CascadeType.ALL)
    @Nonnull
    @Audited(targetAuditMode = NOT_AUDITED)
    public List<MembershipOperation> getDefaultOperations()
    {
        return _defaultOperations;
    }

    /**
     * Set the default operations for new Memberships.
     *
     * @param defaultOperations the operations.
     */
    public void setDefaultOperations(List<MembershipOperation> defaultOperations)
    {
        _defaultOperations = defaultOperations;
    }

    @Id
    @Column(name = ID_COLUMN)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(name = GENERATOR, sequenceName = GENERATOR)
    @Override
    public Integer getId()
    {
        return super.getId();
    }

    @Column(name = NAME_COLUMN_PROP)
    @Nonnull
    @NotNull
    @Override
    public LocalizedObjectKey getName()
    {
        return _name;
    }

    @Transient
    @Nullable
    @Override
    public TextSource getDescription()
    {
        return null;
    }

    /**
     * Set the name of this Membership Type
     *
     * @param name the name of this Membership Type
     */
    public void setName(@Nonnull LocalizedObjectKey name)
    {
        _name = name;
    }

    /**
     * Get the ProfileType that owns this MembershipType
     *
     * @return the owning ProfileType
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ProfileType.ID_COLUMN)
    @NotNull
    @Nonnull
    public ProfileType getProfileType()
    {
        return _profileType;
    }

    /**
     * Set the ProfileType that owns this MembershipType
     *
     * @param profileType the owning ProfileType
     */
    public void setProfileType(ProfileType profileType)
    {
        _profileType = profileType;
    }

    /**
     * Get the programmatic identifier of this MembershipType
     *
     * @return the programmatic identifier of this MembershipType
     */
    @Column(name = PROGRAMMATIC_ID_COLUMN_PROP)
    @Nonnull
    @NotNull
    @NotEmpty
    public String getProgrammaticIdentifier()
    {
        return _programmaticIdentifier;
    }

    /**
     * Set the programmatic identifier of this MembershipType
     *
     * @param programmaticIdentifier the programmatic identifier of this MembershipType
     */
    public void setProgrammaticIdentifier(@Nonnull String programmaticIdentifier)
    {
        _programmaticIdentifier = programmaticIdentifier;
    }
}
