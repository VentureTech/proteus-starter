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

package com.example.app.profile.ui.user;


import com.example.app.profile.model.Profile;
import com.example.app.profile.model.ProfileDAO;
import com.example.app.profile.model.user.User;
import com.example.app.profile.model.user.UserDAO;
import com.example.app.profile.service.MembershipOperationProvider;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.support.ui.UIPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.Action;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyViewer;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;

import static com.example.app.profile.ui.UIText.USER;
import static com.example.app.profile.ui.user.UserPropertyViewerLOK.ERROR_MESSAGE_INSUFFICIENT_PERMISSIONS_VIEW_FMT;
import static com.example.app.profile.ui.user.UserPropertyViewerLOK.LABEL_UPDATE_PASSWORD;
import static net.proteusframework.core.notification.NotificationImpl.error;

/**
 * {@link PropertyViewer} for {@link User}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/8/15 9:47 AM
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.user.UserPropertyViewer",
    i18n = {
        @I18N(symbol = "Error Message Insufficient Permissions View FMT",
            l10n = @L10N("You do not have the correct roles to view this {0}.")),
        @I18N(symbol = "Label Update Password", l10n = @L10N("Update Password"))
    }
)
@Configurable
public class UserPropertyViewer extends PropertyViewer
{
    private final MessageContainer _messages = new MessageContainer(35_000L);
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private MembershipOperationProvider _mop;
    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private UIPreferences _uiPreferences;

    private boolean _canEdit;

    /**
     * Instantiate a new instance of UserPropertyViewer
     */
    public UserPropertyViewer()
    {
        super();

        addClassName("user-viewer");
        setHTMLElement(HTMLElement.section);
    }

    void configure(@Nullable User user)
    {
        UserValueViewer viewer;
        if (user != null)
        {
            Profile adminProfile = _uiPreferences.getSelectedCompany();
            User currentUser = _userDAO.getAssertedCurrentUser();
            final TimeZone timeZone = Event.getRequest().getTimeZone();
            boolean canView = _profileDAO.canOperate(currentUser, adminProfile, timeZone, _mop.viewUser());
            if (!canView)
            {
                _messages.sendNotification(error(ERROR_MESSAGE_INSUFFICIENT_PERMISSIONS_VIEW_FMT(USER())));
                viewer = null;
            }
            else
            {
                viewer = new UserValueViewer();
                viewer.setUser(user);
                viewer.setNotifiable(_messages);

                _canEdit = _profileDAO.canOperate(currentUser, adminProfile, timeZone, _mop.modifyUser());
            }
        }
        else
        {
            viewer = null;
        }
        setValueViewer(viewer);
    }

    @Override
    public void init()
    {
        super.init();
        super.lazyInit();

        NavigationAction editAction = CommonActions.EDIT.navAction();
        editAction.configure().toPage(ApplicationFunctions.User.EDIT).usingCurrentURLData().withSourceComponent(this);
        editAction.setTarget(this, "close");
        UserValueViewer valueViewer = getValueViewer();

        List<Action> actions = new ArrayList<>();
        if (_canEdit)
            actions.add(editAction);
        if (valueViewer != null && valueViewer.canUpdatePassword())
        {
            ReflectiveAction updatePassword = new ReflectiveAction();
            updatePassword.prop(Action.NAME, LABEL_UPDATE_PASSWORD());
            updatePassword.setActionListener(ev -> valueViewer.beginUpdatePassword());
            actions.add(updatePassword);
        }

        setPersistenceActions(actions.toArray(new Action[actions.size()]));

        moveToTop(_messages);
    }

    @Nullable
    @Override
    public UserValueViewer getValueViewer()
    {
        return (UserValueViewer) super.getValueViewer();
    }



}
