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

package com.example.app.service;


import com.example.app.model.profile.Profile;
import com.example.app.model.user.User;
import com.example.app.support.ContactUtil;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.urlshortener.Urlshortener;
import com.google.api.services.urlshortener.UrlshortenerRequestInitializer;
import com.google.api.services.urlshortener.model.Url;
import com.google.common.util.concurrent.RateLimiter;
import com.ibm.icu.text.BreakIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import com.i2rd.mail.MailConfig;
import com.i2rd.mail.model.TrackedEmail;

import net.proteusframework.core.StringFactory;
import net.proteusframework.core.mail.EmailValidationService;
import net.proteusframework.core.mail.MailDataHandlerException;
import net.proteusframework.core.mail.MailMessage;
import net.proteusframework.core.mail.UnparsedAddress;
import net.proteusframework.data.mail.FileEntityMailDataHandler;
import net.proteusframework.email.EmailTemplate;
import net.proteusframework.email.EmailTemplateContext;
import net.proteusframework.email.EmailTemplateException;
import net.proteusframework.email.EmailTemplateProcessor;
import net.proteusframework.email.EmailTemplateRecipient;
import net.proteusframework.internet.http.resource.DeploymentContext;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.EmailAddress;
import net.proteusframework.users.model.PhoneNumber;

/**
 * Service for sending notifications.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@SuppressWarnings("unused")
@Service
public class NotificationService implements ApplicationListener<ContextRefreshedEvent>
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(NotificationService.class);

    /** sms limit - 160 7-bit characters, 140 8-bit characters, or 70 16-bit characters */
    private static final int SMS_MESSAGE_LIMIT = 160;

    private DeploymentContext _deploymentContext;
    @Autowired
    private EmailValidationService _emailValidationService;
