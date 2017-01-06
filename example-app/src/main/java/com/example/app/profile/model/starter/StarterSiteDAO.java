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

package com.example.app.profile.model.starter;

import com.example.app.support.service.EntityIdCollector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import net.proteusframework.core.hibernate.dao.DAOHelper;
import net.proteusframework.users.config.UsersCacheRegions;
import net.proteusframework.users.model.Principal;

/**
 * Provides methods specific to the Starter Site for accessing data.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/3/17
 */
@Repository
@Lazy
public class StarterSiteDAO extends DAOHelper
{

    @Value("${admin-access-role}") private String _adminRoleProgId;

    /**
     * Needs setup boolean.
     *
     * @return the boolean
     */
    public boolean needsSetup()
    {
        return doInTransaction(session ->
            ((Long)session.createQuery("SELECT COUNT(c) FROM Company c")
            .setMaxResults(1)
            .uniqueResult()) < 1);
    }

    /**
     * Gets admins needing a user record.
     *
     * @return the admins needing user
     */
    public List<Principal> getAdminsNeedingUser()
    {
        return doInTransaction(session -> {
            @SuppressWarnings("unchecked")
            List<Principal> admins = (List<Principal>)
                session.createQuery("SELECT DISTINCT p FROM Principal p INNER JOIN p.children r\n"
                                    + "WHERE r.programmaticName = :adminRolePN")
                    .setCacheRegion(UsersCacheRegions.ROLE_QUERY)
                    .setCacheable(true)
                    .setParameter("adminRolePN", _adminRoleProgId)
                    .list();
            @SuppressWarnings("unchecked")
            List<Principal> adminsWithUser = (List<Principal>)
                session.createQuery("SELECT p FROM User u INNER JOIN u.principal p\n"
                                    + "WHERE p.id IN (:adminIds)")
                    .setCacheRegion(UsersCacheRegions.ROLE_QUERY)
                    .setCacheable(true)
                    .setParameterList("adminIds", admins.stream()
                        .map(Principal::getId)
                        .collect(new EntityIdCollector<Long>(() -> 0L)))
                    .list();
            return admins.stream().filter(a -> !adminsWithUser.contains(a)).collect(Collectors.toList());
        });
    }
}
