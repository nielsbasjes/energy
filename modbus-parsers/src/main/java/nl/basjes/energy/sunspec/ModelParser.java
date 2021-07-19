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

import org.apache.commons.lang3.NotImplementedException;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;

import static java.nio.charset.StandardCharsets.US_ASCII;

public abstract class ModelParser {

    public abstract int getId();

    public String getLabel()       { return ""; };
    public String getDescription() { return ""; };
    public String getNotes()       { return ""; };

    public abstract Map<String, Object> toHashMap(byte[] dataBlock) throws Exception;
    public abstract String toString(byte[] dataBlock) throws Exception;

    // All format specs are from SunSpec-Information-Models-12041.pdf from https://sunspec.org/download/

    // ===================================================================================================
    // 16-bit Integer Values
    // Values are stored in big-endian order per the Modbus specification and consist of a single register.
    // All integer values are documented as signed or unsigned.
    // All signed values are represented using two’s-compliment format.
    // -----

    private int get16bits(byte[] block, int registerAddress) {
        int byteNo = registerAddress * 2;
        int value = 0;
        value |= (block[byteNo] & 255) << 8;
        value |= (block[byteNo + 1] & 255);
        return value;
    }

    // int16 Range: -32767 ... 32767 Not Implemented: 0x8000
    public Short int16(byte[] block, int registerAddress) {
        int value = get16bits(block, registerAddress);

        if (value == 0x8000) {
            return null;
        }

        return (short) value;
    }

    // uint16 Range: 0 ... 65534 Not Implemented: 0xFFFF
    public Integer uint16(byte[] block, int registerAddress) {
        int value = get16bits(block, registerAddress);

        if (value == 0xFFFF) {
            return null;
        }

        // Although incorrect some devices seem to have mixedup int16 and uint16.
        // As a consequence some send 0x8000 when 'unused'
        if (value == 0x8000) {
            return null;
        }

        return value;
    }

    // acc16 Range: 0 ... 65535 Not Accumulated: 0x0000
    // NOTE: it is up to the master to detect rollover of accumulated values.
    public Integer acc16(byte[] block, int registerAddress) {
        return uint16(block, registerAddress);
    }

    // count is NOT documented. From the actual use we assume it is 1 register accumulator
    public Integer count(byte[] block, int registerAddress) {
        return uint16(block, registerAddress);
    }

    // enum16 Range: 0 ... 65534 Not Implemented: 0xFFFF
    public Integer enum16(byte[] block, int registerAddress) {
        return uint16(block, registerAddress);
    }

    // bitfield16 Range: 0 ... 0x7FFF Not Implemented: 0xFFFF
    // NOTE: if the most significant bit in a bitfield is set, all other bits shall be ignored.
    public Integer bitfield16(byte[] block, int registerAddress) {
        int value = get16bits(block, registerAddress);

        if ((value & 0xA000) == 0xA000) {
            return null;
        }
        return value;
    }

    // pad Range: 0x8000 Always returns 0x8000
    public Integer pad(byte[] block, int registerAddress) {
        int value = get16bits(block, registerAddress);

        if (value != 0x8000) {
            return null;
        }
        return value;
    }

    // ===================================================================================================
    // 32-bit Integer Values
    // 32-bit integers are stored using two registers in big-endian order
    // -----

    private long get32bits(byte[] block, int registerAddress) {
        int byteNo = registerAddress * 2;
        long value = 0;
        value |= (block[byteNo] & 255) << 24;
        value |= (block[byteNo + 1] & 255) << 16;
        value |= (block[byteNo + 2] & 255) << 8;
        value |= (block[byteNo + 3] & 255);
        return value;
    }

    // int32 Range: -2147483647 ... 2147483647 Not Implemented: 0x80000000
    public Integer int32(byte[] block, int registerAddress) {
        long value = get32bits(block, registerAddress);

        if (value == 0x80000000) {
            return null;
        }

        return (int) value;
    }

    // uint32 Range: 0 ... 4294967294 Not Implemented: 0xFFFFFFFF
    public Long uint32(byte[] block, int registerAddress) {
        long value = get32bits(block, registerAddress);

        if (value == 0xFFFFFFFF) {
            return null;
        }

        return value;
    }

    // acc32 Range: 0 ... 4294967295 Not Accumulated: 0x00000000
    // NOTE: it is up to the master to detect rollover of accumulated values.
    public Long acc32(byte[] block, int registerAddress) {
        return uint32(block, registerAddress);
    }

    // enum32 Range: 0 ... 4294967294 Not Implemented: 0xFFFFFFFF
    public Long enum32(byte[] block, int registerAddress) {
        return uint32(block, registerAddress);
    }

    // bitfield32 Range: 0 ... 0x7FFFFFFF Not Implemented: 0xFFFFFFFF
    // NOTE: if the most significant bit in a bitfield is set, all other bits shall be ignored.
    public Long bitfield32(byte[] block, int registerAddress) {
        long value = get32bits(block, registerAddress);

        if ((value & 0x80000000) == 0x80000000) {
            return null;
        }

        return value;
    }

