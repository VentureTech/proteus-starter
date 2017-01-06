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

package com.example.app.support.ui.contact;

import com.example.app.support.service.ContactUtil;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.Contact;
import net.proteusframework.users.model.EmailAddress;
import net.proteusframework.users.model.Gender;
import net.proteusframework.users.model.PhoneNumber;

import static com.example.app.support.ui.contact.ContactValueEditorLOK.*;


/**
 * {@link CompositeValueEditor} for {@link Contact}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/2/15 1:40 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.support.ui.contact.ContactValueEditor",
    i18n = {
        @I18N(symbol = "Label Department", l10n = @L10N("Department")),
        @I18N(symbol = "Label Position", l10n = @L10N("Position")),
        @I18N(symbol = "Label Notes", l10n = @L10N("Notes")),
        @I18N(symbol = "Label Gender", l10n = @L10N("Gender")),
        @I18N(symbol = "Label Preferred Time Zone", l10n = @L10N("Preferred Time Zone")),
        @I18N(symbol = "Label Preferred Locale", l10n = @L10N("Preferred Locale")),
        @I18N(symbol = "Label Birth Date", l10n = @L10N("Birth Date")),
        @I18N(symbol = "Label Opt In", l10n = @L10N("Receive Opt-in Mailings?"))
    }
)
public class ContactValueEditor extends CompositeValueEditor<Contact>
{
    /**
     * Enum defining fields within the ContactValueEditor
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static enum ContactField
    {
        /** contact field */
        name,
        //addresses: handled by setting configs to null
        //phoneNumbers: handled by setting configs to null
        //emailAddresses: handled by setting configs to null
        /** contact field */
        department,
        /** contact field */
        position,
        /** contact field */
        notes,
        /** contact field */
        gender,
        /** contact field */
        timezone
    }

    /**
     * Class for configuring the display of {@link ContactValueEditor}
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class ContactValueEditorConfig
    {
        private final EnumSet<ContactField> _includedFields = EnumSet.noneOf(ContactField.class);
        private final EnumSet<ContactField> _requiredFields = EnumSet.noneOf(ContactField.class);
        private final List<AddressValueEditor.AddressValueEditorConfig> _addressConfigs = new ArrayList<>();
        private final List<PhoneNumberValueEditor.PhoneNumberValueEditorConfig> _phoneNumberConfigs = new ArrayList<>();
        private final List<EmailAddressValueEditor.EmailAddressValueEditorConfig> _emailAddressConfigs = new ArrayList<>();
        private NameValueEditor.NameValueEditorConfig _nameConfig;
        private Supplier<ValueEditor<?>> _departmentSupplier;
        private Supplier<ValueEditor<?>> _positionSupplier;
        private Supplier<ValueEditor<?>> _notesSupplier;
        private Supplier<ValueEditor<?>> _genderSupplier;
        private Supplier<ValueEditor<?>> _timezoneSupplier;

        /**
         * Get the {@link AddressValueEditor.AddressValueEditorConfig}s to be used for constructing the editors for
         * the Contact's addresses.  By default returns a list of one config with default values.
         *
         * @return a list of AddressValueEditorConfigs
         */
        @Nonnull
        public List<AddressValueEditor.AddressValueEditorConfig> getAddressConfigs()
        {
            if (_addressConfigs.isEmpty())
            {
                _addressConfigs.add(new AddressValueEditor.AddressValueEditorConfig());
            }
            return _addressConfigs;
        }

        /**
         * Set the {@link AddressValueEditor.AddressValueEditorConfig}s to be used for constructing the editors for
         * the Contact's addresses.  By default returns a list of one config with default values.
         *
         * @param addressConfigs AddressValueEditorConfigs
         */
        public void setAddressConfigs(@Nullable AddressValueEditor.AddressValueEditorConfig... addressConfigs)
        {
            _addressConfigs.clear();
            if (addressConfigs != null)
            {
                Collections.addAll(_addressConfigs, addressConfigs);
            }
        }

        /**
         * Get the Supplier for the department property.  By default, this is just a text editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getDepartmentSupplier()
        {
            if (_departmentSupplier != null)
            {
                return _departmentSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_DEPARTMENT(), null);
                if (getRequiredFields().contains(ContactField.department))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Get an EnumSet of all required fields in the Editor
         * <br>
         * By default, no fields are returned by this, but addresses, phoneNumbers, and emailAddresses are not managed by this
         * <br>
         * This is only used if the Supplier for the field has not been set
         *
         * @return an EnumSet of all required fields in the Editor
         */
        @Nonnull
        public EnumSet<ContactField> getRequiredFields()
        {
            return _requiredFields;
        }

        /**
         * Set an EnumSet of all required fields in the Editor
         * <br>
         * By default, no fields are returned by this, but addresses, phoneNumbers, and emailAddresses are not managed by this
         * <br>
         * This is only used if the Supplier for the field has not been set
         *
         * @param requiredFields an Array of all required fields in the Editor
         */
        public void setRequiredFields(@Nullable ContactField... requiredFields)
        {
            _requiredFields.clear();
            if (requiredFields != null)
                Collections.addAll(_requiredFields, requiredFields);
        }

        /**
         * Set the Supplier for the department property.  By default, this is just a text editor.
         *
         * @param departmentSupplier a Supplier for a ValueEditor
         */
        public void setDepartmentSupplier(@Nullable Supplier<ValueEditor<?>> departmentSupplier)
        {
            _departmentSupplier = departmentSupplier;
        }

        /**
         * Get the {@link EmailAddressValueEditor.EmailAddressValueEditorConfig}s to be used for constructing the editors for
         * the Contact's email addresses.  By default returns a list of one config with default values.
         *
         * @return a list of EmailAddressValueEditorConfigs
         */
        @Nonnull
        public List<EmailAddressValueEditor.EmailAddressValueEditorConfig> getEmailAddressConfigs()
        {
            if (_emailAddressConfigs.isEmpty())
            {
                _emailAddressConfigs.add(new EmailAddressValueEditor.EmailAddressValueEditorConfig());
            }
            return _emailAddressConfigs;
        }

        /**
         * Set the {@link EmailAddressValueEditor.EmailAddressValueEditorConfig}s to be used for constructing the editors for
         * the Contact's email addresses.  By default returns a list of one config with default values.
         *
         * @param emailAddressConfigs EmailAddressValueEditorConfig
         */
        public void setEmailAddressConfigs(@Nullable EmailAddressValueEditor.EmailAddressValueEditorConfig... emailAddressConfigs)
        {
            _emailAddressConfigs.clear();
            if (emailAddressConfigs != null)
            {
                Collections.addAll(_emailAddressConfigs, emailAddressConfigs);
            }
        }

        /**
         * Get the Supplier for the gender property.  By default, this is just a comboBox editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getGenderSupplier()
        {
            if (_genderSupplier != null)
            {
                return _genderSupplier;
            }
            return () -> {
                ComboBoxValueEditor<Gender> editor = new ComboBoxValueEditor<>(
                    LABEL_GENDER(), new ArrayList<>(Arrays.asList(Gender.values())), Gender.UNSPECIFIED);
                if (getRequiredFields().contains(ContactField.gender))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the gender property.  By default, this is just a comboBox editor.
         *
         * @param genderSupplier a Supplier for a ValueEditor
         */
        public void setGenderSupplier(@Nullable Supplier<ValueEditor<?>> genderSupplier)
        {
            _genderSupplier = genderSupplier;
        }

        /**
         * Get an EnumSet of all included fields in the Editor
         * <br>
         * By default, no fields are returned by this, but addresses, phoneNumbers, and emailAddresses are not managed by this
         *
         * @return an EnumSet of all included fields in the Editor
         */
        @Nonnull
        public EnumSet<ContactField> getIncludedFields()
        {
            return _includedFields;
        }

        /**
         * Set an EnumSet of all included fields in the Editor
         * <br>
         * By default, no fields are returned by this, but addresses, phoneNumbers, and emailAddresses are not managed by this
         *
         * @param includedFields an Array of all included fields in the Editor
         */
        public void setIncludedFields(@Nullable ContactField... includedFields)
        {
            _includedFields.clear();
            if (includedFields != null)
                Collections.addAll(_includedFields, includedFields);
        }

        /**
         * Get the {@link NameValueEditor.NameValueEditorConfig} to be used for constructing the editor for the Contact's name
         *
         * @return a NameValueEditorConfig
         */
        @Nonnull
        public NameValueEditor.NameValueEditorConfig getNameConfig()
        {
            if (_nameConfig != null)
            {
                return _nameConfig;
            }
            return new NameValueEditor.NameValueEditorConfig();
        }

        /**
         * Set the {@link NameValueEditor.NameValueEditorConfig} to be used for constructing the editor for the Contact's name
         *
         * @param nameConfig a NameValueEditorConfig
         */
        public void setNameConfig(NameValueEditor.NameValueEditorConfig nameConfig)
        {
            _nameConfig = nameConfig;
        }

        /**
         * Get the Supplier for the notes property.  By default, this is just a text editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getNotesSupplier()
        {
            if (_notesSupplier != null)
            {
                return _notesSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_NOTES(), null);
                if (getRequiredFields().contains(ContactField.notes))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the notes property.  By default, this is just a text editor.
         *
         * @param notesSupplier a Supplier for a ValueEditor
         */
        public void setNotesSupplier(@Nullable Supplier<ValueEditor<?>> notesSupplier)
        {
            _notesSupplier = notesSupplier;
        }

        /**
         * Get the {@link PhoneNumberValueEditor.PhoneNumberValueEditorConfig}s to be used for constructing the editors for
         * the Contact's phone numbers.  By default returns a list of one config with default values.
         *
         * @return a list of PhoneNumberValueEditorConfigs
         */
        @Nonnull
        public List<PhoneNumberValueEditor.PhoneNumberValueEditorConfig> getPhoneNumberConfigs()
        {
            if (_phoneNumberConfigs.isEmpty())
            {
                _phoneNumberConfigs.add(new PhoneNumberValueEditor.PhoneNumberValueEditorConfig());
            }
            return _phoneNumberConfigs;
        }

        /**
         * Set the {@link PhoneNumberValueEditor.PhoneNumberValueEditorConfig}s to be used for constructing the editors for
         * the Contact's phone numbers.  By default returns a list of one config with default values.
         *
         * @param phoneNumberConfigs PhoneNumberValueEditorConfigs
         */
        public void setPhoneNumberConfigs(@Nullable PhoneNumberValueEditor.PhoneNumberValueEditorConfig... phoneNumberConfigs)
        {
            _phoneNumberConfigs.clear();
            if (phoneNumberConfigs != null)
            {
                Collections.addAll(_phoneNumberConfigs, phoneNumberConfigs);
            }
        }

        /**
         * Get the Supplier for the position property.  By default, this is just a text editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getPositionSupplier()
        {
            if (_positionSupplier != null)
            {
                return _positionSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_POSITION(), null);
                if (getRequiredFields().contains(ContactField.position))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the position property.  By default, this is just a text editor.
         *
         * @param positionSupplier a Supplier for a ValueEditor
         */
        public void setPositionSupplier(@Nullable Supplier<ValueEditor<?>> positionSupplier)
        {
            _positionSupplier = positionSupplier;
        }

        /**
         * Get the Supplier for the preferredTimeZone property.  By default, this is a comboBox editor.
         *
         * @return a Supplier for a ValueEditor
         */
        public Supplier<ValueEditor<?>> getTimezoneSupplier()
        {
            if (_timezoneSupplier != null)
            {
                return _timezoneSupplier;
            }
            return () -> {
                TimeZoneValueEditor editor = TimeZoneValueEditor.create(LABEL_PREFERRED_TIME_ZONE(), ContactUtil
                    .getTimeZoneList(null, null), Event.isInRequestResponseCycle()
                    ? Event.getRequest().getHostname().getSite().getDefaultTimeZone()
                    : null, TimeZoneValueEditor.TimezoneRenderStyle.ID, null);
                if (getRequiredFields().contains(ContactField.timezone))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the preferredTimeZone property.  By default, this is a comboBox editor.
         *
         * @param timezoneSupplier a Supplier for a ValueEditor
         */
        public void setTimezoneSupplier(@Nullable Supplier<ValueEditor<?>> timezoneSupplier)
        {
            _timezoneSupplier = timezoneSupplier;
        }
    }
    private final List<AddressValueEditor> _addressEditors = new ArrayList<>();
    private final List<PhoneNumberValueEditor> _phoneNumberEditors = new ArrayList<>();
    private final List<EmailAddressValueEditor> _emailAddressEditors = new ArrayList<>();
    private ContactValueEditorConfig _config = new ContactValueEditorConfig();

    /**
     * Instantiate a new instance of ContactValueEditor
     */
    public ContactValueEditor()
    {
        this(null);
    }

    /**
     * Instantiate a new instance of ContactValueEditor
     *
     * @param config the config for this ContactValueEditor
     */
    public ContactValueEditor(@Nullable ContactValueEditorConfig config)
    {
        super(Contact.class);
        if (config != null)
            _config = config;
    }

    /**
     * Get the config for this ContactValueEditor
     *
     * @return the config
     */
    @Nonnull
    public ContactValueEditorConfig getConfig()
    {
        return _config;
    }

    @Override
    public void setValue(@Nullable Contact value)
    {
        super.setValue(value);

        if (isInited())
        {
            setValuesOnEditorsForListFields(value);
        }
    }

    @Override
    public ModificationState getModificationState()
    {
        ModificationState modState = super.getModificationState();
        if (!modState.isModified())
        {
            List<ValueEditor<?>> editors = new ArrayList<>();
            editors.addAll(_addressEditors);
            editors.addAll(_phoneNumberEditors);
            editors.addAll(_emailAddressEditors);
            for (ValueEditor<?> editor : editors)
            {
                if (editor.getModificationState().isModified()) return ModificationState.CHANGED;
            }
        }
        return modState;
    }

    @Nullable
    @Override
    public Contact getUIValue(Level logErrorLevel)
    {
        Contact result = super.getUIValue(logErrorLevel);
        if (result != null)
        {
            result.getAddresses().clear();
            result.getPhoneNumbers().clear();
            result.getEmailAddresses().clear();
            _addressEditors.forEach(ae -> {
                Address a = ae.getUIValue(logErrorLevel);
                if (a != null)
                    result.getAddresses().add(a);
            });
            _phoneNumberEditors.forEach(pne -> {
                PhoneNumber pn = pne.getUIValue(logErrorLevel);
                if (pn != null)
                    result.getPhoneNumbers().add(pn);
            });
            _emailAddressEditors.forEach(eae -> {
                EmailAddress ea = eae.getUIValue(logErrorLevel);
                if (ea != null)
                    result.getEmailAddresses().add(ea);
            });
        }
        return result;
    }

    @Override
    public boolean validateUIValue(Notifiable notifiable)
    {
        boolean valid = super.validateUIValue(notifiable);
        valid = _addressEditors.stream().map(ae -> ae.validateUIValue(notifiable)).reduce(valid, Boolean::logicalAnd);
        valid = _phoneNumberEditors.stream().map(pne -> pne.validateUIValue(notifiable)).reduce(valid, Boolean::logicalAnd);
        valid = _emailAddressEditors.stream().map(eae -> eae.validateUIValue(notifiable)).reduce(valid, Boolean::logicalAnd);
        return valid;
    }

    @Nullable
    @Override
    public Contact commitValue() throws MIWTException
    {
        Contact result = super.commitValue();
        if (result != null)
        {
            result.getAddresses().clear();
            result.getPhoneNumbers().clear();
            result.getEmailAddresses().clear();
            _addressEditors.forEach(ae -> {
                Address a = ae.commitValue();
                if (a != null)
                    result.getAddresses().add(a);
            });
            _phoneNumberEditors.forEach(pne -> {
                PhoneNumber pn = pne.commitValue();
                if (pn != null)
                    result.getPhoneNumbers().add(pn);
            });
            _emailAddressEditors.forEach(eae -> {
                EmailAddress ea = eae.commitValue();
                if (ea != null)
                    result.getEmailAddresses().add(ea);
            });
        }
        return result;
    }

    @Override
    public void init()
    {
        super.init();

        if (_config.getIncludedFields().contains(ContactField.name))
            addEditorForProperty(() -> new NameValueEditor(_config.getNameConfig()), "name");
        _config.getAddressConfigs().forEach(ac -> _addressEditors.add(new AddressValueEditor(ac)));
        _addressEditors.forEach(this::add);
        _config.getEmailAddressConfigs().forEach(eac -> _emailAddressEditors.add(new EmailAddressValueEditor(eac)));
        _emailAddressEditors.forEach(this::add);
        _config.getPhoneNumberConfigs().forEach(pnc -> _phoneNumberEditors.add(new PhoneNumberValueEditor(pnc)));
        _phoneNumberEditors.forEach(this::add);
        if (_config.getIncludedFields().contains(ContactField.department))
            addEditorForProperty(_config.getDepartmentSupplier(), "department");
        if (_config.getIncludedFields().contains(ContactField.position))
            addEditorForProperty(_config.getPositionSupplier(), "position");
        if (_config.getIncludedFields().contains(ContactField.notes))
            addEditorForProperty(_config.getNotesSupplier(), "notes");
        if (_config.getIncludedFields().contains(ContactField.gender))
            addEditorForProperty(_config.getGenderSupplier(), "gender");
        if (_config.getIncludedFields().contains(ContactField.timezone))
            addEditorForProperty(_config.getTimezoneSupplier(), "preferredTimeZone");

        setValuesOnEditorsForListFields(getValue());
    }

    private void setValuesOnEditorsForListFields(@Nullable Contact value)
    {
        _addressEditors.forEach(ae -> ae.setValue(null));
        _phoneNumberEditors.forEach(pne -> pne.setValue(null));
        _emailAddressEditors.forEach(eae -> eae.setValue(null));

        if (value != null)
        {
            for (int i = 0; i < value.getAddresses().size() && i < _addressEditors.size(); i++)
            {
                _addressEditors.get(i).setValue(value.getAddresses().get(i));
            }
            for (int i = 0; i < value.getPhoneNumbers().size() && i < _phoneNumberEditors.size(); i++)
            {
                _phoneNumberEditors.get(i).setValue(value.getPhoneNumbers().get(i));
            }
            for (int i = 0; i < value.getEmailAddresses().size() && i < _emailAddressEditors.size(); i++)
            {
                _emailAddressEditors.get(i).setValue(value.getEmailAddresses().get(i));
            }
        }
    }

    /**
     * Set the config for this ContactValueEditor
     *
     * @param config the config
     *
     * @return this
     */
    @Nonnull
    public ContactValueEditor withConfig(@Nonnull ContactValueEditorConfig config)
    {
        _config = config;
        return this;
    }
}
