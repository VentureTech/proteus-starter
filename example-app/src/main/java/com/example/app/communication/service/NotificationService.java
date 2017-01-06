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

package com.example.app.communication.service;


import com.example.app.profile.model.Profile;
import com.example.app.profile.model.user.User;
import com.example.app.support.service.ContactUtil;
import com.google.common.base.Preconditions;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.i2rd.java.qrelay.mail.StaticMailRequest;
import com.i2rd.mail.MailConfig;
import com.i2rd.mail.model.TrackedEmail;
import com.i2rd.qrelay.scheduled.ScheduledRequestProcessor;

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
    @Autowired
    private ScheduledRequestProcessor _scheduledRequestProcessor;
    @Autowired
    private MailConfig _mailConfig;
    @Autowired
    private SmsService _smsService;
    @Autowired
    private EmailTemplateProcessor _emailTemplateProcessor;
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

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        _deploymentContext = DeploymentContext.getContext();
    }

    /**
     * Send an email.
     *
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
     *
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
            try (MailMessage mm = new MailMessage(mdh))
            {
                if (mm.getToRecipients().isEmpty())
                {
                    final EmailTemplateRecipient recipientAttribute = context.getRecipientAttribute();
                    if (recipientAttribute != null)
                        mm.addTo(recipientAttribute.getEmailAddress());
                }
                if (mm.getFrom().isEmpty())
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
     * Send email.
     *
     * @param profile the plan.
     * @param mm the message.
     */
    public void sendEmail(Profile profile, MailMessage mm)
    {
        try
        {
            if (_deploymentContext != DeploymentContext.release)
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
     * Send email
     *
     * @param emailTemplate the email template
     * @param context the context
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
     * Send email
     *
     * @param mm the message.
     */
    public void sendEmail(MailMessage mm)
    {
        try
        {
            _mailConfig.mailProviderHandler().sendMessage(mm.getMessage());
        }
        catch (MessagingException e)
        {
            _logger.error("Unable to send message.", e);
        }
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
        sendSMS(user, phoneNumber, content, null);
    }

    /**
     * Send an SMS message delaying until an appropriate time if necessary.
     *
     * @param user the user.
     * @param phoneNumber the phone number to send too.
     * @param content the content of the message.
     * @param delayMillis the delay millis
     */
    public void sendSMS(User user, PhoneNumber phoneNumber, String content, @Nullable Long delayMillis)
    {
        // TODO : make sure SMS messages are delayed if necessary based on user timezone, etc.
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
     * Send email.
     *
     * @param emailTemplate the email template
     * @param context the context
     * @param defaultFromNameSupplier the supplier for the name of the default sender.
     * @param delayMillis the delay millis
     */
    public void sendEmail(EmailTemplate emailTemplate, EmailTemplateContext context,
        @Nullable Supplier<String> defaultFromNameSupplier, Long delayMillis)
    {
        try
        {
            Preconditions.checkArgument(delayMillis >= 0, "Invalid arg: delayAmount. Expecting a number >= 0");
            final FileEntityMailDataHandler mdh = _emailTemplateProcessor.process(context, emailTemplate);
            StaticMailRequest smr = new StaticMailRequest(mdh);
            if(smr.getTo().length == 0)
            {
                final EmailTemplateRecipient recipientAttribute = context.getRecipientAttribute();
                if(recipientAttribute != null)
                    smr.setTo(new String[]{recipientAttribute.getEmailAddress().getAddress()});
            }
            if(StringFactory.isEmptyString(smr.getFrom()))
            {
                UnparsedAddress from = new UnparsedAddress(_systemSender,
                    defaultFromNameSupplier != null ? defaultFromNameSupplier.get() : null);
                smr.setFrom(from.getAddress());
            }
            EmailTemplate et = (EmailTemplate) mdh.getAttribute(EmailTemplate.class);
            if (et != null)
            {
                smr.setMessageName(et.getProgrammaticName());
            }
            smr.setScheduledTime(delayMillis == 0
                ? new Date()
                : new Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.toMillis(delayMillis)));

            _scheduledRequestProcessor.save(smr);
        }
        catch(MailDataHandlerException | EmailTemplateException e)
        {
            _logger.error("An unexpected error occurred while processing the email.", e);
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
            if (partBuffer.length() + substring.length() > SMS_MESSAGE_LIMIT)
            {
                if (substring.length() > SMS_MESSAGE_LIMIT)
                {
                    while (substring.length() > SMS_MESSAGE_LIMIT)
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
        if (partBuffer.length() > 0)
            parts.add(partBuffer.toString());
        return parts;
    }

}
