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

import net.proteusframework.ui.management.URLConfigDef;

/**
 * URLConfiguration names for {@link URLConfigDef}.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public class URLConfigurations
{
    /**
     * Defined URL Configurations used by ResourceRepositoryItem UIs
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class ResourceRepositoryItem
    {
        /** URL Config for Editing a ResourceRepositoryItem */
        public static final String EDIT = "Resource Repository Item Edit URL Config";
        /** URL Config for Viewing a ResourceRepositoryItem */
        public static final String VIEW = "Resource Repository Item View URL Config";
        private ResourceRepositoryItem()
        {
        }
    }

    /**
     * Defined URL Configurations used by User UIs
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class User
    {
        /** URL Config for Editing a User */
        public static final String EDIT = "User Edit URL Config";
        /** URL Config for Viewing a User */
        public static final String VIEW = "USer View URL Config";
        private User()
        {
        }
    }

    /**
     * Defined URL Configurations used by Company UIs
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class Company
    {
        private Company(){}

        /** URL Config for Editing Company */
        public static final String EDIT = "Company Edit URL Config";
        /** URL Config for Viewing a Company */
        public static final String VIEW = "Company View URL Config";
    }

    private URLConfigurations()
    {
    }


}
