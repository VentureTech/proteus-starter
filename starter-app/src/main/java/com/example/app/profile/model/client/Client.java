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

package com.example.app.profile.model.client;

import com.example.app.config.ProjectCacheRegions;
import com.example.app.config.ProjectConfig;
import com.example.app.profile.model.Profile;
import com.example.app.profile.model.company.Company;
import com.example.app.profile.model.location.Location;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.data.filesystem.FileEntity;

import static net.proteusframework.core.locale.annotation.I18NFile.Visibility.PUBLIC;

/**
 * Represents a client.
 *
 * @author Ken Logan (klogan@venturetech.net)
 */
@Entity
@Table(name = Client.TABLE_NAME, schema = ProjectConfig.PROJECT_SCHEMA)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = ProjectCacheRegions.PROFILE_DATA)
@Audited
@DiscriminatorValue("client")
@I18NFile(
    symbolPrefix = "com.example.app.profile.model.client.Client",
    classVisibility = PUBLIC,
    i18n = {
        @I18N(symbol = "Client Name Prop Name", l10n = @L10N("Client Name")),
        @I18N(symbol = "Email Address Prop Name", l10n = @L10N("Main Contact Email Address")),
        @I18N(symbol = "Address Prop Name", l10n = @L10N("Address")),
        @I18N(symbol = "Phone Prop Name", l10n = @L10N("Main Contact Phone")),
        @I18N(symbol = "Logo Prop Name", l10n = @L10N("Logo")),
    }
)
public class Client extends Profile
{
    /** The database table name */
    public static final String TABLE_NAME = "Client";
    /** The property: status */
    public static final String STATUS_PROP = "status";
    /** The property: logo */
    public static final String LOGO_PROP = "logo";
    /** The property: locations */
    public static final String LOCATIONS_PROP = "locations";
    /** Joni Column for relationship between Location Client */
    public static final String LOCATIONS_JOIN_COLUMN = "location_id";
    /** Join table for relationship between Location and Client */
    public static final String LOCATIONS_JOIN_TABLE = TABLE_NAME + '_' + Location.TABLE_NAME;
    /** The property: primaryLocation */
    public static final String PRIMARY_LOCATION_PROP = "primaryLocation";
    /** The property: company */
    public static final String COMPANY_PROP = "company";
    /** Serial ID */
    private static final long serialVersionUID = 9008466730246583817L;

    private ClientStatus _status = ClientStatus.PENDING;
    private List<Location> _locations = new ArrayList<>();
    private Location _primaryLocation = new Location();
    private FileEntity _logo;
    private Company _company;

    /**
     * Default constructor
     */
    public Client()
    {
        super();
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
    public void setPrimaryLocation(@Nonnull Location primaryLocation)
    {
        _primaryLocation = primaryLocation;
    }

    /**
     * Get the client logo
     *
     * @return the logo
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Nullable
    public FileEntity getLogo()
    {
        return _logo;
    }

    /**
     * Set the client logo
     *
     * @param logo the logo
     */
    public void setLogo(@Nullable FileEntity logo)
    {
        _logo = logo;
    }

    /**
     * Get the status
     *
     * @return the status
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    public ClientStatus getStatus()
    {
        return _status;
    }

    /**
     * Set the status
     *
     * @param status the status
     */
    public void setStatus(ClientStatus status)
    {
        _status = status;
    }

    /**
     * Gets company.
     *
     * @return the company
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = Company.JOIN_COLUMN)
    @NotNull
    public Company getCompany()
    {
        return _company;
    }

    /**
     * Sets company.
     *
     * @param company the company
     */
    public void setCompany(@Nonnull Company company)
    {
        _company = company;
    }
}
