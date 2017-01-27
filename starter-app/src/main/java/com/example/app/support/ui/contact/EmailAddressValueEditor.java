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

import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.validation.EmailValidator;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.EmailAddress;

import static com.example.app.support.ui.contact.EmailAddressValueEditorLOK.*;


/**
 * {@link CompositeValueEditor} for {@link EmailAddress}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/2/15 1:23 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.support.ui.contact.EmailAddressValueEditor",
    i18n = {
        @I18N(symbol = "Label Data Visibility", l10n = @L10N("Data Visibility")),
        @I18N(symbol = "Label Category", l10n = @L10N("Category")),
        @I18N(symbol = "Label Email", l10n = @L10N("Email")),
        @I18N(symbol = "Error Message Email Not Valid", l10n = @L10N("Email must be a valid email address"))
    }
)
public class EmailAddressValueEditor extends CompositeValueEditor<EmailAddress>
{
    /**
     * Enum defining fields within the EmailAddressValueEditor
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static enum EmailAddressField
    {
        /** email address field */
        category,
        /** email address field */
        dataVisibility,
        /** email address field */
        email
    }

    /**
     * Class for configuring the display of {@link EmailAddressValueEditor}
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class EmailAddressValueEditorConfig
    {
        private final EnumSet<EmailAddressField> _includedFields = EnumSet.of(EmailAddressField.email);
        private final EnumSet<EmailAddressField> _requiredFields = EnumSet.of(EmailAddressField.email);
        private ContactDataCategory _defaultContactDataCategory = ContactDataCategory.UNKNOWN;
        private Supplier<ValueEditor<?>> _categorySupplier;
        private Supplier<ValueEditor<?>> _dataVisibilitySupplier;
        private Supplier<ValueEditor<?>> _emailSupplier;

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
                List<ContactDataCategory> categories = new ArrayList<>(Arrays.asList(ContactDataCategory.values()));
                categories.add(0, null);
                ComboBoxValueEditor<ContactDataCategory> editor = new ComboBoxValueEditor<>(
                    LABEL_CATEGORY(), categories, getDefaultContactDataCategory());
                editor.addClassName("category");
                editor.setCellRenderer(new CustomCellRenderer(CommonButtonText.PLEASE_SELECT));
                if (getRequiredFields().contains(EmailAddressField.category))
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
         * Get an EnumSet of all required fields for the EmailAddress Editor.
         * <br>
         * This is only used if the Supplier for the field has not been set.
         * <br>
         * By default, the only required field is {@link EmailAddressField#email}
         *
         * @return EnumSet of required fields
         */
        @Nonnull
        public EnumSet<EmailAddressField> getRequiredFields()
        {
            return _requiredFields;
        }

        /**
         * Set an EnumSet of all required fields for the EmailAddress Editor.
         * <br>
         * This is only used if the Supplier for the field has not been set.
         * <br>
         * By default, the only required field is {@link EmailAddressField#email}
         *
         * @param requiredFields Array of required fields
         */
        public void setRequiredFields(@Nullable EmailAddressField... requiredFields)
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
                if (getRequiredFields().contains(EmailAddressField.dataVisibility))
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
         * Get the Supplier for the email property.  By default, this is a text editor that includes
         * validation that the input is a valid email address
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getEmailSupplier()
        {
            if (_emailSupplier != null)
            {
                return _emailSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_EMAIL(), null);
                editor.addClassName("email");
                editor.setInputType(Field.InputType.email);
                editor.setValueValidator(EmailValidator.custom(ERROR_MESSAGE_EMAIL_NOT_VALID(), false)
                    .withNotificationSourceSetter((validator, component, notification) -> notification.setSource(editor)));
                editor.setRequiredValueValidator();
                return editor;
            };
        }

        /**
         * Set the Supplier for the email property.  By default, this is a text editor that includes
         * validation that the input is a valid email address
         *
         * @param emailSupplier a Supplier for a ValueEditor
         */
        public void setEmailSupplier(@Nullable Supplier<ValueEditor<?>> emailSupplier)
        {
            _emailSupplier = emailSupplier;
        }

        /**
         * Get an EnumSet of all included fields for the EmailAddress Editor.
         * <br>
         * By default, the only included field is {@link EmailAddressField#email}
         *
         * @return EnumSet of included fields
         */
        @Nonnull
        public EnumSet<EmailAddressField> getIncludedFields()
        {
            return _includedFields;
        }

        /**
         * Set an EnumSet of all included fields for the EmailAddress Editor.
         * <br>
         * By default, the only included field is {@link EmailAddressField#email}
         *
         * @param includedFields Array of included fields
         */
        public void setIncludedFields(@Nullable EmailAddressField... includedFields)
        {
            _includedFields.clear();
            if (includedFields != null)
                Collections.addAll(_includedFields, includedFields);
        }
    }

    private EmailAddressValueEditorConfig _config = new EmailAddressValueEditorConfig();

    /**
     * Instantiate a new instance of EmailAddressValueEditor
     */
    public EmailAddressValueEditor()
    {
        this(null);
    }

    /**
     * Instantiate a new instance of EmailAddressValueEditor
     *
     * @param config the config for this EmailAddressValueEditor.  If null, uses a config with default values.
     */
    public EmailAddressValueEditor(@Nullable EmailAddressValueEditorConfig config)
    {
        super(EmailAddress.class);
        if (config != null)
            _config = config;
        addClassName("prop-group email-address");
        setNewInstanceSupplier(() -> new EmailAddress("hint: you didn't set the email field!"));
    }

    /**
     * Get the config for this EmailAddressValueEditor
     *
     * @return the config
     */
    @Nonnull
    public EmailAddressValueEditorConfig getConfig()
    {
        return _config;
    }

    @SuppressWarnings("Duplicates")
    @Nullable
    @Override
    public EmailAddress getUIValue(Level logErrorLevel)
    {
        EmailAddress result = super.getUIValue(logErrorLevel);
        if (result != null)
        {
            if (!_config.getIncludedFields().contains(EmailAddressField.category))
                result.setCategory(_config.getDefaultContactDataCategory());
            if (StringFactory.isEmptyString(result.getEmail()))
                result = null;
        }
        return result;
    }

    @SuppressWarnings("Duplicates")
    @Nullable
    @Override
    public EmailAddress commitValue() throws MIWTException
    {
        EmailAddress result = super.commitValue();
        if (result != null)
        {
            if (!_config.getIncludedFields().contains(EmailAddressField.category))
                result.setCategory(_config.getDefaultContactDataCategory());
            if (StringFactory.isEmptyString(result.getEmail()))
                result = null;
        }
        return result;
    }

    @Override
    public void init()
    {
        super.init();

        if (_config.getIncludedFields().contains(EmailAddressField.category))
            addEditorForProperty(_config.getCategorySupplier(), "category");
        if (_config.getIncludedFields().contains(EmailAddressField.dataVisibility))
            addEditorForProperty(_config.getDataVisibilitySupplier(), "dataVisibility");
        if (_config.getIncludedFields().contains(EmailAddressField.email))
            addEditorForProperty(_config.getEmailSupplier(), "email");
    }

    /**
     * Set the config for this EmailAddressValueEditor
     *
     * @param config the config
     *
     * @return this
     */
    @Nonnull
    public EmailAddressValueEditor withConfig(@Nonnull EmailAddressValueEditorConfig config)
    {
        _config = config;
        return this;
    }
}
