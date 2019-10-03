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

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import nl.basjes.energy.RunProcessImageAsModbusTCPSlave;
import nl.basjes.energy.RunSunSpecProcessImageAsModbusTCPSlave;
import nl.basjes.energy.sunspec.SunSpecModbusDataReader.ModelLocation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_STARTBASE;
import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_UNITID;

public class TestRealDeviceSolarEdge extends RunSunSpecProcessImageAsModbusTCPSlave {
    @BeforeClass
    public static void startTestSlave() throws Exception {
        startTestSlave(SunSpecSolarEdgeProcessImage.class, SUNSPEC_STANDARD_STARTBASE, SUNSPEC_STANDARD_UNITID);
    }
}
