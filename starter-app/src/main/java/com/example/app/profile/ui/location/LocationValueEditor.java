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

package com.example.app.profile.ui.location;

import com.example.app.profile.model.location.Location;
import com.example.app.profile.model.location.LocationStatus;
import com.example.app.support.service.AppUtil;
import com.example.app.support.ui.CommonEditorFields;
import com.example.app.support.ui.contact.AddressValueEditor;
import com.example.app.support.ui.contact.AddressValueEditor.AddressValueEditorConfig;
import com.example.app.support.ui.contact.EmailAddressValueEditor;
import com.example.app.support.ui.contact.EmailAddressValueEditor.EmailAddressValueEditorConfig;
import com.example.app.support.ui.contact.PhoneNumberValueEditor;
import com.example.app.support.ui.contact.PhoneNumberValueEditor.PhoneNumberValueEditorConfig;

import java.util.EnumSet;

import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.util.CommonColumnText;

/**
 * ValueEditor implementation for {@link Location}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/25/17
 */
public class LocationValueEditor extends CompositeValueEditor<Location>
{
    /**
     * Instantiates a new Location value editor.
     */
    public LocationValueEditor()
    {
        super(Location.class);
    }

    @Override
    public void init()
    {
        super.init();

        CommonEditorFields.addNameEditor(this);

        addEditorForProperty(() -> {
            AddressValueEditorConfig cfg = new AddressValueEditorConfig();
            return new AddressValueEditor(cfg);
        }, Location::getAddress, Location::setAddress);

        addEditorForProperty(() -> {
            EmailAddressValueEditorConfig cfg = new EmailAddressValueEditorConfig();
            return new EmailAddressValueEditor(cfg);
        }, Location::getEmailAddress, Location::setEmailAddress);

        addEditorForProperty(() -> {
            PhoneNumberValueEditorConfig cfg = new PhoneNumberValueEditorConfig();
            return new PhoneNumberValueEditor(cfg);
        }, Location::getPhoneNumber, Location::setPhoneNumber);

        addEditorForProperty(() -> {
            ComboBoxValueEditor<LocationStatus> editor = new ComboBoxValueEditor<>(CommonColumnText.STATUS,
                AppUtil.nullFirst(EnumSet.allOf(LocationStatus.class)), null);
            editor.setRequiredValueValidator();
            return editor;
        }, Location::getStatus, Location::setStatus);
    }
}
