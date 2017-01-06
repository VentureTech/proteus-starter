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

package com.example.app.profile.model;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import com.example.app.profile.model.membership.MembershipType;
import com.example.app.support.model.AbstractAuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

import net.proteusframework.cms.label.Label;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.NamedObject;

import static javax.persistence.FetchType.LAZY;

/**
 * Profile Type entity.  It is fully auditable and implements soft delete functionality
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 10/27/15 10:00 AM
 */
@Entity
@Table(name = ProfileType.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.PROFILE_DATA)
@Audited
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = ProjectConfig.DISCRIMINATOR_COLUMN)
@DiscriminatorValue("default")
public class ProfileType extends AbstractAuditableEntity<Integer> implements NamedObject
{
    /** The database table name for this entity */
    public static final String TABLE_NAME = "ProfileType";
    /** The database id column for this entity */
    public static final String ID_COLUMN = "profiletype_id";
    /** The database column name and property: name */
    public static final String NAME_COLUMN_PROP = "name";
    /** The database column name and property: description */
    public static final String DESCRIPTION_COLUMN_PROP = "description";
    /** The property: membershipTypeSet */
    public static final String MEMBERSHIP_TYPES_PROP = "membershipTypeSet";
    /** The database column name and property: programmaticName */
    public static final String PROGRAMMATIC_ID_COLUMN_PROP = "programmaticIdentifier";
    /** The database column for property: kind */
    public static final String KIND_COLUMN = "kind_id";
    /** The property: kind */
    public static final String KIND_PROP = "kind";
    /** The serial version UID */
    private static final long serialVersionUID = -8546697320416386303L;
    /** The ID generator identifier for this entity */
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + ".profiletype_id_seq";
    private LocalizedObjectKey _name;
    private LocalizedObjectKey _description;
    private Set<MembershipType> _membershipTypeSet = new HashSet<>();
    private String _programmaticIdentifier;
    private Label _kind;

    @Id
    @Column(name = ID_COLUMN)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(name = GENERATOR, sequenceName = GENERATOR)
    @Override
    public Integer getId()
    {
        return super.getId();
    }

    /**
     * Get the Kind of this ProfileType
     *
     * @return the Kind
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = KIND_COLUMN)
    @Nullable
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    public Label getKind()
    {
        return _kind;
    }

    /**
     * Set the Kind of this ProfileType
     *
     * @param kind the Kind
     */
    public void setKind(@Nullable Label kind)
    {
        _kind = kind;
    }

    /**
     * Get the MembershipTypes associated with this ProfileType
     *
     * @return the MembershipTypes associated with this ProfileType
     */
    @OneToMany(mappedBy = MembershipType.PROFILE_TYPE_PROP, orphanRemoval = true)
    @Cascade(CascadeType.ALL)
    public Set<MembershipType> getMembershipTypeSet()
    {
        return _membershipTypeSet;
    }

    /**
     * Set the MembershipTypes associated with this ProfileType
     *
     * @param membershipTypeSet the MembershipTypes to associate with this ProfileType
     */
    public void setMembershipTypeSet(Set<MembershipType> membershipTypeSet)
    {
        _membershipTypeSet = membershipTypeSet;
    }

    @NotNull
    @Column(name = NAME_COLUMN_PROP)
    @Nonnull
    @Override
    public LocalizedObjectKey getName()
    {
        return _name;
    }

    /**
     * Set the name of this NamedObject (ProfileType)
     *
     * @param name the intended name of this NamedObject, cannot be null.
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
     * Set the description of this NamedObject (ProfileType)
     *
     * @param description the intended description of this NamedObject, may be null.
     */
    public void setDescription(@Nullable LocalizedObjectKey description)
    {
        _description = description;
    }

    /**
     * Get the programmatic identifier of this ProfileType
     *
     * @return the programmatic identifier
     */
    @Column(name = PROGRAMMATIC_ID_COLUMN_PROP, unique = true)
    @Nonnull
    @NotNull
    @NotEmpty
    public String getProgrammaticIdentifier()
    {
        return _programmaticIdentifier;
    }

    /**
     * Set the programmatic identifier of this ProfileType
     *
     * @param programmaticIdentifier the programmatic identifier
     */
    public void setProgrammaticIdentifier(@Nonnull String programmaticIdentifier)
    {
        _programmaticIdentifier = programmaticIdentifier;
    }
}
