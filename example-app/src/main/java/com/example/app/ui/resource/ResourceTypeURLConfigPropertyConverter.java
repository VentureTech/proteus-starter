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

import com.example.app.model.resource.ResourceType;
import com.example.app.service.ResourceTypeService;

import javax.annotation.Nullable;
import java.util.Optional;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.spring.ApplicationContextUtils;
import net.proteusframework.ui.management.DefaultURLConfigPropertyConverter;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigPropertyConverter;


/**
 * {@link URLConfigPropertyConverter} implementation for {@link ResourceType}
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@SuppressWarnings("unused")
public class ResourceTypeURLConfigPropertyConverter extends DefaultURLConfigPropertyConverter
{
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <V> V convert(ParsedRequest request, String property, @Nullable String value, Class<V> type)
    {
        if (ResourceType.class.isAssignableFrom(type) && !StringFactory.isEmptyString(value))
        {
            return (V) getResourceTypeService().getResourceType(ResourceTypeService.SPRING_FACTORY_IDENTIFIER, value);
        }
        else return null;
    }

    @Nullable
    @Override
    public <V> String convert(String property, @Nullable V value)
    {
        if (value instanceof ResourceType)
        {
            return ((ResourceType) value).getIdentifier();
        }
        else return null;
    }

    private static ResourceTypeService getResourceTypeService()
    {
        return Optional.ofNullable(ApplicationContextUtils.getInstance().getContext())
            .orElseThrow(() -> new IllegalStateException("ApplicationContext was null."))
            .getBean(ResourceTypeService.class);
    }
}
