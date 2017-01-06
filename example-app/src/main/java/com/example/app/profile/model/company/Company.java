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

package com.example.app.profile.model.company;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import com.example.app.profile.model.Profile;
import com.example.app.profile.model.location.Location;
import com.example.app.profile.model.terminology.ProfileTerms;
import com.example.app.profile.model.user.User;
import com.example.app.support.service.StandaloneLabelDomainProvider;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.validator.constraints.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import net.proteusframework.cms.CmsHostname;
import net.proteusframework.cms.label.LabelDomain;
import net.proteusframework.cms.label.LabelDomainProvider;
import net.proteusframework.data.filesystem.FileEntity;

/**
 * An entity that represents a person or organization within the leadership development platform that facilitates coaching and
 * manages plans.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11 /23/15 8:52 AM
 */
@Entity
@Table(name = Company.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.PROFILE_DATA)
@Audited
@DiscriminatorValue("company")
public class Company extends Profile
{
    private static final long serialVersionUID = -233660273002058726L;

    /** The database table name */
    public static final String TABLE_NAME = "Company";
    /** The database column and property: programmaticName */
    public static final String PROGRAMMATIC_IDENTIFIER_COLUMN_PROP = "programmaticIdentifier";
    /** THe database join table for Company to User */
    public static final String USERS_JOIN_TABLE = "company_user";
    /** The property: users */
    public static final String USERS_PROP = "users";
    /** the database column name and property: linkedInLink */
    public static final String LINKEDIN_LINK_COLUMN_PROP = "linkedInLink";
    /** the database column name and property: twitterLink */
    public static final String TWITTER_LINK_COLUMN_PROP = "twitterLink";
    /** the database column name and property: facebookLink */
    public static final String FACEBOOK_LINK_COLUMN_PROP = "facebookLink";
    /** the database column name and property: googlePlusLink */
    public static final String GOOGLEPLUS_LINK_COLUMN_PROP = "googlePlusLink";
    /** The property: locations */
    public static final String LOCATIONS_PROP = "locations";
    /** Joni Column for relationship between Location and Company */
    public static final String LOCATIONS_JOIN_COLUMN = "location_id";
    /** Join table for relationship between Location and Company */
    public static final String LOCATIONS_JOIN_TABLE = TABLE_NAME + '_' + Location.TABLE_NAME;
    /** The property: primaryLocation */
    public static final String PRIMARY_LOCATION_PROP = "primaryLocation";
    /** the database column name for the property: emailLogo */
    public static final String EMAIL_LOGO_COLUMN = "emaillogo_id";
    /** the database column name for the property: emailLogo */
    public static final String EMAIL_LOGO_PROP = "emailLogo";
    /** the database column name for the property: image */
    public static final String IMAGE_COLUMN = "image_id";
    /** the property: image */
    public static final String IMAGE_PROP = "image";
    /** The property: status */
    public static final String STATUS_PROP = "status";
    /** The property: profileTerms */
    public static final String PROFILE_TERMS_PROP = "profileTerms";
    /** The property: resourceTags */
    public static final String RESOURCE_TAGS_PROP = "resourceTags";
    /** The property: resourceCategories */
    public static final String RESOURCE_CATEGORIES_PROP = "resourceCategories";

    /** The property: websiteLink */
    public static final String WEBSITE_LINK_PROP = "websiteLink";

    /** The database column: hostname */
    public static final String HOSTNAME_COLUMN = "hostname_id";
    /** The property: hostname */
    public static final String HOSTNAME_PROP = "hostname";

    private String _websiteLink;
    private FileEntity _image;
    private FileEntity _emailLogo;
    private String _linkedInLink;
    private String _twitterLink;
    private String _facebookLink;
    private String _googlePlusLink;
    private List<Location> _locations = new ArrayList<>();
    private Location _primaryLocation = new Location();
    private String _programmaticIdentifier;
    private List<User> _users = new ArrayList<>();
    private CompanyStatus _status = CompanyStatus.Inactive;
    private ProfileTerms _profileTerms;
    private LabelDomain _resourceTags;
    private LabelDomain _resourceCategories;
    private CmsHostname _hostname = new CmsHostname();

    /**
     * Instantiates a new company.
     */
    public Company()
    {
        super();
    }

