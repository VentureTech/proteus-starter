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
 * CSS Files used by VTCrop
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 10/30/15 11:05 AM
 */
public enum VTCropCSSLibrary implements ClassPathResourceLibrary
{
    /** VTCrop */
    VTCrop("vtcrop/VTCrop.css");

    @Component
    static class VTCropCSSLibraryInjector
    {
        @Autowired
        private ClassPathResourceLibraryHelper _classPathResourceLibraryHelper;

        @PostConstruct
        public void postConstruct()
        {
            for (VTCropCSSLibrary css : EnumSet.allOf(VTCropCSSLibrary.class))
            {
                css.setClassPathResourceLibraryHelper(_classPathResourceLibraryHelper);
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
    VTCropCSSLibrary(String classPath)
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
    VTCropCSSLibrary(String classPath, ClassPathResourceLibrary... dependencies)
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
        return "text/css";
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
