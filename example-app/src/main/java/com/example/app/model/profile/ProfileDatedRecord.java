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

package com.example.app.model.profile;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Entity record used to signify <i>something</i>.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 4/26/16 4:28 PM
 */
@Entity
@Table(name = ProfileDatedRecord.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.PROFILE_DATA)
public class ProfileDatedRecord implements net.proteusframework.core.hibernate.model.Entity<Integer>
{
    /** The database table name */
    public static final String TABLE_NAME = "ProfileDatedRecord";
    /** The database id column */
    public static final String ID_COLUMN = "profileDatedRecord_id";
    /** The property: date */
    public static final String DATE_PROP = "date";
    /** The property: profile */
    public static final String PROFILE_PROP = "profile";
    /** The property: category */
    public static final String CATEGORY_PROP = "category";
    /** The property: subCategory */
    public static final String SUBCATEGORY_PROP = "subCategory";
    private static final long serialVersionUID = 4441129836124060203L;
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + '.' + ID_COLUMN + "_seq";
    private Integer _id;
    private Date _date;
    private Profile _profile;
    private String _category;
    private String _subCategory;

    /**
     * Get the Category for this DatedRecord
     *
     * @return category
     */
    @Column(name = CATEGORY_PROP)
    @NotNull
    @Nonnull
    public String getCategory()
    {
        return _category;
    }

    /**
     * Set the Category for this DatedRecord
     *
     * @param category the category
     */
    public void setCategory(@Nonnull String category)
    {
        _category = category;
    }

    /**
     * Get the Date for this DatedRecord
     *
     * @return date
     */
    @Column(name = DATE_PROP)
    @NotNull
    @Nonnull
    public Date getDate()
    {
        return _date;
    }

    /**
     * Set the Date for this DatedRecord
     *
     * @param date date
     */
    public void setDate(@Nonnull Date date)
    {
        _date = date;
    }

    @Id
    @Column(name = ID_COLUMN)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(name = GENERATOR, sequenceName = GENERATOR)
    @Override
    @NotNull
    @Access(AccessType.PROPERTY)
    public Integer getId()
    {
        return _id;
    }

    /**
     * Set the Id
     *
     * @param id the Id
     */
    public void setId(Integer id)
    {
        _id = id;
    }

    /**
     * Get the Profile for this DatedRecord
     *
     * @return profile
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    @NotNull
    @Nonnull
    public Profile getProfile()
    {
        return _profile;
    }

    /**
     * Set the Profile for this DatedRecord
     *
     * @param profile the profile
     */
    public void setProfile(@Nonnull Profile profile)
    {
        _profile = profile;
    }

    /**
     * Get the SubCategory for this DatedRecord
     *
     * @return sub-category
     */
    @Column(name = SUBCATEGORY_PROP)
    @Nullable
    public String getSubCategory()
    {
        return _subCategory;
    }

    /**
     * Set the SubCategory for this DatedRecord
     *
     * @param subCategory sub-category
     */
    public void setSubCategory(@Nullable String subCategory)
    {
        _subCategory = subCategory;
    }
}
