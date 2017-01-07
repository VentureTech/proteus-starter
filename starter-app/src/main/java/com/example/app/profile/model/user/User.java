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

package com.example.app.profile.model.user;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import com.example.app.profile.model.company.Company;
import com.example.app.support.model.AbstractAuditableEntity;
import org.hibernate.annotations.BatchSize;
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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import com.i2rd.users.miwt.PrincipalRenderer;

import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.users.model.PhoneNumber;
import net.proteusframework.users.model.Principal;

import static com.i2rd.users.miwt.PrincipalRenderer.DEFAULT_FORMAT_WITHOUT_USER;

/**
 * The User Entity -- It represents a user within the LDP System.  This Entity is fully auditable and implements soft delete.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 10/30/15 10:23 AM
 */
@Entity
@Table(name = User.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA, indexes = {

})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
@BatchSize(size = 10)
@Audited
public class User extends AbstractAuditableEntity<Integer> implements NamedObject
{
    /** the database table name for this entity */
    public static final String TABLE_NAME = "User";
    /** the database id column for this entity */
    public static final String ID_COLUMN = "user_id";
    /** the database column name for the property: principal */
    public static final String PRINCIPAL_COLUMN = "principal_id";
    /** the property: principal */
    public static final String PRINCIPAL_PROP = "principal";
    /** the database column name for the property: image */
    public static final String IMAGE_COLUMN = "image_id";
    /** the property: image */
    public static final String IMAGE_PROP = "image";
    /** the database column name and property: linkedInLink */
    public static final String LINKEDIN_LINK_COLUMN_PROP = "linkedInLink";
    /** the database column name and property: twitterLink */
    public static final String TWITTER_LINK_COLUMN_PROP = "twitterLink";
    /** the database column name and property: facebookLink */
    public static final String FACEBOOK_LINK_COLUMN_PROP = "facebookLink";
    /** the database column name and property: googlePlusLink */
    public static final String GOOGLEPLUS_LINK_COLUMN_PROP = "googlePlusLink";
    /** the property: userPositions */
    public static final String USER_POSITIONS_PROP = "userPositions";
    /** The database column for property: smsPhone */
    public static final String SMS_PHONE_COLUMN = "smsPhone_id";
    /** The property: smsPhone */
    public static final String SMS_PHONE_PROP = "smsPhone";
    /** The property: preferredContactMethod */
    public static final String PREFERRED_CONTACT_METHOD_PROP = "preferredContactMethod";
    /** The database column for property: preferredContactMethodProcess */
    public static final String PREFERRED_CONTACT_METHOD_COLUMN = "preferredContactMethod";
    /** The property: companies */
    public static final String COMPANIES_PROP = "companies";
    /** Preference Node for Login */
    public static final String LOGIN_PREF_NODE = "LOGIN_PREF";
    /** Prefernce for Login Landing Page */
    public static final String LOGIN_PREF_NODE_LANDING_PAGE = "LANDING_PAGE";
    /** the serial version UID */
    private static final long serialVersionUID = -5249238131672936683L;
    /** the ID generator identifier for this entity */
    private static final String GENERATOR = ProjectConfig.PROJECT_SCHEMA + ".user_id_seq";
    /** the underlying principal for this user */
    private Principal _principal;
    /** the user image for this user */
    private FileEntity _image;
    /** url to user's linkedin profile */
    private String _linkedInLink;
    /** url to user's twitter account */
    private String _twitterLink;
    /** url to users facebook page */
    private String _facebookLink;
    /** url to user's google plus page */
    private String _googlePlusLink;
    /** list of positions that this user holds or has held */
    private List<UserPosition> _userPositions = new ArrayList<>();
    /** the phone number for sms/text. */
    private PhoneNumber _smsPhone;
    /** preferred contact method */
    private ContactMethod _preferredContactMethod;
    /** coaching entities for this user */
    private List<Company> _companies = new ArrayList<>();

    /**
     * Constructor.
     */
    public User()
    {
        super();
    }

    /**
     * Get the user's Facebook profile URL
     *
     * @return the user's Facebook profile URL
     */
    @Column(name = FACEBOOK_LINK_COLUMN_PROP)
    @URL
    public String getFacebookLink()
    {
        return _facebookLink;
    }

    /**
     * Set the user's Facebook profile URL
     *
     * @param facebookLink the user's Facebook profile URL
     */
    public void setFacebookLink(String facebookLink)
    {
        _facebookLink = facebookLink;
    }

    /**
     * Get the user's Google Plus profile URL
     *
     * @return the user's Google Plus profile URL
     */
    @Column(name = GOOGLEPLUS_LINK_COLUMN_PROP)
    @URL
    public String getGooglePlusLink()
    {
        return _googlePlusLink;
    }

