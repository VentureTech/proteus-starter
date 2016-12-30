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

package com.example.app.ui.company;

import com.example.app.model.company.Company;
import com.example.app.model.company.SelectedCompanyTermProvider;
import com.example.app.model.profile.MembershipTypeInfo;
import com.example.app.model.user.User;
import com.example.app.model.user.UserDAO;
import com.example.app.support.AppUtil;
import com.example.app.support.Functions;
import com.example.app.ui.Application;
import com.example.app.ui.ApplicationFunctions;
import com.example.app.ui.UIPreferences;
import com.example.app.ui.URLConfigurations;
import com.example.app.ui.profile.ProfileTypeMembershipTypeManagement;
import com.example.app.ui.text.LabelDomainLabelManagement;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;

import net.proteusframework.cms.category.CmsCategory;
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
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.TabItemDisplay;
import net.proteusframework.ui.miwt.component.composite.TabbedContainerImpl;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.users.model.Principal;
import net.proteusframework.users.model.dao.PrincipalDAO;

import static com.example.app.ui.UIText.*;
import static com.example.app.ui.URLProperties.COMPANY;
import static com.example.app.ui.URLProperties.COMPANY_PATH_INFO;
import static com.example.app.ui.company.CompanyViewerComponentLOK.*;

/**
 * Viewer for {@link Company}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 6/27/16 1:04 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.ui.company.CompanyViewerComponent",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Company Viewer")),
        @I18N(symbol = "Tab Resource Tags and Categories", l10n = @L10N("{0} Tags & Categories")),
        @I18N(symbol = "Label Categories", l10n = @L10N("Categories")),
        @I18N(symbol = "Label Tags", l10n = @L10N("Tags"))

    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Company.VIEW,
    description = "Viewer for Company",
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.Company.VIEW,
        properties = @URLProperty(name = COMPANY, type = Company.class),
        pathInfoPattern = COMPANY_PATH_INFO
    )
)
public class CompanyViewerComponent extends MIWTPageElementModelContainer
{
    private static final String SELECTED_TAB_PROP = "company-selected-tab-";

    @Autowired private PrincipalDAO _principalDAO;
    @Autowired private EntityRetriever _er;
    @Autowired private UIPreferences _uiPreferences;
    @Autowired private UserDAO _userDAO;
    @Autowired private SelectedCompanyTermProvider _terms;

    private Company _company;

    /**
     * Instantiates a new company viewer component.
     */
    public CompanyViewerComponent()
    {
        super();
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
        addClassName("company-viewer");
        setHTMLElement(HTMLElement.section);
    }

    private Company getCompany()
    {
        return _er.reattachIfNecessary(_company);
    }

    private String getSelectedTabProp()
    {
        return SELECTED_TAB_PROP + (getCompany() != null
            ? getCompany().getId() != null
            ? getCompany().getId()
            : 0 : 0);
    }

    @Override
    public void init()
    {
        super.init();

        NavigationAction backAction = CommonActions.BACK.navAction();
        backAction.configure().toPage(ApplicationFunctions.Company.MANAGEMENT)
            .withSourceComponent(this)
            .usingCurrentURLData();
        backAction.setTarget(this, "close");

        TabItemDisplay infoTID = new TabItemDisplay(INFO());
        infoTID.addClassName("company-info");
        TabItemDisplay rolesTID = new TabItemDisplay()
        {
            @Override
            public TextSource getLabel()
            {
                return MEMBERSHIPS();
            }
        };
        rolesTID.addClassName("roles-management");
        TabItemDisplay resourceTagsCatsTID = new TabItemDisplay()
        {
            @Override
            public TextSource getLabel()
            {
                return TAB_RESOURCE_TAGS_AND_CATEGORIES(RESOURCE());
            }
        };
        resourceTagsCatsTID.addClassName("resource-tags-cats-management");

        CompanyPropertyViewer propertyViewer = new CompanyPropertyViewer().configure(getCompany());
        propertyViewer.addComponentClosedListener(componentEvent -> queueClose());

        ProfileTypeMembershipTypeManagement roleMgt = new ProfileTypeMembershipTypeManagement(getCompany().getProfileType());
        roleMgt.setCoreTypeProgIds(((Supplier<List<String>>)() -> {
            List<String> coreProgIds = new ArrayList<>();
            coreProgIds.add(MembershipTypeInfo.SystemAdmin.getProgId());
            return coreProgIds;
        }).get());

        assert getCompany().getResourceCategories() != null;
        LabelDomainLabelManagement categoriesManagement = new LabelDomainLabelManagement(
            getCompany().getResourceCategories(), null){
            @Override
            protected TextSource getLabel()
            {
                return ConcatTextSource.create(RESOURCE(), LABEL_CATEGORIES()).withSpaceSeparator();
            }
        };
        categoriesManagement.addClassName("categories-management");
        assert getCompany().getResourceTags() != null;
        LabelDomainLabelManagement tagsManagement = new LabelDomainLabelManagement(
            getCompany().getResourceTags(), null){
            @Override
            protected TextSource getLabel()
            {
                return ConcatTextSource.create(RESOURCE(), LABEL_TAGS()).withSpaceSeparator();
            }
        };
        tagsManagement.addClassName("tags-management");

        Container tagsCatsManagement = of("resource-tags-cats-management", categoriesManagement, tagsManagement);

        TabbedContainerImpl tabs = new TabbedContainerImpl();
        tabs.addTab(infoTID, propertyViewer);
        tabs.addTab(rolesTID, roleMgt);
        tabs.addTab(resourceTagsCatsTID, tagsCatsManagement);
        tabs.setSelectedIndex(_uiPreferences.getStoredInteger(getSelectedTabProp()).orElse(0));
        tabs.getSelectionModel().addListSelectionListener(e ->
            _uiPreferences.setStoredInteger(getSelectedTabProp(), e.getFirstIndex()));

        TextSource viewCompany = ConcatTextSource.create(CommonButtonText.VIEW, _terms.company()).withSpaceSeparator();
        add(of("text", new Label(ConcatTextSource.create(viewCompany, _company.getName()).withSeparator(": "))
            .withHTMLElement(HTMLElement.h1)));
        add(of("actions nav-actions", new PushButton(backAction)));
        add(tabs);
    }

    @SuppressWarnings("unused") //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        final User currentUser = _userDAO.getCurrentUser();
        final Principal currentPrincipal = _principalDAO.getCurrentPrincipal();
        if(currentPrincipal == null
           || !Optional.ofNullable(currentUser).map(AppUtil::userHasAdminRole).orElse(AppUtil.userHasAdminRole(currentPrincipal)))
            throw new IllegalArgumentException(String.format("User %s does not have the correct role to view this page",
                Functions.orElseFlatMap(Optional.ofNullable(currentUser).map(User::getId),
                    () -> Optional.ofNullable(currentPrincipal).map(Principal::getId).map(Long::intValue)).orElse(0)));

        _company = request.getPropertyValue(COMPANY);

        if(_company == null)
        {
            throw new IllegalArgumentException("Unable to determine Development Provider");
        }
    }
}
