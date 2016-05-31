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

import com.example.app.model.resource.Resource;
import com.example.app.model.resource.ResourceType;
import com.example.app.model.resource.URIResource;
import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ObjectStreamException;

import com.i2rd.contentmodel.data.ModelDataString;
import com.i2rd.implementationmodel.IImplementationModel;

import net.proteusframework.cms.component.generator.Renderer;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.spring.ApplicationContextUtils;

import static com.example.app.ui.resource.URIResourceTypeLOK.DESCRIPTION;
import static com.example.app.ui.resource.URIResourceTypeLOK.NAME;

/**
 * {@link ResourceType} implementation for a {@link Resource} that has a {@link ModelDataString} within its resourceData that
 * represents a URI.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/9/15 4:03 PM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.model.resource.URIResourceType",
    i18n = {
        @I18N(symbol = "NAME", l10n = @L10N("Link")),
        @I18N(symbol = "DESCRIPTION", l10n = @L10N("ResourceType for a Resource backed by a URI"))
    }
)
@Component(URIResourceType.IDENTIFIER)
public class URIResourceType implements ResourceType
{

    /** The identifier for this ResourceType */
    static final String IDENTIFIER = "ldp.URIResourceType";
    private static final long serialVersionUID = 5648718450016157570L;

    @Autowired
    private transient EntityRetriever _er;

    @SuppressWarnings("rawtypes")
    @Override
    @Nonnull
    public Renderer createRenderer(Resource resource)
    {
        return new URIResourceRenderer(_er.narrowProxyIfPossible(resource));
    }

    @Override
    @Nonnull
    public ResourceValueEditor<URIResource> createEditor(@Nullable Resource resource)
    {
        return new URIResourceEditor(_er.narrowProxyIfPossible(resource));
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
        URIResource resource = new URIResource();
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

