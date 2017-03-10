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

package com.example.app.profile.ui.repository;

import com.example.app.profile.model.repository.Repository;
import com.example.app.profile.model.repository.ResourceRepositoryItem;
import com.example.app.profile.model.resource.Resource;
import com.example.app.profile.model.resource.ResourceType;
import com.example.app.profile.ui.resource.FileEntityResourceEditor;
import com.example.app.profile.ui.resource.ResourceValueEditor;
import com.example.app.support.service.AppUtil;
import com.example.app.support.ui.CommonEditorFields;
import com.example.app.support.ui.vtcrop.VTCropPictureEditor;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

import com.i2rd.cms.backend.files.FileChooser;

import net.proteusframework.cms.FileSystemDirectory;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.data.filesystem.DirectoryEntity;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.filesystem.FileSystemDAO;
import net.proteusframework.data.filesystem.FileSystemEntity;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TemplateCompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.component.template.FileSystemTemplateDataSource;

/**
 * {@link CompositeValueEditor} for {@link ResourceRepositoryItem}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/11/15 10:11 AM
 */
@Configurable
public class ResourceRepositoryItemValueEditor extends TemplateCompositeValueEditor<ResourceRepositoryItem>
{

    @Autowired
    EntityRetriever entityRetriever;
    @Autowired
    private FileSystemDAO _fileSystemDAO;
    @Autowired
    private AppUtil _appUtil;

    private ResourceType _resourceType;
    private Repository _owner;

    private ValueEditor<? extends Resource> _resourceValueEditor;

    /**
     * Instantiate a new instance of ResourceRepositoryItemValueEditor
     */
    public ResourceRepositoryItemValueEditor()
    {
        super(ResourceRepositoryItem.class,
            new FileSystemTemplateDataSource("profile/repository/ResourceValueEditor.xml"));
        setComponentName("ResourceValueEditor");
    }

    /**
     * Get the picture editor for the Resource value editor
     *
     * @return the picture editor
     */
    @Nullable
    public VTCropPictureEditor getPictureEditor()
    {
        if (getResourceValueEditor() instanceof ResourceValueEditor)

        {
            ResourceValueEditor<?> resourceValueEditor = (ResourceValueEditor) getResourceValueEditor();
            return resourceValueEditor.getPictureEditor();
        }
        return null;
    }

    private ValueEditor<? extends Resource> getResourceValueEditor()
    {
        if (_resourceValueEditor == null)
        {
            ValueEditor<? extends Resource> editor = getResourceType().createEditor(null);
            if (editor instanceof FileEntityResourceEditor)
            {
                FileEntityResourceEditor castEditor = (FileEntityResourceEditor) editor;
                final DirectoryEntity root = FileSystemDirectory.Documents.getDirectory2(_appUtil.getSite());

                Integer ownerId = getOwner().getId();
                DirectoryEntity repoRoot = _fileSystemDAO.mkdirs(root, null, "repository", ownerId.toString());

                FileChooser<FileEntity> fileChooser = new FileChooser<>(repoRoot);
                fileChooser.setFilePathRenderer(new FileChooser.FilePathRenderer()
                {
                    final String ownerIdString = ownerId.toString();

                    @Override
                    public String getPath(FileSystemEntity fileSystemEntity, FileSystemEntity relativeTo)
                    {
                        FileSystemEntity ownerFSE = fileSystemEntity;
                        while (!Objects.equals(ownerFSE.getName(), ownerIdString) && !_fileSystemDAO.isRoot(ownerFSE))
                            ownerFSE = ownerFSE.getParent();
                        String path = fileSystemEntity.getPath(ownerFSE);
                        if (!_fileSystemDAO.isRoot(ownerFSE))
                        {
                            Repository owner = EntityRetriever.getInstance().reattachIfNecessary(getOwner());
                            path = owner.getName().getText(getLocaleContext()) + "/" + path;
                        }
                        return path;
                    }

                    @Override
                    public String getPathComponent(FileSystemEntity fileSystemEntity)
                    {
                        final String name = fileSystemEntity.getName();
                        if (Objects.equals(name, ownerIdString))
                        {
                            Repository owner = EntityRetriever.getInstance().reattachIfNecessary(getOwner());
                            return owner.getName().getText(getLocaleContext()).toString();
                        }
                        return name;
                    }
                });
                castEditor.setFileChooser(fileChooser);
                _resourceValueEditor = castEditor;
            }
            else
            {
                _resourceValueEditor = editor;
            }
            _resourceValueEditor.withComponentName("resource-value-editor");
        }
        return _resourceValueEditor;
    }

    /**
     * Get the Owner Repository for this ResourceRepositoryItemValueEditor
     *
     * @return the Owner Repository
     */
    @Nonnull
    public Repository getOwner()
    {
        return Optional.ofNullable(_owner).orElseThrow(() -> new IllegalStateException("Owner has not been set!"));
    }

    /**
     * Set the Owner Repository for this ResourceRepositoryItemValueEditor
     *
     * @param owner the Owner Repository
     */
    public void setOwner(@Nonnull Repository owner)
    {
        _owner = owner;
    }

    @Nullable
    @Override
    public ResourceRepositoryItem getUIValue(Level logErrorLevel)
    {
        ResourceRepositoryItem result = super.getUIValue(logErrorLevel);
        if (result != null)
        {
            result.getResource().setResourceType(getResourceType());
        }
        return result;
    }

    @Nullable
    @Override
    public ResourceRepositoryItem commitValue() throws MIWTException
    {
        ResourceRepositoryItem result = super.commitValue();
        if (result != null)
        {
            result.getResource().setResourceType(getResourceType());
        }
        return result;
    }

    /**
     * Get the ResourceType for this ResourceRepositoryItemValueEditor
     *
     * @return the ResourceType.  This MUST be set before retrieving it.
     */
    @Nonnull
    public ResourceType getResourceType()
    {
        return Optional.ofNullable(_resourceType).orElseThrow(() -> new IllegalStateException("ResourceType has not been set!"));
    }

    /**
     * Set the ResourceType for this ResourceRepositoryItemValueEditor
     *
     * @param resourceType the ResourceType.  This MUST be set before retrieving it.
     */
    public void setResourceType(@Nonnull ResourceType resourceType)
    {
        _resourceType = resourceType;

        setNewInstanceSupplier(() -> {
            ResourceRepositoryItem newInstance = new ResourceRepositoryItem();
            newInstance.setResource(resourceType.createInstance(resourceType));
            return newInstance;
        });
    }

    @Override
    public void init()
    {
        super.init();

        addEditorForProperty(
            this::getResourceValueEditor,
            readValue -> entityRetriever.narrowProxyIfPossible(readValue.getResource()),
            (writeValue, value) -> writeValue.setResource(entityRetriever.narrowProxyIfPossible(value)));

        CommonEditorFields.addRepositoryItemSourceEditor(this);
        CommonEditorFields.addRepositoryItemStatusEditor(this);

        applyTemplate();
    }
}
