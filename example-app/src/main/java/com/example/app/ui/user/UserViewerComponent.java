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


import com.example.app.model.profile.Profile;
import com.example.app.model.profile.ProfileDAO;
import com.example.app.model.user.User;
import com.example.app.model.user.UserDAO;
import com.example.app.service.MembershipOperationProvider;
import com.example.app.service.ProfileService;
import com.example.app.support.AppUtil;
import com.example.app.ui.Application;
import com.example.app.ui.ApplicationFunctions;
import com.example.app.ui.UIPreferences;
import com.example.app.ui.UIText;
import com.example.app.ui.URLConfigurations;
import com.example.app.ui.URLProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigDef;
import net.proteusframework.ui.management.URLProperty;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.TabItemDisplay;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.component.composite.TabbedContainerImpl;
import net.proteusframework.ui.miwt.component.event.ComponentAdapter;
import net.proteusframework.ui.miwt.component.event.ComponentEvent;
import net.proteusframework.ui.miwt.util.CommonActions;

import static com.example.app.ui.user.UserViewerComponentLOK.*;

/**
 * Viewer component for {@link User}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/9/15 11:32 AM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.UserViewerComponent",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("User Viewer")),
        @I18N(symbol = "UI Heading Format", l10n = @L10N("User Account for {0}")),
        @I18N(symbol = "Tab Profile", l10n = @L10N("Profile")),
        @I18N(symbol = "Tab Role Assignments", l10n = @L10N("Role Assignments"))
    }
)
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.User.VIEW,
    description = "Viewer for User",
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.User.VIEW,
        properties = {
            @URLProperty(name = URLProperties.USER, type = User.class)
        },
        pathInfoPattern = "/{" + URLProperties.USER + '}'
    )
)
public class UserViewerComponent extends MIWTPageElementModelContainer
{
    @Autowired
    private ProfileService _profileService;
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private MembershipOperationProvider _mop;
    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private UIPreferences _uiPreferences;

    private User _user;
    private Profile _userProfile;
    private final MessageContainer _messages = new MessageContainer(35_000L);

    /**
     *   Instantiate a new instance of UserViewerComponent
     */
    public UserViewerComponent()
    {
        super();
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);

        addClassName("user-viewer");
    }

    @Override
    public void init()
    {
        super.init();

        _uiPreferences.consumeMessages().forEach(_messages::sendNotification);

        NavigationAction backAction = CommonActions.BACK.navAction();
        backAction.configure().toPage(ApplicationFunctions.User.MANAGEMENT).usingCurrentURLData().withSourceComponent(this);
        backAction.addClassName("back");

        final TabItemDisplay profileTID = new TabItemDisplay(TAB_PROFILE());
        profileTID.addClassName("profile");
        final TabItemDisplay positionsTID = new TabItemDisplay(UIText.POSITIONS());
        positionsTID.addClassName("positions");
        final TabItemDisplay roleAssignTID = new TabItemDisplay(TAB_ROLE_ASSIGNMENTS());
        roleAssignTID.addClassName("roles");

        final UserPropertyViewer valueViewer = new UserPropertyViewer();
        valueViewer.configure(_user);
        valueViewer.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentClosed(ComponentEvent e)
            {
                queueClose();
            }
        });

        final UserPositionManagement positionManagement = new UserPositionManagement(_user);

        final UserMembershipManagement roleMgt = new UserMembershipManagement(_user, _userProfile);

        final TabbedContainerImpl tabs = new TabbedContainerImpl();
        tabs.addTab(profileTID, valueViewer);
        tabs.addTab(positionsTID, positionManagement);
        tabs.addTab(roleAssignTID, roleMgt);

        final Label manageUserLabel = new Label(TextSources.createText(UI_HEADING_FORMAT(), AppUtil.renderUser(_user)));
        manageUserLabel.setHTMLElement(HTMLElement.h3);
        add(manageUserLabel);

        add(of("actions nav-actions", new PushButton(backAction)));
        add(_messages);
        add(tabs);
    }

    @SuppressWarnings("unused") //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        _user = request.getPropertyValue(URLProperties.USER);
        if(_user == null)
            throw new IllegalArgumentException("Unable to determine User.");
        _userProfile = _profileService.getOwnerProfileForUser(_user)
            .orElseThrow(() -> new IllegalStateException("User must have an owner profile."));
        User currentUser = _userDAO.getAssertedCurrentUser();

        if(!_profileDAO.canOperate(currentUser, _userProfile, AppUtil.UTC, _mop.viewUser()))
            throw new IllegalArgumentException("Invalid Permissions To View Page");
    }
}
