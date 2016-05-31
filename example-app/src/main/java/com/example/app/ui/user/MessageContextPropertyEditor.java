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
import com.example.app.model.user.User;
import com.example.app.service.NotificationService;
import com.example.app.support.ContactUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Nonnull;
import javax.mail.MessagingException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.mail.MailMessage;
import net.proteusframework.core.mail.MemoryMailPart;
import net.proteusframework.core.notification.NotificationType;
import net.proteusframework.ui.miwt.Action;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.composite.Message;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyEditor;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.users.model.ContactDataCategory;
import net.proteusframework.users.model.EmailAddress;
import net.proteusframework.users.model.PhoneNumber;

import static com.example.app.ui.user.MessageContextPropertyEditorLOK.BUTTON_TEXT_SEND;
import static com.example.app.ui.user.MessageContextPropertyEditorLOK.ERROR_PONESMS;

/**
 * {@link PropertyEditor} for providing a UI to contact a user or list of users with a specified contact method.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/16/15 10:05 AM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.MessageContextPropertyEditor",
    i18n = {
        @I18N(symbol = "Label Message Content", l10n = @L10N("Message Content")),
        @I18N(symbol = "Button Text Send", l10n = @L10N("Send")),
        @I18N(symbol = "Error PoneSms", l10n = @L10N("Failed to send SMS message to {0}.")),
    }
)
@Configurable
public class MessageContextPropertyEditor extends PropertyEditor<MessageContext>
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(MessageContextPropertyEditor.class);

    @Autowired
    private NotificationService _notificationService;
    @Autowired
    private EntityRetriever _er;
    @Value("${programmatic_email_sender:noreply@venturetech.net}")
    private String _emailSender;

    private final List<User> _users;
    private final ActionListener _closer;

    /**
     *   Instantiate a new instance of ContactUserPropertyEditor
     *   @param contactMethod the contact method to use for contacting users
     *   @param usersToContact the users to contact
     *   @param closer the action listener to use for closing this component
     */
    public MessageContextPropertyEditor(
        @Nonnull ContactMethod contactMethod, @Nonnull List<User> usersToContact, @Nonnull ActionListener closer)
    {
        super();
        setValueEditor(new MessageContextValueEditor(contactMethod));
        getValueEditor().setValue(new MessageContext(contactMethod));

        _users = usersToContact;
        _closer = closer;
        setHTMLElement(HTMLElement.section);
    }

    private User getUser(User user)
    {
        return _er.reattachIfNecessary(user);
    }

    @Override
    public void init()
    {
        super.init();

        ReflectiveAction sendAction = CommonActions.SAVE.defaultAction();
        sendAction.prop(Action.NAME, BUTTON_TEXT_SEND());

        sendAction.setActionListener(ev -> {
            if (persist(input -> {
                if (input != null)
                {
                    return _users.stream()
                        .map(this::getUser)
                        .map(user -> {
                            switch (input.getContactMethod())
                            {
                                case PhoneSms:
                                    try
                                    {
                                        sendSms(input, user);
                                    }
                                    catch (ExecutionException | InterruptedException | RuntimeException e)
                                    {
                                        _logger.error(e.getMessage(), e);
                                        getNotifiable().sendNotification(
                                            new Message(NotificationType.ERROR,
                                                TextSources.createText(ERROR_PONESMS(), user.getPrincipal().getUsername())));
                                        return false;
                                    }
                                    break;
                                case Email:
                                    sendEmail(input, user);
                                    break;
                                default:
                                    //Shouldn't get here, but you never know.
                                    break;
                            }
                            return true;
                        }).noneMatch(successful -> !successful);
                }
                return false;
            }))
            {
                _closer.actionPerformed(ev);
            }
        });

        ReflectiveAction cancelAction = CommonActions.CANCEL.defaultAction();
        cancelAction.setActionListener(_closer);

        setPersistenceActions(sendAction, cancelAction);
    }

    private void sendSms(MessageContext contactInfo, User user) throws InterruptedException, ExecutionException, RuntimeException
    {
        user = getUser(user);
        Optional<PhoneNumber> numberToSendTo = Optional.ofNullable(user.getSmsPhone());
        if(!numberToSendTo.isPresent())
            numberToSendTo = ContactUtil.getPhoneNumber(user.getPrincipal().getContact(), ContactDataCategory.values());
        if(numberToSendTo.isPresent())
        {
            _notificationService.sendSMS(user, numberToSendTo.get(), contactInfo.getContent());
        }
        else
        {
            _logger.warn("User: " + user.getId() + " does not have a phone number to send to!  Skipping...");
        }
    }

    private void sendEmail(MessageContext contactInfo, User user)
    {
        user = getUser(user);
        Optional<EmailAddress> addressToSendTo = ContactUtil.getEmailAddress(user.getPrincipal().getContact(),
            ContactDataCategory.values());
        if(addressToSendTo.isPresent())
        {
            try (MailMessage message = new MailMessage())
            {
                message.addFrom(_emailSender);
                message.setSubject(contactInfo.getEmailSubject());
                message.addTo(addressToSendTo.get().getEmail());
                message.setBody(new MemoryMailPart(contactInfo.getContent(), "text/html"));
                _notificationService.sendEmail(message);
            }
            catch (MessagingException e)
            {
                _logger.error("Unable to process mail message.", e);
            }
        }
        else
        {
            _logger.warn("User: " + user.getId() + " does not have an email address to send to!  Skipping...");
        }
    }
}
