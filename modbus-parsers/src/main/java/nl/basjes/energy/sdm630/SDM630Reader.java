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

package nl.basjes.energy.sdm630;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.AbstractModbusMaster;
import nl.basjes.modbus.ModBusDataReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static nl.basjes.energy.Utils.waitTillNextTimeModulo;

public class SDM630Reader extends ModBusDataReader {

    public static class Field {
        public int address;
        public int parameterNumber;
        public String description;
        public String note;
        public String units;
        public int modbusAddress;

        public Field(int address, int parameterNumber, String description, String note, String units, int modbusAddressHi, int modbusAddressLo) {
            this.address = address;
            this.parameterNumber = parameterNumber;
            this.description = description;
            this.note = note;
            this.units = units;
            this.modbusAddress = modbusAddressHi * 256 + modbusAddressLo;
        }

        @Override
        public String toString() {
            return String.format("%5d (%3d) [%4d] %-40s in %s       %s", address, parameterNumber, modbusAddress, description, units, note);
        }
    }

    public List<Field> fields = new ArrayList<>();

    public SDM630Reader(AbstractModbusMaster master, int unitId) {
        super(master, unitId);

        // See http://www.eastroneurope.com/media/_system/tech_specs/3924/SDM630%20Modbus-Protocol.pdf
        setMaxRegistersPerModbusRequest(80);

        String Note_1 = "(1): The power factor has its sign adjusted to indicate the nature of the load. Positive for capacitive and negative for inductive.";
        String Note_2 = "(2): There is a user option to select either k or M for the energy prefix.";
        String Note_3 = "(3): The same user option as in 2 above gives a prefix of None or k for Amp hours";
        String Note_4 = "(4): The power sum demand calculation is for import power only";
        String Note_5 = "(5): The negative total system power factor is a sign inverted version of parameter 32, the magnitude is the same as parameter 32.";
        // String Note_6 = "(6): There is a user option to select None, k or M for the energy prefix.";

        fields.add(new Field(30001,    1, "Phase 1 line to neutral volts",      "",          "Volts",        0x00, 0x00));
        fields.add(new Field(30003,    2, "Phase 2 line to neutral volts",      "",          "Volts",        0x00, 0x02));
        fields.add(new Field(30005,    3, "Phase 3 line to neutral volts",      "",          "Volts",        0x00, 0x04));
        fields.add(new Field(30007,    4, "Phase 1 current",                    "",          "Amps",         0x00, 0x06));
        fields.add(new Field(30009,    5, "Phase 2 current",                    "",          "Amps",         0x00, 0x08));
        fields.add(new Field(30011,    6, "Phase 3 current",                    "",          "Amps",         0x00, 0x0A));
        fields.add(new Field(30013,    7, "Phase 1 power",                      "",          "Watts",        0x00, 0x0C));
        fields.add(new Field(30015,    8, "Phase 2 power",                      "",          "Watts",        0x00, 0x0E));
        fields.add(new Field(30017,    9, "Phase 3 power",                      "",          "Watts",        0x00, 0x10));
        fields.add(new Field(30019,   10, "Phase 1 volt amps",                  "",          "VA",           0x00, 0x12));
        fields.add(new Field(30021,   11, "Phase 2 volt amps",                  "",          "VA",           0x00, 0x14));
        fields.add(new Field(30023,   12, "Phase 3 volt amps",                  "",          "VA",           0x00, 0x16));
        fields.add(new Field(30025,   13, "Phase 1 volt amps reactive",         "",          "VAr",          0x00, 0x18));
        fields.add(new Field(30027,   14, "Phase 2 volt amps reactive",         "",          "VAr",          0x00, 0x1A));
        fields.add(new Field(30029,   15, "Phase 3 volt amps reactive",         "",          "VAr",          0x00, 0x1C));
        fields.add(new Field(30031,   16, "Phase 1 power factor",               Note_1,      "",             0x00, 0x1E));
        fields.add(new Field(30033,   17, "Phase 2 power factor",               Note_1,      "",             0x00, 0x20));
        fields.add(new Field(30035,   18, "Phase 3 power factor",               Note_1,      "",             0x00, 0x22));
        fields.add(new Field(30037,   19, "Phase 1 phase angle",                "",          "Degrees",      0x00, 0x24));
        fields.add(new Field(30039,   20, "Phase 2 phase angle",                "",          "Degrees",      0x00, 0x26));
        fields.add(new Field(30041,   21, "Phase 3 phase angle",                "",          "Degrees",      0x00, 0x28));
        fields.add(new Field(30043,   22, "Average line to neutral volts",      "",          "Volts",        0x00, 0x2A));
        fields.add(new Field(30047,   24, "Average line current",               "",          "Amps",         0x00, 0x2E));
        fields.add(new Field(30049,   25, "Sum of line currents",               "",          "Amps",         0x00, 0x30));
        fields.add(new Field(30053,   27, "Total system power",                 "",          "Watts",        0x00, 0x34));
        fields.add(new Field(30057,   29, "Total system volt amps",             "",          "VA",           0x00, 0x38));
        fields.add(new Field(30061,   31, "Total system VAr",                   "",          "VAr",          0x00, 0x3C));
        fields.add(new Field(30063,   32, "Total system power factor",          Note_1,      "",             0x00, 0x3E));
        fields.add(new Field(30067,   34, "Total system phase angle",           "",          "Degrees",      0x00, 0x42));
        fields.add(new Field(30071,   36, "Frequency of supply voltages",       "",          "Hz",           0x00, 0x46));
        fields.add(new Field(30073,   37, "Import Wh since last reset",         Note_2,      "kWh/MWh",      0x00, 0x48));
        fields.add(new Field(30075,   38, "Export Wh since last reset",         Note_2,      "kWH/MWh",      0x00, 0x4A));
        fields.add(new Field(30077,   39, "Import VArh since last reset",       Note_2,      "kVArh/MVArh",  0x00, 0x4C));
        fields.add(new Field(30079,   40, "Export VArh since last reset",       Note_2,      "kVArh/MVArh",  0x00, 0x4e));
        fields.add(new Field(30081,   41, "VAh since last reset",               Note_2,      "kVAh/MVAh",    0x00, 0x50));
        fields.add(new Field(30083,   42, "Ah since last reset",                Note_3,      "Ah/kAh",       0x00, 0x52));
        fields.add(new Field(30085,   43, "Total system power demand",          Note_4,      "W",            0x00, 0x54));
        fields.add(new Field(30087,   44, "Maximum total system power demand",  Note_4,      "VA",           0x00, 0x56));
        fields.add(new Field(30101,   51, "Total system VA demand",             "",          "VA",           0x00, 0x64));
        fields.add(new Field(30103,   52, "Maximum total VA system demand",     "",          "VA",           0x00, 0x66));
        fields.add(new Field(30105,   53, "Neutral current demand",             "",          "Amps",         0x00, 0x68));
        fields.add(new Field(30107,   54, "Maximum neutral current demand",     "",          "Amps",         0x00, 0x6A));
        fields.add(new Field(30201,  101, "Line 1 to Line 2 volts",             "",          "Volts",        0x00, 0xC8));
        fields.add(new Field(30203,  102, "Line 2 to Line 3 volts",             "",          "Volts",        0x00, 0xCA));
        fields.add(new Field(30205,  103, "Line 3 to Line 1 volts",             "",          "Volts",        0x00, 0xCC));
        fields.add(new Field(30207,  104, "Average line to line volts",         "",          "Volts",        0x00, 0xCE));
        fields.add(new Field(30225,  113, "Neutral current",                    "",          "Amps",         0x00, 0xE0));
        fields.add(new Field(30235,  118, "Phase 1 L/N volts THD",              "",          "%",            0x00, 0xEA));
        fields.add(new Field(30237,  119, "Phase 2 L/N volts THD",              "",          "%",            0x00, 0xEC));
        fields.add(new Field(30239,  120, "Phase 3 L/N volts THD",              "",          "%",            0x00, 0xEE));
        fields.add(new Field(30241,  121, "Phase 1 Current THD",                "",          "%",            0x00, 0xF0));
        fields.add(new Field(30243,  122, "Phase 2 Current THD",                "",          "%",            0x00, 0xF2));
        fields.add(new Field(30245,  123, "Phase 3 Current THD",                "",          "%",            0x00, 0xF4));
        fields.add(new Field(30249,  125, "Average line to neutral volts THD",  "",          "%",            0x00, 0xF8));
        fields.add(new Field(30251,  126, "Average line current THD",           "",          "%",            0x00, 0xFA));
        fields.add(new Field(30255,  128, "Total system power factor",          Note_5,      "Degrees",      0x00, 0xFE));
        fields.add(new Field(30259,  130, "Phase 1 current demand",             "",          "Amps",         0x01, 0x02));
        fields.add(new Field(30261,  131, "Phase 2 current demand",             "",          "Amps",         0x01, 0x04));
        fields.add(new Field(30263,  132, "Phase 3 current demand",             "",          "Amps",         0x01, 0x06));
        fields.add(new Field(30265,  133, "Maximum phase 1 current demand",     "",          "Amps",         0x01, 0x08));
        fields.add(new Field(30267,  134, "Maximum phase 2 current demand",     "",          "Amps",         0x01, 0x0A));
        fields.add(new Field(30269,  135, "Maximum phase 3 current demand",     "",          "Amps",         0x01, 0x0C));
        fields.add(new Field(30335,  168, "Line 1 to line 2 volts THD",         "",          "%",            0x01, 0x4E));
        fields.add(new Field(30337,  169, "Line 2 to line 3 volts THD",         "",          "%",            0x01, 0x50));
        fields.add(new Field(30339,  170, "Line 3 to line 1 volts THD",         "",          "%",            0x01, 0x52));
        fields.add(new Field(30341,  171, "Average line to line volts THD",     "",          "%",            0x01, 0x54));
        fields.add(new Field(30343,  172, "Total kWh",                          "",          "kWh",          0x01, 0x56));
        fields.add(new Field(30345,  173, "Total kVArh",                        "",          "kVArh",        0x01, 0x58));
        fields.add(new Field(30347,  174, "L1 import kWh",                      "",          "kWh",          0x01, 0x5a));
        fields.add(new Field(30349,  175, "L2 import kWh",                      "",          "kWh",          0x01, 0x5c));
        fields.add(new Field(30351,  176, "L3 import kWh",                      "",          "kWh",          0x01, 0x5e));
        fields.add(new Field(30353,  177, "L1 export kWh",                      "",          "kWh",          0x01, 0x60));
        fields.add(new Field(30355,  178, "L2 export kWh",                      "",          "kWh",          0x01, 0x62));
        fields.add(new Field(30357,  179, "L3 export kWh",                      "",          "kWh",          0x01, 0x64));
        fields.add(new Field(30359,  180, "L1 total kWh",                       "",          "kWh",          0x01, 0x66));
        fields.add(new Field(30361,  181, "L2 total kWh",                       "",          "kWh",          0x01, 0x68));
        fields.add(new Field(30363,  182, "L3 total kWh",                       "",          "kWh",          0x01, 0x6a));
        fields.add(new Field(30365,  183, "L1 import kVArh",                    "",          "kVArh",        0x01, 0x6c));
        fields.add(new Field(30367,  184, "L2 import kVArh",                    "",          "kVArh",        0x01, 0x6e));
        fields.add(new Field(30369,  185, "L3 import kVArh",                    "",          "kVArh",        0x01, 0x70));
        fields.add(new Field(30371,  186, "L1 export kVArh",                    "",          "kVArh",        0x01, 0x72));
        fields.add(new Field(30373,  187, "L2 export kVArh",                    "",          "kVArh",        0x01, 0x74));
        fields.add(new Field(30375,  188, "L3 export kVArh",                    "",          "kVArh",        0x01, 0x76));
        fields.add(new Field(30377,  189, "L1 total kVArh",                     "",          "kVArh",        0x01, 0x78));
        fields.add(new Field(30379,  190, "L2 total kVArh",                     "",          "kVArh",        0x01, 0x7a));
        fields.add(new Field(30381,  191, "L3 total kVArh",                     "",          "kVArh",        0x01, 0x7c));
    }

