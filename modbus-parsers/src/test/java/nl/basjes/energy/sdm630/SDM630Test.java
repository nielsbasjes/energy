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

package nl.basjes.energy.sdm630;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import nl.basjes.energy.RunProcessImageAsModbusTCPSlave;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

public class SDM630Test extends RunProcessImageAsModbusTCPSlave {

    private static final Logger LOG  = LoggerFactory.getLogger(SDM630Test.class);

    @BeforeClass
    public static void startTestSlave() throws Exception {
        startTestSlave(SDM630ProcessImage.class, 0, 1);
    }

    @Test
    public void testReadFromAddress() throws Exception {
        SDM630Reader reader = new SDM630Reader(new ModbusTCPMaster(getHost(), getTestport()), 1);
        reader.connect();

        boolean failed = false;

        for (SDM630Reader.Field field : reader.fields) {
            try {
                float value = reader.readFloat32(field.modbusAddress);
                LOG.info("{}", String.format("%5d (%3d) [%4d] %-40s = %10.2f %-15s  %s", field.address, field.parameterNumber, field.modbusAddress, field.description, value, field.units, field.note).trim());
            } catch (ModbusException me) {
                LOG.error("{}", String.format("%5d (%3d) [%4d] %-40s = --- FAILED --- %s", field.address, field.parameterNumber, field.modbusAddress, field.description, me.getMessage()));
                failed = true;
            }
        }

        reader.disconnect();

        if (failed) {
            fail("Something went wrong");
        }
    }

    private void showFunctionOutput(SDM630Reader.SDM630Values.Value value) {
        LOG.info("{}", String.format("%-40s = %10.2f %-15s", value.name, value.value, value.unit));
    }

