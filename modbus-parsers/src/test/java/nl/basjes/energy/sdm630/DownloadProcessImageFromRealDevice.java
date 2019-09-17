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

import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ghgande.j2mod.modbus.Modbus.SERIAL_ENCODING_RTU;

@Ignore
public class DownloadProcessImageFromRealDevice {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadProcessImageFromRealDevice.class);

    @Test
    public void getAllBytesFromRealDevice() throws Exception {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setEncoding(SERIAL_ENCODING_RTU);
        serialParameters.setPortName("/dev/ttyUSB0");
        serialParameters.setBaudRate(9600);


        try(SDM630Reader dataReader = new SDM630Reader(new ModbusSerialMaster(serialParameters), 1)) {

            dataReader.connect();

            final SDM630Reader.SDM630Values values = dataReader.read();

            final byte[] rawRegisterBytes = values.getRawRegisterBytes();

            dataReader.disconnect();

            String sb = "byte[] bytes = {\n" +
                "    " + bytesToHex(rawRegisterBytes) + "\n" +
                "}\n\n";
            LOG.info("\n\n{}", sb);
        }
    }

    //    https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 15);
        for (byte aByte : bytes) {
            int v = aByte & 0xFF;
            sb
                .append("(byte)0x")
                .append(HEX_ARRAY[v >>> 4])
                .append(HEX_ARRAY[v & 0x0F])
                .append(", ");
        }
        return sb.toString();
    }

}
