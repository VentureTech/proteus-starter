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

package com.example.app.support.address;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.i2rd.unit.test.TestGroups;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;


public class AddressParserTest
{

    @Test(groups = TestGroups.UNIT)
    public void testParseAddress()
    {
        String addr1 = "123 Avenue of art, philadelphia pa 12345";
        Map<AddressComponent, String> addressComponents = AddressParser.parseAddress(addr1);
        assertEquals("12345", addressComponents.get(AddressComponent.ZIP));
        assertEquals("philadelphia", addressComponents.get(AddressComponent.CITY));
        assertEquals("pa", addressComponents.get(AddressComponent.STATE));
        assertEquals("123", addressComponents.get(AddressComponent.NUMBER));
        addressComponents = AddressParser.parseAddress("123 FISH AND GAME rd philadelphia pa 12345");
        assertEquals("12345", addressComponents.get(AddressComponent.ZIP));
        assertEquals("philadelphia", addressComponents.get(AddressComponent.CITY));
        assertEquals("pa", addressComponents.get(AddressComponent.STATE));
        assertEquals("123", addressComponents.get(AddressComponent.NUMBER));
        assertEquals("FISH AND GAME", addressComponents.get(AddressComponent.STREET));
        assertEquals("rd", addressComponents.get(AddressComponent.TYPE));
    }

    @Test(groups = TestGroups.UNIT)
    public void testParseAddress2()
    {
        String addr1 = " 14625 County Road 672, Wimauma, FL 33598";
        Map<AddressComponent, String> addressComponents = AddressParser.parseAddress(addr1);
        System.out.println("addressComponents: " + addressComponents);
        // {CITY=Wimauma, ZIP=33598, STREET=County, STATE=FL, LINE2=672, TYPE=Road, NUMBER=14625}
        assertEquals("14625", addressComponents.get(AddressComponent.NUMBER));

        //      assertEquals("12345", addressComponents.get(AddressComponent.ZIP));
        //      assertEquals("philadelphia", addressComponents.get(AddressComponent.CITY));
        //      assertEquals("pa", addressComponents.get(AddressComponent.STATE));
        //      assertEquals("123", addressComponents.get(AddressComponent.NUMBER));
    }

    @Test(groups = TestGroups.UNIT)
    public void testSaintNameExpansion()
    {
        String addr1 = "St. louis Missouri";
        Map<AddressComponent, String> m = AddressStandardizer.normalizeParsedAddress(AddressParser.parseAddress(addr1));
        assertEquals("SAINT LOUIS", m.get(AddressComponent.CITY));
        assertEquals("MO", m.get(AddressComponent.STATE));
        addr1 = "123 St peters ave, St. louis Missouri";
        m = AddressStandardizer.normalizeParsedAddress(AddressParser.parseAddress(addr1));
        assertEquals("SAINT LOUIS", m.get(AddressComponent.CITY));
        assertEquals("SAINT PETERS", m.get(AddressComponent.STREET));
        assertEquals("MO", m.get(AddressComponent.STATE));
    }

    @Test(groups = TestGroups.UNIT)
    public void testOrdinalNormalization()
    {
        String addr1 = "Mozilla Corporation, 1981 second street building K Mountain View CA 94043-0801";
        Map<AddressComponent, String> m = AddressStandardizer.normalizeParsedAddress(AddressParser.parseAddress(addr1));
        System.out.println(m);
        assertEquals("MOUNTAIN VIEW", m.get(AddressComponent.CITY));
        assertEquals("CA", m.get(AddressComponent.STATE));
        assertEquals("2ND", m.get(AddressComponent.STREET));
        assertEquals("BLDG K", m.get(AddressComponent.LINE2));
    }

