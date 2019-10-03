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

import nl.basjes.energy.AbstractSunSpecProcessImage;

public class SunSpecBrokenTerminatorProcessImage extends AbstractSunSpecProcessImage {

        public SunSpecBrokenTerminatorProcessImage(Integer offset, Integer unit) {
            super(offset, unit);
        }

        @Override
        public byte[] getRawBytes() {

        // Data created to mimick the problem described in https://github.com/sunspec/models/issues/44
        byte[] bytes = {
            // The SunS header
            (byte)0x53, (byte)0x75, (byte)0x6E, (byte)0x53,

            // Model Id 1 at 40004.
            (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x42, // Model header
            // Model Id 1 is 66 bytes.
            (byte)0x53, (byte)0x4D, (byte)0x41, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x53, (byte)0x6F, (byte)0x6C, (byte)0x61, (byte)0x72, (byte)0x20, (byte)0x49, (byte)0x6E,
            (byte)0x76, (byte)0x65, (byte)0x72, (byte)0x74, (byte)0x65, (byte)0x72, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x39, (byte)0x34, (byte)0x30, (byte)0x32, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x31, (byte)0x2E, (byte)0x30, (byte)0x31, (byte)0x2E, (byte)0x33, (byte)0x32, (byte)0x2E,
            (byte)0x52, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x33, (byte)0x30, (byte)0x30, (byte)0x35, (byte)0x30, (byte)0x36, (byte)0x37, (byte)0x34,
            (byte)0x31, (byte)0x35, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xFF, (byte)0xFF, (byte)0x80, (byte)0x00,

            // Model Id 11 at 40072.
            (byte)0x00, (byte)0x0B, (byte)0x00, (byte)0x0D, // Model header
            // Model Id 11 is 13 bytes.
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0xFF, (byte)0xFF,
            (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFF,
            (byte)0xFF, (byte)0xFF,

            // The BROKEN End Model (i.e. "No more blocks" marker)
            // - BlockId == 0 --> is bad but apparently happens
            (byte)0x00, (byte)0x00
        };

        return bytes;
    }

}
