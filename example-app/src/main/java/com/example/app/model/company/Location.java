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

package com.example.app.model.company;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import com.example.app.model.profile.Profile;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.EmailAddress;
import net.proteusframework.users.model.PhoneNumber;

import static net.proteusframework.core.locale.annotation.I18NFile.Visibility.PUBLIC;

/**
 * Represents a location for a company.
 *
 * @author Ken Logan (klogan@venturetech.net)
 */
@Entity
@Table(name = Location.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.PROFILE_DATA)
@Audited
@DiscriminatorValue("location")
@I18NFile(
    symbolPrefix = "com.example.app.model.company.Location",
    classVisibility = PUBLIC,
    i18n = {
        @I18N(symbol = "Certification Info Prop Name", l10n = @L10N("Certification or Qualification Information")),
        @I18N(symbol = "Company Prop Name", l10n = @L10N("Company")),
        @I18N(symbol = "Location Name Prop Name", l10n = @L10N("Plant Location")),
        @I18N(symbol = "Payment Info Prop Name", l10n = @L10N("Payment Info (Sent to the Winner)")),
        @I18N(symbol = "USDA Plant ID Prop Name", l10n = @L10N("USDA Plant ID")),
        @I18N(symbol = "Email Address Prop Name", l10n = @L10N("Main Contact Email Address")),
        @I18N(symbol = "Unsold Recipients Prop Name", l10n = @L10N("Unsold Recipients")),
        @I18N(symbol = "Sold Recipients Prop Name", l10n = @L10N("Sold Recipients")),
        @I18N(symbol = "Address Prop Name", l10n = @L10N("Address")),
        @I18N(symbol = "Phone Prop Name", l10n = @L10N("Main Contact Phone"))
    }
)
public class Location extends Profile
{
    /** The database table name */
    public static final String TABLE_NAME = "Location";
    /** The property: address */
    public static final String ADDRESS_PROP = "address";
    /** The property: company */
    public static final String COMPANY_PROP = "company";
    /** The property: emailAddress */
    public static final String EMAIL_ADDRESS_PROP = "emailAddress";
    /** The property: phoneNumber */
    public static final String PHONE_NUMBER_PROP = "phoneNumber";
    /** The property: status */
    public static final String STATUS_PROP = "status";

    /** Serial id */
    private static final long serialVersionUID = 4763895118879179540L;

    /** Status */
    private LocationStatus _status = LocationStatus.ACTIVE;

    /** Address */
    private Address _address;

    /** Primary Email */
    private EmailAddress _emailAddress;

    /** Primary Phone */
    private PhoneNumber _phoneNumber;

    /** Company */
    private Company _company;

    /**
     * Default constructor
     */
    public Location()
    {
        super();
    }

    /**
     * Get the address
     *
     * @return the address
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    public Address getAddress()
    {
        return _address;
    }

    /**
     * Set the address
     *
     * @param address the address
     */
    public void setAddress(Address address)
    {
        _address = address;
    }

    /**
     * Get the {@link Company} this location belongs to
     *
     * @return the company
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    public Company getCompany()
    {
        return _company;
    }

    /**
     * Set the company
     *
     * @param company the company
     */
    public void setCompany(Company company)
    {
        _company = company;
    }

    /**
     * Get primary contact email address
     *
     * @return the primary email address
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    public EmailAddress getEmailAddress()
    {
        return _emailAddress;
    }

    /**
     * Set the email address
     *
     * @param emailAddress the email address
     */
    public void setEmailAddress(EmailAddress emailAddress)
    {
        _emailAddress = emailAddress;
    }

    /**
     * Get the phone number
     *
     * @return the phone number
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    public PhoneNumber getPhoneNumber()
    {
        return _phoneNumber;
    }

    /**
     * Set the phone number
     *
     * @param phoneNumber the phone number
     */
    public void setPhoneNumber(PhoneNumber phoneNumber)
    {
        _phoneNumber = phoneNumber;
    }

    /**
     * Get the status
     *
     * @return the status
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    public LocationStatus getStatus()
    {
        return _status;
    }

    /**
     * Set the status
     *
     * @param status the status
     */
    public void setStatus(LocationStatus status)
    {
        _status = status;
    }

}
