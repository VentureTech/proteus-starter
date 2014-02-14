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

package com.example.app.config.automation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.proteusframework.core.automation.AbstractDataConversion;
import net.proteusframework.core.automation.DataConversion;
import net.proteusframework.core.automation.SQLDataConversion;
import net.proteusframework.core.automation.SQLStatement;
import net.proteusframework.core.automation.TaskArgument;
import net.proteusframework.core.automation.TaskQualifier;

// You can also put your code in the groovy source folder so you can more easily create
//  mult-line string literals. Don't duplicate the package-info.java though - only create one
//  for the same package.

/**
 * Data conversions for major version 1 of example app module
 * in the Proteus Framework project.
 *
 * @author Russ Tennant (russ@i2rd.com)
 */
@Configuration
//@Profile({"automation", "com.example.1"})
@Lazy
public class ExampleAppDataConversionVersion1
{
    // !!! PLEASE PUT THE LATEST conversion at the stop so it is easy to determine the next version #
    /** Data Conversion Identifier. */
    private static final String IDENTIFIER = "example-app";


    /** Example Task Argument. */
    private static final String WHAT_S_YOUR_ZODIAC_SIGN = "What's your zodiac sign";

    /**
     * Example data conversion.
     * @return data conversion.
     */
    @Bean
    @TaskQualifier(TaskQualifier.Type.data_conversion)
    public DataConversion example2DataConversion()
    {
        final String[] signs = {
            "ARIES", "TAURUS", "GEMINI", "CANCER", "LEO", "VIRGO",
            "LIBRA", "SCORPIO", "SAGITTARIUS", "CAPRICORN",
            "AQUARIUS", "PISCES"
        };
        Arrays.sort(signs);

        final List<SQLStatement> preDDL =
            Collections.singletonList(new SQLStatement("CREATE TABLE Example_FOO(id serial, val text, primary key (id))", null));
        final List<SQLStatement> postDDL =
            Collections.singletonList(new SQLStatement("DROP TABLE Example_FOO;", null));
        AbstractDataConversion dc = new AbstractDataConversion("example", "Example Numero Dos", 2, true, null, preDDL, postDDL)
        {
            @Override
            public List<TaskArgument> getAdditionalArguments()
            {
                List<TaskArgument> args = new ArrayList<>();
                args.add(new TaskArgument(WHAT_S_YOUR_ZODIAC_SIGN, "", "Cancer", null));
                return args;
            }

            @Override
            public List<? extends SQLStatement> execute(TaskArgument[] arguments)
                throws IllegalArgumentException, SQLException, IllegalArgumentException
            {
                final SQLStatement stmt = new SQLStatement("INSERT INTO Example_Foo (val) VALUES (?)",
                    "Insert Sign");
                final Map<String, TaskArgument> argumentMapping = getArgumentMapping(arguments);
                stmt.setString(1, argumentMapping.get(WHAT_S_YOUR_ZODIAC_SIGN).getArgument().toString());
                return Collections.singletonList(stmt);
            }

            @Override
            public Map<TaskArgument, String> validateArguments(TaskArgument... arguments)
            {
                final Map<TaskArgument, String> validationMap = new HashMap<>();
                final String sign = arguments[0].getArgument().toString().toUpperCase();
                final int foundIndex = Arrays.binarySearch(signs, sign);
                if(foundIndex < 0)
                    validationMap.put(arguments[0], "That's not your sign fool.");
                return validationMap;
            }

            @Override
            public List<? extends SQLStatement> getValidationStatements()
            {
                return Collections.singletonList(new SQLStatement("SELECT COUNT(*) FROM Example_FOO", null));
            }

            @Override
            public String validateResult(SQLStatement stmt, int index, ResultSet result) throws SQLException
            {
                if(result.next())
                {
                    long count = result.getLong(1);
                    return count == 1 ? null : "Received incorrect record count: " + count;
                }
                else
                    return "Expecting Results";
            }
        };
        return dc;
    }


    /**
     * Example data conversion.
     * @return data conversion.
     */
    @Bean
    @TaskQualifier(TaskQualifier.Type.data_conversion)
    public DataConversion example1DataConversion()
    {
        return new SQLDataConversion("example", "Example of a data conversion", 1,
            Arrays.asList(
                new SQLStatement("SELECT 1", "This data conversion is just an example. It does not do anything.")
            ));
    }

}
