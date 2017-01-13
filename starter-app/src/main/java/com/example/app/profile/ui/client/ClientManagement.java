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
import com.example.app.profile.model.client.ClientDAO;
import com.example.app.profile.model.client.ClientLOK;
import com.example.app.profile.model.client.ClientStatus;
import com.example.app.profile.model.location.Location;
import com.example.app.profile.service.ProfileUIService;
import com.example.app.profile.service.SelectedCompanyTermProvider;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.profile.ui.UIText;
import com.example.app.profile.ui.URLProperties;
import com.example.app.support.service.AppUtil;
import com.example.app.support.ui.Application;
import com.example.app.support.ui.search.SearchUIHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.EnumSet;
import java.util.Map;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Response;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigPropertyConverter;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.management.nav.config.CurrentURLPropertyValueResolver;
import net.proteusframework.ui.management.nav.config.PropertyValueResolverParameter;
import net.proteusframework.ui.miwt.HistoryImpl;
import net.proteusframework.ui.miwt.data.SortOrder;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.search.AbstractPropertyConstraint;
import net.proteusframework.ui.search.ComboBoxConstraint;
import net.proteusframework.ui.search.LocalizedObjectKeyQLOrderBy;
import net.proteusframework.ui.search.NavigationLinkColumn;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.QLOrderByImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUIAction;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;
import net.proteusframework.ui.search.SimpleConstraint;

import static com.example.app.profile.ui.client.ClientManagementLOK.*;
import static net.proteusframework.ui.miwt.data.SortOrder.ASCENDING;
import static net.proteusframework.ui.miwt.data.SortOrder.DESCENDING;
import static net.proteusframework.ui.search.PropertyConstraint.Operator.eq;
import static net.proteusframework.ui.search.PropertyConstraint.Operator.like;

