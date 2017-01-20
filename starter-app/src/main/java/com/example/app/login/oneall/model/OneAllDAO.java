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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.Objects;

import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.users.model.AuthenticationDomain;
import net.proteusframework.users.model.Credentials;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.SSOCredentials;
import net.proteusframework.users.model.SSOType;
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
     * Gets principal for user token.
     *
     * @param userToken the user token
     * @param domains the domains
     *
     * @return the principal for user token
     */
    @Nullable
    public Principal getPrincipalForUserToken(String userToken, AuthenticationDomain... domains)
    {
        return _principalDAO.getPrincipalBySSO(SSOType.other, userToken, "oneall", domains);
    }

    /**
     * Gets user token for principal.
     *
     * @param principal the principal
     * @param domains the domains
     *
     * @return the user token for principal
     */
    @Nullable
    public String getUserTokenForPrincipal(Principal principal, AuthenticationDomain... domains)
    {
        assert domains != null;
        StringBuilder hql = new StringBuilder();
        hql.append("select cred from ").append(Principal.class.getName()).append(" p inner join p.credentials cred ")
            .append(" inner join p.authenticationDomains auth ")
            .append(" where auth.id = :authId and cred.SSOType = :ssoType and p.id = :principalId and ");
        hql.append("cred.otherType = :otherType");
        return doInTransaction(session -> {
            Query query = getSession().createQuery(hql.toString());
            query.setParameter("ssoType", SSOType.other);
            query.setParameter("principalId", principal.getId());
            query.setParameter("otherType", "oneall");
            for (AuthenticationDomain ad : domains)
            {
                if(ad == null) continue;
                query.setLong("authId", ad.getId());
                SSOCredentials creds = (SSOCredentials) query.uniqueResult();
                if(creds != null)
                    return creds.getSSOId();
            }
            return null;
        });
    }

    /**
     * Create one all credential.
     *
     * @param principal the principal
     * @param userToken the user token
     */
    public void createOneAllCredential(Principal principal, String userToken)
    {
        if(hasOneAllCredential(principal)) return;
        SSOCredentials ssoCred = new SSOCredentials();
        ssoCred.setSSOType(SSOType.other);
        ssoCred.setOtherType("oneall");
        ssoCred.setSSOId(userToken);
        principal.getCredentials().add(ssoCred);
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
     * Has one all credential boolean.
     *
     * @param principal the principal
     *
     * @return the boolean
     */
    public boolean hasOneAllCredential(Principal principal)
    {
        for(Credentials c : principal.getCredentials())
        {
            c = _er.narrowProxyIfPossible(c);
            if(c instanceof SSOCredentials)
            {
                final SSOCredentials ssoCredentials = (SSOCredentials) c;
                if(ssoCredentials.getSSOType() == SSOType.other
                   && Objects.equals(ssoCredentials.getOtherType(), "oneall")) return true;
            }
        }
        return false;
    }
}
