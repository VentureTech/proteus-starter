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
import com.example.app.profile.model.location.Location;
import com.example.app.profile.service.SelectedCompanyTermProvider;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.profile.ui.URLProperties;
import com.example.app.support.ui.Application;
import com.google.common.base.Supplier;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Optional;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Response;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigPropertyConverter;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.HistoryImpl;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.Table;
import net.proteusframework.ui.miwt.component.TableCellRenderer;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.search.NavigationLinkColumn;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.QLOrderByImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUI;
import net.proteusframework.ui.search.SearchUIAction;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;
import net.proteusframework.ui.search.SimpleConstraint;
import net.proteusframework.users.model.PhoneNumber;

import static com.example.app.profile.model.company.CompanyStatus.Active;
import static com.example.app.profile.model.company.CompanyStatus.Inactive;
import static com.example.app.profile.ui.UIText.*;
import static com.example.app.profile.ui.company.CompanyManagementLOK.*;
import static com.example.app.profile.ui.company.CompanyValueEditorLOK.LABEL_WEBSITE;
import static net.proteusframework.core.locale.TextSources.EMPTY;

/**
 * UI for managing {@link Company}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 6/27/16 10:19 AM
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.company.CompanyManagement",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Company Management")),
        @I18N(symbol = "Activate Button", l10n = @L10N("Activate")),
        @I18N(symbol = "Deactivate Button", l10n = @L10N("Deactivate")),
        @I18N(symbol = "Message Are You Sure Deactivate", l10n = @L10N("Are you sure you want to {0} this {1}?"))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Company.MANAGEMENT,
    description = "Company Management"
)
public class CompanyManagement extends MIWTPageElementModelContainer implements SearchUIOperationHandler
{
    private class CustomNavLinkColumn extends NavigationLinkColumn
    {
        @Override
        public TableCellRenderer getTableCellRenderer(SearchUI searchUI)
        {
            final SearchUIOperationHandler handler = searchUI.getSearchSupplier().getSearchUIOperationHandler();
            getDeleteAction().setActionListener(ev -> handler
                .handle(new SearchUIOperationContext(searchUI, SearchUIOperation.delete,
                    SearchUIOperationContext.DataContext.lead_selection)));
            PushButton deleteButton = new PushButton(getDeleteAction());
            final Container con = new NavLinkColumnTableCellRenderer(deleteButton);
            con.getDisplay().appendDisplayClass("actions");
            con.add(getViewLink());
            con.add(deleteButton);
            return con;
        }
    }

    private class NavLinkColumnTableCellRenderer extends Container
    {
        private final PushButton _deleteButton;

        public NavLinkColumnTableCellRenderer(PushButton deleteButton)
        {
            _deleteButton = deleteButton;
        }

        @Override
        public Component getTableCellRendererComponent(Table table, Object value, boolean isSelected, boolean hasFocus,
            int row,
            int column)
        {
            Company val = (Company) value;
            _deleteButton.setLabel(val.getStatus() == Active
                ? DEACTIVATE_BUTTON()
                : ACTIVATE_BUTTON());
            _deleteButton.getButtonDisplay().setConfirmText(val.getStatus() == Active
                ? MESSAGE_ARE_YOU_SURE_DEACTIVATE(DEACTIVATE_BUTTON(), _terms.company())
                : null);
            if(val.getStatus() == Active)
            {
                _deleteButton.addClassName("delete");
            }
            else
            {
                _deleteButton.removeClassName("delete");
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    @Autowired private CompanyDAO _companyDAO;
    @Autowired private SelectedCompanyTermProvider _terms;
    @Autowired private CompanyUIPermissionCheck _permissionCheck;

    /**
     * Instantiates a new company management.
     */
    public CompanyManagement()
    {
        super();

        setName(CompanyManagementLOK.COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);

        addClassName("company-management");
        setHTMLElement(HTMLElement.section);
    }

    @Override
    public boolean supportsOperation(SearchUIOperation operation)
    {
        switch(operation)
        {
            case add:
            case view:
            case delete:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void handle(SearchUIOperationContext context)
    {
        Company val = context.getData();
        if(val != null)
        {
            switch (context.getOperation())
            {
                //case add: Handled by EntityAction
//                case view: Handled by NavigationLinkColumn
                case delete:
                    val.setStatus(val.getStatus() == Active
                        ? Inactive
                        : Active);
                    _companyDAO.saveCompany(val);
                    context.getSearchUI().doAction(SearchUIAction.search);
                    Response response = Event.getResponse();
                    response.redirect(response.createURL());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void init()
    {
        super.init();

        final SearchSupplierImpl searchSupplier = getSearchSupplier();
        searchSupplier.setSearchUIOperationHandler(this);
        searchSupplier.setBuilderSupplier(getQLBuilderSupplier());
        SearchUIImpl.Options options = new SearchUIImpl.Options("Company Index");
        options.setSearchOnPageLoad(true);
        options.setRowExtractor((obj) -> ((Object[])obj)[0]);

        NavigationAction addAction = CommonActions.ADD.navAction();
        addAction.configure().toPage(ApplicationFunctions.Company.EDIT);
        addAction.setPropertyValueResolver(parameter -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put(URLProperties.COMPANY, URLConfigPropertyConverter.ENTITY_NEW);
            return map;
        });

        options.getEntityActions().add(addAction);

        options.addSearchSupplier(searchSupplier);
        options.setHistory(new HistoryImpl());

        SearchUIImpl searchUI = new SearchUIImpl(options);

        add(of("text", new Label(_terms.companies()).withHTMLElement(HTMLElement.h1)));
        add(of("search-wrapper company-search", searchUI));
    }

    private SearchSupplierImpl getSearchSupplier()
    {
        SearchModelImpl searchModel = getSearchModel();

        SearchSupplierImpl searchSupplier = new SearchSupplierImpl();
        searchSupplier.setName(SEARCH_SUPPLIER_NAME_FMT(_terms.company()));
        searchSupplier.setDescription(SEARCH_SUPPLIER_DESCRIPTION_FMT(_terms.company()));
        searchSupplier.setSearchModel(searchModel);
        return searchSupplier;
    }

    private SearchModelImpl getSearchModel()
    {
        SearchModelImpl searchModel = new SearchModelImpl();
        searchModel.setName("Company Search");
        searchModel.setDisplayName(SEARCH_MODEL_NAME_FMT(_terms.company()));

        addConstraints(searchModel);
        addColumns(searchModel);

        return searchModel;
    }

    private static void addConstraints(SearchModelImpl searchModel)
    {
        searchModel.getConstraints().add(new SimpleConstraint("name").withLabel(CommonColumnText.NAME)
            .withProperty(Company.NAME_COLUMN_PROP)
            .withOperator(PropertyConstraint.Operator.like));

        searchModel.getConstraints().add(new SimpleConstraint("website").withLabel(LABEL_WEBSITE())
            .withProperty(Company.WEBSITE_LINK_PROP)
            .withOperator(PropertyConstraint.Operator.like));
    }

    private void addColumns(SearchModelImpl searchModel)
    {
        final NavigationLinkColumn actionColumn = new CustomNavLinkColumn();
        actionColumn.configure()
            .usingDataColumnTableRow(URLProperties.COMPANY)
            .withSourceComponent(this);
        actionColumn.getViewLink().configure()
            .toPage(ApplicationFunctions.Company.VIEW);
        searchModel.getResultColumns().add(actionColumn);

        SearchResultColumnImpl nameColumn;
        searchModel.getResultColumns().add(nameColumn = new SearchResultColumnImpl()
            .withName("name")
            .withTableColumn(new PropertyColumn(Company.class, Company.NAME_COLUMN_PROP)
                .withColumnName(CommonColumnText.NAME))
            .withOrderBy(new QLOrderByImpl("companyName")));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("address")
            .withTableColumn(new FixedValueColumn().withColumnName(CommonColumnText.ADDRESS))
            .withTableCellRenderer(new CustomCellRenderer(EMPTY, input -> {
                Company company = (Company) input;
                return Optional.ofNullable(company)
                    .map(Company::getPrimaryLocation)
                    .map(Location::getAddress)
                    .map(a -> a.getAddressLine(1)).orElse("");
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("website")
            .withTableColumn(new FixedValueColumn().withColumnName(LABEL_WEBSITE()))
            .withTableCellRenderer(new CustomCellRenderer(EMPTY, input -> {
                Company company = (Company) input;
                return Optional.ofNullable(company)
                    .map(Company::getWebsiteLink)
                    .orElse("");
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("phone")
            .withTableColumn(new FixedValueColumn().withColumnName(CommonColumnText.PHONE))
            .withTableCellRenderer(new CustomCellRenderer(EMPTY, input -> {
                Company company = (Company)input;
                return Optional.ofNullable(company)
                    .map(Company::getPrimaryLocation)
                    .map(Location::getPhoneNumber)
                    .map(PhoneNumber::toExternalForm).orElse("");
            })));

        searchModel.setDefaultSortColumn(nameColumn);
    }

    private static Supplier<QLBuilder> getQLBuilderSupplier()
    {
        return () -> {
            QLBuilder builder = new QLBuilderImpl(Company.class, "ce");
            builder.setProjection("distinct " + builder.getAlias() + ", getText(" + builder.getAlias() + '.' + Company
                .NAME_COLUMN_PROP + ") as companyName");
            return builder;
        };
    }

    @SuppressWarnings("unused") //Used by ApplicationFunction
    void configure(ParsedRequest parsedRequest)
    {
        _permissionCheck.checkPermissionsForCurrent("You do not have the correct role to view this page");
    }
}