    /**
     * Get the programmatic identifier for this Company
     *
     * @return the programmatic identifier
     */
    @Column(name = PROGRAMMATIC_IDENTIFIER_COLUMN_PROP, unique = true)
    @Nonnull
    @NotNull
    public String getProgrammaticIdentifier()
    {
        return _programmaticIdentifier;
    }

    /**
     * Set the programmatic identifier for this Company
     *
     * @param programmaticIdentifier the programmatic identifier
     */
    public void setProgrammaticIdentifier(@Nonnull String programmaticIdentifier)
    {
        _programmaticIdentifier = programmaticIdentifier;
    }

    /**
     * Get list of Users that are associated with this Company
     *
     * @return a list of Users
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(schema = ProjectConfig.PROJECT_SCHEMA, name = USERS_JOIN_TABLE,
        joinColumns = { @JoinColumn(name = ID_COLUMN) },
        inverseJoinColumns = { @JoinColumn(name = User.ID_COLUMN) })
    @Nonnull
    public List<User> getUsers()
    {
        return _users;
    }

    /**
     * Set list of Users that are associated with this Company
     *
     * @param users a list of Users
     */
    public void setUsers(List<User> users)
    {
        _users = users;
    }

    /**
     * Add user.
     *
     * @param user the user
     * @see CompanyDAO#addUserToCompany(Company, User)
     */
    @Transient
    public void addUser(User user)
    {
        getUsers().add(user);
        if(!user.getCompanies().contains(this))
            user.getCompanies().add(this);
    }

    /**
     * Get this company's Image {@link FileEntity}
     *
     * @return this company's Image FileEntity
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = IMAGE_COLUMN)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Nullable
    public FileEntity getImage()
    {
        return _image;
    }

    /**
     * Set this company's Image {@link FileEntity}
     *
     * @param image the new Image
     */
    public void setImage(@Nullable  FileEntity image)
    {
        _image = image;
    }

    /**
     * Gets email logo.
     *
     * @return the email logo
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = EMAIL_LOGO_COLUMN)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Nullable
    public FileEntity getEmailLogo()
    {
        return _emailLogo;
    }

    /**
     * Sets email logo.
     *
     * @param emailLogo the email logo
     */
    public void setEmailLogo(@Nullable FileEntity emailLogo)
    {
        _emailLogo = emailLogo;
    }

    /**
     * Gets website link.
     *
     * @return the website link
     */
    @Column(name = WEBSITE_LINK_PROP)
    @URL
    @Nullable
    public String getWebsiteLink()
    {
        return _websiteLink;
    }

    /**
     * Sets website link.
     *
     * @param websiteLink the website link
     */
    public void setWebsiteLink(@Nullable String websiteLink)
    {
        _websiteLink = websiteLink;
    }

    /**
     * Get the company's LinkedIn profile URL
     *
     * @return the company's LinkedIn profile URL
     */
    @Column(name = LINKEDIN_LINK_COLUMN_PROP)
    @URL
    @Nullable
    public String getLinkedInLink()
    {
        return _linkedInLink;
    }

    /**
     * Set the company's LinkedIn profile URL
     *
     * @param linkedInLink the company's LinkedIn profile URL
     */
    public void setLinkedInLink(@Nullable String linkedInLink)
    {
        _linkedInLink = linkedInLink;
    }

    /**
     * Get the company's Twitter profile URL
     *
     * @return the company's Twitter profile URL
     */
    @Column(name = TWITTER_LINK_COLUMN_PROP)
    @URL
    @Nullable
    public String getTwitterLink()
    {
        return _twitterLink;
    }

    /**
     * Set the company's Twitter profile URL
     *
     * @param twitterLink the company's Twitter profile URL
     */
    public void setTwitterLink(@Nullable String twitterLink)
    {
        _twitterLink = twitterLink;
    }

    /**
     * Get the company's Facebook profile URL
     *
     * @return the company's Facebook profile URL
     */
    @Column(name = FACEBOOK_LINK_COLUMN_PROP)
    @URL
    @Nullable
    public String getFacebookLink()
    {
        return _facebookLink;
    }

    /**
     * Set the company's Facebook profile URL
     *
     * @param facebookLink the company's Facebook profile URL
     */
    public void setFacebookLink(@Nullable String facebookLink)
    {
        _facebookLink = facebookLink;
    }

    /**
     * Get the company's Google Plus profile URL
     *
     * @return the company's Google Plus profile URL
     */
    @Column(name = GOOGLEPLUS_LINK_COLUMN_PROP)
    @URL
    @Nullable
    public String getGooglePlusLink()
    {
        return _googlePlusLink;
    }