    @Test
    public void testReadUsingFunctions() throws Exception {

        SDM630Reader reader = new SDM630Reader(new ModbusTCPMaster(getHost(), getTestport()), 1);

        reader.connect();

        final SDM630Reader.SDM630Values values = reader.read();

        showFunctionOutput(values.getPhase1LineToNeutralVolts());
        showFunctionOutput(values.getPhase2LineToNeutralVolts());
        showFunctionOutput(values.getPhase3LineToNeutralVolts());
        showFunctionOutput(values.getPhase1Current());
        showFunctionOutput(values.getPhase2Current());
        showFunctionOutput(values.getPhase3Current());
        showFunctionOutput(values.getPhase1Power());
        showFunctionOutput(values.getPhase2Power());
        showFunctionOutput(values.getPhase3Power());
        showFunctionOutput(values.getPhase1VoltAmps());
        showFunctionOutput(values.getPhase2VoltAmps());
        showFunctionOutput(values.getPhase3VoltAmps());
        showFunctionOutput(values.getPhase1VoltAmpsReactive());
        showFunctionOutput(values.getPhase2VoltAmpsReactive());
        showFunctionOutput(values.getPhase3VoltAmpsReactive());
        showFunctionOutput(values.getPhase1PowerFactor());
        showFunctionOutput(values.getPhase2PowerFactor());
        showFunctionOutput(values.getPhase3PowerFactor());
        showFunctionOutput(values.getPhase1PhaseAngle());
        showFunctionOutput(values.getPhase2PhaseAngle());
        showFunctionOutput(values.getPhase3PhaseAngle());
        showFunctionOutput(values.getAverageLineToNeutralVolts());
        showFunctionOutput(values.getAverageLineCurrent());
        showFunctionOutput(values.getSumOfLineCurrents());
        showFunctionOutput(values.getTotalSystemPower());
        showFunctionOutput(values.getTotalSystemVoltAmps());
        showFunctionOutput(values.getTotalSystemVAr());
        showFunctionOutput(values.getTotalSystemPowerFactor());
        showFunctionOutput(values.getTotalSystemPhaseAngle());
        showFunctionOutput(values.getFrequencyOfSupplyVoltages());
        showFunctionOutput(values.getImportWhSinceLastReset());
        showFunctionOutput(values.getExportWhSinceLastReset());
        showFunctionOutput(values.getImportVArhSinceLastReset());
        showFunctionOutput(values.getExportVArhSinceLastReset());
        showFunctionOutput(values.getVAhSinceLastReset());
        showFunctionOutput(values.getAhSinceLastReset());
        showFunctionOutput(values.getTotalSystemPowerDemand());
        showFunctionOutput(values.getMaximumTotalSystemPowerDemand());
        showFunctionOutput(values.getTotalSystemVADemand());
        showFunctionOutput(values.getMaximumTotalVASystemDemand());
        showFunctionOutput(values.getNeutralCurrentDemand());
        showFunctionOutput(values.getMaximumNeutralCurrentDemand());
        showFunctionOutput(values.getLine1ToLine2Volts());
        showFunctionOutput(values.getLine2ToLine3Volts());
        showFunctionOutput(values.getLine3ToLine1Volts());
        showFunctionOutput(values.getAverageLineToLineVolts());
        showFunctionOutput(values.getNeutralCurrent());
        showFunctionOutput(values.getPhase1L_NVoltsTHD());
        showFunctionOutput(values.getPhase2L_NVoltsTHD());
        showFunctionOutput(values.getPhase3L_NVoltsTHD());
        showFunctionOutput(values.getPhase1CurrentTHD());
        showFunctionOutput(values.getPhase2CurrentTHD());
        showFunctionOutput(values.getPhase3CurrentTHD());
        showFunctionOutput(values.getAverageLineToNeutralVoltsTHD());
        showFunctionOutput(values.getAverageLineCurrentTHD());
        showFunctionOutput(values.getTotalSystemPowerFactorDegrees());
        showFunctionOutput(values.getPhase1CurrentDemand());
        showFunctionOutput(values.getPhase2CurrentDemand());
        showFunctionOutput(values.getPhase3CurrentDemand());
        showFunctionOutput(values.getMaximumPhase1CurrentDemand());
        showFunctionOutput(values.getMaximumPhase2CurrentDemand());
        showFunctionOutput(values.getMaximumPhase3CurrentDemand());
        showFunctionOutput(values.getLine1ToLine2VoltsTHD());
        showFunctionOutput(values.getLine2ToLine3VoltsTHD());
        showFunctionOutput(values.getLine3ToLine1VoltsTHD());
        showFunctionOutput(values.getAverageLineToLineVoltsTHD());
        showFunctionOutput(values.getTotalKWh());
        showFunctionOutput(values.getTotalKVArh());
        showFunctionOutput(values.getL1ImportKWh());
        showFunctionOutput(values.getL2ImportKWh());
        showFunctionOutput(values.getL3ImportKWh());
        showFunctionOutput(values.getL1ExportKWh());
        showFunctionOutput(values.getL2ExportKWh());
        showFunctionOutput(values.getL3ExportKWh());
        showFunctionOutput(values.getL1TotalKWh());
        showFunctionOutput(values.getL2TotalKWh());
        showFunctionOutput(values.getL3TotalKWh());
        showFunctionOutput(values.getL1ImportKVArh());
        showFunctionOutput(values.getL2ImportKVArh());
        showFunctionOutput(values.getL3ImportKVArh());
        showFunctionOutput(values.getL1ExportKVArh());
        showFunctionOutput(values.getL2ExportKVArh());
        showFunctionOutput(values.getL3ExportKVArh());
        showFunctionOutput(values.getL1TotalKVArh());
        showFunctionOutput(values.getL2TotalKVArh());
        showFunctionOutput(values.getL3TotalKVArh());

        reader.disconnect();
    }

    @Test
    public void testReadUsingToMap() throws Exception {
        try (SDM630Reader reader = new SDM630Reader(new ModbusTCPMaster(getHost(), getTestport()), 1)) {
            final SDM630Reader.SDM630Values values = reader.read();
            values.toMap().forEach((k, v) -> LOG.info("{} = {}", k, v));
        }
    }

    @Test
    public void testReadUsingToString() throws Exception {
        try(SDM630Reader reader = new SDM630Reader(new ModbusTCPMaster(getHost(), getTestport()), 1)) {
            LOG.info("\n{}", reader.read().toString());
        }
    }
}
