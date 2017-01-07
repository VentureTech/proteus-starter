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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.cms.dao.CmsFrontendDAO;
import net.proteusframework.cms.label.Label;
import net.proteusframework.cms.label.LabelDomainProvider;
import net.proteusframework.core.locale.TransientLocalizedObjectKey;

/**
 * Service for obtaining and modifying the {@link Label}s for the {@link Resource} api.
 *
 * This service has to do with the Resource Category label domain
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@Service
@Lazy
public class ResourceCategoryLabelProvider extends LabelDomainProvider
{
    @Autowired
    private CmsFrontendDAO _cmsFrontendDAO;

    @Value("${resource_types_label_domain:resource-category}")
    private String _progId;
    @Value("${default_site_assignment}")
    private Long _siteAssignmentId;

    /**
     * Instantiates a new Resource category label provider.
     */
    public ResourceCategoryLabelProvider()
    {
        super();
    }

    @Nonnull
    @Override
    protected String getProgramaticIdentifier()
    {
        return _progId;
    }

    @Nonnull
    @Override
    protected TransientLocalizedObjectKey getDefaultName()
    {
        TransientLocalizedObjectKey defaultName = new TransientLocalizedObjectKey(new HashMap<>());
        defaultName.addLocalization(Locale.ENGLISH, "Category");
        return defaultName;
    }

    @Nonnull
    @Override
    protected Set<CmsSite> getDefaultAssignments()
    {
        return Collections.singleton(_cmsFrontendDAO.getSite(_siteAssignmentId));
    }
}
