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

import com.example.app.model.user.User;
import com.example.app.ui.vtcrop.VTCropPictureEditorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Configuration} defining configuration information for {@link User} viewers and editors
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/7/15 10:58 AM
 */
@Configuration
public class UserConfig
{
    /** Picture editor config bean name */
    public static final String PICTURE_EDITOR_CONFIG = "lr-user-picture-editor-config";

    /**
     *   Get the {@link VTCropPictureEditorConfig} for the User viewer/editor
     *   @return a VTCropPictureEditorConfig
     */
    @Bean(name = PICTURE_EDITOR_CONFIG)
    public VTCropPictureEditorConfig getPictureEditorConfig()
    {
        VTCropPictureEditorConfig config = new VTCropPictureEditorConfig();
        config.setMaxHeight(500);
        config.setMaxWidth(500);
        config.setMinHeight(60);
        config.setMinWidth(60);
        config.setCropHeight(200);
        config.setCropWidth(200);
        config.setImageBackgroundStr("rgba(255,255,255, 1.0)");
        config.setImageType("image/jpeg");
        config.setImageScales(new VTCropPictureEditorConfig.ImageScaleOption(2.0, 1.0, "uer_img"));
        return config;
    }
}
