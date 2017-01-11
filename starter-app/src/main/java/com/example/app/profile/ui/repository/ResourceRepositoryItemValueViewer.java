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

package com.example.app.profile.ui.repository;


import com.example.app.profile.model.repository.ResourceRepositoryItem;
import com.example.app.profile.model.resource.Resource;
import com.example.app.resource.ui.ResourceText;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.proteusframework.cms.controller.RendererComponent;
import net.proteusframework.cms.support.ImageFileUtil;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.metric.Dimension;
import net.proteusframework.ui.miwt.Image;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.ImageComponent;
import net.proteusframework.ui.miwt.component.Label;

import static net.proteusframework.core.StringFactory.isEmptyString;
import static net.proteusframework.core.locale.TextSources.createText;

/**
 * Viewer for {@link ResourceRepositoryItem}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/11/15 3:21 PM
 */
@Configurable
public class ResourceRepositoryItemValueViewer extends Container
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ResourceRepositoryItemValueViewer.class);
    private final ResourceRepositoryItem _value;
    @Autowired
    private EntityRetriever _er;

    /**
     * Instantiate a new instance of ResourceRepositoryItemValueViewer
     *
     * @param value the ResourceRepositoryItem to view
     */
    public ResourceRepositoryItemValueViewer(@Nonnull ResourceRepositoryItem value)
    {
        super();
        Preconditions.checkNotNull(value, "ResourceRepositoryItem was null.  This should not happen.");
        _value = value;
        addClassName("resource-viewer");
    }

    @Override
    public void init()
    {
        super.init();

        ResourceRepositoryItem repoItem = getValue();
        final Resource resource = repoItem.getResource();

        boolean hasAuthor = !isEmptyString(resource.getAuthor());
        boolean hasSource = !isEmptyString(repoItem.getSource());

        LocaleContext lc = getLocaleContext();

        final ImageComponent resourceImage = new ImageComponent();
        if (resource.getImage() != null)
        {
            try
            {
                final Dimension size = ImageFileUtil.getDimension(resource.getImage(), true);
                if (size != null)
                    resourceImage.setSize(size);
            }
            catch (IOException e)
            {
                _logger.debug("Unable to get file dimension.", e);
            }
            resourceImage.setImage(new Image(resource.getImage()));
        }
        resourceImage.setImageCaching(false);
        resourceImage.addClassName("resource-image");
        final Container resourceImageField = of("resource-icon", resourceImage);

        final Label nameLabel = new Label(resource.getName());
        nameLabel.withHTMLElement(HTMLElement.h1);
        nameLabel.addClassName("resource-name");

        final Label descriptionLabel = new Label(resource.getDescription());
        descriptionLabel.withHTMLElement(HTMLElement.p);
        descriptionLabel.addClassName("resource-description");

        final RendererComponent content = new RendererComponent(resource.getResourceType().createRenderer(resource));
        content.setElement(HTMLElement.span);
        content.addClassName("resource-content");


        final Label sourceLabel = new Label(createText(repoItem.getSource()));
        sourceLabel.withHTMLElement(HTMLElement.span);
        final Label authorLabel = new Label(createText(resource.getAuthor()));
        authorLabel.withHTMLElement(HTMLElement.span);
        List<Component> citeList = new ArrayList<>();
        if (hasSource) citeList.add(of("resource-source", ResourceText.LABEL_SOURCE(), sourceLabel));
        if (hasAuthor) citeList.add(of("resource-author", ResourceText.LABEL_AUTHOR(), authorLabel));

        add(nameLabel);
        if (resource.getImage() != null)
            add(resourceImageField);
        if (!lc.isError(lc.getLocalizedText(resource.getDescription())))
            add(descriptionLabel);
        add(content);
        if (!citeList.isEmpty())
            add(of("resource-cite", citeList.toArray(new Component[citeList.size()])));
    }

    /**
     * Get the RepositoryItem being viewed
     *
     * @return the Value
     */
    public ResourceRepositoryItem getValue()
    {
        return _er.reattachIfNecessary(_value);
    }
}
