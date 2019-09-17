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
package nl.basjes.energy.sdm630;

import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import com.ghgande.j2mod.modbus.util.SerialParameters;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.fazecast.jSerialComm.SerialPort.getCommPorts;
import static nl.basjes.energy.sdm630.FetchSDM630.BAUD_RATE;
import static nl.basjes.energy.sdm630.FetchSDM630.OPEN_DELAY;
import static nl.basjes.energy.sdm630.FetchSDM630.PORT_NAME;
import static nl.basjes.energy.sdm630.FetchSDM630.SUCCESS;
import static nl.basjes.energy.sdm630.FetchSDM630.UNIT_ID;
import static org.junit.Assert.assertEquals;

public class FetchSDM630Test {

    private TestRunner runner;

    private static boolean canRunTests = true;

    private static final Logger LOG  = LoggerFactory.getLogger(FetchSDM630Test.class);

    private static final String SERIAL_TEST_DEVICE = "/dev/ttyS30";

    @BeforeClass
    public static void startTestSlave() throws Exception {
        final SerialPort[] commPorts = getCommPorts();
        LOG.info("Available serial ports: {}", Arrays.asList(commPorts));
        if (commPorts.length == 0) {
            LOG.error("=======================================================");
            LOG.error("UNABLE TO RUN ANY SERIAL PORT RELATED TESTS!");
            LOG.error("Usual cause: The user running the tests does not have access to any serial ports.");
            LOG.error("See https://github.com/Fazecast/jSerialComm/wiki/Troubleshooting");
            LOG.error("=======================================================");
            canRunTests = false;
            return;
        }

        // Create your register set
        ProcessImage image1 = new SDM630ProcessImage(1, 0);
        ProcessImage image2 = new SDM630ProcessImage(2, 0);
        ProcessImage image3 = new SDM630ProcessImage(3, 0);

        LOG.info("SunSpec slave: Starting");
        // Create a slave to listen on port 502 and create a pool of 5 listener threads
        // This will create a new slave or return you the same slave already assigned to this port
        // Create a serial slave
        SerialParameters parameters = new SerialParameters();
        parameters.setPortName(SERIAL_TEST_DEVICE);
        parameters.setOpenDelay(1000);
        parameters.setEncoding(Modbus.SERIAL_ENCODING_RTU);
        ModbusSlave slave = ModbusSlaveFactory.createSerialSlave(parameters);

        // Add the register set to the slave for unit ID 126
        // Each slave can have multiple process images but they must have a unique Unit ID within the slave
        slave.addProcessImage(1, image1);
        slave.addProcessImage(2, image2);
        slave.addProcessImage(3, image3);

        // Start the slave listening on the port - this will throw an error if the socket is already in use
        slave.open();
        Thread.sleep(100);
        LOG.info("SDM630 slave: Running on device {}", SERIAL_TEST_DEVICE);
    }

    @AfterClass
    public static void stopTestSlave() {
        LOG.info("SunSpec slave: Stopping");
        ModbusSlaveFactory.close();
        LOG.info("SunSpec slave: Stopped");
    }


    @Before
    public void init() {
        runner = TestRunners.newTestRunner(FetchSDM630.class);
    }

    @Test
    public void testProcessor() {
        if (!canRunTests) {
            LOG.error("Skipping tests because no serial ports are available");
            return;
        }

        runner.setProperty(PORT_NAME,            SERIAL_TEST_DEVICE);
//        runner.setProperty(BAUD_RATE,            "38400"     );
//        runner.setProperty(FLOW_CONTROL_IN,                     );
//        runner.setProperty(FLOW_CONTROL_OUT,                    );
//        runner.setProperty(DATA_BITS,                           );
//        runner.setProperty(STOP_BITS,                           );
//        runner.setProperty(PARITY,                              );
        runner.setProperty(OPEN_DELAY,                   "100"       );
//        runner.setProperty(ENCODING,                            );
//        runner.setProperty(UNIT_ID, "1,2,3");
        runner.setProperty(UNIT_ID, "1");

        // Run the enqueued content, it also takes an int = number of contents queued
        runner.run(1);

        // All results were processed with out failure
        runner.assertQueueEmpty();

        // If you need to read or do additional tests on results you can access the content
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(SUCCESS);
        assertEquals("1 match", 1, results.size());

        results.get(0).assertAttributeEquals("SDM630_Phase_1_line_to_neutral_volts_Volts",  "236.00955"      );
        results.get(0).assertAttributeEquals("SDM630_Phase_2_line_to_neutral_volts_Volts",  "234.70023"      );
        results.get(0).assertAttributeEquals("SDM630_Phase_3_line_to_neutral_volts_Volts",  "236.17432"      );
        results.get(0).assertAttributeEquals("SDM630_Phase_1_current_Amps",                 "0.0"            );
        results.get(0).assertAttributeEquals("SDM630_Phase_2_current_Amps",                 "0.0"            );
        results.get(0).assertAttributeEquals("SDM630_Phase_3_current_Amps",                 "0.79662913"     );
        results.get(0).assertAttributeEquals("SDM630_Phase_1_power_Watts",                  "0.0"            );
        results.get(0).assertAttributeEquals("SDM630_Phase_2_power_Watts",                  "0.0"            );
        results.get(0).assertAttributeEquals("SDM630_Phase_3_power_Watts",                  "62.625095"      );

        results.get(0).getAttributes().forEach((k,v) -> LOG.info("Attribute {} = {}", k, v));

    }

    @Ignore
    @Test
    public void runSlaveForEver() throws InterruptedException {
        LOG.info("Running on {}", SERIAL_TEST_DEVICE);
        Thread.sleep(1000000000000L);
    }

}
