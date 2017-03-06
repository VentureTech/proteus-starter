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

package com.example.app.profile.ui.company.location;

import com.example.app.profile.model.company.Company;
import com.example.app.profile.model.location.Location;
import com.example.app.profile.model.location.LocationDAO;
import com.example.app.profile.service.ProfileUIService;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.profile.ui.URLProperties;
import com.example.app.profile.ui.location.AbstractLocationManagement;
import com.example.app.support.ui.Application;
import com.example.app.support.ui.search.SearchUIHelper;
import com.example.app.support.ui.search.ToggleDeleteNavigationColumn;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import net.proteusframework.cms.category.CmsCategory;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.internet.http.Response;
import net.proteusframework.ui.management.ApplicationFunction;
import net.proteusframework.ui.management.ParsedRequest;
import net.proteusframework.ui.management.URLConfigPropertyConverter;
import net.proteusframework.ui.management.nav.NavigationAction;
import net.proteusframework.ui.miwt.Action;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.search.JoinedQLBuilder;
import net.proteusframework.ui.search.LocalizedObjectKeyQLOrderBy;
import net.proteusframework.ui.search.NavigationLinkColumn;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.QLOrderBy;
import net.proteusframework.ui.search.SearchUIAction;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;

import static com.example.app.profile.model.location.LocationStatus.ACTIVE;
import static com.example.app.profile.model.location.LocationStatus.INACTIVE;
import static com.example.app.profile.ui.company.location.CompanyLocationManagementLOK.*;

/**
 * Company Location Management
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/26/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.company.location.CompanyLocationManagement",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Company Location Management")),
        @I18N(symbol = "Activate Button", l10n = @L10N("Activate")),
        @I18N(symbol = "Deactivate Button", l10n = @L10N("Deactivate"))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Company.Location.MANAGEMENT,
    description = "Company Location Management"
)
public class CompanyLocationManagement extends AbstractLocationManagement
{
    @Autowired private ProfileUIService _uiService;
    @Autowired private SearchUIHelper _uiHelper;
    @Autowired private CompanyLocationUIPermissionCheck _permissionCheck;
    @Autowired private LocationDAO _locationDAO;

    /**
     * Instantiates a new Company location management.
     */
    public CompanyLocationManagement()
    {
        super();
        addCategory(CmsCategory.ClientBackend);
        setName(COMPONENT_NAME());
    }

    @Override
    public boolean supportsOperation(SearchUIOperation operation)
    {
        return _uiHelper.isStandardSearchUIOperation(operation);
    }

    @Override
    public void handle(SearchUIOperationContext<Location> context)
    {
        Location val = context.getData();
        if(val != null)
        {
            switch(context.getOperation())
            {
                //case add: EntityAction
                //case view: NavigationLinkColumn
                case delete:
                    val.setStatus(val.getStatus() == ACTIVE
                        ? INACTIVE
                        : ACTIVE);
                    _locationDAO.saveLocation(val);
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
    protected QLBuilder<Location> createQLBuilder()
    {
        QLBuilder<Company> builder = new QLBuilderImpl<>(Company.class, "c");
        builder.appendCriteria("id", PropertyConstraint.Operator.eq, _uiService.getSelectedCompany().getId());
        JoinedQLBuilder<Location> locationJoin = builder.createInnerJoin(Company.LOCATIONS_PROP);
        locationJoin.setProjection("distinct " + locationJoin.getAlias());
        return locationJoin;
    }

    @Override
    protected List<Action> createEntityActions()
    {
        NavigationAction addAction = CommonActions.ADD.navAction();
        addAction.configure().toPage(ApplicationFunctions.Company.Location.EDIT);
        addAction.setPropertyValueResolver(parameter ->
            Collections.singletonMap(URLProperties.LOCATION, URLConfigPropertyConverter.ENTITY_NEW));
        return Collections.singletonList(addAction);
    }

    @Override
    protected NavigationLinkColumn createActionColumn()
    {
        final ToggleDeleteNavigationColumn navigationColumn = new ToggleDeleteNavigationColumn()
            .withToggleHtmlClassExtractor(obj -> {
                Location location = (Location)obj;
                if(location.getStatus() != INACTIVE)
                    return "delete status-inactive";
                else if(location.getStatus() == ACTIVE)
                    return "status-active";
                else
                    return "status-pending";
            }).withToggleTextExtractor(obj -> {
                Location location = (Location)obj;
                if(location.getStatus() != INACTIVE)
                    return DEACTIVATE_BUTTON();
                else return ACTIVATE_BUTTON();
            }).withToggleConfirmTextExtractor(obj -> {
                Location location = (Location)obj;
                if(location.getStatus() != INACTIVE)
                    return CommonButtonText.DELETE_CONFIRM;
                else return null;
            });
        final boolean canModify = _permissionCheck.checkCanCurrentUserModify(Event.getRequest());
        navigationColumn.setIncludeEdit(canModify);
        navigationColumn.setIncludeDelete(canModify);
        navigationColumn.configure()
            .usingDataColumnTableRow(URLProperties.LOCATION)
            .withSourceComponent(this);
        navigationColumn.getViewLink().configure()
            .toPage(ApplicationFunctions.Company.Location.VIEW);
        return navigationColumn;
    }

    @Override
    protected QLOrderBy createNameOrderBy()
    {
        return new LocalizedObjectKeyQLOrderBy(this, Location.NAME_COLUMN_PROP);
    }

    @Override
    protected TextSource getPageTitle()
    {
        return _terms.locations();
    }

    @SuppressWarnings("unused") //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        _permissionCheck.checkPermissionsForCurrent(Event.getRequest(), "Insufficient permissions to view page.");
    }
}
