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

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import nl.basjes.energy.sunspec.SunSpecModbusDataReader.ModelLocation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Map;

public class TestBrokenModelListTerminator {

    private static final Logger LOG = LoggerFactory.getLogger(TestBrokenModelListTerminator.class);
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
        ProcessImage image = new SunSpecBrokenTerminatorProcessImage(126, 40000);

        LOG.info("Starting slave");
        // Create a slave to listen on port 502 and create a pool of 5 listener threads
        // This will create a new slave or return you the same slave already assigned to this port
        ModbusSlave slave = ModbusSlaveFactory.createTCPSlave(InetAddress.getLoopbackAddress(), testport, 5, false);

        // Add the register set to the slave for unit ID 126
        // Each slave can have multiple process images but they must have a unique Unit ID within the slave
        slave.addProcessImage(126, image);

        // Start the slave listening on the port - this will throw an error if the socket is already in use
        slave.open();
        Thread.sleep(100);
        LOG.info("Slave should be running on port {}", testport);
    }

    @AfterClass
    public static void stopTestSlave() {
        LOG.info("Stopping slave");
        ModbusSlaveFactory.close();
        LOG.info("Done");
    }

    @Test
    public void getBlockListTest() throws Exception {
        try(SunSpecModbusDataReader dataReader = new SunSpecModbusDataReader(new ModbusTCPMaster(HOST, testport))) {
            dataReader.connect();

            final Map<Integer, ModelLocation> modelLocations = dataReader.getModelLocations(40000);

            modelLocations.forEach((k,m) -> LOG.info("Model {}: {}", m.id, ParseSunSpec.modelParsers().get(m.id).getDescription()));
        }
    }

    @Test
    public void showAllFields() throws Exception {

        try(SunSpecModbusDataReader dataReader = new SunSpecModbusDataReader(new ModbusTCPMaster(HOST, testport))) {

            SunSpecFetcher fetcher = new SunSpecFetcher(dataReader).useAllModels();

            fetcher.refresh();

            LOG.info("ALl data\n{}", fetcher.toString());
        }
    }
}
