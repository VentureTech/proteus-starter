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

package com.example.app.support.service;

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
import net.proteusframework.data.filesystem.http.FileEntityFileItem;
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
public class FileSaver<E>
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(FileSaver.class);

    @Autowired
    private FileSystemDAO _fileSystemDAO;

    private final BiFunction<E, FileEntity, E> _setFile;
    private final Function<E, FileEntity> _getFile;
    private final BiFunction<E, FileItem, DirectoryEntity> _getDirectory;
    private final BiFunction<E, FileItem, String> _getFileName;
    private final Function<E, E> _saveValue;

    /**
     *   Instantiate a new instance of FileSaver.
     *   @param setFile function for setting the file on {@link E}
     *   @param getFile function for getting the file from {@link E}
     *   @param getDirectory function for getting a Directory for the given {@link E}.  May return null in the case that
     *   retrieving the directory fails.
     *   @param getFileName function for getting a file name for the given {@link E}
     *   @param saveValue function for saving {@link E}.
     */
    public FileSaver(@Nonnull BiFunction<E, FileEntity, E> setFile, @Nonnull Function<E, FileEntity> getFile,
        @Nonnull BiFunction<E, FileItem, DirectoryEntity> getDirectory, @Nonnull BiFunction<E, FileItem, String> getFileName,
        @Nonnull Function<E, E> saveValue)
    {
        _setFile = setFile;
        _getFile = getFile;
        _getDirectory = getDirectory;
        _getFileName = getFileName;
        _saveValue = saveValue;
    }

    /**
     *   Set the file on {@link E} and saves it.  Returns the persisted instance of {@link E}
     *   @param value the instance of {@link E} to set the file on and save
     *   @param file the Image to save
     *   @return the updated and persisted value
     */
    public E save(@Nonnull E value, @Nullable FileItem file)
    {
        if(file == null)
        {
            value = _setFile.apply(value, null);
        }
        else
        {
            value = _saveValue.apply(value);
            FileEntity currFile = _getFile.apply(value);
            if(currFile == null)
                currFile = new FileEntity();
            String fileName = StringFactory.getBasename(file.getName());
            String ct = file.getContentType();
            if(ct == null || "application/octet-stream".equals(ct)) ct = MimeTypeUtility.getInstance().getContentType(fileName);
            currFile.setContentType(ct);
            final DirectoryEntity directory = _getDirectory.apply(value, file);
            if(directory != null)
            {
                currFile.setName(_getFileName.apply(value, file));

                FileSystemDAO.StoreRequest request = new FileSystemDAO.StoreRequest(
                    directory, currFile, new FileItemByteSource(file));
                request.setCreateMode(FileSystemEntityCreateMode.overwrite);

                currFile = _fileSystemDAO.store(request);
                clearVolatileMetaData(currFile);
            }
            else
            {
                _logger.error("Unable to save File.");
                currFile = _getFile.apply(value);
            }

            value = _setFile.apply(value, currFile);
        }
        value = _saveValue.apply(value);
        return value;
    }

    /**
     * Copy file from the given FileEntity into the value.
     *
     * @param value the value
     * @param file the file
     *
     * @return the e
     */
    public E copyFromEntity(@Nonnull E value, @Nullable FileEntity file)
    {
        if(file == null)
        {
            value = _setFile.apply(value, null);
        }
        else
        {
            value = _saveValue.apply(value);
            FileEntityFileItem fileItem = new FileEntityFileItem(file);
            FileEntity copy = _fileSystemDAO.copy(file, _getDirectory.apply(value, fileItem),
                _getFileName.apply(value, fileItem), null, FileSystemEntityCreateMode.overwrite);
            clearVolatileMetaData(copy);
            value = _setFile.apply(value, copy);
        }
        value = _saveValue.apply(value);
        return value;
    }
}
