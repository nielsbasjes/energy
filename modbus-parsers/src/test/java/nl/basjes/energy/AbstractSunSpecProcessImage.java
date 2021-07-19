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

package nl.basjes.energy;

import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;

public abstract class AbstractSunSpecProcessImage extends SimpleProcessImage {

    public AbstractSunSpecProcessImage(Integer offset, Integer unit) {
        super(unit);
        byte[] bytes = getRawBytes();
        int i = 0;
        int register = offset;
        while ( i < bytes.length) {
            byte byte1 = bytes[i++];
            byte byte2 = bytes[i++];
            addRegister(register, new SimpleInputRegister(byte1, byte2));
            addInputRegister(register++, new SimpleInputRegister(byte1, byte2));
        }
    }

    public abstract byte[] getRawBytes();

}
