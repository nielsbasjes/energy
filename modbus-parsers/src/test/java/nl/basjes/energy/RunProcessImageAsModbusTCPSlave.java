/*
 * Energy readers and parsers toolkit
 * Copyright (C) 2019-2021 Niels Basjes
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

package nl.basjes.energy;

import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.ServerSocket;

public abstract class RunProcessImageAsModbusTCPSlave {

    private static final Logger LOG = LoggerFactory.getLogger(RunProcessImageAsModbusTCPSlave.class);
    private static final String HOST = InetAddress.getLoopbackAddress().getHostAddress();

    private static int testport;

    public static String getHost() {
        return HOST;
    }

    public static int getTestport() {
        return testport;
    }

    private static Integer registerBase =-1;
    private static Integer unitId       =-1;

    public static void startTestSlave(Class<? extends ProcessImage> processImageClass, int registerBase, int unitId) throws Exception {

        final Constructor<? extends ProcessImage> constructor = processImageClass.getConstructor(Integer.class, Integer.class);
        RunProcessImageAsModbusTCPSlave.registerBase = registerBase;
        RunProcessImageAsModbusTCPSlave.unitId = unitId;
        ProcessImage processImage = constructor.newInstance(registerBase, unitId);

        // First find a free port.
        ServerSocket serverSocket = new ServerSocket(0);
        testport = serverSocket.getLocalPort();
        serverSocket.close();
        // We assume that between this close and the starting of the slave this port remains free.

        LOG.info("Starting slave");
        // Create a slave to listen on port 502 and create a pool of 5 listener threads
        // This will create a new slave or return you the same slave already assigned to this port
        ModbusSlave slave = ModbusSlaveFactory.createTCPSlave(InetAddress.getLoopbackAddress(), testport, 5, false);

        // Add the register set to the slave for unit ID 126
        // Each slave can have multiple process images but they must have a unique Unit ID within the slave
        slave.addProcessImage(unitId, processImage);

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

}
