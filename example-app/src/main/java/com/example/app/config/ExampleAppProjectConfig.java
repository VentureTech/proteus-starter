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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.i2rd.hibernate.util.LocationQualifier;

import net.proteusframework.config.ProteusWebAppConfig;
import net.proteusframework.core.locale.xml.StaticKeyDataConfig;

import static com.i2rd.hibernate.util.LocationQualifier.Type.entity_location;
import static com.i2rd.hibernate.util.LocationQualifier.Type.orm_location;

/**
 * Example configuration class.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@SuppressWarnings("SameReturnValue")
@Configuration
@EnableAsync
@EnableScheduling
@EnableSpringConfigured
// Scan for spring components in my package hierarchy
@ComponentScan({"com.example.app"})
// Add spring xml files if you'd like to register spring beans with XML.
//@ImportResource("classpath:/spring/*.spring.xml")
// SPLIT PropertySource -> https://jira.spring.io/browse/SPR-11637
@PropertySource(
    name = ProteusWebAppConfig.PROTEUSFRAMEWORK_PROPERTY_SOURCE_NAME,
    value = {
        ProteusWebAppConfig.PROTEUSFRAMEWORK_CONFIG_DEFAULT_PROPERTIES
    }
)
@PropertySource(
    name = "your-app-props",
    value = {
        "classpath:/com/example/app/config/default.properties",
        ProteusWebAppConfig.PROTEUSFRAMEWORK_SPRING_PROPERTIES_PLACEHOLDER,
    }
)
public class ExampleAppProjectConfig implements ApplicationListener
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ExampleAppProjectConfig.class);
    /*
     * If you would like to setup your own servlets or filters either
     *
     * 1) implement a WebApplicationInitializer
     *   OR
     * 2) add a /META-INF/web-fragment.xml resource file.
     *
     * both will be picked up automatically by the servlet container when
     * your app starts. If you are using Cms.main to launch the app, you
     * will need to register the package of your web app initializer due
     * to a bug in Jetty. See "Cms.ServerOptions.configurationPackage"
     * you'll probably want to keep "net.proteusframework.config" as one of
     * the option values for the configurationPackage option.
     */


    /*
     * Example scheduled-task
     */
    // @Scheduled(cron="*/5 * * * * MON-FRI")
    //    public void doScheduledExample()
    //    {
    // Requires @EnableAsync and @EnableScheduling to be uncommented above.
    // http://docs.spring.io/spring/docs/3.2.4.RELEASE/spring-framework-reference/html/scheduling.html
        /*
         * Work to be done goes here.
         */
    //    }


    /**
     * Static key config.
     * @return bean.
     */
    @Bean()
    public StaticKeyDataConfig staticKeyDataConfig()
    {
        StaticKeyDataConfig config = new StaticKeyDataConfig();
        config.addScanPackage("com.example.app");
        config.setUpdate(true);
        return config;
    }

    /**
     * Scan com.example for HBM XML files.
     * @return bean.
     */
    @Bean()
    @LocationQualifier(orm_location)
    public String ormLocationComExample()
    {
        return "classpath*:com/example/app/**/*.hbm.xml";
    }

    /**
     * Package to scan for annotated entities.
     * @return bean.
     */
    @Bean()
    @LocationQualifier(entity_location)
    public String annotatedEntityScanComExample()
    {
        return "com.example.app";
    }


    /**
     * Example to test if weaving is working.
     * @param event the event.
     */
    @SuppressFBWarnings("DM_EXIT")
    @Override
    public void onApplicationEvent(ApplicationEvent event)
    {
        if(event instanceof ContextRefreshedEvent)
        {
            if(!new AspectWeavingTest().isConfigured())
            {
                ApplicationContextEvent ace = (ApplicationContextEvent) event;
                ApplicationContext applicationContext = ace.getApplicationContext();
                try
                {
                    if(applicationContext instanceof AbstractApplicationContext)
                        ((AbstractApplicationContext)applicationContext).close();
                }
                finally
                {
                    ApplicationContextException ex = new ApplicationContextException(
                        "AspectJ weaving is not working. Configure compile-time or load-time weaving.");
                    _logger.fatal("AspectJ weaving misconfiguration.", ex);
                }
                System.exit(1);
            }
        }

    }

}
