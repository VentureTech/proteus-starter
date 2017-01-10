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

import com.example.app.profile.model.user.UserDAO;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.support.ui.Application;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelPropertyViewer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.Action;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.RendererEditorState;

import static com.example.app.profile.ui.user.MyAccountViewLOK.COMPONENT_NAME;
import static com.example.app.profile.ui.user.MyAccountViewLOK.LABEL_UPDATE_PASSWORD;

/**
 * Component for the My Account view page
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/8/15 2:22 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.user.MyAccountView",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("My Account View")),
        @I18N(symbol = "Label Update Password", l10n = @L10N("Update Password"))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.User.MY_ACCOUNT_VIEW,
    description = "UI for a user to view their account"
)
public class MyAccountView extends MIWTPageElementModelPropertyViewer
{
    private final MessageContainer _messages = new MessageContainer(35_000L);

    @Autowired private UserDAO _userDAO;
    @Autowired private MyAccountPermissionCheck _permissionCheck;

    /**
     * Instantiate a new instance of MyAccountView
     */
    public MyAccountView()
    {
        super();

        setName(COMPONENT_NAME());
        addCategory(CmsCategory.UserManagement);
        addClassName("my-account-view");
        setHTMLElement(HTMLElement.section);
    }

    @Override
    public void preRenderProcess(Request request, Response response, RendererEditorState<?> state)
    {
        super.preRenderProcess(request, response, state);
        UserValueViewer valueViewer = getValueViewer();
        if (valueViewer != null)
        {
            valueViewer.setUser(_userDAO.getAssertedCurrentUser());
        }
    }    @Override
    public void init()
    {
        super.init();
        super.lazyInit();

        NavigationAction edit = CommonActions.EDIT.navAction();
        edit.configure().toPage(ApplicationFunctions.User.MY_ACCOUNT_EDIT).usingCurrentURLData().withSourceComponent(this);
        edit.setTarget(this, "close");

        UserValueViewer valueViewer = getValueViewer();

        if (valueViewer != null && valueViewer.canUpdatePassword())
        {
            ReflectiveAction updatePassword = new ReflectiveAction();
            updatePassword.prop(Action.NAME, LABEL_UPDATE_PASSWORD());
            updatePassword.setActionListener(ev -> valueViewer.beginUpdatePassword());

            setPersistenceActions(edit, updatePassword);
        }
        else
        {
            setPersistenceActions(edit);
        }

        moveToTop(_messages);
    }

    @SuppressWarnings("unused")
        //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        _permissionCheck.checkPermissionsForCurrent(Event.getRequest(), "You don't have a User account in the system.");
        UserValueViewer valueViewer = new UserValueViewer();
        valueViewer.setUser(_userDAO.getAssertedCurrentUser());
        valueViewer.setNotifiable(_messages);
        setValueViewer(valueViewer);
    }    @Nullable
    @Override
    public UserValueViewer getValueViewer()
    {
        return (UserValueViewer) super.getValueViewer();
    }




}
