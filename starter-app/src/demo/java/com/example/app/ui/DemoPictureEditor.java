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

package com.example.app.ui;

import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.filesystem.TemporaryFileEntity;
import net.proteusframework.data.filesystem.http.FileEntityFileItem;
import net.proteusframework.internet.http.resource.ClassPathResourceLibraryHelper;
import net.proteusframework.internet.http.resource.FactoryResource;
import net.proteusframework.ui.miwt.Image;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.FileField;
import net.proteusframework.ui.miwt.component.ImageComponent;
import net.proteusframework.ui.miwt.component.composite.editor.AbstractSimpleValueEditor;

/**
 * Picture editor.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@Configurable
public class DemoPictureEditor extends AbstractSimpleValueEditor<FileEntity>
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(DemoPictureEditor.class);

    /** File Field. */
    private final FileField _fileField = new FileField();
    /** File Field. */
    private final ImageComponent _picture = new ImageComponent();
    /** UI Value - UI state isn't held in component. */
    private FileItem _uiValue;
    /** Default Profile Picture. */
    private final FactoryResource _defaultProfilePicture;
    /** Preserver File Entity. */
    private boolean _preserveFileEntity;
    /** Modified Flag. */
    private boolean _modified;
    /** service. */
    @Autowired
    private ClassPathResourceLibraryHelper _classPathResourceLibraryHelper;

    /**
     * Create an instance.
     */
    public DemoPictureEditor()
    {
        super();
        addClassName("prop");
        _picture.setImageCaching(false);
        _fileField.setAccept("image/*");
        _fileField.addPropertyChangeListener(FileField.PROP_FILE_ITEMS, evt ->
        {
            @SuppressWarnings("unchecked")
            final List<FileItem> files = (List<FileItem>) evt.getNewValue();
            if(files != null && files.size() > 0)
            {
                _uiValue = files.get(0);
                _picture.setImage(new Image(_uiValue));
                _modified = true;
            }
            _fileField.resetFile();
        });

        _defaultProfilePicture = _classPathResourceLibraryHelper.createResource(
            _classPathResourceLibraryHelper.createLibrary("default-profile-picture.png"));
    }

    /**
     * Test if we should preserve the original file entity.
     * If true, a temporary file entity will be created that references the original file entity.
     * @return true or false.
     */
    public boolean isPreserveFileEntity()
    {
        return _preserveFileEntity;
    }

    /**
     * Set flag to determine if we should preserve the original file entity.
     * If true, a temporary file entity will be created that references the original file entity.
     * @param preserveFileEntity true or false.
     */
    public void setPreserveFileEntity(boolean preserveFileEntity)
    {
        _preserveFileEntity = preserveFileEntity;
    }

    @Override
    public void init()
    {
        super.init();
        // Create drop-zone container in case someone wants to use DnD and Jcrop with this.
        add(of("drop-zone", of("jcrop-wrapper", _picture)));
    }

    @Override
    public Component getValueComponent()
    {
        return _fileField;
    }

    @Override
    public void setUIValue(@Nullable FileEntity value)
    {
        if(value == null)
        {
            _fileField.resetFile();
            _picture.setImage(new Image(_defaultProfilePicture));
            _uiValue = null;
        }
        else
        {
            _uiValue = new FileEntityFileItem(value);
            _picture.setImage(new Image(value));
        }
        _modified = false;
    }

    @Override
    public ModificationState getModificationState()
    {
        return (_modified) ? ModificationState.CHANGED : ModificationState.UNCHANGED;
    }

    @Override
    public FileEntity commitValue() throws MIWTException
    {
        _modified = false;
        return super.commitValue();
    }

    @Nullable
    @Override
    public FileEntity getUIValue(Level logErrorLevel)
    {
        if(_uiValue == null)
            return null;
        if(_uiValue instanceof FileEntityFileItem)
            return ((FileEntityFileItem)_uiValue).getFileEntity();
        TemporaryFileEntity tfe;
        if(isPreserveFileEntity() && getInternalValue() != null)
        {
            final FileEntity fileEntity = EntityRetriever.getInstance().reattachIfNecessary(getInternalValue());
            tfe = new TemporaryFileEntity(fileEntity);
        }
        else
            tfe = new TemporaryFileEntity();
        tfe.setContentType(_uiValue.getContentType());
        tfe.setName(_uiValue.getName());
        try
        {
            tfe.setInputStream(_uiValue.getInputStream());
        }
        catch (IOException e)
        {
            _logger.log(logErrorLevel, "Unable access file data.", e);
            return null;
        }
        return tfe;
    }
}
