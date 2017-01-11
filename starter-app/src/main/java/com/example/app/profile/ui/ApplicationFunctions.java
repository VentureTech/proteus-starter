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


import net.proteusframework.ui.management.ApplicationRegistry;

/**
 * Application functions for {@link ApplicationRegistry}.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public final class ApplicationFunctions
{

    /**
     * Static class defining Application Functions for {@link ResourceRepositoryItem}
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static final class ResourceRepositoryItem
    {
        /** Resource Repository Item Public Listing */
        public static final String PUBLIC_LISTING = "Resource Repository Item Public Listing";

        private ResourceRepositoryItem()
        {
        }
    }

    /**
     * Static class defining Application Functions for {@link User}
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static final class User
    {
        /** User Management Application Function */
        public static final String MANAGEMENT = "User Management";
        /** User Editor Application Function */
        public static final String EDIT = "User Editor";
        /** User Viewer Application Function */
        public static final String VIEW = "User Viewer";
        /** My Account Viewer Application Function */
        public static final String MY_ACCOUNT_VIEW = "My Account View";
        /** My Account Editor Application Function */
        public static final String MY_ACCOUNT_EDIT = "My Account Edit";

        private User()
        {
        }
    }

    /**
     * Static class defining Application Functions for {@link com.example.app.profile.model.company.Company}
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static final class Company
    {
        /** The constant MANAGEMENT. */
        public static final String MANAGEMENT = "Company Management";
        /** The constant EDIT. */
        public static final String EDIT = "Company Editor";
        /** The constant VIEW. */
        public static final String VIEW = "Company Viewer";

        /**
         * Static class defining Application Functions for {@link com.example.app.profile.model.resource.Resource}s on a
         * Company
         *
         * @author Alan Holt (aholt@venturetech.net)
         */
        @SuppressWarnings("InnerClassFieldHidesOuterClassField")
        public static final class Resource
        {
            /** Company Resource Management Application Function */
            public static final String MANAGEMENT = "Company Resource Management";
            /** Company Resource Editor Application Function */
            public static final String EDIT = "Company Resource Editor";
            /** Company Resource Viewer Application Function */
            public static final String VIEW = "Company Resource Viewer";

            private Resource(){}
        }

        private Company(){}
    }

    /**
     * Static class defining Application Functions for the Starter Site.
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static final class StarterSite
    {
        /** Starter Site Setup Application Function */
        public static final String SETUP = "Starter Site Setup";
    }

    private ApplicationFunctions()
    {
    }


}
