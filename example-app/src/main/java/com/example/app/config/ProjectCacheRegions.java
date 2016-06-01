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

import net.sf.ehcache.CacheManager;

import org.springframework.stereotype.Component;

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


    /** Max TTI for data cache regions */
    private static final int MAX_DATA_TTI = 14_400;
    /** Max TTI for query cache regions */
    private static final int MAX_QUERY_TTI = 14_400;
    /** Max entries for data cache regions */
    private static final int MAX_DATA_ENTRIES = 1000;
    /** Max entries for query cache regions */
    private static final int MAX_QUERY_ENTRIES = 300;

    @Override
    public void applyConfiguration(CacheManager manager)
    {
        setupRegion(manager, PROFILE_DATA, MAX_DATA_TTI, MAX_DATA_ENTRIES);
        setupRegion(manager, MEMBER_DATA, MAX_DATA_TTI, MAX_DATA_ENTRIES);
        setupRegion(manager, ENTITY_DATA, MAX_DATA_TTI, MAX_DATA_ENTRIES);

        setupRegion(manager, PROFILE_QUERY, MAX_QUERY_TTI, 500);
        setupRegion(manager, MEMBER_QUERY, MAX_QUERY_TTI, 500);
        setupRegion(manager, ENTITY_QUERY, MAX_QUERY_TTI, MAX_QUERY_ENTRIES);
    }
}
