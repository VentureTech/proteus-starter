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

package com.example.app.ui;

import com.example.app.model.DemoUserProfile;
import com.example.app.model.DemoUserProfileDAO;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collections;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.resource.ClassPathResourceLibraryHelper;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.management.nav.NavigationDestination;
import net.proteusframework.ui.miwt.HistoryImpl;
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUIAction;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;

import static com.example.app.ui.DemoUserProfileApplicationFunctions.*;
import static com.example.app.ui.UserProfileListingLOK.COMPONENT_NAME;

/**
 * User profile listing UI.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@I18NFile(
    symbolPrefix = "com.example.app.ui.UserProfileListing",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("UserProfile Management"))
    }
)
@ApplicationFunction(applicationName = APPLICATION_NAME, name = USER_PROFILE_LISTING,
    description = "Management UI For UserProfiles"
)
public class DemoUserProfileListing extends MIWTPageElementModelContainer implements SearchUIOperationHandler
{

    /** DAO. */
    @Autowired
    private DemoUserProfileDAO _demoUserProfileDAO;
    /** Service. */
    @Autowired
    private ClassPathResourceLibraryHelper _classPathResourceLibraryHelper;

    /**
     * Instantiates a new User profile listing.
     */
    public DemoUserProfileListing()
    {
        addClassName("user-profile-search");
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.UserManagement);
    }

    /**
     * Post construct.
     */
    @PostConstruct
    public void postConstruct()
    {
        setIcon(_classPathResourceLibraryHelper.createResource(ICON_PROFILE));
    }


    @Override
    public void init()
    {
        super.init();
        final SearchSupplierImpl searchSupplier = DemoUserProfileApp.createSearchSupplier();
        searchSupplier.setSearchUIOperationHandler(this);
        SearchUIImpl.Options options = new SearchUIImpl.Options("User Profile");
        options.setSearchOnPageLoad(true);

        // Add action using Navigation API
        NavigationAction addAction = CommonActions.ADD.navAction();
        addAction.configure().toPage(USER_PROFILE_EDITOR).usingNewEntity(URL_PROP_PROFILE);
        options.addEntityAction(addAction);

        options.addSearchSupplier(searchSupplier);
        options.setHistory(new HistoryImpl());
        SearchUIImpl searchUI = new SearchUIImpl(options);
        add(searchUI);
    }

    @Override
    public boolean supportsOperation(SearchUIOperation operation)
    {
        switch (operation)
        {

            case add:
            case edit:
            case delete:
            case view:
                return true;
            case select:
            case copy:
            default:
                return false;
        }
    }

    @Override
    public void handle(SearchUIOperationContext context)
    {
        // Alternatively, you could use a NavigationLinkColumn instead of an ActionColumn.
        switch (context.getOperation())
        {
            case view:
            {
                final NavigationDestination destination = new NavigationDestination(USER_PROFILE_VIEWER);
                destination.apply(Collections.singletonMap(URL_PROP_PROFILE, context.getData()));
                destination.actionPerformed(new ActionEvent(this, this, "view"));
                break;
            }
            case edit:
                //case add: /* handled by NavigationAction. */
            {
                final NavigationDestination destination = new NavigationDestination(USER_PROFILE_EDITOR);
                destination.apply(Collections.singletonMap(URL_PROP_PROFILE, context.getData()));
                destination.actionPerformed(new ActionEvent(this, this, "edit"));
                break;
            }
            case delete:
            {
                _demoUserProfileDAO.deleteUserProfile(context.<DemoUserProfile>getData());
                context.getSearchUI().doAction(SearchUIAction.search);
                break;
            }
            default:
                break;
        }
    }


    /**
     * Configure.
     *
     * @param parsedRequest the parsed request.
     */
    void configure(@SuppressWarnings("UnusedParameters") ParsedRequest parsedRequest)
    {
        // Nothing to configure
    }
}
