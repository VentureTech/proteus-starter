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

/**
 * Contains classes that represent resources for users like files, articles, videos, etc.
 */
@TypeDefs({
    @TypeDef(name = ResourceTypeUserType.TYPEDEF, typeClass = ResourceTypeUserType.class, defaultForType = ResourceType.class)
})
package com.example.app.model.resource;

import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;