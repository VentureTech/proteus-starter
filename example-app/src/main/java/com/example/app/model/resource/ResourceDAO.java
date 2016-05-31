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

import com.example.app.support.ImageSaver;
import com.example.app.support.LRLabsUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.proteusframework.cms.FileSystemDirectory;
import net.proteusframework.cms.dao.CmsFrontendDAO;
import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.data.filesystem.DirectoryEntity;
import net.proteusframework.data.filesystem.FileSystemDAO;

/**
 * DAO for {@link Resource}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/9/15 4:12 PM
 */
@Repository
@Lazy
public class ResourceDAO extends DAOHelper
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ResourceDAO.class);

    /** The folder to store Resource Pictures within */
    public static final String RESOURCE_PICTURE_FOLDER = "ResourcePictures";
    /** The Resource Picture file name suffix. */
    public static final String RESOURCE_PICTURE_FILE_NAME_SUFFIX = "_img";

    @Autowired
    private CmsFrontendDAO _cmsFrontendDAO;
    @Autowired
    private FileSystemDAO _fileSystemDAO;
    @Autowired
    private LRLabsUtil _appUtil;

    private ImageSaver<Resource> _resourceImageSaver;

    /**
     *   Get the Resource Image Saver
     *   @return the Resource Image Saver
     */
    @SuppressWarnings("ConstantConditions")
    @Nonnull
    protected ImageSaver<Resource> getResourceImageSaver()
    {
        if(_resourceImageSaver == null)
        {
            _resourceImageSaver = new ImageSaver<>((resource, image) -> {
                resource.setImage(image);
                return resource;
            }, Resource::getImage, (resource, image) -> {
                final DirectoryEntity directory = FileSystemDirectory.Pictures.getDirectory2(_cmsFrontendDAO
                    .getOperationalSite());
                return _fileSystemDAO.mkdirs(directory, null, RESOURCE_PICTURE_FOLDER, String.valueOf(resource.getId()));
            }, (resource, image) -> {
                String fileNameSuffix = RESOURCE_PICTURE_FILE_NAME_SUFFIX + _appUtil.getExtensionWithDot(image);
                return resource.getId() > 0
                    ? resource.getId() + fileNameSuffix
                    : UUID.randomUUID().toString() + fileNameSuffix;
            }, this::mergeResource);
        }
        return _resourceImageSaver;
    }

    /**
     *   Save the given {@link Resource} into the database via a merge
     *   @param resource the resource to save
     *   @return the persisted Resource
     */
    public Resource mergeResource(Resource resource)
    {
        return doInTransaction(session -> (Resource)session.merge(resource));
    }

    /**
     *   Delete the given {@link Resource} from the database.
     *   @param resource the resource to remove from the database
     */
    public void deleteResource(Resource resource)
    {
        doInTransaction(session -> {
            session.delete(resource);
        });
    }

    /**
     * Get all the resources of a specific type.
     *
     * @param <T> The type of resource
     * @param resultType The type of resource
     * @return the resources, sorted by create date
     */
    @SuppressWarnings("unchecked")
    public <T extends Resource> List<T> getResources(@NotNull Class<T> resultType)
    {
        @SuppressWarnings("StringBufferReplaceableByString")
        StringBuilder hql = new StringBuilder("from ");
        hql.append(resultType.getSimpleName());
        hql.append(" as r order by r.createTime");

        final Query q = getSession().createQuery(hql.toString());
        // Not an expensive query and not called frequently
        q.setCacheable(false);

        return q.list();
    }

    /**
     *   Set the {@link Resource} image and save hte Resource before returning the updated Resource instance.
     *   Ensures that the Resource is persisted within the database before setting the image on the Resource, as the Resource ID
     *   is used to determine file location of the persisted image.
     *
     *   @param resource the resource to set the image on and save
     *   @param image the fileItem that is the image to save
     *   @return the updated and recently saved resource
     */
    @Nonnull
    public Resource saveResourceImage(@Nonnull Resource resource, @Nullable FileItem image)
    {
        return getResourceImageSaver().saveImage(resource, image);
    }

    /**
     *   Get a Resource of the given Class for the given Id
     *   @param <R> the resource type
     *   @param clazz the Resource class
     *   @param id the ID
     *   @return the resource
     */
    @SuppressWarnings("unchecked")
    public <R extends Resource> Optional<R> getResource(Class<R> clazz, Integer id)
    {
        return Optional.ofNullable((R)getSession().get(clazz, id));
    }
}
