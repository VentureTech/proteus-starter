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

package com.example.app.model.terminology;

import java.io.Serializable;

import net.proteusframework.core.locale.TextSource;

/**
 * Term provider for profile related terminology.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public interface ProfileTermProvider extends Serializable
{
    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource company();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource companies();
}
