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

package com.example.app.ui.resource;

import com.example.app.model.resource.FileEntityResource;
import com.example.app.model.resource.Resource;
import com.example.app.model.resource.ResourceType;
import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ObjectStreamException;

import com.i2rd.cms.backend.files.FileChooser;
import com.i2rd.contentmodel.data.ModelDataFileSystemEntity;
import com.i2rd.implementationmodel.IImplementationModel;

import net.proteusframework.cms.component.generator.Renderer;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.spring.ApplicationContextUtils;

import static com.example.app.ui.resource.FileEntityResourceTypeLOK.DESCRIPTION;
import static com.example.app.ui.resource.FileEntityResourceTypeLOK.NAME;

/**
 * {@link ResourceType} implementation for a {@link Resource} that has a {@link ModelDataFileSystemEntity} within its resourceData
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/9/15 3:26 PM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.model.resource.FileEntityResourceType",
    i18n = {
        @I18N(symbol = "NAME", l10n = @L10N("File")),
        @I18N(symbol = "DESCRIPTION", l10n = @L10N("ResourceType for a Resource backed by a File"))
    }
)
@Component(FileEntityResourceType.IDENTIFIER)
public class FileEntityResourceType implements ResourceType
{

    /** The identifier for this ResourceType */
    static final String IDENTIFIER = "ldp.FileEntityResourceType";
    private static final long serialVersionUID = 7587224767233169020L;

    @Autowired
    private transient EntityRetriever _er;

    @Override
    @Nonnull
    public Renderer<?> createRenderer(Resource resource)
    {
        return new FileEntityResourceRenderer(_er.narrowProxyIfPossible(resource));
    }

    /**
     *   {@inheritDoc}
     *   <br><br>
     *   Before attempting to add this editor into a UI, the editor must be configured by calling
     *   {@link FileEntityResourceEditor#setFileChooser(FileChooser)}.
     */
    @Override
    @Nonnull
    public ResourceValueEditor<FileEntityResource> createEditor(@Nullable Resource resource)
    {
        return new FileEntityResourceEditor(_er.narrowProxyIfPossible(resource));
    }

    @Override
    public String getIdentifier()
    {
        return IDENTIFIER;
    }

    @Nonnull
    @Override
    public TextSource getName()
    {
        return NAME();
    }

    @Nullable
    @Override
    public TextSource getDescription()
    {
        return DESCRIPTION();
    }

    @Override
    public Resource createInstance(IImplementationModel<Resource> model)
    {
        FileEntityResource resource = new FileEntityResource();
        resource.setResourceType(this);
        return resource;
    }

    private Object readResolve() throws ObjectStreamException
    {
        ApplicationContext context = ApplicationContextUtils.getInstance().getContext();
        assert context != null;
        return context.getBean(IDENTIFIER);
    }

    @Contract(pure = true)
    private Object writeReplace() throws ObjectStreamException
    {
        return this;
    }
}

