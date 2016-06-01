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

package com.example.app.ui.repository;


import com.example.app.model.repository.RepositoryDAO;
import com.example.app.model.repository.ResourceRepositoryItem;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.spring.ApplicationContextUtils;
import net.proteusframework.ui.management.DefaultURLConfigPropertyConverter;
import net.proteusframework.ui.management.ParsedRequest;

/**
 * URL Property Resolver used for mapping a list of ResourceRepositoryItems to and from a string.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 4/12/16 10:24 AM
 */
public class ResourceRepositoryItemListURLPropertyResolver extends DefaultURLConfigPropertyConverter
{
    private static final String SEPARATOR = ",";
    private static final String SEPARATOR_ENCODED = "%2C";

    @SuppressWarnings({"unchecked", "DynamicRegexReplaceableByCompiledPattern"})
    @Nullable
    @Override
    public <V> V convert(ParsedRequest parsedRequest, String property, @Nullable String value, Class<V> type)
    {
        if (List.class.isAssignableFrom(type) && !StringFactory.isEmptyString(value))
        {
            value = value.replace("%5B", "").replace("%5D", "");
            String[] values = value.split(SEPARATOR_ENCODED);
            List<ResourceRepositoryItem> resources = Arrays.asList(values).stream()
                .map(Integer::valueOf)
                .map(id -> getRepositoryDAO().getRepositoryItem(ResourceRepositoryItem.class, id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
            return (V) resources;
        }
        else return null;
    }

    @Nullable
    @Override
    public <V> String convert(String property, V value)
    {
        if (value != null)
        {
            @SuppressWarnings("unchecked")
            List<ResourceRepositoryItem> resources = (List<ResourceRepositoryItem>) value;
            StringBuilder str = new StringBuilder();
            str.append('[');
            AtomicReference<Integer> counter = new AtomicReference<>(0);
            resources.stream()
                .map(ResourceRepositoryItem::getId)
                .forEach(id -> {
                    str.append(id);
                    if (counter.getAndAccumulate(1, (i1, i2) -> i1 + i2) < resources.size() - 1)
                        str.append(SEPARATOR);
                });
            str.append(']');
            return str.toString();
        }
        else return null;
    }

    @Override
    public String createRegexPattern(Class<?> propertyType, @Nullable Character nextCharacter)
    {
        return '(' + "\\d+(?:,\\d+)*" + ')';
    }

    private static RepositoryDAO getRepositoryDAO()
    {
        return Optional.ofNullable(ApplicationContextUtils.getInstance().getContext())
            .orElseThrow(() -> new IllegalStateException("ApplicationContext was null"))
            .getBean(RepositoryDAO.class);
    }
}
