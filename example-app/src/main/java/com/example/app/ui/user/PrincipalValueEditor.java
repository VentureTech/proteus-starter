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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.example.app.ui.contact.ContactValueEditor;
import com.example.app.ui.contact.ContactValueEditor.ContactValueEditorConfig;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.i2rd.miwt.util.CSSUtil;

import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.core.notification.NotificationImpl;
import net.proteusframework.core.validation.CommonValidationText;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.validation.CompositeValidator;
import net.proteusframework.ui.miwt.validation.EmailValidator;
import net.proteusframework.ui.miwt.validation.NotificationSourceSetter;
import net.proteusframework.ui.miwt.validation.RequiredValueValidator;
import net.proteusframework.ui.miwt.validation.Validator;
import net.proteusframework.users.model.AuthenticationDomain;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.PrincipalStatus;
import net.proteusframework.users.model.dao.AuthenticationDomainList;
import net.proteusframework.users.model.dao.PrincipalDAO;

import static com.example.app.ui.contact.AddressValueEditor.AddressValueEditorConfig;
import static com.example.app.ui.contact.ContactValueEditor.ContactField;
import static com.example.app.ui.contact.EmailAddressValueEditor.EmailAddressValueEditorConfig;
import static com.example.app.ui.contact.NameValueEditor.NameField.first;
import static com.example.app.ui.contact.NameValueEditor.NameField.last;
import static com.example.app.ui.contact.NameValueEditor.NameValueEditorConfig;
import static com.example.app.ui.contact.PhoneNumberValueEditor.*;
import static com.example.app.ui.user.PrincipalValueEditorLOK.*;
import static net.proteusframework.core.locale.TextSources.createText;
import static net.proteusframework.core.notification.NotificationImpl.error;
import static net.proteusframework.users.model.ContactDataCategory.BUSINESS;
import static net.proteusframework.users.model.ContactDataCategory.PERSONAL;
import static net.proteusframework.users.model.PhoneNumberType.MOBILE;
import static net.proteusframework.users.model.dao.AuthenticationDomainList.createDomainList;

