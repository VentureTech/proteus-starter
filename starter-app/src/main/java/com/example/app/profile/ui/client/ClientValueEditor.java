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

package com.example.app.profile.ui.client;

import com.example.app.profile.model.client.Client;
import com.example.app.profile.model.client.ClientStatus;
import com.example.app.profile.model.location.Location;
import com.example.app.profile.service.SelectedCompanyTermProvider;
import com.example.app.support.service.AppUtil;
import com.example.app.support.ui.CommonEditorFields;
import com.example.app.support.ui.contact.AddressValueEditor;
import com.example.app.support.ui.contact.AddressValueEditor.AddressValueEditorConfig;
import com.example.app.support.ui.contact.EmailAddressValueEditor;
import com.example.app.support.ui.contact.EmailAddressValueEditor.EmailAddressValueEditorConfig;
import com.example.app.support.ui.contact.PhoneNumberValueEditor;
import com.example.app.support.ui.contact.PhoneNumberValueEditor.PhoneNumberValueEditorConfig;
import com.example.app.support.ui.vtcrop.VTCropPictureEditor;
import com.example.app.support.ui.vtcrop.VTCropPictureEditorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;

import com.i2rd.miwt.util.CSSUtil;

import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.data.filesystem.http.FileEntityFileItem;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.CompositeValueEditor;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;

import static com.example.app.profile.ui.UIText.INSTRUCTIONS_PICTURE_EDITOR_FMT;
import static com.example.app.profile.ui.client.ClientValueEditorLOK.LABEL_LOGO;

/**
 * ValueEditor for Client
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/16/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.client.ClientValueEditor",
    i18n = {
        @I18N(symbol = "Label Logo", l10n = @L10N("Logo"))
    }
)
@Configurable
public class ClientValueEditor extends CompositeValueEditor<Client>
{
    @Autowired private ClientConfig _clientConfig;
    @Autowired private SelectedCompanyTermProvider _terms;
    @Autowired private AppUtil _appUtil;

    private VTCropPictureEditor _logoEditor;

    /**
     * Instantiates a new Client value editor.
     */
    public ClientValueEditor()
    {
        super(Client.class);

        addClassName("client-value-editor");
    }

    @Override
    public void init()
    {
        VTCropPictureEditorConfig logoConfig = _clientConfig.clientLogoConfig();
        _logoEditor = new VTCropPictureEditor(logoConfig);
        _logoEditor.addClassName("client-logo");
        _logoEditor.setDefaultResource(_appUtil.getDefaultResourceImage());

        super.init();

        Label logoInstructions = new Label(INSTRUCTIONS_PICTURE_EDITOR_FMT(logoConfig.getCropWidth(), logoConfig.getCropHeight()));
        logoInstructions.addClassName(CSSUtil.CSS_INSTRUCTIONS);
        logoInstructions.withHTMLElement(HTMLElement.div);

        add(of("prop logo", LABEL_LOGO(), _logoEditor, logoInstructions));
        CommonEditorFields.addNameEditor(this);
        addEditorForProperty(() -> {
            final CompositeValueEditor<Location> editor = new CompositeValueEditor<>(Location.class);

            editor.addEditorForProperty(() -> {
                AddressValueEditorConfig cfg = new AddressValueEditorConfig();
                return new AddressValueEditor(cfg);
            }, Location.ADDRESS_PROP);

            editor.addEditorForProperty(() -> {
                EmailAddressValueEditorConfig cfg = new EmailAddressValueEditorConfig();
                cfg.setRequiredFields();
                return new EmailAddressValueEditor(cfg);
            }, Location.EMAIL_ADDRESS_PROP);

            editor.addEditorForProperty(() -> {
                PhoneNumberValueEditorConfig cfg = new PhoneNumberValueEditorConfig();
                cfg.setRequiredFields();
                return new PhoneNumberValueEditor(cfg);
            }, Location.PHONE_NUMBER_PROP);

            return editor;
        }, Client.PRIMARY_LOCATION_PROP);

        addEditorForProperty(() -> {
            ComboBoxValueEditor<ClientStatus> editor = new ComboBoxValueEditor<>(CommonColumnText.STATUS,
                AppUtil.nullFirst(EnumSet.allOf(ClientStatus.class)), ClientStatus.PENDING);
            editor.setCellRenderer(new CustomCellRenderer(CommonButtonText.PLEASE_SELECT));
            editor.setRequiredValueValidator();
            return editor;
        }, Client.STATUS_PROP);
    }

    @Override
    public void setValue(@Nullable Client value)
    {
        super.setValue(value);

        if(value != null)
        {
            AppUtil.initialize(value);
        }
        if(isInited())
        {
            _logoEditor.setValue(Optional.ofNullable(value)
                .map(Client::getLogo)
                .map(FileEntityFileItem::new)
                .orElse(null));
        }
    }

    @Override
    public void setEditable(boolean b)
    {
        super.setEditable(b);

        if(isInited())
        {
            _logoEditor.setEditable(b);
        }
    }

    @Override
    public boolean validateUIValue(Notifiable notifiable)
    {
        final boolean valid = super.validateUIValue(notifiable);
        return _logoEditor.validateUIValue(notifiable) && valid;
    }

    @Override
    public ModificationState getModificationState()
    {
        return AppUtil.getModificationStateForComponent(this);
    }

    /**
     * Gets logo editor.
     *
     * @return the logo editor
     */
    @Nonnull
    public VTCropPictureEditor getLogoEditor()
    {
        return Optional.ofNullable(_logoEditor).orElseThrow(() ->
            new IllegalStateException("PictureEditor was null.  Do not call getLogoEditor before initialization"));
    }
}
