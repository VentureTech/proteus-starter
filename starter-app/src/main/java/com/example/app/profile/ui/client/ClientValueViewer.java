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
import com.example.app.profile.model.client.ClientLOK;
import com.example.app.profile.model.client.ClientStatus;
import com.example.app.profile.model.location.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import java.util.EnumSet;

import com.i2rd.hr.miwt.AddressCellRenderer;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.internet.http.Request;
import net.proteusframework.internet.http.Response;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.LocalizedTextEditor;
import net.proteusframework.ui.miwt.component.composite.editor.TextEditor;
import net.proteusframework.ui.miwt.component.template.FileSystemTemplateDataSource;
import net.proteusframework.ui.miwt.component.template.TemplateContainer;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.miwt.util.RendererEditorState;

/**
 * Diaplays basic information about a Client
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/13/17
 */
@Configurable
public class ClientValueViewer extends TemplateContainer
{
    private static final String PROP_PAGE_REFRESHED = "page.refreshed";

    @Autowired private EntityRetriever _er;

    private final Client _client;

    /**
     * Instantiates a new Client value viewer.
     *
     * @param client the client
     */
    public ClientValueViewer(@Nonnull Client client)
    {
        super(new FileSystemTemplateDataSource("profile/client/ClientValueViewer.xml"));
        _client = client;
        addClassName("client-value-viewer");
        setComponentName("client-value-viewer");
    }

    /**
     * Gets client.
     *
     * @return the client
     */
    @Nonnull
    public Client getClient()
    {
        return _er.reattachIfNecessary(_client);
    }

    @Override
    public void preRenderProcess(Request request, Response response, RendererEditorState<?> state)
    {
        super.preRenderProcess(request, response, state);

        if(!request.isPartial() && isInited())
        {
            firePropertyChange(PROP_PAGE_REFRESHED, false, true);
        }
    }

    @Override
    public void init()
    {
        super.init();

        final Client client = getClient();
        final Location primaryLoc = client.getPrimaryLocation();

        final LocalizedTextEditor nameField = new LocalizedTextEditor(ClientLOK.CLIENT_NAME_PROP_NAME(), client.getName());
        nameField.setEditable(false);
        nameField.addClassName("name");
        nameField.setComponentName("name");

        final AddressCellRenderer addressRenderer = new AddressCellRenderer(primaryLoc.getAddress());
        final Container addressFields = Container.of("prop address", ClientLOK.ADDRESS_PROP_NAME(), addressRenderer);
        addressFields.withComponentName("address");

        String email = primaryLoc.getEmailAddress() == null ? "" : primaryLoc.getEmailAddress().getEmail();
        final TextEditor emailField = new TextEditor(ClientLOK.EMAIL_ADDRESS_PROP_NAME(), email);
        emailField.setEditable(false);
        emailField.addClassName("email");
        emailField.setComponentName("email");

        String phone = primaryLoc.getPhoneNumber() == null ? "" : primaryLoc.getPhoneNumber().toExternalForm();
        final TextEditor phoneField = new TextEditor(ClientLOK.PHONE_PROP_NAME(), phone);
        phoneField.setEditable(false);
        phoneField.addClassName("phone");
        phoneField.setComponentName("phone");

        final ComboBoxValueEditor<ClientStatus> statusField = new ComboBoxValueEditor<>(CommonColumnText.STATUS,
            EnumSet.allOf(ClientStatus.class), client.getStatus());
        statusField.setEditable(false);
        statusField.addClassName("status");
        statusField.setComponentName("status");

        add(nameField);
        add(addressFields);
        add(emailField);
        add(phoneField);
        add(statusField);

        addPropertyChangeListener(PROP_PAGE_REFRESHED, evt -> {
            final Client refresh = getClient();
            final Location refreshPrimaryLoc = refresh.getPrimaryLocation();

            nameField.setValue(refresh.getName());
            addressRenderer.setAddress(refreshPrimaryLoc.getAddress());
            String refreshEmail = refreshPrimaryLoc.getEmailAddress() == null
                ? ""
                : refreshPrimaryLoc.getEmailAddress().getEmail();
            emailField.setValue(refreshEmail);
            String refreshPhone = refreshPrimaryLoc.getPhoneNumber() == null
                ? ""
                : refreshPrimaryLoc.getPhoneNumber().toExternalForm();
            phoneField.setValue(refreshPhone);
            statusField.setValue(refresh.getStatus());
        });

        applyTemplate();
    }
}
