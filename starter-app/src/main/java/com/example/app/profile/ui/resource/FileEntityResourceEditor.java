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

package com.example.app.profile.ui.resource;

import com.example.app.profile.model.resource.FileEntityResource;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;

import com.i2rd.cms.backend.files.FileChooser;
import com.i2rd.cms.backend.files.FileField;

import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.ui.miwt.component.composite.editor.ValueChooserDisplayHandler;
import net.proteusframework.ui.miwt.component.template.FileSystemTemplateDataSource;

/**
 * {@link ResourceValueEditor} for a {@link FileEntityResource}
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@SuppressWarnings("unused")
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.resource.FileEntityResourceEditor",
    i18n = {
        @I18N(symbol = "File Label", l10n = @L10N("File"))
    }
)
@Configurable
public class FileEntityResourceEditor extends ResourceValueEditor<FileEntityResource>
{
    private final FileField _fileField = new FileField();
    private boolean _configured;

    /**
     * Instantiate a new instance
     *
     * @param value the resource for this editor
     */
    public FileEntityResourceEditor(@Nullable FileEntityResource value)
    {
        super(FileEntityResource.class, value,
            new FileSystemTemplateDataSource("profile/resource/FileEntityResourceEditor.xml"));
        _fileField.setShowImage(true);
        _fileField.getDisplay().setDisplayClass("file-field");
        _fileField.addClassName("prop file");
        _fileField.setLabel(FileEntityResourceEditorLOK.FILE_LABEL());
        _fileField.setComponentName("file-property");
    }

    @Override
    public void init()
    {
        if (!_configured)
        {
            throw new IllegalArgumentException("FileEntityResourceEditor has not been configured.  Please make sure you call "
                                               + "setFileChooser on the FileEntityResourceEditor before trying to initialize it.");
        }

        super.init();

        addEditorForProperty(() -> _fileField,
            FileEntityResource.FILE_PROP);

        applyTemplate();
    }

    /**
     * Set the file chooser.
     * Must be called before component initialization.
     *
     * @param fileChooser the file chooser.
     */
    public void setFileChooser(@Nullable final FileChooser<FileEntity> fileChooser)
    {
        _configured = true;
        _fileField.setFileChooser(fileChooser);
    }

    /**
     * Set optional file chooser display handler. A display handler is responsible for
     * determining where in the UI the file chooser is displayed when a user tries to select a file.
     *
     * @param handler optional handler.
     */
    public void setValueChooserDisplayHandler(@Nullable ValueChooserDisplayHandler<FileEntity> handler)
    {
        _fileField.setValueChooserDisplayHandler(handler);
    }
}
