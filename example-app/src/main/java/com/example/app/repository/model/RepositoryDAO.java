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

package com.example.app.repository.model;


import com.example.app.config.ProjectCacheRegions;
import com.example.app.profile.model.Profile;
import com.example.app.profile.model.ProfileDAO;
import com.example.app.profile.model.membership.MembershipOperation;
import com.example.app.profile.model.user.User;
import com.example.app.resource.model.Resource;
import com.example.app.support.service.AppUtil;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.core.hibernate.dao.EntityRetriever;

/**
 * {@link DAOHelper} implementation for {@link Repository}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/11/15 9:45 AM
 */
@SuppressWarnings("unused")
@org.springframework.stereotype.Repository
@Lazy
public class RepositoryDAO extends DAOHelper
{
    @Autowired
    private EntityRetriever _er;
    @Autowired
    private ProfileDAO _profileDAO;

    /**
     * Assign the given RepositoryItem to the given Repository if a relation does not already exist.
     * If a Relation already exists, returns the existing relation.
     *
     * @param repository the Repository
     * @param repositoryItem the RepositoryItem
     *
     * @return the newly persisted assignment relation, or the existing relation
     */
    public RepositoryItemRelation assignRepositoryItem(Repository repository, RepositoryItem repositoryItem)
    {
        RepositoryItemRelation rirel = getRelationOrNew(repository, repositoryItem, null);
        if (isTransient(rirel))
        {
            return mergeRepositoryItemRelation(rirel);
        }
        else return rirel;
    }

    /**
     * Get an existing Relation for the given  Repository/RepositoryItem combination.
     * If a Relation does not exist, one is created and returned
     *
     * @param repository the Repository
     * @param repositoryItem the RepositoryItem
     * @param relationType the relation type for the newly created relation.  If null, it defaults to assigned
     *
     * @return the existing Relation, or a newly created, unpersisted Relation
     */
    public RepositoryItemRelation getRelationOrNew(
        Repository repository, RepositoryItem repositoryItem, @Nullable RepositoryItemRelationType relationType)
    {
        return getRelation(repository, repositoryItem).orElseGet(() -> {
            RepositoryItemRelation rirel = new RepositoryItemRelation();
            rirel.setRepository(repository);
            rirel.setRepositoryItem(repositoryItem);
            rirel.setRelationType(relationType != null ? relationType : RepositoryItemRelationType.assigned);
            return rirel;
        });
    }

    /**
     * Persist the given RepositoryItem into the database via a merge command
     *
     * @param repositoryItemRelation the RepositoryItem to save
     *
     * @return the persisted RepositoryItem
     */
    public RepositoryItemRelation mergeRepositoryItemRelation(RepositoryItemRelation repositoryItemRelation)
    {
        return doInTransaction(session -> (RepositoryItemRelation) session.merge(repositoryItemRelation));
    }

    /**
     * Get the RepositoryItemRelation for the given Repository/RepositoryItem combination, if one exists
     *
     * @param repository the Repository
     * @param repositoryItem the  RepositoryItem
     *
     * @return the RepositoryItemRelation
     */
    public Optional<RepositoryItemRelation> getRelation(Repository repository, RepositoryItem repositoryItem)
    {
        return Optional.ofNullable((RepositoryItemRelation) getSession().createQuery("SELECT rirel\n"
                                                                                     + "FROM RepositoryItemRelation rirel\n"
                                                                                     + "where rirel.repository.id = :repoId\n"
                                                                                     + "and rirel.repositoryItem.id = :repoItemId")
            .setParameter("repoId", repository.getId())
            .setParameter("repoItemId", repositoryItem.getId())
            .setMaxResults(1)
            .uniqueResult());
    }

    /**
     * Get boolean flag.  If true, the given user can perform the given MembershipOperation on the given Repository
     *
     * @param user the User to check
     * @param repository the Repository to check for permission
     * @param timeZone the timezone.
     * @param operation the Operation the User wishes to perform
     *
     * @return boolean flag
     */
    public boolean canOperate(@Nonnull User user, @Nonnull Repository repository, TimeZone timeZone,
        @Nonnull MembershipOperation operation)
    {
        Optional<Profile> owner = getOwnerOfRepository(repository);
        return owner
            .map(ownerProfile -> _profileDAO
                .canOperate(user, ownerProfile, timeZone, operation))
            .orElse(false);
    }

