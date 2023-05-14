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
import nl.basjes.energy.sunspec.SunSpecModbusDataReader.ModelLocation;

import java.util.Collections;
import java.util.Map;

public abstract class ModelFetcher extends ModelParser {

    private SunSpecModbusDataReader               dataReader;
    private ModelLocation modelLocation;

    public ModelFetcher(SunSpecModbusDataReader dataReader, int model) {
        this.dataReader = dataReader;
        if (dataReader != null) {
            this.modelLocation = dataReader.getModelLocation(model);
            if (modelLocation == null) {
                throw new UnsupportedOperationException("The device does not support SunSpec model " + model);
            }
        }
    }

    private long   currentDataTimestamp = 0;
    private byte[] currentData = null;

    public void refresh() throws ModbusException {
        if (dataReader == null) {
            throw new ModbusException("No Modbus connection available");
        }
        try {
            dataReader.connect();
            currentData = dataReader.getRawModel(modelLocation);
            currentDataTimestamp = System.currentTimeMillis();
        } catch (Exception e) {
            throw new ModbusException("Unable to refresh the data", e);
        }
    }

    public long getCurrentDataTimestamp() throws ModbusException {
        if (dataReader == null) {
            throw new ModbusException("No Modbus connection available");
        }
        if (currentDataTimestamp == 0) {
            refresh();
        }
        return currentDataTimestamp;
    }

    protected byte[] getCurrentData() throws ModbusException {
        if (dataReader == null) {
            throw new ModbusException("No Modbus connection available");
        }
        if (currentData == null) {
            refresh();
        }
        return currentData;
    }

    @Override
    public String toString() {
        try {
            return toString(getCurrentData());
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> toHashMap() {
        try {
            return toHashMap(getCurrentData());
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

}
