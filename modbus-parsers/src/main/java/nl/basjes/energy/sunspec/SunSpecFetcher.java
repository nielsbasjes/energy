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

package nl.basjes.energy.sunspec;

import com.ghgande.j2mod.modbus.ModbusException;
import nl.basjes.energy.sunspec.ParseSunSpec.ModelParserHolder;

import java.util.LinkedHashMap;
import java.util.Map;

import static nl.basjes.energy.Utils.waitTillNextTimeModulo;

public class SunSpecFetcher extends ModelParserHolder {
    private SunSpecModbusDataReader    dataReader;
    private Map<Integer, ModelFetcher> modelFetchers;

    public SunSpecFetcher(SunSpecModbusDataReader dataReader) {
        this.dataReader = dataReader;
        modelFetchers = new LinkedHashMap<>();
    }

    private long   currentDataTimestamp = 0;

    public SunSpecFetcher useModel(int modelId) {
        modelFetchers.put(modelId, getModelFetcher(dataReader, modelId));
        return this;
    }

    public SunSpecFetcher useAllModels() {
        dataReader.getModelLocations().forEach((modelId, blockParser) -> modelFetchers.put(modelId, getModelFetcher(dataReader, modelId)));
        return this;
    }

    public void refresh() throws ModbusException {
        currentDataTimestamp = System.currentTimeMillis();
        for (Map.Entry<Integer, ModelFetcher> entry : modelFetchers.entrySet()) {
            entry.getValue().refresh();
        }
    }

    public void refresh(long moduloMs) throws ModbusException {
        currentDataTimestamp = waitTillNextTimeModulo(moduloMs);
        for (Map.Entry<Integer, ModelFetcher> entry : modelFetchers.entrySet()) {
            entry.getValue().refresh();
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


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(10000);
        for (Map.Entry<Integer, ModelFetcher> entry : modelFetchers.entrySet()) {
            sb.append(entry.getValue().toString());
        }
        return sb.toString();
    }

    public Map<String, Object> toHashMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, ModelFetcher> entry : modelFetchers.entrySet()) {
            result.putAll(entry.getValue().toHashMap());
        }
        return result;
    }

}
