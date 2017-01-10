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
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelPropertyViewer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.resource.ClassPathResourceLibraryHelper;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.util.CommonActions;

import static com.example.app.ui.DemoUserProfileApplicationFunctions.*;
import static com.example.app.ui.DemoUserProfilePropertyViewerLOK.COMPONENT_NAME;

/**
 * PropertyViewer for UserProfile.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@I18NFile(
    symbolPrefix = "com.example.app.ui.UserProfilePropertyViewer",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("User Profile Viewer"))
    }
)
@ApplicationFunction(
    applicationName = APPLICATION_NAME,
    name = USER_PROFILE_VIEWER,
    description = "Viewer For UserProfiles",
    urlConfigName = USER_PROFILE_URL_CONFIG
)
public class DemoUserProfilePropertyViewer extends MIWTPageElementModelPropertyViewer
{
    /** Service. */
    @Autowired
    private ClassPathResourceLibraryHelper _classPathResourceLibraryHelper;

    /**
     * Instantiates a new User profile property viewer.
     */
    public DemoUserProfilePropertyViewer()
    {
        addClassName("user-profile-viewer");
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.UserManagement);
    }

    @Override
    public void init()
    {
        super.init();

        NavigationAction editAction = CommonActions.EDIT.navAction();
        editAction.configure().toPage(USER_PROFILE_EDITOR).usingCurrentURLData().withSourceComponent(this);
        NavigationAction cancelAction = CommonActions.CANCEL.navAction();
        cancelAction.configure().toReturnPath(USER_PROFILE_LISTING).usingCurrentURLData().withSourceComponent(this);
        //noinspection ThisEscapedInObjectConstruction
        cancelAction.setTarget(this, "close");
        //noinspection ThisEscapedInObjectConstruction
        editAction.setTarget(this, "close");
        setPersistenceActions(editAction, cancelAction);
    }

    /**
     * Post construct.
     */
    @PostConstruct
    public void postConstruct()
    {
        setIcon(_classPathResourceLibraryHelper.createResource(ICON_PROFILE));
    }

    /**
     * Configure the viewer.
     *
     * @param parsedRequest the parsed request.
     */
    void configure(ParsedRequest parsedRequest)
    {
        final DemoUserProfile profile = parsedRequest.getPropertyValue(URL_PROP_PROFILE);
        if (profile != null)
            setValueViewer(new DemoUserProfileViewer(profile));
        else
            setValueViewer(null);
    }
}
