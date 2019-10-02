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

package nl.basjes.modbus;


import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.facade.AbstractModbusMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ModBusDataReader implements AutoCloseable {

    private AbstractModbusMaster master;
    private int unitId;
    private boolean isConnected = false;

    public ModBusDataReader(AbstractModbusMaster master, int unitId) {
        this.master = master;
        this.unitId = unitId;
    }

    InputRegister[] read(int registerAddress, int count) throws ModbusException {
        if (!isConnected) {
            try {
                connect();
            } catch (Exception e) {
                throw new ModbusIOException("Not connected", e);
            }
            if (!isConnected) {
                throw new ModbusIOException("Not connected");
            }
        }

        return master.readMultipleRegisters(unitId, registerAddress, count);
    }

//    http://files.sma.de/dl/2585/WEBBOX-MODBUS-TB-en-19.pdf
//    3.6.1 Data Types and NaN Values
//    The following table gives the data types used in the SMA Modbus profile and sets them against
//    possible NaN values. The SMA data types are used in the assignment tables, in the Type column.
//    They describe the data widths of the assigned values:
//    Type    Description                                                         NaN value
//    U16     A word (16 bit/WORD) in the local processor format                  0xFFFF
//    S16     Signed word (16 bit/WORD) in the local processor format             0x8000
//    U32     A double word (32 bit/DWORD) in the local processor format          0xFFFF FFFF
//    S32     A signed double word (32 bit/DWORD) in the local processor format   0x8000 0000
//    U64     A quad word (64 bit/2 x DWORD) in the local processor format        0xFFFF FFFF FFFF FFFF

    public Integer readU16(int registerAddress) throws ModbusException {
        final InputRegister[] registers = read(registerAddress, 1);

        int value = registers[0].toUnsignedShort();
        if (value == 0xFFFF) {
            return null;
        }
        return value;
    }

    public Short readS16(int registerAddress) throws ModbusException {
        final InputRegister[] registers = read(registerAddress, 1);
        short value = registers[0].toShort();
        if (registers[0].getValue() == 0x8000) {
            return null;
        }
        return value;
    }

    public long readU32(int registerAddress) throws ModbusException {
        if (!isConnected) {
            throw new ModbusIOException("Not connected");
        }
        final InputRegister[] registers = master.readMultipleRegisters(unitId, registerAddress, 2);
        long                  result    = registers[0].getValue() & 0xffff;
        result = result << 16;
        result += registers[1].getValue() & 0xffff;
        return result;
    }

    // Floating Point Values
    // Floating point values are 32 bits and encoded according to the IEEE 754 floating point standard.
    // float32 Range: see IEEE 754 Not Implemented: 0x7FC00000 (NaN)
    private static final byte[] NOT_CONFIGURED_FLOAT32 = {(byte)0x7F, (byte)0xC0, (byte)0x00, (byte)0x00};

    public Float readFloat32(int registerAddress) throws ModbusException {
        if (!isConnected) {
            throw new ModbusIOException("Not connected");
        }
        final InputRegister[] registers = master.readMultipleRegisters(unitId, registerAddress, 2);

        int r0 = registers[0].getValue();
        int r1 = registers[1].getValue();
        byte[] bytes = new byte[4];

        bytes[0] = (byte) (r0 >> 8 & (byte)0xff);
        bytes[1] = (byte) (r0      & (byte)0xff);
        bytes[2] = (byte) (r1 >> 8 & (byte)0xff);
        bytes[3] = (byte) (r1      & (byte)0xff);

        if (Arrays.equals(bytes, NOT_CONFIGURED_FLOAT32)) {
            return null;
        }

        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
    }

    public int readS32(int registerAddress) throws ModbusException {
        if (!isConnected) {
            throw new ModbusIOException("Not connected");
        }
        final InputRegister[] registers = master.readMultipleRegisters(unitId, registerAddress, 2);
        long                  result    = registers[0].getValue();// & 0xffff;
        result = result << 16;
        result += registers[1].getValue() & 0xffff;
        return (int)result;
    }

    public String readASCII(int registerAddress, int len) throws ModbusException {
        if (!isConnected) {
            throw new ModbusIOException("Not connected");
        }
        final InputRegister[] registers = master.readMultipleRegisters(unitId, registerAddress, len);

        char[] chars = new char[len * 2];
        int offset = 0;
        for (InputRegister register: registers) {
            chars[offset] = (char)((register.getValue() & 0x7f00) >> 8);
            if (chars[offset]==0x00) break;
            offset++;
            chars[offset] = (char)(register.getValue() & 0x7f);
            if (chars[offset]==0x00) break;
            offset++;
        }
        return new String(chars, 0, offset);
    }

    private static final int MAX_REGISTERS_PER_MODBUS_REQUEST = 125;
    private int              maxRegistersPerModbusRequest     = MAX_REGISTERS_PER_MODBUS_REQUEST;

    public void setMaxRegistersPerModbusRequest(int newMaxRegistersPerModbusRequest) {
        if (newMaxRegistersPerModbusRequest <= 1 || newMaxRegistersPerModbusRequest >= MAX_REGISTERS_PER_MODBUS_REQUEST ) {
            throw new IllegalArgumentException("The value for MaxRegistersPerModbusRequest MUST be between 1 and " + MAX_REGISTERS_PER_MODBUS_REQUEST);
        }
        this.maxRegistersPerModbusRequest = newMaxRegistersPerModbusRequest;
    }

    public byte[] getRawRegisterBytes(int base, int len) throws ModbusException {
        byte[] bytes = new byte[len * 2];

        int i = 0;
        int remaining = len;
        while (remaining > 0) {
            int readSize = Math.min(remaining, maxRegistersPerModbusRequest);
            remaining -= readSize;
            final InputRegister[] registers = read(base, readSize);
            for (InputRegister register : registers) {
                byte[] registerBytes = register.toBytes();
                bytes[i++] = registerBytes[0];
                bytes[i++] = registerBytes[1];
            }
        }
        return bytes;
    }


    public void connect() throws Exception {
        if (!isConnected) {
            master.connect();
            isConnected = true;
        }
    }

    public void disconnect() {
        if (isConnected) {
            master.disconnect();
            isConnected = false;
        }
    }

    @Override
    public void close() {
        disconnect();
    }
}
