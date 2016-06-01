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

package com.example.app.model;

import org.hibernate.Hibernate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.jetbrains.annotations.Contract;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.Date;

import com.i2rd.hibernate.util.HibernateUtil;
import com.i2rd.util.objectgraph.GraphWalkerNoVisit;

import net.proteusframework.core.hibernate.model.Entity;
import net.proteusframework.users.audit.FullyAuditable;
import net.proteusframework.users.model.Principal;

/**
 * SoftDeleteEntity implementation that extends AbstractAuditableEntity.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@Audited
@MappedSuperclass
public abstract class AbstractAuditableSoftDeleteEntity implements FullyAuditable, SoftDeleteEntity, Entity<Integer>
{
    /** Serial UID. */
    private static final long serialVersionUID = 6197177026558816309L;
    /** boolean flag property.  If true, this entity has been deleted */
    private boolean _deleted;
    /** Last mod user */
    private Principal _lastModUser;
    /** Create user */
    private Principal _createUser;
    /** Last mod time */
    private Date _lastModTime;
    /** Create time */
    private Date _createTime;
    /** the id */
    private Integer _id;

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
    @NotAudited
    @Column(nullable = false)
    public Date getCreateTime()
    {
        return _createTime;
    }

    @Override
    public void setCreateTime(Date t)
    {
        _createTime = t;
    }

    @Override
    @GraphWalkerNoVisit(modes = HibernateUtil.DEEP_INIT_MODE_UI)
    @ManyToOne(fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
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

        Integer thisId = getId();
        // If either instance is transient, use object identity (Object.equals())
        if (thisId == null)
        {
            return this == obj;
        }

        Integer otherId = ((AbstractAuditableSoftDeleteEntity) obj).getId();
        if (otherId == null)
            return this == obj;
        else
            return thisId.equals(otherId);
    }

    /**
     * Get the id.
     *
     * @return the id.
     */
    @Override
    @NotNull
    @Transient
    public Integer getId()
    {
        return _id;
    }

    /**
     * Set the id.
     *
     * @param id the id.
     */
    public void setId(Integer id)
    {
        _id = id;
    }

    @Override
    public String toString()
    {
        return Hibernate.getClass(this).getName() + '#' + getId();
    }

    @Override
    @Basic
    @Column(name = SOFT_DELETE_COLUMN_PROP, nullable = false, columnDefinition = "boolean DEFAULT false")
    public boolean isDeleted()
    {
        return _deleted;
    }

    @Override
    public void setDeleted(boolean deleted)
    {
        _deleted = deleted;
    }
}
