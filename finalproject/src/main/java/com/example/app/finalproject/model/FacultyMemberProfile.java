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

package com.example.app.finalproject.model;



import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.util.Date;

import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.users.model.AbstractAuditableEntity;

/**
 * Model for FacultyMember
 *
 * @author Fajie Han (fhan@venturetechasia.net)
 * @since 14-11-5 ??10:12
 */
@Entity
@Table(
    name = "FacultyMemberProfile",
    uniqueConstraints = {@UniqueConstraint(columnNames ="slug")})
public class FacultyMemberProfile extends AbstractAuditableEntity
{
    /** The id */
    private long _id;
    /** The first name */
    private String _firstName;
    /** The last name */
    private String _lastName;
    /** The rank */
    private Rank _rank;
    /** The research area specialty */
    private String _researchArea;
    /** The picture of faculty*/
    private FileEntity _picture;
    /** The join date */
    private Date _joinDate;
    /** Boolean flag if the faculty is on sabbatical */
    private boolean _sabbatical;
    /** Boolean flag if the object is deleted or not */
    private boolean _deleted;
    /** The slug to use as part of the URL path */
    private String _slug;

    /**
     * Set the id.
     *
     * @param id the id.
     */
    public void setId(Long id)
    {
        _id = id;
    }

    /**
     * Get the id.
     *
     * @return the id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "FacultyMember_id_seq")
    @SequenceGenerator(name = "FacultyMember_id_seq", sequenceName = "FacultyMember_id_seq")
    @Override
    public Long getId()
    {
        return _id;
    }

    /**
     * Set the firstName
     * @param firstName-the firstName
     */
    public void setFirstName(String firstName)
    {
        _firstName = firstName;
    }

    /**
     * Get the firstName
     * @return the firstName
     */
    @NotNull
    public String getFirstName()
    {
        return _firstName;
    }

    /**
     * Set the lastName
     * @param lastName-the lastName
     */
    public void setLastName(String lastName)
    {
        _lastName = lastName;
    }

    /**
     * Get the lastName
     * @return the lastName
     */
    @NotNull
    public String getLastName()
    {
        return _lastName;
    }

    /**
     * Set the rank
     * @param rank-the rank
     */
    public void setRank(Rank rank)
    {
        _rank = rank;
    }

    /**
     * Get the rank
     * @return the rank
     */
    @Enumerated(EnumType.STRING)
    public Rank getRank()
    {
        return _rank;
    }

    /**
     * Set the researchArea
     * @param researchArea-the researchArea
     */
    public void setResearchArea(String researchArea)
    {
        _researchArea = researchArea;
    }

    /**
     * Get the researchArea
     * @return the researchArea
     */
    public String getResearchArea()
    {
        return _researchArea;
    }

    /**
     * Set the picture
     * @param picture-the picture
     */
    public void setPicture(FileEntity picture)
    {
        _picture = picture;
    }

    /**
     * Get the picture
     * @return the picture
     */
    @OneToOne(fetch= FetchType.LAZY)
    @Cascade(CascadeType.PERSIST)
    public FileEntity getPicture()
    {
        return _picture;
    }

    /**
     * Set the joinDate
     * @param joinDate-the joinDate
     */
    public void setJoinDate(Date joinDate)
    {
        _joinDate = joinDate;
    }

    /**
     * Get the join
     * @return the joinDate
     */
    public Date getJoinDate()
    {
        return _joinDate;
    }

    /**
     * Set the sabbatical
      * @param sabbatical-the sabbatical
     */
    public void setSabbatical(boolean sabbatical)
    {
        _sabbatical = sabbatical;
    }

    /**
     * Get the sabbatical
      * @return the sabbatical
     */
    public boolean getSabbatical()
    {
        return _sabbatical;
    }

    /**
     * Set the deleted
     * @param deleted-the deleted
     */
    public void setDeleted(boolean deleted)
    {
        _deleted = deleted;
    }

    /**
     * Get the deleted
      * @return the deleted
     */
    public boolean getDeleted()
    {
        return _deleted;
    }

    /**
     * Set the slug
      * @param slug-the slug
     */
    public void setSlug(String slug)
    {
        _slug = slug;
    }

    /**
     * Get the slug
      * @return the slug
     */
    public String getSlug()
    {
        return  _slug;
    }
}
