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

package com.example.app.support.ui.vtcrop;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;
import java.util.Objects;

import net.proteusframework.core.StringFactory;

/**
 * The configuration for a {@link VTCropPictureEditor}.
 *
 * <pre>//Example crop_opts
 * var crop_opts = {
 * //The parent element that contains the image and all associated elements for cropping
 * parent_target: $(document).get(0),
 * //The sub-selector used to find the img element within the parent_target
 * image_selector: undefined,
 * //Boolean flag:  if true, a default value will not be assigned to image_target if crop_opts.image_target is undefined
 * accept_undefined_image_target: false,
 * //The img element.  If not assigned, it will be found.  Also see accept_undefined_image_target documentation
 * image_target: undefined,
 * //The drop zone target element
 * drop_zone_target: undefined,
 * //Boolean flag:  if true, constrain the image during resizing to the images original aspect ratio
 * constrain_aspectratio: undefined,
 * //Image minimum width
 * min_width: 60,
 * //Image minimum height
 * min_height: 60,
 * //Image and Parent maximum width
 * max_width: 500,
 * //Image and Parent maximum height
 * max_height: 500,
 * //The crop width.  This will be the width of the cropped image.
 * crop_width: 200,
 * //The crop height.  This will be the height of the cropped image.
 * crop_height: 200,
 * //The image background string.  This will be the background of the image.
 * Keep in mind that if you use the default (transparent), the image_type will be set to the default no matter what.
 * image_background_str: undefined,
 * //The image type of the image element
 * image_type: undefined,
 * //The encoder options used by the crop function
 * encoder_options: undefined,
 * //Additional image scale sizes. Will scale the cropped image from the original to a size based on scale size.
 * //If I crop to 100 x 100 and I specify an additional scale size of 2.0,
 * //it will include an additional image in the crop method callback that is 200 x 200.
 * //Example: [{ scale: 2.0, quality: 1.0 }, { scale: 5.0, quality: 0.25 }]
 * additional_image_scales: []
 * };</pre>
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 9/20/15 3:52 PM
 */
