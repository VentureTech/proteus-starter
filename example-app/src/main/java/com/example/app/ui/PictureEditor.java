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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;

import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.filesystem.TemporaryFileEntity;
import net.proteusframework.data.filesystem.http.FileEntityFileItem;
import net.proteusframework.data.filesystem.http.FileSystemEntityResourceFactory;
import net.proteusframework.internet.http.resource.ClassPathResourceLibraryHelper;
import net.proteusframework.internet.http.resource.FactoryBasedResource;
import net.proteusframework.internet.http.resource.ResourceManager;
import net.proteusframework.ui.miwt.Image;
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
public class PictureEditor extends AbstractSimpleValueEditor<FileEntity>
{
    /** Logger. */
    private final static Logger _logger = Logger.getLogger(PictureEditor.class);

    /** File Field. */
    private final FileField _fileField = new FileField();
    /** File Field. */
    private final ImageComponent _picture = new ImageComponent();
    /** UI Value - UI state isn't held in component. */
    private FileItem _uiValue;
    /** Resource Manager. */
    @Autowired
    private ResourceManager _resourceManager;
    /** FileSystemEntityResourceFactory. */
    @Autowired
    private FileSystemEntityResourceFactory _fileSystemEntityResourceFactory;
    /** Default Profile Picture. */
    private FactoryBasedResource _defaultProfilePicture;

    /**
     * Create an instance.
     */
    public PictureEditor()
    {
        super();
        addClassName("prop");
        _picture.setImageCaching(false);
        _fileField.setAccept("image/*");
        _fileField.addPropertyChangeListener(FileField.PROP_FILE_ITEMS, new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                final List<FileItem> files = (List<FileItem>) evt.getNewValue();
                if(files != null && files.size() > 0)
                {
                    _uiValue = files.get(0);
                    _picture.setImage(new Image(_uiValue));
                }
                _fileField.resetFile();
            }
        });

        final ClassPathResourceLibraryHelper helper = ClassPathResourceLibraryHelper.getInstance();
        _defaultProfilePicture = helper.createResource(helper.createLibrary("default-profile-picture.png"));
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
    }


    @Nullable
    @Override
    public FileEntity getUIValue(Level logErrorLevel)
    {
        if(_uiValue == null)
            return null;
        if(_uiValue instanceof FileEntityFileItem)
            return ((FileEntityFileItem)_uiValue).getFileEntity();
        TemporaryFileEntity tfe = new TemporaryFileEntity();
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
