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

import javax.annotation.Nullable;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelPropertyViewer;

import net.proteusframework.ui.management.nav.NavigationAction;

/**
 * PropertyViewer for {@link Location}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/26/17
 */
public abstract class AbstractLocationPropertyViewer extends MIWTPageElementModelPropertyViewer
{
    /**
     * Instantiates a new Abstract location property viewer.
     */
    public AbstractLocationPropertyViewer()
    {
        super();

        addClassName("location-view");
    }

    /**
     * Create peristence actions
     *
     * @return the navigation action [ ]
     */
    protected abstract NavigationAction[] createPeristenceActions();

    @Override
    public void init()
    {
        super.init();
        super.lazyInit();

        setPersistenceActions(createPeristenceActions());
    }

    @Nullable
    @Override
    public LocationValueViewer getValueViewer()
    {
        return (LocationValueViewer)super.getValueViewer();
    }

    /**
     * Configure abstract location property viewer.
     *
     * @param value the value
     *
     * @return the abstract location property viewer
     */
    public AbstractLocationPropertyViewer configure(@Nullable Location value)
    {
        if(value == null)
        {
            throw new IllegalArgumentException("Unable to determine Location");
        }

        LocationValueViewer viewer = new LocationValueViewer();
        viewer.setLocation(value);
        setValueViewer(viewer);
        return this;
    }
}
