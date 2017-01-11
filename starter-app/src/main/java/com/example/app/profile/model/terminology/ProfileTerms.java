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

package com.example.app.profile.model.terminology;

import org.hibernate.annotations.Cache;
import org.hibernate.envers.Audited;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import net.proteusframework.core.hibernate.model.AbstractEntity;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.TextSource;

import static com.example.app.config.ProjectCacheRegions.PROFILE_DATA;
import static com.example.app.config.ProjectConfig.PROJECT_SCHEMA;
import static org.hibernate.annotations.CacheConcurrencyStrategy.NONSTRICT_READ_WRITE;

/**
 * Profile term entity.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@SuppressWarnings("unused")
@Entity
@Audited
@Access(AccessType.FIELD)
@Table(name = ProfileTerms.TABLE_NAME, schema = PROJECT_SCHEMA)
@Cache(usage = NONSTRICT_READ_WRITE, region = PROFILE_DATA)
public class ProfileTerms extends AbstractEntity<Integer> implements ProfileTermProvider
{
    private static final long serialVersionUID = -5712956850947878150L;
    /**
     * The constant TABLE_NAME.
     */
    public static final String TABLE_NAME = "profileterms";
    /**
     * The constant ID_COLUMN.
     */
    public static final String ID_COLUMN = "profileterms_id";
    private static final String GENERATOR = PROJECT_SCHEMA + '.' + ID_COLUMN + "_seq";

    private LocalizedObjectKey company;

    private LocalizedObjectKey companies;

    private LocalizedObjectKey client;

    private LocalizedObjectKey clients;

    private LocalizedObjectKey location;

    private LocalizedObjectKey locations;

    @Override
    public TextSource company()
    {
        return company;
    }

    @Override
    public TextSource companies()
    {
        return companies;
    }

    @Override
    public TextSource client()
    {
        return client;
    }

    @Override
    public TextSource clients()
    {
        return clients;
    }

    @Override
    public TextSource location()
    {
        return location;
    }

    @Override
    public TextSource locations()
    {
        return locations;
    }

    @Id
    @Column(name = ID_COLUMN)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(name = GENERATOR, sequenceName = GENERATOR)
    @NotNull
    @Override
    @Access(AccessType.PROPERTY)
    public Integer getId()
    {
        return super.getId();
    }

    /**
     * Gets company.
     *
     * @return the company
     */
    public LocalizedObjectKey getCompany()
    {
        return company;
    }

    /**
     * Sets company.
     *
     * @param pCoachingEntity the company
     */
    public void setCompany(LocalizedObjectKey pCoachingEntity)
    {
        company = pCoachingEntity;
    }

    /**
     * Gets company.
     *
     * @return the company
     */
    public LocalizedObjectKey getCompanies()
    {
        return companies;
    }

    /**
     * Sets company.
     *
     * @param pCompanies the companies
     */
    public void setCompanies(LocalizedObjectKey pCompanies)
    {
        companies = pCompanies;
    }

    /**
     * Gets client.
     *
     * @return the client
     */
    public LocalizedObjectKey getClient()
    {
        return client;
    }

    /**
     * Sets client.
     *
     * @param pClient the pclient
     */
    public void setClient(LocalizedObjectKey pClient)
    {
        client = pClient;
    }

    /**
     * Gets clients.
     *
     * @return the clients
     */
    public LocalizedObjectKey getClients()
    {
        return clients;
    }

    /**
     * Sets clients.
     *
     * @param pClients the clients
     */
    public void setClients(LocalizedObjectKey pClients)
    {
        clients = pClients;
    }

    /**
     * Gets location.
     *
     * @return the location
     */
    public LocalizedObjectKey getLocation()
    {
        return location;
    }

    /**
     * Sets location.
     *
     * @param pLocation the location
     */
    public void setLocation(LocalizedObjectKey pLocation)
    {
        location = pLocation;
    }

    /**
     * Gets locations.
     *
     * @return the locations
     */
    public LocalizedObjectKey getLocations()
    {
        return locations;
    }

    /**
     * Sets locations.
     *
     * @param pLocations the locations
     */
    public void setLocations(LocalizedObjectKey pLocations)
    {
        locations = pLocations;
    }
}
