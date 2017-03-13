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
import com.example.app.profile.service.SelectedCompanyTermProvider;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.profile.ui.URLConfigurations;
import com.example.app.profile.ui.URLProperties;
import com.example.app.profile.ui.membership.ProfileTypeMembershipTypeManagement;
import com.example.app.support.ui.Application;
import com.example.app.support.ui.UIPreferences;
import org.springframework.beans.factory.annotation.Autowired;

import net.proteusframework.cms.component.miwt.impl.MIWTPageElementModelContainer;
import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.ConcatTextSource;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigDef;
import net.proteusframework.ui.management.URLProperty;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.TabItemDisplay;
import net.proteusframework.ui.miwt.component.composite.TabbedContainerImpl;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;

import static com.example.app.profile.ui.UIText.INFO;
import static com.example.app.profile.ui.UIText.MEMBERSHIPS;
import static com.example.app.profile.ui.client.ClientViewerComponentLOK.COMPONENT_NAME;

/**
 * Viewer Component for Client
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/16/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.client.ClientViewerComponent",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Client Viewer"))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Client.VIEW,
    description = "Viewer for Client",
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.Client.VIEW,
        properties = @URLProperty(name = URLProperties.CLIENT, type = Client.class),
        pathInfoPattern = URLProperties.CLIENT_PATH_INFO
    )
)
public class ClientViewerComponent extends MIWTPageElementModelContainer
{
    private static final String SELECTED_TAB_PROP = "client-selected-tab-";

    @Autowired private UIPreferences _uiPreferences;
    @Autowired private ClientManagementPermissionCheck _permissionCheck;
    @Autowired private EntityRetriever _er;
    @Autowired private SelectedCompanyTermProvider _terms;

    private Client _client;

    /**
     * Instantiates a new Client viewer component.
     */
    public ClientViewerComponent()
    {
        super();

        setName(COMPONENT_NAME());
        addClassName("client-viewer");
        setHTMLElement(HTMLElement.section);
    }

    /**
     * Gets client.
     *
     * @return the client
     */
    public Client getClient()
    {
        return _er.reattachIfNecessary(_client);
    }

    /**
     * Gets selected tab prop.
     *
     * @return the selected tab prop
     */
    public String getSelectedTabProp()
    {
        return SELECTED_TAB_PROP + (getClient() != null
            ? getClient().getId() != null
            ? getClient().getId()
            : 0 : 0);
    }

    @Override
    public void init()
    {
        super.init();

        final Client client = getClient();

        NavigationAction backAction = CommonActions.BACK.navAction();
        backAction.configure().toPage(ApplicationFunctions.Client.MANAGEMENT)
            .withSourceComponent(this)
            .usingCurrentURLData();
        backAction.setTarget(this, "close");

        TabItemDisplay infoTID = new TabItemDisplay(INFO());
        infoTID.addClassName("client-info");
        TabItemDisplay rolesTID = new TabItemDisplay(MEMBERSHIPS());
        rolesTID.addClassName("roles-management");

        TabbedContainerImpl tabs = new TabbedContainerImpl();
        final ClientPropertyViewer viewer = new ClientPropertyViewer(client);
        viewer.addComponentClosedListener(ev -> queueClose());
        tabs.addTab(infoTID, viewer);
        tabs.addTab(rolesTID, new ProfileTypeMembershipTypeManagement(client.getProfileType())
            .withCanEditCheck(() -> _permissionCheck.checkCanCurrentUserModify(Event.getRequest())));
        tabs.setSelectedIndex(_uiPreferences.getStoredInteger(getSelectedTabProp()).orElse(0));
        tabs.getSelectionModel().addListSelectionListener(e ->
            _uiPreferences.setStoredInteger(getSelectedTabProp(), e.getFirstIndex()));

        TextSource viewClient = ConcatTextSource.create(CommonButtonText.VIEW, _terms.client()).withSpaceSeparator();
        add(new Label(ConcatTextSource.create(viewClient, client.getName()).withSeparator(": "))
            .withHTMLElement(HTMLElement.h1).addClassName("page-header"));
        add(of("actions nav-actions", new PushButton(backAction)));
        add(tabs);
    }

    /**
     * Configure.
     *
     * @param request the request
     */
    @SuppressWarnings("unused") //Used by applicationFunction
    void configure(ParsedRequest request)
    {
        _client = request.getPropertyValue(URLProperties.CLIENT);

        _permissionCheck.checkPermissionsForCurrent(Event.getRequest(), "Insufficient permissions to view page.");
    }
}
