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

import com.example.app.model.user.User;
import com.example.app.model.user.UserDAO;
import com.example.app.model.user.UserPosition;
import com.example.app.support.AppUtil;
import com.example.app.terminology.ProfileTermProvider;
import com.google.common.base.Preconditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nonnull;
import java.util.Collections;

import net.proteusframework.core.hibernate.dao.EntityRetriever;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.ui.column.FixedValueColumn;
import net.proteusframework.ui.column.PropertyColumn;
import net.proteusframework.ui.miwt.HistoryElement;
import net.proteusframework.ui.miwt.ReflectiveAction;
import net.proteusframework.ui.miwt.component.Checkbox;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Table;
import net.proteusframework.ui.miwt.component.composite.DateFormatLabel;
import net.proteusframework.ui.miwt.component.composite.HistoryContainer;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.search.ActionColumn;
import net.proteusframework.ui.search.PropertyConstraint;
import net.proteusframework.ui.search.QLBuilderImpl;
import net.proteusframework.ui.search.SearchModelImpl;
import net.proteusframework.ui.search.SearchResultColumnImpl;
import net.proteusframework.ui.search.SearchSupplierImpl;
import net.proteusframework.ui.search.SearchUIAction;
import net.proteusframework.ui.search.SearchUIImpl;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;

import static com.example.app.support.AppUtil.UTC;
import static com.example.app.ui.user.UserPositionManagementLOK.*;
import static net.proteusframework.core.locale.TextSources.createText;

