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

import javax.annotation.Nonnull;
import java.util.function.Function;

import net.proteusframework.core.locale.TextSource;
import net.proteusframework.ui.miwt.component.Component;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.Table;
import net.proteusframework.ui.miwt.component.TableCellRenderer;
import net.proteusframework.ui.miwt.util.CommonButtonText;
import net.proteusframework.ui.search.NavigationLinkColumn;
import net.proteusframework.ui.search.SearchUI;
import net.proteusframework.ui.search.SearchUIOperation;
import net.proteusframework.ui.search.SearchUIOperationContext;
import net.proteusframework.ui.search.SearchUIOperationHandler;

/**
 * A {@link NavigationLinkColumn} implementation that allows the Delete button to be a toggle.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1 /17/17
 */
public class ToggleDeleteNavigationColumn extends NavigationLinkColumn
{
    private Function<Object, TextSource> _toggleTextExtractor = o -> CommonButtonText.DELETE;
    private Function<Object, String> _toggleHtmlClassExtractor = o -> "delete";

    @Override
    public TableCellRenderer getTableCellRenderer(SearchUI searchUI)
    {
        final SearchUIOperationHandler handler = searchUI.getSearchSupplier().getSearchUIOperationHandler();
        boolean deleteIncluded = isIncludeDelete() && handler.supportsOperation(SearchUIOperation.delete);
        if(deleteIncluded)
        {
            getDeleteAction().setActionListener(ev -> handler
                .handle(new SearchUIOperationContext(searchUI, SearchUIOperation.delete,
                    SearchUIOperationContext.DataContext.lead_selection)));
        }
        PushButton delete = new PushButton(getDeleteAction());

        final Container con = new Container(){
            @Override
            public Component getTableCellRendererComponent(Table table, Object value, boolean isSelected, boolean hasFocus, int row,
                int column)
            {
                delete.getButtonDisplay().setLabel(getToggleTextExtractor().apply(value));
                delete.setClassName(getToggleHtmlClassExtractor().apply(value));

                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        con.getDisplay().appendDisplayClass("actions");
        if (isIncludeView() && handler.supportsOperation(SearchUIOperation.view))
        {
            con.add(getViewLink());
        }
        if (isIncludeEdit() && handler.supportsOperation(SearchUIOperation.edit))
        {
            con.add(getEditLink());
        }
        if (isIncludeCopy() && (handler.supportsOperation(SearchUIOperation.copy)))
        {
            con.add(getCopyLink());
        }
        if (deleteIncluded)
        {
            con.add(delete);
        }
        return con;
    }

    /**
     * Gets toggle text extractor.
     *
     * @return the toggle text extractor
     */
    @Nonnull
    public Function<Object, TextSource> getToggleTextExtractor()
    {
        return _toggleTextExtractor;
    }

    /**
     * Sets toggle text extractor.
     *
     * @param toggleTextExtractor the toggle text extractor
     */
    public void setToggleTextExtractor(@Nonnull Function<Object, TextSource> toggleTextExtractor)
    {
        _toggleTextExtractor = toggleTextExtractor;
    }

    /**
     * With toggle text extractor toggle delete navigation column.
     *
     * @param toggleTextExtractor the toggle text extractor
     *
     * @return the toggle delete navigation column
     */
    public ToggleDeleteNavigationColumn withToggleTextExtractor(@Nonnull Function<Object, TextSource> toggleTextExtractor)
    {
        setToggleTextExtractor(toggleTextExtractor);
        return this;
    }

    /**
     * Gets toggle html class extractor.
     *
     * @return the toggle html class extractor
     */
    public Function<Object, String> getToggleHtmlClassExtractor()
    {
        return _toggleHtmlClassExtractor;
    }

    /**
     * Sets toggle html class extractor.
     *
     * @param toggleHtmlClassExtractor the toggle html class extractor
     */
    public void setToggleHtmlClassExtractor(Function<Object, String> toggleHtmlClassExtractor)
    {
        _toggleHtmlClassExtractor = toggleHtmlClassExtractor;
    }

    /**
     * Set the {@link #_toggleHtmlClassExtractor toggleHtmlClassExtractor} property
     * returning this.
     *
     * @param toggleHtmlClassExtractor the toggleHtmlClassExtractor.
     * @return this.
     * @see #setToggleHtmlClassExtractor(Function)
     */
    public ToggleDeleteNavigationColumn withToggleHtmlClassExtractor(Function<Object,String> toggleHtmlClassExtractor)
    {
        setToggleHtmlClassExtractor(toggleHtmlClassExtractor);
        return this;
    }
}
