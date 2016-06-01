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

package com.example.app.ui.user;

import com.example.app.model.user.ContactMethod;

import javax.annotation.Nonnull;

/**
 * Class for holding values for processing a User Contact form or bulk action
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/16/15 10:54 AM
 */
public class MessageContext
{
    private ContactMethod _contactMethod;
    private String _content;
    private String _emailSubject;

    /**
     * Instantiate a new instance of ContactUser with the given ContactMethod
     *
     * @param contactMethod the contactMethod to use
     */
    public MessageContext(@Nonnull ContactMethod contactMethod)
    {
        _contactMethod = contactMethod;
    }

    /**
     * Get the contact method for this ContactUser instance
     *
     * @return the contact method
     */
    @Nonnull
    public ContactMethod getContactMethod()
    {
        return _contactMethod;
    }

    /**
     * Set the contact method for this ContactUser instance
     *
     * @param contactMethod the contact method
     */
    public void setContactMethod(@Nonnull ContactMethod contactMethod)
    {
        _contactMethod = contactMethod;
    }

    /**
     * Get the content for this ContactUser instance
     *
     * @return the message content
     */
    public String getContent()
    {
        return _content;
    }

    /**
     * Set the content for this ContactUser instance
     *
     * @param content the message content
     */
    public void setContent(String content)
    {
        _content = content;
    }

    /**
     * Get the email subject for this ContactUser instance
     * <br>
     * Only used if {@link #getContactMethod()} returns {@link ContactMethod#Email}
     *
     * @return the email subject
     */
    public String getEmailSubject()
    {
        return _emailSubject;
    }

    /**
     * Set the email subject for this ContactUser instance
     * <br>
     * Only used if {@link #getContactMethod()} returns {@link ContactMethod#Email}
     *
     * @param emailSubject the email subject
     */
    public void setEmailSubject(String emailSubject)
    {
        _emailSubject = emailSubject;
    }
}
