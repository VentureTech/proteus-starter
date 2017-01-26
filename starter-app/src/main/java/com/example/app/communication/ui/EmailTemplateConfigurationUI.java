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

package com.example.app.communication.ui;

import com.example.app.profile.model.Profile;
import com.example.app.profile.service.SelectedCompanyTermProvider;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.mail.internet.AddressException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.i2rd.cms.backend.files.FileField;
import com.i2rd.cms.miwt.LinkChooser;
import com.i2rd.cms.miwt.LinkField;
import com.i2rd.miwt.util.CSSUtil;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.cms.dao.CmsFrontendDAO;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.locale.LocalizedText;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.mail.MailDataHandlerException;
import net.proteusframework.core.mail.MailPart;
import net.proteusframework.core.mail.UnparsedAddress;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.core.notification.Notification;
import net.proteusframework.core.notification.NotificationImpl;
import net.proteusframework.data.filesystem.FileEntity;
import net.proteusframework.data.filesystem.FileSystemDAO;
import net.proteusframework.data.mail.FileEntityMailDataHandler;
import net.proteusframework.email.EmailTemplate;
import net.proteusframework.email.EmailTemplateContext;
import net.proteusframework.email.EmailTemplateException;
import net.proteusframework.email.EmailTemplateProcessor;
import net.proteusframework.email.EmailTemplateRecipient;
import net.proteusframework.internet.http.Link;
import net.proteusframework.ui.miwt.CannotCloseMIWTException;
import net.proteusframework.ui.miwt.component.ComboBox;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Dialog;
import net.proteusframework.ui.miwt.component.Field;
import net.proteusframework.ui.miwt.component.HTMLComponent;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.editor.ValueChooser;
import net.proteusframework.ui.miwt.component.composite.editor.ValueChooserDisplayHandler;
import net.proteusframework.ui.miwt.data.SimpleListModel;
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.resource.CKEditorConfig;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.dao.PrincipalDAO;

import static com.example.app.communication.ui.EmailTemplateConfigurationUILOK.*;
import static com.example.app.profile.ui.UIText.PROFILE;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static net.proteusframework.core.StringFactory.capitalize;
import static net.proteusframework.core.StringFactory.isEmptyString;
import static net.proteusframework.core.locale.TextSources.createText;

