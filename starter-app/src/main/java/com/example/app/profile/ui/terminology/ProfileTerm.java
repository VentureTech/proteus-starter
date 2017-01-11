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

package com.example.app.profile.ui.terminology;

import com.example.app.profile.model.terminology.ProfileTermProvider;
import com.example.app.profile.model.terminology.ProfileTerms;
import com.google.common.base.MoreObjects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.proteusframework.core.locale.LocaleSource;
import net.proteusframework.core.locale.LocaleSourceException;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TransientLocalizedObjectKey;

/**
 * A single term from ProfileTerms.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public final class ProfileTerm
{
    @Nonnull
    private final PropertyDescriptor _termProperty;
    @Nonnull
    private final Method _defaultTermAccessor;

    /**
     * Populate term map.
     *
     * @param terms the terms.
     * @param map the map.
     * @param localeSource  the locale source.
     */
    public static void populateTermMap(ProfileTerms terms, LinkedHashMap<ProfileTerm, TransientLocalizedObjectKey> map,
        LocaleSource localeSource)
    {
        Map<String, PropertyDescriptor> pdMap = new HashMap<>();
        List<Method> methodList = new ArrayList<>();
        Collections.addAll(methodList, ProfileTermProvider.class.getDeclaredMethods());
        methodList.sort((m1, m2) -> m1.getName().compareToIgnoreCase(m2.getName()));
        try
        {
            BeanInfo beanInfo = Introspector.getBeanInfo(ProfileTerms.class);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors())
                pdMap.put(pd.getName(), pd);
        }
        catch (IntrospectionException e)
        {
            throw new IllegalStateException("Unable to get bean info.", e);
        }
        methodList.forEach(method -> {
            PropertyDescriptor pd = pdMap.get(method.getName());
            assert pd != null;
            ProfileTerm pt = new ProfileTerm(pd, method);
            LocalizedObjectKey term = pt.getTerm(terms);
            TransientLocalizedObjectKey key;
            try
            {
                key = term == null
                    ? new TransientLocalizedObjectKey(null)
                    : TransientLocalizedObjectKey.getTransientLocalizedObjectKey(localeSource, term);
            }
            catch (LocaleSourceException e)
            {
                throw new IllegalStateException("Unable to get term data.", e);
            }
            map.put(pt, key);
        });
    }


    /**
     * Instantiates a new Profile term.
     *
     * @param termProperty the term property. Example: {@link ProfileTerms#getCompany()}
     * @param defaultTermAccessor the default term accessor. Example: {@link ProfileTermProvider#company()}.
     */
    public ProfileTerm(@Nonnull PropertyDescriptor termProperty, @Nonnull Method defaultTermAccessor)
    {
        _termProperty = termProperty;
        _defaultTermAccessor = defaultTermAccessor;
    }

    /**
     * Gets term property.
     *
     * @return the term property
     */
    @Nonnull
    public PropertyDescriptor getTermProperty()
    {
        return _termProperty;
    }

    /**
     * Gets default term accessor.
     *
     * @return the default term accessor
     */
    @Nonnull
    public Method getDefaultTermAccessor()
    {
        return _defaultTermAccessor;
    }

    /**
     * Gets default term.
     *
     * @param profileTermProvider the profile term provider
     *
     * @return the default term.
     */
    public TextSource getDefaultTerm(@Nonnull ProfileTermProvider profileTermProvider)
    {
        try
        {
            return (TextSource) _defaultTermAccessor.invoke(profileTermProvider);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            throw new IllegalStateException("Unable to get default term.", e);
        }
    }

    /**
     * Gets term.
     *
     * @param terms the terms
     *
     * @return the term.
     */
    public LocalizedObjectKey getTerm(@Nonnull ProfileTerms terms)
    {
        try
        {
            return (LocalizedObjectKey) _termProperty.getReadMethod().invoke(terms);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            throw new IllegalStateException("Unable to get term.", e);
        }
    }

    /**
     * Sets term.
     *
     * @param terms the terms.
     * @param value the value.
     */
    public void setTerm(@Nonnull ProfileTerms terms, @Nullable LocalizedObjectKey value)
    {
        try
        {
            _termProperty.getWriteMethod().invoke(terms, value);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            throw new IllegalStateException("Unable to set term.", e);
        }
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
            .add("defaultTermAccessor", getDefaultTermAccessor().getName())
            .add("termProperty", getTermProperty().getName())
            .toString();
    }
}
