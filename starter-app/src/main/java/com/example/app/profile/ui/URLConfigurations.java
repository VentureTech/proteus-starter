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

package com.example.app.profile.ui;

import net.proteusframework.ui.management.URLConfigDef;

/**
 * URLConfiguration names for {@link URLConfigDef}.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public class URLConfigurations
{
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
        /** URL Config for Editing Company */
        public static final String EDIT = "Company Edit URL Config";
        /** URL Config for Viewing a Company */
        public static final String VIEW = "Company View URL Config";

        /**
         * Defined URL Configurations for Company Resource UIs
         *
         * @author Alan Holt (aholt@venturetech.net)
         */
        @SuppressWarnings("InnerClassFieldHidesOuterClassField")
        public static final class Resource
        {
            /** URL Config for Editing a Resource on a Company */
            public static final String EDIT = "Company Resource Edit URL Config";
            /** URL Config for Viewing a Resource on a Company */
            public static final String VIEW = "Company Resource VIEW URL Config";

            private Resource(){}
        }

        private Company(){}
    }

    /**
     * Defined URL Configurations used by Client UIs
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class Client
    {
        /** URL Config for Editing a Client*/
        public static final String EDIT = "Client Edit URL Config";

        /** URL Config for Viewing a Client */
        public static final String VIEW = "Client View URL Config";

        private Client(){}
    }

    private URLConfigurations()
    {
    }


}
