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
import com.example.app.profile.model.company.CompanyDAO;
import com.example.app.profile.model.membership.MembershipTypeInfo;
import com.example.app.profile.model.terminology.ProfileTerms;
import com.example.app.profile.service.SelectedCompanyTermProvider;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.profile.ui.URLConfigurations;
import com.example.app.profile.ui.membership.ProfileTypeMembershipTypeManagement;
import com.example.app.profile.ui.terminology.ProfileTermsEditor;
import com.example.app.profile.ui.terminology.ProfileTermsViewer;
import com.example.app.support.ui.Application;
import com.example.app.support.ui.UIPreferences;
import com.example.app.support.ui.text.LabelDomainLabelManagement;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.proteusframework.cms.category.CmsCategory;
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
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.TabItemDisplay;
import net.proteusframework.ui.miwt.component.composite.TabbedContainerImpl;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyEditor;
import net.proteusframework.ui.miwt.component.composite.editor.PropertyViewer;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;

import static com.example.app.profile.ui.UIText.*;
import static com.example.app.profile.ui.URLProperties.COMPANY;
import static com.example.app.profile.ui.URLProperties.COMPANY_PATH_INFO;
import static com.example.app.profile.ui.company.CompanyViewerComponentLOK.*;

/**
 * Viewer for {@link Company}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 6/27/16 1:04 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.company.CompanyViewerComponent",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Company Viewer")),
        @I18N(symbol = "Tab Terminology", l10n = @L10N("Terminology")),
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

    @Autowired private EntityRetriever _er;
    @Autowired private UIPreferences _uiPreferences;
    @Autowired private SelectedCompanyTermProvider _terms;
    @Autowired private CompanyUIPermissionCheck _permissionCheck;
    @Autowired private CompanyDAO _companyDAO;

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
        TabItemDisplay termTID = new TabItemDisplay(TAB_TERMINOLOGY());
        termTID.addClassName("terminology");
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
        tabs.addTab(termTID, createTerminologyUI());
        tabs.addTab(rolesTID, roleMgt);
        tabs.addTab(resourceTagsCatsTID, tagsCatsManagement);
        tabs.setSelectedIndex(_uiPreferences.getStoredInteger(getSelectedTabProp()).orElse(0));
        tabs.getSelectionModel().addListSelectionListener(e ->
            _uiPreferences.setStoredInteger(getSelectedTabProp(), e.getFirstIndex()));

        TextSource viewCompany = ConcatTextSource.create(CommonButtonText.VIEW, _terms.company()).withSpaceSeparator();
        add(new Label(ConcatTextSource.create(viewCompany, _company.getName()).withSeparator(": "))
            .withHTMLElement(HTMLElement.h1).addClassName("page-header"));
        add(of("actions nav-actions", new PushButton(backAction)));
        add(tabs);
    }

    private Component createTerminologyUI()
    {
        Container con = new Container();
        con.addClassName("terminology-con");
        PropertyViewer pv = new PropertyViewer(new ProfileTermsViewer(getCompany(), getCompany().getProfileTerms()));
        con.add(pv);
        ReflectiveAction editAction = CommonActions.EDIT.defaultAction();
        pv.setPersistenceActions(editAction);
        editAction.setActionListener(ev -> {
            con.removeAllComponents();
            PropertyEditor<ProfileTerms> pe = new PropertyEditor<>();
            ReflectiveAction saveAction = CommonActions.SAVE.defaultAction();
            ReflectiveAction cancelAction = CommonActions.CANCEL.defaultAction();
            pe.setPersistenceActions(saveAction, cancelAction);
            ProfileTermsEditor editor = new ProfileTermsEditor(getCompany());
            editor.setValue(getCompany().getProfileTerms());
            pe.setValueEditor(editor);
            con.add(pe);

            ActionListener cancelListener = ev1 -> {
                con.removeAllComponents();
                PropertyViewer pv2 = new PropertyViewer(new ProfileTermsViewer(getCompany(), getCompany().getProfileTerms()));
                con.add(pv2);
                pv2.setPersistenceActions(editAction);
            };
            cancelAction.setActionListener(cancelListener);

            saveAction.setActionListener(ev1 -> {
                if(pe.persist(input -> {
                    assert input != null;
                    _companyDAO.mergeProfileTerms(input);
                    return true;
                }))
                {
                    cancelListener.actionPerformed(ev1);
                }
            });
        });
        return con;
    }

    @SuppressWarnings("unused") //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        _permissionCheck.checkPermissionsForCurrent(Event.getRequest(), "You do not have the correct role to view this page");

        _company = request.getPropertyValue(COMPANY);

        if(_company == null)
        {
            throw new IllegalArgumentException("Unable to determine Development Provider");
        }
    }
}
