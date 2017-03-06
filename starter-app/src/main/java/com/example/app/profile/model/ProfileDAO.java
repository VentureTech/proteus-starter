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
import com.example.app.profile.model.membership.Membership;
import com.example.app.profile.model.membership.MembershipOperation;
import com.example.app.profile.model.membership.MembershipType;
import com.example.app.profile.model.membership.MembershipTypeInfo;
import com.example.app.profile.model.user.User;
import com.example.app.support.service.AppUtil;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolationException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Supplier;

import com.i2rd.hibernate.util.HibernateUtil;

import net.proteusframework.cms.label.Label;
import net.proteusframework.core.JunctionOperator;
import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.ConcatTextSource;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.spring.ApplicationContextUtils;
import net.proteusframework.ui.search.JoinedQLBuilder;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.QLResolverOptions;
import net.proteusframework.users.model.PrincipalStatus;

import static com.example.app.profile.model.membership.Membership.OPERATIONS_PROP;
import static com.example.app.profile.model.membership.Membership.USER_PROP;
import static com.example.app.profile.model.membership.MembershipOperation.PROGRAMMATIC_IDENTIFIER_COLUMN_PROP;
import static com.example.app.profile.model.user.User.PRINCIPAL_PROP;
import static com.example.app.support.service.AppUtil.convertForPersistence;
import static com.example.app.support.service.AppUtil.getZonedDateTimeForComparison;
import static net.proteusframework.core.StringFactory.isEmptyString;
import static net.proteusframework.ui.search.PropertyConstraint.Operator.*;
import static net.proteusframework.ui.search.QLBuilder.JoinType.INNER;

/**
 * {@link DAOHelper} implementation for Profile and related entities
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/23/15 10:16 AM
 */
