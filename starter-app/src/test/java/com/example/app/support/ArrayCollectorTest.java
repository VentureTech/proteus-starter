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

package com.example.app.support;

import com.example.app.support.service.ArrayCollector;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.i2rd.unit.test.BaseTest;

/**
 * Unit tests for {@link ArrayCollector}
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 4/11/16 10:36 AM
 */
public class ArrayCollectorTest extends BaseTest
{
    private static class TestCustomClass
    {
        public int i;
        public String str;
    }

    @DataProvider(name = "str-arr-DP")
    public Object[][] stringArrayDP()
    {
        return new Object[][]{
            {
                ((Supplier<List<String>>)() -> {
                    List<String> strings = new ArrayList<>();
                    strings.add("");
                    return strings;
                }).get()
            },
            {
                ((Supplier<List<String>>)() -> {
                    List<String> strings = new ArrayList<>();
                    strings.add("1");
                    strings.add("2");
                    strings.add("3");
                    strings.add("4");
                    return strings;
                }).get()
            },
            {
                ((Supplier<List<String>>)() -> {
                    List<String> strings = new ArrayList<>();
                    strings.add("Hello World");
                    strings.add("How are you?");
                    return strings;
                }).get()
            }
        };
    }

    @DataProvider(name = "tcc-arr-DP")
    public Object[][] customClassArrayDP()
    {
        return new Object[][]{
            {
                ((Supplier<List<TestCustomClass>>)() -> {
                    List<TestCustomClass> tccs = new ArrayList<>();
                    TestCustomClass tcc1 = new TestCustomClass();
                    tcc1.i = 9;
                    tcc1.str = "1";
                    TestCustomClass tcc2 = new TestCustomClass();
                    tcc2.i = 1;
                    tcc2.str = "9";
                    tccs.add(tcc1);
                    tccs.add(tcc2);
                    return tccs;
                }).get()
            },
            {
                ((Supplier<List<TestCustomClass>>)() -> {
                    List<TestCustomClass> tccs = new ArrayList<>();
                    TestCustomClass tcc1 = new TestCustomClass();
                    tcc1.i = 1;
                    tcc1.str = "9";
                    TestCustomClass tcc2 = new TestCustomClass();
                    tcc2.i = 9;
                    tcc2.str = "1";
                    tccs.add(tcc1);
                    tccs.add(tcc2);
                    return tccs;
                }).get()
            },
            {
                ((Supplier<List<TestCustomClass>>)() -> {
                    List<TestCustomClass> tccs = new ArrayList<>();
                    TestCustomClass tcc1 = new TestCustomClass();
                    tcc1.i = 15;
                    tcc1.str = "Hello World";
                    TestCustomClass tcc2 = new TestCustomClass();
                    tcc2.i = 1;
                    tcc2.str = "9";
                    tccs.add(tcc1);
                    tccs.add(tcc2);
                    return tccs;
                }).get()
            }
        };
    }

    @Test(groups = "unit", dataProvider = "str-arr-DP")
    public void testArrayCollectorCollectsToStringArray(List<String> strings)
    {
        String[] arr = strings.stream().collect(new ArrayCollector<>(String.class));

        Assert.assertEquals(arr.length, strings.size());
        for(int i = 0; i < arr.length; i++)
        {
            Assert.assertEquals(arr[i], strings.get(i));
        }
    }

    @Test(groups = "unit", dataProvider = "tcc-arr-DP")
    public void testArrayCollectorCollectsToCustomClassArray(List<TestCustomClass> tccs)
    {
        TestCustomClass[] arr = tccs.stream().collect(new ArrayCollector<>(TestCustomClass.class));

        Assert.assertEquals(arr.length, tccs.size());
        for(int i = 0; i < arr.length; i++)
        {
            Assert.assertEquals(arr[i].i, tccs.get(i).i);
            Assert.assertEquals(arr[i].str, tccs.get(i).str);
        }
    }
}
