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

package com.example.app.model;

/**
 * Interface for defining methods required by Entities that implement a Soft Delete functionality.
 *
 * <p>
 *     Things to consider:<br>
 *         <ul>
 *             <li>Collection properties can be troublesome.</li>
 *             <li>Lists with indexes especially troublesome.</li>
 *             <li>Make sure SQLQuery sets {@code delete = FALSE}; otherwise, you will get holes / null values in the results.</li>
 *         </ul>
 * </p>
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
public interface SoftDeleteEntity
{
    /** The column and property name of the deleted property used by soft delete entities */
    String SOFT_DELETE_COLUMN_PROP = "deleted";
    /** The where clause for a soft delete entity -- to be used with a @Where annotation on the entity */
    String WHERE_CLAUSE = SOFT_DELETE_COLUMN_PROP + " = 'false'";

    /**
     * Gets the soft delete value for the "deleted" column.
     *
     * @return a boolean flag.  If true, the entity has been deleted.
     */
    boolean isDeleted();

    /**
     * Sets the soft delete value for the "deleted" column.
     *
     * @param deleted a boolean flag.  If true, the entity has been deleted.
     */
    void setDeleted(boolean deleted);
}