    public SDM630Values read(long moduloMs) throws ModbusException {
        long refreshAt = waitTillNextTimeModulo(moduloMs);
        SDM630Values values = read();
        values.setTimestamp(refreshAt);
        return values;
    }

    public SDM630Values read() throws ModbusException {
        final byte[] rawRegisterBytes = getRawRegisterBytes(0, 382);
        return new SDM630Values(rawRegisterBytes);
    }

    public static class SDM630Values {

        private long timestamp;
        private byte[] rawRegisterBytes;

        public SDM630Values(byte[] newRawRegisterBytes) {
            timestamp = System.currentTimeMillis();
            rawRegisterBytes = newRawRegisterBytes;
        }

        public byte[] getRawRegisterBytes() {
            return rawRegisterBytes;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public static class Value {
            String name;
            String unit;
            Float value;

            public Value(String name, String unit, Float value) {
                this.name = name;
                this.unit = unit;
                this.value = value;
            }

            @Override
            public String toString() {
                return "Value: " + "'" + name + "\' = " + value + " " + unit;
            }
        }

        // Floating Point Values
        // Floating point values are 32 bits and encoded according to the IEEE 754 floating point standard.
        // float32 Range: see IEEE 754 Not Implemented: 0x7FC00000 (NaN)
        private static final byte[] NOT_CONFIGURED_FLOAT32 = {(byte)0x7F, (byte)0xC0, (byte)0x00, (byte)0x00};

        public Float readFloat32(byte[] bytes, int modbusAddressHi, int modbusAddressLo) {
            int modbusAddress = modbusAddressHi * 256 + modbusAddressLo;
            int registerByteOffset = modbusAddress*2;
            byte[] valueBytes = new byte[4];
            valueBytes[0] = bytes[registerByteOffset  ];
            valueBytes[1] = bytes[registerByteOffset+1];
            valueBytes[2] = bytes[registerByteOffset+2];
            valueBytes[3] = bytes[registerByteOffset+3];

            if (Arrays.equals(valueBytes, NOT_CONFIGURED_FLOAT32)) {
                return null;
            }

            return ByteBuffer.wrap(valueBytes).order(ByteOrder.BIG_ENDIAN).getFloat();
        }

        public Value get(String name, String unit, int modbusAddressHi, int modbusAddressLo) {
            return new Value(name, unit, readFloat32(rawRegisterBytes, modbusAddressHi, modbusAddressLo));
        }

        public Value getPhase1LineToNeutralVolts      () { return get("Phase 1 line to neutral volts",          "Volts",       0x00, 0x00); }
        public Value getPhase2LineToNeutralVolts      () { return get("Phase 2 line to neutral volts",          "Volts",       0x00, 0x02); }
        public Value getPhase3LineToNeutralVolts      () { return get("Phase 3 line to neutral volts",          "Volts",       0x00, 0x04); }
        public Value getPhase1Current                 () { return get("Phase 1 current",                        "Amps",        0x00, 0x06); }
        public Value getPhase2Current                 () { return get("Phase 2 current",                        "Amps",        0x00, 0x08); }
        public Value getPhase3Current                 () { return get("Phase 3 current",                        "Amps",        0x00, 0x0A); }
        public Value getPhase1Power                   () { return get("Phase 1 power",                          "Watts",       0x00, 0x0C); }
        public Value getPhase2Power                   () { return get("Phase 2 power",                          "Watts",       0x00, 0x0E); }
        public Value getPhase3Power                   () { return get("Phase 3 power",                          "Watts",       0x00, 0x10); }
        public Value getPhase1VoltAmps                () { return get("Phase 1 volt amps",                      "VA",          0x00, 0x12); }
        public Value getPhase2VoltAmps                () { return get("Phase 2 volt amps",                      "VA",          0x00, 0x14); }
        public Value getPhase3VoltAmps                () { return get("Phase 3 volt amps",                      "VA",          0x00, 0x16); }
        public Value getPhase1VoltAmpsReactive        () { return get("Phase 1 volt amps reactive",             "VAr",         0x00, 0x18); }
        public Value getPhase2VoltAmpsReactive        () { return get("Phase 2 volt amps reactive",             "VAr",         0x00, 0x1A); }
        public Value getPhase3VoltAmpsReactive        () { return get("Phase 3 volt amps reactive",             "VAr",         0x00, 0x1C); }
        public Value getPhase1PowerFactor             () { return get("Phase 1 power factor",                   "",            0x00, 0x1E); }
        public Value getPhase2PowerFactor             () { return get("Phase 2 power factor",                   "",            0x00, 0x20); }
        public Value getPhase3PowerFactor             () { return get("Phase 3 power factor",                   "",            0x00, 0x22); }
        public Value getPhase1PhaseAngle              () { return get("Phase 1 phase angle",                    "Degrees",     0x00, 0x24); }
        public Value getPhase2PhaseAngle              () { return get("Phase 2 phase angle",                    "Degrees",     0x00, 0x26); }
        public Value getPhase3PhaseAngle              () { return get("Phase 3 phase angle",                    "Degrees",     0x00, 0x28); }
        public Value getAverageLineToNeutralVolts     () { return get("Average line to neutral volts",          "Volts",       0x00, 0x2A); }
        public Value getAverageLineCurrent            () { return get("Average line current",                   "Amps",        0x00, 0x2E); }
        public Value getSumOfLineCurrents             () { return get("Sum of line currents",                   "Amps",        0x00, 0x30); }
        public Value getTotalSystemPower              () { return get("Total system power",                     "Watts",       0x00, 0x34); }
        public Value getTotalSystemVoltAmps           () { return get("Total system volt amps",                 "VA",          0x00, 0x38); }
        public Value getTotalSystemVAr                () { return get("Total system VAr",                       "VAr",         0x00, 0x3C); }
        public Value getTotalSystemPowerFactor        () { return get("Total system power factor",              "",            0x00, 0x3E); }
        public Value getTotalSystemPhaseAngle         () { return get("Total system phase angle",               "Degrees",     0x00, 0x42); }
        public Value getFrequencyOfSupplyVoltages     () { return get("Frequency of supply voltages",           "Hz",          0x00, 0x46); }
        public Value getImportWhSinceLastReset        () { return get("Import Wh since last reset",             "kWh/MWh",     0x00, 0x48); }
        public Value getExportWhSinceLastReset        () { return get("Export Wh since last reset",             "kWH/MWh",     0x00, 0x4A); }
        public Value getImportVArhSinceLastReset      () { return get("Import VArh since last reset",           "kVArh/MVArh", 0x00, 0x4C); }
        public Value getExportVArhSinceLastReset      () { return get("Export VArh since last reset",           "kVArh/MVArh", 0x00, 0x4e); }
        public Value getVAhSinceLastReset             () { return get("VAh since last reset",                   "kVAh/MVAh",   0x00, 0x50); }
        public Value getAhSinceLastReset              () { return get("Ah since last reset",                    "Ah/kAh",      0x00, 0x52); }
        public Value getTotalSystemPowerDemand        () { return get("Total system power demand",              "W",           0x00, 0x54); }
        public Value getMaximumTotalSystemPowerDemand () { return get("Maximum total system power demand",      "VA",          0x00, 0x56); }
        public Value getTotalSystemVADemand           () { return get("Total system VA demand",                 "VA",          0x00, 0x64); }
        public Value getMaximumTotalVASystemDemand    () { return get("Maximum total VA system demand",         "VA",          0x00, 0x66); }
        public Value getNeutralCurrentDemand          () { return get("Neutral current demand",                 "Amps",        0x00, 0x68); }
        public Value getMaximumNeutralCurrentDemand   () { return get("Maximum neutral current demand",         "Amps",        0x00, 0x6A); }
        public Value getLine1ToLine2Volts             () { return get("Line 1 to Line 2 volts",                 "Volts",       0x00, 0xC8); }
        public Value getLine2ToLine3Volts             () { return get("Line 2 to Line 3 volts",                 "Volts",       0x00, 0xCA); }
        public Value getLine3ToLine1Volts             () { return get("Line 3 to Line 1 volts",                 "Volts",       0x00, 0xCC); }
        public Value getAverageLineToLineVolts        () { return get("Average line to line volts",             "Volts",       0x00, 0xCE); }
        public Value getNeutralCurrent                () { return get("Neutral current",                        "Amps",        0x00, 0xE0); }
        public Value getPhase1L_NVoltsTHD             () { return get("Phase 1 L/N volts THD",                  "%",           0x00, 0xEA); }
        public Value getPhase2L_NVoltsTHD             () { return get("Phase 2 L/N volts THD",                  "%",           0x00, 0xEC); }
        public Value getPhase3L_NVoltsTHD             () { return get("Phase 3 L/N volts THD",                  "%",           0x00, 0xEE); }
        public Value getPhase1CurrentTHD              () { return get("Phase 1 Current THD",                    "%",           0x00, 0xF0); }
        public Value getPhase2CurrentTHD              () { return get("Phase 2 Current THD",                    "%",           0x00, 0xF2); }
        public Value getPhase3CurrentTHD              () { return get("Phase 3 Current THD",                    "%",           0x00, 0xF4); }
        public Value getAverageLineToNeutralVoltsTHD  () { return get("Average line to neutral volts THD",      "%",           0x00, 0xF8); }
        public Value getAverageLineCurrentTHD         () { return get("Average line current THD",               "%",           0x00, 0xFA); }
        public Value getTotalSystemPowerFactorDegrees () { return get("Total system power factor",              "Degrees",     0x00, 0xFE); }
        public Value getPhase1CurrentDemand           () { return get("Phase 1 current demand",                 "Amps",        0x01, 0x02); }
        public Value getPhase2CurrentDemand           () { return get("Phase 2 current demand",                 "Amps",        0x01, 0x04); }
        public Value getPhase3CurrentDemand           () { return get("Phase 3 current demand",                 "Amps",        0x01, 0x06); }
        public Value getMaximumPhase1CurrentDemand    () { return get("Maximum phase 1 current demand",         "Amps",        0x01, 0x08); }
        public Value getMaximumPhase2CurrentDemand    () { return get("Maximum phase 2 current demand",         "Amps",        0x01, 0x0A); }
        public Value getMaximumPhase3CurrentDemand    () { return get("Maximum phase 3 current demand",         "Amps",        0x01, 0x0C); }
        public Value getLine1ToLine2VoltsTHD          () { return get("Line 1 to line 2 volts THD",             "%",           0x01, 0x4E); }
        public Value getLine2ToLine3VoltsTHD          () { return get("Line 2 to line 3 volts THD",             "%",           0x01, 0x50); }
        public Value getLine3ToLine1VoltsTHD          () { return get("Line 3 to line 1 volts THD",             "%",           0x01, 0x52); }
        public Value getAverageLineToLineVoltsTHD     () { return get("Average line to line volts THD",         "%",           0x01, 0x54); }
        public Value getTotalKWh                      () { return get("Total kWh",                              "kWh",         0x01, 0x56); }
        public Value getTotalKVArh                    () { return get("Total kVArh",                            "kVArh",       0x01, 0x58); }
        public Value getL1ImportKWh                   () { return get("L1 import kWh",                          "kWh",         0x01, 0x5a); }
        public Value getL2ImportKWh                   () { return get("L2 import kWh",                          "kWh",         0x01, 0x5c); }
        public Value getL3ImportKWh                   () { return get("L3 import kWh",                          "kWh",         0x01, 0x5e); }
        public Value getL1ExportKWh                   () { return get("L1 export kWh",                          "kWh",         0x01, 0x60); }
        public Value getL2ExportKWh                   () { return get("L2 export kWh",                          "kWh",         0x01, 0x62); }
        public Value getL3ExportKWh                   () { return get("L3 export kWh",                          "kWh",         0x01, 0x64); }
        public Value getL1TotalKWh                    () { return get("L1 total kWh",                           "kWh",         0x01, 0x66); }
        public Value getL2TotalKWh                    () { return get("L2 total kWh",                           "kWh",         0x01, 0x68); }
        public Value getL3TotalKWh                    () { return get("L3 total kWh",                           "kWh",         0x01, 0x6a); }
        public Value getL1ImportKVArh                 () { return get("L1 import kVArh",                        "kVArh",       0x01, 0x6c); }
        public Value getL2ImportKVArh                 () { return get("L2 import kVArh",                        "kVArh",       0x01, 0x6e); }
        public Value getL3ImportKVArh                 () { return get("L3 import kVArh",                        "kVArh",       0x01, 0x70); }
        public Value getL1ExportKVArh                 () { return get("L1 export kVArh",                        "kVArh",       0x01, 0x72); }
        public Value getL2ExportKVArh                 () { return get("L2 export kVArh",                        "kVArh",       0x01, 0x74); }
        public Value getL3ExportKVArh                 () { return get("L3 export kVArh",                        "kVArh",       0x01, 0x76); }
        public Value getL1TotalKVArh                  () { return get("L1 total kVArh",                         "kVArh",       0x01, 0x78); }
        public Value getL2TotalKVArh                  () { return get("L2 total kVArh",                         "kVArh",       0x01, 0x7a); }
        public Value getL3TotalKVArh                  () { return get("L3 total kVArh",                         "kVArh",       0x01, 0x7c); }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(4096);
            sb.append("- SDM630Values\n");
            toMap().forEach((k,v) -> sb.append("    ").append(v.toString()).append("\n"));
            return sb.toString();
        }

