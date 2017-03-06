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

package com.example.app.config;


import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import net.proteusframework.core.cache.AbstractCacheRegionsConfiguration;

/**
 * Cache regions for app entities and queries
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@Component
public class ProjectCacheRegions extends AbstractCacheRegionsConfiguration
{
    /** The prefix string to use for data cache regions */
    private static final String DATA_PREFIX = "app";
    /** The prefix string to use for query cache regions */
    private static final String QUERY_PREFIX = "query." + DATA_PREFIX;

    /** The suffix to use for profile cache regions */
    private static final String PROFILE_SUFFIX = "profile";
    /** Profile data cache region */
    public static final String PROFILE_DATA = DATA_PREFIX + '.' + PROFILE_SUFFIX;
    /** Profile query cache region */
    public static final String PROFILE_QUERY = QUERY_PREFIX + '.' + PROFILE_SUFFIX;
    /** The suffix to use for member cache regions */
    private static final String MEMBER_SUFFIX = "member";
    /** Member data cache region */
    public static final String MEMBER_DATA = DATA_PREFIX + '.' + MEMBER_SUFFIX;
    /** Member query cache region */
    public static final String MEMBER_QUERY = QUERY_PREFIX + '.' + MEMBER_SUFFIX;
    /** The suffix to use for entity cache regions */
    private static final String ENTITY_SUFFIX = "entity";
    /** Entity data cache region */
    public static final String ENTITY_DATA = DATA_PREFIX + '.' + ENTITY_SUFFIX;
    /** Entity query cache region */
    public static final String ENTITY_QUERY = QUERY_PREFIX + '.' + ENTITY_SUFFIX;

    @Override
    public void applyConfiguration(Properties props)
    {
        setupRegion(props, PROFILE_DATA, 4, TimeUnit.HOURS);
        setupRegion(props, MEMBER_DATA, 4, TimeUnit.HOURS);
        setupRegion(props, ENTITY_DATA, 4, TimeUnit.HOURS);

        setupRegion(props, PROFILE_QUERY, 4, TimeUnit.HOURS);
        setupRegion(props, MEMBER_QUERY, 4, TimeUnit.HOURS);
        setupRegion(props, ENTITY_QUERY, 4, TimeUnit.HOURS);
    }
}
