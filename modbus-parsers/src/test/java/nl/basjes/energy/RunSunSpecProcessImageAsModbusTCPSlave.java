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

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import nl.basjes.energy.sunspec.ParseSunSpec;
import nl.basjes.energy.sunspec.SunSpecFetcher;
import nl.basjes.energy.sunspec.SunSpecModbusDataReader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_STARTBASE;
import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_UNITID;

public class RunSunSpecProcessImageAsModbusTCPSlave extends RunProcessImageAsModbusTCPSlave {

    private static final Logger LOG = LoggerFactory.getLogger(RunSunSpecProcessImageAsModbusTCPSlave.class);



    @Test
    public void getBlockListTest() throws Exception {
        try(SunSpecModbusDataReader dataReader = new SunSpecModbusDataReader(
            new ModbusTCPMaster(getHost(), getTestport()), SUNSPEC_STANDARD_STARTBASE, SUNSPEC_STANDARD_UNITID)) {
            dataReader.connect();
            final Map<Integer, SunSpecModbusDataReader.ModelLocation> modelLocations = dataReader.getModelLocations(SUNSPEC_STANDARD_STARTBASE);
            modelLocations.forEach((k,m) -> LOG.info("Model {}: {}", m.id, ParseSunSpec.modelParsers().get(m.id).getDescription()));
        }
        catch (ModbusException me) {
            LOG.error("{}", me.getMessage());
        }
    }

    @Test
    public void showAllFields() throws Exception {
        try(SunSpecModbusDataReader dataReader = new SunSpecModbusDataReader(new ModbusTCPMaster(getHost(), getTestport()))) {
            SunSpecFetcher fetcher = new SunSpecFetcher(dataReader).useAllModels();
            fetcher.refresh();
            LOG.info("ALl data\n{}", fetcher.toString());
        }
    }
}
