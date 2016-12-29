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
import com.example.app.model.profile.MembershipTypeProvider;
import com.example.app.model.profile.Profile;
import com.example.app.model.profile.ProfileDAO;
import com.example.app.model.terminology.ProfileTermProvider;
import com.example.app.model.user.ContactMethod;
import com.example.app.model.user.User;
import com.example.app.model.user.UserDAO;
import com.example.app.service.MembershipOperationProvider;
import com.example.app.support.AppUtil;
import com.example.app.support.ContactUtil;
import com.example.app.ui.UIPreferences;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.prefs.Preferences;

import com.i2rd.hr.miwt.AddressCellRenderer;
import com.i2rd.hr.miwt.NameCellRenderer;

import net.proteusframework.cms.support.ImageFileUtil;
import net.proteusframework.core.StringFactory;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.metric.Dimension;
import net.proteusframework.core.metric.PixelMetric;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.core.notification.Notification;
import net.proteusframework.core.notification.NotificationImpl;
import net.proteusframework.internet.http.Link;
import net.proteusframework.ui.miwt.Image;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Dialog;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.ImageComponent;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.Window;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.component.composite.editor.CheckboxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.validation.CompositeValidator;
import net.proteusframework.ui.miwt.validation.Validator;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.EmailAddress;
import net.proteusframework.users.model.PhoneNumber;
import net.proteusframework.users.model.dao.NonUniqueCredentialsException;
import net.proteusframework.users.model.dao.PrincipalDAO;

import static com.example.app.support.AppUtil.nullFirst;
import static com.example.app.ui.user.UserValueViewerLOK.*;
import static net.proteusframework.core.notification.NotificationImpl.error;
import static net.proteusframework.core.notification.NotificationType.INFO;
import static net.proteusframework.core.validation.CommonValidationText.ARG0_IS_REQUIRED;
import static net.proteusframework.ui.miwt.validation.RequiredValueValidator.createRequiredValueValidator;

