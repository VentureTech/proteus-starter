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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import com.i2rd.spring.I2RDApplicationEventMulticaster;
import com.i2rd.util.InstallConfiguration;


/**
 * This is only temporary until access to BaseTest within proteus can be restored for projects.
 *
 * @author Alan Holt (aholt@venturetech.net)
 */
@ActiveProfiles(profiles = {TemporaryUnitTestDataConfig.PROFILE})
@TestExecutionListeners({ServletTestExecutionListener.class, DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class})
@ContextConfiguration(classes = {I2RDApplicationEventMulticaster.class,
    TemporaryUnitTestDataConfig.class})
public abstract class BaseTest implements IHookable, ApplicationContextAware
{

    static
    {
        System.setProperty(InstallConfiguration.PROPERTY_NAME, "proteus_test");
    }

    /** test context manager */
    private final TestContextManager _testContextManager;
    /** application context */
    private ApplicationContext _applicationContext;
    /** test exception */
    private Throwable _testException;

    /**
     * Instantiates a new Core test class test nG.
     */
    public BaseTest()
    {
        _testContextManager = new TestContextManager(getClass());
    }

    /**
     * Gets application context.
     *
     * @return the application context
     */
    public ApplicationContext getApplicationContext()
    {
        return _applicationContext;
    }

    /**
     * Sets application context.
     *
     * @param applicationContext the application context
     */
    @Override
    public final void setApplicationContext(ApplicationContext applicationContext)
    {
        _applicationContext = applicationContext;
    }

    /**
     * Run void.
     *
     * @param callBack the call back
     * @param testResult the test result
     */
    @Override
    public void run(IHookCallBack callBack, ITestResult testResult)
    {
        callBack.runTestMethod(testResult);

        Throwable testResultException = testResult.getThrowable();

        if (testResultException instanceof InvocationTargetException)
        {
            testResultException = ((InvocationTargetException) testResultException).getCause();
        }

        _testException = testResultException;
    }

    /**
     * Gets random string.
     *
     * @param length the length
     * @return the random string
     */
    public String getRandomString(int length)
    {
        String UUIDString = UUID.randomUUID().toString().replace("-","");
        return UUIDString.substring(0, Math.min(UUIDString.length(), length));
    }

    /**
     * Spring test context before test class.
     *
     * @throws Exception the exception
     */
    @BeforeClass(alwaysRun = true)
    protected void springTestContextBeforeTestClass() throws Exception
    {
        _testContextManager.beforeTestClass();
    }

    /**
     * Spring test context prepare test instance.
     *
     * @throws Exception the exception
     */
    @BeforeClass(alwaysRun = true, dependsOnMethods = "springTestContextBeforeTestClass")
    protected void springTestContextPrepareTestInstance() throws Exception
    {
        _testContextManager.prepareTestInstance(this);
    }

    /**
     * Spring test context before test method.
     *
     * @param testMethod the test method
     * @throws Exception the exception
     */
    @BeforeMethod(alwaysRun = true)
    protected void springTestContextBeforeTestMethod(Method testMethod) throws Exception
    {
        _testContextManager.beforeTestMethod(this, testMethod);
    }

    /**
     * Spring test context after test method.
     *
     * @param testMethod the test method
     * @throws Exception the exception
     */
    @AfterMethod(alwaysRun = true)
    protected void springTestContextAfterTestMethod(Method testMethod) throws Exception
    {
        try
        {
            _testContextManager.afterTestMethod(this, testMethod, _testException);
        }
        finally
        {
            _testException = null;
        }
    }

    /**
     * Spring test context after test class.
     *
     * @throws Exception the exception
     */
    @AfterClass(alwaysRun = true)
    protected void springTestContextAfterTestClass() throws Exception
    {
        _testContextManager.afterTestClass();
    }
}