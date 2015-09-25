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

import com.google.common.base.Preconditions;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;
import org.jetbrains.annotations.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Date;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.users.audit.TimeAuditable;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.Name;

/**
 * Example user profile.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@Entity
public class UserProfile implements TimeAuditable
{
    /** Sequence name. */
    private static final String SEQ = "userprofile_seq";
    /** identifier name. */
    private static final String USERPROFILE_ID = "userprofile_id";
    /** Identifier. */
    private long _id;
    /** Name - we'll use some of the properties of this class. */
    private Name _name = new Name();
    /** Postal Address - we'll use some of the properties of this class. */
    private Address _postalAddress = new Address();
    /** Phone Number. */
    private String _phoneNumber = "";
    /** Email Address. */
    private String _emailAddress = "";
    /** Twitter Link. */
    private URL _twitterLink;
    /** Facebook Link. */
    private URL _facebookLink;
    /** LinkIn Link. */
    private URL _linkedInLink;
    /** About Me Video Link. */
    private URL _aboutMeVideoLink;
    /** About Me Prose. */
    private String _aboutMeProse = "";
    /** Picture. */
    private FileEntity _picture;
    /** Last Modified Time. */
    private Date _lastModTime = new Date();
    /** Create Time. */
    private Date _createTime = _lastModTime;
    /** Site. */
    private CmsSite _site;

    /**
     * Create a new User Profile.
     */
    public UserProfile()
    {
        super();
    }

    /**
     * Copy constructor.
     *
     * @param toCopy profile to copy.
     */
    public UserProfile(UserProfile toCopy)
    {
        super();
        setName(new Name(toCopy.getName()));
        setPostalAddress(new Address(toCopy.getPostalAddress()));
        setPhoneNumber(toCopy.getPhoneNumber());
        setEmailAddress(toCopy.getEmailAddress());
        setTwitterLink(toCopy.getTwitterLink());
        setFacebookLink(toCopy.getFacebookLink());
        setLinkedInLink(toCopy.getLinkedInLink());
        setAboutMeVideoLink(toCopy.getAboutMeVideoLink());
        setAboutMeProse(toCopy.getAboutMeProse());
        setPicture(toCopy.getPicture());
        setSite(toCopy.getSite());
    }

    /**
     * Get the identifier.
     *
     * @return the identifier.
     */
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.AUTO, generator = SEQ)
    @SequenceGenerator(name = SEQ, sequenceName = SEQ)
    @Column(name = USERPROFILE_ID)
    public Long getId()
    {
        return _id;
    }

    /**
     * Set the identifier.
     *
     * @param id the identifier.
     */
    public void setId(Long id)
    {
        _id = id;
    }

    /**
     * Get the name.
     *
     * @return the name.
     */
    @ManyToOne(cascade = {CascadeType.ALL})
    @NotNull
    public Name getName()
    {
        return _name;
    }

    /**
     * Set the name.
     *
     * @param name the name.
     */
    public void setName(Name name)
    {
        if (name == null)
            _name = new Name();
        else
            _name = name;
    }

    /**
     * Get the postal address.
     *
     * @return the postal address.
     */
    @ManyToOne(cascade = {CascadeType.ALL})
    public Address getPostalAddress()
    {
        return _postalAddress;
    }

    /**
     * Set the postal address.
     *
     * @param postalAddress the postal address.
     */
    public void setPostalAddress(Address postalAddress)
    {
        if (postalAddress == null)
            _postalAddress = new Address();
        else
            _postalAddress = postalAddress;
    }

    /**
     * Get the phone number.
     *
     * @return the phone number.
     */
    public String getPhoneNumber()
    {
        return _phoneNumber;
    }

    /**
     * Set the phone number.
     *
     * @param phoneNumber the phone number.
     */
    public void setPhoneNumber(@Nullable String phoneNumber)
    {
        if (phoneNumber == null)
            _phoneNumber = "";
        else
            _phoneNumber = phoneNumber;
    }

    /**
     * Get the email address.
     *
     * @return the email address.
     */
    @Email
    public String getEmailAddress()
    {
        return _emailAddress;
    }

    /**
     * Set the email address.
     *
     * @param emailAddress the email address.
     */
    public void setEmailAddress(@Nullable String emailAddress)
    {
        if (emailAddress == null)
            _emailAddress = "";
        else
            _emailAddress = emailAddress;
    }

    /**
     * Get the Twitter link.
     *
     * @return the Twitter link. May be null.
     */
    public URL getTwitterLink()
    {
        return _twitterLink;
    }

    /**
     * Set the Twitter link.
     *
     * @param twitterLink the Twitter link.
     */
    public void setTwitterLink(@Nullable URL twitterLink)
    {
        _twitterLink = twitterLink;
    }

    /**
     * Get the Facebook link.
     *
     * @return the Facebook link. May be null.
     */
    public URL getFacebookLink()
    {
        return _facebookLink;
    }

    /**
     * Set the Facebook link.
     *
     * @param facebookLink the Facebook link.
     */
    public void setFacebookLink(@Nullable URL facebookLink)
    {
        _facebookLink = facebookLink;
    }

    /**
     * Get the LinkedIn link.
     *
     * @return the LinkedIn link. May be null.
     */
    public URL getLinkedInLink()
    {
        return _linkedInLink;
    }

    /**
     * Set the LinkedIn link.
     *
     * @param linkedInLink the LinkedIn link.
     */
    public void setLinkedInLink(@Nullable URL linkedInLink)
    {
        _linkedInLink = linkedInLink;
    }

    /**
     * Get the about me video link.
     *
     * @return the about me video link. May be null.
     */
    public URL getAboutMeVideoLink()
    {
        return _aboutMeVideoLink;
    }

    /**
     * Set the about me video link.
     *
     * @param aboutMeVideoLink the about me video link.
     */
    public void setAboutMeVideoLink(@Nullable URL aboutMeVideoLink)
    {
        _aboutMeVideoLink = aboutMeVideoLink;
    }

    /**
     * Get the about me prose.
     *
     * @return the about me prose.
     */
    @Length(max=4000) // Shooting for about 500 words max plus HTML tags
    @Column(columnDefinition = "varchar(4000)")
    @SafeHtml
    public String getAboutMeProse()
    {
        return _aboutMeProse;
    }

    /**
     * Set the about me prose.
     *
     * @param aboutMeProse the about me prose.
     */
    public void setAboutMeProse(@Nullable String aboutMeProse)
    {
        if (aboutMeProse == null)
            _aboutMeProse = "";
        else
            _aboutMeProse = aboutMeProse;
    }

    /**
     * Get the picture.
     *
     * @return the picture.
     */
    @ManyToOne(cascade = {CascadeType.REMOVE}/*cascade persist will be handled in the DAO.*/)
    public FileEntity getPicture()
    {
        return _picture;
    }

    /**
     * Set the picture.
     *
     * @param picture the picture.
     */
    public void setPicture(@Nullable FileEntity picture)
    {
        _picture = picture;
    }

    @Override
    public Date getLastModTime()
    {
        return _lastModTime;
    }

    @Override
    public void setLastModTime(Date lastModTime)
    {
        Preconditions.checkNotNull(lastModTime);
        _lastModTime = lastModTime;
    }

    @Override
    public Date getCreateTime()
    {
        return _createTime;
    }

    @Override
    public void setCreateTime(Date createTime)
    {
        _createTime = createTime;
    }

    /**
     * Get the site.
     *
     * @return the site.
     */
    @ManyToOne
    // NOTE: Some CMS entities are mapped using XML. Hibernate gives them a goofy column name like "site_site_id".
    //  we want to make the name make sense, so we'll override the default name in this case.
    @JoinColumn(name = "site_id")
    public CmsSite getSite()
    {
        return _site;
    }

    /**
     * Set the site that the user profile is associated with.
     *
     * @param site the site.
     */
    public void setSite(CmsSite site)
    {
        _site = site;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof UserProfile)) return false;

        UserProfile that = (UserProfile) o;

        if (getId() != null)
            return getId().equals(that.getId());

        if (_aboutMeProse != null ? !_aboutMeProse.equals(that._aboutMeProse) : that._aboutMeProse != null) return false;
        if (_aboutMeVideoLink != null ? !_aboutMeVideoLink.toString().equals(that._aboutMeVideoLink.toString())
            : that._aboutMeVideoLink != null)
            return false;
        if (_createTime != null ? !_createTime.equals(that._createTime) : that._createTime != null) return false;
        if (_emailAddress != null ? !_emailAddress.equals(that._emailAddress) : that._emailAddress != null) return false;
        if (_facebookLink != null ? !_facebookLink.toString().equals(that._facebookLink.toString()) : that._facebookLink != null)
            return false;
        if (_lastModTime != null ? !_lastModTime.equals(that._lastModTime) : that._lastModTime != null) return false;
        if (_linkedInLink != null ? !_linkedInLink.toString().equals(that._linkedInLink.toString()) : that._linkedInLink != null)
            return false;
        if (_name != null ? !_name.equals(that._name) : that._name != null) return false;
        if (_phoneNumber != null ? !_phoneNumber.equals(that._phoneNumber) : that._phoneNumber != null) return false;
        if (_picture != null ? !_picture.equals(that._picture) : that._picture != null) return false;
        if (_postalAddress != null ? !_postalAddress.equals(that._postalAddress) : that._postalAddress != null) return false;
        if (_site != null ? !_site.equals(that._site) : that._site != null) return false;

        return !(_twitterLink != null ? !_twitterLink.toString().equals(that._twitterLink.toString()) : that._twitterLink != null);
    }

    @Override
    public int hashCode()
    {
        if (getId() != null)
            return getId().hashCode();
        int result = _name != null ? _name.hashCode() : 0;
        result = 31 * result + (_postalAddress != null ? _postalAddress.hashCode() : 0);
        result = 31 * result + (_phoneNumber != null ? _phoneNumber.hashCode() : 0);
        result = 31 * result + (_emailAddress != null ? _emailAddress.hashCode() : 0);
        result = 31 * result + (_twitterLink != null ? _twitterLink.toString().hashCode() : 0);
        result = 31 * result + (_facebookLink != null ? _facebookLink.toString().hashCode() : 0);
        result = 31 * result + (_linkedInLink != null ? _linkedInLink.toString().hashCode() : 0);
        result = 31 * result + (_aboutMeVideoLink != null ? _aboutMeVideoLink.toString().hashCode() : 0);
        result = 31 * result + (_aboutMeProse != null ? _aboutMeProse.hashCode() : 0);
        result = 31 * result + (_picture != null ? _picture.hashCode() : 0);
        result = 31 * result + (_lastModTime != null ? _lastModTime.hashCode() : 0);
        result = 31 * result + (_createTime != null ? _createTime.hashCode() : 0);
        result = 31 * result + (_site != null ? _site.hashCode() : 0);
        return result;
    }
}