        public Map<String, Value> toMap() {
            Map<String, Value> values = new LinkedHashMap<>();
            values.put("Phase 1 line to neutral volts",          getPhase1LineToNeutralVolts      ());
            values.put("Phase 2 line to neutral volts",          getPhase2LineToNeutralVolts      ());
            values.put("Phase 3 line to neutral volts",          getPhase3LineToNeutralVolts      ());
            values.put("Phase 1 current",                        getPhase1Current                 ());
            values.put("Phase 2 current",                        getPhase2Current                 ());
            values.put("Phase 3 current",                        getPhase3Current                 ());
            values.put("Phase 1 power",                          getPhase1Power                   ());
            values.put("Phase 2 power",                          getPhase2Power                   ());
            values.put("Phase 3 power",                          getPhase3Power                   ());
            values.put("Phase 1 volt amps",                      getPhase1VoltAmps                ());
            values.put("Phase 2 volt amps",                      getPhase2VoltAmps                ());
            values.put("Phase 3 volt amps",                      getPhase3VoltAmps                ());
            values.put("Phase 1 volt amps reactive",             getPhase1VoltAmpsReactive        ());
            values.put("Phase 2 volt amps reactive",             getPhase2VoltAmpsReactive        ());
            values.put("Phase 3 volt amps reactive",             getPhase3VoltAmpsReactive        ());
            values.put("Phase 1 power factor",                   getPhase1PowerFactor             ());
            values.put("Phase 2 power factor",                   getPhase2PowerFactor             ());
            values.put("Phase 3 power factor",                   getPhase3PowerFactor             ());
            values.put("Phase 1 phase angle",                    getPhase1PhaseAngle              ());
            values.put("Phase 2 phase angle",                    getPhase2PhaseAngle              ());
            values.put("Phase 3 phase angle",                    getPhase3PhaseAngle              ());
            values.put("Average line to neutral volts",          getAverageLineToNeutralVolts     ());
            values.put("Average line current",                   getAverageLineCurrent            ());
            values.put("Sum of line currents",                   getSumOfLineCurrents             ());
            values.put("Total system power",                     getTotalSystemPower              ());
            values.put("Total system volt amps",                 getTotalSystemVoltAmps           ());
            values.put("Total system VAr",                       getTotalSystemVAr                ());
            values.put("Total system power factor",              getTotalSystemPowerFactor        ());
            values.put("Total system phase angle",               getTotalSystemPhaseAngle         ());
            values.put("Frequency of supply voltages",           getFrequencyOfSupplyVoltages     ());
            values.put("Import Wh since last reset",             getImportWhSinceLastReset        ());
            values.put("Export Wh since last reset",             getExportWhSinceLastReset        ());
            values.put("Import VArh since last reset",           getImportVArhSinceLastReset      ());
            values.put("Export VArh since last reset",           getExportVArhSinceLastReset      ());
            values.put("VAh since last reset",                   getVAhSinceLastReset             ());
            values.put("Ah since last reset",                    getAhSinceLastReset              ());
            values.put("Total system power demand",              getTotalSystemPowerDemand        ());
            values.put("Maximum total system power demand",      getMaximumTotalSystemPowerDemand ());
            values.put("Total system VA demand",                 getTotalSystemVADemand           ());
            values.put("Maximum total VA system demand",         getMaximumTotalVASystemDemand    ());
            values.put("Neutral current demand",                 getNeutralCurrentDemand          ());
            values.put("Maximum neutral current demand",         getMaximumNeutralCurrentDemand   ());
            values.put("Line 1 to Line 2 volts",                 getLine1ToLine2Volts             ());
            values.put("Line 2 to Line 3 volts",                 getLine2ToLine3Volts             ());
            values.put("Line 3 to Line 1 volts",                 getLine3ToLine1Volts             ());
            values.put("Average line to line volts",             getAverageLineToLineVolts        ());
            values.put("Neutral current",                        getNeutralCurrent                ());
            values.put("Phase 1 L/N volts THD",                  getPhase1L_NVoltsTHD             ());
            values.put("Phase 2 L/N volts THD",                  getPhase2L_NVoltsTHD             ());
            values.put("Phase 3 L/N volts THD",                  getPhase3L_NVoltsTHD             ());
            values.put("Phase 1 Current THD",                    getPhase1CurrentTHD              ());
            values.put("Phase 2 Current THD",                    getPhase2CurrentTHD              ());
            values.put("Phase 3 Current THD",                    getPhase3CurrentTHD              ());
            values.put("Average line to neutral volts THD",      getAverageLineToNeutralVoltsTHD  ());
            values.put("Average line current THD",               getAverageLineCurrentTHD         ());
            values.put("Total system power factor degrees",      getTotalSystemPowerFactorDegrees ());
            values.put("Phase 1 current demand",                 getPhase1CurrentDemand           ());
            values.put("Phase 2 current demand",                 getPhase2CurrentDemand           ());
            values.put("Phase 3 current demand",                 getPhase3CurrentDemand           ());
            values.put("Maximum phase 1 current demand",         getMaximumPhase1CurrentDemand    ());
            values.put("Maximum phase 2 current demand",         getMaximumPhase2CurrentDemand    ());
            values.put("Maximum phase 3 current demand",         getMaximumPhase3CurrentDemand    ());
            values.put("Line 1 to line 2 volts THD",             getLine1ToLine2VoltsTHD          ());
            values.put("Line 2 to line 3 volts THD",             getLine2ToLine3VoltsTHD          ());
            values.put("Line 3 to line 1 volts THD",             getLine3ToLine1VoltsTHD          ());
            values.put("Average line to line volts THD",         getAverageLineToLineVoltsTHD     ());
            values.put("Total kWh",                              getTotalKWh                      ());
            values.put("Total kVArh",                            getTotalKVArh                    ());
            values.put("L1 import kWh",                          getL1ImportKWh                   ());
            values.put("L2 import kWh",                          getL2ImportKWh                   ());
            values.put("L3 import kWh",                          getL3ImportKWh                   ());
            values.put("L1 export kWh",                          getL1ExportKWh                   ());
            values.put("L2 export kWh",                          getL2ExportKWh                   ());
            values.put("L3 export kWh",                          getL3ExportKWh                   ());
            values.put("L1 total kWh",                           getL1TotalKWh                    ());
            values.put("L2 total kWh",                           getL2TotalKWh                    ());
            values.put("L3 total kWh",                           getL3TotalKWh                    ());
            values.put("L1 import kVArh",                        getL1ImportKVArh                 ());
            values.put("L2 import kVArh",                        getL2ImportKVArh                 ());
            values.put("L3 import kVArh",                        getL3ImportKVArh                 ());
            values.put("L1 export kVArh",                        getL1ExportKVArh                 ());
            values.put("L2 export kVArh",                        getL2ExportKVArh                 ());
            values.put("L3 export kVArh",                        getL3ExportKVArh                 ());
            values.put("L1 total kVArh",                         getL1TotalKVArh                  ());
            values.put("L2 total kVArh",                         getL2TotalKVArh                  ());
            values.put("L3 total kVArh",                         getL3TotalKVArh                  ());
            return values;
        }
    }

}






