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

package com.example.app.profile.model.membership;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import com.example.app.profile.model.Profile;
import com.example.app.profile.model.ProfileType;
import com.example.app.profile.model.user.User;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

import net.proteusframework.cms.controller.CmsRequestContext;
import net.proteusframework.core.hibernate.dao.EntityRetrieverImpl;
import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.users.model.AbstractAuditableEntity;
import net.proteusframework.users.model.dao.PrincipalContactUtil;

import static net.proteusframework.core.locale.TextSources.createText;

/**
 * Membership entity for Profiles.  It is fully auditable and implements soft delete functionality
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 10/27/15 11:41 AM
 */
@Entity
@Table(name = Membership.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA, indexes = {
    @Index(name = "membership_user_idx", columnList = Membership.USER_COLUMN),
    @Index(name = "membership_profile_idx", columnList = Membership.PROFILE_COLUMN),
    @Index(name = "membership_membershiptype_idx", columnList = Membership.MEMBERSHIP_TYPE_COLUMN),
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.MEMBER_DATA)
@Audited
@I18NFile(
    symbolPrefix = "com.lrlabs.model.profile.Membership",
    i18n = {
        @I18N(symbol = "Name", l10n = @L10N("{0} ({1})")),
        @I18N(symbol = "Description", l10n = @L10N("{0} assigned as {1} in {2} ({3})"))
    }
)
public class Membership extends AbstractAuditableEntity<Integer> implements NamedObject
{
    /** The database table name for this entity */
    public static final String TABLE_NAME = "Membership";
    /** The database id column for this entity */
    public static final String ID_COLUMN = "membership_id";
    /** The database column name for property: membershipType */
    public static final String MEMBERSHIP_TYPE_COLUMN = "membershiptype_id";
    /** The property name: membershipType */
    public static final String MEMBERSHIP_TYPE_PROP = "membershipType";
    /** The database column name for the user property */
    public static final String USER_COLUMN = "user_id";
    /** The property: user */
    public static final String USER_PROP = "user";
    /** The database column name for the profile property */
    public static final String PROFILE_COLUMN = "profile_id";
    /** The property: profile */
    public static final String PROFILE_PROP = "profile";
    /** The join table for property: operations */
    public static final String OPERATIONS_JOIN_TABLE = "membership_operations";
    /** The property: operations */
    public static final String OPERATIONS_PROP = "operations";
    /** The database column and property: startDate */
    public static final String START_DATE_PROP = "startDate";
    /** The database column and property: endDate */
    public static final String END_DATE_PROP = "endDate";
    /** The serial version UID */
    private static final long serialVersionUID = -3652556289337226787L;
    /** The ID generator identifier for this entity */
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + ".membership_id_seq";
    private MembershipType _membershipType;
    private User _user;
    private Profile _profile;
    private List<MembershipOperation> _operations = new ArrayList<>();
    private Date _startDate;
    private Date _endDate;

    @Nonnull
    @Override
    @Transient
    public TextSource getName()
    {
        return createText(
            MembershipLOK.NAME(),
            getUser().getName(),
            Optional.ofNullable(getMembershipType())
                .map(membershipType -> (TextSource) membershipType.getName())
                .orElse(createText("N/A"))
        );
    }

    /**
     * Get the membership type of this Membership
     *
     * @return the membership type of this Membership
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = MEMBERSHIP_TYPE_COLUMN)
    @Nullable
    public MembershipType getMembershipType()
    {
        return _membershipType;
    }

    /**
     * Set the membership type of this Membership
     *
     * @param membershipType the membership type of this Membership
     */
    public void setMembershipType(@Nullable MembershipType membershipType)
    {
        _membershipType = membershipType;
    }

    @Nullable
    @Override
    @Transient
    public TextSource getDescription()
    {
        ProfileType profileType = EntityRetrieverImpl.getInstance().reattachIfNecessary(getProfile().getProfileType());
        return createText(
            MembershipLOK.DESCRIPTION(),
            getUser().getName(),
            Optional.ofNullable(getMembershipType()).map(MembershipType::getName).orElse(null),
            getProfile().getName(),
            profileType.getName()
        );
    }

    /**
     * Get the Profile for this Membership
     *
     * @return the Profile for this Membership
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PROFILE_COLUMN)
    public Profile getProfile()
    {
        return _profile;
    }

    /**
     * Set the Profile for this Membership
     *
     * @param profile the Profile for this Membership
     */
    public void setProfile(Profile profile)
    {
        _profile = profile;
    }

    /**
     * Get the MembershipOperations that this Membership has
     *
     * @return the MembershipOperations
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = OPERATIONS_JOIN_TABLE, schema = ProjectConfig.PROJECT_SCHEMA, joinColumns = @JoinColumn(name = ID_COLUMN),
        inverseJoinColumns = @JoinColumn(name = MembershipOperation.ID_COLUMN))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @BatchSize(size = 10)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
    @Nonnull
    public List<MembershipOperation> getOperations()
    {
        return _operations;
    }

    /**
     * Set the MembershipOperations that this Membership has
     *
     * @param operations the MembershipOperations
     */
    public void setOperations(@Nonnull List<MembershipOperation> operations)
    {
        _operations = operations;
    }

    @Override
    public int hashCode()
    {
        if (getId() != null)
            return getId().hashCode();
        return Objects.hash(super.hashCode(), getMembershipType(), getUser(), getProfile());
    }

    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    @Override
    public boolean equals(@Nullable Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof Membership))
            return false;
        Membership that = (Membership) o;
        if (getId() != null)
            return getId().equals(that.getId());

        return Objects.equals(getMembershipType(), that.getMembershipType())
               && Objects.equals(getUser(), that.getUser())
               && Objects.equals(getProfile(), that.getProfile());
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

    @Override
    public String toString()
    {
        return Hibernate.getClass(this).getName() + '#' + getId();
    }

    /**
     * Test if the membership is active.
     *
     * @return the active flag.
     */
    @Transient
    public boolean isActive()
    {
        final TimeZone defaultTZ = TimeZone.getTimeZone("US/Central");
        TimeZone tz = defaultTZ;
        if (CmsRequestContext.isInRequestResponseCycle())
            tz = CmsRequestContext.getUserTimeZone();
        if (tz == defaultTZ)
        {
            final TimeZone preferredTimeZone = PrincipalContactUtil.getPreferredTimeZone(getUser().getPrincipal());
            if (preferredTimeZone != null)
                tz = preferredTimeZone;
        }
        return isActive(tz);
    }

    /**
     * Get the user for this Membership
     *
     * @return the User for this Membership
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = USER_COLUMN)
    @NotNull
    @Nonnull
    public User getUser()
    {
        return _user;
    }

    /**
     * Set the user for this Membership
     *
     * @param user the User to set for this Membership
     */
    public void setUser(User user)
    {
        _user = user;
    }

    /**
     * Test if the membership is active.
     *
     * @param timeZone the timezone of the user checking the active flag.
     *
     * @return the active flag.
     */
    @Transient
    public boolean isActive(TimeZone timeZone)
    {
        Date startDate = getStartDate();
        Date endDate = getEndDate();
        if (endDate == null && startDate == null) return true;

        final Calendar calendar = Calendar.getInstance(timeZone, Locale.ENGLISH);
        final Date now = calendar.getTime();

        boolean active = true;
        if (startDate != null) active = now.after(startDate);
        if (endDate != null) active = active && now.before(endDate);
        return active;
    }

    /**
     * Get the StartDate of this Membership.  Could be null.
     *
     * @return the start date
     */
    @Nullable
    public Date getStartDate()
    {
        return _startDate;
    }

    /**
     * Set the StartDate of this Membership
     *
     * @param startDate the Start Date
     */
    public void setStartDate(@Nullable Date startDate)
    {
        _startDate = startDate;
    }

    /**
     * Get the EndDate of this Membership.  Could be null.
     *
     * @return the end date
     */
    @Nullable
    public Date getEndDate()
    {
        return _endDate;
    }

    /**
     * Set the EndDate of this Membership
     *
     * @param endDate the End Date
     */
    public void setEndDate(@Nullable Date endDate)
    {
        _endDate = endDate;
    }
}
