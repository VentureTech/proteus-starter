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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Date;

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
    private final static String SEQ = "userprofile_seq";
    /** Identifier. */
    private long _id;
    /** Name - we'll use some of the properties of this class. */
    private Name _name = new Name();
    /** Postal Address - we'll use some of the properties of this class. */
    private Address _postalAddress = new Address();
    /** Phone Number. */
    private String _phoneNumber="";
    /** Email Address. */
    private String _emailAddress="";
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

    /**
     * Create a new User Profile.
     */
    public UserProfile()
    {
        super();
    }

    /**
     * Copy constructor.
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
    }
    /**
     * Get the identifier.
     * @return the identifier.
     */
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.AUTO, generator = SEQ)
    @SequenceGenerator(name = SEQ, sequenceName = SEQ)
    @Column(name = "userprofile_id")
    public Long getId()
    {
        return _id;
    }

    /**
     * Set the identifier.
     * @param id the identifier.
     */
    public void setId(Long id)
    {
        _id = id;
    }

    /**
     * Get the name.
     * @return the name.
     */
    @ManyToOne
    public Name getName()
    {
        return _name;
    }

    /**
     * Set the name.
     * @param name the name.
     */
    public void setName(Name name)
    {
        if(name == null)
            _name = new Name();
        else
            _name = name;
    }

    /**
     * Get the postal address.
     * @return the postal address.
     */
    @ManyToOne
    public Address getPostalAddress()
    {
        return _postalAddress;
    }

    /**
     * Set the postal address.
     * @param postalAddress the postal address.
     */
    public void setPostalAddress(Address postalAddress)
    {
        if(postalAddress == null)
            _postalAddress = new Address();
        else
            _postalAddress = postalAddress;
    }

    /**
     * Get the phone number.
     * @return the phone number.
     */
    public String getPhoneNumber()
    {
        return _phoneNumber;
    }

    /**
     * Set the phone number.
     * @param phoneNumber the phone number.
     */
    public void setPhoneNumber(String phoneNumber)
    {
        if(phoneNumber == null)
            _phoneNumber = "";
        else
            _phoneNumber = phoneNumber;
    }

    /**
     * Get the email address.
     * @return the email address.
     */
    public String getEmailAddress()
    {
        return _emailAddress;
    }

    /**
     * Set the email address.
     * @param emailAddress the email address.
     */
    public void setEmailAddress(String emailAddress)
    {
        if(emailAddress == null)
            _emailAddress = "";
        else
            _emailAddress = emailAddress;
    }

    /**
     * Get the Twitter link.
     * @return the Twitter link. May be null.
     */
    public URL getTwitterLink()
    {
        return _twitterLink;
    }

    /**
     * Set the Twitter link.
     * @param twitterLink the Twitter link.
     */
    public void setTwitterLink(URL twitterLink)
    {
        _twitterLink = twitterLink;
    }

    /**
     * Get the Facebook link.
     * @return the Facebook link. May be null.
     */
    public URL getFacebookLink()
    {
        return _facebookLink;
    }

    /**
     * Set the Facebook link.
     * @param facebookLink the Facebook link.
     */
    public void setFacebookLink(URL facebookLink)
    {
        _facebookLink = facebookLink;
    }

    /**
     * Get the LinkedIn link.
     * @return the LinkedIn link. May be null.
     */
    public URL getLinkedInLink()
    {
        return _linkedInLink;
    }

    /**
     * Set the LinkedIn link.
     * @param linkedInLink the LinkedIn link.
     */
    public void setLinkedInLink(URL linkedInLink)
    {
        _linkedInLink = linkedInLink;
    }

    /**
     * Get the about me video link.
     * @return the about me video link. May be null.
     */
    public URL getAboutMeVideoLink()
    {
        return _aboutMeVideoLink;
    }

    /**
     * Set the about me video link.
     * @param aboutMeVideoLink the about me video link.
     */
    public void setAboutMeVideoLink(URL aboutMeVideoLink)
    {
        _aboutMeVideoLink = aboutMeVideoLink;
    }

    /**
     * Get the about me prose.
     * @return the about me prose.
     */
    public String getAboutMeProse()
    {
        return _aboutMeProse;
    }

    /**
     * Set the about me prose.
     * @param aboutMeProse the about me prose.
     */
    public void setAboutMeProse(String aboutMeProse)
    {
        if(aboutMeProse == null)
            _aboutMeProse = "";
        else
            _aboutMeProse = aboutMeProse;
    }

    /**
     * Get the picture.
     * @return the picture.
     */
    @ManyToOne
    public FileEntity getPicture()
    {
        return _picture;
    }

    /**
     * Set the picture.
     * @param picture the picture.
     */
    public void setPicture(FileEntity picture)
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
}
