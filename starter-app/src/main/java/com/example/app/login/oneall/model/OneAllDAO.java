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

package com.example.app.login.oneall.model;

import com.example.app.login.oneall.service.OneAllLoginService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.users.model.AuthenticationDomain;
import net.proteusframework.users.model.OpenAuthCredentials;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.dao.AuthenticationDomainList;
import net.proteusframework.users.model.dao.NonUniqueCredentialsException;
import net.proteusframework.users.model.dao.PrincipalDAO;

/**
 * DAO For OneAll
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/20/17
 */
@Repository
public class OneAllDAO extends DAOHelper
{
    private static final Logger _logger = LogManager.getLogger(OneAllDAO.class);

    @Autowired private PrincipalDAO _principalDAO;
    @Autowired private EntityRetriever _er;

    /**
     * Gets registered providers.
     *
     * @param principal the principal
     *
     * @return the registered providers
     */
    public List<String> getRegisteredProviders(@Nullable Principal principal)
    {
        if(principal == null) return Collections.emptyList();
        return doInTransaction(session -> {
            return session.createQuery("SELECT distinct cred.openAuthSubType FROM Principal p\n"
                                       + "inner join p.credentials cred\n"
                                       + "where cred.openAuthType = :openAuthType\n"
                                       + "and p.id = :principalId")
                .setParameter("openAuthType", OneAllLoginService.SERVICE_IDENTIFIER)
                .setParameter("principalId", principal.getId())
                .list();
        });
    }

    /**
     * Gets principal for user token.
     *
     * @param userToken the user token
     * @param provider the provider
     * @param domains the domains.  If empty, will return any principal within the system that is linked to the given token
     *
     * @return the principal for user token
     */
    @Nullable
    public Principal getPrincipalForUserToken(@Nonnull String userToken, @Nonnull String provider, @Nonnull
        AuthenticationDomainList domains)
    {
        if(!domains.isEmpty())
        {
            return _principalDAO.getPrincipalByOpenAuth(OneAllLoginService.SERVICE_IDENTIFIER, userToken, provider, domains);
        }
        else
        {
            final String queryString =
                "select p from Principal p \n"
                + "inner join p.credentials cred \n"
                + "where cred.openAuthType = :openAuthType \n"
                + "and cred.openAuthId = :openAuthId \n"
                + "and cred.openAuthSubType = :openAuthSubType";
            return doInTransaction(session -> {
                Query query = getSession().createQuery(queryString);
                query.setParameter("openAuthType", OneAllLoginService.SERVICE_IDENTIFIER);
                query.setString("openAuthSubType", provider);
                query.setString("openAuthId", userToken);
                query.setMaxResults(1);
                return (Principal) query.uniqueResult();
            });
        }
    }

    /**
     * Gets user token for principal.
     *
     * @param principal the principal
     * @param provider the provider
     * @param domains the domains
     *
     * @return the user token for principal
     */
    @Nullable
    public String getUserTokenForPrincipal(Principal principal, String provider, @Nonnull AuthenticationDomainList domains)
    {
        final String queryString =
            "select cred from Principal p \n"
               + "inner join p.credentials cred \n"
               + "inner join p.authenticationDomains auth \n"
               + "where auth.id = :authId \n"
               + "and cred.openAuthType = :openAuthType \n"
               + "and p.id = :principalId \n"
               + "and cred.openAuthSubType = :openAuthSubType";
        return doInTransaction(session -> {
            Query query = getSession().createQuery(queryString);
            query.setParameter("openAuthType", OneAllLoginService.SERVICE_IDENTIFIER);
            query.setParameter("principalId", principal.getId());
            query.setParameter("openAuthSubType", provider);
            for (AuthenticationDomain ad : domains)
            {
                if(ad == null) continue;
                query.setLong("authId", ad.getId());
                OpenAuthCredentials creds = (OpenAuthCredentials) query.uniqueResult();
                if(creds != null)
                    return creds.getOpenAuthId();
            }
            return null;
        });
    }

    /**
     * Create one all credential.
     *
     * @param principal the principal
     * @param userToken the user token
     * @param provider the provider
     */
    public void createOneAllCredential(Principal principal, String userToken, String provider)
    {
        if(hasOneAllCredential(principal, provider)) return;
        OpenAuthCredentials cred = new OpenAuthCredentials();
        cred.setOpenAuthType(OneAllLoginService.SERVICE_IDENTIFIER);
        cred.setOpenAuthSubType(provider);
        cred.setOpenAuthId(userToken);
        principal.getCredentials().add(cred);
        try
        {
            _principalDAO.savePrincipal(principal);
        }
        catch(NonUniqueCredentialsException e)
        {
            _logger.error("Couldn't save principal when adding OneAll credential.", e);
        }
    }

    /**
     * Delete one all credential.
     *
     * @param principal the principal
     * @param provider the provider
     */
    public void deleteOneAllCredential(Principal principal, String provider)
    {
        OpenAuthCredentials creds = principal.getOpenAuthCredentials(OneAllLoginService.SERVICE_IDENTIFIER, provider);
        if(creds != null)
        {
            doInTransaction(session -> {
                principal.getCredentials().remove(creds);
                session.saveOrUpdate(principal);
                session.delete(creds);
            });
        }
    }

    /**
     * Has one all credential boolean.
     *
     * @param principal the principal
     * @param provider the provider
     *
     * @return the boolean
     */
    public boolean hasOneAllCredential(Principal principal, String provider)
    {
        return principal.getOpenAuthCredentials(OneAllLoginService.SERVICE_IDENTIFIER, provider) != null;
    }
}
