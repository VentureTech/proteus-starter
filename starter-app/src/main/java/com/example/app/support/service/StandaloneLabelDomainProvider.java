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

package com.example.app.support.service;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.cms.dao.CmsFrontendDAO;
import net.proteusframework.cms.label.LabelDomain;
import net.proteusframework.cms.label.LabelDomainProvider;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.TransientLocalizedObjectKey;

/**
 * An instantiatable {@link LabelDomainProvider} that takes an already persisted {@link LabelDomain}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 7/13/16 3:07 PM
 */
@Configurable
public class StandaloneLabelDomainProvider extends LabelDomainProvider
{
    @Autowired
    private EntityRetriever _er;
    @Value("${default_site_assignment}")
    private Long _siteAssignmentId;
    @Autowired
    private CmsFrontendDAO _cmsFrontendDAO;

    private final LabelDomain _labelDomain;

    /**
     * Instantiates a new Standalone label domain provider.
     *
     * @param labelDomain the label domain
     */
    public StandaloneLabelDomainProvider(@Nonnull LabelDomain labelDomain)
    {
        super();
        Preconditions.checkNotNull(labelDomain.getProgrammaticIdentifier());
        Preconditions.checkArgument(labelDomain.getId() != null && labelDomain.getId() > 0);
        _labelDomain = labelDomain;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        //Do Nothing
    }

    @Nonnull
    @Override
    public LabelDomain getLabelDomain()
    {
        return _er.reattachIfNecessary(_labelDomain);
    }

    @SuppressWarnings("ConstantConditions")
    @Nonnull
    @Override
    protected String getProgramaticIdentifier()
    {
        return getLabelDomain().getProgrammaticIdentifier();
    }

    @Nonnull
    @Override
    protected TransientLocalizedObjectKey getDefaultName()
    {
        return new TransientLocalizedObjectKey(new HashMap<>());
    }

    @Nonnull
    @Override
    protected Set<CmsSite> getDefaultAssignments()
    {
        return Collections.singleton(_cmsFrontendDAO.getSite(_siteAssignmentId));
    }
}
