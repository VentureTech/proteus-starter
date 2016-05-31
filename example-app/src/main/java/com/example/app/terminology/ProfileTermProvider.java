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

package com.example.app.terminology;

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
    TextSource attendee();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource attendees();

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
    TextSource location();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource membership();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource membershipType();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource profile();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource profileType();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource repository();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource resource();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource resourceAllLower();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource resources();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource result();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource user();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource userAllLower();

    /**
     * Return the term.
     * This is the profile type name that owns users.
     * @return the term.
     */
    TextSource userProfile();

    /**
     * Return the term.
     *
     * @return the term.
     */
    TextSource users();
}
