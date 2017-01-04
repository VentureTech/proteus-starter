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

package com.example.app.ui.user;


import com.example.app.model.company.SelectedCompanyTermProvider;
import com.example.app.model.profile.Membership;
import com.example.app.model.profile.MembershipOperation;
import com.example.app.model.profile.MembershipType;
import com.example.app.model.profile.ProfileType;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.proteusframework.core.html.HTMLElement;
import net.proteusframework.core.locale.LocaleContextSource;
import net.proteusframework.core.locale.TextSources;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.miwt.History;
import net.proteusframework.ui.miwt.component.Checkbox;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.Label;
import net.proteusframework.ui.miwt.component.Table;
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer;
import net.proteusframework.ui.miwt.component.composite.MessageContainer;
import net.proteusframework.ui.miwt.data.SortOrder;
import net.proteusframework.ui.search.JoinedQLBuilder;
import net.proteusframework.ui.search.LocalizedObjectKeyQLOrderBy;
import net.proteusframework.ui.search.QLBuilder;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUIImpl;

import static com.example.app.ui.UIText.USER;
import static com.example.app.ui.user.UserMembershipManagementLOK.*;
import static net.proteusframework.core.locale.TextSources.createText;
import static net.proteusframework.ui.search.PropertyConstraint.Operator;

/**
 * Membership operation editor.
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.MembershipOperationsEditorUI",
    i18n = {
        @I18N(symbol = "Membership UI Heading Format", l10n = @L10N("User''s Operations for ''{0} : {1}'' Role")),
    }
)
@Configurable
class MembershipOperationsEditorUI extends Container
{
    private static class MyOrderBy extends LocalizedObjectKeyQLOrderBy
    {
        public MyOrderBy(LocaleContextSource localeContextSource,
            String lokProperty)
        {
            super(localeContextSource, lokProperty);
        }

        @Override
        public String getPrefixedOrderBy(QLBuilder builder)
        {
            return super.getPrefixedOrderBy(builder);
        }
    }

    private class MyCheckbox extends Checkbox
    {
        public MyCheckbox()
        {
            super(TextSources.EMPTY);
        }

        @Override
        public Component getTableCellRendererComponent(Table table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
        {
            Checkbox cb = (Checkbox) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            MembershipOperation op = (MembershipOperation) value;

            cb.setSelected(getSelectedOperations().contains(op));
            cb.setEnabled(true);

            return cb;
        }
    }
    private final Membership _membership;
    private final History _history;
    private final List<MembershipOperation> _selectedOperations = new ArrayList<>();
    @Autowired
    private SelectedCompanyTermProvider _terms;
    private SearchUIImpl _searchUI;

    public MembershipOperationsEditorUI(@Nonnull Membership membership, @Nonnull History history)
    {
        super();
        _membership = membership;
        Hibernate.initialize(membership);
        Hibernate.initialize(membership.getProfile());
        Hibernate.initialize(membership.getProfile().getProfileType());
        _selectedOperations.addAll(_membership.getOperations());
        _history = history;
    }

    @Override
    public void init()
    {
        super.init();

        MessageContainer messages = new MessageContainer(35_000L);

        final SearchSupplierImpl searchSupplier = getSearchSupplier();
        SearchUIImpl.Options options = new SearchUIImpl.Options("Membership Operation Selector");
        options.setSearchOnPageLoad(true);

        options.setSearchActions(Collections.emptyList());
        options.addSearchSupplier(searchSupplier);
        options.setHistory(_history);
        options.setRowExtractor(input -> {
            if (input.getClass().isArray())
                return Array.get(input, 0);
            else
                return input;
        });

        _searchUI = new SearchUIImpl(options);

        Label heading = new Label(createText(MembershipOperationsEditorUILOK.MEMBERSHIP_UI_HEADING_FORMAT(),
            _membership.getProfile().getName(), _membership.getMembershipType().getName()));
        heading.setHTMLElement(HTMLElement.h3);

        add(of("search-wrapper membership-operation-search", heading, messages, _searchUI));
    }

    private SearchSupplierImpl getSearchSupplier()
    {
        SearchModelImpl searchModel = new SearchModelImpl();
        searchModel.setName("Operations Selector");
        searchModel.setDisplayName(OPERATIONS_SEARCH_MODEL_NAME_FMT(USER()));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("operation")
            .withTableColumn(new FixedValueColumn().withColumnName(COLUMN_OPERATION()))
            .withTableCellRenderer(new CustomCellRenderer(TextSources.EMPTY)))
        ;

        Checkbox selectOperation = new MyCheckbox();
        selectOperation.addActionListener(ev -> {
            MembershipOperation operation = _searchUI.getLeadSelection();
            if (operation != null)
            {
                if (getSelectedOperations().contains(operation))
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

        SearchSupplierImpl searchSupplier = new SearchSupplierImpl();
        searchSupplier.setName(OPERATIONS_SEARCH_SUPPLIER_NAME_FMT(USER()));
        searchSupplier.setDescription(OPERATIONS_SEARCH_SUPPLIER_DESCRIPTION_FMT(USER()));
        searchSupplier.setSearchModel(searchModel);
        searchSupplier.setBuilderSupplier(() -> {
            final QLBuilderImpl qb = new QLBuilderImpl(ProfileType.class, "ptAlias");
            qb.appendCriteria("id", Operator.eq, _membership.getProfile().getProfileType().getId());
            final JoinedQLBuilder mtQB = qb.createInnerJoin(ProfileType.MEMBERSHIP_TYPES_PROP);
            final JoinedQLBuilder opQB = mtQB.createInnerJoin(MembershipType.DEFAULT_OPERATIONS_PROP);

            MyOrderBy orderBy = new MyOrderBy(this, String.format("%s.%s", opQB.getAlias(), MembershipOperation.NAME_COLUMN_PROP));

            opQB.setProjection("DISTINCT %s, %s", opQB.getAlias(), orderBy.getPrefixedOrderBy(opQB));
            orderBy.updateOrderBy(opQB, SortOrder.ASCENDING);
            return opQB;
        });

        return searchSupplier;
    }

    public List<MembershipOperation> getSelectedOperations()
    {
        return _selectedOperations;
    }
}
