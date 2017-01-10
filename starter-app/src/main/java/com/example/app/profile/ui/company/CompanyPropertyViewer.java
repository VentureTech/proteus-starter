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

package com.example.app.profile.ui.company;

import com.example.app.profile.model.company.Company;
import com.example.app.profile.ui.ApplicationFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;

import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyViewer;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;

/**
 * PropertyViewer for {@link Company}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 6/28/16 10:14 AM
 */
@Configurable
public class CompanyPropertyViewer extends PropertyViewer
{
    @Autowired private CompanyUIPermissionCheck _permissionCheck;

    /**
     * Instantiates a new company property viewer.
     */
    public CompanyPropertyViewer()
    {
        super();

        addClassName("company-view");
    }

    @Override
    public void init()
    {
        super.init();
        super.lazyInit();

        NavigationAction editAction = CommonActions.EDIT.navAction();
        editAction.configure().toPage(ApplicationFunctions.Company.EDIT)
            .withSourceComponent(this)
            .usingCurrentURLData();
        editAction.setTarget(this, "close");

        NavigationAction backAction = CommonActions.BACK.navAction();
        backAction.configure().toReturnPath(ApplicationFunctions.Company.MANAGEMENT)
            .withSourceComponent(this)
            .usingCurrentURLData();
        backAction.setTarget(this, "close");

        setPersistenceActions(editAction, backAction);
    }

    @Nullable
    @Override
    public CompanyValueViewer getValueViewer()
    {
        return (CompanyValueViewer)super.getValueViewer();
    }

    /**
     * Configure company property viewer.
     *
     * @param value the value
     *
     * @return the company property viewer
     */
    public CompanyPropertyViewer configure(Company value)
    {
        _permissionCheck.checkPermissionsForCurrent(Event.getRequest(), "You do not have the correct role to view this page");

        if(value == null)
        {
            throw new IllegalArgumentException("Unable to determine Development Provider");
        }

        CompanyValueViewer viewer = new CompanyValueViewer();
        viewer.setCompany(value);
        setValueViewer(viewer);
        return this;
    }
}