/**
 * UI for configuring an email template
 * for sending to a recipient.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
@I18NFile(
    classVisibility = I18NFile.Visibility.PUBLIC,
    symbolPrefix = "com.example.app.communication.ui.EmailTemplateConfigurationUI",
    i18n = {
        @I18N(symbol = "Recipient TO", l10n = @L10N("Email Recipients")),
        @I18N(symbol = "Label Recipient", l10n = @L10N("Recipients' Email Addresses (comma separated for multiple)")),
        @I18N(symbol = "Label Email Template", l10n = @L10N("Email Template")),
        @I18N(symbol = "Label Preview", l10n = @L10N("Email Preview")),
        @I18N(symbol = "Label Email Template Variables", l10n = @L10N("Email Template Variables")),
        @I18N(symbol = "Instructions Add Recipient", l10n =
        @L10N("Enter the email address of the recipient(s) or add a user/role for a {Profile:0} User")),
        @I18N(symbol = "Action Add Recipient", l10n = @L10N("Add Email Recipient")),
        @I18N(symbol = "Action Update Preview", l10n = @L10N("Update Preview")),
        @I18N(symbol = "Title Variables", l10n = @L10N("Variables")),
        @I18N(symbol = "Disjunction", l10n = @L10N("OR"))
    }
)
@Configurable
public class EmailTemplateConfigurationUI extends Container
{
    /** Prefix for email template input variables. */
    public static final String VAR_PREFIX = "var_";

    private static final Pattern PAT_VAR = compile(quote("${") + '(' + VAR_PREFIX + ".+?)" + quote("}"));
    private static final Logger _logger = LogManager.getLogger(EmailTemplateConfigurationUI.class);

    /**
     * Recipient container.
     *
     * @author Russ Tennant (russ@venturetech.net)
     */
    private static class Recipient extends Container
    {
        private final UnparsedAddress _address;

        private TextSource _label;

        public Recipient(@Nonnull UnparsedAddress address)
        {
            _address = address;
        }

        @Override
        public void init()
        {
            super.init();
            setHTMLElement(HTMLElement.span);
            addClassName("recipient");
            _label = createText(getAddress().toString());
            PushButton removeButton = CommonActions.REMOVE.push();
            add(new Label(_label));
            add(removeButton);
            removeButton.addActionListener(ev -> close());
        }

        UnparsedAddress getAddress()
        {
            return _address;
        }

        TextSource getLabel()
        {
            return _label;
        }
    }

    private class Variable extends Container
    {
        private final String _variable;

        /**
         * Instantiates a new Variable.
         *
         * @param variable the variable
         */
        public Variable(String variable)
        {
            _variable = variable;
        }

        @Override
        public void init()
        {
            super.init();
            String value = ofNullable(_variableValues.get(_variable)).orElse("");
            final String variableName = getVariableName(_variable);
            final TextSource labelText = createText(variableName);
            if (variableName.contains("Link"))
            {
                addClassName("variable-wrapper");
                LinkChooser chooser = new LinkChooser();
                LinkField linkField = new LinkField();
                linkField.setLabel(labelText);
                linkField.setValueChooser(chooser);
                chooser.addValueListener(event -> {
                    final Link link = event.getValue();
                    if (link == null || link.getURI() == null)
                    {
                        _variableValues.remove(_variable);
                    }
                    else
                    {

                        _variableValues.put(_variable, link.getURIAsString());
                    }
                    // FUTURE : re-enable when *all* UIs can auto-update
                    //updateEmailTemplatePreview(null);
                });
                linkField.setValueChooserDisplayHandler(new ValueChooserDisplayHandler<Link>()
                {
                    @Override
                    public void displayChooser(ValueChooser<Link> defaultChooser)
                    {
                        Dialog dlg = new Dialog(getApplication(), labelText)
                        {
                            @Override
                            public void close() throws CannotCloseMIWTException
                            {
                                remove(chooser);
                                super.close();
                            }
                        };
                        chooser.addValueListener(event -> dlg.close());
                        dlg.add(chooser);
                        dlg.setVisible(true);
                        dlg.addClassName("link-chooser-dialog");
                        getWindowManager().add(dlg);

                    }
                });
                linkField.addClassName("prop").addClassName("variable");
                add(linkField);
            }
            else if (variableName.contains("File"))
            {
                addClassName("variable-wrapper");
                FileField fileField = new FileField();
                fileField.setLabel(labelText);
                fileField.addActionListener(ev -> {
                    if (ev.getSource() instanceof FileEntity)
                    {
                        final FileEntity fileEntity = (FileEntity) ev.getSource();
                        final URI localURI = FileSystemDAO.getInstance().getLocalURI(Event.getRequest(), fileEntity);
                        _variableValues.put(_variable, localURI.toString());
                    }
                    else
                    {
                        _variableValues.remove(_variable);
                    }
                    // FUTURE : re-enable when *all* UIs can auto-update
                    //updateEmailTemplatePreview(null);
                });
                fileField.addClassName("prop").addClassName("variable");
                add(fileField);
            }
            else
            {
                addClassName("prop").addClassName("variable").addClassName("variable-wrapper");
                Field field = new Field();
                field.setText(value);
                if (variableName.contains("Content"))
                {
                    field.setRichEditor(true);
                    field.setRichEditorConfig(CKEditorConfig.standard.name());
                    field.setDisplayHeight(5);
                }
                Label label = new Label(labelText);
                label.setLabelFor(field);
                add(label);
                add(field);
                field/*.watchIncremental()*/.addPropertyChangeListener(Field.PROP_TEXT, evt -> {
                    String text = field.getText();
                    if (field.isRichEditor())
                    {
                        final Document document = Jsoup.parse(text);
                        if (document.text().trim().isEmpty())
                            text = "";
                    }
                    _variableValues.put(_variable, text);
                    // FUTURE : re-enable when *all* UIs can auto-update
                    //updateEmailTemplatePreview(null);
                });
            }
        }
    }

    @Nullable
    private final Profile _profile;
    private final ComboBox _emailTemplateChoice = new ComboBox(new SimpleListModel<EmailTemplate>());
    private final HTMLComponent _emailTemplatePreview = new HTMLComponent("");
    private final Field _emailTemplateSubjectField = new Field("", 75);
    private final Map<String, String> _variableValues = new HashMap<>();
    private final Container _recipientContainer = new Container();
    private final Container _variableContainer = new Container();
    @Autowired
    private EmailTemplateProcessor _emailTemplateProcessor;
    @Autowired
    private SelectedCompanyTermProvider _profileTermProvider;
    @Autowired
    private CmsFrontendDAO _siteContext;
    @Autowired
    private EntityRetriever _entityRetriever;
    @Nullable
    private Consumer<EmailTemplateContext> _configurator;
    @Nullable
    private Notifiable _notifiable;
    private EmailTemplate _lastEmailTemplate;
    private final AtomicReference<String> _modifiedSubject = new AtomicReference<>();
    private String _processedSubject;

    private static void _getVariables(List<String> list, String body)
    {
        if (isEmptyString(body))
            return;
        final Matcher matcher = PAT_VAR.matcher(body);
        while (matcher.find())
        {
            final String variable = matcher.group(1);
            if (!list.contains(variable))
                list.add(variable);
        }
    }

    /**
     * Get the body as text from the email data. HTML content will be cleaned for inclusion in an HTML document.
     *
     * @param emailData the email data.
     *
     * @return the body or empty string.
     *
     * @throws MailDataHandlerException on error.
     * @throws IOException on error.
     */
    private static String getBody(FileEntityMailDataHandler emailData) throws MailDataHandlerException, IOException
    {
        String lastBody = "";
        Whitelist whitelist = Whitelist.relaxed();
        whitelist.addProtocols("a", "href", "ftp", "http", "https", "mailto", "cms");
        whitelist.addProtocols("img", "src", "http", "https", "cms");
        Cleaner cleaner = new Cleaner(whitelist);
        for (MailPart body : emailData.getBody())
        {
            if (body.getContentType() == null || !body.getContentType().startsWith("text/"))
                continue;
            try (final InputStream inputStream = body.getInputStream())
            {
                String nextBody = CharStreams.toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).trim();
                if (nextBody.isEmpty())
                    continue;
                lastBody = nextBody;
                if ("text/html".equalsIgnoreCase(body.getContentType()))
                {
                    lastBody = cleaner.clean(Jsoup.parseBodyFragment(lastBody, "")).body().html();
                }
            }
        }
        return lastBody;
    }

    /**
     * Get the variables.
     *
     * @param emailTemplate the email template
     *
     * @return the variables.
     */
    public static List<String> getEmailTemplateVariables(EmailTemplate emailTemplate)
    {
        List<String> list = new ArrayList<>();
        _getVariables(list, emailTemplate.getPlainTextBody());
        _getVariables(list, emailTemplate.getHtmlBody());
        return list;
    }

    /**
     * Get the value for preview / testing.
     *
     * @param element the element.
     * @param value the value.
     *
     * @return the updated value.
     */
    public static String getPreviewValue(HTMLElement element, String value)
    {
        return '<' + element.name() + " class=\"et-user-variable\">" + value + "</" + element.name() + '>';
    }

    /**
     * Get the variable name for the specified variable.
     *
     * @param variable the variable.
     *
     * @return the name.
     */
    public static String getVariableName(String variable)
    {
        String vn = variable;
        if (vn.startsWith(VAR_PREFIX))
            vn = vn.substring(VAR_PREFIX.length());
        vn = vn.replace('_', ' ');
        return capitalize(vn);
    }

    /**
     * Instantiates a new Email template sending ui.
     *
     * @param emailTemplates the email templates
     * @param profile optional profile.
     */
    public EmailTemplateConfigurationUI(List<EmailTemplate> emailTemplates, @Nullable Profile profile)
    {
        super();
        _profile = profile;
        Class<?> configType = null;
        for (EmailTemplate emailTemplate : emailTemplates)
        {
            Preconditions.checkNotNull(emailTemplate.getEmailConfig(), "EmailConfig cannot be null.");
            final Class<?> currentConfigType = Hibernate.getClass(emailTemplate.getEmailConfig());
            if (configType == null)
                configType = currentConfigType;
            else
                Preconditions.checkArgument(configType == currentConfigType,
                    "EmailTemplates must be of the same configuration type.");
        }
        @SuppressWarnings("unchecked")
        final SimpleListModel<EmailTemplate> listModel = (SimpleListModel<EmailTemplate>) _emailTemplateChoice.getModel();
        listModel.getList().addAll(emailTemplates);

        _recipientContainer.addClassName("recipients");
        _variableContainer.addClassName("variable-list");
        _emailTemplatePreview.addClassName("email-preview-content");
        addClassName("email-configuration-ui");
    }

    /**
     * Get the selected email template - hibernate attached.
     *
     * @return the email template.
     */
    public EmailTemplate getEmailTemplate()
    {
        EmailTemplate emailTemplate = EntityRetriever.getInstance().reattachIfNecessary(
            (EmailTemplate) _emailTemplateChoice.getSelectedObject()
        );
        assert emailTemplate != null;
        return emailTemplate;
    }

    /**
     * Set the email template.
     *
     * @param emailTemplate the email template.
     */
    public void setEmailTemplate(EmailTemplate emailTemplate)
    {
        _emailTemplateChoice.setSelectedObject(emailTemplate);
        if (isInited())
            updateEmailTemplatePreview(null);
    }

    /**
     * Get the selected recipients.
     *
     * @return the recipients.
     */
    public List<UnparsedAddress> getRecipients()
    {
        List<UnparsedAddress> addressList = new ArrayList<>(_recipientContainer.getComponentCount());
        _recipientContainer.components().forEachRemaining(
            component -> {
                if (component instanceof Recipient)
                {
                    final Recipient recipient = (Recipient) component;
                    if (recipient.getAddress() != null)
                        addressList.add(recipient.getAddress());
                }
            }
        );
        return addressList;
    }

    /**
     * Set the recipients.
     *
     * @param recipients the recipients.
     */
    public void setRecipients(List<UnparsedAddress> recipients)
    {
        _recipientContainer.removeAllComponents();
        for (UnparsedAddress address : recipients)
            _recipientContainer.add(new Recipient(address));
        sortRecipientContainer();
    }    @Override
    public void init()
    {
        super.init();

        add(of("prop email-template", LABEL_EMAIL_TEMPLATE(), _emailTemplateChoice));

        PushButton addBtn = CommonActions.ADD.push();
        add(
            of("prop recipients-wrapper",
                new Label(RECIPIENT_TO()).withHTMLElement(HTMLElement.label),
                of("actions recipient-actions", addBtn).withHTMLElement(HTMLElement.span),
                _recipientContainer)
        );
        addBtn.addActionListener(this::addRecipient);


        add(of("prop subject", CommonColumnText.SUBJECT, _emailTemplateSubjectField));
        PushButton updateBtn = new PushButton(ACTION_UPDATE_PREVIEW());
        add(of("prop email-preview", new Label(LABEL_PREVIEW()).withHTMLElement(HTMLElement.h1),
            _emailTemplatePreview)
            .withHTMLElement(HTMLElement.section));
        add(of("prop-group variables", of("actions preview-actions", updateBtn),
            new Label(TITLE_VARIABLES()).withHTMLElement(HTMLElement.h1), _variableContainer)
            .withHTMLElement(HTMLElement.section));

        updateBtn.addActionListener(this::updateEmailTemplatePreview);
        _emailTemplateSubjectField.addPropertyChangeListener(Field.PROP_TEXT,
            evt -> {
                String text = _emailTemplateSubjectField.getText();
                _modifiedSubject.set(isEmptyString(text) || Objects.equals(text, _processedSubject) ? null : text);
            });
        _emailTemplateChoice.addActionListener(this::updateEmailTemplatePreview);
        updateEmailTemplatePreview(null);
    }

    private void sortRecipientContainer()
    {
        if (!isAttached())
            return;
        final LocaleContext lc = getLocaleContext();
        _recipientContainer.sort((o1, o2) -> {
            if (o1 instanceof Recipient && o2 instanceof Recipient)
            {
                final Recipient r1 = (Recipient) o1;
                final Recipient r2 = (Recipient) o2;
                return lc.compareIgnoreCase(r1.getLabel(), r2.getLabel());
            }
            return 0;
        });

    }

    /**
     * Get the modified subject.
     *
     * @return the subject if modified.
     */
    @Nullable
    public String getSubject()
    {
        return _modifiedSubject.get();
    }

    /**
     * Set the subject.
     *
     * @param subject the subject.
     */
    public void setSubject(String subject)
    {
        _modifiedSubject.set(isEmptyString(subject) ? null : subject);
        _emailTemplateSubjectField.setText(subject);
    }

    /**
     * Get the variables.
     *
     * @return the variables.
     */
    public Map<String, String> getVariableValues()
    {
        final List<String> currentVariables = getEmailTemplateVariables(getEmailTemplate());
        _variableValues.keySet().removeIf(key -> !currentVariables.contains(key));
        return _variableValues;
    }

    /**
     * Set the variable values.
     *
     * @param variableValues the variable values.
     */
    public void setVariableValues(Map<String, String> variableValues)
    {
        _lastEmailTemplate = null;
        _variableValues.putAll(variableValues);
    }

    /**
     * Set function to configure the email template for preview purposes.
     *
     * @param configurator the configuration function.
     */
    public void setConfigurator(@Nullable Consumer<EmailTemplateContext> configurator)
    {
        _configurator = configurator;
    }

    /**
     * Set the notifiable.
     *
     * @param notifiable the notifiable
     */
    public void setNotifiable(@Nullable Notifiable notifiable)
    {
        _notifiable = notifiable;
    }

    /**
     * Update the EmailTemplate preview.
     *
     * @param ignored ignored.
     */
    protected void updateEmailTemplatePreview(@Nullable ActionEvent ignored)
    {
        EmailTemplate emailTemplate = EntityRetriever.getInstance().reattachIfNecessary(
            (EmailTemplate) _emailTemplateChoice.getSelectedObject()
        );
        if (emailTemplate == null)
        {
            return;
        }

        CmsSite site = _siteContext.getOperationalSite();
        try
        {
            // The sender's permissions determine what data is exposed to the end user.
            Principal permissionPrincipal = PrincipalDAO.getInstance().getCurrentPrincipal();
            EmailTemplateContext etc = new EmailTemplateContext(site, getLocaleContext().getLocale(),
                getSession().getTimeZone(), EmailTemplateContext.Mode.TESTING)
                .withPrincipal(permissionPrincipal);
            final List<UnparsedAddress> recipients = getRecipients();
            if (recipients.isEmpty())
            {
                etc.withRecipientAttribute(etc.getPrincipal());
            }
            else
            {
                etc.withRecipientAttribute(new EmailTemplateRecipient(recipients.get(0).toInternetAddress()));
            }

            if (_configurator != null)
                _configurator.accept(etc);

            getVariableValues().entrySet()
                .forEach(entry -> {
                    String value = entry.getValue();
                    final String key = entry.getKey();
                    if (!(key.contains("_file") || key.contains("_link")))
                    {
                        HTMLElement el = HTMLElement.span;
                        if (key.contains("_content"))
                            el = HTMLElement.div;
                        value = getPreviewValue(el, value);
                    }
                    etc.getAttributes().put(key, value);
                });
            FileEntityMailDataHandler emailData = _emailTemplateProcessor.process(etc, emailTemplate);

            String newBody = getBody(emailData);
            _emailTemplatePreview.setText(new LocalizedText(newBody));
            _processedSubject = emailData.getSubject();
            if (!Objects.equals(emailTemplate, _lastEmailTemplate))
            {
                _modifiedSubject.set(null);
                _emailTemplateSubjectField.setText(_processedSubject);
                updateVariables(emailTemplate);
            }
            _lastEmailTemplate = emailTemplate;
        }
        catch (MailDataHandlerException | EmailTemplateException | IllegalArgumentException | AddressException | IOException e)
        {
            sendNotification(NotificationImpl.create(e));
        }
    }

    @Nullable
    Profile getProfile()
    {
        return _entityRetriever.reattachIfNecessary(_profile);
    }

    void sendNotification(Notification notification)
    {
        if (_notifiable != null)
        {
            _notifiable.sendNotification(notification);
        }
        else
        {
            Level level;
            switch (notification.getType())
            {

                case INFO:
                    level = Level.INFO;
                    break;
                case IMPORTANT:
                    level = Level.WARN;
                    break;
                case ERROR:
                default:
                    level = Level.ERROR;
                    break;
            }
            final LocaleContext localeContext = getLocaleContext();
            String message = notification.getMessage().getText(localeContext).toString();
            if (notification.getMessageDetail() != null)
            {
                message += '\n';
                message += notification.getMessageDetail().getText(localeContext).toString();
            }
            _logger.log(level, message);
        }
    }

    private void addRecipient(ActionEvent actionEvent)
    {
        Dialog dialog = new Dialog(getApplication(), ACTION_ADD_RECIPIENT());
        dialog.setVisible(true);
        getWindowManager().add(dialog);
        Label instructions = new Label(INSTRUCTIONS_ADD_RECIPIENT(PROFILE()));
        instructions.setHTMLElement(HTMLElement.p);
        instructions.addClassName(CSSUtil.CSS_INSTRUCTIONS);
        dialog.add(instructions);

        Field recipient = new Field();
        dialog.addClassName("prop-wrapper").addClassName("prop-editor").addClassName("recipient-editor-dialog");
        dialog.add(of("prop email", LABEL_RECIPIENT(), recipient));

        PushButton okBtn = CommonActions.OK.push();
        PushButton cancelBtn = CommonActions.CANCEL.push();
        dialog.add(of("actions persistence-actions", okBtn, cancelBtn));
        cancelBtn.addActionListener(new Dialog.Closer());
        final ActionListener okAction = ev -> {
            final List<UnparsedAddress> addressList = UnparsedAddress.create(recipient.getText());
            for (UnparsedAddress address : addressList)
                _recipientContainer.add(new Recipient(address));
            sortRecipientContainer();
            dialog.close();
        };
        okBtn.addActionListener(okAction);
        recipient.addActionListener(okAction);

    }

    private void updateVariables(EmailTemplate emailTemplate)
    {
        final List<String> variableList = getEmailTemplateVariables(emailTemplate);
        final Component variableContainerParent = _variableContainer.getParent();
        if (variableList.isEmpty())
        {
            if (variableContainerParent != null)
                variableContainerParent.setVisible(false);
            return;
        }
        _variableContainer.removeAllComponents();
        if (variableContainerParent != null)
            variableContainerParent.setVisible(true);
        for (String variable : variableList)
        {
            _variableContainer.add(new Variable(variable));
        }
    }




}
