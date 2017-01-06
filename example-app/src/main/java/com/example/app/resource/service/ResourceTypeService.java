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

package com.example.app.resource.service;

import com.example.app.resource.model.Resource;
import com.example.app.resource.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.i2rd.implementationmodel.IImplementationModelFactory;
import com.i2rd.implementationmodel.ModelFactoryContext;
import com.i2rd.implementationmodel.impl.SpringComponentImplementationModelFactory;

import net.proteusframework.cms.dao.CmsFrontendDAO;
import net.proteusframework.internet.http.Site;

/**
 * Service to ensure unified api for retrieving {@link ResourceType}s
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 11/10/15 9:56 AM
 */
@SuppressWarnings("unused")
@Service(ResourceTypeService.SERVICE_NAME)
public class ResourceTypeService
{
    /** The service name */
    public static final String SERVICE_NAME = "ldp.ResourceTypeService";
    /** The default resource name */
    public static final String SPRING_FACTORY_IDENTIFIER = "ldp.ResourceImplementationModelFactory";

    /**
     * Context for model queries.
     */
    @Configurable
    public static class Context extends ModelFactoryContext
    {
        private final Site _site;

        /**
         * Instantiates a new Context
         * using the operational site
         */
        public Context()
        {
            this(CmsFrontendDAO.getInstance().getOperationalSite());
        }

        /**
         * Instantiates a new Context
         *
         * @param site the site
         */
        public Context(@Nonnull final Site site)
        {
            _site = site;
            attributes.put(Attributes.SITE, site);
        }

        /**
         * Gets the site for this Context
         *
         * @return the site
         */
        public Site getSite()
        {
            return _site;
        }
    }
    private final IImplementationModelFactory<ResourceType, Resource> _modelFactory;

    /**
     * Create a new instance
     *
     * @param implementationModels the implementation models autowired in for this service
     */
    @Autowired
    public ResourceTypeService(Collection<ResourceType> implementationModels)
    {
        _modelFactory = new SpringComponentImplementationModelFactory<>(
            SPRING_FACTORY_IDENTIFIER, Resource.class, implementationModels);
    }

    /**
     * Create a new {@link Resource} that corresponds to the given {@link ResourceType} identifier
     *
     * @param factoryId the factory identifier
     * @param identifier the identifier for the ResourceType to use for creating the new Resource
     *
     * @return the new Resource.  May be null if no ResourceType with the given identifier exists.
     */
    @Nullable
    public Resource createResource(String factoryId, String identifier)
    {
        ResourceType resourceType = getResourceType(factoryId, identifier);
        return resourceType != null ? _modelFactory.createInstance(resourceType) : null;
    }

    /**
     * Get the {@link ResourceType} that corresponds to the given identifier
     *
     * @param factoryId the factory identifier
     * @param identifier the identifier for the ResourceType to retrieve
     *
     * @return the ResourceType that corresponds to the given identifier, may be null if no ResourceType with the given
     * identifier exists
     */
    @Nullable
    public ResourceType getResourceType(String factoryId, String identifier)
    {
        return factoryId.equals(SPRING_FACTORY_IDENTIFIER) ? _modelFactory.getModel(identifier) : null;
    }

    /**
     * Get the factory identifier for the specified ResourceType
     *
     * @param context the context
     * @param resourceType the resource type
     *
     * @return the identifier
     */
    public String getFactoryIdentifier(Context context, ResourceType resourceType)
    {
        return _modelFactory.getIdentifier();
    }

    /**
     * Get available ResourceTypes
     *
     * @return a list of available ResourceTypes
     */
    public List<ResourceType> getResourceTypes()
    {
        return new ArrayList<>(_modelFactory.getModels(new ModelFactoryContext()));
    }
}