    private static final byte[] NOT_CONFIGURES_IPV4 = {0, 0, 0, 0};

    // ipaddr 32 bit IPv4 address Not Configured: 0x00000000
    public InetAddress ipaddr(byte[] block, int registerAddress) throws UnknownHostException {
        int byteNo = registerAddress * 2;
        byte[] ipBytes = new byte[4];
        ipBytes[0] = (byte) (block[byteNo] & 255);
        ipBytes[1] = (byte) (block[byteNo + 1] & 255);
        ipBytes[2] = (byte) (block[byteNo + 2] & 255);
        ipBytes[3] = (byte) (block[byteNo + 3] & 255);

        if (Arrays.equals(ipBytes, NOT_CONFIGURES_IPV4)) {
            return null;
        }

        return Inet4Address.getByAddress(ipBytes);
    }


    // ===================================================================================================
    // 64-bit Integer Values
    // 64-bit integers are stored using four registers in big-endian order.
    // -----

    private long get64bits(byte[] block, int registerAddress) {
        int byteNo = registerAddress * 2;
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= (block[byteNo++] & 255);
            value <<= 8;
        }
        return value;
    }

    // int64 Range: -9223372036854775807 ... 9223372036854775807 Not Implemented: 0x8000000000000000
    public Long int64(byte[] block, int registerAddress) {
        long value = get64bits(block, registerAddress);

        if (value == 0x8000000000000000L) {
            return null;
        }

        return value;
    }

    // uint64: Present in specification. Undocumented,  Not used.
