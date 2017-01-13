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

import org.springframework.stereotype.Service;

import net.proteusframework.ui.search.SearchUIOperation;

/**
 * Provides methods for helping with SearchUI Components
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 1/12/17
 */
@Service
public class SearchUIHelper
{
    /**
     * Checks if the given {@link SearchUIOperation} is one of the standard SearchUIOperations that most management UIs use:
     * <br><br>
     * {@link SearchUIOperation#add}<br>
     * {@link SearchUIOperation#view}<br>
     * OR<br>
     * {@link SearchUIOperation#delete}<br>
     *
     * @param operation the operation
     *
     * @return true if the given operation is one of the standard operations, false otherwise.
     */
    public boolean isStandardSearchUIOperation(SearchUIOperation operation)
    {
        switch(operation)
        {
            case add:
            case view:
            case delete:
                return true;
            default:
                return false;
        }
    }
}