    /**
     * Set the company's Google Plus profile URL
     *
     * @param googlePlusLink the company's Google Plus profile URL
     */
    public void setGooglePlusLink(@Nullable String googlePlusLink)
    {
        _googlePlusLink = googlePlusLink;
    }

    /**
     * Gets locations.
     *
     * @return the locations
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = LOCATIONS_JOIN_TABLE, schema = ProjectConfig.PROJECT_SCHEMA,
        joinColumns = @JoinColumn(name = ID_COLUMN),
        inverseJoinColumns = @JoinColumn(name = LOCATIONS_JOIN_COLUMN))
    @Cascade(CascadeType.ALL)
    @Nonnull
    public List<Location> getLocations()
    {
        return _locations;
    }

    /**
     * Sets locations.
     *
     * @param locations the locations
     */
    public void setLocations(@Nonnull List<Location> locations)
    {
        _locations = locations;
    }

    /**
     * Gets primary location.
     *
     * @return the primary location
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = LOCATIONS_JOIN_COLUMN)
    @Cascade(CascadeType.ALL)
    @NotNull
    public Location getPrimaryLocation()
    {
        return _primaryLocation;
    }

    /**
     * Sets primary location.
     *
     * @param primaryLocation the primary location
     */
    public void setPrimaryLocation(@NotNull Location primaryLocation)
    {
        _primaryLocation = primaryLocation;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = STATUS_PROP)
    @Nonnull
    @NotNull
    public CompanyStatus getStatus()
    {
        return _status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(@Nonnull CompanyStatus status)
    {
        _status = status;
    }

    /**
     * Gets profile terms.
     *
     * @return the profile terms
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PROFILE_TERMS_PROP + "_id")
    @Cascade(CascadeType.ALL)
    @NotNull
    public ProfileTerms getProfileTerms()
    {
        return _profileTerms;
    }

    /**
     * Sets profile terms.
     *
     * @param profileTerms the profile terms
     */
    public void setProfileTerms(ProfileTerms profileTerms)
    {
        _profileTerms = profileTerms;
    }

    /**
     * Gets resource tags.
     *
     * @return the resource tags
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = RESOURCE_TAGS_PROP + "_id")
    @Cascade(CascadeType.ALL)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Nullable
    public LabelDomain getResourceTags()
    {
        return _resourceTags;
    }

    /**
     * Sets resource tags.
     *
     * @param resourceTags the resource tags
     */
    public void setResourceTags(@Nonnull LabelDomain resourceTags)
    {
        _resourceTags = resourceTags;
    }

    /**
     * Gets resource tags label provider.
     *
     * @return the resource tags label provider
     */
    @Transient
    @Nullable
    public LabelDomainProvider getResourceTagsLabelProvider()
    {
        if(getResourceTags() == null || getResourceTags().getId() == null || getResourceTags().getId() == 0)
            return null;
        return new StandaloneLabelDomainProvider(getResourceTags());
    }

    /**
     * Gets resource categories.
     *
     * @return the resource categories
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = RESOURCE_CATEGORIES_PROP + "_id")
    @Cascade(CascadeType.ALL)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Nullable
    public LabelDomain getResourceCategories()
    {
        return _resourceCategories;
    }

    /**
     * Sets resource categories.
     *
     * @param resourceCategories the resource categories
     */
    public void setResourceCategories(@Nonnull LabelDomain resourceCategories)
    {
        _resourceCategories = resourceCategories;
    }

    /**
     * Gets resource categories label provider.
     *
     * @return the resource categories label provider
     */
    @Transient
    @Nullable
    public LabelDomainProvider getResourceCategoriesLabelProvider()
    {
        if(getResourceCategories() == null || getResourceCategories().getId() == null || getResourceCategories().getId() == 0)
            return null;
        return new StandaloneLabelDomainProvider(getResourceCategories());
    }

    /**
     * Gets host name.
     *
     * @return the host name
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = HOSTNAME_COLUMN)
    @Cascade(CascadeType.ALL)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull
    public CmsHostname getHostname()
    {
        return _hostname;
    }

    /**
     * Sets host name.
     *
     * @param hostname the host name
     */
    public void setHostname(@Nonnull CmsHostname hostname)
    {
        _hostname = hostname;
    }
}