@SuppressWarnings("unused")
public class VTCropPictureEditorConfig
{
    /**
     * Image Scale Option json mapping
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    @SuppressWarnings({"ParameterHidesMemberVariable", "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class ImageScaleOption
    {
        /** json mapping */
        public Double scale;
        /** json mapping */
        public Double quality;
        /** json mapping */
        public String fileName;
        /**
         * Instantiate a new instance
         *
         * @param scale the scale of the image
         * @param quality the quality of the scaled image
         * @param fileName the file name of the scaled image
         */
        public ImageScaleOption(Double scale, Double quality, String fileName)
        {
            this.scale = scale;
            this.quality = quality;
            this.fileName = fileName;
        }
    }

    /** image selector */
    private String _imageSelector;

    /** accept undefined image target */
    private Boolean _acceptUndefinedImageTarget = Boolean.TRUE;

    /** constrain aspect ratio */
    private Boolean _constrainAspectRatio;

    /** min width */
    private Integer _minWidth;

    /** min height */
    private Integer _minHeight;

    /** max width */
    private Integer _maxWidth;

    /** max height */
    private Integer _maxHeight;

    /** crop width */
    private Integer _cropWidth;

    /** crop height */
    private Integer _cropHeight;

    /** image background string */
    private String _imageBackgroundStr;

    /** image type */
    private String _imageType;

    /** encoder options */
    private Double _encoderOptions;

    /** image scales */
    private ImageScaleOption[] _imageScales;

    /**
     * Get an ImageScaleOption from this config's ImageScales based on filename
     *
     * @param fileName the fileName to search for
     *
     * @return a matching ImageScaleOption for the given filename, or null if one could not be found or the given file name is
     * null or empty.
     */
    @Nullable
    public ImageScaleOption getImageScaleOptionForName(@Nullable String fileName)
    {
        if (StringFactory.isEmptyString(fileName)) return null;
        ImageScaleOption[] imageScales = getImageScales();
        for (ImageScaleOption option : imageScales)
        {
            if (Objects.equals(option.fileName, fileName))
            {
                return option;
            }
        }
        return null;
    }

    /**
     * Gets the additional image scales
     *
     * @return the additional image scales
     */
    public ImageScaleOption[] getImageScales()
    {
        return _imageScales;
    }

    /**
     * Sets the additional image scales
     *
     * @param imageScales the additional image scales
     */
    public void setImageScales(ImageScaleOption... imageScales)
    {
        _imageScales = imageScales;
    }

    /**
     * To json.
     *
     * @return the string
     */
    public String toJson()
    {
        Gson gson = new Gson();

        JsonObject json = new JsonObject();
        if (getImageSelector() != null)
            json.add("image_selector", new JsonPrimitive(getImageSelector()));
        if (getAcceptUndefinedImageTarget() != null)
            json.add("accept_undefined_image_target", new JsonPrimitive(getAcceptUndefinedImageTarget()));
        if (getConstrainAspectRatio() != null)
            json.add("constrain_aspectratio", new JsonPrimitive(getConstrainAspectRatio()));
        if (getMinWidth() != null)
            json.add("min_width", new JsonPrimitive(getMinWidth()));
        if (getMinHeight() != null)
            json.add("min_height", new JsonPrimitive(getMinHeight()));
        if (getMaxWidth() != null)
            json.add("max_width", new JsonPrimitive(getMaxWidth()));
        if (getMaxHeight() != null)
            json.add("max_height", new JsonPrimitive(getMaxHeight()));
        if (getCropWidth() != null)
            json.add("crop_width", new JsonPrimitive(getCropWidth()));
        if (getCropHeight() != null)
            json.add("crop_height", new JsonPrimitive(getCropHeight()));
        if (getImageBackgroundStr() != null)
            json.add("image_background_str", new JsonPrimitive(getImageBackgroundStr()));
        if (getImageType() != null)
            json.add("image_type", new JsonPrimitive(getImageType()));
        if (getEncoderOptions() != null)
            json.add("encoder_options", new JsonPrimitive(getEncoderOptions()));
        if (getImageScales() != null)
        {
            JsonArray imageScales = new JsonArray();
            for (ImageScaleOption imageScale : getImageScales())
            {
                imageScales.add(gson.toJsonTree(imageScale));
            }
            json.add("image_scales", imageScales);
        }
        return gson.toJson(json);
    }

    /**
     * Gets image selector.
     *
     * @return the image selector
     */
    public String getImageSelector()
    {
        return _imageSelector;
    }

    /**
     * Sets image selector.
     *
     * @param imageSelector the image selector
     */
    public void setImageSelector(String imageSelector)
    {
        _imageSelector = imageSelector;
    }

    /**
     * Gets accept undefined image target.
     *
     * @return the accept undefined image target
     */
    public Boolean getAcceptUndefinedImageTarget()
    {
        return _acceptUndefinedImageTarget;
    }

    /**
     * Sets accept undefined image target.
     *
     * @param acceptUndefinedImageTarget the accept undefined image target
     */
    public void setAcceptUndefinedImageTarget(Boolean acceptUndefinedImageTarget)
    {
        _acceptUndefinedImageTarget = acceptUndefinedImageTarget;
    }

    /**
     * Gets constrain aspect ratio.
     *
     * @return the constrain aspect ratio
     */
    public Boolean getConstrainAspectRatio()
    {
        return _constrainAspectRatio;
    }

    /**
     * Sets constrain aspect ratio.
     *
     * @param constrainAspectRatio the constrain aspect ratio
     */
    public void setConstrainAspectRatio(Boolean constrainAspectRatio)
    {
        _constrainAspectRatio = constrainAspectRatio;
    }

    /**
     * Gets min width.
     *
     * @return the min width
     */
    public Integer getMinWidth()
    {
        return _minWidth;
    }

    /**
     * Sets min width.
     *
     * @param minWidth the min width
     */
    public void setMinWidth(Integer minWidth)
    {
        _minWidth = minWidth;
    }

    /**
     * Gets min height.
     *
     * @return the min height
     */
    public Integer getMinHeight()
    {
        return _minHeight;
    }

    /**
     * Sets min height.
     *
     * @param minHeight the min height
     */
    public void setMinHeight(Integer minHeight)
    {
        _minHeight = minHeight;
    }

    /**
     * Gets max width.
     *
     * @return the max width
     */
    public Integer getMaxWidth()
    {
        return _maxWidth;
    }

    /**
     * Sets max width.
     *
     * @param maxWidth the max width
     */
    public void setMaxWidth(Integer maxWidth)
    {
        _maxWidth = maxWidth;
    }

    /**
     * Gets max height.
     *
     * @return the max height
     */
    public Integer getMaxHeight()
    {
        return _maxHeight;
    }

    /**
     * Sets max height.
     *
     * @param maxHeight the max height
     */
    public void setMaxHeight(Integer maxHeight)
    {
        _maxHeight = maxHeight;
    }

    /**
     * Gets crop width.
     *
     * @return the crop width
     */
    public Integer getCropWidth()
    {
        return _cropWidth;
    }

    /**
     * Sets crop width.
     *
     * @param cropWidth the crop width
     */
    public void setCropWidth(Integer cropWidth)
    {
        _cropWidth = cropWidth;
    }

    /**
     * Gets crop height.
     *
     * @return the crop height
     */
    public Integer getCropHeight()
    {
        return _cropHeight;
    }

    /**
     * Sets crop height.
     *
     * @param cropHeight the crop height
     */
    public void setCropHeight(Integer cropHeight)
    {
        _cropHeight = cropHeight;
    }

    /**
     * Gets the image background string
     *
     * @return the image background string
     */
    public String getImageBackgroundStr()
    {
        return _imageBackgroundStr;
    }

    /**
     * Sets the image background string
     *
     * @param imageBackgroundStr the image background string
     */
    public void setImageBackgroundStr(String imageBackgroundStr)
    {
        _imageBackgroundStr = imageBackgroundStr;
    }

    /**
     * Gets image type.
     *
     * @return the image type
     */
    public String getImageType()
    {
        return _imageType;
    }

    /**
     * Sets image type.
     *
     * @param imageType the image type
     */
    public void setImageType(String imageType)
    {
        _imageType = imageType;
    }

    /**
     * Gets encoder options.
     *
     * @return the encoder options
     */
    public Double getEncoderOptions()
    {
        return _encoderOptions;
    }

    /**
     * Sets encoder options.
     *
     * @param encoderOptions the encoder options
     */
    public void setEncoderOptions(Double encoderOptions)
    {
        _encoderOptions = encoderOptions;
    }
}
