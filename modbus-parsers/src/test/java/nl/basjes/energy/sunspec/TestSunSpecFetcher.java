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

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import nl.basjes.energy.RunProcessImageAsModbusTCPSlave;
import nl.basjes.energy.sunspec.SunSpecModbusDataReader.ModelLocation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_STARTBASE;
import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_UNITID;
import static org.junit.Assert.assertEquals;

public class TestSunSpecFetcher extends RunProcessImageAsModbusTCPSlave {

    private static final Logger LOG = LoggerFactory.getLogger(TestSunSpecFetcher.class);

    @BeforeClass
    public static void startTestSlave() throws Exception {
        startTestSlave(SunSpecTestProcessImage.class, SUNSPEC_STANDARD_STARTBASE, SUNSPEC_STANDARD_UNITID);
    }

    @Test
    public void getBlockListTest() throws Exception {
        try(SunSpecModbusDataReader dataReader = new SunSpecModbusDataReader(new ModbusTCPMaster(getHost(), getTestport()))) {
            dataReader.connect();

            final Map<Integer, SunSpecModbusDataReader.ModelLocation> blockLocations = dataReader.getModelLocations(SUNSPEC_STANDARD_STARTBASE);

            int[][] expected = {
                {   1, 40004,  65 },
                { 101, 40071,  50 },
                { 120, 40123,  26 },
                { 121, 40151,  30 },
                { 122, 40183,  44 },
                { 123, 40229,  24 },
                { 126, 40255, 226 },
                { 131, 40483, 226 },
                { 132, 40711, 226 },
            };

            int exp = 0;
            for (Map.Entry<Integer, ModelLocation> entry : blockLocations.entrySet()) {
                Integer                               i = entry.getKey();
                SunSpecModbusDataReader.ModelLocation b = entry.getValue();
                assertEquals((int)i, b.id);
                assertEquals(expected[exp][0], b.id);
                assertEquals(expected[exp][1], b.registerBase);
                assertEquals(expected[exp][2], b.len);
                exp++;
            }
        }
    }

    @Test
    public void sunSpecFetcherTest() throws Exception {

        try(SunSpecModbusDataReader dataReader = new SunSpecModbusDataReader(new ModbusTCPMaster(getHost(), getTestport()))) {

            SunSpecFetcher fetcher = new SunSpecFetcher(dataReader)
                .useModel(1)
                .useModel(101)
                .useModel(132); // Which has a repeating block

            fetcher.refresh();

            final Map<String, Object> result = fetcher.toHashMap();

//            result.forEach((k,v) -> System.out.println("assertEquals("+v.toString()+",        result.get(\""+k+"\"));"));

            assertEquals("SunSpecText",        result.get("1|-|Manufacturer|"));
            assertEquals("TestInverter",       result.get("1|-|Model|"));
            assertEquals("opt_a_b_c",          result.get("1|-|Options|"));
            assertEquals("1.2.3",              result.get("1|-|Version|"));
            assertEquals("sn-123456789",       result.get("1|-|SerialNumber|"));
            assertEquals(1,                    result.get("1|-|DeviceAddress|"));
            assertEquals(5.49,                 (Double)result.get("101|-|Amps|A"),               0.01);
            assertEquals(5.49,                 (Double)result.get("101|-|AmpsPhaseA|A"),         0.01);
            assertEquals(243.0,                (Double)result.get("101|-|PhaseVoltageAN|V"),     0.01);
            assertEquals(1307.0,               (Double)result.get("101|-|Watts|W"),              0.01);
            assertEquals(60.01,                (Double)result.get("101|-|Hz|Hz"),                0.01);
            assertEquals(1334.0,               (Double)result.get("101|-|VA|VA"),                0.01);
            assertEquals(267.0,                (Double)result.get("101|-|VAr|var"),              0.01);
            assertEquals(97.9,                 (Double)result.get("101|-|PF|Pct"),               0.01);
            assertEquals(1720967.0,            (Double)result.get("101|-|WattHours|Wh"),         0.01);
            assertEquals(3.8000,               (Double)result.get("101|-|DCAmps|A"),             0.01);
            assertEquals(350.0,                (Double)result.get("101|-|DCVoltage|V"),          0.01);
            assertEquals(1330.0,               (Double)result.get("101|-|DCWatts|W"),            0.01);
            assertEquals(0.0,                  (Double)result.get("101|-|CabinetTemperature|C"), 0.01);
            assertEquals("MPPT",               result.get("101|-|OperatingState|").toString());
            assertEquals(4,                    result.get("101|-|VendorOperatingState|"));
            assertEquals("[]",                 result.get("101|-|Event1|").toString());
            assertEquals(0L,                   result.get("101|-|EventBitfield2|"));
            assertEquals(0L,                   result.get("101|-|VendorEventBitfield1|"));
            assertEquals(0L,                   result.get("101|-|VendorEventBitfield2|"));
            assertEquals(0L,                   result.get("101|-|VendorEventBitfield3|"));
            assertEquals(0L,                   result.get("101|-|VendorEventBitfield4|"));
        }
    }


    @Test(expected = UnsupportedOperationException.class)
    public void requestUnsupportedModel() throws Exception {
        SunSpecModbusDataReader dataReader = new SunSpecModbusDataReader(new ModbusTCPMaster(getHost(), getTestport()));

        new SunSpecFetcher(dataReader)
            .useModel(1)
            .useModel(63001);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void requestNonExistendModel() throws Exception {
        SunSpecModbusDataReader dataReader = new SunSpecModbusDataReader(new ModbusTCPMaster(getHost(), getTestport()));

        new SunSpecFetcher(dataReader)
            .useModel(1)
            .useModel(123456);
    }

}
