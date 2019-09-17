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

import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;

import static nl.basjes.energy.sunspec.FetchSunSpec.FETCH_INTERVAL;
import static nl.basjes.energy.sunspec.FetchSunSpec.HOSTNAME;
import static nl.basjes.energy.sunspec.FetchSunSpec.PORT;
import static nl.basjes.energy.sunspec.FetchSunSpec.SUCCESS;
import static org.junit.Assert.assertEquals;


public class FetchSunSpecTest {

    private TestRunner runner;

    private static final Logger LOG  = LoggerFactory.getLogger(FetchSunSpecTest.class);
    private static final String HOST = InetAddress.getLoopbackAddress().getHostAddress();

    private static int testport;

    @BeforeClass
    public static void startTestSlave() throws Exception {
        // First find a free port.
        ServerSocket serverSocket = new ServerSocket(0);
        testport = serverSocket.getLocalPort();
        serverSocket.close();
        // We assume that between this close and the starting of the slave this port remains free.

        // Create your register set
        ProcessImage image = new SunSpecTestProcessImage(126, 40000);

        LOG.info("SunSpec slave: Starting");
        // Create a slave to listen on port 502 and create a pool of 5 listener threads
        // This will create a new slave or return you the same slave already assigned to this port
        ModbusSlave slave = ModbusSlaveFactory.createTCPSlave(InetAddress.getLoopbackAddress(), testport, 5, false);

        // Add the register set to the slave for unit ID 126
        // Each slave can have multiple process images but they must have a unique Unit ID within the slave
        slave.addProcessImage(126, image);

        // Start the slave listening on the port - this will throw an error if the socket is already in use
        slave.open();
        Thread.sleep(100);
        LOG.info("SunSpec slave: Running on port {}", testport);
    }

    @AfterClass
    public static void stopTestSlave() {
        LOG.info("SunSpec slave: Stopping");
        ModbusSlaveFactory.close();
        LOG.info("SunSpec slave: Stopped");
    }


    @Before
    public void init() {
        runner = TestRunners.newTestRunner(FetchSunSpec.class);
    }

    @Test
    public void testProcessor() {
        runner.setProperty(HOSTNAME, HOST);
        runner.setProperty(PORT,     String.valueOf(testport));
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
        LOG.info("Running on {}:{}", HOST, testport);
        Thread.sleep(1000000000000L);
    }

}
