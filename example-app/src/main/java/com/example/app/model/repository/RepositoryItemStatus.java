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

package com.example.app.model.repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import net.proteusframework.core.locale.LocaleContext;
import net.proteusframework.core.locale.NamedObject;
import net.proteusframework.core.locale.NamedObjectComparator;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;

import static com.example.app.model.repository.RepositoryItemStatusLOK.*;

/**
 * Status of a RepositoryItem.
 *
 * @author Russ Tennant (russ@i2rd.com)
 * @author Alan Holt (aholt@venturetech.net)
 */
@I18NFile(
    symbolPrefix = "com.lrlabs.model.repository.RepositoryItemStatus",
    i18n = {
        @I18N(symbol = "Name Draft", l10n = @L10N("Draft")),
        @I18N(symbol = "Name Active", l10n = @L10N("Active")),
        @I18N(symbol = "Name Archived", l10n = @L10N("Archived"))
    }
)
public enum RepositoryItemStatus implements NamedObject
{
    /** Status. */
    Draft(NAME_DRAFT(), "status-draft"),
    /** Status. */
    Active(NAME_ACTIVE(), "status-active"),
    /** Status. */
    Archived(NAME_ARCHIVED(), "status-archived");

    /**
     * Comparator that uses NamedObjectComparator to perform comparison of RepositoryItemStatus
     *
     * @author Alan Holt (aholt@venturetech.net)
     */
    public static class RepositoryItemStatusComparator implements Comparator<RepositoryItemStatus>, Serializable
    {
        private static final long serialVersionUID = 3091041039178200031L;

        private final LocaleContext _lc;

        /**
         * Constructor
         *
         * @param lc the Locale Context
         */
        public RepositoryItemStatusComparator(LocaleContext lc)
        {
            _lc = lc;
        }

        @Override
        public int compare(RepositoryItemStatus o1, RepositoryItemStatus o2)
        {
            return new NamedObjectComparator(_lc).compare(o1, o2);
        }
    }

    private final TextSource _name;
    private final String _htmlClass;

    /**
     * Get a list for a combo box. This includes a null value in the 0 index
     *
     * @param comparator comparator for the combo box option order
     *
     * @return a list of RepositoryItemStatus
     */
    public static List<RepositoryItemStatus> getListForCombo(@Nullable Comparator<RepositoryItemStatus> comparator)
    {
        List<RepositoryItemStatus> statusList = new ArrayList<>(EnumSet.allOf(RepositoryItemStatus.class));
        if (comparator != null)
            Collections.sort(statusList, comparator);
        statusList.add(0, null);
        return statusList;
    }


    RepositoryItemStatus(TextSource name, String htmlClass)
    {
        _name = name;
        _htmlClass = htmlClass;
    }

    /**
     * Get the HTML class for this RepositoryItemStatus
     *
     * @return html class
     */
    public String getHtmlClass()
    {
        return _htmlClass;
    }

    @Nonnull
    @Override
    public TextSource getName()
    {
        return _name;
    }

    @Nullable
    @Override
    public TextSource getDescription()
    {
        return null;
    }
}