/**
 * {@link CompositeValueEditor} for {@link Principal}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/7/15 10:38 AM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.PrincipalValueEditor",
    i18n = {
        @I18N(symbol = "Label Status", l10n = @L10N("Status")),
        @I18N(symbol = "Label Email", l10n = @L10N("Email/Username")),
        @I18N(symbol = "Error Message Username Exists FMT", l10n = @L10N("Given {0} is already in use.")),
        @I18N(symbol = "Label SMS Phone Number", l10n = @L10N("Cell Phone Number"))
    }
)
@Configurable
public class PrincipalValueEditor extends CompositeValueEditor<Principal>
{
    private final List<AuthenticationDomain> _authDomains = new ArrayList<>();
    private final TextEditor _smsEditor = new TextEditor(LABEL_SMS_PHONE_NUMBER(), null);
    @Autowired
    private PrincipalDAO _principalDAO;
    private boolean _adminMode = true;

    /**
     * Instantiate a new instance of PrincipalValueEditor
     */
    public PrincipalValueEditor()
    {
        super(Principal.class);
    }

    /**
     * Get the SMS Phone Number Editor
     *
     * @return editor
     */
    public TextEditor getSmsEditor()
    {
        return _smsEditor;
    }

    @Nullable
    @Override
    public Principal getUIValue(Level logErrorLevel)
    {
        Principal result = super.getUIValue(logErrorLevel);
        if (result != null)
        {
            result.setAuthenticationDomains(getAuthDomains());
            if (result.getStatus() == null)
                result.setStatus(PrincipalStatus.active);
        }
        return result;
    }

    @Nullable
    @Override
    public Principal commitValue() throws MIWTException
    {
        Principal result = super.commitValue();
        if (result != null)
        {
            result.setAuthenticationDomains(getAuthDomains());
            if (result.getStatus() == null)
                result.setStatus(PrincipalStatus.active);
        }
        return result;
    }

    @Override
    public void init()
    {
        super.init();

        addEditorForProperty(() -> {
            ContactValueEditorConfig config = new ContactValueEditorConfig();
            NameValueEditorConfig nameConfig = new NameValueEditorConfig();
            nameConfig.setIncludedFields(first, last);
            nameConfig.setRequiredFields(first, last);
            EmailAddressValueEditorConfig emailConfig = new EmailAddressValueEditorConfig();
            emailConfig.setEmailSupplier(() -> {
                TextEditor editor = new TextEditor(LABEL_EMAIL(), null);
                editor.addClassName(CSSUtil.CSS_REQUIRED_FIELD);

                editor.setValueValidator(new CompositeValidator(
                    new RequiredValueValidator<>().withErrorMessage(CommonValidationText.ARG0_IS_REQUIRED, LABEL_EMAIL()),
                    new EmailValidator(LABEL_EMAIL(), true)
                        .withNotificationSourceSetter((validator, component, notification) -> notification.setSource(editor)),
                    new Validator()
                    {

                        public NotificationSourceSetter<Validator> _notificationSourceSetter;

                        /**
                         * Set the notification source setter.
                         * @see NotificationSourceSetter#defaultSourceSetter()
                         * @param notificationSourceSetter the notification source setter
                         */
                        @SuppressFBWarnings(value = "UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS",
                            justification = "Called via reflection")
                        public void setNotificationSourceSetter(
                            @Nullable NotificationSourceSetter<Validator> notificationSourceSetter)
                        {
                            _notificationSourceSetter = notificationSourceSetter;
                        }

                        @Override
                        public boolean validate(Component component, Notifiable notifiable)
                        {
                            Field field = (Field) component;
                            if (editor.getModificationState() != ModificationState.UNCHANGED)
                            {
                                AuthenticationDomainList authDomain = createDomainList(getAuthDomains());
                                Principal result = _principalDAO.getPrincipalByLogin(field.getText(), authDomain);
                                if (result != null && !Objects.equals(result, getValue()))
                                {
                                    NotificationImpl error = error(createText(ERROR_MESSAGE_USERNAME_EXISTS_FMT(), LABEL_EMAIL()));
                                    _notificationSourceSetter.setSource(this, field, error);
                                    notifiable.sendNotification(error);
                                    return false;
                                }
                            }
                            return true;
                        }
                    }).withNotificationSourceSetterOnChildren(
                    (validator, component, notification) -> notification.setSource(editor)));
                return editor;
            });
            PhoneNumberValueEditorConfig phoneConfig = new PhoneNumberValueEditorConfig();
            phoneConfig.setDefaultContactDataCategory(BUSINESS);
            phoneConfig.setRequiredFields(PhoneNumberField.phoneNumber);
            PhoneNumberValueEditorConfig smsConfig = new PhoneNumberValueEditorConfig();
            smsConfig.setSingleFieldSupplier(() -> {
                _smsEditor.setValueValidator(new PhoneNumberValidator());
                return _smsEditor;
            });
            smsConfig.setDefaultPhoneNumberType(MOBILE);
            smsConfig.setDefaultContactDataCategory(PERSONAL);

            AddressValueEditorConfig addressValueEditorConfig = new AddressValueEditorConfig();
            addressValueEditorConfig.getRequiredFields().clear();

            config.setIncludedFields(ContactField.name, ContactField.timezone);
            config.setRequiredFields(ContactField.name, ContactField.timezone);
            config.setNameConfig(nameConfig);
            config.setEmailAddressConfigs(emailConfig);
            config.setPhoneNumberConfigs(phoneConfig, smsConfig);
            config.setAddressConfigs(addressValueEditorConfig);

            return new ContactValueEditor(config);
        }, "contact");

        addEditorForProperty(() -> {
                List<PrincipalStatus> statusList = new ArrayList<>();
                statusList.add(null);
                Collections.addAll(statusList, PrincipalStatus.values());
                ComboBoxValueEditor<PrincipalStatus> editor = new ComboBoxValueEditor<>(
                    LABEL_STATUS(), statusList, PrincipalStatus.active);
                editor.setRequiredValueValidator();
                editor.setVisible(false);
                return editor;
            },
            Principal::getStatus,
            (writeVal, propVal) -> {
                if (propVal != null)
                {
                    switch (propVal)
                    {
                        case active:
                        case pending:
                        case suspended:
                            writeVal.setEnabled(true);
                            break;
                        case closed:
                            writeVal.setEnabled(false);
                            break;
                        default:
                            break;
                    }
                }
                writeVal.setStatus(propVal);
            });
    }

    /**
     * Get the list of AuthenticationDomains to check for username uniqueness, and to set them on the edited Principal
     *
     * @return a list of AuthenticationDomains
     */
    @Nonnull
    public List<AuthenticationDomain> getAuthDomains()
    {
        return _authDomains;
    }

    /**
     * Set the list of AuthenticationDomains to check for username uniqueness, and to set them on the edited Principal
     *
     * @param authDomains a list of AuthenticationDomains
     */
    public void setAuthDomains(@Nonnull List<AuthenticationDomain> authDomains)
    {
        _authDomains.clear();
        _authDomains.addAll(authDomains);
    }

    /**
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
}
