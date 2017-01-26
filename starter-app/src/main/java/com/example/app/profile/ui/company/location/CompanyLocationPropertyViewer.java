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

package com.example.app.profile.ui.company.location;

import com.example.app.profile.model.location.Location;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.profile.ui.URLConfigurations;
import com.example.app.profile.ui.URLProperties;
import com.example.app.profile.ui.location.AbstractLocationPropertyViewer;
import com.example.app.support.ui.Application;
import org.springframework.beans.factory.annotation.Autowired;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigDef;
import net.proteusframework.ui.management.URLProperty;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;

import static com.example.app.profile.ui.company.location.CompanyLocationPropertyViewerLOK.COMPONENT_NAME;

/**
 * Property Viewer for Company Locations
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/26/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.company.location.CompanyLocationPropertyViewer",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Company Location Viewer"))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Company.Location.VIEW,
    description = "Company Location Viewer",
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.Company.Location.VIEW,
        properties = @URLProperty(name = URLProperties.LOCATION, type = Location.class),
        pathInfoPattern = URLProperties.LOCATION_PATH_INFO
    )
)
public class CompanyLocationPropertyViewer extends AbstractLocationPropertyViewer
{
    @Autowired private CompanyLocationUIPermissionCheck _permissionCheck;

    /**
     * Instantiates a new Company location property viewer.
     */
    public CompanyLocationPropertyViewer()
    {
        super();
        addCategory(CmsCategory.ClientBackend);
        setName(COMPONENT_NAME());
    }

    @Override
    protected NavigationAction[] createPeristenceActions()
    {
        NavigationAction edit = CommonActions.EDIT.navAction();
        edit.configure().toPage(ApplicationFunctions.Company.Location.EDIT).usingCurrentURLData().withSourceComponent(this);
        edit.setTarget(this, "close");

        NavigationAction back = CommonActions.BACK.navAction();
        back.configure().toReturnPath(ApplicationFunctions.Company.Location.MANAGEMENT)
            .usingCurrentURLData()
            .withSourceComponent(this);
        back.setTarget(this, "close");
        return _permissionCheck.checkCanCurrentUserModify(Event.getRequest())
            ? new NavigationAction[]{edit, back}
            : new NavigationAction[]{back};
    }

    @SuppressWarnings("unused") //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        _permissionCheck.checkPermissionsForCurrent(Event.getRequest(), "Unsifficient permissions to view page.");
        super.configure((Location)request.getPropertyValue(URLProperties.LOCATION));
    }
}
