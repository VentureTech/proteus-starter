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

import org.intellij.lang.annotations.Language;
import org.testng.annotations.Test;

import java.util.List;

import static com.example.app.service.NotificationService.splitContent;
import static org.testng.Assert.assertEquals;

/**
 * Tests.
 *
 * @author Russ Tennant (russ@venturetech.net)
 */
public class NotificationServiceTest
{

    private static final String CHARS_160 =
        "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
        + "12345678901234567890123456789012345678901234567890123456789012345678901234567890";

    @Test(groups = "unit")
    public void testSplitContent() throws Exception
    {
        List<String> parts = splitContent(CHARS_160);
        assertEquals(parts.size(), 1);

        parts = splitContent(CHARS_160 + " Foo");
        assertEquals(parts.size(), 2);
        assertEquals(parts.get(1), " Foo");
        assertEquals(parts.get(0).length(), 160);

        parts = splitContent("Foo " + CHARS_160  + CHARS_160 + "1234567890");
        assertEquals(parts.size(), 3);
        assertEquals(parts.get(0).length(), 160);
        assertEquals(parts.get(1).length(), 160);
        assertEquals(parts.get(2).length(), 14);
    }

    @Test(groups = "unit")
    public void testSplitContent2() throws Exception
    {
        @Language("HTML")
        final String mesg = "Hi Russ,\n"
            + "I just want to remind you about the Complete Emerging Leader Questionnaire. Let me know if you "
            + "have any questions or need any help.\n"
            + "https://goog.gl/eOFBtw";
        final List<String> parts = splitContent(mesg);
        assertEquals(parts.size(), 2);
    }

    @Test(groups = "unit")
    public void testSplitContent3() throws Exception
    {
        @Language("HTML")
        final String mesg = "Hi Russ,\n"
            + "I just want to remind you about the Initial Executive Guide Meeting Instruction Email. Let me know if you "
            + "have any questions or need any help.\n"
            + "https://goog.gl/CdEdLY";
        final List<String> parts = splitContent(mesg);
        assertEquals(parts.size(), 2);
    }
}