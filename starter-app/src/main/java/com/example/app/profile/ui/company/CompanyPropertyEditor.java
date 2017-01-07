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

package com.example.app.profile.ui.company;

import com.example.app.profile.model.company.Company;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.profile.ui.URLConfigurations;
import com.example.app.support.ui.Application;

import java.util.function.Consumer;

import com.i2rd.cms.HostnameDestination;
import com.i2rd.cms.PageTitleAffixOption;
import com.i2rd.cms.SiteSSLOption;

import net.proteusframework.cms.CmsSite;
import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.locale.LocalizedObjectKey;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.URLConfigDef;
import net.proteusframework.ui.management.URLProperty;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyEditor;
import net.proteusframework.users.model.AuthenticationDomain;

import static com.example.app.profile.ui.URLProperties.COMPANY;
import static com.example.app.profile.ui.URLProperties.COMPANY_PATH_INFO;
import static com.example.app.profile.ui.company.CompanyPropertyEditorLOK.COMPONENT_NAME;

/**
 * {@link PropertyEditor} implementation for {@link Company}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 6/27/16 1:04 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.company.CompanyPropertyEditor",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Company Editor")),
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Company.EDIT,
    description = "Editor for Company",
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.Company.EDIT,
        properties = @URLProperty(name = COMPANY, type = Company.class),
        pathInfoPattern = COMPANY_PATH_INFO
    )
)
public class CompanyPropertyEditor extends AbstractCompanyPropertyEditor
{
    /**
     * Instantiates a new company property editor.
     */
    public CompanyPropertyEditor()
    {
        super();
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
    }

    @Override
    protected Consumer<Company> getHostnameSetup()
    {
        return company -> {
            if(_companyDAO.isTransient(company) || company.getHostname().getDomain() == null)
            {
                final AuthenticationDomain authDomain = new AuthenticationDomain();
                authDomain.setDomainName(company.getHostname().getName());
                company.getHostname().setDomain(authDomain);
                company.getHostname().setDestination(HostnameDestination.welcome_page);
                company.getHostname().setPageTitleAffix(LocalizedObjectKey.copyLocalizedObjectKey(
                    getLocaleContext().getLocaleSource(), company.getName()));
                company.getHostname().setPageTitleAffixOption(PageTitleAffixOption.Prefix);
                final CmsSite site = _appUtil.getSite();
                company.getHostname().setSite(site);
                company.getHostname().setSslOption(SiteSSLOption.no_influence);
                company.getHostname().setWelcomePage(site.getDefaultHostname().getWelcomePage());
            }
        };
    }

    @Override
    protected Runnable getPostSave()
    {
        return () -> {};
    }

    @Override
    protected Consumer<NavigationAction> getPostPersist()
    {
        return this::promptCopyResourceCatsAndTags;
    }
}
