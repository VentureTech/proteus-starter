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

package com.example.app.repository.ui;

import com.example.app.profile.model.Profile;
import com.example.app.repository.model.Repository;
import com.example.app.repository.model.RepositoryItem;

import net.proteusframework.ui.management.URLProperty;

/**
 * ApplicationFunction properties for {@link URLProperty}.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/6/17
 */
public final class URLProperties
{
    /** URL Property to signify a {@link RepositoryItem} */
    public static final String REPOSITORY_ITEM = "repo-item";
    /** Path Info for URL Property repo-item */
    public static final String REPOSITORY_ITEM_PATH_INFO = '/' + REPOSITORY_ITEM + "-{" + REPOSITORY_ITEM + '}';

    /** URL Property to signify a {@link Profile} owning a {@link Repository} */
    public static final String REPOSITORY_OWNER = "repository-owner";
    /** Path Info for URL Property repository-owner */
    public static final String REPOSITORY_OWNER_PATH_INFO = '/' + REPOSITORY_OWNER + "-{" + REPOSITORY_OWNER + '}';
    /** URL Property to signify a {@link Repository} */
    public static final String REPOSITORY = "repository";
    /** Path Info for URL Property repository */
    public static final String REPOSITORY_PATH_INFO = '/' + REPOSITORY + "-{" + REPOSITORY + '}';

    private URLProperties(){}
}
