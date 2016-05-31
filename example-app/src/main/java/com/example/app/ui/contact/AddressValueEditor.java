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

import com.example.app.support.ContactUtil;
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
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ListComponentValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.users.model.Address;
import net.proteusframework.users.model.AddressType;
import net.proteusframework.users.model.ContactDataCategory;

import static com.example.app.ui.contact.AddressValueEditorLOK.*;

/**
 * {@link CompositeValueEditor} for {@link Address}
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@SuppressWarnings("unused")
@I18NFile(
    symbolPrefix = "com.lrlabs.ui.contact.AddressValueEditor",
    i18n = {
        @I18N(symbol = "Label Category", l10n = @L10N("Category")),
        @I18N(symbol = "Label Type", l10n = @L10N("Type")),
        @I18N(symbol = "Label Data Visibility", l10n = @L10N("Data Visibility")),
        @I18N(symbol = "Label Address Line FMT", l10n = @L10N("Address Line {0}")),
        @I18N(symbol = "Label City", l10n = @L10N("City")),
        @I18N(symbol = "Label State", l10n = @L10N("State")),
        @I18N(symbol = "Label Region", l10n = @L10N("Region")),
        @I18N(symbol = "Label Postal Code", l10n = @L10N("Postal Code")),
        @I18N(symbol = "Label Country", l10n = @L10N("Country")),
        @I18N(symbol = "Label Recipient Name", l10n = @L10N("Recipient Name")),
        @I18N(symbol = "Label Recipient Organization", l10n = @L10N("Recipient Organization")),
        @I18N(symbol = "Label Address Name", l10n = @L10N("Address Name"))
    }
)
public class AddressValueEditor extends CompositeValueEditor<Address>
{
    /**
     * Enum defining fields within the AddressValueEditor
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static enum AddressField
    {
        /** address field */
        category,
        /** address field */
        type,
        /** address field */
        dataVisibility,
        /** address field */
        addressLines,
        /** address field */
        city,
        /** address field */
        state,
        /** address field */
        regions,
        /** address field */
        postalCode,
        /** address field */
        country,
        /** address field */
        recipientName,
        /** address field */
        recipientOrganization,
        /** address field */
        addressName
    }

    /**
     * Class for configuring the display of {@link AddressValueEditor}
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    @SuppressWarnings({"RedundantFieldInitialization", "unused"})
    public static class AddressValueEditorConfig
    {
        private static class StateSelector extends ComboBoxValueEditor<String>
        {
            public StateSelector()
            {
                super(LABEL_STATE(), ContactUtil.getUSStateAbbreviationsWithNullValue(), null);
            }

            @Override
            public void setValue(@Nullable String value)
            {
                super.setValue(ContactUtil.getUSStateCode(value, getLocaleContext()));
            }
        }

        private final EnumSet<AddressField> _includedFields = EnumSet.of(
            AddressField.addressLines, AddressField.city, AddressField.state, AddressField.postalCode);
        private final EnumSet<AddressField> _requiredFields = EnumSet.of(
            AddressField.addressLines, AddressField.city, AddressField.state, AddressField.postalCode);
        private ContactDataCategory _defaultContactDataCategory = ContactDataCategory.UNKNOWN;
        private Supplier<ValueEditor<?>> _categorySupplier;
        private AddressType _defaultAddressType = AddressType.UNKNOWN;
        private Supplier<ValueEditor<?>> _typeSupplier;
        private Supplier<ValueEditor<?>> _dataVisibilitySupplier;
        private final List<Supplier<ValueEditor<String>>> _addressLinesSuppliers = new ArrayList<>();
        private Supplier<ValueEditor<?>> _citySupplier;
        private Supplier<ValueEditor<?>> _stateSupplier;
        private Supplier<ValueEditor<?>> _regionsSupplier;
        private Supplier<ValueEditor<?>> _postalCodeSupplier;
        private Supplier<ValueEditor<?>> _countrySupplier;
        private NameValueEditor.NameValueEditorConfig _recipientNameValueEditorConfig;
        private Supplier<ValueEditor<?>> _recipientOrganizationSupplier;
        private Supplier<ValueEditor<?>> _addressNameSupplier;
        private LocaleContext _localeContext;
        private TextSource _label;

        /**
         *   Get an EnumSet of all fields to be included in the Editor
         *   <br/>
         *   By default, the included fields are: addressLines, city, state, and postalCode
         *   @return an EnumSet
         */
        @Nonnull
        public EnumSet<AddressField> getIncludedFields()
        {
            return _includedFields;
        }
        /**
         *   Set an EnumSet of all fields to be included in the Editor
         *   @param includedFields an Array of all fields to be included in the Editor, may be null.  If null, no fields will
         *   be included.
         */
        public void setIncludedFields(@Nullable AddressField... includedFields)
        {
            _includedFields.clear();
            if(includedFields != null)
                Collections.addAll(_includedFields, includedFields);
        }

        /**
         *   Get An EnumSet of all required fields in the Editor.
         *   <br/>
         *   This only affects the first address line in the addressLines field
         *   <br/>
         *   By default, the required fields are: addressLines, city, state, and postalCode
         *   <br/><br/>
         *   This is only used if the Supplier for the field is not set explicitly.
         *   @return an EnumSet of all required fields in the Editor
         */
        @Nonnull
        public EnumSet<AddressField> getRequiredFields()
        {
            return _requiredFields;
        }
        /**
         *   Set An EnumSet of all required fields in the Editor.
         *   <br/>
         *   This only affects the first address line in the addressLines field
         *   <br/><br/>
         *   This is only used if the Supplier for the field is not set explicitly.
         *   @param requiredFields an Array of all required fields in the Editor
         */
        public void setRequiredFields(@Nullable AddressField... requiredFields)
        {
            _requiredFields.clear();
            if(requiredFields != null)
                Collections.addAll(_requiredFields, requiredFields);
        }

        /**
         *   Get the default ContactDataCategory to be used in the case of it not being selectable on the UI
         *   @return the default ContactDataCategory
         */
        @Nonnull
        public ContactDataCategory getDefaultContactDataCategory()
        {
            return _defaultContactDataCategory;
        }
        /**
         *   Set the default ContactDataCategory to be used in the case of it not being selectable on the UI
         *   @param defaultContactDataCategory the default ContactDataCategory
         */
        public void setDefaultContactDataCategory(@Nonnull ContactDataCategory defaultContactDataCategory)
        {
            _defaultContactDataCategory = defaultContactDataCategory;
        }

        /**
         *   Get the Supplier for the category property.  By default, this is just a comboBox editor.
         *   @return a Supplier for a ValueEditor
         */
        @SuppressWarnings("Duplicates")
        @Nonnull
        public Supplier<ValueEditor<?>> getCategorySupplier()
        {
            if(_categorySupplier != null)
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
                if(getRequiredFields().contains(AddressField.category))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }
        /**
         *   Set the Supplier for the category property.  By default, this is just a comboBox editor.
         *   @param categorySupplier a Supplier for a ValueEditor
         */
        public void setCategorySupplier(@Nullable Supplier<ValueEditor<?>> categorySupplier)
        {
            _categorySupplier = categorySupplier;
        }

        /**
         *   Get the default AddressType to be used in the case of it not being selectable on the UI
         *   @return the default AddressType
         */
        @Nonnull
        public AddressType getDefaultAddressType()
        {
            return _defaultAddressType;
        }
        /**
         *   Set the default AddressType to be used in the case of it not being selectable on the UI
         *   @param defaultAddressType the default AddressType
         */
        public void setDefaultAddressType(@Nonnull AddressType defaultAddressType)
        {
            _defaultAddressType = defaultAddressType;
        }

        /**
         *   Get the Supplier for the type property.  By default, this is just a comboBox editor.
         *   @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getTypeSupplier()
        {
            if(_typeSupplier != null)
            {
                return _typeSupplier;
            }
            return () -> {
                List<AddressType> types = new ArrayList<>(Arrays.asList(AddressType.values()));
                types.add(0, null);
                ComboBoxValueEditor<AddressType> editor = new ComboBoxValueEditor<>(LABEL_TYPE(), types, getDefaultAddressType());
                editor.addClassName("type");
                editor.setCellRenderer(new CustomCellRenderer(CommonButtonText.PLEASE_SELECT));
                if(getRequiredFields().contains(AddressField.type))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }
        /**
         *   Set the Supplier for the type property.  By default, this is just a comboBox editor.
         *   @param typeSupplier a Supplier for a ValueEditor
         */
        public void setTypeSupplier(@Nullable Supplier<ValueEditor<?>> typeSupplier)
        {
            _typeSupplier = typeSupplier;
        }

        /**
         *   Get the Supplier for the dataVisibility property.  By default, this is just a text editor.
         *   @return a Supplier for a ValueEditor
         */
        @SuppressWarnings("Duplicates")
        @Nonnull
        public Supplier<ValueEditor<?>> getDataVisibilitySupplier()
        {
            if(_dataVisibilitySupplier != null)
            {
                return _dataVisibilitySupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_DATA_VISIBILITY(), null);
                editor.addClassName("data-visibility");
                if(getRequiredFields().contains(AddressField.dataVisibility))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }
        /**
         *   Set the Supplier for the dataVisibility property.  By default, this is just a text editor.
         *   @param dataVisibilitySupplier a Supplier for a ValueEditor
         */
        public void setDataVisibilitySupplier(@Nullable Supplier<ValueEditor<?>> dataVisibilitySupplier)
        {
            _dataVisibilitySupplier = dataVisibilitySupplier;
        }

        /**
         *   Get the Suppliers for the address lines.  By default, this returns two text editors.
         *   @return a Suppliers for ValueEditors
         */
        @SuppressWarnings("unchecked")
        @Nonnull
        public List<Supplier<ValueEditor<String>>> getAddressLinesSuppliers()
        {
            if(_addressLinesSuppliers.isEmpty())
            {
                _addressLinesSuppliers.add(() -> {
                    TextSource label = TextSources.createText(LABEL_ADDRESS_LINE_FMT(), "1");
                    TextEditor editor = new TextEditor(label, null);
                    editor.addClassName("address-line-1");
                    //noinspection ConstantConditions
                    if(getRequiredFields().contains(AddressField.addressLines))
                    {
                        editor.setRequiredValueValidator();
                    }
                    return editor;
                });
                _addressLinesSuppliers.add(() -> {
                    TextSource label = TextSources.createText(LABEL_ADDRESS_LINE_FMT(), "2");
                    TextEditor editor = new TextEditor(label, null);
                    editor.addClassName("address-line-2");
                    return editor;
                });
            }
            return _addressLinesSuppliers;
        }
        /**
         *   Set the Suppliers for the address lines.  By default, this returns two text editors.
         *   @param addressLinesSuppliers Suppliers for a ValueEditors
         */
        public final void setAddressLinesSuppliers(@Nullable List<Supplier<ValueEditor<String>>> addressLinesSuppliers)
        {
            _addressLinesSuppliers.clear();
            if(addressLinesSuppliers != null)
            {
                _addressLinesSuppliers.addAll(addressLinesSuppliers);
            }
        }

        /**
         *   Get the Supplier for the city property.  By default, this is just a text editor.
         *   @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getCitySupplier()
        {
            if(_citySupplier != null)
            {
                return _citySupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_CITY(), null);
                editor.addClassName("city");
                if(getRequiredFields().contains(AddressField.city))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }
        /**
         *   Set the Supplier for the city property.  By default, this is just a text editor.
         *   @param citySupplier a Supplier for a ValueEditor
         */
        public void setCitySupplier(@Nullable Supplier<ValueEditor<?>> citySupplier)
        {
            _citySupplier = citySupplier;
        }

        /**
         *   Get the Supplier for the state property.  By default, this is just a comboBox editor.
         *   @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getStateSupplier()
        {
            if(_stateSupplier != null)
            {
                return _stateSupplier;
            }
            return () -> {
                ComboBoxValueEditor<String> editor = new StateSelector();
                editor.addClassName("state");
                if(getRequiredFields().contains(AddressField.state))
                {
                    editor.setRequiredValueValidator();
                }
                editor.setCellRenderer(new CustomCellRenderer(CommonButtonText.PLEASE_SELECT,
                    input -> ContactUtil.getUSStateName((String)input, _localeContext)));
                return editor;
            };
        }
        /**
         *   Set the Supplier for the state property.  By default, this is just a comboBox editor.
         *   @param stateSupplier a Supplier for a ValueEditor
         */
        public void setStateSupplier(@Nullable Supplier<ValueEditor<?>> stateSupplier)
        {
            _stateSupplier = stateSupplier;
        }

        /**
         *   Get the Supplier for the regionList property.  By default, this is just a blank ListComponent editor.
         *   @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getRegionsSupplier()
        {
            if(_regionsSupplier != null)
            {
                return _regionsSupplier;
            }
            return () -> {
                ListComponentValueEditor<String> editor = new ListComponentValueEditor<>(LABEL_REGION(), null, null);
                editor.addClassName("region");
                return editor;
            };
        }
        /**
         *   Set the Supplier for the regionList property.  By default, this is just a blank ListComponent editor.
         *   @param regionsSupplier a Supplier for a ValueEditor
         */
        public void setRegionsSupplier(@Nullable Supplier<ValueEditor<?>> regionsSupplier)
        {
            _regionsSupplier = regionsSupplier;
        }

        /**
         *   Get the Supplier for the postalCode property.  By default, this is just a text editor.
         *   @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getPostalCodeSupplier()
        {
            if(_postalCodeSupplier != null)
            {
                return _postalCodeSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_POSTAL_CODE(), null);
                editor.addClassName("postal-code");
                if(getRequiredFields().contains(AddressField.postalCode))
                {
                    editor.setRequiredValueValidator();
                }
                return editor;
            };
        }
        /**
         *   Set the Supplier for the postalCode property.  By default, this is just a text editor.
         *   @param postalCodeSupplier a Supplier for a ValueEditor
         */
        public void setPostalCodeSupplier(@Nullable Supplier<ValueEditor<?>> postalCodeSupplier)
        {
            _postalCodeSupplier = postalCodeSupplier;
        }

        /**
         *   Get the Supplier for the country property.  By default, this is just a text editor.
         *   @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getCountrySupplier()
        {
            if(_countrySupplier != null)
            {
                return _countrySupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_COUNTRY(), null);
                editor.addClassName("country");
                if(getRequiredFields().contains(AddressField.country))
                    editor.setRequiredValueValidator();
                return editor;
            };
        }
        /**
         *   Set the Supplier for the country property.  By default, this is just a text editor.
         *   @param countrySupplier a Supplier for a ValueEditor
         */
        public void setCountrySupplier(@Nullable Supplier<ValueEditor<?>> countrySupplier)
        {
            _countrySupplier = countrySupplier;
        }

        /**
         *   Get the {@link NameValueEditor.NameValueEditorConfig} for the Address's recipientName property
         *   @return a NameValueEditorConfig.  Uses default values by default.
         */
        @Nonnull
        public NameValueEditor.NameValueEditorConfig getRecipientNameValueEditorConfig()
        {
            if(_recipientNameValueEditorConfig != null)
            {
                return _recipientNameValueEditorConfig;
            }
            return new NameValueEditor.NameValueEditorConfig();
        }
        /**
         *   Set the {@link NameValueEditor.NameValueEditorConfig} for the Address's recipientName property
         *   @param recipientNameValueEditorConfig a NameValueEditorConfig.  Uses default values by default.
         */
        public void setRecipientNameValueEditorConfig(
            @Nullable NameValueEditor.NameValueEditorConfig recipientNameValueEditorConfig)
        {
            _recipientNameValueEditorConfig = recipientNameValueEditorConfig;
        }

        /**
         *   Get the Supplier for the recipientOrganization property.  By default, this is just a text editor.
         *   @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getRecipientOrganizationSupplier()
        {
            if(_recipientOrganizationSupplier != null)
            {
                return _recipientOrganizationSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_RECIPIENT_ORGANIZATION(), null);
                editor.addClassName("recipient-org");
                if(getRequiredFields().contains(AddressField.recipientOrganization))
                    editor.setRequiredValueValidator();
                return editor;
            };
        }
        /**
         *   Set the Supplier for the recipientOrganization property.  By default, this is just a text editor.
         *   @param recipientOrganizationSupplier a Supplier for a ValueEditor
         */
        public void setRecipientOrganizationSupplier(@Nullable Supplier<ValueEditor<?>> recipientOrganizationSupplier)
        {
            _recipientOrganizationSupplier = recipientOrganizationSupplier;
        }

        /**
         *   Get the Supplier for the addressName property.  By default, this is just a text editor.
         *   @return a Supplier for a ValueEditor
         */
        @Nonnull
        public Supplier<ValueEditor<?>> getAddressNameSupplier()
        {
            if(_addressNameSupplier != null)
            {
                return _addressNameSupplier;
            }
            return () -> {
                TextEditor editor = new TextEditor(LABEL_ADDRESS_NAME(), null);
                editor.addClassName("address-name");
                if(getRequiredFields().contains(AddressField.addressName))
                    editor.setRequiredValueValidator();
                return editor;
            };
        }
        /**
         *   Set the Supplier for the addressName property.  By default, this is just a text editor.
         *   @param addressNameSupplier a Supplier for a ValueEditor
         */
        public void setAddressNameSupplier(@Nullable Supplier<ValueEditor<?>> addressNameSupplier)
        {
            _addressNameSupplier = addressNameSupplier;
        }

        /**
         *   Get the label for the value editor
         *   @return the label
         */
        @Nullable
        public TextSource getLabel()
        {
            return _label;
        }

        /**
         *   Set the label for the value editor
         *   @param label the label
         */
        public void setLabel(@Nullable TextSource label)
        {
            _label = label;
        }

        private LocaleContext getLocaleContext()
        {
            return _localeContext;
        }

        private void setLocaleContext(LocaleContext localeContext)
        {
            _localeContext = localeContext;
        }
    }

    private AddressValueEditorConfig _config = new AddressValueEditorConfig();
    private final List<ValueEditor<String>> _addressLinesEditors = new ArrayList<>();

    /**
     *   Instantiate a new instance of AddressValueEditor
     */
    public AddressValueEditor()
    {
        this(null);
    }
    /**
     *   Instantiate a new instance of AddressValueEditor
     *   @param config the AddressValueEditorConfig for this AddressValueEditor.  If null, just uses default values.
     */
    public AddressValueEditor(@Nullable AddressValueEditorConfig config)
    {
        super(Address.class);
        if(config != null)
            _config = config;

        addClassName("prop-group address");
    }

    /**
     *   Get the AddressValueEditorConfig for this AddressValueEditor
     *   @return the config
     */
    @Nonnull
    public AddressValueEditorConfig getConfig()
    {
        return _config;
    }
    /**
     *   Set the AddressValueEditorConfig for this AddressValueEditor
     *   @param config the config
     *   @return this
     */
    @Nonnull
    public AddressValueEditor withConfig(@Nonnull AddressValueEditorConfig config)
    {
        _config = config;
        return this;
    }

    @Override
    public void init()
    {
        super.init();

        if(_config.getLocaleContext() == null)
        {
            _config.setLocaleContext(getLocaleContext());
        }

        if(_config.getLabel() != null)
            add(new Label(_config.getLabel()).withHTMLElement(HTMLElement.div)
                .addClassName("address-label")
                .addClassName("prop-group-title"));

        if(_config.getIncludedFields().contains(AddressField.category))
            addEditorForProperty(_config.getCategorySupplier(), "category");
        if(_config.getIncludedFields().contains(AddressField.type))
            addEditorForProperty(_config.getTypeSupplier(), "type");
        if(_config.getIncludedFields().contains(AddressField.dataVisibility))
            addEditorForProperty(_config.getDataVisibilitySupplier(), "dataVisibility");
        if(_config.getIncludedFields().contains(AddressField.addressLines))
        {
            _config.getAddressLinesSuppliers().forEach(als -> _addressLinesEditors.add(als.get()));
            Address value = getValue();
            if (value != null)
            {
                for (int i = 0; i < value.getAddressLineList().size() && i < _addressLinesEditors.size(); i++)
                {
                    _addressLinesEditors.get(i).setValue(value.getAddressLineList().get(i));
                }
            }
            _addressLinesEditors.forEach(this::add);
        }
        if(_config.getIncludedFields().contains(AddressField.city))
            addEditorForProperty(_config.getCitySupplier(), "city");
        if(_config.getIncludedFields().contains(AddressField.state))
            addEditorForProperty(_config.getStateSupplier(), "state");
        if(_config.getIncludedFields().contains(AddressField.regions))
            addEditorForProperty(_config.getRegionsSupplier(), "regionList");
        if(_config.getIncludedFields().contains(AddressField.postalCode))
            addEditorForProperty(_config.getPostalCodeSupplier(), "postalCode");
        if(_config.getIncludedFields().contains(AddressField.country))
            addEditorForProperty(_config.getCountrySupplier(), "country");
        if(_config.getIncludedFields().contains(AddressField.recipientName))
            addEditorForProperty(() -> new NameValueEditor(_config.getRecipientNameValueEditorConfig()), "recipientName");
        if(_config.getIncludedFields().contains(AddressField.recipientOrganization))
            addEditorForProperty(_config.getRecipientOrganizationSupplier(), "recipientOrganization");
        if(_config.getIncludedFields().contains(AddressField.addressName))
            addEditorForProperty(_config.getAddressNameSupplier(), "addressName");
    }

    @Override
    public boolean validateUIValue(Notifiable notifiable)
    {
        boolean valid = super.validateUIValue(notifiable);
        return _addressLinesEditors.stream().map(ale -> ale.validateUIValue(notifiable)).reduce(valid, Boolean::logicalAnd);
    }

    @Override
    public void setValue(@Nullable Address value)
    {
        super.setValue(value);

        if(isInited())
        {
            _addressLinesEditors.forEach(ale -> ale.setValue(null));
            if (value != null)
            {
                for (int i = 0; i < value.getAddressLineList().size() && i < _addressLinesEditors.size(); i++)
                {
                    _addressLinesEditors.get(i).setValue(value.getAddressLineList().get(i));
                }
            }
        }
    }

    @Nullable
    @Override
    public Address getUIValue(Level logErrorLevel)
    {
        Address result = super.getUIValue(logErrorLevel);
        if(result != null)
        {
            if(!_config.getIncludedFields().contains(AddressField.category))
                result.setCategory(_config.getDefaultContactDataCategory());
            if(_config.getIncludedFields().contains(AddressField.type))
                result.setAddressType(_config.getDefaultAddressType());
            result.getAddressLineList().clear();
            _addressLinesEditors.forEach(ale -> {
                String addressLine = ale.getUIValue(logErrorLevel);
                if (!StringFactory.isEmptyString(addressLine))
                {
                    result.getAddressLineList().add(addressLine);
                }
            });
        }
        return result;
    }

    @Nullable
    @Override
    public Address commitValue() throws MIWTException
    {
        Address result = super.commitValue();
        if(result != null)
        {
            if(!_config.getIncludedFields().contains(AddressField.category))
                result.setCategory(_config.getDefaultContactDataCategory());
            if(_config.getIncludedFields().contains(AddressField.type))
                result.setAddressType(_config.getDefaultAddressType());
            result.getAddressLineList().clear();
            _addressLinesEditors.forEach(ale -> {
                String addressLine = ale.commitValue();
                if (!StringFactory.isEmptyString(addressLine))
                {
                    result.getAddressLineList().add(addressLine);
                }
            });
        }
        return result;
    }

    @Override
    public ModificationState getModificationState()
    {
        ModificationState modState = super.getModificationState();
        if(!modState.isModified())
        {
            for (ValueEditor<String> ale : _addressLinesEditors)
            {
                if(ale.getModificationState().isModified()) return ModificationState.CHANGED;
            }
        }
        return modState;
    }
}
