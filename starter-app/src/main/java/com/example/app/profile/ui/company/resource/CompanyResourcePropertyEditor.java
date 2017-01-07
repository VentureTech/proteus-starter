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

import com.example.app.profile.model.Profile;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.profile.ui.URLConfigurations;
import com.example.app.profile.ui.resource.AbstractProfileResourcePropertyEditor;
import com.example.app.repository.model.Repository;
import com.example.app.repository.model.ResourceRepositoryItem;
import com.example.app.repository.ui.URLProperties;
import com.example.app.resource.model.ResourceType;
import com.example.app.resource.ui.ResourceTypeURLConfigPropertyConverter;
import com.example.app.support.ui.Application;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigDef;
import net.proteusframework.ui.management.URLProperty;

import static com.example.app.profile.ui.company.resource.CompanyResourcePropertyEditorLOK.COMPONENT_NAME;

/**
 * Property Editor for Resources on a Company
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/6/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.company.resource.CompanyResourcePropertyEditor",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Company Resource Editor"))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Company.Resource.EDIT,
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.Company.Resource.EDIT,
        properties = {
            @URLProperty(name = URLProperties.REPOSITORY_ITEM, type = ResourceRepositoryItem.class),
            @URLProperty(
                name = com.example.app.resource.ui.URLProperties.RESOURCE_TYPE,
                type = ResourceType.class,
                converter = ResourceTypeURLConfigPropertyConverter.class),
            @URLProperty(name = URLProperties.REPOSITORY, type = Repository.class),
            @URLProperty(name = URLProperties.REPOSITORY_OWNER, type = Profile.class)
        },
        pathInfoPattern = URLProperties.REPOSITORY_ITEM_PATH_INFO
                          + com.example.app.resource.ui.URLProperties.RESOURCE_TYPE_PATH_INFO
                          + URLProperties.REPOSITORY_PATH_INFO
                          + URLProperties.REPOSITORY_OWNER_PATH_INFO
    )
)
public class CompanyResourcePropertyEditor extends AbstractProfileResourcePropertyEditor
{
    /**
     * Instantiates a new Company resource property editor.
     */
    public CompanyResourcePropertyEditor()
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
    protected String getViewerApplicationFunction()
    {
        return ApplicationFunctions.Company.Resource.VIEW;
    }

    @SuppressWarnings("unused")
        //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        ResourceRepositoryItem value = request.getPropertyValue(URLProperties.REPOSITORY_ITEM);
        ResourceType resourceType = request.getPropertyValue(com.example.app.resource.ui.URLProperties.RESOURCE_TYPE);
        Repository repo = request.getPropertyValue(URLProperties.REPOSITORY);
        Profile owner = request.getPropertyValue(URLProperties.REPOSITORY_OWNER);

        configure(value, resourceType, repo, owner);
    }
}
