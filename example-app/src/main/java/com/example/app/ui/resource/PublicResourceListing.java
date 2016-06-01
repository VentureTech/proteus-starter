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

package com.example.app.ui.resource;

import com.example.app.model.repository.Repository;
import com.example.app.ui.Application;
import com.example.app.ui.ApplicationFunctions;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.resource.ClassPathResourceLibraryHelper;
import net.proteusframework.ui.management.ApplicationFunction;

import static com.example.app.ui.resource.PublicResourceListingLOK.COMPONENT_NAME;

/**
 * UI for listing public resources
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 4/18/16 8:16 AM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.resource.PublicResourceListing",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Public Resources"))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.ResourceRepositoryItem.PUBLIC_LISTING,
    description = "Proivdes a listing of public Resources"
)
public class PublicResourceListing extends ResourceListing
{
    @Autowired
    private ClassPathResourceLibraryHelper _classPathResourceLibraryHelper;

    /**
     * Instantiates a new Public Resource Listing
     */
    public PublicResourceListing()
    {
        super();
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
        addClassName("public-resources");
        setIncludePublicOnly(true);
        setSortMethods(EnumSet.allOf(SortMethod.class));
    }

    /**
     * Post construction.
     */
    @PostConstruct
    public void postConstruct()
    {
        setIcon(_classPathResourceLibraryHelper.createResource("lr/cms/icons/plan-resources.png"));
    }

    @Override
    protected List<Repository> getRepositories()
    {
        return Collections.emptyList();
    }

}
