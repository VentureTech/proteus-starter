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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

import com.i2rd.hibernate.I2RDNamingStrategy;
import com.i2rd.hibernate.util.DynamicEntitySessionFactoryBuilder;

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

    @Override
    @Bean
    @DependsOn({RESOURCE_NAME_CACHE_MANAGER})
    @Primary
    public DynamicEntitySessionFactoryBuilder sessionFactoryBuilder()
    {
        final DynamicEntitySessionFactoryBuilder lsf = new DynamicEntitySessionFactoryBuilder(dataSource());

        lsf.setNamingStrategy(new I2RDNamingStrategy());

        lsf.addProperties(sessionFactoryHibernateProperties());
        sessionFactorySQLFunctions().entrySet().forEach(e -> lsf.addSqlFunction(e.getKey(), e.getValue()));

        return lsf;
    }

    @Bean
    @Override
    public Properties sessionFactoryHibernateProperties()
    {
        final Properties props = super.sessionFactoryHibernateProperties();

        props.put("org.hibernate.envers.default_schema", ProjectConfig.ENVERS_SCHEMA);

        return props;
    }
}