    /**
     * Set the user's Google Plus profile URL
     *
     * @param googlePlusLink the user's Google Plus profile URL
     */
    public void setGooglePlusLink(String googlePlusLink)
    {
        _googlePlusLink = googlePlusLink;
    }

    @Id
    @Column(name = ID_COLUMN)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(name = GENERATOR, sequenceName = GENERATOR)
    @Override
    public Integer getId()
    {
        return super.getId();
    }

    /**
     * Get this User's Image {@link FileEntity}
     *
     * @return this User's Image FileEntity
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
     * Set this User's Image {@link FileEntity}
     *
     * @param image the new Image
     */
    public void setImage(@Nullable FileEntity image)
    {
        _image = image;
    }

    /**
     * Get the user's LinkedIn profile URL
     *
     * @return the user's LinkedIn profile URL
     */
    @Column(name = LINKEDIN_LINK_COLUMN_PROP)
    @URL
    public String getLinkedInLink()
    {
        return _linkedInLink;
    }

    /**
     * Set the user's LinkedIn profile URL
     *
     * @param linkedInLink the user's LinkedIn profile URL
     */
    public void setLinkedInLink(String linkedInLink)
    {
        _linkedInLink = linkedInLink;
    }

    @Transient
    @Nonnull
    @Override
    public TextSource getName()
    {
        PrincipalRenderer renderer = new PrincipalRenderer();
        renderer.setFormat(DEFAULT_FORMAT_WITHOUT_USER);
        return renderer.getFormattedTextSource(getPrincipal());
    }

    /**
     * Get the underlying {@link Principal} for this
     *
     * @return the underlying Principal for this
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PRINCIPAL_COLUMN, unique = true)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @NotNull
    @Nonnull
    public Principal getPrincipal()
    {
        return _principal;
    }

    /**
     * Set the underlying {@link Principal} for this
     *
     * @param principal the new underlying Principal for this
     */
    public void setPrincipal(Principal principal)
    {
        _principal = principal;
    }

    @Transient
    @Nullable
    @Override
    public TextSource getDescription()
    {
        return new PrincipalRenderer().getFormattedTextSource(getPrincipal());
    }

    /**
     * Get the preferred contact method.
     *
     * @return the preferred contact method.
     */
    @Column(name = PREFERRED_CONTACT_METHOD_COLUMN)
    @Enumerated(EnumType.STRING)
    @Nullable
    public ContactMethod getPreferredContactMethod()
    {
        return _preferredContactMethod;
    }

    /**
     * Set the preferred contact method.
     *
     * @param preferredContactMethod the preferred contact method
     */
    public void setPreferredContactMethod(@Nullable ContactMethod preferredContactMethod)
    {
        _preferredContactMethod = preferredContactMethod;
    }

    /**
     * Get the preferred phone number for receiving SMS messages for this User
     *
     * @return phone number
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = SMS_PHONE_COLUMN)
    @Cascade(CascadeType.ALL)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Nullable
    public PhoneNumber getSmsPhone()
    {
        return _smsPhone;
    }

    /**
     * Set the preferred phone number ofr receiving SMS messages for this User
     *
     * @param smsPhone phone number
     */
    public void setSmsPhone(@Nullable PhoneNumber smsPhone)
    {
        _smsPhone = smsPhone;
    }

    /**
     * Get the user's Twitter profile URL
     *
     * @return the user's Twitter profile URL
     */
    @Column(name = TWITTER_LINK_COLUMN_PROP)
    @URL
    public String getTwitterLink()
    {
        return _twitterLink;
    }

    /**
     * Set the user's Twitter profile URL
     *
     * @param twitterLink the user's Twitter profile URL
     */
    public void setTwitterLink(String twitterLink)
    {
        _twitterLink = twitterLink;
    }

    /**
     * Get list of {@link UserPosition}s that this user holds or has held
     *
     * @return list of UserPositions
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = UserPosition.USER_PROP)
    @Cascade(CascadeType.ALL)
    @BatchSize(size = 10)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.ENTITY_DATA)
    @NotNull
    @Nonnull
    public List<UserPosition> getUserPositions()
    {
        return _userPositions;
    }

    /**
     * Set list of {@link UserPosition}s that this user holds or has held
     *
     * @param userPositions list of UserPositions
     */
    public void setUserPositions(List<UserPosition> userPositions)
    {
        _userPositions = userPositions;
    }

    /**
     * Gets coaching entities.
     *
     * @return the coaching entities
     */
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = Company.USERS_PROP)
    public List<Company> getCompanies()
    {
        return _companies;
    }

    /**
     * Sets coaching entities.
     *
     * @param companies the coaching entities
     */
    public void setCompanies(List<Company> companies)
    {
        _companies = companies;
    }
}
