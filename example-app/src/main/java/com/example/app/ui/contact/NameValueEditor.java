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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Supplier;

import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.users.model.Name;

import static com.example.app.ui.contact.NameValueEditorLOK.*;

/**
 * {@link CompositeValueEditor} for {@link Name}
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@SuppressWarnings("unused")
@I18NFile(
    symbolPrefix = "com.lrlabs.ui.contact.NameValueEditor",
    i18n = {
        @I18N(symbol = "Label Data Visibility", l10n = @L10N("Data Visibility")),
        @I18N(symbol = "Label Form Of Address", l10n = @L10N("Title")),
        @I18N(symbol = "Label First", l10n = @L10N("First Name")),
        @I18N(symbol = "Label Preferred Given Name", l10n = @L10N("Preferred Given Name")),
        @I18N(symbol = "Label Middle Name", l10n = @L10N("Middle Name")),
        @I18N(symbol = "Label Last Name", l10n = @L10N("Last Name")),
        @I18N(symbol = "Label Preferred Family Name", l10n = @L10N("Preferred Family Name")),
        @I18N(symbol = "Label Suffix", l10n = @L10N("Suffix")),
        @I18N(symbol = "Label Formatted Name", l10n = @L10N("Formatted Name"))
    }
)
public class NameValueEditor extends CompositeValueEditor<Name>
{
    /**
     * Enum defining fields within the NameValueEditor
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static enum NameField
    {
        /** name field */
        dataVisibility,
        /** name field */
        formOfAddress,
        /** name field */
        first,
        /** name field */
        preferredGivenName,
        /** name field */
        middle,
        /** name field */
        last,
        /** name field */
        preferredFamilyName,
        /** name field */
        suffix,
        /** name field */
        formattedName
    }

    /**
     * Class for configuring the display of a {@link NameValueEditor}
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class NameValueEditorConfig
    {
        private final EnumSet<NameField> _includedFields = EnumSet.of(
            NameField.formOfAddress, NameField.first, NameField.middle, NameField.last, NameField.suffix);
        private final EnumSet<NameField> _requiredFields = EnumSet.of(NameField.first, NameField.last);
        private Supplier<ValueEditor<?>> _dataVisibilitySupplier;
        private Supplier<ValueEditor<?>> _formOfAddressSupplier;
        private Supplier<ValueEditor<?>> _firstSupplier;
        private Supplier<ValueEditor<?>> _preferredGivenNameSupplier;
        private Supplier<ValueEditor<?>> _middleSupplier;
        private Supplier<ValueEditor<?>> _lastSupplier;
        private Supplier<ValueEditor<?>> _preferredFamilyNameSupplier;
        private Supplier<ValueEditor<?>> _suffixSupplier;
        private Supplier<ValueEditor<?>> _formattedNameSupplier;

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
                if (getRequiredFields().contains(NameField.dataVisibility))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Get an EnumSet of all required fields for the Editor.
         * <br/>
         * By default, this contains first and last
         * <br/>
         * This is only used if the Supplier for the field is not set
         *
         * @return an EnumSet of all required fields for the Editor
         */
        @Nonnull
        public EnumSet<NameField> getRequiredFields()
        {
            return _requiredFields;
        }

        /**
         * Get an EnumSet of all required fields for the Editor.
         * <br/>
         * By default, this contains first and last
         * <br/>
         * This is only used if the Supplier for the field is not set
         *
         * @param requiredFields an Array of all required fields for the Editor
         */
        public void setRequiredFields(@Nullable NameField... requiredFields)
        {
            _requiredFields.clear();
            if (requiredFields != null)
                Collections.addAll(_requiredFields, requiredFields);
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
         * Get the Supplier for the first property.  By default, this is just a text editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getFirstSupplier()
        {
            if (_firstSupplier != null)
            {
                return _firstSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_FIRST(), null);
                if (getRequiredFields().contains(NameField.first))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the first property.  By default, this is just a text editor.
         *
         * @param firstSupplier a Supplier for a ValueEditor
         */
        public void setFirstSupplier(@Nullable Supplier<ValueEditor<?>> firstSupplier)
        {
            _firstSupplier = firstSupplier;
        }

        /**
         * Get the Supplier for the formOfAddress property.  By default, this is just a text editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getFormOfAddressSupplier()
        {
            if (_formOfAddressSupplier != null)
            {
                return _formOfAddressSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_FORM_OF_ADDRESS(), null);
                if (getRequiredFields().contains(NameField.formOfAddress))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the formOfAddress property.  By default, this is just a text editor.
         *
         * @param formOfAddressSupplier a Supplier for a ValueEditor
         */
        public void setFormOfAddressSupplier(@Nullable Supplier<ValueEditor<?>> formOfAddressSupplier)
        {
            _formOfAddressSupplier = formOfAddressSupplier;
        }

        /**
         * Get the Supplier for the formattedName property.  By default, this is just a text editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getFormattedNameSupplier()
        {
            if (_formattedNameSupplier != null)
            {
                return _formattedNameSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_FORMATTED_NAME(), null);
                if (getRequiredFields().contains(NameField.formattedName))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the formattedName property.  By default, this is just a text editor.
         *
         * @param formattedNameSupplier a Supplier for a ValueEditor
         */
        public void setFormattedNameSupplier(@Nullable Supplier<ValueEditor<?>> formattedNameSupplier)
        {
            _formattedNameSupplier = formattedNameSupplier;
        }

        /**
         * Get an EnumSet of all included fields for the Editor.
         * <br/>
         * By default, this contains formOfAddress, first, middle, last, and suffix
         *
         * @return an EnumSet of all included fields for the Editor
         */
        @Nonnull
        public EnumSet<NameField> getIncludedFields()
        {
            return _includedFields;
        }

        /**
         * Set an EnumSet of all included fields for the Editor.
         * <br/>
         * By default, this contains formOfAddress, first, middle, last, and suffix
         *
         * @param includedFields an Array of all included fields for the Editor
         */
        public void setIncludedFields(@Nullable NameField... includedFields)
        {
            _includedFields.clear();
            if (includedFields != null)
                Collections.addAll(_includedFields, includedFields);
        }

        /**
         * Get the Supplier for the last property.  By default, this is just a text editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getLastSupplier()
        {
            if (_lastSupplier != null)
            {
                return _lastSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_LAST_NAME(), null);
                if (getRequiredFields().contains(NameField.last))
                    editor.setRequiredValueValidator();
                return editor;
            };
        }

        /**
         * Set the Supplier for the last property.  By default, this is just a text editor.
         *
         * @param lastSupplier a Supplier for a ValueEditor
         */
        public void setLastSupplier(@Nullable Supplier<ValueEditor<?>> lastSupplier)
        {
            _lastSupplier = lastSupplier;
        }

        /**
         * Get the Supplier for the middle property.  By default, this is just a text editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getMiddleSupplier()
        {
            if (_middleSupplier != null)
            {
                return _middleSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_MIDDLE_NAME(), null);
                if (getRequiredFields().contains(NameField.middle))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the middle property.  By default, this is just a text editor.
         *
         * @param middleSupplier a Supplier for a ValueEditor
         */
        public void setMiddleSupplier(@Nullable Supplier<ValueEditor<?>> middleSupplier)
        {
            _middleSupplier = middleSupplier;
        }

        /**
         * Get the Supplier for the preferredFamilyName property.  By default, this is just a text editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getPreferredFamilyNameSupplier()
        {
            if (_preferredFamilyNameSupplier != null)
            {
                return _preferredFamilyNameSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_PREFERRED_FAMILY_NAME(), null);
                if (getRequiredFields().contains(NameField.preferredFamilyName))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the preferredFamilyName property.  By default, this is just a text editor.
         *
         * @param preferredFamilyNameSupplier a Supplier for a ValueEditor
         */
        public void setPreferredFamilyNameSupplier(@Nullable Supplier<ValueEditor<?>> preferredFamilyNameSupplier)
        {
            _preferredFamilyNameSupplier = preferredFamilyNameSupplier;
        }

        /**
         * Get the Supplier for the preferredGivenName property.  By default, this is just a text editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getPreferredGivenNameSupplier()
        {
            if (_preferredGivenNameSupplier != null)
            {
                return _preferredGivenNameSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_PREFERRED_GIVEN_NAME(), null);
                if (getRequiredFields().contains(NameField.preferredGivenName))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the preferredGivenName property.  By default, this is just a text editor.
         *
         * @param preferredGivenNameSupplier a Supplier for a ValueEditor
         */
        public void setPreferredGivenNameSupplier(@Nullable Supplier<ValueEditor<?>> preferredGivenNameSupplier)
        {
            _preferredGivenNameSupplier = preferredGivenNameSupplier;
        }

        /**
         * Get the Supplier for the suffix property.  By default, this is just a text editor.
         *
         * @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getSuffixSupplier()
        {
            if (_suffixSupplier != null)
            {
                return _suffixSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_SUFFIX(), null);
                if (getRequiredFields().contains(NameField.suffix))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }

        /**
         * Set the Supplier for the suffix property.  By default, this is just a text editor.
         *
         * @param suffixSupplier a Supplier for a ValueEditor
         */
        public void setSuffixSupplier(@Nullable Supplier<ValueEditor<?>> suffixSupplier)
        {
            _suffixSupplier = suffixSupplier;
        }
    }

    private NameValueEditorConfig _config = new NameValueEditorConfig();

    /**
     * Instantiate a new NameValueEditor
     */
    public NameValueEditor()
    {
        this(null);
    }

    /**
     * Instantiate a new NameValueEditor
     *
     * @param config the NameValueEditorConfig.  If null, uses NameValueEditorConfig with all default values.
     */
    public NameValueEditor(@Nullable NameValueEditorConfig config)
    {
        super(Name.class);
        if (config != null)
        {
            _config = config;
        }
    }

    /**
     * Get the NameValueEditorConfig for this NameValueEditor
     *
     * @return the NameValueEditorConfig
     */
    @Nonnull
    public NameValueEditorConfig getConfig()
    {
        return _config;
    }

    @Override
    public void init()
    {
        super.init();

        addClassName("name-editor");

        if (_config.getIncludedFields().contains(NameField.dataVisibility))
            addEditorForProperty(_config.getDataVisibilitySupplier(), "dataVisibility");
        if (_config.getIncludedFields().contains(NameField.formOfAddress))
            addEditorForProperty(_config.getFormOfAddressSupplier(), "formOfAddress");
        if (_config.getIncludedFields().contains(NameField.first))
            addEditorForProperty(_config.getFirstSupplier(), "first");
        if (_config.getIncludedFields().contains(NameField.preferredGivenName))
            addEditorForProperty(_config.getPreferredGivenNameSupplier(), "preferredGivenName");
        if (_config.getIncludedFields().contains(NameField.middle))
            addEditorForProperty(_config.getMiddleSupplier(), "middle");
        if (_config.getIncludedFields().contains(NameField.last))
            addEditorForProperty(_config.getLastSupplier(), "last");
        if (_config.getIncludedFields().contains(NameField.preferredFamilyName))
            addEditorForProperty(_config.getPreferredFamilyNameSupplier(), "preferredFamilyName");
        if (_config.getIncludedFields().contains(NameField.suffix))
            addEditorForProperty(_config.getSuffixSupplier(), "suffix");
        if (_config.getIncludedFields().contains(NameField.formattedName))
            addEditorForProperty(_config.getFormattedNameSupplier(), "formattedName");
    }

    /**
     * Set the NameValueEditorConfig for this NameValueEditor
     *
     * @param config the NameValueEditorConfig
     *
     * @return this
     */
    @Nonnull
    public NameValueEditor withConfig(@Nonnull NameValueEditorConfig config)
    {
        _config = config;
        return this;
    }
}
