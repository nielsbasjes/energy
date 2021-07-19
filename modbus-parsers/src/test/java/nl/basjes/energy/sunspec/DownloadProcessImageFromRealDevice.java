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

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import nl.basjes.energy.sunspec.SunSpecModbusDataReader.ModelLocation;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_STARTBASE;

public class DownloadProcessImageFromRealDevice {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadProcessImageFromRealDevice.class);

    @Ignore
    @Test
    public void getAllBytesFromRealDevice() throws Exception {
        SunSpecModbusDataReader dataReader = new SunSpecModbusDataReader(new ModbusTCPMaster("10.11.12.13"));

        dataReader.connect();

        final Map<Integer, ModelLocation> modelLocations = dataReader.getModelLocations();

        modelLocations.forEach((i, p) -> LOG.info("Found model {}: at register {} of {} registers", p.id, p.registerBase, p.len));

        StringBuilder sb = new StringBuilder(4096);

        sb  .append("byte[] bytes = {\n")
            .append("    // The SunS header\n")
            .append("    ").append(bytesToHex(dataReader.getRawRegisterBytes(SUNSPEC_STANDARD_STARTBASE, 2))).append("\n\n");

        for (ModelLocation modelLocation: modelLocations.values()) {
            LOG.info("Reading bytes for Model {}: Start {} Size {}", modelLocation.id, modelLocation.registerBase, modelLocation.len);
            byte[] modelHeader = dataReader.getRawRegisterBytes(modelLocation.registerBase - 2, 2);
            sb  .append("    // Model Id ").append(modelLocation.id)
                .append(" at ").append(modelLocation.registerBase).append(".\n")
                .append("    ").append(bytesToHex(modelHeader)).append("// Model header\n");

            byte[] blockBytes = dataReader.getRawModel(modelLocation);
            sb  .append("    // Model Id ").append(modelLocation.id).append(" is ").append(modelLocation.len).append(" bytes.\n")
                .append("    ").append(bytesToHex(blockBytes)).append("\n\n");
        }

        // NOTE: This is the end marker according to the standard.
        //       Some devices deviate from this, here we assume the standard.
        sb  .append("    // The End Model (i.e. the standard \"No more blocks\" marker)\n\n")
            .append("    // - BlockId == 0xFFFF == 'NaN'\n")
            .append("    // - BlockLen == 0\n")
            .append("    (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00\n")
            .append("};\n");

        dataReader.disconnect();

        LOG.info("\n\n\n" + sb.toString());
    }

    //    https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 15);
        int i = 0;
        for (byte aByte : bytes) {
            int v = aByte & 0xFF;
            sb
                .append("(byte)0x")
                .append(HEX_ARRAY[v >>> 4])
                .append(HEX_ARRAY[v & 0x0F])
                .append(", ");
            i++;
            if (i%8 == 0) {
                sb.append("\n    ");
            }
        }
        return sb.toString();
    }

}
