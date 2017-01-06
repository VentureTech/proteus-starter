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

import com.example.app.profile.model.user.ContactMethod;
import com.example.app.support.CustomCKEditorConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.util.CommonButtonText;

import static com.example.app.ui.user.MessageContextPropertyEditorLOK.LABEL_MESSAGE_CONTENT;
import static com.example.app.ui.user.MessageContextValueEditorLOK.LABEL_CONTACT_METHOD;
import static com.example.app.ui.user.MessageContextValueEditorLOK.LABEL_SUBJECT;

/**
 * {@link CompositeValueEditor} implementation for {@link MessageContext}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/16/15 11:00 AM
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.MessageContextValueEditor",
    i18n = {
        @I18N(symbol = "Label Contact Method", l10n = @L10N("Contact Method")),
        @I18N(symbol = "Label Sender", l10n = @L10N("Sender")),
        @I18N(symbol = "Label Subject", l10n = @L10N("Subject")),
        @I18N(symbol = "Label Message Content", l10n = @L10N("Message Content"))
    }
)
public class MessageContextValueEditor extends CompositeValueEditor<MessageContext>
{

    private final ContactMethod _initialContactMethod;
    private ComboBoxValueEditor<ContactMethod> _contactMethodSelector;
    private TextEditor _emailSubjectEditor;
    private TextEditor _messageContentEditor;

    /**
     * Instantiate a new instance of ContactUserValueEditor
     *
     * @param initialContactMethod the contact method to start with initially
     */
    public MessageContextValueEditor(@Nonnull ContactMethod initialContactMethod)
    {
        super(MessageContext.class);

        _initialContactMethod = initialContactMethod;
        setNewInstanceSupplier(() -> new MessageContext(_initialContactMethod));
    }

    @Override
    public void init()
    {
        super.init();

        List<ContactMethod> contactMethods = new ArrayList<>();
        contactMethods.add(null);
        contactMethods.addAll(EnumSet.allOf(ContactMethod.class));
        _contactMethodSelector = new ComboBoxValueEditor<>(LABEL_CONTACT_METHOD(), contactMethods, _initialContactMethod);
        _contactMethodSelector.setCellRenderer(new CustomCellRenderer(CommonButtonText.PLEASE_SELECT));
        _contactMethodSelector.setRequiredValueValidator();
        _contactMethodSelector.getValueComponent().addActionListener(ev -> {
            ContactMethod selected = _contactMethodSelector.getUIValue();
            _emailSubjectEditor.setVisible(selected == ContactMethod.Email);
            _messageContentEditor.getValueComponent().setRichEditor(selected == ContactMethod.Email);
        });

        _emailSubjectEditor = new TextEditor(LABEL_SUBJECT(), null);
        _emailSubjectEditor.setRequiredValueValidator();
        _emailSubjectEditor.setDisplayWidth(50);
        _emailSubjectEditor.setVisible(_initialContactMethod == ContactMethod.Email);

        _messageContentEditor = new TextEditor(LABEL_MESSAGE_CONTENT(), null);
        _messageContentEditor.getValueComponent().setRichEditorConfig(CustomCKEditorConfig.minimal.toString());
        _messageContentEditor.getValueComponent().setRichEditor(false);
        _messageContentEditor.getValueComponent().setDisplayHeight(5);
        _messageContentEditor.getValueComponent().setDisplayWidth(50);
        _messageContentEditor.setRequiredValueValidator();

        addEditorForProperty(() -> _contactMethodSelector, "contactMethod");
        addEditorForProperty(() -> _emailSubjectEditor, "emailSubject");
        addEditorForProperty(() -> _messageContentEditor, "content");
    }
}
