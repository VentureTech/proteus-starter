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

package com.example.app.schedule.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

/**
 * UserType for java.time.Period.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public class PeriodUserType implements UserType
{
    // NOTE, we are not defining this as a default since the next version of hibernate will have support for this type


    private static final int[] SQL_TYPES = {Types.VARCHAR};

    @Override
    public int[] sqlTypes()
    {
        return SQL_TYPES;
    }

    @Override
    public Class<?> returnedClass()
    {
        return Period.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException
    {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException
    {
        return Objects.hashCode(x);
    }

    @Nullable
    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
        throws HibernateException, SQLException
    {

        final String period = rs.getString(names[0]);
        if (rs.wasNull())
        {
            return null;
        }
        try
        {
            return Period.parse(period);
        }
        catch (DateTimeParseException e)
        {
            throw new HibernateException("Unexpected value: " + period, e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
        throws HibernateException, SQLException
    {
        StringType.INSTANCE.nullSafeSet(st, Optional.ofNullable(value).map(Object::toString).orElse(null), index, session);
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

    @Nullable
    @Override
    public Serializable disassemble(Object value) throws HibernateException
    {
        if (value != null)
            return value.toString();
        else
            return null;
    }

    @Nullable
    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException
    {
        if (cached instanceof CharSequence)
            return Period.parse((CharSequence) cached);
        else
            return null;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException
    {
        return original;
    }
}
