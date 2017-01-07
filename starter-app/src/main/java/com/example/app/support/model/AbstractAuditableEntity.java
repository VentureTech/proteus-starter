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
import org.hibernate.envers.NotAudited;
import org.jetbrains.annotations.Contract;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

import com.i2rd.hibernate.util.HibernateUtil;
import com.i2rd.util.objectgraph.GraphWalkerNoVisit;

import net.proteusframework.core.hibernate.model.Entity;
import net.proteusframework.users.audit.FullyAuditable;
import net.proteusframework.users.model.Principal;

/**
 * Entity {@link MappedSuperclass} Implementation that is also {@link FullyAuditable}
 *
 * @param <T> the type of the ID
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@MappedSuperclass
public abstract class AbstractAuditableEntity<T extends Serializable> implements Entity<T>, FullyAuditable
{
    private static final long serialVersionUID = -8980924850730777958L;
    /** Database ID */
    private T _id;
    /** Last mod user */
    private Principal _lastModUser;
    /** Create user */
    private Principal _createUser;
    /** Last mod time */
    private Date _lastModTime;
    /** Create time */
    private Date _createTime;

    /** Create a new instance. */
    public AbstractAuditableEntity()
    {
        super();
    }

    @Override
    @Column(nullable = false)
    public Date getLastModTime()
    {
        return _lastModTime;
    }

    @Override
    public void setLastModTime(Date t)
    {
        _lastModTime = t;
    }

    @Override
    @Column(nullable = false)
    public Date getCreateTime()
    {
        return _createTime;
    }

    @Override
    @NotAudited
    public void setCreateTime(Date t)
    {
        _createTime = t;
    }

    @Override
    @GraphWalkerNoVisit(modes = HibernateUtil.DEEP_INIT_MODE_UI)
    @ManyToOne(fetch = FetchType.LAZY)
    public Principal getLastModUser()
    {
        return _lastModUser;
    }

    @Override
    public void setLastModUser(Principal p)
    {
        _lastModUser = p;
    }

    @Override
    @GraphWalkerNoVisit(modes = HibernateUtil.DEEP_INIT_MODE_UI)
    @ManyToOne(fetch = FetchType.LAZY)
    @NotAudited
    public Principal getCreateUser()
    {
        return _createUser;
    }

    @Override
    public void setCreateUser(Principal p)
    {
        _createUser = p;
    }

    @Override
    public int hashCode()
    {
        if (getId() == null)
            return System.identityHashCode(this);
        else
            return getId().hashCode();
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS")
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

        Object otherId = ((AbstractAuditableEntity) obj).getId();
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