/**
 * Provides a UI for managing {@link UserPosition}s
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@I18NFile(
    symbolPrefix = "com.lrsuccess.ldp.ui.user.UserPositionManagement",
    i18n = {
        @I18N(symbol = "Search Model Name FMT", l10n = @L10N("{0} Position Search")),
        @I18N(symbol = "Search Supplier Name FMT", l10n = @L10N("{0} Position Search")),
        @I18N(symbol = "Search Supplier Description FMT", l10n = @L10N("Search Supplier for {0} Positions")),
        @I18N(symbol = "Delete Confirm Text", l10n = @L10N("Are you sure you want to delete this Position?")),
        @I18N(symbol = "Column Title", l10n = @L10N("Title")),
        @I18N(symbol = "Column Start Date", l10n = @L10N("Start Date")),
        @I18N(symbol = "Column End Date", l10n = @L10N("End Date")),
        @I18N(symbol = "Column Current", l10n = @L10N("Primary"))
    }
)
@Configurable
public class UserPositionManagement extends HistoryContainer implements SearchUIOperationHandler
{
    private static class CurrentColumnRenderer extends Checkbox
    {
        @Override
        public Component getTableCellRendererComponent(Table table, Object value, boolean isSelected, boolean hasFocus,
            int row,
            int column)
        {
            Checkbox cb = (Checkbox)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            UserPosition position = (UserPosition)value;

            cb.setSelected(position.isCurrent());
            cb.setEnabled(false);

            return cb;
        }
    }

    @Autowired
    private EntityRetriever _er;
    @Autowired
    private UserDAO _userDAO;
    @Autowired
    private ProfileTermProvider _terms;

    private final User _user;
    private SearchUIImpl _searchUI;
    private boolean _canBeModified = true;

    /**
     *   Instantiate a new instance of UserPositionManagement
     *   @param user the User to manage the Positions for
     */
    public UserPositionManagement(@Nonnull User user)
    {
        super();
        Preconditions.checkNotNull(user, "User was null, this should not happen.");

        _user = user;

        addClassName("user-positions");
    }

    @Nonnull
    private User getUser()
    {
        return _er.reattachIfNecessary(_user);
    }

    /**
     *   Get boolean flag -- if true, this management UI will display the add, edit, and delete buttons
     *   By default, this is true.
     *   @return boolean flag
     */
    public boolean canBeModified()
    {
        return _canBeModified;
    }
    /**
     *   Set boolean flag -- if true, this management UI will display the add, edit, and delete buttons
     *   @param canBeModified boolean flag
     */
    public void setCanBeModified(boolean canBeModified)
    {
        _canBeModified = canBeModified;
    }

    @Override
    public void init()
    {
        super.init();

        final SearchSupplierImpl searchSupplier = getSearchSupplier();
        searchSupplier.setSearchUIOperationHandler(this);
        SearchUIImpl.Options options = new SearchUIImpl.Options("User Position Management");
        options.setSearchOnPageLoad(true);
        options.setSearchActions(Collections.emptyList());
        options.addSearchSupplier(searchSupplier);
        options.setHistory(getHistory());

        if(canBeModified())
        {
            final ReflectiveAction addAction = CommonActions.ADD.defaultAction();
            addAction.setActionListener(ev -> doEdit(new UserPosition()));
            options.getEntityActions().add(addAction);
        }

        _searchUI = new SearchUIImpl(options);

        setDefaultComponent(of("search-wrapper user-position-search", _searchUI));
    }

    @Override
    public boolean supportsOperation(SearchUIOperation operation)
    {
        switch(operation)
        {
            case add:
            case edit:
            case delete:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void handle(SearchUIOperationContext context)
    {
        UserPosition uPos = context.getData();
        switch(context.getOperation())
        {
//            case add: handled by Entity Action
            case edit:
                if(uPos != null)
                    doEdit(uPos);
                _searchUI.doAction(SearchUIAction.search);
                break;
            case delete:
                if(uPos != null)
                    _userDAO.deleteUserPosition(uPos);
                _searchUI.doAction(SearchUIAction.search);
                break;
            default:
                break;
        }
    }

    @Nonnull
    private SearchSupplierImpl getSearchSupplier()
    {
        SearchModelImpl searchModel = new SearchModelImpl();
        searchModel.setName("User Position Search");
        searchModel.setDisplayName(createText(SEARCH_MODEL_NAME_FMT(), _terms.user()));

        ActionColumn actions = new ActionColumn();
        actions.setIncludeCopy(false);
        actions.setIncludeView(false);
        actions.setIncludeEdit(canBeModified());
        actions.setIncludeDelete(canBeModified());
        actions.getDeleteButton().getButtonDisplay().setConfirmText(DELETE_CONFIRM_TEXT());
        searchModel.getResultColumns().add(actions);

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("title")
            .withTableColumn(new PropertyColumn(UserPosition.class, UserPosition.POSITION_COLUMN_PROP)
                .withColumnName(COLUMN_TITLE())));

        DateFormatLabel dateRenderer = new DateFormatLabel(AppUtil.getDateFormat(getLocaleContext().getLocale()));
        dateRenderer.setFixedTimeZone(UTC);

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("start-date")
            .withTableCellRenderer(dateRenderer)
            .withTableColumn(new PropertyColumn(UserPosition.class, UserPosition.START_DATE_COLUMN_PROP)
                .withColumnName(COLUMN_START_DATE())));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("end-date")
            .withTableCellRenderer(dateRenderer)
            .withTableColumn(new PropertyColumn(UserPosition.class, UserPosition.END_DATE_COLUMN_PROP)
                .withColumnName(COLUMN_END_DATE())));

        searchModel.getResultColumns().add(new SearchResultColumnImpl()
            .withName("current")
            .withTableColumn(new FixedValueColumn().withColumnName(COLUMN_CURRENT()))
            .withTableCellRenderer(new CurrentColumnRenderer()));


        SearchSupplierImpl searchSupplier = new SearchSupplierImpl();
        searchSupplier.setName(createText(SEARCH_SUPPLIER_NAME_FMT(), _terms.user()));
        searchSupplier.setDescription(createText(SEARCH_SUPPLIER_DESCRIPTION_FMT(), _terms.user()));
        searchSupplier.setSearchModel(searchModel);

        searchSupplier.setBuilderSupplier(() ->
            new QLBuilderImpl(UserPosition.class, "positionAlias")
            .appendCriteria(UserPosition.USER_PROP, PropertyConstraint.Operator.eq, getUser())
            .setOrderBy("positionAlias._" + UserPosition.START_DATE_COLUMN_PROP + " ASC"));

        return searchSupplier;
    }

    private void doEdit(@Nonnull UserPosition uPos)
    {
        final ActionListener closer = ev -> {
            getHistory().backOrClear();
            _searchUI.doAction(SearchUIAction.search);
        };

        final UserPositionPropertyEditor editor = new UserPositionPropertyEditor(getUser(), closer);
        editor.setValue(uPos);

        getHistory().add(new HistoryElement(editor));
    }
}