//    public Long uint64(byte[] block, int registerAddress) {
//        return int64(block, registerAddress);
//    }

    // acc64 Range: 0 ... 9223372036854775807 Not Accumulated: 0
    // NOTE: Only positive values in the int64 range are allowed.
    // Accumulator values outside of the defined range shall be considered invalid.
    // NOTE: The accumulator value shall rollover after the highest positive value in the int64 range (0x7fffffffffffffff).
    // It is up to the reader to detect rollover of accumulated values.
    public Long acc64(byte[] block, int registerAddress) {
        long value = get64bits(block, registerAddress);

        if (value < 0) {
            return null;
        }

        return value;
    }


    // ===================================================================================================

    // 128 Bit Integer Values
    // 128 bit integers are stored using eight registers in big-endian order.

    private static final byte[] NOT_CONFIGURED_IPV6 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // ipv6addr 128 bit IPv6 address Not Configured: 0
    public InetAddress ipv6addr(byte[] block, int registerAddress) throws UnknownHostException {
        int byteNo = registerAddress * 2;
        byte[] ipBytes = new byte[16];
        ipBytes[0] = (byte) (block[byteNo] & 255);
        ipBytes[1] = (byte) (block[byteNo + 1] & 255);
        ipBytes[2] = (byte) (block[byteNo + 2] & 255);
        ipBytes[3] = (byte) (block[byteNo + 3] & 255);
        ipBytes[4] = (byte) (block[byteNo + 4] & 255);
        ipBytes[5] = (byte) (block[byteNo + 5] & 255);
        ipBytes[6] = (byte) (block[byteNo + 6] & 255);
        ipBytes[7] = (byte) (block[byteNo + 7] & 255);
        ipBytes[8] = (byte) (block[byteNo + 8] & 255);
        ipBytes[9] = (byte) (block[byteNo + 9] & 255);
        ipBytes[10] = (byte) (block[byteNo + 10] & 255);
        ipBytes[11] = (byte) (block[byteNo + 11] & 255);
        ipBytes[12] = (byte) (block[byteNo + 12] & 255);
        ipBytes[13] = (byte) (block[byteNo + 13] & 255);
        ipBytes[14] = (byte) (block[byteNo + 14] & 255);
        ipBytes[15] = (byte) (block[byteNo + 15] & 255);

        if (Arrays.equals(ipBytes, NOT_CONFIGURED_IPV6)) {
            return null;
        }

        return Inet6Address.getByAddress(ipBytes);
    }

    // ===================================================================================================
    // Special: Missing from specification but used anyway
    // eui48 has the same format as an int64 (8 bytes) used for the network MAC address which is 6 bytes
    // See https://github.com/sunspec/models/issues/41 and https://github.com/sunspec/models/pull/57

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private String byteToHex(byte myByte) {
        return "" + HEX_ARRAY[(myByte >>> 4) & 0x0F ] + HEX_ARRAY[myByte & 0x0F];
    }

    public String eui48(byte[] block, int registerAddress) {
        StringBuilder sb = new StringBuilder(16);
        int byteNo = registerAddress * 2;
        if (block[byteNo] == -1) { // Which is 0xFF
            return null;
        }

        // This matches
        sb.append(byteToHex((byte) (block[byteNo + 2] & 255))).append(':');
        sb.append(byteToHex((byte) (block[byteNo + 3] & 255))).append(':');
        sb.append(byteToHex((byte) (block[byteNo + 4] & 255))).append(':');
        sb.append(byteToHex((byte) (block[byteNo + 5] & 255))).append(':');
        sb.append(byteToHex((byte) (block[byteNo + 6] & 255))).append(':');
        sb.append(byteToHex((byte) (block[byteNo + 7] & 255)));

        return sb.toString();
    }

    // ===================================================================================================
    // String Values
    // Store variable length string values in a fixed size register range using a NULL (0 value) to terminate or pad the string.
    // NOT_IMPLEMENTED value: all registers filled with NULL or 0x0000

    public String string(byte[] block, int registerAddress, int len) {
        int letters = len * 2;
        int baseOffset = registerAddress * 2;
        byte[] chars = new byte[letters];

        // FIXME: Since no formal charset has been defined we __ASSUME__ that only the base ASCII is supported.
        // See discussion at https://github.com/sunspec/models/issues/45

        if (block[baseOffset] == (byte)0x00 ||
            block[baseOffset] == (byte)0x80 ){
            return ""; // Empty string.
        }

        int letter = 0;
        for (; letter < letters; letter++) {
            chars[letter] = block[baseOffset + letter];
            if (chars[letter] == 0x00) break;
        }
        return new String(chars, 0, letter, US_ASCII);
    }


    // ===================================================================================================

    // Floating Point Values
    // Floating point values are 32 bits and encoded according to the IEEE 754 floating point standard.
    // float32 Range: see IEEE 754 Not Implemented: 0x7FC00000 (NaN)

    public Float float32(byte[] block, int registerAddress) {
        throw new NotImplementedException("FIXME: Implement float32"); // FIXME: Implement float32
    }

    // Scale Factors
    // As an alternative to floating point format, values are represented by integer values
    // with a signed scale factor applied. The scale factor explicitly shifts the decimal point
    // to the left (negative value) or the right (positive value). Scale factors may be fixed
    // and specified in the documentation of a value, or may have a variable scale factor
    // associated with it. For example, a value “Value” may have an associated value
    // “Value_SF” of type “sunssf” that is a 16 bit two’s compliment integer.
    //
    // sunssf signed range: -10 … 10 Not Implemented: 0x8000

    // If a value is implemented and has an associated scale factor, the scale factor must
    // also be implemented.

    public Short sunssf(byte[] block, int registerAddress) {
        Short value = int16(block, registerAddress);
        if (value == null || -10 > value || value > 10) {
            return null; // Too far away
        }
        return value;
    }

    private Double scale(double value, Short scale) {
        if (scale == null) {
            // If no scale is available we simply return the unmodified base value
            return value;
        }

        return (value) * (Math.pow(10, scale));
    }

    public Double calculateScaledValue(Short value, Short scale) {
        if (value == null) { return null; }
        return scale((double)value, scale);
    }

    public Double calculateScaledValue(Integer value, Short scale) {
        if (value == null) { return null; }
        return scale((double)value, scale);
    }

    public Double calculateScaledValue(Long value, Short scale) {
        if (value == null) { return null; }
        return scale((double)value, scale);
    }

    public Double calculateScaledValue(Short value, short scale) {
        if (value == null) { return null; }
        return scale((double)value, scale);
    }

    public Double calculateScaledValue(Integer value, short scale) {
        if (value == null) { return null; }
        return scale((double)value, scale);
    }

    public Double calculateScaledValue(Long value, short scale) {
        if (value == null) { return null; }
        return scale((double)value, scale);
    }

    private static boolean strictMode = false;

    /**
     * If a mandatory field is missing: Fail hard.
     */
    public static void enableStrictMode() {
       strictMode=true;
    }

    /**
     * If a mandatory field is missing: Ignore and continue.
     */
    public static void disableStrictMode() {
        strictMode=false;
    }

    public <T> T throwIfNull(String model, String field, T value) throws MissingMandatoryFieldException {
        return throwIfNull(model, field, value, null);
    }

    public String throwIfNull(String model, String field, String value) throws MissingMandatoryFieldException {
        return throwIfNull(model, field, value, "<<MISSING>>");
    }

    public Short throwIfNull(String model, String field, Short value) throws MissingMandatoryFieldException {
        return throwIfNull(model, field, value, (short)0);
    }

    public Integer throwIfNull(String model, String field, Integer value) throws MissingMandatoryFieldException {
        return throwIfNull(model, field, value, 0);
    }

    public Long throwIfNull(String model, String field, Long value) throws MissingMandatoryFieldException {
        return throwIfNull(model, field, value, 0L);
    }

    public <T> T throwIfNull(String model, String field, T value, T nullValue) throws MissingMandatoryFieldException {
        if (value == null) {
            if (strictMode) {
                throw new MissingMandatoryFieldException(model, field);
            }
            value = nullValue;
//            LOG.error("In model {} the mandatory field called {} is empty.", model, field);
        }
        return value;
    }


}
