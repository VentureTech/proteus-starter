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

package com.example.app.login.social.ui;

import com.example.app.login.social.service.SocialLoginProvider;
import com.example.app.login.social.service.SocialLoginService;
import com.example.app.support.service.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.i2rd.cms.controller.PreferencesUtil;
import com.i2rd.cms.miwt.LinkSelector;
import com.i2rd.cms.scripts.impl.ScriptableRedirectType;
import com.i2rd.lib.Library;
import com.i2rd.lib.LibraryConfiguration;
import com.i2rd.lib.LibraryDAO;
import com.i2rd.lib.miwt.LibraryConfigurationForm;

import net.proteusframework.cms.component.editor.ContentBuilderBasedEditor;
import net.proteusframework.cms.component.editor.EditorUI;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Link;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.editor.BooleanValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ComboBoxValueEditor;
import net.proteusframework.ui.miwt.component.composite.editor.ListComponentValueEditor;
import net.proteusframework.ui.miwt.util.CommonButtonText;

import static com.example.app.login.social.ui.SocialLoginEditorLOK.*;

/**
 * Editor for {@link SocialLoginElement}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/19/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.login.social.ui.SocialLoginEditor",
    i18n = {
        @I18N(symbol = "Label Login Service", l10n = @L10N("Login Service")),
        @I18N(symbol = "Label Providers", l10n = @L10N("Providers")),
        @I18N(symbol = "Label Mode", l10n = @L10N("Mode")),
        @I18N(symbol = "Label Landing Page", l10n = @L10N("Landing Page")),
        @I18N(symbol = "Error Please Select A Landing Page", l10n = @L10N("Please Select a Landing Page")),
        @I18N(symbol = "Label Override Dynamic Return", l10n = @L10N("Override Dynamic Return Page")),
        @I18N(symbol = "Label Scripted Redirect", l10n = @L10N("Scripted Redirect"))
    }
)
@Configurable
public class SocialLoginEditor extends ContentBuilderBasedEditor<SocialLoginContentBuilder>
{
    private static final long serialVersionUID = -8012327237005466314L;

    @Autowired private List<SocialLoginService> _loginServices;
    @Autowired private LibraryDAO _libraryDAO;
    @Autowired private PreferencesUtil _preferencesUtil;

    private ComboBoxValueEditor<SocialLoginService> _loginServiceSelector;
    private ListComponentValueEditor<SocialLoginProvider> _providerSelector;
    private ComboBoxValueEditor<SocialLoginMode> _modeSelector;
    private LinkSelector _landingPageSelector;
    private BooleanValueEditor _overrideDynamicReturn;
    private LibraryConfigurationForm<ScriptableRedirectType> _scriptedRedirectSelector = null;

    /**
     * Constructor.
     */
    public SocialLoginEditor()
    {
        super(SocialLoginContentBuilder.class);
    }

    @Override
    public void createUI(EditorUI editorUI)
    {
        super.createUI(editorUI);

        //Existing Data
        SocialLoginService selectedService = Optional.ofNullable(getBuilder().getLoginServiceIdentifier())
            .flatMap(pid -> _loginServices.stream().filter(ls -> Objects.equals(ls.getServiceIdentifier(), pid)).findFirst())
            .orElse(null);
        List<SocialLoginProvider> availableProviders = selectedService != null
            ? selectedService.getSupportedProviders()
            : new ArrayList<>();
        List<SocialLoginProvider> selectedProviders = getBuilder().getProviderProgrammaticNames().stream()
            //Map the provider programmatic identifier to a provider from the available providers
            .map(pid -> availableProviders.stream().filter(slp -> Objects.equals(slp.getProgrammaticName(), pid)).findFirst())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
        Link landingPage = getBuilder().getLandingPage();
        boolean overrideDynamicReturn = getBuilder().isOverrideDynamicReturnPage();
        final List<Library<ScriptableRedirectType>> availableScriptedRedirects =
            _libraryDAO.getAvailableLibraries(_preferencesUtil.getSelectedSite(), ScriptableRedirectType.class);
        LibraryConfiguration<ScriptableRedirectType> scriptableRedirect = getBuilder().getScriptedRedirectInstance() > 0
            ? _libraryDAO.getLibraryConfiguration(getBuilder().getScriptedRedirectInstance())
            : null;

        //Construct UI
        _loginServiceSelector = new ComboBoxValueEditor<>(
            LABEL_LOGIN_SERVICE(), AppUtil.nullFirst(_loginServices), selectedService);
        _loginServiceSelector.setRequiredValueValidator();
        _loginServiceSelector.setCellRenderer(new CustomCellRenderer(CommonButtonText.PLEASE_SELECT));
        _loginServiceSelector.addClassName("login-service-selector");
        _loginServiceSelector.getValueComponent().addActionListener(ev -> {
            SocialLoginService ls = _loginServiceSelector.getUIValue();

            List<SocialLoginProvider> providers = ls != null ? ls.getSupportedProviders() : new ArrayList<>();
            _providerSelector.setOptions(providers);
            //If there were any providers selected, leave the ones selected that are supported by the newly selected service.
            Optional.ofNullable(_providerSelector.getUIValue()).ifPresent(uiVal -> _providerSelector.setUIValue(uiVal.stream()
                .filter(sel -> providers.stream()
                    .anyMatch(p -> Objects.equals(p.getProgrammaticName(), sel.getProgrammaticName())))
                .collect(Collectors.toList())));
        });

        _providerSelector = new ListComponentValueEditor<>(LABEL_PROVIDERS(), availableProviders, selectedProviders);
        _providerSelector.setRequiredValueValidator();
        _providerSelector.addClassName("provider-selector");

        _modeSelector = new ComboBoxValueEditor<>(
            LABEL_MODE(), AppUtil.nullFirst(EnumSet.allOf(SocialLoginMode.class)), getBuilder().getMode());
        _modeSelector.setRequiredValueValidator();
        _modeSelector.addClassName("mode-selector");

        _landingPageSelector = new LinkSelector();
        _landingPageSelector.setSelection(landingPage);

        _overrideDynamicReturn = new BooleanValueEditor(LABEL_OVERRIDE_DYNAMIC_RETURN(), overrideDynamicReturn);
        _overrideDynamicReturn.addClassName("override-dynamic-return");

        if(!availableScriptedRedirects.isEmpty())
        {
            _scriptedRedirectSelector = LibraryConfigurationForm.create(ScriptableRedirectType.MODEL_NAME);
            _scriptedRedirectSelector.setLibraryConfiguration(scriptableRedirect);
        }

        //Add UI Components to the Editor
        editorUI.addComponent(_loginServiceSelector);
        editorUI.addComponent(_providerSelector);
        editorUI.addComponent(_modeSelector);
        editorUI.addComponent(Container.of("landing-page", LABEL_LANDING_PAGE(), _landingPageSelector));
        editorUI.addComponent(_overrideDynamicReturn);
        if(_scriptedRedirectSelector != null)
        {
            editorUI.addComponent(Container.of("scriptable-redirect", LABEL_SCRIPTED_REDIRECT(), _scriptedRedirectSelector));
        }
    }

    @Override
    protected void _updateBuilder()
    {
        getBuilder().setLoginService(Optional.ofNullable(_loginServiceSelector.commitValue())
            .orElse(_loginServices.get(0)));
        getBuilder().setProviders(Optional.ofNullable(_providerSelector.commitValue())
            .orElse(new ArrayList<>()));
        getBuilder().setMode(Optional.ofNullable(_modeSelector.commitValue())
            .orElse(SocialLoginMode.Login));
        getBuilder().setLandingPage(_landingPageSelector.commitValue());
        getBuilder().setOverrideDynamicReturnPage(_overrideDynamicReturn.commitValue());
        if(_scriptedRedirectSelector != null)
        {
            final LibraryConfiguration<ScriptableRedirectType> scriptedRedirect =
                _scriptedRedirectSelector.getSelectedLibraryConfiguration();
            if(scriptedRedirect != null)
            {
                _libraryDAO.saveLibraryConfiguration(scriptedRedirect);
                getBuilder().setScriptedRedirectInstance(scriptedRedirect.getId());
            }
            else
            {
                getBuilder().setScriptedRedirectInstance(0L);
            }
        }
        else
        {
            getBuilder().setScriptedRedirectInstance(0L);
        }
    }
}