@Repository
@Lazy
public class ProfileDAO extends DAOHelper implements Serializable
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ProfileDAO.class);
    private static final long serialVersionUID = 7736105536343360133L;

    @Autowired
    private transient AppUtil _appUtil;
    @Autowired
    private transient EntityRetriever _er;
    @Value("#{new Boolean('${update-memberships:false}')}")
    private transient Boolean _updateMemberships;

    /**
     * Instantiates a new Profile dao.
     */
    public ProfileDAO()
    {
        super();
    }

    /**
     * Returns a boolean flag on whether or not the given User can perform the given MembershipOperations on the given Profile
     *
     * @param user the User, may be null
     * @param profile the Profile, may be null
     * @param operations the MembershipOperations to check
     * @param timeZone the timezone.
     *
     * @return a boolean flag.  If true, the given user can perform the given operation on the given profile
     */
    public boolean canOperate(@Nullable User user, @Nullable Profile profile, TimeZone timeZone, MembershipOperation... operations)
    {
        if(user != null && _appUtil.userHasAdminRole(user)) return true;
        if (profile == null) return false;
        return canOperate(user, Collections.singletonList(profile), timeZone, operations);
    }

    /**
     * Test if the specified user can perform all the operations on any of the specified profiles.
     *
     * @param user the User.
     * @param profiles the Profiles.
     * @param timeZone the timezone.
     * @param operations the MembershipOperations to check
     *
     * @return true or false.
     */
    @Contract("null,_,_->false")
    public boolean canOperate(@Nullable User user, @Nonnull Collection<Profile> profiles,
        TimeZone timeZone,
        @Nonnull MembershipOperation... operations)
    {
        if(user != null && _appUtil.userHasAdminRole(user)) return true;
        if (user == null || profiles.isEmpty())
            return false;
        final Date now = convertForPersistence(getZonedDateTimeForComparison(timeZone));
        Preconditions.checkArgument(operations.length > 0);
        final org.hibernate.query.Query<Number> query = getSession().createQuery(
            "SELECT COUNT(m) FROM Membership m INNER JOIN m.profile p\n"
            + " INNER JOIN m.operations  op\n"
            + " WHERE m.user = :user\n"
            + " AND p IN (:profiles)\n"
            + " AND op IN (:operations)\n"
            + " AND (m.startDate IS NULL OR m.startDate <= :today)\n"
            + " AND (m.endDate IS NULL OR m.endDate >= :today)\n"
            + " GROUP BY m\n"
            + "  HAVING COUNT(op) = :operationCount", Number.class);
        query.setCacheable(true).setCacheRegion(ProjectCacheRegions.PROFILE_QUERY);
        query.setParameter("user", user);
        query.setParameterList("profiles", profiles);
        query.setParameterList("operations", operations);
        query.setParameter("today", now);
        query.setParameter("operationCount", operations.length);
        return Optional.ofNullable(((Number) query.uniqueResult()))
            .map(Number::intValue)
            .map(i -> i > 0)
            .orElse(false);
    }

    /**
     * Returns a boolean flag on whether or not the given User can perform the given MembershipOperation on the given Profile
     *
     * @param user the User, may be null
     * @param profileType the ProfileType.
     * @param operations the MembershipOperations to check
     * @param timeZone the timezone.
     *
     * @return a boolean flag.  If true, the given user can perform the given operation on the given profile
     */
    public boolean canOperate(@Nullable User user, @Nullable ProfileType profileType,
        TimeZone timeZone,
        MembershipOperation... operations)
    {
        if(user != null && _appUtil.userHasAdminRole(user)) return true;
        if (user == null || profileType == null) return false;
        Preconditions.checkArgument(operations.length > 0);
        final Date now = convertForPersistence(getZonedDateTimeForComparison(timeZone));
        final org.hibernate.query.Query<Number> query = getSession().createQuery(
            "SELECT COUNT(m) FROM Membership m INNER JOIN m.profile p INNER JOIN p.profileType pt\n"
            + " INNER JOIN m.operations  op\n"
            + " WHERE m.user = :user\n"
            + " AND pt.id = :profileTypeId\n"
            + " AND op IN (:operations)\n"
            + " AND (m.startDate IS NULL OR m.startDate <= :today)\n"
            + " AND (m.endDate IS NULL OR m.endDate >= :today)\n"
            + " GROUP BY m\n"
            + "  HAVING COUNT(op) = :operationCount", Number.class);
        query.setCacheable(true).setCacheRegion(ProjectCacheRegions.PROFILE_QUERY);
        query.setParameter("user", user);
        query.setParameter("profileTypeId", profileType.getId());
        query.setParameterList("operations", operations);
        query.setParameter("today", now);
        query.setParameter("operationCount", operations.length);
        return Optional.ofNullable(((Number) query.uniqueResult()))
            .map(Number::intValue)
            .map(i -> i > 0)
            .orElse(false);
    }

    /**
     * Create a Membership for the given Profile, User combination.
     * If a MembershipType is given, the Operations from that MembershipType are copied into the resulting Membership
     *
     * @param profile the Profile
     * @param user the User
     * @param membershipType the MembershipType
     * @param startDate optional start date.
     * @param refreshEntity refresh profile entity flag.
     *
     * @return a new Membership for the given Profile, User combination
     */
    public Membership createMembership(@Nonnull Profile profile, @Nonnull User user, @Nullable MembershipType membershipType,
        @Nullable ZonedDateTime startDate, boolean refreshEntity)
    {
        if (refreshEntity)
            profile = _er.reattachIfNecessary(profile);
        if (refreshEntity && !isTransient(profile))
            getSession().refresh(profile);
        Membership result = new Membership();
        result.setStartDate(convertForPersistence(startDate));
        result.setProfile(profile);
        result.setUser(user);
        if (membershipType != null)
        {
            membershipType = _er.reattachIfNecessary(membershipType);
            result.setMembershipType(membershipType);
            result.getOperations().addAll(membershipType.getDefaultOperations());
        }
        profile.getMembershipSet().add(result);
        return result;
    }

    /**
     * Create an save a ProfileDatedRecord for the given Profile, category, and subcategory.
     * Uses LocalDateTime.now() for the date, at UTC.
     *
     * @param profile the Profile
     * @param category the category
     * @param subCategory the subcategory
     *
     * @return the new ProfileDatedRecord
     */
    public ProfileDatedRecord createProfileDatedRecord(Profile profile, String category, @Nullable String subCategory)
    {
        ProfileDatedRecord pdr = new ProfileDatedRecord();
        pdr.setProfile(profile);
        pdr.setDate(convertForPersistence(ZonedDateTime.now(ZoneOffset.UTC)));
        pdr.setCategory(category);
        pdr.setSubCategory(subCategory);
        saveProfileDatedRecord(pdr);
        return pdr;
    }

    /**
     * Gets profile.
     *
     * @param <P> the type parameter
     * @param clazz the clazz
     * @param profileId the profile id
     *
     * @return the profile
     */
    @SuppressWarnings("unchecked")
    public <P extends Profile> P getProfile(Class<P> clazz, Integer profileId)
    {
        return (P) getSession().get(clazz, profileId);
    }

    /**
     * Save the given ProfileDatedRecord into the database
     *
     * @param datedRecord the ProfileDatedRecord
     */
    public void saveProfileDatedRecord(ProfileDatedRecord datedRecord)
    {
        doInTransaction(session -> {
            session.saveOrUpdate(datedRecord);
        });
    }

    /**
     * Delete the given Membership from the database
     *
     * @param membership the membership to delete
     */
    public void deleteMembership(Membership membership)
    {
        doInTransaction(session -> {
            Membership toDelete = _er.reattachIfNecessary(membership);
            toDelete.getProfile().getMembershipSet().remove(toDelete);
            session.delete(toDelete);
            session.merge(toDelete.getProfile());
        });
    }

    /**
     * Delete the given ProfileDatedRecord from the database
     *
     * @param datedRecord the ProfileDatedRecord
     */
    public void deleteProfileDatedRecord(ProfileDatedRecord datedRecord)
    {
        doInTransaction(session -> {
            session.delete(datedRecord);
        });
    }

    /**
     * Delete the given ProfileType from the database
     *
     * @param profileType the ProfileType to delete
     */
    public void deleteProfileType(ProfileType profileType)
    {
        doInTransaction(session -> {
            session.delete(profileType);
        });
    }

    /**
     * Get the children of the given subclass for the given Profile parent
     *
     * @param profile the parent profile
     * @param clazz the subclass
     * @param <Pr> profile type.
     *
     * @return the children
     */
    public <Pr extends Profile> List<Pr> getChildren(Pr profile, Class<Pr> clazz)
    {
        List<Pr> children = new ArrayList<>();
        getDirectChildren(profile, clazz).forEach(child -> {
            //Do check on child != profile to prevent infinite loop
            children.add(child);
            if (child != profile)
            {
                children.addAll(getChildren(child, clazz));
            }
        });
        return children;
    }

    /**
     * Get ProfileDatedRecords for the given Profile,
     * starting at the given startDate and ending at the given endDate for the given category and subCategory
     *
     * @param profile the Profile
     * @param startDate the start date
     * @param endDate the end date
     * @param category the category
     * @param subCategory the subCategory
     *
     * @return ProfileDatedRecords
     */
    @SuppressWarnings("unchecked")
    public List<ProfileDatedRecord> getDatedRecords(Profile profile, ZonedDateTime startDate, @Nullable ZonedDateTime endDate,
        String category, @Nullable String subCategory)
    {
        StringBuilder hql = new StringBuilder();
        hql.append("SELECT pdr FROM ProfileDatedRecord pdr\n");
        hql.append("WHERE pdr.profile.id = :profileId\n");
        hql.append("AND pdr.date >= :startDate\n");
        if (endDate != null) hql.append("AND pdr.date <= :endDate\n");
        hql.append("AND pdr.category = :category\n");
        if (!isEmptyString(subCategory)) hql.append("AND pdr.subCategory = :subCategory\n");
        return (List<ProfileDatedRecord>) doInTransaction(session -> {
            Query query = session.createQuery(hql.toString());
            query.setParameter("profileId", profile.getId());
            query.setParameter("startDate", convertForPersistence(startDate));
            query.setParameter("category", category);
            if (endDate != null) query.setParameter("endDate", convertForPersistence(endDate));
            if (!isEmptyString(subCategory)) query.setParameter("subCategory", subCategory);
            return query.list();
        });
    }

    /**
     * Get the direct children of the given subclass for the given Profile parent
     *
     * @param profile the parent profile
     * @param clazz the subclass
     * @param <Pr> profile type.
     *
     * @return the children
     */
    @SuppressWarnings("unchecked")
    public <Pr extends Profile> List<Pr> getDirectChildren(Pr profile, Class<Pr> clazz)
    {
        return getSession().createQuery(
            "select pr from " + clazz.getSimpleName() + " pr where pr.parent=:parent")
            .setParameter("parent", profile)
            .list();
    }

    /**
     * Get the name of the given Profile, along with hierarchy based on profile parent.
     * <br><br>
     * example output:  ProfileParent1&gt;ProfileParent2&gt;Profile
     * <br> or ProfileParent1&gt;Profile
     * <br> or Profile
     *
     * @param profile the profile
     *
     * @return the hierarchy names
     */
    @Nonnull
    public TextSource getHierarchyDisplay(Profile profile)
    {
        profile = _er.reattachIfNecessary(profile);
        //Do check on parent != profile to prevent infinite loop
        if (profile.getParent() != null && profile.getParent() != profile)
        {
            return ConcatTextSource.create(getHierarchyDisplay(profile.getParent()), profile.getName())
                .withSeparator(" > ");
        }
        else
        {
            return profile.getName();
        }
    }

    /**
     * Get a MembershipOperation from the database based on the programmatic identifier, or if one does not exist, create it.
     *
     * @param programmaticIdentifier the programmatic identifier to search for
     * @param displayNameSupplier a supplier of the display name of the MembershipOperation -- only used in the case of creation
     *
     * @return a matching MembershipOperation based on programmatic identifier,
     * or a new MembershipOperation that has been persisted.
     */
    public MembershipOperation getMembershipOperationOrNew(
        @Nonnull String programmaticIdentifier, @Nonnull Supplier<LocalizedObjectKey> displayNameSupplier)
    {
        return getMembershipOperation(programmaticIdentifier).orElseGet(() -> {
            MembershipOperation newOperation = new MembershipOperation();
            newOperation.setProgrammaticIdentifier(programmaticIdentifier);
            newOperation.setName(displayNameSupplier.get());

            return saveMembershipOperation(newOperation);
        });
    }

    /**
     * Get a MembershipOperation by programmatic identifier.
     *
     * @param programmaticIdentifier the programmatic identifier.
     *
     * @return an Optional containing a MembershipOperation or containing null if one could not be found
     */
    public Optional<MembershipOperation> getMembershipOperation(@Nullable String programmaticIdentifier)
    {
        if (isEmptyString(programmaticIdentifier)) return Optional.empty();
        return Optional.ofNullable((MembershipOperation) getSession().createQuery(
            "FROM MembershipOperation WHERE programmaticIdentifier = :programmaticIdentifier")
            .setParameter("programmaticIdentifier", programmaticIdentifier)
            .setReadOnly(true)
            .setCacheable(true).setCacheRegion(ProjectCacheRegions.MEMBER_QUERY)
            .setMaxResults(1)
            .uniqueResult());
    }

    /**
     * Save the given MembershipOperation into the database
     *
     * @param membershipOperation the MembershipOperation to save
     *
     * @return the persisted MembershipOperation
     */
    @Nonnull
    public MembershipOperation saveMembershipOperation(
        MembershipOperation membershipOperation)
    {
        try
        {
            HibernateUtil.getInstance().setEntityReadOnly(membershipOperation, false);
            return doInTransaction(session -> {
                session.saveOrUpdate(membershipOperation);
                return membershipOperation;
            });
        }
        catch (ConstraintViolationException e)
        {
            _logger.debug("Unable to save MembershipOperation.  Looks like there is already one within the database with the "
                          + "given programmatic identifier, retrieving it...", e);
            Optional<MembershipOperation> optMembershipOperation = getMembershipOperation(
                membershipOperation.getProgrammaticIdentifier());
            assert optMembershipOperation.isPresent();
            return optMembershipOperation.get();
        }
    }

    /**
     * Get a QLBuilder instance for Membership
     *
     * @return a QLBuilder
     */
    public QLBuilder<Membership> getMembershipQLBuilder()
    {
        return getMembershipQLBuilder(null);
    }

    /**
     * Get a QLBuilder instance for Membership
     *
     * @param options the QLResolverOptions for this QLBuilder
     *
     * @return a QLBuilder
     */
    public QLBuilder<Membership> getMembershipQLBuilder(@Nullable QLResolverOptions options)
    {
        return new QLBuilderImpl<>(Membership.class, "membershipAlias")
            .setQLResolverOptions(options);
    }

    /**
     * Get the MembershipType whose ProgrammaticIdentifier corresponds to the given ProgrammaticIdentifier.
     * If one does not exist, it is created and persisted.
     *
     * @param profileType the ProfileType that owns the MembershipType to search for
     * @param programmaticId the programmatic identifier to search for
     * @param nameSupplier a supplier for the MembershipType's name
     * @param defaultOperationsSupplier a supplier for the MembershipOperations for the MembershipType
     *
     * @return a matching MembershipType, or a newly persisted one.
     */
    @Nonnull
    public MembershipType getMembershipTypeOrNew(@Nonnull ProfileType profileType, @Nonnull String programmaticId,
        @Nonnull Supplier<LocalizedObjectKey> nameSupplier, @Nonnull Supplier<List<MembershipOperation>> defaultOperationsSupplier)
    {
        MembershipType mt = getMembershipType(profileType, programmaticId).orElseGet(() -> {
            ProfileType pt = _er.reattachIfNecessary(profileType);
            MembershipType membershipType = new MembershipType();
            membershipType.setProfileType(pt);
            membershipType.setName(nameSupplier.get());
            membershipType.setProgrammaticIdentifier(programmaticId);
            pt.getMembershipTypeSet().add(membershipType);
            pt = mergeProfileType(pt);
            return getMembershipType(pt, programmaticId).orElseThrow(() -> new IllegalStateException(
                "Unable to find MembershipType even after it was persisted."));
        });
        mt.setDefaultOperations(defaultOperationsSupplier.get());
        return mergeMembershipType(mt);
    }

    /**
     * Get the MembershipType whose ProgrammaticIdentifier corresponds to the given ProgrammaticIdentifier.
     * If one does not exist, it is created and persisted.
     *
     * @param profileType the ProfileType that owns the MembershipType to search for
     * @param info the membershiptype info
     * @param defaultOperationsSupplier a supplier for the MembershipOperations for the MembershipType
     *
     * @return a matching MembershipType, or a newly persisted one.
     */
    @Nonnull
    public MembershipType getMembershipTypeOrNew(@Nonnull ProfileType profileType, MembershipTypeInfo info, @Nonnull
        Supplier<List<MembershipOperation>> defaultOperationsSupplier)
    {
        MembershipType mt = getMembershipType(profileType, info.getProgId()).orElseGet(() -> {
            ProfileType pt = _er.reattachIfNecessary(profileType);
            MembershipType membershipType = new MembershipType();
            membershipType.setProfileType(pt);
            membershipType.setName(info.getNewNameLocalizedObjectKey());
            membershipType.setProgrammaticIdentifier(info.getProgId());
            pt.getMembershipTypeSet().add(membershipType);
            pt = mergeProfileType(pt);
            return getMembershipType(pt, info.getProgId()).orElseThrow(() -> new IllegalStateException(
                "Unable to find MembershipType even after it was persisted."));
        });
        mt.setDefaultOperations(defaultOperationsSupplier.get());
        return mergeMembershipType(mt);
    }

    /**
     * Get the MembershipType whose ProgrammaticIdentifier corresponds to the given ProgrammaticIdentifier,
     * or null if one does not exist
     *
     * @param profileType the  ProfileType that owns the MembershipType to search for
     * @param programmaticId the programmatic identifier to search for
     *
     * @return a matching MembershipType, or null.
     */
    @Nonnull
    public Optional<MembershipType> getMembershipType(@Nonnull ProfileType profileType, @Nonnull String programmaticId)
    {
        return getMembershipType(profileType.getProgrammaticIdentifier(), programmaticId);
    }

    /**
     * Merge the given MembershipType into the database
     *
     * @param membershipType the membership type to save
     *
     * @return the persisted membership type
     */
    @Nonnull
    public MembershipType mergeMembershipType(MembershipType membershipType)
    {
        return mergeMembershipType(membershipType, _updateMemberships);
    }

    /**
     * Merge the given MembershipType into the database
     *
     * @param membershipType the membership type to save
     * @param updateMemberships boolean flag.  If true, will update existing Memberships that use the given MembershipType
     *
     * @return the persisted membership type
     */
    @Nonnull
    public MembershipType mergeMembershipType(MembershipType membershipType, boolean updateMemberships)
    {
        return doInTransaction(session -> {
            MembershipType mt = (MembershipType)session.merge(membershipType);
            if(updateMemberships)
            {
                QLBuilderImpl membershipBuilder = new QLBuilderImpl(Membership.class, "membershipAlias");
                membershipBuilder.appendCriteria(Membership.MEMBERSHIP_TYPE_PROP, eq, mt);
                List<Membership> memberships = membershipBuilder.getQueryResolver().list();
                if(!memberships.isEmpty())
                {
                    _logger.debug("Updating existing memberships within the system based on recently updated membership type: "
                                  + mt.getId());
                }
                memberships.forEach(membership -> {
                    mt.getDefaultOperations().forEach(op -> {
                        if(!membership.getOperations().contains(op))
                        {
                            membership.getOperations().add(op);
                        }
                    });
                    saveMembership(membership);
                });
            }
            return mt;
        });
    }

    /**
     * Get the MembershipType whose ProgrammaticIdentifier corresponds to the given ProgrammaticIdentifier,
     * or null if one does not exist
     *
     * @param profileTypeProgId the programmatic identifier for the ProfileType that owns the MembershipType to search for
     * @param programmaticId the programmatic identifier to search for
     *
     * @return a matching MembershipType, or null.
     */
    @Nonnull
    public Optional<MembershipType> getMembershipType(@Nullable String profileTypeProgId, @Nullable String programmaticId)
    {
        if (isEmptyString(programmaticId)) return Optional.empty();
        return Optional.ofNullable(getProfileType(profileTypeProgId)
            .map(profileType ->
                (MembershipType) new QLBuilderImpl(MembershipType.class, "memTypeAlias")
                    .appendCriteria(MembershipType.PROFILE_TYPE_PROP, eq, profileType)
                    .appendCriteria(MembershipType.PROGRAMMATIC_ID_COLUMN_PROP, eq, programmaticId)
                    .getQueryResolver().createQuery(getSession())
                    .setCacheable(true).setCacheRegion(ProjectCacheRegions.PROFILE_QUERY)
                    .uniqueResult())
            .orElse(null));
    }

    /**
     * Save the given Membership into the database
     *
     * @param membership the membership to save
     */
    public void saveMembership(Membership membership)
    {
        doInTransaction(session -> {
            session.saveOrUpdate(membership);
        });
    }

    /**
     * Get a list of all MembershipTypes for the given Profile via the ProfileType
     *
     * @param profile the Profile to get the MembershipTypes for
     *
     * @return a list of MembershipTypes for the given Profile.
     */
    @Nonnull
    public List<MembershipType> getMembershipTypesForProfile(@Nullable Profile profile)
    {
        if (profile == null) return Collections.emptyList();
        return new ArrayList<>(profile.getProfileType().getMembershipTypeSet());
    }

    /**
     * Determines if the given MembershipType is currently assigned to any Users via a Membership
     *
     * @param membershipType the membership type
     *
     * @return true if in use, false otherwise
     */
    public boolean isMembershipTypeInUse(@Nonnull MembershipType membershipType)
    {
        return doInTransaction(session -> (Long)session.createQuery(
            "SELECT COUNT(mem) FROM Membership mem\n"
            + "WHERE mem.membershipType.id = :memTypeId")
            .setParameter("memTypeId", membershipType.getId())
            .setMaxResults(1)
            .uniqueResult()) > 0;
    }

    /**
     * Get all Memberships for the given Profile, MembershipType combination.
     *
     * @param profile the Profile.
     * @param membershipType the MembershipType.
     * @param timeZone the current TimeZone
     *
     * @return a list of all Memberships for the given Profile, MembershipType combination.
     */
    public List<Membership> getMemberships(@Nonnull Profile profile, @Nonnull MembershipType membershipType, TimeZone timeZone)
    {
        final Date now = convertForPersistence(getZonedDateTimeForComparison(timeZone));
        QLResolverOptions options = new QLResolverOptions();
        options.setCacheRegion(ProjectCacheRegions.PROFILE_QUERY);
        return getMembershipQLBuilder(options)
            .startGroup(JunctionOperator.AND)
            .appendCriteria(Membership.PROFILE_PROP, eq, profile)
            .appendCriteria(Membership.MEMBERSHIP_TYPE_PROP, eq, membershipType)
            .startGroup(JunctionOperator.OR)
            .appendCriteria(Membership.START_DATE_PROP, eq, null)
            .appendCriteria(Membership.START_DATE_PROP, le, now)
            .endGroup()
            .startGroup(JunctionOperator.OR)
            .appendCriteria(Membership.END_DATE_PROP, eq, null)
            .appendCriteria(Membership.END_DATE_PROP, ge, now)
            .endGroup()
            .endGroup()
            .getQueryResolver().list();
    }

    /**
     * Get all Memberships for the given Profile, User combination
     *
     * @param profile the Profile to search for
     * @param user the User to search for
     * @param timeZone the current TimeZone
     *
     * @return a list of all Memberships for the given Profile, User combination
     */
    public List<Membership> getMemberships(@Nonnull Profile profile, @Nonnull User user, TimeZone timeZone)
    {
        final Date now = convertForPersistence(getZonedDateTimeForComparison(timeZone));
        QLResolverOptions options = new QLResolverOptions();
        options.setCacheRegion(ProjectCacheRegions.PROFILE_QUERY);
        return getMembershipQLBuilder(options)
            .startGroup(JunctionOperator.AND)
            .appendCriteria(Membership.PROFILE_PROP, eq, profile)
            .appendCriteria(USER_PROP, eq, user)
            .startGroup(JunctionOperator.OR)
            .appendCriteria(Membership.START_DATE_PROP, eq, null)
            .appendCriteria(Membership.START_DATE_PROP, le, now)
            .endGroup()
            .startGroup(JunctionOperator.OR)
            .appendCriteria(Membership.END_DATE_PROP, eq, null)
            .appendCriteria(Membership.END_DATE_PROP, ge, now)
            .endGroup()
            .endGroup()
            .getQueryResolver().list();
    }

    /**
     * Get memberships for the given profile and membership type programmatic identifier
     *
     * @param profile the profile to search for memberships for
     * @param memTypeProgId the membership type programmatic identifier
     * @param timeZone the timezone.
     *
     * @return a list of all memberships for the given profile that have a membership type with the given programmatic identifier
     */
    @SuppressWarnings("unchecked")
    public List<Membership> getMembershipsForMembershipTypeProgId(Profile profile, String memTypeProgId, TimeZone timeZone)
    {
        final Date now = convertForPersistence(getZonedDateTimeForComparison(timeZone));
        return getSession().createQuery(
            "select membership\n"
            + "from Membership membership\n"
            + "inner join membership.profile profile\n"
            + "left join membership.membershipType memType\n"
            + "where memType.programmaticIdentifier = :memTypeProgId\n"
            + " AND (membership.startDate IS NULL OR membership.startDate <= :today)\n"
            + " AND (membership.endDate IS NULL OR membership.endDate >= :today)\n"
            + "and profile.id = :profileId")
            .setParameter("memTypeProgId", memTypeProgId)
            .setParameter("profileId", profile.getId())
            .setParameter("today", now)
            .setCacheRegion(ProjectCacheRegions.PROFILE_QUERY)
            .setCacheable(true)
            .list();
    }

    /**
     * Get a list of Memberships for the given user that are for the given Profile subclass
     *
     * @param user the User to search for
     * @param profileSubclass the Profile subclass to filter by
     * @param timeZone the time zone.
     *
     * @return a list of all Memberships for the given user, profile subclass combination
     */
    @SuppressWarnings("unchecked")
    public List<Membership> getMembershipsForProfileSubClass(
        @Nonnull User user, @Nonnull Class<? extends Profile> profileSubclass, TimeZone timeZone)
    {
        final Calendar calendar = Calendar.getInstance(timeZone, Locale.ENGLISH);
        calendar.add(Calendar.HOUR_OF_DAY, -1);// truncate to an hour for caching
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        final Date now = calendar.getTime();
        return getSession().createQuery(
            "select membership\n"
            + "from Membership membership, " + profileSubclass.getSimpleName() + " subclass\n"
            + "where membership.profile=subclass\n"
            + "and membership.user=:user\n"
            + "and (membership.startDate is null or membership.startDate <= :today)\n"
            + "and (membership.endDate is null or membership.endDate >= :today)")
            .setParameter("user", user)
            .setParameter("today", now)
            .setCacheable(true)
            .setCacheRegion(ProjectCacheRegions.MEMBER_QUERY)
            .list();
    }

    /**
     * Get all memberships for the given User
     *
     * @param user the user to search for
     * @param orderBy order by expression
     * @param timeZone the current TimeZone
     *
     * @return all memberships for the user
     */
    @SuppressWarnings("unchecked")
    public List<Membership> getMembershipsForUser(@Nullable User user, @Nullable String orderBy, TimeZone timeZone)
    {
        if (user == null || isTransient(user)) return Collections.emptyList();
        StringBuilder query = new StringBuilder();
        query.append("select membership\n"
                     + "from Membership membership\n"
                     + "where membership.user=:user\n"
                     + "and (membership.startDate is null or membership.startDate <= :today)\n"
                     + "and (membership.endDate is null or membership.endDate >= :today)\n");
        if (!isEmptyString(orderBy))
        {
            query.append("order by ").append(orderBy);
        }
        final Date now = convertForPersistence(getZonedDateTimeForComparison(timeZone));
        return getSession().createQuery(query.toString())
            .setParameter("user", user)
            .setParameter("today", now)
            .setCacheable(true).setCacheRegion(ProjectCacheRegions.MEMBER_QUERY)
            .list();
    }

    /**
     * Get the ProfileType whose ID corresponds to the given ID, or null if one does not exist.
     *
     * @param id the id to search for
     *
     * @return a matching ProfileType, or null.
     */
    @Nullable
    public ProfileType getProfileType(@Nullable Long id)
    {
        if (id == null || id == 0L) return null;
        return (ProfileType) getSession().get(ProfileType.class, id);
    }

    /**
     * Get a ProfileType with the given programmatic identifier, or create one
     *
     * @param programmaticId the programmatic identifier
     * @param nameSupplier a Supplier for a LocalizedObjectKey -- only used if the ProfileType is created
     * @param kindSupplier a Supplier for a Profile Type Kind.  This may return null.
     *
     * @return the persisted ProfileType
     */
    @Nonnull
    public ProfileType getProfileTypeOrNew(@Nonnull String programmaticId, @Nonnull Supplier<LocalizedObjectKey> nameSupplier,
        @Nonnull Supplier<Label> kindSupplier)
    {
        return getProfileType(programmaticId).orElseGet(() -> {
            ProfileType pt = new ProfileType();
            pt.setProgrammaticIdentifier(programmaticId);
            pt.setName(nameSupplier.get());
            pt.setKind(kindSupplier.get());

            return mergeProfileType(pt);
        });
    }

    /**
     * Get the ProfileType whose ProgrammaticIdentifier corresponds to the given ProgrammaticIdentifier,
     * or null if one does not exist
     *
     * @param programmaticId the programmatic identifier to search for
     *
     * @return a matching ProfileType, or null.
     */
    @Nonnull
    public Optional<ProfileType> getProfileType(@Nullable String programmaticId)
    {
        if (isEmptyString(programmaticId)) return Optional.empty();
        return Optional.ofNullable((ProfileType) getProfileTypeQLBuilder()
            .appendCriteria(ProfileType.PROGRAMMATIC_ID_COLUMN_PROP, eq, programmaticId)
            .getQueryResolver().createQuery(getSession()).uniqueResult());
    }

    /**
     * Save the given ProfileType into the database
     *
     * @param profileType the ProfileType to save
     *
     * @return the persisted ProfileType
     */
    public ProfileType mergeProfileType(ProfileType profileType)
    {
        return doInTransaction(session -> (ProfileType) session.merge(profileType));
    }

    /**
     * Get a QLBuilder instance for ProfileType
     *
     * @return a QLBuilder
     */
    public QLBuilder getProfileTypeQLBuilder()
    {
        return new QLBuilderImpl(ProfileType.class, "profileTypeAlias");
    }

    /**
     * Get a list of all ProfileTypes within the system
     *
     * @param kind the Kind to filter by.  If null, an unfiltered list is returned.
     *
     * @return a list of ProfileTypes
     */
    @Nonnull
    public List<ProfileType> getProfileTypes(@Nullable Label kind)
    {
        if (kind == null)
        {
            return getProfileTypeQLBuilder().getQueryResolver().list();
        }
        else
        {
            return getProfileTypeQLBuilder().appendCriteria(ProfileType.KIND_PROP, eq, kind)
                .getQueryResolver().list();
        }
    }

    /**
     * Get a list of all {@link P} that the given User has a Membership for
     *
     * @param <P> the profile subclass
     * @param user the user to search for
     * @param profileSubclass the Profile subclass to filter by
     * @param timeZone the TimeZone
     *
     * @return a list of P that the given User has a Membership for
     */
    @Nonnull
    public <P extends Profile> List<P> getProfilesThatUserHasMembershipFor(
        @Nullable User user, @Nonnull Class<P> profileSubclass, TimeZone timeZone)
    {
        return getProfilesThatUserHasMembershipFor(user, profileSubclass, null, timeZone);
    }

    /**
     * Get a list of all {@link P} that the given User has a Membership for
     *
     * @param <P> the profile subclass
     * @param user the user to search for
     * @param profileSubclass the Profile subclass to filter by
     * @param additionalQueryLine an additional query line to include in the query
     * @param timeZone the current TimeZone
     *
     * @return a list of P that the given User has a Membership for
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public <P extends Profile> List<P> getProfilesThatUserHasMembershipFor(
        @Nullable User user, @Nonnull Class<P> profileSubclass, @Nullable String additionalQueryLine, TimeZone timeZone)
    {
        if (user == null) return Collections.emptyList();
        final Date now = convertForPersistence(getZonedDateTimeForComparison(timeZone));
        StringBuilder query = new StringBuilder();
        query.append("select distinct subclass \n")
            .append("from ").append(profileSubclass.getSimpleName()).append(" subclass, Membership membership\n")
            .append("where membership.profile=subclass\n")
            .append("and membership.user=:user\n")
            .append("and (membership.startDate is null or membership.startDate <= :today)\n")
            .append("and (membership.endDate is null or membership.endDate >= :today)\n");
        if (!isEmptyString(additionalQueryLine))
            query.append(additionalQueryLine);
        return getSession().createQuery(query.toString())
            .setParameter("user", user)
            .setParameter("today", now)
            .setCacheable(true)
            .setCacheRegion(ProjectCacheRegions.PROFILE_QUERY)
            .list();
    }

    /**
     * Get the owner of the given repository that has the given Profile subclass
     *
     * @param repository the Repository
     * @param pClass the Profile subclass
     * @param <P> the Profile subclass
     *
     * @return the owner of the Repository or empty
     */
    @SuppressWarnings("unchecked")
    public <P extends Profile> Optional<P> getRepoOwner(com.example.app.profile.model.repository.Repository repository,
        Class<P> pClass)
    {
        return Optional.ofNullable(_er.narrowProxyIfPossible((P) getSession().createQuery(
            "select p from " + pClass.getSimpleName() + " p inner join p.repository repo where repo.id = :repoId")
            .setParameter("repoId", repository.getId())
            .setMaxResults(1)
            .uniqueResult()));
    }

    /**
     * Get a list of Users that have a Membership to the given Profile that has the given MembershipOperation
     *
     * @param profile the Profile
     * @param operation the Operation
     * @param timeZone the timezone.
     *
     * @return list of Users
     */
    public List<User> getUsersWithOperation(@Nonnull Profile profile, TimeZone timeZone, @Nonnull
        MembershipOperation operation)
    {
        ZonedDateTime now = ZonedDateTime.now(timeZone.toZoneId());
        return getUsersWithOperation(profile, operation, now, now);
    }

    /**
     * Get a list of Users that have a Membership to the given Profile that has the given MembershipOperation
     *
     * @param profile the Profile
     * @param operation the Operation
     * @param startDate the start date to use when looking for active memberships
     * @param endDate the end date to use when looking for active memberships
     *
     * @return list of Users
     */
    @SuppressWarnings("unchecked")
    public List<User> getUsersWithOperation(@Nonnull Profile profile, @Nonnull
        MembershipOperation operation,
        @Nonnull ZonedDateTime startDate, @Nonnull ZonedDateTime endDate)
    {
        return getSession().createQuery(
            "select distinct user\n"
            + "from Membership membership\n"
            + "inner join membership.user user\n"
            + "inner join membership.profile profile\n"
            + "inner join membership.operations operation\n"
            + "where profile.id = :profileId\n"
            + "and operation.id = :mopId\n"
            + "and (membership.startDate is null or membership.startDate <= :startDate)\n"
            + "and (membership.endDate is null or membership.endDate >= :endDate)\n")
            .setParameter("profileId", profile.getId())
            .setParameter("mopId", operation.getId())
            .setParameter("startDate", convertForPersistence(startDate))
            .setParameter("endDate", convertForPersistence(endDate))
            .list();
    }

    /**
     * Get users that have the given Operations on their Membership for the given Profile
     *
     * @param profile the Profile to get Users for
     * @param timeZone the current TimeZone
     * @param ops the Membership Operations
     *
     * @return a list of Users
     */
    public List<User> getUsersWithOperations(
        Profile profile, TimeZone timeZone, @Nonnull MembershipOperation... ops)
    {
        return getUsersWithOperationsQLBuilder(profile, timeZone, ops).getQueryResolver().list();
    }

    /**
     * Get QLBuilder for users that have the given Operations on their Membership for the given Profile
     *
     * @param profile the Profile to get Users for
     * @param timeZone the current TimeZone
     * @param ops the Membership Operations
     *
     * @return a QLBuilder for possible facilitators
     */
    public QLBuilder getUsersWithOperationsQLBuilder(
        @Nonnull Profile profile, TimeZone timeZone, @Nonnull MembershipOperation... ops)
    {
        final Date now = convertForPersistence(getZonedDateTimeForComparison(timeZone));
        QLBuilderImpl builder = new QLBuilderImpl(Profile.class, "profile");
        JoinedQLBuilder membership = builder.createJoin(INNER, Profile.MEMBERSHIPS_PROP, "membership");
        membership.startGroup(JunctionOperator.AND)
            .startGroup(JunctionOperator.OR)
            .appendCriteria(Membership.START_DATE_PROP, eq, null)
            .appendCriteria(Membership.START_DATE_PROP, le, now)
            .endGroup()
            .startGroup(JunctionOperator.OR)
            .appendCriteria(Membership.END_DATE_PROP, eq, null)
            .appendCriteria(Membership.END_DATE_PROP, ge, now)
            .endGroup();
        JoinedQLBuilder user = membership.createJoin(INNER, USER_PROP, "user");
        JoinedQLBuilder principal = user.createJoin(INNER, PRINCIPAL_PROP, "principal");
        JoinedQLBuilder operation = membership.createJoin(INNER, OPERATIONS_PROP, "operation");
        builder.setProjection("distinct " + user.getAlias());
        builder.appendCriteria("id", eq, profile.getId());
        principal.appendCriteria("status", eq, PrincipalStatus.active);
        if (ops.length > 0)
        {
            operation.startGroup(JunctionOperator.OR);
            for (MembershipOperation op : ops)
            {
                operation.appendCriteria(PROGRAMMATIC_IDENTIFIER_COLUMN_PROP, eq, op.getProgrammaticIdentifier());
            }
            operation.endGroup();
        }
        return user;
    }

    /**
     * Test if the specified user has any of the specified MembershipTypes on
     * any active profile.
     *
     * @param user the user.
     * @param timeZone the current TimeZone
     * @param types the types.
     *
     * @return true or false.
     */
    public boolean hasMembership(User user, TimeZone timeZone, MembershipType... types)
    {
        final Date now = convertForPersistence(getZonedDateTimeForComparison(timeZone));
        Number count = (Number)
            getSession().createQuery(
                "SELECT COUNT(DISTINCT p) FROM Profile p INNER JOIN p.membershipSet m INNER JOIN m.membershipType mt\n"
                + " WHERE m.user = :user AND mt IN (:types)\n"
                + " AND (m.startDate IS NULL OR m.startDate <= :now)\n"
                + " AND (m.endDate IS NULL OR m.endDate >= :now)"
            )
                .setParameter("user", user)
                .setParameterList("types", types)
                .setParameter("now", now)
                .setCacheRegion(ProjectCacheRegions.PROFILE_QUERY)
                .setCacheable(true)
                .uniqueResult();
        return count.intValue() > 0;
    }

    /**
     * Return boolean flag on whether the given user has a membership on the given profile
     *
     * @param profile the Profile to search for
     * @param user the User to search for
     * @param timeZone the current timezone
     *
     * @return boolean flag -- if true, the user has a membership on the given profile
     */
    public Boolean hasMembership(@Nonnull Profile profile, @Nonnull User user, TimeZone timeZone)
    {
        final Date now = convertForPersistence(getZonedDateTimeForComparison(timeZone));
        QLResolverOptions options = new QLResolverOptions();
        options.setCacheRegion(ProjectCacheRegions.PROFILE_QUERY);
        return getMembershipQLBuilder(options)
            .setProjection("count(*)")
                .changeRelationClass(Number.class)
            .startGroup(JunctionOperator.AND)
            .appendCriteria(Membership.PROFILE_PROP, eq, profile)
            .appendCriteria(USER_PROP, eq, user)
            .startGroup(JunctionOperator.OR)
            .appendCriteria(Membership.START_DATE_PROP, eq, null)
            .appendCriteria(Membership.START_DATE_PROP, le, now)
            .endGroup()
            .startGroup(JunctionOperator.OR)
            .appendCriteria(Membership.END_DATE_PROP, eq, null)
            .appendCriteria(Membership.END_DATE_PROP, ge, now)
            .endGroup()
            .endGroup()
            .getQueryResolver().createQuery(getSession())
            .setCacheable(true).setCacheRegion(ProjectCacheRegions.PROFILE_QUERY)
            .uniqueResult().longValue() > 0;
    }

    /**
     * Save the given Profile into the database
     *
     * @param profile the Profile to save
     *
     * @return updated profile in case of a merge operation.
     */
    public Profile saveOrMergeProfile(Profile profile)
    {
        return
            doInTransaction(session -> {
                final Profile toReturn;
                if (HibernateUtil.getInstance().isTransient(profile) || session.contains(profile))
                {
                    session.saveOrUpdate(profile);
                    toReturn = profile;
                }
                else
                {
                    toReturn = (Profile) session.merge(profile);
                }
                return toReturn;
            });
    }

    /**
     * Save the given Profile into the database
     *
     * @param profile the Profile to save
     */
    public void saveProfile(Profile profile)
    {
        doInTransaction(session -> {
            session.saveOrUpdate(profile);
        });
    }

    private Object readResolve() throws ObjectStreamException
    {
        ApplicationContext context = ApplicationContextUtils.getInstance().getContext();
        assert context != null;
        return context.getBean(ProfileDAO.class);
    }

    @Contract(pure = true)
    private Object writeReplace() throws ObjectStreamException
    {
        return this;
    }
}