/**
 * Value Viewer for {@link User}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/7/15 3:49 PM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.UserValueViewer",
    i18n = {
        @I18N(symbol = "Label Name", l10n = @L10N("Name")),
        @I18N(symbol = "Label Address", l10n = @L10N("Address")),
        @I18N(symbol = "Label Email", l10n = @L10N("Email/Username")),
        @I18N(symbol = "Label Phone", l10n = @L10N("Phone Number")),
        @I18N(symbol = "Label SMS Phone", l10n = @L10N("SMS Phone Number")),
        @I18N(symbol = "Label Update Password", l10n = @L10N("Update Password")),
        @I18N(symbol = "Label New Password", l10n = @L10N("New Password")),
        @I18N(symbol = "Label Preferred Time Zone", l10n = @L10N("Preferred Time Zone")),
        @I18N(symbol = "Label Confirm Password", l10n = @L10N("Confirm New Password")),
        @I18N(symbol = "Error Message Passwords Not Match", l10n = @L10N("Passwords do not match.")),
        @I18N(symbol = "Error Message Error Occurred Updating Password",
            l10n = @L10N("An error occurred updating password, please contact support.")),
        @I18N(symbol = "Message Password Updated", l10n = @L10N("Password successfully updated.")),
        @I18N(symbol = "Label Contact Preferences", l10n = @L10N("Notification Preferences")),
        @I18N(symbol = "Label Send Notification to PhoneSms", l10n = @L10N("Send notifications to your SMS Phone Number.")),
        @I18N(symbol = "Label Login Landing Page", l10n = @L10N("Login Landing Page"))
    }
)
@Configurable
public class UserValueViewer extends Container
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(UserValueViewer.class);
    private static final int MAX_HEIGHT_PROFILE_PIC_VIEW = 200;

    private static class ConfirmPasswordValidator implements Validator
    {
        private final TextEditor _newPassword;
        private final TextEditor _confirmPassword;

        public ConfirmPasswordValidator(TextEditor newPassword, TextEditor confirmPassword)
        {
            _newPassword = newPassword;
            _confirmPassword = confirmPassword;
        }

        @Override
        public boolean isComponentValidationSupported(Component component)
        {
            return true;
        }

        @Override
        public boolean validate(Component component, Notifiable notifiable)
        {
            if (!Objects.equals(_newPassword.getUIValue(), _confirmPassword.getUIValue()))
            {
                NotificationImpl error = error(ERROR_MESSAGE_PASSWORDS_NOT_MATCH());
                error.setSource(_confirmPassword);
                notifiable.sendNotification(error);
                return false;
            }
            return true;
        }
    }

    @Autowired
    private AppUtil _appUtil;
    @Autowired
    private EntityRetriever _er;
    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private ProfileDAO _profileDAO;
    @Autowired
    private MembershipOperationProvider _mop;
    @Autowired
    private PrincipalDAO _principalDAO;
    @Autowired
    private ProfileTermProvider _terms;
    @Autowired
    private MembershipTypeProvider _mtp;
    @Autowired
    private UIPreferences _uiPreferences;

    private boolean _adminMode = true;
    private User _user;
    private Notifiable _notifiable;

    /**
     * Instantiate a new instance of UserValueViewer
     * <br>
     * NOTE:  Must set User with {@link #setUser(User)} before initialization.
     */
    public UserValueViewer()
    {
        super();
    }

    /**
     * Displays a dialog for the current user to update the password for the user being viewed
     */
    public void beginUpdatePassword()
    {
        final Dialog dlg = new Dialog(getApplication(), LABEL_UPDATE_PASSWORD());
        dlg.addClassName("update-password-dialog");
        final MessageContainer dlgMessages = new MessageContainer(35_000L);

        final TextEditor newPassword = new TextEditor(LABEL_NEW_PASSWORD(), null);
        newPassword.setInputType(Field.InputType.password);
        newPassword.setRequiredValueValidator();
        final TextEditor confirmPassword = new TextEditor(LABEL_CONFIRM_PASSWORD(), null);
        confirmPassword.setInputType(Field.InputType.password);
        confirmPassword.setValueValidator(new CompositeValidator(
            createRequiredValueValidator(ARG0_IS_REQUIRED, confirmPassword.getLabel().getText())
                .withNotificationSourceSetter((validator, component, notification) -> notification.setSource(confirmPassword)),
            new ConfirmPasswordValidator(newPassword, confirmPassword)));

        final PushButton save = CommonActions.SAVE.push();
        final PushButton cancel = CommonActions.CANCEL.push();
        ActionListener cancelAction = new Window.Closer();
        cancel.addActionListener(cancelAction);
        save.addActionListener(ev -> {
            dlgMessages.clearNotifications();
            boolean valid = newPassword.validateUIValue(dlgMessages);
            valid = confirmPassword.validateUIValue(dlgMessages) && valid;
            if (valid)
            {
                List<Notification> notifications = new ArrayList<>();
                try
                {
                    boolean success = _principalDAO.setNewPassword(
                        getUser().getPrincipal(), getUser().getPrincipal().getUsername(), notifications, newPassword.commitValue());

                    if (!success)
                    {
                        notifications.forEach(notification -> {
                            notification.setSource(newPassword);
                            dlgMessages.sendNotification(notification);
                        });
                    }
                    else
                    {
                        getNotifiable().sendNotification(new NotificationImpl(INFO, MESSAGE_PASSWORD_UPDATED()));
                        cancelAction.actionPerformed(ev);
                    }
                }
                catch (NonUniqueCredentialsException e)
                {
                    _logger.error("Unable to update Password", e);
                    dlgMessages.sendNotification(error(ERROR_MESSAGE_ERROR_OCCURRED_UPDATING_PASSWORD()));
                }
            }
        });

        dlg.add(of(HTMLElement.section, "prop-editor prop-wrapper",
            dlgMessages,
            of("prop-body", newPassword, confirmPassword),
            of("prop-footer",
                of("prop-footer-actions",
                    of("persistence-actions actions bottom", save, cancel)
                )
            )
        ));

        getWindowManager().add(dlg);
        dlg.setVisible(true);
    }    /**
     * Get the User to view
     *
     * @return the User to view
     */
    @Nonnull
    public User getUser()
    {
        return Optional.ofNullable(_er.reattachIfNecessary(_user)).orElseThrow(() -> new IllegalStateException(
            "User has not been set, so it cannot be retrieved."));
    }

    /**
     * Get the Notifiable for this UserValueViewer
     * If one has not been set, a warning is logged.
     *
     * @return the Notifiable
     */
    @Nonnull
    public Notifiable getNotifiable()
    {
        if (_notifiable == null)
        {
            _logger.warn("Notifiable has not been set yet, any messages sent to this returned value will not show up on the UI.");
            return new MessageContainer(35_000L);
        }
        return _notifiable;
    }    /**
     * Set the User to view
     *
     * @param user the User to view
     */
    public void setUser(@Nonnull User user)
    {
        _user = user;

        if (isInited())
        {
            setupUI();
        }
    }

    /**
     * Set the Notifiable for this UserValueViewer
     *
     * @param notifiable the Notifiable
     */
    public void setNotifiable(@Nonnull Notifiable notifiable)
    {
        _notifiable = notifiable;
    }

    /**
     * Get boolean flag.  If true, the current user has the right permissions/roles to update the viewed User's password
     *
     * @return boolean flag
     */
    public boolean canUpdatePassword()
    {
        User currentUser = _userDAO.getAssertedCurrentUser();
        final TimeZone timeZone = getSession().getTimeZone();
        Profile profile = _uiPreferences.getSelectedCompany();
        return _profileDAO.canOperate(currentUser, profile, timeZone, _mop.changeUserPassword())
               || Objects.equals(currentUser.getId(), getUser().getId());
    }

    @Override
    public void init()
    {
        super.init();

        setupUI();
    }    /**
     * Get boolean flag.  If true, this viewer is being displayed in admin mode
     * <br>
     * defaults to true.
     *
     * @return boolean flag
     */
    public boolean isAdminMode()
    {
        return _adminMode;
    }

    /**
     * Set boolean flag.  If true, this viewer is being displayed in admin mode
     *
     * @param adminMode boolean flag
     */
    public void setAdminMode(boolean adminMode)
    {
        _adminMode = adminMode;
    }





    private void setupUI()
    {
        removeAllComponents();

        User currentUser = _userDAO.getAssertedCurrentUser();
        Profile profile = _uiPreferences.getSelectedCompany();

        final ImageComponent userImage = new ImageComponent();
        if (getUser().getImage() != null)
        {
            try
            {
                final Dimension size = ImageFileUtil.getDimension(getUser().getImage(), true);
                // We are showing a max height of 200px
                if (size != null && size.getHeightMetric().intValue() < MAX_HEIGHT_PROFILE_PIC_VIEW)
                    userImage.setSize(size);
                else
                    userImage.setHeight(new PixelMetric(MAX_HEIGHT_PROFILE_PIC_VIEW));
            }
            catch (IOException e)
            {
                _logger.debug("Unable to get file dimension.", e);
            }
            userImage.setImage(new Image(getUser().getImage()));
        }
        else
            userImage.setImage(new Image(_appUtil.getDefaultUserImage()));
        userImage.addClassName("image");
        final Container userImageField = of("prop picture", userImage);

        final TextEditor nameField = new TextEditor(LABEL_NAME(),
            NameCellRenderer.getFormattedString(getUser().getPrincipal().getContact().getName()));
        nameField.setEditable(false);
        setFieldVisibility(nameField);
        nameField.addClassName("name");

        Address address = ContactUtil.getAddress(getUser().getPrincipal().getContact(), ContactDataCategory.values())
            .orElse(new Address());
        final Container addressContainer = Container.of("prop address", LABEL_ADDRESS(), new AddressCellRenderer(address));

        final TextEditor emailField = new TextEditor(LABEL_EMAIL(), ContactUtil.getEmailAddress(getUser().getPrincipal()
            .getContact(), ContactDataCategory.values()).map(EmailAddress::getEmail).orElse(""));
        emailField.setEditable(false);
        setFieldVisibility(emailField);
        emailField.addClassName("email");

        final TextEditor phoneField = new TextEditor(LABEL_PHONE(), ContactUtil.getPhoneNumber(getUser().getPrincipal()
            .getContact(), ContactDataCategory.values()).map(PhoneNumber::toExternalForm).orElse(""));
        phoneField.setEditable(false);
        setFieldVisibility(phoneField);
        phoneField.addClassName("phone");

        final TextEditor smsPhoneField = new TextEditor(LABEL_SMS_PHONE(), Optional.ofNullable(getUser().getSmsPhone()).map
            (PhoneNumber::toExternalForm).orElse(""));
        smsPhoneField.setEditable(false);
        setFieldVisibility(smsPhoneField);
        smsPhoneField.addClassName("sms-phone");

        final TextEditor timeZoneField = new TextEditor(LABEL_PREFERRED_TIME_ZONE(), Optional.ofNullable(getUser().getPrincipal()
            .getContact().getPreferredTimeZone())
            //            .map(tz -> tz.getDisplayName(false, TimeZone.LONG, getLocaleContext().getLocale()))
            .map(TimeZone::getID)
            .orElse(""));
        timeZoneField.setEditable(false);
        setFieldVisibility(timeZoneField);
        timeZoneField.addClassName("time-zone");

        final ComboBoxValueEditor<Profile> coachingField = new ComboBoxValueEditor<>(
            _terms.company(), Collections.singletonList(profile), profile);
        coachingField.setEditable(false);
        coachingField.addClassName("coaching");

        final CheckboxValueEditor<LocalizedObjectKey> contactPrefsField = new CheckboxValueEditor<>(
            LABEL_CONTACT_PREFERENCES(), Collections.singleton(LABEL_SEND_NOTIFICATION_TO_PHONESMS()), null);
        contactPrefsField.getLabel().withHTMLElement(HTMLElement.h3);
        contactPrefsField.withHTMLElement(HTMLElement.section);
        contactPrefsField.setEditable(false);
        contactPrefsField.setVisible(Objects.equals(currentUser.getId(), getUser().getId()));
        if (getUser().getPreferredContactMethod() == ContactMethod.PhoneSms)
            contactPrefsField.setValue(Collections.singleton(LABEL_SEND_NOTIFICATION_TO_PHONESMS()));

        List<Link> links = LoginLandingLinks.getAvailableLinks(getUser(), getLocaleContext());
        links = nullFirst(links);
        final ComboBoxValueEditor<Link> loginLangingPage = new ComboBoxValueEditor<>(
            LABEL_LOGIN_LANDING_PAGE(),
            links,
            null);
        //This field is available when the current user is the user being viewed
        // AND only to the user who has a particular membership under the company.
        loginLangingPage.setVisible(
            Objects.equals(currentUser.getId(), getUser().getId())
            && _profileDAO.getMembershipsForUser(getUser(), null, getSession().getTimeZone()).stream()
                .map(Membership::getMembershipType).filter(membershipType1 -> membershipType1 != null)
                .anyMatch(membershipType ->
                    membershipType.equals(_mtp.companyAdmin())
                )
        );
        loginLangingPage.setCellRenderer(new CustomCellRenderer(CommonButtonText.PLEASE_SELECT, input -> {
            Link link = (Link) input;
            return TextSources.createText(link.getAdditionalAttributes().get("label"));
        }));
        final Preferences userPref = Preferences.userRoot().node(User.LOGIN_PREF_NODE);
        final String uri = userPref != null ? userPref.get(User.LOGIN_PREF_NODE_LANDING_PAGE, null) : null;
        for (Link l : links)
        {
            if (l != null && l.getURIAsString().equals(uri))
            {
                loginLangingPage.setValue(l);
                break;
            }
        }

        loginLangingPage.getLabel().withHTMLElement(HTMLElement.h3);
        loginLangingPage.withHTMLElement(HTMLElement.section);
        loginLangingPage.setEditable(false);

        add(userImageField);
        add(nameField);
        add(addressContainer);
        add(emailField);
        add(phoneField);
        if (getUser().getSmsPhone() != null)
            add(smsPhoneField);
        add(timeZoneField);
        if (isAdminMode())
        {
            add(coachingField);
        }
        add(contactPrefsField);
        add(loginLangingPage);
    }



    private static void setFieldVisibility(TextEditor field)
    {
        field.setVisible(!StringFactory.isEmptyString(field.getValue()));
    }

}
