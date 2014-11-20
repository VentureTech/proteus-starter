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

package com.example.app.finalproject.util;


import javax.annotation.Nullable;

import com.i2rd.cms.bean.contentmodel.CmsModelDataSet;

import net.proteusframework.cms.component.content.ContentBuilder;
import net.proteusframework.internet.http.Link;

/**
 * DataModel for Content builder of ContentElement.
 *
 * @author Fajie Han (fhan@venturetechasia.net)
 * @since 14-11-17 ??7:49
 */
public class DetailInfoContentBuilder extends ContentBuilder<DetailInfoProperties>
{
    /**
     * Load the config.
     * @param configurationData the configuration data.
     * @return the configuration.
     */
    public static DetailInfoContentBuilder load(@Nullable CmsModelDataSet configurationData)
    {
        return ContentBuilder.load(configurationData, DetailInfoContentBuilder.class, true);
    }
    /**
     * Constructor
     */
    public DetailInfoContentBuilder(){};

    /**
     * Get the listFacultyPage
     * @return-listFaculty_page
     */
    public Link getListFacultyPage() { return getLinkPropertyValue(DetailInfoProperties.listFaculty_page,null); }

    /**
     * Set the listFacultyPage
     * @param listFacultyPage the listFaculty page
     */
    void setListFacultyPage(Link listFacultyPage){ setLinkPropertyValue(DetailInfoProperties.listFaculty_page,listFacultyPage);}
    /**
     * Get the detailInfoPage
     * @return-detailInfo_page
     */
    public Link getDetailInfoPage() { return getLinkPropertyValue(DetailInfoProperties.detailInfo_page,null); }

    /**
     * Set the detailInfoPage
     * @param detailInfoPage the detailInfo page
     */
    void setDetailInfoPage(Link detailInfoPage){ setLinkPropertyValue(DetailInfoProperties.detailInfo_page,detailInfoPage);}
    /**
     * Get save page.
     *
     * @return-submit page.
     */
    public Link getSavePage()
    {
        return getLinkPropertyValue(DetailInfoProperties.save_page, null);
    }


    /**
     * Set save page.
     *
     * @param save_page the save page.
     */
    void setSavePage(Link save_page)
    {
        setLinkPropertyValue(DetailInfoProperties.save_page, save_page);
    }

    /**
     * Get cancel page.
     *
     * @return-cancel page.
     */
    public Link getCancelPage()
    {
        return getLinkPropertyValue(DetailInfoProperties.cancel_page, null);
    }

    /**
     * set cancel page.
     *
     * @param cancelPage the cancel page.
     */
    void setCancelPage(Link cancelPage)
    {
        setLinkPropertyValue(DetailInfoProperties.cancel_page, cancelPage);
    }

    /**
     * Get the pageConfigFirst
     * @return-error_page
     */
    public Link getErrorPage() { return getLinkPropertyValue(DetailInfoProperties.error_page,null); }

    /**
     * Set the pageConfigFirst
     * @param error_page the error page
     */
    void setError_page(Link error_page){ setLinkPropertyValue(DetailInfoProperties.error_page,error_page);}

}
