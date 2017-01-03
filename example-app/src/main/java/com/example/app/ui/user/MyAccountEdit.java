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


import com.example.app.model.user.User;
import com.example.app.model.user.UserDAO;
import com.example.app.support.ContactUtil;
import com.example.app.ui.Application;
import com.example.app.ui.ApplicationFunctions;
import com.example.app.ui.UIPreferences;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Objects;
import java.util.prefs.Preferences;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelPropertyEditor;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.hibernate.HibernateSessionHelper;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Link;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.EmailAddress;
import net.proteusframework.users.model.PasswordCredentials;
import net.proteusframework.users.model.dao.NonUniqueCredentialsException;
import net.proteusframework.users.model.dao.PrincipalDAO;

import static com.example.app.ui.UIText.USER;
import static com.example.app.ui.user.MyAccountEditLOK.COMPONENT_NAME;
import static com.example.app.ui.user.MyAccountEditLOK.ERROR_MESSAGE_USERNAME_EXISTS_FMT;
import static net.proteusframework.core.notification.NotificationImpl.error;

/**
 * Component for the My Account edit page
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/22/15 3:15 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.ui.user.MyAccountEdit",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("My Account Edit")),
        @I18N(symbol = "Error Message Username Exists FMT",
            l10n = @L10N("Unable to save: {0} with given email address already exists."))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.User.MY_ACCOUNT_EDIT,
    description = "UI for a user to edit their account"
)
public class MyAccountEdit extends MIWTPageElementModelPropertyEditor<User>
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(MyAccountEdit.class);

    @Autowired private UserDAO _userDAO;
    @Autowired private PrincipalDAO _principalDAO;
    @Autowired @Qualifier(HibernateSessionHelper.RESOURCE_NAME) private HibernateSessionHelper _sessionHelper;
    @Autowired private UIPreferences _uiPreferences;
    @Autowired private MyAccountPermissionCheck _permissionCheck;

    /**
     * Instantiate a new instance of MyAccountEdit
     */
    public MyAccountEdit()
    {
        super(new UserValueEditor());

        setName(COMPONENT_NAME());
        addCategory(CmsCategory.UserManagement);
        addClassName("my-account-edit");
        setHTMLElement(HTMLElement.section);
    }

    @SuppressWarnings("unused")
        //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        _permissionCheck.checkPermissionsForCurrent("You don't have a User account in the system.");
        User currentUser = _userDAO.getAssertedCurrentUser();
        getValueEditor().setAuthDomains(_userDAO.getAuthenticationDomainsToSaveOnUserPrincipal(currentUser));
        getValueEditor().setAdminMode(false);
        getValueEditor().setValue(currentUser);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void init()
    {
        super.init();

        NavigationAction saveAction = CommonActions.SAVE.navAction();
        saveAction.onCondition(input ->
            persist(user -> {
                assert user != null : "User should not be null if you are persisting!";
                UserValueEditor editor = getValueEditor();
                User currentUser = _userDAO.getAssertedCurrentUser();
                _sessionHelper.beginTransaction();
                boolean success = false;
                try
                {
                    if (Objects.equals(currentUser.getId(), user.getId()))
                    {
                        user.setPreferredContactMethod(editor.commitValuePreferredContactMethod());

                        final Link link = editor.commitValueLoginLandingPage();
                        if (link != null)
                        {
                            Preferences userPref = Preferences.userRoot().node(User.LOGIN_PREF_NODE);
                            userPref.put(User.LOGIN_PREF_NODE_LANDING_PAGE, link.getURIAsString());
                        }
                    }

                    user = _userDAO.mergeUser(user);
                    try
                    {
                        EmailAddress emailAddress = ContactUtil.getEmailAddress(
                            user.getPrincipal().getContact(), ContactDataCategory.values())
                            .orElseThrow(() -> new IllegalStateException(
                                "Email Address was null on PrincipalValueEditor.  This should not happen."));


                        PasswordCredentials creds = user.getPrincipal().getPasswordCredentials();
                        creds.setUsername(emailAddress.getEmail());
                        _principalDAO.savePrincipal(user.getPrincipal());

                        user = _userDAO.mergeUser(user);

                        if (editor.getPictureEditor().getModificationState().isModified())
                        {
                            _userDAO.saveUserImage(user, editor.getPictureEditor().commitValue());
                        }

                        success = true;
                    }
                    catch (NonUniqueCredentialsException e)
                    {
                        _logger.error("Unable to persist changes to the Principal.", e);
                        getNotifiable().sendNotification(error(ERROR_MESSAGE_USERNAME_EXISTS_FMT(USER())));
                    }
                    _sessionHelper.commitTransaction();
                }
                finally
                {
                    if(!success)
                        _sessionHelper.recoverableRollbackTransaction();
                }
                return success;
            }));
        saveAction.configure().toReturnPath(ApplicationFunctions.User.MY_ACCOUNT_VIEW).usingCurrentURLData()
            .withSourceComponent(this);
        saveAction.setTarget(this, "close");

        NavigationAction cancelAction = CommonActions.CANCEL.navAction();
        cancelAction.configure().toReturnPath(ApplicationFunctions.User.MY_ACCOUNT_VIEW).usingCurrentURLData()
            .withSourceComponent(this);
        cancelAction.setTarget(this, "close");

        setPersistenceActions(saveAction, cancelAction);
    }

    @Override
    public UserValueEditor getValueEditor()
    {
        return (UserValueEditor) super.getValueEditor();
    }

    @Override
    public void setValueEditor(ValueEditor<User> valueEditor)
    {
        if (!(valueEditor instanceof UserValueEditor))
            throw new IllegalArgumentException("Given ValueEditor is not a UserValueEditor");
        super.setValueEditor(valueEditor);
    }


}
