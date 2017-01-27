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

package com.example.app.profile.ui.location;

import com.example.app.profile.model.location.Location;
import com.example.app.profile.model.location.LocationStatus;
import com.example.app.profile.service.SelectedCompanyTermProvider;
import com.example.app.support.service.AppUtil;
import com.google.common.base.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import com.i2rd.cms.component.miwt.impl.MIWTPageElementModelContainer;

import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.miwt.Action;
import net.proteusframework.ui.miwt.HistoryImpl;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.miwt.util.CommonColumnText;
import net.proteusframework.ui.search.ComboBoxConstraint;
import net.proteusframework.ui.search.NavigationLinkColumn;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLOrderBy;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperationHandler;
import net.proteusframework.ui.search.SimpleConstraint;
import net.proteusframework.users.model.EmailAddress;
import net.proteusframework.users.model.PhoneNumber;

import static com.example.app.profile.ui.UIText.*;
import static net.proteusframework.core.locale.TextSources.EMPTY;

/**
 * Location Management
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1 /25/17
 */
@Configurable
public abstract class AbstractLocationManagement extends MIWTPageElementModelContainer implements SearchUIOperationHandler
{
    @Autowired protected SelectedCompanyTermProvider _terms;

    /**
     * Instantiates a new Abstract location management.
     */
    public AbstractLocationManagement()
    {
        super();
        addClassName("location-management");
        setHTMLElement(HTMLElement.section);
    }

    /**
     * Create ql builder of Location
     *
     * @return the ql builder
     */
    protected abstract QLBuilder createQLBuilder();

    /**
     * Create row extractor function.
     *
     * @return the function
     */
    protected abstract Function<Object, Object> createRowExtractor();

    /**
     * Create entity actions list.
     *
     * @return the list
     */
    protected abstract List<Action> createEntityActions();

    /**
     * Create action column navigation link column.
     *
     * @return the navigation link column
     */
    protected abstract NavigationLinkColumn createActionColumn();

    /**
     * Create name order by ql order by.
     *
     * @return the ql order by
     */
    protected abstract QLOrderBy createNameOrderBy();

    /**
     * Gets page title.
     *
     * @return the page title
     */
    protected abstract TextSource getPageTitle();

    @Override
    public void init()
    {
        super.init();

        final SearchSupplierImpl searchSupplier = createSearchSupplier();
        SearchUIImpl.Options options = new SearchUIImpl.Options("Location Index");
        options.setSearchOnPageLoad(true);
        options.setRowExtractor(createRowExtractor());

        createEntityActions().forEach(ea -> options.getEntityActions().add(ea));
        options.addSearchSupplier(searchSupplier);
        options.setHistory(new HistoryImpl());

        SearchUIImpl searchUI = new SearchUIImpl(options);

        add(of("text", new Label(getPageTitle()).withHTMLElement(HTMLElement.h1)));
        add(of("search-wrapper location-search", searchUI));
    }

    /**
     * Create search supplier search supplier.
     *
     * @return the search supplier
     */
    protected SearchSupplierImpl createSearchSupplier()
    {
        SearchModelImpl searchModel = createSearchModel();

        SearchSupplierImpl searchSupplier = new SearchSupplierImpl();
        searchSupplier.setName(SEARCH_SUPPLIER_NAME_FMT(_terms.location()));
        searchSupplier.setDescription(SEARCH_SUPPLIER_DESCRIPTION_FMT(_terms.location()));
        searchSupplier.setSearchModel(searchModel);
        searchSupplier.setSearchUIOperationHandler(this);
        searchSupplier.setBuilderSupplier(this::createQLBuilder);
        return searchSupplier;
    }

    /**
     * Create search model search model.
     *
     * @return the search model
     */
    protected SearchModelImpl createSearchModel()
    {
        SearchModelImpl searchModel = new SearchModelImpl();
        searchModel.setName("Location Search");
        searchModel.setDisplayName(SEARCH_MODEL_NAME_FMT(_terms.location()));

        addConstraints(searchModel);
        addColumns(searchModel);
        return searchModel;
    }

    /**
     * Add columns.
     *
     * @param searchModel the search model
     */
    protected void addColumns(SearchModelImpl searchModel)
    {
        final NavigationLinkColumn actionColumn = createActionColumn();
        searchModel.getResultColumns().add(actionColumn);

        SearchResultColumnImpl nameColumn;
        searchModel.getResultColumns().add(nameColumn = new SearchResultColumnImpl()
            .withName("name")
            .withTableColumn(new PropertyColumn(Location.class, Location.NAME_COLUMN_PROP)
                .withColumnName(CommonColumnText.NAME))
            .withOrderBy(createNameOrderBy()));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("address")
            .withTableColumn(new FixedValueColumn().withColumnName(CommonColumnText.ADDRESS))
            .withTableCellRenderer(new CustomCellRenderer(EMPTY, input -> {
                Location location = (Location)input;
                return Optional.ofNullable(location)
                    .map(Location::getAddress)
                    .map(a -> a.getAddressLine(1)).orElse("");
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("email")
            .withTableColumn(new FixedValueColumn().withColumnName(CommonColumnText.EMAIL))
            .withTableCellRenderer(new CustomCellRenderer(EMPTY, input -> {
                Location location = (Location)input;
                return Optional.ofNullable(location)
                    .map(Location::getEmailAddress)
                    .map(EmailAddress::getEmail)
                    .orElse("");
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("phone")
            .withTableColumn(new FixedValueColumn().withColumnName(CommonColumnText.PHONE))
            .withTableCellRenderer(new CustomCellRenderer(EMPTY, input -> {
                Location location = (Location)input;
                return Optional.ofNullable(location)
                    .map(Location::getPhoneNumber)
                    .map(PhoneNumber::toExternalForm)
                    .orElse("");
            })));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("status")
            .withTableColumn(new PropertyColumn(Location.class, Location.STATUS_PROP)
                .withColumnName(CommonColumnText.STATUS)));

        searchModel.setDefaultSortColumn(nameColumn);
    }

    /**
     * Add constraints.
     *
     * @param searchModel the search model
     */
    protected void addConstraints(SearchModelImpl searchModel)
    {
        searchModel.getConstraints().add(new SimpleConstraint("name").withLabel(CommonColumnText.NAME)
            .withProperty(Location.NAME_COLUMN_PROP)
            .withOperator(PropertyConstraint.Operator.like));

        searchModel.getConstraints().add(new SimpleConstraint("email").withLabel(CommonColumnText.EMAIL)
            .withProperty(Location.EMAIL_ADDRESS_PROP + ".email")
            .withOperator(PropertyConstraint.Operator.like));

        searchModel.getConstraints().add(new ComboBoxConstraint(AppUtil.nullFirst(EnumSet.allOf(LocationStatus.class)),
            LocationStatus.ACTIVE,
            CommonButtonText.ANY)
            .withProperty(Location.STATUS_PROP)
            .withOperator(PropertyConstraint.Operator.eq));
    }
}
