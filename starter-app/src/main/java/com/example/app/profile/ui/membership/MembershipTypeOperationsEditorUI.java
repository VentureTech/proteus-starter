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

package com.example.app.profile.ui.membership;


import com.example.app.profile.model.membership.MembershipOperation;
import com.example.app.profile.model.membership.MembershipType;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.miwt.HistoryImpl;
import net.proteusframework.ui.miwt.component.Checkbox;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.Table;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUIImpl;

import static com.example.app.profile.ui.UIText.MEMBERSHIP_TYPE;
import static com.example.app.profile.ui.membership.MembershipTypeOperationsEditorUILOK.*;

/**
 * Provides a UI for editing the MembershipOperations on a MembershipType
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 7 /1/16 3:03 PM
 */
@I18NFile(
    symbolPrefix = "com.example.app.profile.ui.membership.MembershipTypeOperationsEditorUI",
    classVisibility = I18NFile.Visibility.PUBLIC,
    i18n = {
        @I18N(symbol = "MembershipType UI Heading Format", l10n = @L10N("{0} Operations for: {1}")),
        @I18N(symbol = "Operations Search Model Name FMT", l10n = @L10N("{0} Operations Search")),
        @I18N(symbol = "Operations Search Supplier Name FMT", l10n = @L10N("{0} Operations Search")),
        @I18N(symbol = "Operations Search Supplier Description FMT", l10n = @L10N("Search Supplier for {0} Operations")),
        @I18N(symbol = "Column Operation", l10n = @L10N("Operation")),
        @I18N(symbol = "Column Enabled", l10n = @L10N("Enabled"))
    }
)
@Configurable(preConstruction = true)
public class MembershipTypeOperationsEditorUI extends Container
{
    private final MembershipType _membershipType;
    private SearchUIImpl<MembershipOperation> _searchUI;
    private final List<MembershipOperation> _selectedOperations = new ArrayList<>();

    /**
     * Instantiates a new Membership type operations editor ui.
     *
     * @param membershipType the membership type
     */
    public MembershipTypeOperationsEditorUI(@Nonnull MembershipType membershipType)
    {
        super();
        _membershipType = membershipType;
        Hibernate.initialize(membershipType);
        Hibernate.initialize(membershipType.getDefaultOperations());
        _selectedOperations.addAll(_membershipType.getDefaultOperations());
    }

    /**
     * Gets selected operations.
     *
     * @return the selected operations
     */
    public List<MembershipOperation> getSelectedOperations()
    {
        return _selectedOperations;
    }

    @Override
    public void init()
    {
        super.init();

        MessageContainer messages = new MessageContainer(35_000L);

        final SearchSupplierImpl<MembershipOperation> searchSupplier = getSearchSupplier();
        SearchUIImpl.Options<MembershipOperation> options = new SearchUIImpl.Options<>("Membership Operation Selector");
        options.setSearchOnPageLoad(true);

        options.setSearchActions(Collections.emptyList());
        options.addSearchSupplier(searchSupplier);
        options.setHistory(new HistoryImpl());

        _searchUI = new SearchUIImpl<>(options);

        Label heading = new Label(MEMBERSHIPTYPE_UI_HEADING_FORMAT(MEMBERSHIP_TYPE(), _membershipType.getName()));
        heading.setHTMLElement(HTMLElement.h3);

        add(of("search-wrapper membership-operation-search", heading, messages,  _searchUI));
    }

    @SuppressWarnings("Duplicates")
    private SearchSupplierImpl<MembershipOperation> getSearchSupplier()
    {
        SearchModelImpl searchModel = new SearchModelImpl();
        searchModel.setName("Operations Selector");
        searchModel.setDisplayName(OPERATIONS_SEARCH_MODEL_NAME_FMT(MEMBERSHIP_TYPE()));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("operation")
            .withTableColumn(new FixedValueColumn().withColumnName(COLUMN_OPERATION()))
            .withTableCellRenderer(new CustomCellRenderer(TextSources.EMPTY)))
        ;

        Checkbox selectOperation = new Checkbox(TextSources.EMPTY){
            @Override
            public Component getTableCellRendererComponent(Table table, Object value, boolean isSelected,
                boolean hasFocus, int row,
                int column)
            {
                Checkbox cb = (Checkbox)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                MembershipOperation op = (MembershipOperation)value;

                cb.setSelected(getSelectedOperations().contains(op));
                cb.setEnabled(true);

                return cb;
            }
        };
        selectOperation.addActionListener(ev -> {
            MembershipOperation operation = _searchUI.getLeadSelection();
            if(operation != null)
            {
                if(getSelectedOperations().contains(operation))
                    getSelectedOperations().remove(operation);
                else
                    getSelectedOperations().add(operation);
            }
        });

        Container actionColumnRenderer = new Container();
        actionColumnRenderer.add(selectOperation);

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("actions")
            .withTableColumn(new FixedValueColumn().withColumnName(COLUMN_ENABLED()))
            .withTableCellRenderer(actionColumnRenderer));

        SearchSupplierImpl<MembershipOperation> searchSupplier = new SearchSupplierImpl<>();
        searchSupplier.setName(OPERATIONS_SEARCH_SUPPLIER_NAME_FMT(MEMBERSHIP_TYPE()));
        searchSupplier.setDescription(OPERATIONS_SEARCH_SUPPLIER_DESCRIPTION_FMT(MEMBERSHIP_TYPE()));
        searchSupplier.setSearchModel(searchModel);
        searchSupplier.setBuilderSupplier(() -> new QLBuilderImpl<>(MembershipOperation.class, "mopAlias"));

        return searchSupplier;
    }
}
