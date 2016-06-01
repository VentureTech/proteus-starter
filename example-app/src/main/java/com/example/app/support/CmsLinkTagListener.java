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

package com.example.app.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.proteusframework.cms.controller.LinkUtil;
import net.proteusframework.core.StringFactory;
import net.proteusframework.core.xml.TagListener;
import net.proteusframework.core.xml.TagListenerConfiguration;
import net.proteusframework.core.xml.XMLUtil;
import net.proteusframework.internet.http.Link;
import net.proteusframework.internet.http.LinkHelper;
import net.proteusframework.internet.http.Site;

import static net.proteusframework.core.StringFactory.isEmptyString;

/**
 * {@link TagListener} implementation for processing cms links into external ones.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 5/16/16 2:00 PM
 */
public class CmsLinkTagListener extends TagListener<TagListenerConfiguration>
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(CmsLinkTagListener.class);

    private final LinkHelper _linkHelper;
    private final Site _site;
    private Supplier<Map<String, String>> _querySupplier;
    private Function<Link, String> _linkConverter;

    /**
     * Instantiates a new instance
     *
     * @param linkHelper the Link Externalizer
     * @param site the site
     */
    public CmsLinkTagListener(@Nonnull LinkHelper linkHelper, @Nonnull Site site)
    {
        _linkHelper = linkHelper;
        _site = site;
    }

    @Override
    public String[] getSupportedTags()
    {
        return new String[]{"a"};
    }

    @Override
    public boolean startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        Link link = Link.getLink(uri, attributes);
        if (link != null && LinkUtil.isInternal(link))
        {
            try
            {
                Link externalLink = _linkHelper.getAbsoluteExternalLink(link, _site);
                if (_querySupplier != null)
                {
                    addQueryToLink(externalLink, _querySupplier.get());
                }
                String newURI = externalLink.getURIAsString();
                if (_linkConverter != null)
                {
                    newURI = _linkConverter.apply(externalLink);
                }
                getConfiguration().getWriter().append("<a href=\"")
                    .append(newURI).append("\"");
                XMLUtil.writeAttributes(getConfiguration().getWriter(), attributes, Collections.singletonList("href"));
                getConfiguration().getWriter().append(">");
                return true;
            }
            catch (URISyntaxException e)
            {
                _logger.error("Unable to externalize link.", e);
                throw new RuntimeException(e);
            }
        }
        getConfiguration().getWriter().append("<a");
        XMLUtil.writeAttributes(getConfiguration().getWriter(), attributes, Collections.emptyList());
        getConfiguration().getWriter().append('>');
        return true;
    }

    private static void addQueryToLink(Link link, Map<String, String> queryMap) throws URISyntaxException
    {
        final URI uri = link.getURI();
        final String query = uri.getQuery();
        final StringBuilder pQuery = new StringBuilder();
        final Iterator<Map.Entry<String, String>> it = queryMap.entrySet().iterator();
        while (it.hasNext())
        {
            try
            {
                final Map.Entry<String, String> entry = it.next();
                pQuery
                    .append(URLEncoder.encode(entry.getKey(), StringFactory.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StringFactory.UTF_8));
            }
            catch (UnsupportedEncodingException e)
            {
                _logger.error("UTF-8 must be supported.", e);
            }
            if (it.hasNext())
                pQuery.append('&');
        }
        final String newQuery = isEmptyString(query)
            ? pQuery.toString()
            : query + '&' + pQuery;

        final URI newURI = new URI(
            uri.getScheme(),
            uri.getUserInfo(),
            uri.getHost(),
            uri.getPort(),
            uri.getPath(),
            newQuery,
            uri.getFragment()
        );
        link.setURI(newURI);
    }

    @Override
    public void closeStartElement() throws SAXException
    {
        //Do Nothing
    }

    @Override
    public boolean characters(String characters, boolean closedStartElement) throws SAXException
    {
        if (!isEmptyString(characters))
        {
            getConfiguration().getWriter().append(characters);
        }
        return true;
    }

    @Override
    public void endElement(String uri, String localName, String qName, boolean empty, boolean closedStartElement)
        throws SAXException
    {
        getConfiguration().getWriter().append("</a>");
    }

    /**
     * Set the link converter for this tag listener
     *
     * @param linkConverter the link converter
     *
     * @return this
     */

    public CmsLinkTagListener withLinkConverter(Function<Link, String> linkConverter)
    {
        _linkConverter = linkConverter;
        return this;
    }

    /**
     * Set the query supplier for this tag listener
     *
     * @param querySupplier the query supplier
     *
     * @return this
     */
    public CmsLinkTagListener withQuerySupplier(Supplier<Map<String, String>> querySupplier)
    {
        _querySupplier = querySupplier;
        return this;
    }
}
