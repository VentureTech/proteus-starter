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

package com.example.app.ui;


import com.example.app.model.company.Company;
import com.example.app.model.profile.Profile;
import com.example.app.model.repository.Repository;
import com.example.app.model.repository.RepositoryItem;
import com.example.app.model.resource.ResourceType;
import com.example.app.model.user.User;

import net.proteusframework.ui.management.URLProperty;


/**
 * ApplicationFunction properties for {@link URLProperty}.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public final class URLProperties
{
    /** URL Property to signify a {@link User} */
    public static final String USER = "user";
    /** URL Property to signify a {@link RepositoryItem} */
    public static final String REPOSITORY_ITEM = "repo-item";
    /** URL Property to signify a {@link ResourceType} */
    public static final String RESOURCE_TYPE = "resource-type";
    /** URL Property to signify a {@link Profile} owning a {@link Repository} */
    public static final String REPOSITORY_OWNER = "repository-owner";
    /** URL Property to signify a {@link Repository} */
    public static final String REPOSITORY = "repository";
    /** URL Property to signify a boolean flag - true or false */
    public static final String COPY = "copy";
    /** URL Property to specify a context for saving an entity */
    public static final String SAVE_CONTEXT = "save-context";
    /** URL Property to signify a {@link Profile} */
    public static final String PROFILE = "profile";
    /** URL Property to signify a Resource */
    public static final String RESOURCE = "resource";
    /** URL Property to signify a list of ResourceRepositoryItems */
    public static final String RESOURCES = "resources";
    /** URL Property to signify a selector action that should be fired upon page load */
    public static final String SELECTOR_ACTION = "selector-action";
    /** URL Property to signify a {@link Company} */
    public static final String COMPANY = "company";

    /** Path Info for URL Property user */
    public static final String USER_PATH_INFO = '/' + USER + "-{" + USER + '}';
    /** Path Info for URL Property repo-item */
    public static final String REPOSITORY_ITEM_PATH_INFO = '/' + REPOSITORY_ITEM + "-{" + REPOSITORY_ITEM + '}';
    /** Path Info for URL Property resource-type */
    public static final String RESOURCE_TYPE_PATH_INFO = '/' + RESOURCE_TYPE + "-{" + RESOURCE_TYPE + '}';
    /** Path Info for URL Property repository-owner */
    public static final String REPOSITORY_OWNER_PATH_INFO = '/' + REPOSITORY_OWNER + "-{" + REPOSITORY_OWNER + '}';
    /** Path Info for URL Property repository */
    public static final String REPOSITORY_PATH_INFO = '/' + REPOSITORY + "-{" + REPOSITORY + '}';
    /** Path Info for URL Property copy */
    public static final String COPY_PATH_INFO = '/' + COPY + "-{" + COPY + '}';
    /** Path Info for URL Property save-context */
    public static final String SAVE_CONTEXT_PATH_INFO = '/' + SAVE_CONTEXT + "-{" + SAVE_CONTEXT + '}';
    /** Path Info for URL Property profile */
    public static final String PROFILE_PATH_INFO = '/' + PROFILE + "-{" + PROFILE + '}';
    /** Path Info for URL Property resources */
    public static final String RESOURCES_PATH_INFO = '/' + RESOURCES + "-{" + RESOURCES + '}';
    /** Path Info for URL Property resource */
    public static final String RESOURCE_PATH_INFO = '/' + RESOURCE + "-{" + RESOURCE + '}';
    /** Path Info for URL Property selector-action */
    public static final String SELECTOR_ACTION_PATH_INFO = '/' + SELECTOR_ACTION + "-{" + SELECTOR_ACTION + '}';
    /** Path Info for URL Property company */
    public static final String COMPANY_PATH_INFO = '/' + COMPANY + "-{" + COMPANY + '}';
    private URLProperties()
    {
    }

}
