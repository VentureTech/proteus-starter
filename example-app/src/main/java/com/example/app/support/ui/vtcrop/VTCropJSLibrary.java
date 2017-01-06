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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.internet.http.resource.ClassPathResourceLibrary;
import net.proteusframework.internet.http.resource.ClassPathResourceLibraryHelper;
import net.proteusframework.internet.http.resource.FactoryResource;
import net.proteusframework.internet.http.resource.html.NDE;

/**
 * JavaScript Files used by VTCrop
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
public enum VTCropJSLibrary implements ClassPathResourceLibrary
{
    /** VTCrop */
    VTCrop("vtcrop/VTCrop.js", VTCropCSSLibrary.VTCrop),
    /** DndFile */
    DndFile("vtcrop/dnd-file.js"),
    /** VTCropPictureEditor */
    VTCropPictureEditor("vtcrop/vt-img-cropper.js", DndFile, VTCrop);

    @Component
    static class VTCropJSLibraryInjector
    {
        @Autowired
        private ClassPathResourceLibraryHelper _classPathResourceLibraryHelper;

        @PostConstruct
        public void postConstruct()
        {
            for (VTCropJSLibrary js : EnumSet.allOf(VTCropJSLibrary.class))
            {
                js.setClassPathResourceLibraryHelper(_classPathResourceLibraryHelper);
            }
        }
    }

    /** The class path. */
    private final String _classPath;
    /** The dependencies. */
    private final List<ClassPathResourceLibrary> _dependencies;

    @SuppressWarnings("NonFinalFieldInEnum")
    private ClassPathResourceLibraryHelper _classPathResourceLibraryHelper;

    /**
     * Create a new instance with no dependencies
     *
     * @param classPath the resource classpath
     */
    VTCropJSLibrary(String classPath)
    {
        _classPath = classPath;
        _dependencies = Collections.emptyList();
    }

    /**
     * Create a new instance
     *
     * @param classPath the resource classpath
     * @param dependencies list of dependencies
     */
    VTCropJSLibrary(String classPath, ClassPathResourceLibrary... dependencies)
    {
        _classPath = classPath;
        _dependencies = Arrays.asList(dependencies);
    }

    @Override
    public FactoryResource getResource()
    {
        return getClassPathResourceLibraryHelper().createResource(this);
    }

    @Nullable
    @Override
    public NDE getNDE()
    {
        return getClassPathResourceLibraryHelper().createNDE(this, true);
    }

    @Override
    public String getContentType()
    {
        return "text/javascript";
    }

    @Override
    public String getClassPath()
    {
        return _classPath;
    }

    @Override
    public List<? extends ClassPathResourceLibrary> getDependancies()
    {
        return _dependencies;
    }

    @Nullable
    @Override
    public LocalizedObjectKey getDescription()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return StringFactory.getBasename(_classPath);
    }

    private ClassPathResourceLibraryHelper getClassPathResourceLibraryHelper()
    {
        return _classPathResourceLibraryHelper;
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void setClassPathResourceLibraryHelper(
        ClassPathResourceLibraryHelper classPathResourceLibraryHelper)
    {
        _classPathResourceLibraryHelper = classPathResourceLibraryHelper;
    }
}
