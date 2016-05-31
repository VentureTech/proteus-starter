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

package com.example.app.ui.resource;

import com.example.app.model.resource.FileEntityResource;
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
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.io.EntityUtilWriter;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.http.URLGenerator;
import net.proteusframework.internet.http.HTMLTagHelper;
import net.proteusframework.internet.http.ResponseURL;
import net.proteusframework.ui.miwt.util.CommonActions;

import static com.example.app.ui.resource.FileEntityResourceRendererLOK.LABEL_DOWNLOAD;
import static net.proteusframework.core.StringFactory.getExtension;

/**
 * A {@link Renderer} for a Resource whose ResourceType is {@link FileEntityResourceType}
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@I18NFile(symbolPrefix = "com.lrlabs.ui.resource.FileEntityResourceRenderer", i18n = @I18N(symbol = "Label Download", l10n =
@L10N("Download")))
@Configurable(preConstruction = true)
public class FileEntityResourceRenderer extends GeneratorImpl<PageElement>
{
    @Autowired
    private EntityRetriever _er;
    @Autowired
    private URLGenerator _urlGenerator;

    private final FileEntityResource _resource;
    private CacheableBuilder _cacheableBuilder;

    /**
     *   Instantiate a new instance
     *   @param resource the resource to render
     */
    public FileEntityResourceRenderer(FileEntityResource resource)
    {
        super();
        _resource = resource;
        _urlGenerator.setUseContentDeliveryNetwork(true);
        _urlGenerator.setOutputHostname(true);
    }

    /**
     *   Get the resource.  Makes a call to {@link EntityRetriever#reattachIfNecessary(Object)} before returning.
     *   @return the reattached (if necessary) resource
     */
    public FileEntityResource getResource()
    {
        return _er.reattachIfNecessary(_resource);
    }

    @Override
    public void preRenderProcess(CmsRequest<PageElement> request, CmsResponse response, ProcessChain chain)
    {
        _cacheableBuilder = new CacheableBuilder(request);
        _cacheableBuilder.addEntity(getResource());
    }

    @Override
    public String getIdentity(CmsRequest<PageElement> request)
    {
        return _cacheableBuilder.makeCacheable().getIdentity(request);
    }

    @Override
    public void render(CmsRequest<PageElement> request, CmsResponse response, RenderChain chain) throws IOException
    {
        _urlGenerator.setHostname(request.getHostname());
        EntityUtilWriter pw = response.getContentWriter();

        FileEntity fe = getResource().getFile();
        if(fe != null)
        {
            String fileExtension = getExtension(fe.getName());
            String className = "resource file-resource ext-" + fileExtension;
            pw.append("<span").appendEscapedAttribute("class", className).append(">");

            ResponseURL url = _urlGenerator.createURL(fe);
            url.setAbsolute(true);
            LocaleContext lc = request.getLocaleContext();
            HTMLTagHelper tagHelper = url.getTagHelper(pw);

            pw.append("<span class=\"resource-action view\">");
            url.getLink().setTarget("_blank");
            tagHelper.outputAnchorTag(CommonActions.VIEW.getName().getText(lc), "btn");
            url.getLink().setTarget(null);
            pw.append("</span>");
            pw.append("<span class=\"resource-action download\">");
            pw.append("<a")
                .appendEscapedAttribute("href", url.toString())
                .appendEscapedAttribute("class", "btn")
                .appendEscapedAttribute("data-download", "")
                .append(">")
                .append(LABEL_DOWNLOAD().getText(lc))
                .append("</a>");
            pw.print("</span>");

            pw.append("</span>");
        }
    }
}
