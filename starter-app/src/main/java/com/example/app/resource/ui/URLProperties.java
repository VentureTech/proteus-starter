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

package com.example.app.resource.ui;

import com.example.app.resource.model.ResourceType;

import net.proteusframework.ui.management.URLProperty;

/**
 * ApplicationFunction properties for {@link URLProperty}.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/6/17
 */
public final class URLProperties
{
    /** URL Property to signify a {@link ResourceType} */
    public static final String RESOURCE_TYPE = "resource-type";
    /** Path Info for URL Property resource-type */
    public static final String RESOURCE_TYPE_PATH_INFO = '/' + RESOURCE_TYPE + "-{" + RESOURCE_TYPE + '}';

    /** URL Property to signify a Resource */
    public static final String RESOURCE = "resource";
    /** Path Info for URL Property resource */
    public static final String RESOURCE_PATH_INFO = '/' + RESOURCE + "-{" + RESOURCE + '}';

    /** URL Property to signify a list of ResourceRepositoryItems */
    public static final String RESOURCES = "resources";
    /** Path Info for URL Property resources */
    public static final String RESOURCES_PATH_INFO = '/' + RESOURCES + "-{" + RESOURCES + '}';

    private URLProperties(){}
}