//    @Autowired
//    private ProfileDAO _profileDAO;
    @Autowired
    private MailConfig _mailConfig;
    @Autowired
    private SmsService _smsService;
    @Autowired
    private EmailTemplateProcessor _emailTemplateProcessor;
    @Value("${google-url-shortener-key}")
    private String _urlShortenerKey;
    @Value("${system.sender}")
    private String _systemSender;

    /**
     * Get the system sender.
     *
     * @return the system sender email address.
     */
    public String getSystemSender()
    {
        return _systemSender;
    }

    /**
     * Create shortened url.
     *
     * @param url the url.
     *
     * @return the shortened URL.
     * @throws IOException if the URL cannot be shortened.
     */
    public String createShortenedURL(String url) throws IOException
    {
        Urlshortener shortener =  new Urlshortener.Builder(new ApacheHttpTransport(), new GsonFactory(), null)
            .setGoogleClientRequestInitializer(new UrlshortenerRequestInitializer(_urlShortenerKey))
            .setApplicationName("Leadership Resources")
            .build();
        Url toInsert = new Url();
        toInsert.setLongUrl(url);
        final Urlshortener.Url.Insert insertOp = shortener.url().insert(toInsert);
        return insertOp.execute().getId();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        _deploymentContext = DeploymentContext.getContext();
    }

    /**
     * Send an SMS message delaying until an appropriate time if necessary.
     *
     * @param user the user.
     * @param phoneNumber the phone number to send too.
     * @param content the content of the message.
     */
    public void sendSMS(User user, PhoneNumber phoneNumber, String content)
    {
        // FIXME : make sure SMS messages are delayed if necessary based on user timezone, etc.
        final Optional<EmailAddress> emailAddress =
            ContactUtil.getEmailAddress(user.getPrincipal().getContact(), ContactDataCategory.values());
        if(!emailAddress.isPresent())
        {
            _logger.error("Users are required to have an email address. Not sending SMS. User.id = " + user.getId());
            return;
        }
        if(!_emailValidationService.checkForValidDomain(emailAddress.get().getEmail(), false))
        {
            _logger.info("User's email address is being filtered/sanitized. Not sending SMS. User.id = " + user.getId());
            return;
        }


        if (content.length() < SMS_MESSAGE_LIMIT)
            _smsService.sendSms(phoneNumber, content);
        else
        {
            RateLimiter rateLimiter = RateLimiter.create(1.5);
            // Space out split message so it isn't interleaved
            final List<String> parts = splitContent(content);
            for (String part : parts)
            {
                rateLimiter.acquire();
                _smsService.sendSms(phoneNumber, part);
            }
        }
    }

    /**
     * Send an email.
     * @param plan the plan.
     * @param emailTemplate the email template.
     * @param context the context.
     */
    public void sendEmail(Profile plan, EmailTemplate emailTemplate, EmailTemplateContext context)
    {
        sendEmail(plan, emailTemplate, context, null);
    }

    /**
     * Send an email.
     * @param plan the plan.
     * @param emailTemplate the email template.
     * @param context the context.
     * @param defaultFromNameSupplier the supplier for the name of the default sender.
     */
    public void sendEmail(Profile plan, EmailTemplate emailTemplate, EmailTemplateContext context,
        @Nullable Supplier<String> defaultFromNameSupplier)
    {
        // FUTURE : delay, send at the right time of the day for user
        try
        {
            final FileEntityMailDataHandler mdh = _emailTemplateProcessor.process(context, emailTemplate);
            try(MailMessage mm = new MailMessage(mdh))
            {
                if(mm.getToRecipients().isEmpty())
                {
                    final EmailTemplateRecipient recipientAttribute = context.getRecipientAttribute();
                    if(recipientAttribute != null)
                        mm.addTo(recipientAttribute.getEmailAddress());
                }
                if(mm.getFrom().isEmpty())
                {
                    UnparsedAddress from = new UnparsedAddress(_systemSender,
                        defaultFromNameSupplier != null ? defaultFromNameSupplier.get() : null);
                    mm.addFrom(from);
                }
                sendEmail(plan, mm);
            }
        }
        catch (EmailTemplateException | MailDataHandlerException e)
        {
            _logger.error("Unable to send message.", e);
        }
    }

    /**
     *   Send email
     *   @param emailTemplate the email template
     *   @param context the context
     */
    public void sendEmail(EmailTemplate emailTemplate, EmailTemplateContext context)
    {
        sendEmail(emailTemplate, context, null);
    }

    /**
     *   Send email
     * @param emailTemplate the email template
     * @param context the context
     * @param defaultFromNameSupplier the supplier for the name of the default sender.
     */
    public void sendEmail(EmailTemplate emailTemplate, EmailTemplateContext context,
        @Nullable Supplier<String> defaultFromNameSupplier)
    {
        try
        {
            final FileEntityMailDataHandler mdh = _emailTemplateProcessor.process(context, emailTemplate);
            try(MailMessage mm = new MailMessage(mdh))
            {
                if(mm.getToRecipients().isEmpty())
                {
                    final EmailTemplateRecipient recipientAttribute = context.getRecipientAttribute();
                    if(recipientAttribute != null)
                        mm.addTo(recipientAttribute.getEmailAddress());
                }
                if(mm.getFrom().isEmpty())
                {
                    UnparsedAddress from = new UnparsedAddress(_systemSender,
                        defaultFromNameSupplier != null ? defaultFromNameSupplier.get() : null);
                    mm.addFrom(from);
                }
                sendEmail(mm);
            }
        }
        catch(EmailTemplateException | MailDataHandlerException e)
        {
            _logger.error("Unable to send message.", e);
        }
    }

    /**
     * Send email.
     *
     * @param profile the plan.
     * @param mm the message.
     *
     */
    public void sendEmail(Profile profile, MailMessage mm)
    {
        try
        {
            if(_deploymentContext != DeploymentContext.release)
            {
                final String contextName = _deploymentContext == DeploymentContext.qa
                    ? _deploymentContext.name()
                    : StringFactory.capitalize(_deploymentContext.name());
                mm.setSubject('{' + contextName + "} " + mm.getSubject());
            }
            final TrackedEmail trackedEmail = _mailConfig.mailProviderHandler().sendMessage(mm.getMessage());
            // FIXME : you should associate the tracked email to the interaction responsible for triggering it.
        }
        catch (MessagingException e)
        {
            _logger.error("Unable to send message.", e);
        }
    }

    /**
     *   Send email
     *   @param mm the message.
     */
    public void sendEmail(MailMessage mm)
    {
        try
        {
            _mailConfig.mailProviderHandler().sendMessage(mm.getMessage());
        }
        catch(MessagingException e)
        {
            _logger.error("Unable to send message.", e);
        }
    }

    /**
     * Split content list.
     *
     * @param content the content.
     *
     * @return the parts.
     */
    static List<String> splitContent(String content)
    {
        List<String> parts = new ArrayList<>();
        StringBuilder partBuffer = new StringBuilder();
        final BreakIterator wb = BreakIterator.getSentenceInstance(Locale.ENGLISH);
        wb.setText(content);
        int start = wb.first();
        for (int end = wb.next();
             end != BreakIterator.DONE;
             start = end, end = wb.next())
        {
            String substring = content.substring(start, end);
            if(partBuffer.length() + substring.length() > SMS_MESSAGE_LIMIT)
            {
                if(substring.length() > SMS_MESSAGE_LIMIT)
                {
                    while(substring.length() > SMS_MESSAGE_LIMIT)
                    {
                        int idx = SMS_MESSAGE_LIMIT - partBuffer.length();
                        String piece = substring.substring(0, idx);
                        partBuffer.append(piece);
                        parts.add(partBuffer.toString());
                        partBuffer.setLength(0);
                        substring = substring.substring(idx);
                    }
                    partBuffer.append(substring);
                }
                else
                {
                    parts.add(partBuffer.toString());
                    partBuffer.setLength(0);
                    partBuffer.append(substring);
                }
            }
            else
            {
                partBuffer.append(substring);
            }
        }
        if(partBuffer.length() > 0)
            parts.add(partBuffer.toString());
        return parts;
    }

}
