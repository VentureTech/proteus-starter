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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Supplier;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.xml.TagListener;
import net.proteusframework.core.xml.TagListenerConfiguration;
import net.proteusframework.core.xml.XMLUtil;
import net.proteusframework.ui.management.ApplicationFunction;

/**
 * {@link TagListener} implementation that uses custom attribute (to-app-function) on anchor tag for
 * generating a link to an {@link ApplicationFunction}.  Processes all other anchor tags normally.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 5/16/16 11:28 AM
 */
public class ApplicationFunctionTagListener extends TagListener<TagListenerConfiguration>
{
    private final String _applicationFunctionName;
    private final Supplier<String> _urlSupplier;

    /**
     * Creates a new instance.
     *
     * @param applicationFunctionName the ApplicationFunction name -- used to
     * @param urlSupplier supplier for creating a url to the application function.
     * Supplier may return null or an empty string if a url could not be created.
     */
    public ApplicationFunctionTagListener(String applicationFunctionName, Supplier<String> urlSupplier)
    {
        _applicationFunctionName = applicationFunctionName;
        _urlSupplier = urlSupplier;
    }

    @Override
    public String[] getSupportedTags()
    {
        return new String[]{"a"};
    }

    @Override
    public boolean startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        String appFunction = attributes.getValue("data-app-function");
        if (!StringFactory.isEmptyString(appFunction)
            && Objects.equals(appFunction, _applicationFunctionName))
        {
            String url = _urlSupplier.get();
            if (!StringFactory.isEmptyString(url))
            {
                getConfiguration().getWriter().append("<a href=\"")
                    .append(url).append("\"");
                XMLUtil.writeAttributes(getConfiguration().getWriter(), attributes, Collections.singletonList("href"));
                getConfiguration().getWriter().append(">");
                return true;
            }
        }
        getConfiguration().getWriter().append("<a");
        XMLUtil.writeAttributes(getConfiguration().getWriter(), attributes, Collections.emptyList());
        getConfiguration().getWriter().append('>');
        return true;
    }

    @Override
    public void closeStartElement() throws SAXException
    {
        //Do Nothing
    }

    @Override
    public boolean characters(String characters, boolean closedStartElement) throws SAXException
    {
        if (!StringFactory.isEmptyString(characters))
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
}
