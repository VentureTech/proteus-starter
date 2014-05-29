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

package com.example.app.model;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.cms.FileSystemDirectory;
import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.lang.CloseableIterator;
import net.proteusframework.core.net.ContentTypes;
import net.proteusframework.data.filesystem.DirectoryEntity;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.filesystem.FileSystemDAO;
import net.proteusframework.data.filesystem.FileSystemEntityCreateMode;
import net.proteusframework.data.filesystem.TemporaryFileEntity;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLResolver;
import net.proteusframework.ui.search.QLResolverOptions;

/**
 * DAO for user profile.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@Repository
public class UserProfileDAO extends DAOHelper
{
    /** Logger. */
    private final static Logger _logger = Logger.getLogger(UserProfileDAO.class);

    /** FileSystem DAO. */
    @Autowired
    private FileSystemDAO _fileSystemDAO;

    /**
     * Get the file extension.
     *
     * @param file the file.
     * @return the extension preceeded by a '.' or an empty string.
     */
    private static String _getFileExtensionWithDot(FileEntity file)
    {
        String ext = ""; // Cannot use the file name since the file may have been converted.
        final String contentType = file.getContentType();
        if (ext.isEmpty() && !ContentTypes.Application.octet_stream.toString().equals(contentType))
        {
            try
            {
                ext = new ContentType(contentType).getSubType().toLowerCase();
                switch(ext)
                {
                    case "jpeg":
                        ext = "jpg";
                        break;
                    case "tiff":
                        ext = "tif";
                        break;
                    case "svg+xml":
                        ext = "svg";
                        break;
                    case "x-portable-anymap":
                        ext = "pnm";
                        break;
                    case "x-portable-bitmap":
                        ext = "pbm";
                        break;
                    case "x-portable-graymap":
                        ext = "pgm";
                        break;
                    case "x-portable-pixmap":
                        ext = "ppm";
                        break;
                    default:break;
                }
            }
            catch (ParseException e)
            {
                _logger.error("Unable to parse content type: " + contentType, e);
            }
        }
        return ext.isEmpty() ? ext : '.' + ext;
    }

    /**
     * Save UserProfile.
     *
     * @param userProfile the user profile to save.
     */
    public void saveUserProfile(UserProfile userProfile)
    {
        beginTransaction();
        boolean success = false;
        try
        {
            final long id = userProfile.getId();
            String name = userProfile.getName().getLast() + ", " + userProfile.getName().getFirst();
            String pictureName = name + " #" + id;
            final Session session = getSession();
            FileEntity picture = userProfile.getPicture();
            if (picture != null)
            {
                pictureName += _getFileExtensionWithDot(picture);
                // Ensure our picture file has a unique file name consistent with the profile.
                if (picture.getId() < 1)
                {
                    final CmsSite site = userProfile.getSite();
                    final DirectoryEntity rootDirectory = FileSystemDirectory.getRootDirectory(site);
                    DirectoryEntity parentDirectory = _fileSystemDAO.mkdirs(rootDirectory, null, "UserProfilePictures");
                    picture.setName(pictureName);
                    picture = _fileSystemDAO.newFile(parentDirectory, picture, FileSystemEntityCreateMode.truncate);
                    userProfile.setPicture(picture);
                }
                else if(picture instanceof TemporaryFileEntity)
                {
                    TemporaryFileEntity tfe  = (TemporaryFileEntity) picture;
                    EntityRetriever er = EntityRetriever.getInstance();
                    picture = er.reattachIfNecessary(tfe.getFileEntity());
                    picture.setName(pictureName);
                    _fileSystemDAO.update(picture);
                    _fileSystemDAO.setStream(picture, tfe.getStream(), true);
                    userProfile.setPicture(picture); // In case we are cascading.
                    tfe.deleteStream();
                }
            }

            if (isTransient(userProfile) || isAttached(userProfile))
                session.saveOrUpdate(userProfile);
            else
                session.merge(userProfile);

            if (picture != null && id == 0)
            {
                // New user profile. Update picture name to include the ID
                pictureName = name + " #" + userProfile.getId() + _getFileExtensionWithDot(picture);
                picture.setName(pictureName);
                _fileSystemDAO.update(picture);
            }
            success = true;
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Unable to access filesystem.", ioe);
        }
        finally
        {
            if (success)
                commitTransaction();
            else
                recoverableRollbackTransaction();
        }
    }

    /**
     * Delete the specified user profile.
     *
     * @param userProfile the user profile to delete.
     */
    public void deleteUserProfile(UserProfile userProfile)
    {
        beginTransaction();
        boolean success = false;
        try
        {
            final Session session = getSession();
            session.delete(userProfile);
            success = true;
        }
        finally
        {
            if (success)
                commitTransaction();
            else
                recoverableRollbackTransaction();
        }
    }

    /**
     * Delete the specified user profiles.
     *
     * @param userProfiles the user profiles to delete.
     */
    public void deleteUserProfiles(Collection<? extends UserProfile> userProfiles)
    {
        beginTransaction();
        boolean success = false;
        try
        {
            final Session session = getSession();
            userProfiles.forEach(session::delete);
            success = true;
        }
        finally
        {
            if (success)
                commitTransaction();
            else
                recoverableRollbackTransaction();
        }
    }

    /**
     * Delete the specified user profiles.
     *
     * @param qlBuilder a QL builder that will return UserProfiles to delete.
     */
    public void deleteUserProfiles(QLBuilder qlBuilder)
    {
        beginTransaction();
        boolean success = false;
        try
        {
            final Session session = getSession();
            final QLResolver queryResolver = qlBuilder.getQueryResolver();
            final QLResolverOptions options = new QLResolverOptions();
            final int atATime = 200;
            options.setFetchSize(atATime);
            queryResolver.setOptions(options);
            try (CloseableIterator<UserProfile> it = queryResolver.iterate())
            {
                int count = 0;
                while (it.hasNext())
                {
                    UserProfile userProfile = it.next();
                    session.delete(userProfile);
                    if (++count > atATime)
                    {
                        count = 0;
                        session.flush(); // May need to clear action queues as well to free up memory.
                    }
                }
            }
            catch (Exception e)
            {
                throw new HibernateException("Unable to iterate over query results.", e);
            }
            success = true;
        }
        finally
        {
            if (success)
                commitTransaction();
            else
                recoverableRollbackTransaction();
        }
    }

    /**
     * Evict the specified entity from the session.
     *
     * @param entity the entity.
     */
    public void evict(Object entity)
    {
        getSession().evict(entity);
    }

    /**
     * Convert a URL to a URI returning the default value on error.
     *
     * @param url the URL.
     * @param defaultValue the default value.
     * @return the URI.
     */
    @Contract("_,null->null;_,!null->!null")
    public URI toURI(URL url, URI defaultValue)
    {
        if (url == null) return defaultValue;
        try
        {
            return url.toURI();
        }
        catch (URISyntaxException e)
        {
            _logger.warn("Unable to convert URL to URI: " + url, e);
        }
        return defaultValue;
    }

    /**
     * Convert a URL to a string if possible or return null.
     *
     * @param link the link.
     * @return the link as a String or null.
     */
    @Nullable
    @Contract("null->null;!null->!null")
    public String toString(@Nullable URL link)
    {
        return link == null ? null : link.toString();
    }

}
