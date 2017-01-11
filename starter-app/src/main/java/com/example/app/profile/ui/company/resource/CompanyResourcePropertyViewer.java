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

package com.example.app.profile.ui.company.resource;

import com.example.app.profile.model.repository.ResourceRepositoryItem;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.profile.ui.URLConfigurations;
import com.example.app.profile.ui.URLProperties;
import com.example.app.profile.ui.resource.AbstractProfileResourcePropertyViewer;
import com.example.app.support.ui.Application;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigDef;
import net.proteusframework.ui.management.URLProperty;

import static com.example.app.profile.ui.company.resource.CompanyResourcePropertyViewerLOK.COMPONENT_NAME;

/**
 * PropertyViewer for Resources on a Company
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/6/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.company.resource.CompanyResourcePropertyViewer",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Company Resource Viewer"))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Company.Resource.VIEW,
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.Company.Resource.VIEW,
        properties = @URLProperty(name = URLProperties.REPOSITORY_ITEM, type = ResourceRepositoryItem.class),
        pathInfoPattern = URLProperties.REPOSITORY_ITEM_PATH_INFO
    )
)
public class CompanyResourcePropertyViewer extends AbstractProfileResourcePropertyViewer
{
    /**
     * Instantiates a new Company resource property viewer.
     */
    public CompanyResourcePropertyViewer()
    {
        super();
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
    }

    @Override
    protected String getManagementApplicationFunction()
    {
        return ApplicationFunctions.Company.Resource.MANAGEMENT;
    }

    @Override
    protected String getEditorApplicationFunction()
    {
        return ApplicationFunctions.Company.Resource.EDIT;
    }

    @SuppressWarnings("unused")
        //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        ResourceRepositoryItem value = request.getPropertyValue(URLProperties.REPOSITORY_ITEM);

        configure(value);
    }
}
