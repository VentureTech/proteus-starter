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

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.cms.component.miwt.impl.MIWTPageElementModelPropertyEditor;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.resource.ClassPathResourceLibraryHelper;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigDef;
import net.proteusframework.ui.management.URLProperty;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.util.CommonActions;

import static com.example.app.ui.DemoUserProfileApplicationFunctions.*;
import static com.example.app.ui.DemoUserProfilePropertyEditorLOK.COMPONENT_NAME;

/**
 * PropertyEditor for UserProfile.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@I18NFile(
    symbolPrefix = "com.example.app.ui.UserProfilePropertyEditor",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("User Profile Editor"))
    }
)
@ApplicationFunction(
    applicationName = APPLICATION_NAME,
    name = USER_PROFILE_EDITOR,
    description = "Editor For UserProfiles",
    urlConfigDef = @URLConfigDef(
        name = USER_PROFILE_URL_CONFIG,
        properties = {@URLProperty(name = URL_PROP_PROFILE, type = DemoUserProfile.class)},
        pathInfoPattern = "/{profile}"
    )
)
public class DemoUserProfilePropertyEditor extends MIWTPageElementModelPropertyEditor<DemoUserProfile>
{
    /** DAO. */
    @Autowired
    private DemoUserProfileDAO _demoUserProfileDAO;
    /** Service. */
    @Autowired
    private ClassPathResourceLibraryHelper _classPathResourceLibraryHelper;

    /**
     * Instantiates a new User profile property editor.
     */
    public DemoUserProfilePropertyEditor()
    {
        super(new DemoUserProfileEditor());
        addClassName("user-profile-editor");
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

        NavigationAction saveAction = CommonActions.SAVE.navAction();
        saveAction.onCondition(input ->
                persist(userProfile -> {
                    assert userProfile != null;
                    _demoUserProfileDAO.saveUserProfile(userProfile);
                    return Boolean.TRUE;
                })
        );
        saveAction.configure().toReturnPath(USER_PROFILE_LISTING).usingCurrentURLData().withSourceComponent(this);
        NavigationAction cancelAction = CommonActions.CANCEL.navAction();
        cancelAction.configure().toReturnPath(USER_PROFILE_LISTING).usingCurrentURLData().withSourceComponent(this);
        cancelAction.setTarget(this, "close");
        saveAction.setTarget(this, "close");
        setPersistenceActions(saveAction, cancelAction);
    }

    /**
     * Configure the UI.
     *
     * @param parsedRequest the parsed request.
     */
    void configure(ParsedRequest parsedRequest)
    {
        getValueEditor().setValue(parsedRequest.getPropertyValue(URL_PROP_PROFILE));
    }
}
