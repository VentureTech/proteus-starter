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

package com.example.app.profile.ui.company;

import com.example.app.profile.model.company.Company;
import com.example.app.support.ui.vtcrop.VTCropPictureEditorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Configuration} defining configuration information for {@link Company} viewers and editors
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 6/27/16 1:58 PM
 */
@Configuration
public class CompanyConfig
{
    /**
     *   Get the {@link VTCropPictureEditorConfig}.
     *   @return a VTCropPictureEditorConfig
     */
    @Bean
    public VTCropPictureEditorConfig companyWebLogoConfig()
    {
        VTCropPictureEditorConfig config = new VTCropPictureEditorConfig();
        config.setMaxHeight(250);
        config.setMaxWidth(500);
        config.setMinHeight(30);
        config.setMinWidth(60);
        config.setCropHeight(100);
        config.setCropWidth(200);
        config.setImageBackgroundStr("rgba(255,255,255, 1.0)");
        config.setImageType("image/jpeg");
        config.setImageScales(new VTCropPictureEditorConfig.ImageScaleOption(1.0, 1.0, "coaching_img"));
        return config;
    }

    /**
     *   Get the {@link VTCropPictureEditorConfig}.
     *   @return a VTCropPictureEditorConfig
     */
    @Bean
    public VTCropPictureEditorConfig companyEmailLogoConfig()
    {
        VTCropPictureEditorConfig config = new VTCropPictureEditorConfig();
        config.setMaxWidth(1200);
        config.setMaxHeight(220);
        config.setMinHeight(30);
        config.setMinWidth(200);
        config.setCropWidth(600);
        config.setCropHeight(110);
        config.setImageBackgroundStr("rgba(255,255,255, 1.0)");
        config.setImageType("image/jpeg");
        config.setImageScales(new VTCropPictureEditorConfig.ImageScaleOption(1.0, 1.0, "coaching_email_img"));
        return config;
    }
}
