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

import com.example.app.profile.model.ProfileDAO;
import com.example.app.profile.model.client.Client;
import com.example.app.profile.model.client.ClientDAO;
import com.example.app.profile.service.ProfileUIService;
import com.example.app.profile.service.SelectedCompanyTermProvider;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.profile.ui.URLConfigurations;
import com.example.app.profile.ui.URLProperties;
import com.example.app.support.service.AppUtil;
import com.example.app.support.ui.Application;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelPropertyEditor;

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
import net.proteusframework.ui.management.nav.config.CurrentURLPropertyValueResolver;
import net.proteusframework.ui.management.nav.config.PropertyValueResolverParameter;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;

import static com.example.app.profile.ui.client.ClientPropertyEditorLOK.COMPONENT_NAME;
import static java.lang.Boolean.TRUE;

/**
 * PropertyEditor implementation for Client
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/16/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.client.ClientPropertyEditor",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Client Editor"))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Client.EDIT,
    description = "Editor for Client",
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.Client.EDIT,
        properties = @URLProperty(name = URLProperties.CLIENT, type = Client.class),
        pathInfoPattern = URLProperties.CLIENT_PATH_INFO
    )
)
public class ClientPropertyEditor extends MIWTPageElementModelPropertyEditor<Client>
{
    @Autowired private ClientManagementPermissionCheck _permissionCheck;
    @Autowired private ProfileDAO _profileDAO;
    @Autowired private ClientDAO _clientDAO;
    @Autowired private AppUtil _appUtil;
    @Autowired private SelectedCompanyTermProvider _terms;
    @Autowired private ProfileUIService _uiService;

    private Client _saved;

    /**
     * Instantiates a new Client property editor.
     */
    public ClientPropertyEditor()
    {
        super();
        setName(COMPONENT_NAME());
        addClassName("client-editor");
        setHTMLElement(HTMLElement.section);
        setValueEditor(new ClientValueEditor());
    }

    @Override
    public ClientValueEditor getValueEditor()
    {
        return (ClientValueEditor)super.getValueEditor();
    }

    /**
     * Gets saved.
     *
     * @return the saved
     */
    public Client getSaved()
    {
        return _saved;
    }

    /**
     * Sets saved.
     *
     * @param saved the saved
     */
    public void setSaved(Client saved)
    {
        _saved = saved;
    }

    @Override
    public void init()
    {
        super.init();
        super.lazyInit();

        final ReflectiveAction save = CommonActions.SAVE.defaultAction();
        save.setActionListener(ev -> {
            if(persist(client -> {
                final ClientValueEditor editor = getValueEditor();
                assert client != null : "Client should not be null if you are persisting!";
                setProfileTypeIfNeeded(client);
                client.setCompany(_uiService.getSelectedCompany());

                client = _clientDAO.saveClient(client);
                if(editor.getLogoEditor().getModificationState().isModified())
                {
                    client = _clientDAO.saveLogo(client,
                        _clientDAO.getLogoDirectory(_appUtil.getSite(), client),
                        editor.getLogoEditor().commitValue());
                }

                setSaved(client);

                return TRUE;
            }))
            {
                final NavigationAction savedAction = CommonActions.SAVE.navAction();
                savedAction.configure().toReturnPath(ApplicationFunctions.Client.VIEW)
                    .withSourceComponent(this);
                savedAction.setPropertyValueResolver(new CurrentURLPropertyValueResolver(){
                    @Override
                    public Map<String, Object> resolve(PropertyValueResolverParameter parameter)
                    {
                        final Map<String,Object> params = super.resolve(parameter);
                        params.put(URLProperties.CLIENT, getSaved());
                        return params;
                    }
                });
                savedAction.setTarget(this, "close");

                savedAction.actionPerformed(ev);
            }
        });

        final NavigationAction cancel = CommonActions.CANCEL.navAction();
        cancel.configure().toReturnPath(ApplicationFunctions.Client.MANAGEMENT).usingCurrentURLData()
            .withSourceComponent(this);
        cancel.setTarget(this, "close");

        setPersistenceActions(save, cancel);

        final boolean isNew = !Optional.ofNullable(getValueEditor().getValue())
            .filter(c -> c.getId() != null && c.getId() > 0).isPresent();
        final TextSource editClient = ConcatTextSource.create(
            isNew ? CommonButtonText.NEW : CommonButtonText.EDIT,
            _terms.client()).withSpaceSeparator();
        final TextSource clientName = Optional.ofNullable(getValueEditor().getValue())
            .filter(c -> c.getId() != null && c.getId() > 0)
            .map(Client::getName)
            .map(n -> (TextSource)n)
            .orElse(CommonButtonText.NEW);

        moveToTop(new Label(isNew
            ? editClient
            : ConcatTextSource.create(editClient, clientName).withSeparator(": "))
            .withHTMLElement(HTMLElement.h1).addClassName("page-header"));
    }

    private void setProfileTypeIfNeeded(Client client)
    {
        if(client.getProfileType() == null || client.getProfileType().getId() == null || client.getProfileType().getId() == 0)
        {
            client.setProfileType(_profileDAO.mergeProfileType(_clientDAO.createClientProfileType(null)));
        }
    }

    /**
     * Configure.
     *
     * @param request the request
     */
    @SuppressWarnings("unused") //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        _permissionCheck.checkPermissionsForCurrent(Event.getRequest(), "Invalid permissions to view page.");
        if(!_permissionCheck.checkCanCurrentUserModify(Event.getRequest()))
        {
            throw new IllegalArgumentException("Invalid permissions to view page.");
        }

        Client client = request.getPropertyValue(URLProperties.CLIENT);
        if(client == null)
        {
            throw new IllegalArgumentException("Unable to determine Client");
        }
        getValueEditor().setValue(client);
    }
}