/**
 * Client Management UI
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/12/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.client.ClientManagement",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Client Management")),
        @I18N(symbol = "Column City", description = "", l10n = @L10N("City")),
        @I18N(symbol = "Column State", description = "", l10n = @L10N("State"))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Client.MANAGEMENT,
    description = "Provides a UI for managing Clients",
    configurationMethod = "configure"
)
public class ClientManagement extends MIWTPageElementModelContainer implements SearchUIOperationHandler
{
    private static final Logger _logger = LogManager.getLogger(ClientManagement.class);

    private static class AddClientPropertyValueResolver extends CurrentURLPropertyValueResolver
    {
        @Override
        public Map<String, Object> resolve(PropertyValueResolverParameter parameter)
        {
            Map<String, Object> props = super.resolve(parameter);
            props.put(URLProperties.CLIENT, URLConfigPropertyConverter.ENTITY_NEW);
            return props;
        }
    }

    @Autowired private ClientManagementPermissionCheck _permissionCheck;
    @Autowired private ProfileUIService _uiService;
    @Autowired private SelectedCompanyTermProvider _terms;
    @Autowired private SearchUIHelper _uiHelper;
    @Autowired private ClientDAO _clientDAO;

    private static final String EMAIL_PROP_PATH = String.format(
        "%s.%s.%s", Client.PRIMARY_LOCATION_PROP, Location.EMAIL_ADDRESS_PROP, "email");
    private static final String CITY_PROP_PATH = String.format(
        "%s.%s.%s", Client.PRIMARY_LOCATION_PROP, Location.ADDRESS_PROP, "city");
    private static final String STATE_PROP_PATH = String.format(
        "%s.%s.%s", Client.PRIMARY_LOCATION_PROP, Location.ADDRESS_PROP, "state");
    private static final String PHONE_NUMBER_PROP_PATH = String.format(
        "%s.%s", Client.PRIMARY_LOCATION_PROP, Location.PHONE_NUMBER_PROP);

    /**
     * Instantiates a new Client management.
     */
    public ClientManagement()
    {
        super();
        setName(COMPONENT_NAME());
        addCategory(CmsCategory.ClientBackend);
        addClassName("client-management");
    }
    @Override
    public boolean supportsOperation(SearchUIOperation operation)
    {
        return _uiHelper.isStandardSearchUIOperation(operation);
    }

    @Override
    public void handle(SearchUIOperationContext context)
    {
        Client val = context.getData();
        if(val != null)
        {
            switch (context.getOperation())
            {
                //case add: Handled by EntityAction
                //case view: Handled by NavigationLinkColumn
                case delete:
                    val.setStatus(val.getStatus() == ClientStatus.ACTIVE
                        ? ClientStatus.INACTIVE
                        : ClientStatus.ACTIVE);
                    _clientDAO.saveClient(val);
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

        SearchModelImpl searchModel = createSearchModel();
        SearchSupplierImpl searchSupplier = createSearchSupplier();

        searchSupplier.setSearchModel(searchModel);

        //Entity Actions
        final NavigationAction addAction = CommonActions.ADD.navAction();
        addAction.configure().toPage(ApplicationFunctions.Client.EDIT)
            .withSourceComponent(this);
        addAction.setPropertyValueResolver(new AddClientPropertyValueResolver());
        addAction.setEnabled(_permissionCheck.checkCanCurrentUserModify(Event.getRequest()));

        final SearchUIImpl.Options options = new SearchUIImpl.Options("Client Search")
            .addEntityAction(addAction)
            .addSearchSupplier(searchSupplier)
            .setSearchOnPageLoad(true)
            .setHistory(new HistoryImpl());

        add(new SearchUIImpl(options).addClassName("search-wrapper"));
    }

    private SearchSupplierImpl createSearchSupplier()
    {
        SearchSupplierImpl ss = new SearchSupplierImpl();
        ss.setName(UIText.SEARCH_SUPPLIER_NAME_FMT(_terms.client()));
        ss.setDescription(UIText.SEARCH_SUPPLIER_DESCRIPTION_FMT(_terms.client()));
        ss.setSearchUIOperationHandler(this);
        ss.setBuilderSupplier(() -> {
            QLBuilderImpl b = new QLBuilderImpl(Client.class, "client");
            b.appendCriteria(Client.COMPANY_PROP, eq, _uiService.getSelectedCompany());
            b.setProjection("client");
            return b;
        });

        return ss;
    }

    private SearchModelImpl createSearchModel()
    {
        SearchModelImpl sm = new SearchModelImpl();
        sm.setName("Client Search");

        addConstraints(sm);
        addResultRows(sm);

        return sm;
    }

    private static void _updateOrderBy(final QLBuilder builder, final SortOrder order, final String path)
    {
        final String dir = _formatOrder(order);
        builder.setOrderBy("nullif(%s.%s, '') %s nulls last", builder.getAlias(), path, dir);
    }

    private static void _updatePhoneOrderBy(final QLBuilder builder, final SortOrder order, final String path)
    {
        final String dir = _formatOrder(order);
        builder.setOrderBy("nullif(getphoneastrimmedtext(%s.%s), '') %s nulls last", builder.getAlias(), path, dir);
    }

    private static String _formatOrder(final SortOrder order)
    {
        return order == ASCENDING? "asc"
            : order == DESCENDING? "desc"
                : "";
    }

    private void addResultRows(SearchModelImpl sm)
    {
        final NavigationLinkColumn actions = new NavigationLinkColumn();
        actions.setName("actions");
        actions.configure()
            .usingDataColumnTableRow(URLProperties.CLIENT)
            .withSourceComponent(this);
        actions.getViewLink().configure()
            .toPage(ApplicationFunctions.Client.VIEW);
        actions.getDeleteAction().setEnabled(
            _permissionCheck.checkCanCurrentUserModify(Event.getRequest()));

        final SearchResultColumnImpl nameCol = new SearchResultColumnImpl()
            .withName("name")
            .withTableColumn(new PropertyColumn(Client.class, Client.NAME_COLUMN_PROP)
                .withColumnName(ClientLOK.CLIENT_NAME_PROP_NAME()))
            .withOrderBy(new LocalizedObjectKeyQLOrderBy(this::getLocaleContext, Client.NAME_COLUMN_PROP));

        final SearchResultColumnImpl cityCol = new SearchResultColumnImpl()
            .withName("city")
            .withTableColumn(new PropertyColumn(Client.class, CITY_PROP_PATH)
                .withColumnName(COLUMN_CITY()))
            .withOrderBy((qb, order) -> _updateOrderBy(qb
                .createJoin(QLBuilder.JoinType.LEFT, Client.PRIMARY_LOCATION_PROP, null)
                .createJoin(QLBuilder.JoinType.LEFT, Location.ADDRESS_PROP, null),
                order, "city"));

        final SearchResultColumnImpl stateCol = new SearchResultColumnImpl()
            .withName("state")
            .withTableColumn(new PropertyColumn(Client.class, STATE_PROP_PATH)
                .withColumnName(COLUMN_STATE()))
            .withOrderBy((qb, order) -> _updateOrderBy(qb
                .createJoin(QLBuilder.JoinType.LEFT, Client.PRIMARY_LOCATION_PROP, null)
                .createJoin(QLBuilder.JoinType.LEFT, Location.ADDRESS_PROP, null),
                order, "state"));

        final SearchResultColumnImpl emailCol = new SearchResultColumnImpl()
            .withName("email")
            .withTableColumn(new PropertyColumn(Client.class, EMAIL_PROP_PATH)
                .withColumnName(CommonColumnText.EMAIL))
            .withOrderBy((qb, order) -> _updateOrderBy(qb
                .createJoin(QLBuilder.JoinType.LEFT, Client.PRIMARY_LOCATION_PROP, null)
                .createJoin(QLBuilder.JoinType.LEFT, Location.EMAIL_ADDRESS_PROP, null),
                order, "email"));

        final SearchResultColumnImpl phoneCol = new SearchResultColumnImpl()
            .withName("phone")
            .withTableColumn(new PropertyColumn(Client.class, PHONE_NUMBER_PROP_PATH)
                .withColumnName(CommonColumnText.PHONE_NUMBER))
            .withOrderBy((qb, order) -> _updatePhoneOrderBy(qb
                .createJoin(QLBuilder.JoinType.LEFT, Client.PRIMARY_LOCATION_PROP, null),
                order, Location.PHONE_NUMBER_PROP));

        final SearchResultColumnImpl statusCol = new SearchResultColumnImpl()
            .withName("status")
            .withTableColumn(new PropertyColumn(Client.class, Client.STATUS_PROP)
                .withColumnName(CommonColumnText.STATUS))
            .withOrderBy(new QLOrderByImpl(Client.STATUS_PROP));

        sm
            .withResultColumn(actions)
            .withResultColumn(nameCol)
            .withResultColumn(cityCol)
            .withResultColumn(stateCol)
            .withResultColumn(emailCol)
            .withResultColumn(phoneCol)
            .withResultColumn(statusCol);
    }

    @SuppressWarnings("MethodMayBeStatic")
    private void addConstraints(SearchModelImpl sm)
    {
        final AbstractPropertyConstraint nameConstraint = new SimpleConstraint("name")
            .withLabel(ClientLOK.CLIENT_NAME_PROP_NAME())
            .withProperty(Client.NAME_COLUMN_PROP)
            .withOperator(like)
            .withHTMLClass("name first");

        final AbstractPropertyConstraint emailConstraint = new SimpleConstraint("email")
            .withLabel(CommonColumnText.EMAIL)
            .withProperty(EMAIL_PROP_PATH)
            .withOperator(like)
            .withHTMLClass("email");

        final AbstractPropertyConstraint statusConstraint = new ComboBoxConstraint(
            AppUtil.nullFirst(EnumSet.allOf(ClientStatus.class)),
            ClientStatus.ACTIVE, CommonButtonText.ANY)
            .withLabel(CommonColumnText.STATUS)
            .withName("status")
            .withProperty(Client.STATUS_PROP)
            .withHTMLClass("status last");

        sm
            .withConstraint(nameConstraint)
            .withConstraint(emailConstraint)
            .withConstraint(statusConstraint);
    }

    @SuppressWarnings("unused") //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        _permissionCheck.checkPermissionsForCurrent(Event.getRequest(), "Invalid permissions to view page.");
    }
}
