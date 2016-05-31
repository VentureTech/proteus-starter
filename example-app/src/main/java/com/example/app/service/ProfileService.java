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

package com.example.app.service;


import com.example.app.model.profile.Profile;
import com.example.app.model.user.User;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Service for Profile related activities.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public interface ProfileService
{
    // FIXME : Your Application must implement this interface as a spring bean.
    // Typically you will have a DAO that all users are tied to that implements this interface.

    /**
     * Get the owner profile for a user.
     * User's have at most one profile that is considered
     * the owner of the user.
     * @param user the user.
     * @return the profile.
     */
    Optional<Profile> getOwnerProfileForUser(User user);

    /**
     * Set the owner profile for a user.
     * @param user the user.
     * @param profile the profile.
     */
    void setOwnerProfileForUser(User user, Profile profile);

    /**
     * Get a profile by programmatic identifier.
     * @param profileId the profile identifier.
     * @return the profile.
     */
    @Nullable
    Profile getProfileByProgrammaticIdentifier(String profileId);
}
