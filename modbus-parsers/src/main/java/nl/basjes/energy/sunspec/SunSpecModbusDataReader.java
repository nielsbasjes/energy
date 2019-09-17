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
import com.ghgande.j2mod.modbus.facade.AbstractModbusMaster;
import nl.basjes.modbus.ModBusDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class SunSpecModbusDataReader extends ModBusDataReader {
    private static final Logger LOG = LoggerFactory.getLogger(SunSpecModbusDataReader.class);

    private Map<Integer, ModelLocation> modelLocations;

    public SunSpecModbusDataReader(AbstractModbusMaster master) throws ModbusException {
        this(master, 40000, 126);
    }

    public SunSpecModbusDataReader(AbstractModbusMaster master, int startBase) throws ModbusException {
        this(master, startBase, 126);
    }

    public SunSpecModbusDataReader(AbstractModbusMaster master, int startBase, int deviceId) throws ModbusException {
        super(master, deviceId);
        try {
            connect();
        } catch (Exception e) {
            throw new ModbusException("Unable to connect to slave.", e);
        }

        modelLocations = getModelLocations(startBase);
    }

    public Map<Integer, ModelLocation> getModelLocations() {
        return modelLocations;
    }

    public ModelLocation getModelLocation(int model) {
        return modelLocations.get(model);
    }

    public static class ModelLocation {
        public int id;
        public int registerBase;
        public int len;

        public ModelLocation(int id, int registerBase, int len) {
            this.id = id;
            this.registerBase = registerBase;
            this.len = len;
        }

        @Override
        public String toString() {
            return "ModelLocation{" +
                "id=" + id +
                ", registerBase=" + registerBase +
                ", len=" + len +
                '}';
        }
    }

    public byte[] getRawModel(SunSpecModbusDataReader.ModelLocation modelLocation) throws ModbusException {
        return getRawRegisterBytes(modelLocation.registerBase, modelLocation.len);
    }

    public Map<Integer, ModelLocation> getModelLocations(int startBase) throws ModbusException {
        // SunSpec header
        int base     = startBase;
        String sunsMarker = readASCII(base, 2);

        if (!"SunS".equals(sunsMarker)) {
            LOG.error("The SunSpec header was missing at register address {}", startBase);
            return Collections.emptyMap();
        }
        base += 2; // The length in registers of the 'SunS' prefix.

        Map<Integer, ModelLocation> blocks = new TreeMap<>();

        Integer blockId;
        Integer blockLen;
        while (true) {
            // Read the header of the block
            blockId = readU16(base);
            if (blockId == null) {
                break;
            }
            blockLen = readU16(base + 1);
            base = base + 2;
            blocks.put(blockId, new ModelLocation(blockId, base, blockLen));

            // Now skip the actual block
            base += blockLen;
        }

        return blocks;
    }

}
