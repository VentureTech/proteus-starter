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

package com.example.app.model.terminology;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.TransientLocalizedObjectKey;

/**
 * A ProfileTermProvider that delegates to an
 * instance of ProfileTermProvider and
 * falls back to the DefaultProfileTermProvider
 * if the term isn't defined in the instance.
 *
 * @author russ (russ@venturetech.net)
 */
@SuppressWarnings("unused")
@Configurable
public class FallbackProfileTermProvider implements ProfileTermProvider
{
    private static final long serialVersionUID = -1692307008424776324L;
    @Autowired
    private EntityRetriever _entityRetriever;
    @Autowired
    private DefaultProfileTermProvider _defaultProfileTermProvider;

    private final ProfileTermProvider _profileTermProvider;

    /**
     * Instantiates a new Fallback profile term provider.
     *
     * @param profileTermProvider the profile term provider.
     */
    public FallbackProfileTermProvider(ProfileTermProvider profileTermProvider)
    {
        _profileTermProvider = profileTermProvider;
    }

    @Override
    public TextSource company()
    {
        return isBlank(getProfileTermProvider().company())
            ? _defaultProfileTermProvider.company() : getProfileTermProvider().company();
    }

    @Override
    public TextSource companies()
    {
        return isBlank(getProfileTermProvider().companies())
            ? _defaultProfileTermProvider.companies() : getProfileTermProvider().companies();
    }

    /**
     * Gets profile term provider.
     *
     * @return the profile term provider.
     */
    public ProfileTermProvider getProfileTermProvider()
    {
        return _entityRetriever.reattachIfNecessary(_profileTermProvider);
    }
    
    private static boolean isBlank(TextSource textSource)
    {
        if(textSource == null)
            return true;
        if(textSource instanceof TransientLocalizedObjectKey)
        {
            final TransientLocalizedObjectKey tlok = (TransientLocalizedObjectKey) textSource;
            return tlok.getText() == null || tlok.getText().isEmpty();
        }
        return false;
    }
}
