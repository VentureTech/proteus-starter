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

package com.example.app.resource.ui;

import com.example.app.resource.model.URIResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.IOException;

import net.proteusframework.cms.PageElement;
import net.proteusframework.cms.component.generator.CacheableBuilder;
import net.proteusframework.cms.component.generator.GeneratorImpl;
import net.proteusframework.cms.component.generator.Renderer;
import net.proteusframework.cms.controller.CmsRequest;
import net.proteusframework.cms.controller.CmsResponse;
import net.proteusframework.cms.controller.ProcessChain;
import net.proteusframework.cms.controller.RenderChain;
import net.proteusframework.core.StringFactory;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.io.EntityUtilWriter;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;

import static com.example.app.resource.ui.URIResourceRendererLOK.LABEL_OPEN_URL;

/**
 * A {@link Renderer} for a Resource whose ResourceType is {@link URIResourceType}
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@I18NFile(symbolPrefix = "com.example.app.resource.ui.URIResourceRenderer",
    i18n = @I18N(symbol = "Label Open URL", l10n = @L10N("Open Link")))
@Configurable
class URIResourceRenderer extends GeneratorImpl<PageElement>
{

    private final URIResource _resource;
    @Autowired
    private EntityRetriever _er;
    private CacheableBuilder _cacheableBuilder;

    /**
     * Instantiate a new instance
     *
     * @param resource the resource to render
     */
    public URIResourceRenderer(URIResource resource)
    {
        super();
        _resource = resource;
    }

    @Override
    public void preRenderProcess(CmsRequest<PageElement> request, CmsResponse response, ProcessChain chain)
    {
        _cacheableBuilder = new CacheableBuilder(request);
        _cacheableBuilder.addEntity(getResource());
    }

    /**
     * Get the resource.  Makes a call to {@link EntityRetriever#reattachIfNecessary(Object)} before returning.
     *
     * @return the reattached (if necessary) resource
     */
    public URIResource getResource()
    {
        return _er.reattachIfNecessary(_resource);
    }

    @Override
    public void render(CmsRequest<PageElement> request, CmsResponse response, RenderChain chain) throws IOException
    {
        EntityUtilWriter pw = response.getContentWriter();

        String uri = StringFactory.uriToString(getResource().getUri());

        if (uri != null)
        {
            pw.append("<span").appendEscapedAttribute("class", "resource uri-resource").append(">");

            pw.append("<span class=\"resource-action\">");
            pw.append("<a")
                .appendEscapedAttribute("class", "uri-resource-link btn")
                .appendEscapedAttribute("href", uri)
                .appendEscapedAttribute("target", "_blank")
                .append('>')
                .appendEscapedData(LABEL_OPEN_URL())
                .append("</a>");
            pw.append("</span>");
            pw.append("</span>");
        }
    }

    @Override
    public String getIdentity(CmsRequest<PageElement> request)
    {
        return _cacheableBuilder.makeCacheable().getIdentity(request);
    }
}
