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

package com.example.app.ui.user;

import com.example.app.model.profile.Membership;
import com.example.app.model.profile.MembershipOperation;
import com.example.app.model.profile.ProfileDAO;
import com.example.app.model.user.User;
import com.example.app.service.MembershipOperationProvider;
import com.example.app.support.AppUtil;
import com.example.app.terminology.ProfileTermProvider;
import com.example.app.ui.ApplicationFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.internet.http.Link;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ApplicationRegistry;
import net.proteusframework.ui.miwt.event.Event;

/**
 * Login Landing Links
 *
 * @author Alan Holt (tsasaki@venturetech.net)
 */
public enum LoginLandingLinks
{

    /** operation / user management */
    OP_USERS(
        ApplicationFunctions.User.MANAGEMENT,
        daos -> daos.mop.modifyUser(),
        (daos, lc) -> "Operation / " + daos.terms.users().getText(lc)
    ),

    /** configuration / resouce management */
    CNF_RESOURCES(
        ApplicationFunctions.ResourceRepositoryItem.MANAGEMENT,
        daos -> daos.mop.modifyRepositoryResources(),
        (daos, lc) -> "Configuration / " + daos.terms.resources().getText(lc)
    );

    @Configurable
    private static class DAOs
    {
        @Autowired
        ProfileDAO profileDAO;
        @Autowired
        MembershipOperationProvider mop;
        @Autowired
        ProfileTermProvider terms;
        @Autowired
        ApplicationRegistry applicationRegistry;
    }

    final private String _functionName;
    final private Function<DAOs, MembershipOperation> _operation;
    final private BiFunction<DAOs, LocaleContext, String> _label;

    /**
     * Get available links.
     *
     * @param currentUser the current user.
     * @param lc the locale context.
     *
     * @return the list of links.
     */
    static List<Link> getAvailableLinks(User currentUser, LocaleContext lc)
    {
        final DAOs daos = new DAOs();
        final List<Membership> memberships = daos.profileDAO.getMembershipsForUser(
            currentUser, "membership.lastModTime desc", AppUtil.UTC);
        return Arrays.stream(LoginLandingLinks.values())
            .map(availbaleLinks -> availbaleLinks.getLink(memberships, lc))
            .flatMap(link -> link.map(Stream::of).orElseGet(Stream::empty))
            .collect(Collectors.toList());


    }

    /**
     * Get the link.
     *
     * @param memberships Membership list.
     * @param lc the LocaleContext.
     *
     * @return the link.
     */
    public Optional<Link> getLink(List<Membership> memberships, LocaleContext lc)
    {
        DAOs daos = new DAOs();
        if (canAccessMenuLink(getOperation(daos), memberships))
        {
            ApplicationFunction func = daos.applicationRegistry.getApplicationFunctionByName(getFunctionName());
            Link link = daos.applicationRegistry.createLink(
                Event.getRequest(),
                Event.getResponse(),
                func,
                Collections.emptyMap());
            link.putAdditionalAttribute("label", getLabel(daos, lc));
            return Optional.of(link);
        }

        return Optional.empty();
    }

    private static boolean canAccessMenuLink(MembershipOperation mop, List<Membership> memberships)
    {
        if (memberships.isEmpty())
            return false;
        return memberships.stream().anyMatch(m -> m.getOperations().contains(mop));
    }

    private MembershipOperation getOperation(DAOs daos)
    {
        return _operation.apply(daos);
    }

    private String getFunctionName()
    {
        return _functionName;
    }

    private String getLabel(DAOs daos, LocaleContext lc)
    {
        return _label.apply(daos, lc);
    }

    LoginLandingLinks(String functionName, Function<DAOs, MembershipOperation> operation, BiFunction<DAOs, LocaleContext, String>
        label)
    {
        _functionName = functionName;
        _operation = operation;
        _label = label;
    }

}