    /**
     * Get the owner of the given Repository, if one exists.
     *
     * @param repository the Repository to search for
     *
     * @return an Optional containing the owner of the given Repository, if one exists.
     */
    @Nonnull
    public Optional<Profile> getOwnerOfRepository(@Nonnull Repository repository)
    {
        return Optional.ofNullable((Profile) getSession()
            .createQuery("select owner\n"
                         + "from Profile owner\n"
                         + "where owner.repository=:repository")
            .setParameter("repository", repository)
            .setCacheable(true).setCacheRegion(ProjectCacheRegions.ENTITY_QUERY)
            .uniqueResult());
    }

    /**
     * Delete the given Repository from the database
     *
     * @param repository the Repository to delete
     */
    public void deleteRepository(Repository repository)
    {
        doInTransaction(session -> {
            getRelations(repository).forEach(this::deleteRepositoryItemRelation);
            session.delete(repository);
        });
    }

    /**
     * Get all RepositoryItemRelations for the given Repository
     *
     * @param repository the Repository
     *
     * @return RepositoryItemRelations
     */
    @SuppressWarnings("unchecked")
    public List<RepositoryItemRelation> getRelations(Repository repository)
    {
        return getSession().createQuery("SELECT repoItemRelation\n"
                                        + "FROM RepositoryItemRelation repoItemRelation\n"
                                        + "WHERE repoItemRelation.repository.id = :repoId")
            .setParameter("repoId", repository.getId())
            .list();
    }

    /**
     * Delete the given RepositoryItem from the database
     *
     * @param repositoryItemRelation the RepositoryItem to delete
     */
    @SuppressWarnings("unchecked")
    public void deleteRepositoryItemRelation(RepositoryItemRelation repositoryItemRelation)
    {
        doInTransaction(session -> {
            RepositoryItemRelationType relationType = repositoryItemRelation.getRelationType();
            RepositoryItem repositoryItem = repositoryItemRelation.getRepositoryItem();
            //There should only ever be one OWNED RelationType per RepositoryItem.  If there is more, there is corrupt data.
            if (relationType == RepositoryItemRelationType.owned)
            {
                ((List<RepositoryItemRelation>) session.createQuery("SELECT repoItemRelation\n"
                                                                    + "FROM RepositoryItemRelation repoItemRelation\n"
                                                                    + "WHERE repoItemRelation.repositoryItem.id = :repoItemId\n"
                                                                    + "AND repoItemRelation.id <> :repoItemRelationId")
                    .setParameter("repoItemId", repositoryItem.getId())
                    .setParameter("repoItemRelationId", repositoryItemRelation.getId())
                    .list()).forEach(this::deleteRepositoryItemRelation);
            }
            session.delete(repositoryItemRelation);
            if (relationType == RepositoryItemRelationType.owned)
            {
                session.delete(repositoryItem);
            }
        });
    }

    /**
     * Get the owner of the given RepositoryItem
     *
     * @param repositoryItem the RepositoryItem
     *
     * @return the Owning Repository
     */
    public Repository getOwnerOfRepositoryItem(RepositoryItem repositoryItem)
    {
        return Optional.ofNullable((Repository) getSession().createQuery("SELECT rirel.repository\n"
                                                                         + "FROM RepositoryItemRelation rirel\n"
                                                                         + "WHERE rirel.repositoryItem.id = :repoItemId\n"
                                                                         + "AND rirel.relationType = :owned")
            .setParameter("repoItemId", repositoryItem.getId())
            .setParameter("owned", RepositoryItemRelationType.owned)
            .setMaxResults(1)
            .uniqueResult()).orElseThrow(() -> new IllegalStateException("RepositoryItem has no owning Repository. "
                                                                         + "RepositoryItem#" + repositoryItem.getId()));
    }

