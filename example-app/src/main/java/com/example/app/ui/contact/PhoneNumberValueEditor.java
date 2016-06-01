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

package com.example.app.ui.contact;

import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

import com.i2rd.miwt.util.CSSUtil;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.core.notification.NotificationImpl;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.validation.CompositeValidator;
import net.proteusframework.ui.miwt.validation.Validator;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.PhoneNumber;
import net.proteusframework.users.model.PhoneNumberType;

import static com.example.app.ui.contact.PhoneNumberValueEditorLOK.*;
import static net.proteusframework.core.validation.CommonValidationText.ARG0_IS_REQUIRED;
import static net.proteusframework.ui.miwt.validation.RequiredValueValidator.createRequiredValueValidator;

/**
 * {@link CompositeValueEditor} for {@link PhoneNumber}
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@SuppressWarnings("unused")
@I18NFile(
    symbolPrefix = "com.lrlabs.ui.contact.PhoneNumberValueEditor",
    i18n = {
        @I18N(symbol = "Label Data Visibility", l10n = @L10N("Data Visibility")),
        @I18N(symbol = "Label Category", l10n = @L10N("Category")),
        @I18N(symbol = "Label Type", l10n = @L10N("Type")),
        @I18N(symbol = "Label Phone Number", l10n = @L10N("Phone Number")),
        @I18N(symbol = "Error Message Invalid Phone Number", l10n = @L10N("Phone Number must be a valid phone number."))
    }
)
public class PhoneNumberValueEditor extends CompositeValueEditor<PhoneNumber>
{
    /**
     * Enum defining fields within the PhoneNumberValueEditor
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static enum PhoneNumberField
    {
        /** phone number field */
        category,
        /** phone number field */
        type,
        /** phone number field */
        dataVisibility,
        /** phone number field */
        phoneNumber
    }

    /**
     * Validator for validating that the input value is a valid phone number
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class PhoneNumberValidator implements Validator
    {
        @Override
        public boolean isComponentValidationSupported(Component component)
        {
            return component instanceof Field;
        }

        @Override
        public boolean validate(Component component, Notifiable notifiable)
        {
            boolean valid;
            String text = ((Field) component).getText();
            if (StringFactory.isEmptyString(text))   //If the value is empty, assume it is not required.  Required value validation
            {                                       // should be done by a RequiredValueValidator, not this Validator.
                return true;
            }
            if (!(valid = !PhoneNumber.valueOf(text).isEmpty()))
            {
                NotificationImpl error = NotificationImpl.error(ERROR_MESSAGE_INVALID_PHONE_NUMBER());
                error.setSource(component);
                notifiable.sendNotification(error);
            }
            return valid;
        }
    }

    /**
     * Class for configuring the display of {@link PhoneNumberValueEditor}
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class PhoneNumberValueEditorConfig
    {
        private final EnumSet<PhoneNumberField> _includedFields = EnumSet.of(PhoneNumberField.phoneNumber);
        private final EnumSet<PhoneNumberField> _requiredFields = EnumSet.noneOf(PhoneNumberField.class);
        private ContactDataCategory _defaultContactDataCategory = ContactDataCategory.UNKNOWN;
        private Supplier<ValueEditor<?>> _categorySupplier;
        private PhoneNumberType _defaultPhoneNumberType = PhoneNumberType.UNKNOWN;
        private Supplier<ValueEditor<?>> _typeSupplier;
        private Supplier<ValueEditor<?>> _dataVisibilitySupplier;
        private Supplier<ValueEditor<String>> _singleFieldSupplier;

        /**
         * Get the Supplier for the category property.  By default, this is just a comboBox editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getCategorySupplier()
        {
            if (_categorySupplier != null)
            {
                return _categorySupplier;
            }
            return () -> {
                List<ContactDataCategory> categories = new ArrayList<>();
                categories.add(null);
                Collections.addAll(categories, ContactDataCategory.values());
                ComboBoxValueEditor<ContactDataCategory> editor = new ComboBoxValueEditor<>(
                    LABEL_CATEGORY(), categories, getDefaultContactDataCategory());
                editor.addClassName("category");
                editor.setCellRenderer(new CustomCellRenderer(CommonButtonText.PLEASE_SELECT));
                if (getRequiredFields().contains(PhoneNumberField.category))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Get the default ContactDataCategory to be used in the case of it not being selectable on the UI
         *
         * @return the default ContactDataCategory
         */
        @Nonnull
        public ContactDataCategory getDefaultContactDataCategory()
        {
            return _defaultContactDataCategory;
        }

        /**
         * Get an EnumSet of all required fields for the Editor
         * <br>
         * By default is empty
         * <br>
         * This is only used if the Supplier for the field has not been set
         *
         * @return an EnumSet of all required fields for the Editor
         */
        @Nonnull
        public EnumSet<PhoneNumberField> getRequiredFields()
        {
            return _requiredFields;
        }

        /**
         * Set an EnumSet of all required fields for the Editor
         * <br>
         * By default is empty
         * <br>
         * This is only used if the Supplier for the field has not been set
         *
         * @param requiredFields an Array of all required fields for the Editor
         */
        public void setRequiredFields(@Nullable PhoneNumberField... requiredFields)
        {
            _requiredFields.clear();
            if (requiredFields != null)
                Collections.addAll(_requiredFields, requiredFields);
        }

        /**
         * Set the default ContactDataCategory to be used in the case of it not being selectable on the UI
         *
         * @param defaultContactDataCategory the default ContactDataCategory
         */
        public void setDefaultContactDataCategory(@Nonnull ContactDataCategory defaultContactDataCategory)
        {
            _defaultContactDataCategory = defaultContactDataCategory;
        }

        /**
         * Set the Supplier for the category property.  By default, this is just a comboBox editor.
         *
         * @param categorySupplier a Supplier for a ValueEditor
         */
        public void setCategorySupplier(@Nullable Supplier<ValueEditor<?>> categorySupplier)
        {
            _categorySupplier = categorySupplier;
        }

        /**
         * Get the Supplier for the dataVisibility property.  By default, this is just a text editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getDataVisibilitySupplier()
        {
            if (_dataVisibilitySupplier != null)
            {
                return _dataVisibilitySupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_DATA_VISIBILITY(), null);
                editor.addClassName("data-visibility");
                if (getRequiredFields().contains(PhoneNumberField.dataVisibility))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the dataVisibility property.  By default, this is just a text editor.
         *
         * @param dataVisibilitySupplier a Supplier for a ValueEditor
         */
        public void setDataVisibilitySupplier(@Nullable Supplier<ValueEditor<?>> dataVisibilitySupplier)
        {
            _dataVisibilitySupplier = dataVisibilitySupplier;
        }

        /**
         * Get an EnumSet of all included fields for the Editor
         * <br>
         * By default only includes {@link PhoneNumberField#phoneNumber}
         *
         * @return an EnumSet of all included fields for the Editor
         */
        @Nonnull
        public EnumSet<PhoneNumberField> getIncludedFields()
        {
            return _includedFields;
        }

        /**
         * Set an EnumSet of all included fields for the Editor
         * <br>
         * By default only includes {@link PhoneNumberField#phoneNumber}
         *
         * @param includedFields an Array of all included fields for the Editor
         */
        public void setIncludedFields(@Nullable PhoneNumberField... includedFields)
        {
            _includedFields.clear();
            if (includedFields != null)
                Collections.addAll(_includedFields, includedFields);
        }

        /**
         * Get the Supplier for the PhoneNumber.  By default, this is just a text editor that does verification on whether the
         * input is a valid phone number.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<String>> getSingleFieldSupplier()
        {
            if (_singleFieldSupplier != null)
            {
                return _singleFieldSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_PHONE_NUMBER(), null);
                editor.addClassName("phone");
                editor.setInputType(Field.InputType.tel);
                if (getRequiredFields().contains(PhoneNumberField.phoneNumber))
                {
                    editor.addClassName(CSSUtil.CSS_REQUIRED_FIELD);
                    editor.setValueValidator(new CompositeValidator(
                        createRequiredValueValidator(ARG0_IS_REQUIRED, editor.getLabel().getText())
                            .withNotificationSourceSetter((validator, component, notification) -> notification.setSource(editor)),
                        new PhoneNumberValidator()));
                }
                else
                {
                    editor.setValueValidator(new PhoneNumberValidator());
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the PhoneNumber.  By default, this is just a text editor that does verification on whether the
         * input is a valid phone number.
         *
         * @param singleFieldSupplier a Supplier for a ValueEditor
         */
        public void setSingleFieldSupplier(@Nullable Supplier<ValueEditor<String>> singleFieldSupplier)
        {
            _singleFieldSupplier = singleFieldSupplier;
        }

        /**
         * Get the Supplier for the type property.  By default, this is just a comboBox editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getTypeSupplier()
        {
            if (_typeSupplier != null)
            {
                return _typeSupplier;
            }
            return () -> {
                List<PhoneNumberType> types = new ArrayList<>(Arrays.asList(PhoneNumberType.values()));
                types.add(0, null);
                ComboBoxValueEditor<PhoneNumberType> editor = new ComboBoxValueEditor<>(
                    LABEL_TYPE(), types, getDefaultPhoneNumberType());
                editor.addClassName("type");
                editor.setCellRenderer(new CustomCellRenderer(CommonButtonText.PLEASE_SELECT));
                if (getRequiredFields().contains(PhoneNumberField.type))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Get the default PhoneNumberType to be used in the case of it not being selectable on the UI
         *
         * @return the default PhoneNumberType
         */
        @Nonnull
        public PhoneNumberType getDefaultPhoneNumberType()
        {
            return _defaultPhoneNumberType;
        }

        /**
         * Set the default PhoneNumberType to be used in the case of it not being selectable on the UI
         *
         * @param defaultPhoneNumberType the default PhoneNumberType
         */
        public void setDefaultPhoneNumberType(@Nonnull PhoneNumberType defaultPhoneNumberType)
        {
            _defaultPhoneNumberType = defaultPhoneNumberType;
        }

        /**
         * Set the Supplier for the type property.  By default, this is just a comboBox editor.
         *
         * @param typeSupplier a Supplier for a ValueEditor
         */
        public void setTypeSupplier(@Nullable Supplier<ValueEditor<?>> typeSupplier)
        {
            _typeSupplier = typeSupplier;
        }
    }

    @Nonnull
    private PhoneNumberValueEditorConfig _config = new PhoneNumberValueEditorConfig();
    @Nullable
    private ValueEditor<String> _singleFieldEditor;

    /**
     * Instantiate a new instance of PhoneNumberValueEditor
     */
    public PhoneNumberValueEditor()
    {
        this(null);
    }

    /**
     * Instantiate a new instance of PhoneNumberValueEditor
     *
     * @param config the config for this PhoneNumberValueEditor.  If null, uses a config with default values.
     */
    public PhoneNumberValueEditor(@Nullable PhoneNumberValueEditorConfig config)
    {
        super(PhoneNumber.class);
        if (config != null)
            _config = config;

        addClassName("prop-group phone");
    }

    /**
     * Get the config for this PhoneNumberValueEditor
     *
     * @return the config
     */
    @Nonnull
    public PhoneNumberValueEditorConfig getConfig()
    {
        return _config;
    }

    @Override
    public void setValue(@Nullable PhoneNumber value)
    {
        super.setValue(value);

        if (isInited())
        {
            setUIValues(value);
        }
    }

    @Override
    public ModificationState getModificationState()
    {
        ModificationState modState = super.getModificationState();
        if (!modState.isModified() && _singleFieldEditor != null)
        {
            if (_singleFieldEditor.getModificationState().isModified()) return ModificationState.CHANGED;
        }
        return modState;
    }

    @Nullable
    @Override
    public PhoneNumber getUIValue(Level logErrorLevel)
    {
        PhoneNumber result = super.getUIValue(logErrorLevel);
        if (result != null)
        {
            if (!_config.getIncludedFields().contains(PhoneNumberField.category))
                result.setCategory(_config.getDefaultContactDataCategory());
            if (!_config.getIncludedFields().contains(PhoneNumberField.type))
                result.setPhoneType(_config.getDefaultPhoneNumberType());
            result = setUIValuesOnValue(result, false);
        }
        return result;
    }

    @Override
    public boolean validateUIValue(Notifiable notifiable)
    {
        boolean valid = super.validateUIValue(notifiable);
        if (_config.getIncludedFields().contains(PhoneNumberField.phoneNumber))
        {
            assert _singleFieldEditor != null;
            valid = valid && _singleFieldEditor.validateUIValue(notifiable);
        }
        return valid;
    }

    @Nullable
    @Override
    public PhoneNumber commitValue() throws MIWTException
    {
        PhoneNumber result = super.commitValue();
        if (result != null)
        {
            if (!_config.getIncludedFields().contains(PhoneNumberField.category))
                result.setCategory(_config.getDefaultContactDataCategory());
            if (!_config.getIncludedFields().contains(PhoneNumberField.type))
                result.setPhoneType(_config.getDefaultPhoneNumberType());
            result = setUIValuesOnValue(result, true);
        }
        return result;
    }

    @Override
    public void init()
    {
        super.init();

        if (_config.getIncludedFields().contains(PhoneNumberField.category))
            addEditorForProperty(_config.getCategorySupplier(), "category");
        if (_config.getIncludedFields().contains(PhoneNumberField.type))
            addEditorForProperty(_config.getTypeSupplier(), "type");
        if (_config.getIncludedFields().contains(PhoneNumberField.dataVisibility))
            addEditorForProperty(_config.getDataVisibilitySupplier(), "dataVisibility");
        if (_config.getIncludedFields().contains(PhoneNumberField.phoneNumber))
        {
            _singleFieldEditor = _config.getSingleFieldSupplier().get();
            setUIValues(getValue());
            add(_singleFieldEditor);
        }
    }

    private void setUIValues(@Nullable PhoneNumber value)
    {
        if (_config.getIncludedFields().contains(PhoneNumberField.phoneNumber))
        {
            assert _singleFieldEditor != null;
            _singleFieldEditor.setValue(value != null ? value.toExternalForm() : null);
        }
    }

    private PhoneNumber setUIValuesOnValue(@Nonnull PhoneNumber value, boolean committing)
    {
        if (_config.getIncludedFields().contains(PhoneNumberField.phoneNumber))
        {
            assert _singleFieldEditor != null;
            PhoneNumber fromField = PhoneNumber.valueOf(committing
                ? _singleFieldEditor.commitValue()
                : _singleFieldEditor.getUIValue());
            if (fromField != null)
            {
                value.setCountryCode(fromField.getCountryCode());
                value.setAreaCode(fromField.getAreaCode());
                value.setCentralOfficeCode(fromField.getCentralOfficeCode());
                value.setSubscriberCode(fromField.getSubscriberCode());
                value.setExtension(fromField.getExtension());
            }
        }
        return value;
    }

    /**
     * Set the config for this PhoneNumberValueEditor
     *
     * @param config the config
     *
     * @return this
     */
    @Nonnull
    public PhoneNumberValueEditor withConfig(@Nonnull PhoneNumberValueEditorConfig config)
    {
        _config = config;
        return this;
    }
}
