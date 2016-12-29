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

package com.example.app.model.user;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.support.AppUtil;
import com.example.app.support.FileSaver;
import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import net.proteusframework.cms.FileSystemDirectory;
import net.proteusframework.cms.dao.CmsFrontendDAO;
import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.spring.ApplicationContextUtils;
import net.proteusframework.data.filesystem.DirectoryEntity;
import net.proteusframework.data.filesystem.FileSystemDAO;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.QLResolverOptions;
import net.proteusframework.users.model.AuthenticationDomain;
import net.proteusframework.users.model.Contact;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.PrincipalStatus;
import net.proteusframework.users.model.dao.AuthenticationDomainList;
import net.proteusframework.users.model.dao.PrincipalDAO;

import static com.example.app.support.AppUtil.getExtensionWithDot;
import static net.proteusframework.core.StringFactory.isEmptyString;

/**
 * DAO for {@link User}
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@SuppressWarnings("unused")
@Repository
@Lazy
public class UserDAO extends DAOHelper implements Serializable
{

    /** the folder to store User pictures within */
    public static final String USER_PICTURE_FOLDER = "UserPictures";
    /** the user picture file name suffix */
    public static final String USER_PICTURE_FILE_NAME_SUFFIX = "_img";
    /** The alias used by {@link #getUserQLBuilder()} */
    public static final String ALIAS = "userAlias";
    private static final Logger _logger = LogManager.getLogger(UserDAO.class);
    private static final long serialVersionUID = 8994091041309435246L;
    /** entity retriever */
    @Autowired
    protected transient EntityRetriever _er;
    /** cms frontend dao */
    @Autowired
    protected transient CmsFrontendDAO _cmsFrontendDAO;
    /** file system dao */
    @Autowired
    protected transient FileSystemDAO _fileSystemDAO;
    /** app util */
    @Autowired
    protected transient AppUtil _appUtil;
    /** principal dao */
    @Autowired
    protected transient PrincipalDAO _principalDAO;
    private FileSaver<User> _userImageSaver;

    /**
     * Delete the given user from the database
     *
     * @param user the user to delete
     */
    public void deleteUser(User user)
    {
        doInTransaction(session -> {
            session.delete(user);
        });
    }

    /**
     * Delete the given UserPosition from the database
     *
     * @param userPosition the user position to delete
     */
    public void deleteUserPosition(UserPosition userPosition)
    {
        if (userPosition.isCurrent())
        {
            doInTransaction(session -> {
                userPosition.setCurrent(false);
                session.merge(userPosition);
            });
        }
        doInTransaction(session -> {
            session.delete(userPosition);
        });
    }

    /**
     * Get a User from {@link #getCurrentUser()}
     * Does an assertion on the result to verify that the current user is not null
     *
     * @return a User.
     */
    @Nonnull
    public User getAssertedCurrentUser()
    {
        return Optional.ofNullable(getCurrentUser()).orElseThrow(() -> new IllegalArgumentException("Current User was null."));
    }

    /**
     * Get a User for the current Principal returned from {@link PrincipalDAO#getCurrentPrincipal()}
     *
     * @return a User, or null if no User exists for the current Principal
     */
    @Nullable
    public User getCurrentUser()
    {
        return getUserForPrincipal(_principalDAO.getCurrentPrincipal());
    }

    /**
     * Get a list of AuthenticationDomains to save on the given User Principal.
     * This does not actually set them on the Principal, but rather returns a list to be set elsewhere.
     *
     * @param user the User to retrieve the existing AuthenticationDomains from
     *
     * @return a list of AuthenticationDomains, including the AuthenticationDomain from
     * {@link CmsFrontendDAO#getOperationalSite()}
     */
    public List<AuthenticationDomain> getAuthenticationDomainsToSaveOnUserPrincipal(@Nullable User user)
    {
        List<AuthenticationDomain> authDomains = new ArrayList<>();
        if (user != null && !isTransient(user))
        {
            user = _er.reattachIfNecessary(user);
            authDomains.addAll(_er.reattachIfNecessary(user.getPrincipal()).getAuthenticationDomains());
        }
        AuthenticationDomain authDomain = _cmsFrontendDAO.getOperationalSite().getDomain();
        if (!authDomains.contains(authDomain))
            authDomains.add(authDomain);

        return authDomains;
    }

    /**
     * Gets users by email address.
     *
     * @param emailAddress the email address
     * @param domainList authentication domain list.
     * @return the users by email address.
     */
    @SuppressWarnings("unchecked")
    public List<User> getUsersByEmailAddress(String emailAddress, AuthenticationDomainList domainList)
    {
        return (List<User>)
            doInTransaction(session -> {
                @Language("HQL")
                String hql = "SELECT DISTINCT u FROM User u INNER JOIN u.principal p\n"
                             + "INNER JOIN p.credentials cred\n"
                             + "INNER JOIN p.authenticationDomains ad\n"
                             + "INNER JOIN p.contact c INNER JOIN c.emailAddresses ea\n"
                             + "WHERE (LOWER(ea.email) = LOWER(:email) OR LOWER(cred.username) = LOWER(:email))\n";
                if(!domainList.isEmpty())
                {
                    hql += " AND ad IN (:authDomains)";
                }

                Query query = session.createQuery(hql)
                    .setParameter("email", emailAddress);
                if(!domainList.isEmpty())
                {
                    query.setParameterList("authDomains", domainList.getAuthenticationDomainList());
                }

                return query.list();
            });
    }

    /**
     * Get a user by login.
     *
     * @param emailAddressOrUsername the login.
     * @param status optional status to limit.
     *
     * @return the user.
     */
    public Optional<User> getUserByLogin(String emailAddressOrUsername, @Nullable PrincipalStatus... status)
    {
        if (isEmptyString(emailAddressOrUsername))
            return Optional.empty();
        final Principal login = _principalDAO.getPrincipalByLogin(emailAddressOrUsername, null);
        return Optional.ofNullable(getUserForPrincipal(login, status));
    }

    /**
     * Get a User for the given Principal.
     *
     * @param principal the Principal to search for
     * @param status optional status to limit.
     *
     * @return a User, or null if none exists for the given Principal
     */
    @Nullable
    public User getUserForPrincipal(@Nullable Principal principal, @Nullable PrincipalStatus... status)
    {
        if (principal == null) return null;
        String queryString = "SELECT u FROM User u INNER JOIN u.principal p WHERE p = :principal";
        final boolean hasStatus = status != null && status.length > 0;
        if (hasStatus)
            queryString += " AND p.status IN (:status)";
        final Query query = getSession().createQuery(queryString);
        query.setCacheable(true);
        query.setCacheRegion(ProjectCacheRegions.MEMBER_QUERY);
        query.setParameter("principal", principal);
        if (hasStatus)
            query.setParameterList("status", status);
        return (User) query.uniqueResult();
    }

    /**
     * Get a QLBuilder instance for User
     *
     * @return a QLBuilder
     */
    public QLBuilder getUserQLBuilder()
    {
        return getUserQLBuilder(null);
    }

    /**
     * Get a QLBuilder instance for User
     *
     * @param options the QLResolverOptions for this QLBuilder
     *
     * @return a QLBuilder
     */
    public QLBuilder getUserQLBuilder(@Nullable QLResolverOptions options)
    {
        return new QLBuilderImpl(User.class, "userAlias")
            .setQLResolverOptions(options);
    }

    /**
     * Get users matching the specified parameters.
     * All parameters are optional. If none are specified, an empty list is returned.
     *
     * @param firstName first name.
     * @param lastName last name.
     * @param email email address.
     * @param exclude optional exclusion collection.
     *
     * @return the user list.
     */
    public List<User> getUsers(@Nullable String firstName, @Nullable String lastName, @Nullable String email,
        @Nullable Collection<User> exclude)
    {
        boolean hasFirst = !isEmptyString(firstName);
        boolean hasLast = !isEmptyString(lastName);
        boolean hasEmail = !isEmptyString(email);
        boolean hasExclude = exclude != null && !exclude.isEmpty();
        if (!hasFirst && !hasLast && !hasEmail)
            return new ArrayList<>();
        StringBuilder hql = new StringBuilder();
        hql.append("SELECT DISTINCT user FROM User user \n"
                   + " INNER JOIN user.principal as p\n"
                   + " LEFT JOIN p.contact as c \n");
        if (hasFirst || hasLast)
        {
            hql.append(" LEFT JOIN c.name as n\n");
        }
        if (hasEmail)
        {
            hql.append(" LEFT JOIN c.emailAddresses as ea\n");
        }
        hql.append(" WHERE (");
        if (hasFirst)
        {
            hql.append(" LOWER(n.first) LIKE :firstName");
        }
        if (hasLast)
        {
            if (hasFirst) hql.append(" OR");
            hql.append(" LOWER(n.last) LIKE :lastName");
        }
        if (hasEmail)
        {
            if (hasFirst || hasLast) hql.append(" OR");
            hql.append(" LOWER(ea.email) LIKE :email");
        }
        hql.append(')');

        if (hasExclude)
        {
            hql.append(" AND user NOT IN (:exclude)");
        }

        final Session session = getSession();
        final Query query = session.createQuery(hql.toString());
        query.setCacheable(true);
        query.setCacheRegion(ProjectCacheRegions.MEMBER_QUERY);

        if (hasFirst) query.setParameter("firstName", '%' + firstName.trim().toLowerCase() + '%');
        if (hasLast) query.setParameter("lastName", '%' + lastName.trim().toLowerCase() + '%');
        if (hasEmail) query.setParameter("email", '%' + email.trim().toLowerCase() + '%');
        if (hasExclude) query.setParameterList("exclude", exclude);

        @SuppressWarnings("unchecked")
        final List<User> list = query.list();
        return list;
    }

    /**
     * Persist the given User in the database via a merge operation
     *
     * @param user the user to merge
     *
     * @return the persisted User
     */
    public User mergeUser(User user)
    {
        return doInTransaction(session -> {
            if (user.getPrincipal().getContact() != null)
                user.getPrincipal().setContact((Contact) session.merge(user.getPrincipal().getContact()));
            return (User) session.merge(user);
        });
    }

    /**
     * Persist the given UserPosition in the database.  Ensures that if the UserPosition to save is set to current, there are
     * no other current UserPositions for the Position's User
     *
     * @param userPosition the user position to save
     *
     * @return the persisted UserPosition
     */
    public UserPosition mergeUserPosition(UserPosition userPosition)
    {
        Optional<UserPosition> currentPosition = getCurrentUserPosition(userPosition.getUser());
        if (userPosition.isCurrent() && currentPosition.isPresent() && !Objects.equals(userPosition, currentPosition.get()))
        {
            doInTransaction(session -> {
                currentPosition.get().setCurrent(false);
                session.merge(currentPosition.get());
            });
        }
        return doInTransaction(session -> (UserPosition) session.merge(userPosition));
    }

    /**
     * Get the given User's current UserPosition based on the absence of an endDate
     *
     * @param user the User to use for getting the UserPosition
     *
     * @return an optional that could contains the User's current UserPosition, if there is one.
     */
    @Nonnull
    public Optional<UserPosition> getCurrentUserPosition(@Nonnull User user)
    {
        user = _er.reattachIfNecessary(user);
        return Optional.ofNullable((UserPosition) getSession().createQuery("select position\n"
                                                                           + "from UserPosition position\n"
                                                                           + "where position._user=:user\n"
                                                                           + "and position._current=true\n")
            .setParameter("user", user)
            .setCacheable(true).setCacheRegion(ProjectCacheRegions.MEMBER_QUERY)
            .uniqueResult());
    }

    /**
     * Set the {@link User} image and save the user before returning the updated User instance.
     * Ensures that the user is persisted (has an id greater than 0) within the database before setting the image
     * on the User, as the User id is used to determine file location of the image.
     *
     * @param user the user to set the image on and save
     * @param image the fileItem that is the image to save
     *
     * @return the updated and recently saved User
     */
    @Nonnull
    public User saveUserImage(@Nonnull User user, @Nullable FileItem image)
    {
        return getUserImageSaver().save(user, image);
    }

    /**
     * Get the User Image Saver
     *
     * @return the User Image Saver
     */
    @Nonnull
    protected FileSaver<User> getUserImageSaver()
    {
        if (_userImageSaver == null)
        {
            _userImageSaver = new FileSaver<>((user, image) -> {
                user.setImage(image);
                return user;
            }, User::getImage, (user, image) -> {
                // FUTURE : fix this so it doesn't save so many items into a single directory.
                final DirectoryEntity directory = FileSystemDirectory.Pictures.getDirectory2(
                    _cmsFrontendDAO.getOperationalSite());
                return _fileSystemDAO.mkdirs(directory, null, USER_PICTURE_FOLDER);
            }, (user, image) -> {
                String fileNameSuffix = USER_PICTURE_FILE_NAME_SUFFIX + getExtensionWithDot(image);
                return (user.getId() > 0L ? user.getId() : UUID.randomUUID()) + fileNameSuffix;
            }, this::mergeUser);
        }
        return _userImageSaver;
    }

    private Object readResolve() throws ObjectStreamException
    {
        ApplicationContext context = ApplicationContextUtils.getInstance().getContext();
        assert context != null;
        return context.getBean(UserDAO.class);
    }

    @Contract(pure = true)
    private Object writeReplace() throws ObjectStreamException
    {
        return this;
    }
}
