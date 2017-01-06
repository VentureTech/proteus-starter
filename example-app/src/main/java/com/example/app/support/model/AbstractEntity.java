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

package com.example.app.support.model;

import org.hibernate.Hibernate;
import org.jetbrains.annotations.Contract;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

import net.proteusframework.core.hibernate.model.Entity;

/**
 * Entity {@link MappedSuperclass} {@link Entity} Implementation
 *
 * @param <T> the type of the ID
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@MappedSuperclass
public abstract class AbstractEntity<T extends Serializable> implements Entity<T>
{
    private static final long serialVersionUID = 3891246179133533980L;

    /** Database ID */
    private T _id;

    @Override
    public int hashCode()
    {
        if (getId() == null)
            return System.identityHashCode(this);
        else
            return getId().hashCode();
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS",
        "EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS"})
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Contract("_->!fail")
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (Hibernate.getClass(obj) != Hibernate.getClass(this))
            return false;

        T thisId = getId();
        // If either instance is transient, use object identity (Object.equals())
        if (thisId == null)
        {
            return this == obj;
        }

        Object otherId = ((AbstractEntity) obj).getId();
        if (otherId == null)
            return this == obj;
        else
            return thisId.equals(otherId);
    }

    @Override
    @Transient
    @NotNull
    public T getId()
    {
        return _id;
    }

    /**
     * Set the Id.
     *
     * @param id the Id
     */
    public void setId(T id)
    {
        _id = id;
    }

    @Override
    public String toString()
    {
        return Hibernate.getClass(this).getName() + '#' + getId();
    }
}
