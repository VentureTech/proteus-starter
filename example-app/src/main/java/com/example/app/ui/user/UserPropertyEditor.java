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


import com.example.app.model.company.Company;
import com.example.app.model.company.CompanyDAO;
import com.example.app.model.company.SelectedCompanyTermProvider;
import com.example.app.model.profile.MembershipType;
import com.example.app.model.profile.Profile;
import com.example.app.model.profile.ProfileDAO;
import com.example.app.model.user.User;
import com.example.app.model.user.UserDAO;
import com.example.app.service.MembershipOperationProvider;
import com.example.app.support.AppUtil;
import com.example.app.support.ContactUtil;
import com.example.app.ui.Application;
import com.example.app.ui.ApplicationFunctions;
import com.example.app.ui.UIPreferences;
import com.example.app.ui.URLConfigurations;
import com.example.app.ui.URLProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.prefs.Preferences;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelPropertyEditor;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.hibernate.HibernateSessionHelper;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.core.notification.Notification;
import net.proteusframework.core.notification.NotificationImpl;
import net.proteusframework.core.notification.NotificationType;
import net.proteusframework.internet.http.Link;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigDef;
import net.proteusframework.ui.management.URLProperty;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.management.nav.config.CurrentURLPropertyValueResolver;
import net.proteusframework.ui.management.nav.config.PropertyValueResolverParameter;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.EmailAddress;
import net.proteusframework.users.model.PasswordCredentials;
import net.proteusframework.users.model.dao.NonUniqueCredentialsException;
import net.proteusframework.users.model.dao.PrincipalDAO;

import static com.example.app.ui.UIText.USER;
import static com.example.app.ui.user.UserPropertyEditorLOK.*;
import static net.proteusframework.core.notification.NotificationImpl.error;

