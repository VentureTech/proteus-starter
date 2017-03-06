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

package com.example.app.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

import net.proteusframework.config.ProteusDataConfig;

/**
 * Database Configuration.
 *
 * Extended ProteusDataConfig to set up hibernate envers and the envers default schema.
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@EnableAsync
@EnableScheduling
@Configuration
@Primary
public class ProjectDataConfig extends ProteusDataConfig
{


    @Bean
    @Override
    public Map<String, Object> sessionFactoryHibernateProperties()
    {
         Map<String, Object> props = super.sessionFactoryHibernateProperties();

        props.put("org.hibernate.envers.default_schema", ProjectConfig.ENVERS_SCHEMA);
        props.put(AvailableSettings.GENERATE_STATISTICS, "true");

        return props;
    }
}
