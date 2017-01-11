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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.example.app.profile.service.resource.ResourceTypeService;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

import net.proteusframework.internet.http.SiteContext;

/**
 * UserType for {@link ResourceType}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/12/15 11:52 AM
 */
@Configurable
public class ResourceTypeUserType implements UserType
{
    /** User Type Type Def Name */
    public static final String TYPEDEF = "resourceType";
    private static final int[] SQL_TYPES = {Types.VARCHAR};
    @Autowired
    private ResourceTypeService _resourceTypeService;
    @Autowired
    private SiteContext _siteContext;

    @Override
    public int[] sqlTypes()
    {
        return SQL_TYPES;
    }

    @Override
    public Class<?> returnedClass()
    {
        return ResourceType.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException
    {
        if (Objects.equals(x, y))
            return true;
        if ((x == null) || (y == null))
            return false;
        ResourceType resourceType1 = (ResourceType) x;
        ResourceType resourceType2 = (ResourceType) y;
        final ResourceTypeService.Context context = new ResourceTypeService.Context(_siteContext.getOperationalSite());
        String factory1 = _resourceTypeService.getFactoryIdentifier(context, resourceType1);
        if (factory1 == null)
            throw new HibernateException("Invalid ResourceType: " + x);
        String factory2 = _resourceTypeService.getFactoryIdentifier(context, resourceType2);
        if (factory2 == null)
            throw new HibernateException("Invalid ResourceType: " + y);
        return factory1.equals(factory2) && resourceType1.getIdentifier().equals(resourceType2.getIdentifier());
    }

    @Override
    public int hashCode(Object x) throws HibernateException
    {
        if (x == null) return 0;
        ResourceType resourceType = (ResourceType) x;
        String factory = _resourceTypeService.getFactoryIdentifier(
            new ResourceTypeService.Context(_siteContext.getOperationalSite()), resourceType);
        if (factory == null)
            throw new HibernateException("Invalid ResourceType: " + x);
        int res = factory.hashCode();
        res = res * 37 + resourceType.getIdentifier().hashCode();
        return res;
    }

    @Override
    @Nullable
    @SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE", justification = "written for update later.")
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
        throws HibernateException, SQLException
    {
        int index = 0;
        //final String factory = rs.getString(names[index++]);
        final String factory = ResourceTypeService.SPRING_FACTORY_IDENTIFIER;

        final String identifier = rs.getString(names[index]);
        return _resourceTypeService.getResourceType(factory, identifier);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
        throws HibernateException, SQLException
    {
        if (value == null)
        {
            //            StringType.INSTANCE.nullSafeSet(st, null, index++, session);
            StringType.INSTANCE.nullSafeSet(st, null, index, session);
        }
        else
        {
            ResourceType resourceType = (ResourceType) value;
            String factory = _resourceTypeService.getFactoryIdentifier(
                new ResourceTypeService.Context(_siteContext.getOperationalSite()), resourceType);
            if (factory == null)
            {
                throw new HibernateException("Invalid ResourceType: " + value);
            }
            //            StringType.INSTANCE.nullSafeSet(st, factory, index++, session);
            StringType.INSTANCE.nullSafeSet(st, resourceType.getIdentifier(), index, session);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException
    {
        return value;
    }

    @Override
    public boolean isMutable()
    {
        return false;
    }

    @Override
    @Nullable
    public Serializable disassemble(Object value) throws HibernateException
    {
        if (value == null)
            return null;
        ResourceType resourceType = (ResourceType) value;
        String factory = _resourceTypeService.getFactoryIdentifier(
            new ResourceTypeService.Context(_siteContext.getOperationalSite()), resourceType);
        if (factory == null)
        {
            throw new HibernateException("Invalid ResourceType: " + value);
        }
        return new String[]{factory, resourceType.getIdentifier()};
    }

    @Nullable
    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException
    {
        if (cached == null)
            return null;
        String[] key = (String[]) cached;
        return _resourceTypeService.getResourceType(key[0], key[1]);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException
    {
        return original;
    }
}
