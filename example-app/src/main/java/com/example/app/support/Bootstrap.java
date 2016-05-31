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

package com.example.app.support;

/**
 * Utility class containing methods and fields for getting classnames for components based on Bootstrap
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 6/3/15 12:07 PM
 */
@SuppressWarnings("ConstantNamingConvention")
public class Bootstrap
{
    /**
     * The type Grid.
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class Grid
    {
        /** COLUMN */
        private static final String COLUMN = "col";

        /** Fluid container. */
        public static final String fluidContainer = "container-fluid";

        /** Container string. */
        public static final String container = "container";


        /** Row string. */
        public static final String row = "row";

        /**
         * Column string.
         *
         * @param size the size
         * @param width the width
         * @return the string
         */
        public static String column(Sizing size, int width)
        {
            return COLUMN + '-' + size.toString() + '-' + width;
        }
    }

    /**
     * The type Nav.
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class Nav
    {
        /** Nav string. */
        public static final String base = "nav";

        /** Nav stacked. */
        public static final String navStacked = "nav-stacked";

        /** Nav pills. */
        public static final String navPills = "nav-pills";

        /** Nav tabs. */
        public static final String navTabs = "nav-tabs";
    }

    /**
     * The type Label.
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class Label
    {
        /** Label string. */
        public static final String base = "label";

        /** Label default. */
        public static final String labelDefault = "label-default";

        /** Label primary. */
        public static final String labelPrimary = "label-primary";

        /** Label success. */
        public static final String labelSuccess = "label-success";

        /** Label info. */
        public static final String labelInfo = "label-info";

        /** Label warning. */
        public static final String labelWarning = "label-warning";

        /** Label danger. */
        public static final String labelDanger = "label-danger";
    }

    /**
     * The type Panel.
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class Panel
    {
        /** Base string. */
        public static final String base = "panel";

        /** Panel default. */
        public static final String panelDefault = "panel-default";

        /** Panel heading. */
        public static final String panelHeading = "panel-heading";

        /** Panel title. */
        public static final String panelTitle = "panel-title";

        /** Panel body. */
        public static final String panelBody = "panel-body";

        /** Panel footer. */
        public static final String panelFooter = "panel-footer";

        /** Panel primary. */
        public static final String panelPrimary = "panel-primary";

        /** Panel success. */
        public static final String panelSuccess = "panel-success";

        /** Panel info. */
        public static final String panelInfo = "panel-info";

        /** Panel warning. */
        public static final String panelWarning = "panel-warning";

        /** Panel danger. */
        public static final String panelDanger = "panel-danger";
    }

    /**
     * The type Css.
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class Css
    {
        /** Pull left. */
        public static final String pullLeft = "pull-left";

        /** Pull right. */
        public static final String pullRight = "pull-right";

        /** Clearfix string. */
        public static final String clearfix = "clearfix";

        /** Show string. */
        public static final String show = "show";

        /** Hidden string. */
        public static final String hidden = "hidden";

        /** Invisible string. */
        public static final String invisible = "invisible";

        /** Sr only. */
        public static final String srOnly = "sr-only";

        /** Icon bar. */
        public static final String iconBar = "icon-bar";

        /** Collapse string. */
        public static final String collapse = "collapse";
    }

    /**
     * The type Glyphicon.
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class Glyphicon
    {
        /** Base string. */
        public static final String base = "glyphicon";

        /** Ok string. */
        public static final String ok = "glyphicon-ok";

        /** Edit string. */
        public static final String edit = "glyphicon-edit";

        /** Chevron right. */
        public static final String chevronRight = "glyphicon-chevron-right";

        /** Chevron down. */
        public static final String chevronDown = "glyphicon-chevron-down";

        /** The constant chevronUp. */
        public static final String chevronUp = "glyphicon-chevron-up";

        /** The constant chevronLeft. */
        public static final String chevronLeft = "glyphicon-chevron-left";

        /** Align justify. */
        public static final String alignJustify = "glyphicon-align-justify";

        /** Menu right. */
        public static final String menuRight = "glyphicon-menu-right";

        /** Menu down. */
        public static final String menuDown = "glyphicon-menu-down";
    }

    /**
     * The type Background.
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class Background
    {
        /** Primary string. */
        public static final String primary = "bg-primary";

        /** Success string. */
        public static final String success = "bg-success";

        /** Info string. */
        public static final String info = "bg-info";

        /** Warning string. */
        public static final String warning = "bg-warning";

        /** Danger string. */
        public static final String danger = "bg-danger";
    }

    /**
     * The type Responsive embed.
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class ResponsiveEmbed
    {
        /** Base string. */
        public static final String base = "embed-responsive";

        /**
         * Aspect ratio.
         *
         * @param width the width
         * @param height the height
         * @return the string
         */
        public static String aspectRatio(int width, int height)
        {
            return "embed-responsive-" + width + "by" + height;
        }

        /** Item string. */
        public static final String item = "embed-responsive-item";
    }

    /**
     * The type Nav bar.
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class Navbar
    {
        /** Base string. */
        public static final String base = "navbar";

        /** Navbar default. */
        public static final String navbarDefault = "navbar-default";

        /** Navbar collapse. */
        public static final String navbarCollapse = "navbar-collapse";

        /** Navbar toggle. */
        public static final String navbarToggle = "navbar-toggle";

        /** Navbar header. */
        public static final String navbarHeader = "navbar-header";

        /**
         * Navbar toggle data target.
         *
         * @param classname the classname
         * @return the string
         */
        public static String navbarToggleDataTarget(String classname)
        {
            return '.' + classname;
        }

        /**
         * Navbar toggle data target.
         *
         * @return the string
         */
        public static String navbarToggleDataTarget()
        {
            return '.' + navbarCollapse;
        }
    }

    /**
     * The type Alignment.
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class Alignment
    {
        /** The constant textLeft. */
        public static final String textLeft = "text-left";

        /** The constant textRight. */
        public static final String textRight = "text-right";

        /** The constant textCenter. */
        public static final String textCenter = "text-center";

        /** The constant textJustify. */
        public static final String textJustify = "text-justify";

        /** The constant textNowrap. */
        public static final String textNowrap = "text-nowrap";
    }

    /**
     * The type Attributes.
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class Attributes
    {
        /** Data toggle. */
        public static final String dataToggle = "data-toggle";

        /** Data target. */
        public static final String dataTarget = "data-target";
    }

    /**
     * The enum Sizing.
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static enum Sizing
    {
        /** xs */
        xs("xs"),
        /** sm */
        sm("sm"),
        /** md */
        md("md"),
        /** lg */
        lg("lg")
        ;

        /**
         * Instantiates a new Sizing.
         *
         * @param classNameFragment the class name fragment
         */
        Sizing(String classNameFragment)
        {
            _classNameFragment = classNameFragment;
        }

        /** class name fragment */
        private final String _classNameFragment;

        /**
         * Gets class name fragment.
         *
         * @return the class name fragment
         */
        public String getClassNameFragment()
        {
            return _classNameFragment;
        }


        @Override
        public String toString()
        {
            return _classNameFragment;
        }
    }
}
