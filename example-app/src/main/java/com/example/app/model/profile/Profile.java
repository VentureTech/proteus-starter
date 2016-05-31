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
import com.example.app.model.repository.Repository;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
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

import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.NamedObject;

/**
 * Profile superclass.  It is fully auditable and implements a soft delete functionality.
 *
 * Extend for each Profile implementation.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 10/27/15 9:18 AM
 */
@Entity
@Table(name = Profile.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA, indexes = {
    @Index(name="profile_disc_idx", columnList = ProjectConfig.DISCRIMINATOR_COLUMN),
    @Index(name="profile_profletype_idx", columnList = Profile.PROFILE_TYPE_COLUMN)
})
@Where(clause = SoftDeleteEntity.WHERE_CLAUSE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.PROFILE_DATA)
@BatchSize(size = 10)
@Audited
@SQLDelete(sql = "UPDATE " + ProjectConfig.PROJECT_SCHEMA + '.' + Profile.TABLE_NAME
    + " SET " + Profile.SOFT_DELETE_COLUMN_PROP + " = 'true' WHERE " + Profile.ID_COLUMN + " = ?")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = ProjectConfig.DISCRIMINATOR_COLUMN)
public abstract class Profile extends AbstractAuditableSoftDeleteEntity implements NamedObject
{
    /** The serial version UID */
    private static final long serialVersionUID = -7189722406282887545L;

    /** The database table name for this entity */
    public static final String TABLE_NAME = "Profile";

    /** The ID generator identifier for this entity */
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + ".profile_id_seq";
    /** The database id column for this entity */
    public static final String ID_COLUMN = "profile_id";
    /** The database column name and property: parent */
    public static final String PARENT_COLUMN_PROP = "parent";
    /** The database column name of the property: profileType */
    public static final String PROFILE_TYPE_COLUMN = "profiletype_id";
    /** The property: profileType */
    public static final String PROFILE_TYPE_PROP = "profileType";
    /** The property: membershipSet */
    public static final String MEMBERSHIPS_PROP = "membershipSet";
    /** The database column and property: name */
    public static final String NAME_COLUMN_PROP = "name";
    /** The database column and property: description */
    public static final String DESCRIPTION_COLUMN_PROP = "description";
    /** The property: repository */
    public static final String REPOSITORY_PROP = "repository";
    /** Where clause for a collection containing Resources */
    public static final String COLLECTION_WHERE_CLAUSE =
        ID_COLUMN + " in (select profile." + ID_COLUMN + " from app." + TABLE_NAME
        + " profile where profile." + SOFT_DELETE_COLUMN_PROP + "='false')";

    private Profile _parent;
    private ProfileType _profileType;
    private Set<Membership> _membershipSet = new HashSet<>(0);
    private LocalizedObjectKey _name;
    private LocalizedObjectKey _description;
    private Repository _repository = new Repository();

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
     * Get this profile's parent profile.
     * @return this profile's parent profile, if there is one, otherwise returns null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PARENT_COLUMN_PROP)
    @Nullable
    public Profile getParent()
    {
        return _parent;
    }

    /**
     * Set this profile's parent profile.
     * @param parent the intended parent of this profile.  may be null.
     */
    public void setParent(@Nullable  Profile parent)
    {
        _parent = parent;
    }

    /**
    *   Get the profile type of this Profile
    *   @return the ProfileType of this Profile
    */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PROFILE_TYPE_COLUMN)
    @NotNull
    @Nonnull
    public ProfileType getProfileType()
    {
        return _profileType;
    }

    /**
    *   Set the profile type of this Profile
    *   @param profileType the ProfileType of this Profile
    */
    public void setProfileType(ProfileType profileType)
    {
        _profileType = profileType;
    }

    /**
    *   Get the memberships that are associated with this Profile
    *   @return the memberships that are associated with this Profile
    */
    @OneToMany(fetch = FetchType.LAZY,  mappedBy = Membership.PROFILE_PROP, orphanRemoval = true)
    @Cascade(CascadeType.ALL)
    @BatchSize(size = 5)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.PROFILE_DATA)
    public Set<Membership> getMembershipSet()
    {
        return _membershipSet;
    }

    /**
    *   Set the memberships that are associated with this Profile
    *   @param membershipSet the memberships to be associated with this Profile
    */
    public void setMembershipSet(Set<Membership> membershipSet)
    {
        _membershipSet = membershipSet;
    }

    @Column(name = NAME_COLUMN_PROP)
    @NotNull
    @Nonnull
    @Override
    public LocalizedObjectKey getName()
    {
        return _name;
    }
    /**
     *   Set the name of this Profile
     *   @param name the name
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
     *   Set the description of this Profile
     *   @param description the description
     */
    public void setDescription(@Nullable LocalizedObjectKey description)
    {
        _description = description;
    }

    /**
     *   Get this CoachingEntity's Repository
     *   @return the repository
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = Repository.ID_COLUMN)
    @Cascade(CascadeType.ALL)
    @Nullable
    public Repository getRepository()
    {
        return _repository;
    }
    /**
     *   Set this CoachingEntity's Repository
     *   @param repository the repository
     */
    public void setRepository(@Nullable Repository repository)
    {
        _repository = repository;
    }
}
