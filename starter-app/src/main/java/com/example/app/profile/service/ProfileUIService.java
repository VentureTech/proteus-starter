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

package com.example.app.profile.service;

import com.example.app.profile.model.company.Company;
import com.example.app.profile.model.company.CompanyDAO;
import com.example.app.profile.model.terminology.FallbackProfileTermProvider;
import com.example.app.profile.model.terminology.ProfileTermProvider;
import com.example.app.profile.model.user.User;
import com.example.app.profile.model.user.UserDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Nonnull;
import java.util.Optional;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.internet.http.Hostname;
import net.proteusframework.internet.http.SiteContext;

/**
 * Provides methods for Profile API UIS.  This service is SESSION scoped
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/12/17
 */
@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProfileUIService
{
    private static final Logger _logger = LogManager.getLogger(ProfileUIService.class);

    @Autowired private SiteContext _siteContext;
    @Autowired private CompanyDAO _companyDAO;
    @Autowired private UserDAO _userDAO;
    @Autowired private EntityRetriever _er;


    private Company _selectedCompany;
    private User _currentUser;
    private ProfileTermProvider _selectedCompanyTermProvider;

    /**
     * Gets the selected {@link Company} based on the current Hostname.
     * If no company exists for the current Hostname, then null is returned.
     *
     * @return the company for current user
     */
    public synchronized Company getSelectedCompany()
    {
        if(_selectedCompany == null)
        {
            //Get the Company from the hostname of the Request.
            Hostname hostname = _siteContext.getRequestedHostname();
            _selectedCompany = _companyDAO.getCompanyForHostname(hostname);
            if(_selectedCompany == null)
                _logger.error("Unable to determine Company for hostname: " + hostname.getName());
            return _selectedCompany;
        }
        else
        {
            return _er.reattachIfNecessary(_selectedCompany);
        }
    }

    /**
     * Sets the selected Company for current user.
     *
     * @param company the company
     */
    public synchronized void setSelectedCompany(@Nonnull Company company)
    {
        _selectedCompany = company;
        _selectedCompanyTermProvider = null;
    }

    /**
     * Gets company term provider.
     *
     * @return the company term provider.
     */
    public synchronized ProfileTermProvider getSelectedCompanyTermProvider()
    {
        if(_selectedCompanyTermProvider == null)
        {
            return _selectedCompanyTermProvider = new FallbackProfileTermProvider(
                Optional.ofNullable(getSelectedCompany()).map(Company::getProfileTerms).orElse(null));
        }
        else
        {
            return _selectedCompanyTermProvider;
        }
    }

    /**
     * Get the current user.
     *
     * @return the current user.
     */
    public synchronized User getCurrentUser()
    {
        if(_currentUser != null)
        {
            return _er.reattachIfNecessary(_currentUser);
        }
        else
        {
            return _currentUser = _userDAO.getAssertedCurrentUser();
        }
    }
}