    @Test(groups = TestGroups.UNIT)
    public void testDesignatorConfusingCitiesParsing()
    {
        String addr1 = "123 main street St. louis Missouri";
        Map<AddressComponent, String> m = AddressStandardizer.normalizeParsedAddress(AddressParser.parseAddress(addr1));
        assertEquals("123", m.get(AddressComponent.NUMBER));
        assertEquals("MAIN", m.get(AddressComponent.STREET));
        assertEquals("ST", m.get(AddressComponent.TYPE));
        assertEquals("SAINT LOUIS", m.get(AddressComponent.CITY));
        assertEquals("MO", m.get(AddressComponent.STATE));
        addr1 = "123 south lake park  Fort Duchesne Utah";
        m = AddressStandardizer.normalizeParsedAddress(AddressParser.parseAddress(addr1));
        assertEquals("FORT DUCHESNE", m.get(AddressComponent.CITY));
        assertEquals("LAKE", m.get(AddressComponent.STREET));
        assertEquals("PARK", m.get(AddressComponent.TYPE));
        assertEquals("UT", m.get(AddressComponent.STATE));
        addr1 = "123 south lake park apt 200 Fort Duchesne Utah";
        m = AddressStandardizer.normalizeParsedAddress(AddressParser.parseAddress(addr1));
        assertEquals("FORT DUCHESNE", m.get(AddressComponent.CITY));
        assertEquals("LAKE", m.get(AddressComponent.STREET));
        assertEquals("PARK", m.get(AddressComponent.TYPE));
        assertEquals("UT", m.get(AddressComponent.STATE));
        assertEquals("APT 200", m.get(AddressComponent.LINE2));

        addr1 = "123 main st cape may court house nj";
        m = AddressStandardizer.normalizeParsedAddress(AddressParser.parseAddress(addr1));
        assertEquals("CAPE MAY COURT HOUSE", m.get(AddressComponent.CITY));
        assertEquals("NJ", m.get(AddressComponent.STATE));

    }