    /**
     * Get the Owning relation for the given RepositoryItem,
     * filtered by whether the given User has a membership to the owning Repository
     *
     * @param user the User
     * @param repositoryItem the RepositoryItem
     *
     * @return the owning relation
     */
    public Optional<RepositoryItemRelation> getOwningRelationForUser(@Nonnull User user, @Nonnull RepositoryItem repositoryItem)
    {
        final Date now = getNow();
        return Optional.ofNullable((RepositoryItemRelation) getSession()
            .createQuery("SELECT DISTINCT rirel\n"
                         + "FROM RepositoryItemRelation rirel, Membership m\n"
                         + "INNER JOIN m.profile profile\n"
                         + "WHERE m.user.id = :userId\n"
                         + "AND rirel.repository.id = profile.repository.id\n"
                         + "AND rirel.repositoryItem.id = :repoItemId\n"
                         + "AND (m.startDate IS NULL OR m.startDate <= :today)\n"
                         + "AND (m.endDate IS NULL OR m.endDate >= :today)")
            .setParameter("userId", user.getId())
            .setParameter("repoItemId", repositoryItem.getId())
            .setParameter("today", now)
            .setMaxResults(1)
            .uniqueResult());
    }

    private static Date getNow()
    {
        final Calendar calendar = Calendar.getInstance(AppUtil.staticGetDefaultTimeZone(), Locale.ENGLISH);
        calendar.add(Calendar.HOUR_OF_DAY, -1);// truncate to an hour for caching
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Get all RepositoryItemRelations for the given Repository where the RelationType is Owned
     *
     * @param repository the Repository
     *
     * @return RepositoryItemRelations
     */
    @SuppressWarnings("unchecked")
    public List<RepositoryItemRelation> getOwningRelations(Repository repository)
    {
        return getSession().createQuery("SELECT repoItemRelation\n"
                                        + "FROM RepositoryItemRelation repoItemRelation\n"
                                        + "WHERE repoItemRelation.relationType = :owned\n"
                                        + "AND repoItemRelation.repository.id = :repoId")
            .setParameter("owned", RepositoryItemRelationType.owned)
            .setParameter("repoId", repository.getId())
            .list();
    }

    /**
     * Get the {@link ResourceRepositoryItem} that owns the given Resource, if one exists.
     *
     * @param resource the Resource to search for
     *
     * @return an Optional containing an owning RepositoryItem.
     */
    @Nonnull
    public Optional<ResourceRepositoryItem> getRepoItemForResource(@Nullable Resource resource)
    {
        if (resource == null) return Optional.empty();
        return Optional.ofNullable((ResourceRepositoryItem) getSession()
            .createQuery("select resourceRepoItem\n"
                         + "from ResourceRepositoryItem resourceRepoItem\n"
                         + "where resourceRepoItem.resource=:resource")
            .setParameter("resource", resource)
            .setCacheable(true).setCacheRegion(ProjectCacheRegions.ENTITY_QUERY)
            .uniqueResult());
    }

    /**
     * Get the Repository for the given ID
     *
     * @param id the Id
     *
     * @return the Repository
     */
    public Optional<Repository> getRepository(Integer id)
    {
        return Optional.ofNullable((Repository) getSession().get(Repository.class, id));
    }

    /**
     * Get the RepositoryItem subclass for the given ID
     *
     * @param clazz the RepositoryItem subclass
     * @param id the Id
     * @param <RI> the RepositoryItem subclass
     *
     * @return the RepositoryItem
     */
    @SuppressWarnings("unchecked")
    public <RI extends RepositoryItem> Optional<RI> getRepositoryItem(Class<RI> clazz, Integer id)
    {
        return Optional.ofNullable((RI) getSession().get(clazz, id));
    }

    /**
     * Get the RepositoryItemRelation for the given Id
     *
     * @param id the Id
     *
     * @return the repository item relation
     */
    public Optional<RepositoryItemRelation> getRepositoryItemRelation(Number id)
    {
        return Optional.ofNullable((RepositoryItemRelation) getSession().get(RepositoryItemRelation.class, id.intValue()));
    }

    /**
     * Get a list of RepositoryItems from the given list of Repositories, filtered by the given RepositoryItem subclass
     *
     * @param <RI> the RepositoryItem subclass
     * @param repos the repositories to retrieve the items from
     * @param clazz the RepositoryItem subclass to filter by
     * @param statuses optional statuses.
     *
     * @return a list of all RepositoryItemRelations for the given repos, subclass combination
     */
    public <RI extends RepositoryItem> List<RI> getRepositoryItems(
        @Nonnull List<Repository> repos, @Nonnull Class<RI> clazz, @Nullable RepositoryItemStatus... statuses)
    {
        boolean hasStatuses = statuses != null && statuses.length > 0;
        String hql = "SELECT DISTINCT ri\n"
                     + "FROM RepositoryItemRelation repositoryItemRelation,\n"
                     + "  " + clazz.getName() + " ri\n"
                     + "WHERE repositoryItemRelation.repository.id IN (:repoIds)\n"
                     + "AND repositoryItemRelation.repositoryItem.id = ri.id\n"
                     + (hasStatuses ? "AND ri.status IN (:statuses)" : "")
                     + " ORDER BY ri.createTime ASC \n";
        Query query = getSession().createQuery(hql)
            .setParameterList("repoIds", repos.stream().map(Repository::getId).collect(Collectors.toList()))
            .setCacheable(true)
            .setCacheRegion(ProjectCacheRegions.ENTITY_QUERY);
        if (hasStatuses)
            query.setParameterList("statuses", statuses);
        @SuppressWarnings("unchecked")
        List<RI> list = query.list();
        return list;
    }

    /**
     * Get a list of RepositoryItemRelations that hte given User has access to, filtered by the given RepositoryItem subclass
     *
     * @param <RI> the RepositoryItem subclass
     * @param user the User to search for
     * @param repo the repository to retrieve the items from
     * @param clazz the RepositoryItem subclass to filter by
     *
     * @return a list of all RepositoryItemRelations for the given user, subclass combination
     */
    public <RI extends RepositoryItem> List<RI> getRepositoryItemsForUser(
        @Nonnull User user, @Nonnull Repository repo, @Nonnull Class<RI> clazz)
    {
        return getRepositoryItemsForUser(user, Collections.singletonList(repo), clazz);
    }

    /**
     * Get a list of RepositoryItemRelations that the given User has access to, filtered by the given RepositoryItem subclass
     *
     * @param <RI> the RepositoryItem subclass
     * @param user the User to search for
     * @param repos the repositories to retrieve the items from
     * @param clazz the RepositoryItem subclass to filter by
     * @param statuses optional statuses.
     *
     * @return a list of all RepositoryItemRelations for the given user, subclass combination
     */
    public <RI extends RepositoryItem> List<RI> getRepositoryItemsForUser(@Nonnull User user,
        @Nonnull List<Repository> repos,
        @Nonnull Class<RI> clazz,
        @Nullable RepositoryItemStatus... statuses)
    {
        boolean hasStatuses = statuses != null && statuses.length > 0;
        String hql = "SELECT DISTINCT ri\n"
                     + "FROM Membership m,\n"
                     + ' ' + clazz.getName() + " ri,\n"
                     + "RepositoryItemRelation repoItemRelation\n"
                     + "INNER JOIN m.profile owner\n"
                     + "INNER JOIN owner.repository repo\n"
                     + "WHERE m.user = :user \n"
                     + "AND repo.id IN (:repoIds) \n"
                     + "AND repo.id = repoItemRelation.repository.id\n"
                     + "AND ri.id = repoItemRelation.repositoryItem.id\n"
                     + (hasStatuses ? "AND ri.status IN (:statuses)" : "")
                     + "ORDER BY ri.createTime ASC \n";
        Query query = getSession().createQuery(hql)
            .setParameter("user", user)
            .setParameterList("repoIds", repos.stream().map(Repository::getId).collect(Collectors.toList()))
            .setCacheable(true)
            .setCacheRegion(ProjectCacheRegions.ENTITY_QUERY);
        if (hasStatuses)
            query.setParameterList("statuses", statuses);
        @SuppressWarnings("unchecked")
        List<RI> list = query.list();
        return list;
    }

    /**
     * Persist the given Repository into the database via a merge command
     *
     * @param repository the Repository to save
     *
     * @return the persisted Repository
     */
    public Repository mergeRepository(Repository repository)
    {
        return doInTransaction(session -> (Repository) session.merge(repository));
    }

    /**
     * Save the given Repositoryitem via a merge operation
     *
     * @param repositoryItem the RepositoryItem to save
     * @param <RI> the RepositoryItem Subclass
     *
     * @return the saved repositoryItem
     */
    @SuppressWarnings("unchecked")
    public <RI extends RepositoryItem> RI mergeRepositoryItem(RI repositoryItem)
    {
        return doInTransaction(session -> (RI) session.merge(repositoryItem));
    }
}
