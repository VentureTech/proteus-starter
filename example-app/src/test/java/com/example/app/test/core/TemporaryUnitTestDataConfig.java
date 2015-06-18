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

package com.example.app.test.core;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.ConfigurationFactory;

import com.google.common.base.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;
import org.hibernate.cache.internal.NoCachingRegionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.DerbyTenSevenDialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.i2rd.hibernate.ClobberPersistentEntitiesForDetachedUpdatesListener;
import com.i2rd.hibernate.DynamicEntityManager;
import com.i2rd.hibernate.I2RDNamingStrategy;
import com.i2rd.hibernate.LocalizedObjectKeyPostDeleteEventListener;
import com.i2rd.hibernate.util.DynamicEntitySessionFactoryBuilder;
import com.i2rd.hibernate.util.HibernateUtil;
import com.i2rd.hibernate.util.LocationQualifier;
import com.i2rd.hibernate.util.RebuildableSessionFactoryBean;

import net.proteusframework.core.cache.AbstractCacheRegionsConfiguration;
import net.proteusframework.core.config.CoreModuleConfig;
import net.proteusframework.core.config.DataConfig;

import static com.i2rd.hibernate.util.LocationQualifier.Type.*;


/**
 * This is only temporary until access to BaseTest within proteus can be restored for projects.
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@EnableAsync
@EnableScheduling
@Configuration
@Profile(TemporaryUnitTestDataConfig.PROFILE)
@Import({CoreModuleConfig.class})
public class TemporaryUnitTestDataConfig implements DataConfig
{
    /** System Property . */
    public static final String DISABLE_2ND_LEVEL_CACHE = "Disable2ndLevelCache";
    /** Cache Manager Resource Name. */
    public static final String RESOURCE_NAME_CACHE_MANAGER = "cacheManager";
    /** Logger */
    private static final Logger _logger = LogManager.getLogger(TemporaryUnitTestDataConfig.class);
    /** Cache region configs Resource Name. */
    private static final String RESOURCE_NAME_CACHE_REGION_CONFIGS = "cacheRegionConfigs";

    /**
     * The constant PROFILE.
     */
    public static final String PROFILE = "test";

    /** the configs */
    @Autowired(required = false)
    List<AbstractCacheRegionsConfiguration> _configs;
    /** Environment. */
    @Autowired
    Environment _environment;

    /**
     * Property sources placeholder configurer.
     *
     * @return the property sources placeholder configurer
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
    {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * Dynamic entity manager.
     *
     * @return the dynamic entity manager
     */
    @Override
    @Bean
    public DynamicEntityManager dynamicEntityManager()
    {
        return new DynamicEntityManager();
    }

    /**
     * Data source.
     *
     * @return the data source
     */
    @Override
    @Bean(name = {"dataSource", "i18nDataSource"})
    public DataSource dataSource()
    {
        PoolProperties pp = new PoolProperties();
        pp.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        pp.setUrl("jdbc:derby:memory:testing;create=true");
        //pp.setUsername(_environment.getProperty("db.username", "proteusaf"));
        //pp.setPassword(_environment.getProperty("db.password", ""));
        pp.setMaxActive(100);
        pp.setMaxWait(10000);
        //pp.setValidationQuery("SELECT 1");
        pp.setDefaultAutoCommit(true);
        pp.setJdbcInterceptors(ConnectionState.class.getName());
        //pp.setJmxEnabled(true);
        //pp.setTestWhileIdle(true);
        pp.setTimeBetweenEvictionRunsMillis(30000);
        pp.setMinEvictableIdleTimeMillis(60000);

        org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource(pp);
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try
        {
            final ObjectName objectName = ObjectName.getInstance("tomcat.jdbc", "name", "pool");
            if (!server.isRegistered(objectName))
                server.registerMBean(ds.createPool().getJmxPool(), objectName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Session factory.
     *
     * @return the rebuildable session factory bean
     */
    @Override
    @Bean
    @DependsOn({RESOURCE_NAME_CACHE_MANAGER})
    public RebuildableSessionFactoryBean sessionFactory()
    {
        Supplier<DynamicEntitySessionFactoryBuilder> configurationSupplier = new Supplier<DynamicEntitySessionFactoryBuilder>()
        {
            @Override
            public DynamicEntitySessionFactoryBuilder get()
            {
                return sessionFactoryBuilder();
            }
        };
        return new RebuildableSessionFactoryBean(configurationSupplier)
        {
            @Override
            protected void setupListeners(EventListenerRegistry listenerRegistry)
            {
                listenerRegistry.prependListeners(EventType.SAVE_UPDATE, new ClobberPersistentEntitiesForDetachedUpdatesListener());
                // Not sure if needed, if it is we can refactor the listener a bit to support update.  Main thing is the cascade
                // action is hard-coded.  Don't want to have it on at all if not needed.
                //listenerRegistry.prependListeners(EventType.UPDATE, new ClobberPersistentEntitiesForDetachedUpdatesListener());

                listenerRegistry.appendListeners(EventType.POST_DELETE, new LocalizedObjectKeyPostDeleteEventListener());
            }
        };
    }


    /**
     * Scan net.proteusframework for HBM XML files.
     *
     * @return bean. string
     */
    @Bean()
    @LocationQualifier(orm_location)
    public String ormLocationNetProteusframework()
    {
        return "classpath*:net/proteusframework/**/*.hbm.xml";
    }

    /**
     * Scan com.i2rd for HBM XML files.
     *
     * @return bean. string
     */
    @Bean()
    @LocationQualifier(orm_location)
    public String ormLocationComI2rd()
    {
        return "classpath*:com/i2rd/**/*.hbm.xml";
    }

    /**
     * Hibernate metadata in package.
     *
     * @return bean. string
     */
    @Bean()
    @LocationQualifier(metadata_package)
    public String packageMetadataHibernateUserType()
    {
        return "com.i2rd.hibernate.usertype";
    }

    /**
     * Hibernate metadata in package.
     *
     * @return bean. string
     */
    @Bean()
    @LocationQualifier(metadata_package)
    public String packageMetadataContentModelDataTypeUserType()
    {
        return "com.i2rd.contentmodel.def.type";
    }

    /**
     * Hibernate metadata in package.
     *
     * @return bean. string
     */
    @Bean()
    @LocationQualifier(metadata_package)
    public String packageMetadataCmsContentModel()
    {
        return "com.i2rd.cms.bean.contentmodel";
    }

    /**
     * Hibernate metadata in package.
     *
     * @return bean. string
     */
    @Bean()
    @LocationQualifier(metadata_package)
    public String packageMetadataComI2rdMessage()
    {
        return "com.i2rd.message";
    }

    /**
     * Package to scan for annotated entities.
     *
     * @return bean. string
     */
    @Bean()
    @LocationQualifier(entity_location)
    public String annotatedEntityScanComI2rd()
    {
        return "com.i2rd";
    }

    /**
     * Package to scan for annotated entities.
     *
     * @return bean. string
     */
    @Bean()
    @LocationQualifier(entity_location)
    public String annotatedEntityScanNetProteusframework()
    {
        return "net.proteusframework";
    }


    /**
     * Get the session factory builder.
     *
     * @return the session factory builder.
     */
    @Override
    @Bean
    @DependsOn({RESOURCE_NAME_CACHE_MANAGER})
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DynamicEntitySessionFactoryBuilder sessionFactoryBuilder()
    {
        final DynamicEntitySessionFactoryBuilder lsf = new DynamicEntitySessionFactoryBuilder(dataSource());

        lsf.setNamingStrategy(new I2RDNamingStrategy());

        lsf.addProperties(sessionFactoryHibernateProperties());
        for (Map.Entry<String, SQLFunction> e : sessionFactorySQLFunctions().entrySet())
        {
            lsf.addSqlFunction(e.getKey(), e.getValue());
        }

        return lsf;
    }


    /**
     * SessionFactory hibernate properties.
     *
     * @return bean. properties
     */
    @Bean
    public Properties sessionFactoryHibernateProperties()
    {
        final Properties props = new Properties();

        _logger.info("Disabling second level cache via system property.");
        props.put(AvailableSettings.CACHE_REGION_FACTORY, NoCachingRegionFactory.class.getName());
        props.put(AvailableSettings.USE_QUERY_CACHE, "false");
        props.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, "false");

        props.put(AvailableSettings.DIALECT, DerbyTenSevenDialect.class.getName());
        props.put(AvailableSettings.SHOW_SQL, "false");
        props.put(AvailableSettings.FORMAT_SQL, "false");
        props.put(AvailableSettings.RELEASE_CONNECTIONS, "on_close");
        props.put(AvailableSettings.STATEMENT_BATCH_SIZE, "50");
        props.put(AvailableSettings.ORDER_INSERTS, "true");
        props.put(AvailableSettings.ORDER_UPDATES, "true");
        props.put(AvailableSettings.UNIQUE_CONSTRAINT_SCHEMA_UPDATE_STRATEGY, "RECREATE_QUIETLY");

        return props;
    }

    /**
     * SessionFactory SQLFunctions.
     *
     * @return bean. map
     */
    @Bean
    public Map<String, SQLFunction> sessionFactorySQLFunctions()
    {
        final Map<String, SQLFunction> funcMap = new HashMap<>();
        funcMap.put("gettext", new StandardSQLFunction("gettext", HibernateUtil.STRING));
        funcMap.put("gettextvalue", new StandardSQLFunction("gettextvalue", HibernateUtil.STRING));
        funcMap.put("getModelDataSetIndex", new StandardSQLFunction("getModelDataSetIndex", HibernateUtil.INTEGER));
        funcMap.put("check_zipcodes_distance", new StandardSQLFunction("check_zipcodes_distance", HibernateUtil.BOOLEAN));
        funcMap.put("distance_by_latitude_longitude_in_miles",
            new StandardSQLFunction("distance_by_latitude_longitude_in_miles", HibernateUtil.DOUBLE));
        funcMap.put("distance_by_zipcodes_in_miles",
            new StandardSQLFunction("distance_by_zipcodes_in_miles", HibernateUtil.DOUBLE));
        funcMap.put("latitude_by_zipcode", new StandardSQLFunction("latitude_by_zipcode", HibernateUtil.DOUBLE));
        funcMap.put("longitude_by_zipcode", new StandardSQLFunction("longitude_by_zipcode", HibernateUtil.DOUBLE));
        funcMap.put("distance_by_zipcode_latitude_longitude_in_miles",
            new StandardSQLFunction("distance_by_zipcode_latitude_longitude_in_miles", HibernateUtil.DOUBLE));
        funcMap.put("median", new StandardSQLFunction("median", HibernateUtil.DOUBLE));
        return funcMap;
    }

    /**
     * Cache manager.
     *
     * @return the cache manager
     */
    @Bean(name = {RESOURCE_NAME_CACHE_MANAGER})
    @DependsOn(RESOURCE_NAME_CACHE_REGION_CONFIGS)
    @Override
    public CacheManager cacheManager()
    {
        net.sf.ehcache.config.Configuration cfg = ConfigurationFactory.parseConfiguration();
        CacheManager cm = CacheManager.create(cfg);

        for (AbstractCacheRegionsConfiguration cc : cacheRegionConfigs())
            cc.applyConfiguration(cm);

        return cm;
    }


    /**
     * Cache region configs.
     *
     * @return the list
     */
    @Override
    @Bean(name = {RESOURCE_NAME_CACHE_REGION_CONFIGS})
    public List<AbstractCacheRegionsConfiguration> cacheRegionConfigs()
    {
        if(_configs != null)
            return _configs;
        return Collections.emptyList();
    }
}