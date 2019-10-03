/*
 * Energy readers and parsers toolkit
 * Copyright (C) 2019-2019 Niels Basjes
 *
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://creativecommons.org/licenses/by-nc-nd/4.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package nl.basjes.energy.sunspec;

import nl.basjes.energy.RunProcessImageAsModbusTCPSlave;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static nl.basjes.energy.sunspec.FetchSunSpec.FETCH_INTERVAL;
import static nl.basjes.energy.sunspec.FetchSunSpec.HOSTNAME;
import static nl.basjes.energy.sunspec.FetchSunSpec.PORT;
import static nl.basjes.energy.sunspec.FetchSunSpec.SUCCESS;
import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_STARTBASE;
import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_UNITID;
import static org.junit.Assert.assertEquals;


public class FetchSunSpecTest extends RunProcessImageAsModbusTCPSlave {

    private TestRunner runner;

    private static final Logger LOG  = LoggerFactory.getLogger(FetchSunSpecTest.class);

    @BeforeClass
    public static void startTestSlave() throws Exception {
        startTestSlave(SunSpecTestProcessImage.class, SUNSPEC_STANDARD_STARTBASE, SUNSPEC_STANDARD_UNITID);
    }

    @Before
    public void init() {
        runner = TestRunners.newTestRunner(FetchSunSpec.class);
    }

    @Test
    public void testProcessor() {
        runner.setProperty(HOSTNAME, getHost());
        runner.setProperty(PORT,     String.valueOf(getTestport()));
        runner.setProperty(FETCH_INTERVAL, "1000");

        // Run the enqueued content, it also takes an int = number of contents queued
        runner.run(1);

        // All results were processed with out failure
        runner.assertQueueEmpty();

        // If you need to read or do additional tests on results you can access the content
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(SUCCESS);
        assertEquals("1 match", 1, results.size());
        results.get(0).assertAttributeEquals("SunSpec_1___Manufacturer_",  "SunSpecText");
        results.get(0).assertAttributeEquals("SunSpec_1___Version_",       "1.2.3");
        results.get(0).assertAttributeEquals("SunSpec_1___Model_",         "TestInverter");
        results.get(0).assertAttributeEquals("SunSpec_1___SerialNumber_",  "sn-123456789");
        results.get(0).assertAttributeEquals("SunSpec_1___Options_",       "opt_a_b_c");

        results.get(0).getAttributes().forEach((k,v) -> LOG.info("Attribute {} = {}", k, v));

    }

    @Ignore
    @Test
    public void runSlaveForEver() throws InterruptedException {
        LOG.info("Running on {}:{}", getHost(), getTestport());
        Thread.sleep(1000000000000L);
    }

}