/**
 * {@link PropertyEditor} for {@link User}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/7/15 1:30 PM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.UserPropertyEditor",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("User Editor")),
        @I18N(symbol = "Error Message Username Exists FMT",
            l10n = @L10N("Unable to save: {0} with given email address already exists.")),
        @I18N(symbol = "Error Insufficient Permissions FMT", l10n = @L10N("You do not have the correct roles to modify this {0}")),
        @I18N(symbol = "Info Should Pick One Role New User", l10n = @L10N(
            "Suggestion:  Pick at least one role for this new user from the Role Assignments tab.")),

    }
)
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.User.EDIT,
    description = "Editor for User",
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.User.EDIT,
        properties = {
            @URLProperty(name = URLProperties.USER, type = User.class),
            @URLProperty(name = URLProperties.PROFILE, type = Profile.class)
        },
        pathInfoPattern = "/{" + URLProperties.USER + "}/{" + URLProperties.PROFILE + '}'
    )
)
public class UserPropertyEditor extends MIWTPageElementModelPropertyEditor<User>
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(UserPropertyEditor.class);
    boolean _newUser;
    private final List<Notification> _notifications = new ArrayList<>();
    @Autowired
    @Qualifier(HibernateSessionHelper.RESOURCE_NAME)
    private HibernateSessionHelper _sessionHelper;
    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private PrincipalDAO _principalDAO;
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private MembershipOperationProvider _mop;
    @Autowired
    private EntityRetriever _er;
    @Autowired
    private AppUtil _appUtil;
    @Autowired
    private SelectedCompanyTermProvider _terms;
    @Autowired
    private UIPreferences _uiPreferences;
    @Autowired
    private CompanyDAO _companyDAO;
    private User _saved;

    /**
     * Instantiate a new instance of UserPropertyEditor
     */
    public UserPropertyEditor()
    {
        super(new UserValueEditor());
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
        addClassName("user-editor");
        setHTMLElement(HTMLElement.section);
    }

    @SuppressWarnings("unused")
        //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        User value = request.getPropertyValue(URLProperties.USER);
        _newUser = value == null || value.getId() == null || value.getId() < 1;
        getValueEditor().setAuthDomains(_userDAO.getAuthenticationDomainsToSaveOnUserPrincipal(value));
        Company profile = _uiPreferences.getSelectedCompany();
        User currentUser = _userDAO.getAssertedCurrentUser();
        final TimeZone timeZone = Event.getRequest().getTimeZone();
        if (!_profileDAO.canOperate(currentUser, profile, timeZone, _mop.viewUser())
            || !_profileDAO.canOperate(currentUser, profile, timeZone, _mop.modifyUser()))
        {
            getValueEditor().setEditable(false);
            _notifications.add(error(ERROR_INSUFFICIENT_PERMISSIONS_FMT(USER())));
        }
        else
        {
            getValueEditor().setValue(value);
        }
        setSaved(value);
    }    /**
     * Set the saved User to be used for constructing URL properties after saving the User
     *
     * @param saved the persisted User
     */
    public void setSaved(@Nullable User saved)
    {
        _saved = _er.narrowProxyIfPossible(saved);
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
                        boolean result = true;
                        EmailAddress emailAddress = ContactUtil.getEmailAddress(
                            user.getPrincipal().getContact(), ContactDataCategory.values())
                            .orElseThrow(() -> new IllegalStateException(
                                "Email Address was null on PrincipalValueEditor.  This should not happen."));
                        if (user.getPrincipal().getPasswordCredentials() == null)
                        {
                            String randomPassword = UUID.randomUUID().toString();

                            List<Notification> notifications = new ArrayList<>();

                            result = _principalDAO.setNewPassword(user.getPrincipal(), emailAddress.getEmail(),
                                notifications, randomPassword);

                            if (result)
                            {
                                user.setPrincipal(_er.reattachIfNecessary(user.getPrincipal()));

                                user.getPrincipal().getCredentials().forEach(cred -> {
                                    if (_er.narrowProxyIfPossible(cred) instanceof PasswordCredentials)
                                    {
                                        Calendar cal = Calendar.getInstance();
                                        cal.add(Calendar.DAY_OF_YEAR, -10);
                                        cred.setExpireDate(cal.getTime());
                                    }
                                });

                                _principalDAO.savePrincipal(user.getPrincipal());
                            }
                            else
                            {
                                final Notifiable notifiable = getNotifiable();
                                notifications.forEach(notifiable::sendNotification);
                            }
                        }
                        else
                        {
                            PasswordCredentials creds = user.getPrincipal().getPasswordCredentials();
                            creds.setUsername(emailAddress.getEmail());
                            _principalDAO.savePrincipal(user.getPrincipal());
                        }
                        if (!_principalDAO.getAllRoles(user.getPrincipal()).contains(_appUtil.getFrontEndAccessRole()))
                        {
                            user.getPrincipal().getChildren().add(_appUtil.getFrontEndAccessRole());
                            _principalDAO.savePrincipal(user.getPrincipal());
                        }
                        user = _userDAO.mergeUser(user);
                        Company userProfile = _uiPreferences.getSelectedCompany();
                        if (userProfile != null && !userProfile.getUsers().contains(user) && _newUser)
                        {
                            _companyDAO.addUserToCompany(userProfile, user);

                            List<MembershipType> coachingMemTypes = editor.commitValueCoachingMemType();
                            final User finalUser = user;
                            coachingMemTypes.forEach(coachingMemType ->
                                _profileDAO.saveMembership(_profileDAO.createMembership(userProfile, finalUser, coachingMemType,
                                    ZonedDateTime.now(getSession().getTimeZone().toZoneId()), true)));
                        }

                        if (editor.getPictureEditor().getModificationState().isModified())
                        {
                            _userDAO.saveUserImage(user, editor.getPictureEditor().commitValue());
                        }

                        success = result;
                        if (success)
                        {
                            setSaved(user);
                        }
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
                    if (!success)
                        _sessionHelper.recoverableRollbackTransaction();
                }
                if (success)
                {
                    _uiPreferences.addMessage(new NotificationImpl(NotificationType.INFO, INFO_SHOULD_PICK_ONE_ROLE_NEW_USER()));
                }
                return success;
            }));
        saveAction.configure().toPage(ApplicationFunctions.User.VIEW)
            .withSourceComponent(this);
        saveAction.setPropertyValueResolver(new CurrentURLPropertyValueResolver()
        {
            @Override
            public Map<String, Object> resolve(PropertyValueResolverParameter parameter)
            {
                Map<String, Object> map = super.resolve(parameter);
                map.put(URLProperties.USER, _saved);
                return map;
            }
        });
        saveAction.setTarget(this, "close");

        NavigationAction cancelAction = CommonActions.CANCEL.navAction();
        cancelAction.configure().toReturnPath(ApplicationFunctions.User.MANAGEMENT).usingCurrentURLData()
            .withSourceComponent(this);
        cancelAction.setTarget(this, "close");

        setPersistenceActions(saveAction, cancelAction);

        _notifications.forEach(notification -> getNotifiable().sendNotification(notification));
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
