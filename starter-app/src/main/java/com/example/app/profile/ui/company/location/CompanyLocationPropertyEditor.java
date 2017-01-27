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
import com.example.app.profile.model.company.CompanyDAO;
import com.example.app.profile.model.location.Location;
import com.example.app.profile.service.ProfileUIService;
import com.example.app.profile.ui.ApplicationFunctions;
import com.example.app.profile.ui.URLConfigurations;
import com.example.app.profile.ui.URLProperties;
import com.example.app.profile.ui.location.AbstractLocationPropertyEditor;
import com.example.app.support.service.AppUtil;
import com.example.app.support.ui.Application;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import net.proteusframework.cms.category.CmsCategory;
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
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.event.Event;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.miwt.util.CommonButtonText;

import static com.example.app.profile.ui.company.location.CompanyLocationPropertyEditorLOK.COMPONENT_NAME;
import static com.example.app.support.service.Functions.opt;

/**
 * Company Location Editor
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/26/17
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.company.location.CompanyLocationPropertyEditor",
    i18n = {
        @I18N(symbol = "Component Name", l10n = @L10N("Company Location Editor"))
    }
)
@ApplicationFunction(
    applicationName = Application.NAME,
    sessionName = Application.SESSION,
    name = ApplicationFunctions.Company.Location.EDIT,
    description = "Company Location Editor",
    urlConfigDef = @URLConfigDef(
        name = URLConfigurations.Company.Location.EDIT,
        properties = @URLProperty(name = URLProperties.LOCATION, type = Location.class),
        pathInfoPattern = URLProperties.LOCATION_PATH_INFO
    )
)
public class CompanyLocationPropertyEditor extends AbstractLocationPropertyEditor
{
    @Autowired private CompanyLocationUIPermissionCheck _permissionCheck;
    @Autowired private CompanyDAO _companyDAO;
    @Autowired private ProfileUIService _uiService;

    /**
     * Instantiates a new Company location property editor.
     */
    public CompanyLocationPropertyEditor()
    {
        super();
        addCategory(CmsCategory.ClientBackend);
        setName(COMPONENT_NAME());
    }

    @Override
    public NavigationAction getSaveAction()
    {
        NavigationAction save = CommonActions.SAVE.navAction();
        save.configure().toPage(ApplicationFunctions.Company.Location.VIEW).withSourceComponent(this);
        save.setPropertyValueResolver(new CurrentURLPropertyValueResolver(){
            @Override
            public Map<String, Object> resolve(PropertyValueResolverParameter parameter)
            {
                final Map<String, Object> params = super.resolve(parameter);
                params.put(URLProperties.LOCATION, getSaved());
                return params;
            }
        });
        save.setTarget(this, "close");
        return save;
    }

    @Override
    public NavigationAction getCancelAction()
    {
        NavigationAction cancel = CommonActions.CANCEL.navAction();
        cancel.configure().toReturnPath(ApplicationFunctions.Company.Location.MANAGEMENT)
            .usingCurrentURLData()
            .withSourceComponent(this);
        cancel.setTarget(this, "close");
        return cancel;
    }

    @Override
    public UnaryOperator<Location> getPostSave()
    {
        return loc -> {
            Company company = _uiService.getSelectedCompany();
            if(!company.getLocations().contains(loc))
            {
                company.getLocations().add(loc);
                _companyDAO.saveCompany(company);
            }
            return loc;
        };
    }

    @Override
    public TextSource getPageTitle()
    {
        final boolean isNew = !opt(getValueEditor().getValue())
            .map(Location::getId)
            .filter(Objects::nonNull)
            .filter(i -> i > 0)
            .isPresent();
        final TextSource editLoc = ConcatTextSource.create(
            isNew ? CommonButtonText.NEW : CommonButtonText.EDIT,
            _terms.location()).withSpaceSeparator();
        final TextSource locName = opt(getValueEditor().getValue())
            .filter(loc -> opt(loc).map(Location::getId).filter(Objects::nonNull).filter(i -> i > 0).isPresent())
            .map(Location::getName)
            .map(name -> (TextSource)name)
            .orElse(CommonButtonText.NEW);
        return isNew
            ? editLoc
            : ConcatTextSource.create(editLoc, locName).withSeparator(": ");
    }

    @Override
    public BiConsumer<Location, NavigationAction> getPostPersist()
    {
        return (loc, action) -> action.actionPerformed(new ActionEvent(this, this, "done"));
    }

    @SuppressWarnings("unused") //Used by ApplicationFunction
    void configure(ParsedRequest request)
    {
        final String errorMessage = "Insufficient permissions to view page.";
        _permissionCheck.checkPermissionsForCurrent(Event.getRequest(), errorMessage);
        if(!_permissionCheck.checkCanCurrentUserModify(Event.getRequest()))
            throw new IllegalArgumentException(errorMessage);
        Location location = request.getPropertyValue(URLProperties.LOCATION);
        if(location != null) AppUtil.initialize(location);
        getValueEditor().setValue(location);
    }
}
