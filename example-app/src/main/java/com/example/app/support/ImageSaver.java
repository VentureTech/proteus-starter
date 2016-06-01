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

package com.example.app.support;

import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.mail.support.MimeTypeUtility;
import net.proteusframework.data.filesystem.DirectoryEntity;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.filesystem.FileSystemDAO;
import net.proteusframework.data.filesystem.FileSystemEntityCreateMode;
import net.proteusframework.data.filesystem.http.FileItemByteSource;

import static com.i2rd.cms.backend.FileMetaData.clearVolatileMetaData;

/**
 * Utility class for defining a method of saving an image on a given Entity.
 *
 * @param <E> the Entity whose image is to be saved
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/5/16 8:59 AM
 */
@Configurable
public class ImageSaver<E>
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ImageSaver.class);
    private final BiFunction<E, FileEntity, E> _setImage;
    private final Function<E, FileEntity> _getImage;
    private final BiFunction<E, FileItem, DirectoryEntity> _getDirectory;
    private final BiFunction<E, FileItem, String> _getFileName;
    private final Function<E, E> _saveValue;
    @Autowired
    private FileSystemDAO _fileSystemDAO;

    /**
     * Instantiate a new instance of ImageSaver.
     *
     * @param setImage function for setting the image on {@link E}
     * @param getImage function for getting the image from {@link E}
     * @param getDirectory function for getting a Directory for the given {@link E}.  May return null in the case that
     * retrieving the directory fails.
     * @param getFileName function for getting a file name for the given {@link E}
     * @param saveValue function for saving {@link E}.
     */
    public ImageSaver(@Nonnull BiFunction<E, FileEntity, E> setImage, @Nonnull Function<E, FileEntity> getImage,
        @Nonnull BiFunction<E, FileItem, DirectoryEntity> getDirectory, @Nonnull BiFunction<E, FileItem, String> getFileName,
        @Nonnull Function<E, E> saveValue)
    {
        _setImage = setImage;
        _getImage = getImage;
        _getDirectory = getDirectory;
        _getFileName = getFileName;
        _saveValue = saveValue;
    }

    /**
     * Set the image on {@link E} and saves it.  Returns the persisted instance of {@link E}
     *
     * @param value the instance of {@link E} to set the image on and save
     * @param image the Image to save
     *
     * @return the updated and persisted value
     */
    public E saveImage(@Nonnull E value, @Nullable FileItem image)
    {
        if (image == null)
        {
            value = _setImage.apply(value, null);
        }
        else
        {
            value = _saveValue.apply(value);
            FileEntity file = _getImage.apply(value);
            if (file == null)
                file = new FileEntity();
            String fileName = StringFactory.getBasename(image.getName());
            String ct = image.getContentType();
            if (ct == null || "application/octet-stream".equals(ct)) ct = MimeTypeUtility.getInstance().getContentType(fileName);
            file.setContentType(ct);
            final DirectoryEntity directory = _getDirectory.apply(value, image);
            if (directory != null)
            {
                file.setName(_getFileName.apply(value, image));

                FileSystemDAO.StoreRequest request = new FileSystemDAO.StoreRequest(
                    directory, file, new FileItemByteSource(image));
                request.setCreateMode(FileSystemEntityCreateMode.overwrite);

                file = _fileSystemDAO.store(request);
                clearVolatileMetaData(file);
            }
            else
            {
                _logger.error("Unable to save Image.");
                file = _getImage.apply(value);
            }

            value = _setImage.apply(value, file);
        }
        value = _saveValue.apply(value);
        return value;
    }
}
