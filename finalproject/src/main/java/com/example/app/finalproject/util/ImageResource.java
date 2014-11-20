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

import java.io.IOException;

import net.proteusframework.cms.CmsValidationException;
import net.proteusframework.cms.support.ImageFileUtil;
import net.proteusframework.core.metric.Dimension;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.filesystem.http.FileEntityImageResourceFactory;
import net.proteusframework.data.filesystem.http.FileSystemEntityContext;
import net.proteusframework.internet.http.resource.FactoryBasedResource;
import net.proteusframework.internet.http.resource.Resource;
import net.proteusframework.internet.http.resource.ResourceContext;
import net.proteusframework.internet.http.resource.ResourceManager;

/**
 * Get the image resource
 *
 * @author Fajie Han (fhan@venturetechasia.net)
 * @since 14-11-13 ??1:18
 */
public class ImageResource
{
    /**
     * Get the image resource. Both <tt>minWidth</tt> and <tt>minHeight</tt> must be greater than 0.  This scaled
     * resource will match exactly either the minWidth or the minHeight.  The other dimension will be greater than
     * corresponding minWidth/minHeight.
     * @param imageFile the image file.
     * @param minWidth the minimum allowed width of the resource.
     * @param minHeight the minimum allowed height of the resource.
     * @return the sized image or null if an image of the specified size could not be created.
     * @throws CmsValidationException if the file is not an image or the dimensions specified are invalid.
     * @throws NullPointerException if the argument is null.
     * @throws IOException if an error occurs reading the image file.
     */
    public static FactoryBasedResource getResource(final FileEntity imageFile, int minWidth, int minHeight)
        throws CmsValidationException, NullPointerException, IOException
    {
        if(minHeight <= 0 || minWidth <= 0) throw new CmsValidationException("Invalid dimension: " + minWidth + "x" + minHeight);
        final Dimension dim = ImageFileUtil.getDimension(imageFile);
        if(dim == null || dim.getWidthMetric().longValue() == 0 || dim.getHeightMetric().longValue() == 0)
            return null;

        final ResourceManager rm = ResourceContext.RESOURCE_MANAGER.getBean();
        final FileEntityImageResourceFactory feirp = FileSystemEntityContext.FILE_ENTITY_IMAGE_FACTORY.getBean();

        int ow = dim.getWidthMetric().intValue();
        int oh = dim.getHeightMetric().intValue();

        double oratio = (double)ow / (double)oh;
        double newRatio = (double)minWidth / (double)minHeight; //this is the window we want to show the image in

        int     width;
        int     height;
        double  scale;

        if(oratio >= newRatio) //image is wider than the window, match the height
        {
            height = minHeight;
            scale = (double) oh / (double) minHeight;
            width = (int)(ow / scale);
        }
        else //image is taller than the window, match the width
        {
            width = minWidth;
            scale = (double) ow / (double) minWidth;
            height = (int)(oh / scale);
        }

        width  = ( width < 1 ) ? 1 : width;
        height = ( height < 1 ) ? 1 : height;

        final String persistentId = feirp.getPersistentResourceId(imageFile, width, height);
        final FactoryBasedResource imgResource = _getFactoryResource(rm.getResourceFromFactory(feirp.getFactoryId(), persistentId));
        return imgResource;
    }

    /**
     * Get the factory resource from the specified resource.
     * @param res the resource.
     * @return the factory resource.
     */
    private static FactoryBasedResource _getFactoryResource(Resource res)
    {
        return (FactoryBasedResource) res;
    }
}
