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

package com.example.app.profile.model.resource;

import javax.annotation.Nullable;
import java.io.Serializable;

import com.i2rd.implementationmodel.IImplementationModel;
import com.i2rd.implementationmodel.IInstanceAware;

import net.proteusframework.cms.component.generator.Renderer;
import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;

/**
 * Resource Type interface -- used to determine many factors about a resource, such as render method and how to edit it
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/5/15 1:43 PM
 */
public interface ResourceType extends NamedObject, IImplementationModel<Resource>, IInstanceAware<Resource>, Serializable
{
    /**
     * Create an instance of the {@link ValueEditor} for this resource type
     *
     * @param resource the resource to create the editor for
     *
     * @return an instance of the editor for this resource type
     */
    ValueEditor<? extends Resource> createEditor(@Nullable Resource resource);

    /**
     * Create an instance of the {@link Renderer} for this resource type
     *
     * @param resource the resource to create the renderer for
     *
     * @return an instance of the renderer for this resource type
     */
    Renderer<?> createRenderer(Resource resource);
}