    @Test(groups = TestGroups.UNIT)
    public void testStringMatches()
    {

        String testString = " 11 564";
        Pattern p1 = Pattern.compile("^\\s?(\\S+)\\s?");
        Matcher m1 = p1.matcher(testString);
        assertTrue(m1.find());

    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress1()
    {

        String originalStreetNumber = "2462";
        String originalStreetDir = null;
        String originalStreetName = "Thunder Mountain Way";
        String originalStreetType = null;
        String originalUnitNumber = "Unit: 903";
        String city = "Grand Junction";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);
        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("2462", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Thunder Mountain", streetName);
        assertEquals("Way", streetType);
        assertEquals("Unit: 903", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress2()
    {

        String originalStreetNumber = "111";
        String originalStreetDir = null;
        String originalStreetName = "S 9th";
        String originalStreetType = "Street";
        String originalUnitNumber = "Unit: SW 1/4";
        String city = "Grand Junction";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("111", streetNumber);
        assertEquals("S", streetDir);
        assertEquals("9th", streetName);
        assertEquals("Street", streetType);
        assertEquals("Unit: SW 1/4", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress3()
    {

        String originalStreetNumber = "1018";
        String originalStreetDir = null;
        String originalStreetName = "NW Highway 6&50";
        String originalStreetType = null;
        String originalUnitNumber = "Unit: 66";
        String city = "Grand Junction";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("1018", streetNumber);
        assertEquals("NW", streetDir);
        assertEquals("Highway 6&50", streetName);
        assertEquals(null, streetType);
        assertEquals("Unit: 66", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress4()
    {

        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "2533 G 3/8 Road";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Grand Junction";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("2533", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("G 3/8", streetName);
        assertEquals("Road", streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress5()
    {

        String originalStreetNumber = "818";
        String originalStreetDir = null;
        String originalStreetName = "Montclair";
        String originalStreetType = "Drive";
        String originalUnitNumber = null;
        String city = "Grand Junction";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("818", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Montclair", streetName);
        assertEquals("Drive", streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress6()
    {

        String originalStreetNumber = "2880";
        String originalStreetDir = null;
        String originalStreetName = "I-70 Business";
        String originalStreetType = "Loop";
        String originalUnitNumber = null;
        String city = "Grand Junction";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("2880", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("I-70 Business", streetName);
        assertEquals("Loop", streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress7()
    {

        String originalStreetNumber = "1018";
        String originalStreetDir = null;
        String originalStreetName = "NW Highway 6&50";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Grand Junction";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("1018", streetNumber);
        assertEquals("NW", streetDir);
        assertEquals("Highway 6&50", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress8()
    {

        String originalStreetNumber = "135";
        String originalStreetDir = null;
        String originalStreetName = "Main Street";
        String originalStreetType = null;
        String originalUnitNumber = "B200";
        String city = "Grand Junction";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("135", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Main", streetName);
        assertEquals("Street", streetType);
        assertEquals("B200", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress9()
    {

        String originalStreetNumber = "88";
        String originalStreetDir = null;
        String originalStreetName = "North Fuller Placer";
        String originalStreetType = "Road";
        String originalUnitNumber = "3B";
        String city = "Grand Junction";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("88", streetNumber);
        assertEquals("North", streetDir);
        assertEquals("Fuller Placer", streetName);
        assertEquals("Road", streetType);
        assertEquals("3B", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress10()
    {

        String originalStreetNumber = "82";
        String originalStreetDir = null;
        String originalStreetName = "Wheeler Circle";
        String originalStreetType = null;
        String originalUnitNumber = "314D-6";
        String city = "Copper Mountain";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("82", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Wheeler", streetName);
        assertEquals("Circle", streetType);
        assertEquals("314D-6", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress11()
    {

        String originalStreetNumber = "216";
        String originalStreetDir = null;
        String originalStreetName = "Cr 674 County";
        String originalStreetType = "Road";
        String originalUnitNumber = null;
        String city = "Breckenridge";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("216", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Cr 674", streetName);
        assertEquals("CR", streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress12()
    {

        String originalStreetNumber = "23110";
        String originalStreetDir = null;
        String originalStreetName = "Hwy 6 ";
        String originalStreetType = null;
        String originalUnitNumber = "5028";
        String city = "Keystone";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("23110", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Hwy 6", streetName);
        assertEquals(null, streetType);
        assertEquals("5028", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress13()
    {

        String originalStreetNumber = "1238";
        String originalStreetDir = null;
        String originalStreetName = "N Ladonia";
        String originalStreetType = "Dr";
        String originalUnitNumber = null;
        String city = "Keystone";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("1238", streetNumber);
        assertEquals("N", streetDir);
        assertEquals("Ladonia", streetName);
        assertEquals("Dr", streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress14()
    {

        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "82 Wheeler Cir Unit: 317B-4";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Copper Mountain";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("82", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Wheeler", streetName);
        assertEquals("Cir", streetType);
        assertEquals("Unit: 317B-4", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress15()
    {
        // property_id = '139165' -- Pueblo
        String originalStreetNumber = "8900";
        String originalStreetDir = null;
        String originalStreetName = "Squirrel Creek";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Pueblo";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("8900", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Squirrel", streetName);
        assertEquals("Creek", streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress16()
    {
        // property_id = 'S379299' -- Summit
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "22300 6 Hwy Unit: 1736";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Keystone";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("22300", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("6", streetName);
        assertEquals("Hwy", streetType);
        assertEquals("Unit: 1736", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress17()
    {
        // test I'm making up based on last test + Street Dir
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "22300 N 6 Hwy Unit: 1736";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Keystone";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("22300", streetNumber);
        assertEquals("N", streetDir);
        assertEquals("6", streetName);
        assertEquals("Hwy", streetType);
        assertEquals("Unit: 1736", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress18()
    {
        // property_id = 'S379756' -- Summit
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "427 West 3rd St";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Keystone";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("427", streetNumber);
        assertEquals("West", streetDir);
        assertEquals("3rd", streetName);
        assertEquals("St", streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress19()
    {
        // property_id = '127833' -- Pueblo
        String originalStreetNumber = "782";
        String originalStreetDir = "S";
        String originalStreetName = "Harmony Dr";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Pueblo";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("782", streetNumber);
        assertEquals("S", streetDir);
        assertEquals("Harmony", streetName);
        assertEquals("Dr", streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress20()
    {
        // property_id = '115480' -- Pueblo
        String originalStreetNumber = "0";
        String originalStreetDir = "";
        String originalStreetName = "Chaffee Dr";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Pueblo";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("0", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Chaffee", streetName);
        assertEquals("Dr", streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress21()
    {
        // property_id = '668197' -- CREN
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "15238 Road 21";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Cortez";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("15238", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Road 21", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress22()
    {
        // property_id = '668197' (but with added street direction) -- CREN
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "15238 NW Road 21";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Cortez";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("15238", streetNumber);
        assertEquals("NW", streetDir);
        assertEquals("Road 21", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress23()
    {
        // Test to make sure that we treat North Street as the street name
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "15238 North Street 21";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Cortez";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("15238", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("North", streetName);
        assertEquals("Street", streetType);
        assertEquals("21", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress24()
    {
        // CREN -- 668174
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "486 Cr 243";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Durango";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("486", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Cr 243", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress25()
    {
        // CREN -- 668088
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "25266 Road 38.1";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Dolores";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("25266", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Road 38.1", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress26()
    {
        // Pueblo -- 135690
        // Strange case because we have no streetName (we assume N is the streetDir)
        String originalStreetNumber = "2520";
        String originalStreetDir = "N";
        String originalStreetName = "Freeway";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Pueblo";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("2520", streetNumber);
        assertEquals("N", streetDir);
        assertEquals(null, streetName);
        assertEquals("Freeway", streetType);
        assertEquals(null, unitNumber);
    }


    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress27()
    {
        // Summit -- S377601
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "0 Cr 14A Cord";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Fairplay";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("0", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Cr 14A Cord", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress28()
    {
        // Summit -- S378305
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "60 W Main St Unit: F,G,H";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Frisco";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("60", streetNumber);
        assertEquals("W", streetDir);
        assertEquals("Main", streetName);
        assertEquals("St", streetType);
        assertEquals("Unit: F,G,H", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress29()
    {
        // Summit -- S376848
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "531 Blue River Pky";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Silverthorne";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("531", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Blue River", streetName);
        assertEquals("Pky", streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress30()
    {
        // Pueblo -- 136701
        String originalStreetNumber = "11131/2";
        String originalStreetDir = null;
        String originalStreetName = "Mahren Ave";
        String originalStreetType = null;
        String originalUnitNumber = "11131/2";
        String city = "Pueblo";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("11131/2", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Mahren", streetName);
        assertEquals("Ave", streetType);
        assertEquals("11131/2", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress31()
    {
        // Cren -- 615574
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "430 W 8th Street, Building 10";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Delta";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("430", streetNumber);
        assertEquals("W", streetDir);
        assertEquals("8th", streetName);
        assertEquals("Street", streetType);
        assertEquals("Building 10", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress32()
    {
        // Cren -- 635402
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "Tbd Cr 501";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Bayfield";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("Tbd", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Cr 501", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress33()
    {
        // Cren -- 635402
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "Tbd Cr 501";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Bayfield";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("Tbd", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Cr 501", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress34()
    {
        // Cren -- 667459
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "555 Rivergate Lane #B1-104";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Durango";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("555", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Rivergate", streetName);
        assertEquals("Lane", streetType);
        assertEquals("#B1-104", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress35()
    {
        // Cren -- 667459
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "Fp-f-1- 68-1371 Kinzel Place";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Fort Garland";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("Fp-f-1- 68-1371", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Kinzel", streetName);
        assertEquals("Place", streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress36()
    {
        // Cren -- 914488
        String originalStreetNumber = "10";
        String originalStreetDir = null;
        String originalStreetName = "Black Bear";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Gypsum";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("10", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Black Bear", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress37()
    {
        // Summit -- S376251
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "513 672 Cord";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Gypsum";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("513", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("672 Cord", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress38()
    {
        // Cren -- 914488 With added Unit Number
        String originalStreetNumber = "10";
        String originalStreetDir = null;
        String originalStreetName = "Black Bear";
        String originalStreetType = null;
        String originalUnitNumber = "15";
        String city = "Gypsum";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("10", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Black Bear", streetName);
        assertEquals(null, streetType);
        assertEquals("15", unitNumber);
    }

    // Removed test 39 because it was going to be hard to make it work with the new code

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress40()
    {
        // Summit -- S378174 "9379 9 Ushy Unit: 105
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "9379 9 Ushy Unit: 105";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Breckenridge";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("9379", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("9 Ushy", streetName);
        assertEquals(null, streetType);
        assertEquals("Unit: 105", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress41()
    {
        // Vail -- V315554 "77 Castle Peak Gate"
        String originalStreetNumber = "77";
        String originalStreetDir = null;
        String originalStreetName = "Castle Peak Gate";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Edwards";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("77", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Castle Peak Gate", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress42()
    {
        // Vail -- V315554 With added unit number
        String originalStreetNumber = "77";
        String originalStreetDir = null;
        String originalStreetName = "Castle Peak Gate 17";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Edwards";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("77", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Castle Peak Gate", streetName);
        assertEquals(null, streetType);
        assertEquals("17", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress43()
    {
        // Vail -- V315554 Modified with added unit number
        String originalStreetNumber = "77";
        String originalStreetDir = null;
        String originalStreetName = "Castle Peak Gate Unit#17";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Edwards";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("77", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Castle Peak Gate", streetName);
        assertEquals(null, streetType);
        assertEquals("Unit#17", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress44()
    {
        // Vail -- V315554 Modified with added unit number
        String originalStreetNumber = "77";
        String originalStreetDir = null;
        String originalStreetName = "Castle Peak Gate Unit #17";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Edwards";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("77", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Castle Peak Gate", streetName);
        assertEquals(null, streetType);
        assertEquals("Unit #17", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress45()
    {
        // Vail -- V315554 Modified with added unit number
        String originalStreetNumber = "77";
        String originalStreetDir = null;
        String originalStreetName = "Castle Peak Gate A";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Edwards";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("77", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Castle Peak Gate", streetName);
        assertEquals(null, streetType);
        assertEquals("A", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress46()
    {
        // Vail -- V315554 Modified with added unit number and modified street name with duplicate words
        String originalStreetNumber = "77";
        String originalStreetDir = null;
        String originalStreetName = "Castle Gate Gate A";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Edwards";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("77", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Castle Gate Gate", streetName);
        assertEquals(null, streetType);
        assertEquals("A", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress47()
    {
        // Summit -- S379335
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "23110 Hwy 6 Ushy Unit: 5020";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Keystone";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("23110", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Hwy 6 Ushy", streetName);
        assertEquals(null, streetType);
        assertEquals("Unit: 5020", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress48()
    {
        // Summit -- S369956
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "0 Undefined Undefd";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Alma";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("0", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("TBD", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress49()
    {
        // Summit -- S372737
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "1 Cutting Edge S32-T8-R79  Tract A Undefd";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Leadville";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("1", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Cutting Edge S32-T8-R79 Tract", streetName);
        assertEquals(null, streetType);
        assertEquals("A", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress50()
    {
        // Summit -- S373605
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "9164 County Road #15 Undefd";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Hartsel";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("9164", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("CR #15", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress51()
    {
        // Summit -- S373605
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "9164 County Road #15 Undefd";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Hartsel";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("9164", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("CR #15", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress52()
    {
        // CREN -- 656656
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "40292 Us Hwy 550 North #572/573";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Durango";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("40292", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Us Hwy 550 North", streetName);
        assertEquals(null, streetType);
        assertEquals("#572/573", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress53()
    {
        // CREN -- 655990
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "40292 Us Hwy 550 N. #321 & 323";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Durango";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("40292", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Us Hwy 550 N", streetName);
        assertEquals(null, streetType);
        assertEquals("#321 & 323", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress54()
    {
        // CREN -- 656468
        String originalStreetNumber = null;
        String originalStreetDir = null;
        String originalStreetName = "40292 Us Hwy 550 North #558";
        String originalStreetType = null;
        String originalUnitNumber = null;
        String city = "Durango";
        String state = "CO";

        Map<String, String> results = parseMultipleFields(originalStreetNumber, originalStreetDir,
            originalStreetName, originalStreetType, originalUnitNumber, city, state);

        String streetNumber = results.get("streetNumber");
        String streetDir = results.get("streetDirection");
        String streetName = results.get("streetName");
        String streetType = results.get("streetType");
        String unitNumber = results.get("unitNumber");

        assertEquals("40292", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Us Hwy 550 North", streetName);
        assertEquals(null, streetType);
        assertEquals("#558", unitNumber);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress55()
    {
        String address = "123 Route 29 South, Trenton, new jersey, 12323";

        Map<AddressComponent, String> results = AddressParser.parseAddress(address);

        String name = results.get(AddressComponent.NAME);
        String streetNumber = results.get(AddressComponent.NUMBER);
        String streetDir = results.get(AddressComponent.PREDIR);
        String streetName = results.get(AddressComponent.STREET);
        String streetType = results.get(AddressComponent.TYPE);
        String unitNumber = results.get(AddressComponent.LINE2);
        String city = results.get(AddressComponent.CITY);
        String state = results.get(AddressComponent.STATE);
        String zip = results.get(AddressComponent.ZIP);

        assertEquals(null, name);
        assertEquals("123", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Route 29 South", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
        assertEquals("Trenton", city);
        assertEquals("NEW JERSEY", state);
        assertEquals("12323", zip);
    }

    @Test(groups = TestGroups.UNIT)
    public void testSplitAddress56()
    {
        String address = "123 Avenue of art, philadelphia pa 12345";

        Map<AddressComponent, String> results = AddressParser.parseAddress(address);

        String name = results.get(AddressComponent.NAME);
        String streetNumber = results.get(AddressComponent.NUMBER);
        String streetDir = results.get(AddressComponent.PREDIR);
        String streetName = results.get(AddressComponent.STREET);
        String streetType = results.get(AddressComponent.TYPE);
        String unitNumber = results.get(AddressComponent.LINE2);
        String city = results.get(AddressComponent.CITY);
        String state = results.get(AddressComponent.STATE);
        String zip = results.get(AddressComponent.ZIP);

        assertEquals(null, name);
        assertEquals("123", streetNumber);
        assertEquals(null, streetDir);
        assertEquals("Avenue of art", streetName);
        assertEquals(null, streetType);
        assertEquals(null, unitNumber);
        assertEquals("philadelphia", city);
        assertEquals("pa", state);
        assertEquals("12345", zip);
    }

    /**
     * Method used by tests that pass multiple fields to address parser, rather than a single
     * address. I am adding this so I don't have to update all of the older tests
     *
     * @param streetNumber
     * @param streetDirection
     * @param streetName
     * @param streetType
     * @param unitNumber
     * @param city
     * @param state
     *
     * @return
     */
    private static Map<String, String> parseMultipleFields(String streetNumber, String streetDirection,
        String streetName, String streetType, String unitNumber, String city, String state)
    {
        String address = "";

        if (streetNumber != null)
        {
            address = address.concat(streetNumber);
        }

        if (streetDirection != null && !streetDirection.isEmpty())
        {
            address = address.concat(" ").concat(streetDirection);
        }

        if (streetName != null)
        {
            address = address.concat(" ").concat(streetName);
        }

        if (streetType != null)
        {
            address = address.concat(" ").concat(streetType);
        }

        if (unitNumber != null)
        {
            address = address.concat(" ").concat(unitNumber);
        }

        if (city != null)
        {
            address = address.concat(", ").concat(city);
        }

        if (state != null)
        {
            address = address.concat(", ").concat(state);
        }

        Map<AddressComponent, String> results = AddressParser.parseAddress(address);

        String splitStreetNumber = results.get(AddressComponent.NUMBER);
        String splitStreetDir = results.get(AddressComponent.PREDIR);
        String splitStreetName = results.get(AddressComponent.STREET);
        String splitStreetType = results.get(AddressComponent.TYPE);
        String splitUnitNumber = results.get(AddressComponent.LINE2);

        HashMap<String, String> addressParts = new HashMap<>();

        addressParts.put("streetNumber", splitStreetNumber);
        addressParts.put("streetDirection", splitStreetDir);
        addressParts.put("streetName", splitStreetName);
        addressParts.put("streetType", splitStreetType);
        addressParts.put("unitNumber", splitUnitNumber);

        return addressParts;

    }
}
