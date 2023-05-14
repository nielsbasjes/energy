/*
 * Energy readers and parsers toolkit
 * Copyright (C) 2019-2023 Niels Basjes
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
import nl.basjes.energy.RunSunSpecProcessImageAsModbusTCPSlave;
import org.junit.BeforeClass;
import org.junit.Test;

import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_STARTBASE;
import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_UNITID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRealDeviceSolarEdge extends RunSunSpecProcessImageAsModbusTCPSlave {
    @BeforeClass
    public static void startTestSlave() throws Exception {
        startTestSlave(SunSpecSolarEdgeProcessImage.class, SUNSPEC_STANDARD_STARTBASE, SUNSPEC_STANDARD_UNITID);
    }

    @Test
    public void verifyTheValues() throws ModbusException, MissingMandatoryFieldException {
        try(SunSpecModbusDataReader dataReader = new SunSpecModbusDataReader(new ModbusTCPMaster(getHost(), getTestport()))) {
            SunSpecFetcher fetcher = new SunSpecFetcher(dataReader).useAllModels();
            fetcher.refresh();

            assertEquals("SolarEdge ", fetcher.model_1.getManufacturer());
            assertEquals("SE3000H-RW000BNN4", fetcher.model_1.getModel());
            assertEquals(509.20, fetcher.model_101.getWatts(), 0.0001);
            assertEquals(38.14, fetcher.model_101.getHeatSinkTemperature(), 0.0001);
        }
    }

}
