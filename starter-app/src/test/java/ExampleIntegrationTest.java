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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import com.i2rd.unit.test.TestGroups;

/**
 * This tests nothing.
 * Used for pipeline build testing/configuration.
 * @author Russ Tennant (russ@i2rd.com)
 * @since 5/28/16 1:18 PM
 */
public class ExampleIntegrationTest //extends BaseTest
{
    /** Logger. */
    private static final Logger _logger = LogManager.getLogger(ExampleIntegrationTest.class);

    @Test(groups = TestGroups.INTEGRATION)
    public void testTheIntegrationThing()
    {
        try
        {
            Thread.sleep(1_000);
        }
        catch (InterruptedException e)
        {
            _logger.error("Yawn!", e);
        }
    }

    @Test(groups = TestGroups.INTEGRATION)
    public void testTheOtherIntegrationThing()
    {
        try
        {
            Thread.sleep(1_000);
        }
        catch (InterruptedException e)
        {
            _logger.error("Yawn!", e);
        }
    }

}
