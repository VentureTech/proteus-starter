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
import com.example.app.profile.ui.ApplicationFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyViewer;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;

/**
 * PropertyViewer for Client
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/16/17
 */
@Configurable
public class ClientPropertyViewer extends PropertyViewer
{
    @Autowired private ClientManagementPermissionCheck _permissionCheck;

    /**
     * Instantiates a new Client viewer component.
     *
     * @param client the client
     */
    public ClientPropertyViewer(@Nonnull Client client)
    {
        addClassName("client-viewer");
        setHTMLElement(HTMLElement.section);
        setValueViewer(new ClientValueViewer(client));
    }

    @Nullable
    @Override
    public ClientValueViewer getValueViewer()
    {
        return (ClientValueViewer)super.getValueViewer();
    }

    @Override
    public void init()
    {
        super.init();
        super.lazyInit();

        if(_permissionCheck.checkCanCurrentUserModify(Event.getRequest()))
        {
            NavigationAction edit = CommonActions.EDIT.navAction();
            edit.configure().toPage(ApplicationFunctions.Client.EDIT).usingCurrentURLData().withSourceComponent(this);
            edit.setTarget(this, "close");

            setPersistenceActions(edit);
        }
    }
}
