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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.i2rd.hr.miwt.AddressCellRenderer;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.users.model.EmailAddress;
import net.proteusframework.users.model.PhoneNumber;

import static com.example.app.support.service.Functions.opt;
import static net.proteusframework.core.locale.TextSources.createText;
import static net.proteusframework.ui.miwt.util.CommonColumnText.*;

/**
 * Value Viewer for {@link Location}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/26/17
 */
@Configurable
public class LocationValueViewer extends Container
{
    @Autowired private EntityRetriever _er;

    private Location _location;

    /**
     * Instantiates a new Location value viewer.
     */
    public LocationValueViewer()
    {
        super();
    }

    /**
     * Gets location.
     *
     * @return the location
     */
    public Location getLocation()
    {
        return _er.reattachIfNecessary(_location);
    }

    /**
     * Sets location.
     *
     * @param location the location
     */
    public void setLocation(Location location)
    {
        _location = location;
        if(isInited())
            _setupUI();
    }

    /**
     * Set the {@link #_location location} property
     * returning this.
     *
     * @param location the location.
     * @return this.
     * @see #setLocation(Location)
     */
    public LocationValueViewer withLocation(Location location)
    {
        setLocation(location);
        return this;
    }

    @Override
    public void init()
    {
        super.init();

        _setupUI();
    }

    private void _setupUI()
    {
        removeAllComponents();

        final Container nameCon = of("prop name", NAME, new Label(getLocation().getName()));
        final Container addressCon = of("prop address", ADDRESS, new AddressCellRenderer(getLocation().getAddress()));
        final Container emailCon = of("prop email", EMAIL, new Label(createText(
            opt(getLocation().getEmailAddress()).map(EmailAddress::getEmail).orElse(""))));
        final Container phoneCon = of("prop phone", PHONE, new Label(createText(
            opt(getLocation().getPhoneNumber()).map(PhoneNumber::toExternalForm).orElse(""))));
        final Container statusCon = of("prop status", STATUS, new Label(getLocation().getStatus().getName()));

        add(nameCon);
        add(addressCon);
        add(emailCon);
        add(phoneCon);
        add(statusCon);
    }
}
