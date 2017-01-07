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

package com.example.app.repository.model;



import java.util.HashMap;
import java.util.Map;

import com.i2rd.contentmodel.def.type.DataTypeImpl;
import com.i2rd.contentmodel.def.type.PhysicalType;
import com.i2rd.converter.impl.EntityConverter;
import com.i2rd.data.impl.ClassDataDomain;

import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;

/**
 * RepositoryItem data type.
 *
 * @param <RI> RepositoryItem subclass
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 4/8/16 3:33 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.repository.model.RepositoryItemDataType",
    i18n = {
        @I18N(symbol = "Name", l10n = @L10N("Repository Item"))
    }
)
public class RepositoryItemDataType<RI extends RepositoryItem> extends DataTypeImpl<RI>
{
    private static final Map<Class<? extends RepositoryItem>, RepositoryItemDataType<?>> _instances = new HashMap<>();

    /**
     * Get the data type for the given RepositoryItem subclass
     *
     * @param clazz the RepositoryItem subclass
     * @param <RR> the RepositoryItem subclass
     *
     * @return the Data Type
     */
    @SuppressWarnings("unchecked")
    public synchronized static <RR extends RepositoryItem> RepositoryItemDataType<RR> getDataType(Class<RR> clazz)
    {
        if (_instances.get(clazz) != null)
            return (RepositoryItemDataType<RR>) _instances.get(clazz);
        else
        {
            RepositoryItemDataType<RR> instance = new RepositoryItemDataType<>(clazz);
            _instances.put(clazz, instance);
            return instance;
        }
    }


    /**
     * Instantiates a new data type.
     *
     * @param clazz the RepositoryItem class
     */
    public RepositoryItemDataType(Class<RI> clazz)
    {
        super(
            RepositoryItemDataTypeLOK.NAME(),
            clazz.getSimpleName(),
            PhysicalType.Entity,
            new ClassDataDomain<>(clazz),
            EntityConverter.INSTANCE,
            EntityConverter.INSTANCE
        );
    }
}
