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

package com.example.app.profile.ui.client;

import com.example.app.profile.model.client.Client;
import com.example.app.support.ui.vtcrop.VTCropPictureEditorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Configuration} defining configuration information for {@link Client} viewers and editors
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/16/17
 */
@Configuration
public class ClientConfig
{
    /**
     * Client logo config vt crop picture editor config.
     *
     * @return the vt crop picture editor config
     */
    @Bean
    public VTCropPictureEditorConfig clientLogoConfig()
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
        config.setImageScales(new VTCropPictureEditorConfig.ImageScaleOption(1.0, 1.0, "client_img"));
        return config;
    }
}
