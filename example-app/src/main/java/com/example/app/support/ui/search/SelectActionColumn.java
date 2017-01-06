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

package com.example.app.support.ui.search;

import javax.annotation.Nullable;
import java.util.function.Function;

import net.proteusframework.core.locale.TextSource;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.Table;
import net.proteusframework.ui.miwt.component.TableCellRenderer;
import net.proteusframework.ui.miwt.util.CommonActions;
import net.proteusframework.ui.search.ActionColumn;
import net.proteusframework.ui.search.SearchUI;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;

/**
 * Action Column that only has a Select button
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
public class SelectActionColumn extends ActionColumn
{
    private Function<Object, TextSource> _buttonTextExtractor;
    private Function<Object, String> _htmlClassExtractor;
    private Function<Object, Boolean> _isSelectedChecker;

    @Override
    public TableCellRenderer getTableCellRenderer(SearchUI searchUI)
    {
        final SearchUIOperationHandler handler = searchUI.getSearchSupplier().getSearchUIOperationHandler();
        PushButton select = new PushButton();
        select.addActionListener(ev -> handler.handle(new SearchUIOperationContext(searchUI, SearchUIOperation.select,
            SearchUIOperationContext.DataContext.lead_selection)));

        Container con = new Container()
        {
            @Override
            public Component getTableCellRendererComponent(Table table, Object value, boolean isSelected, boolean hasFocus, int row,
                int column)
            {
                select.getButtonDisplay().setLabel(getButtonTextExtractor().apply(value));
                select.setClassName(getHtmlClassExtractor().apply(value));
                if (getIsSelectedChecker().apply(value))
                {
                    select.addClassName("active");
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        con.getDisplay().appendDisplayClass("actions");
        con.add(select);
        return con;
    }

    /**
     * Get the button text extractor.  If one was not supplied, simply uses CommonActions.SELECT.getName()
     *
     * @return the button text extractor.
     */
    public Function<Object, TextSource> getButtonTextExtractor()
    {
        if (_buttonTextExtractor == null)
        {
            return (obj) -> CommonActions.SELECT.getName();
        }
        return _buttonTextExtractor;
    }

    /**
     * Get the html class extractor.  If one was not supplied, simply returns "select"
     *
     * @return the html class extractor.
     */
    public Function<Object, String> getHtmlClassExtractor()
    {
        if (_htmlClassExtractor == null)
        {
            return (obj) -> "select";
        }
        return _htmlClassExtractor;
    }

    /**
     * Get the function for verifying if the current object is selected.  By default returns false for everything.
     *
     * Passing null into the checker should return a boolean flag telling if there is a value selected.
     *
     * @return the isSelectedChecker
     */
    public Function<Object, Boolean> getIsSelectedChecker()
    {
        if (_isSelectedChecker == null)
        {
            return (obj) -> false;
        }
        return _isSelectedChecker;
    }

    /**
     * Set the button text extractor.  If one is not supplied, SelectActionColumn simply uses CommonActions.SELECT.getName()
     *
     * @param buttonTextExtractor the button text extractor
     *
     * @return this
     */
    public SelectActionColumn withButtonTextExtractor(
        @Nullable Function<Object, TextSource> buttonTextExtractor)
    {
        _buttonTextExtractor = buttonTextExtractor;
        return this;
    }

    /**
     * Set the html class extractor.  If one is not supplied, SelectActionColumn simply returns "select"
     *
     * @param htmlClassExtractor the html class extractor
     *
     * @return this
     */
    public SelectActionColumn withHtmlClassExtractor(
        @Nullable Function<Object, String> htmlClassExtractor)
    {
        _htmlClassExtractor = htmlClassExtractor;
        return this;
    }

    /**
     * Set the function for verifying if the current object is selected.  By default returns false for everything.
     *
     * Passing null into the checker should return a boolean flag telling if there is a value selected.
     *
     * @param isSelectedChecker the isSelectedChecker
     *
     * @return this
     */
    public SelectActionColumn withIsSelectedChecker(@Nullable Function<Object, Boolean> isSelectedChecker)
    {
        _isSelectedChecker = isSelectedChecker;
        return this;
    }
}
