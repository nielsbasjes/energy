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

package nl.basjes.energy.sunspec.types;

public enum PointType {
    int16,
    uint16,
    count,
    acc16,
    int32,
    uint32,
    float32,
    acc32,
    int64,
    uint64,
    float64,
    acc64,
    enum16,
    enum32,
    bitfield16,
    bitfield32,
    sunssf,
    string,
    pad,
    ipaddr,
    ipv6addr,
    eui48;

    public static PointType of(String name) {
        switch (name) {
            case "int16":
                return int16;
            case "uint16":
                return uint16;
            case "count":
                return count;
            case "acc16":
                return acc16;
            case "int32":
                return int32;
            case "uint32":
                return uint32;
            case "float32":
                return float32;
            case "acc32":
                return acc32;
            case "int64":
                return int64;
            case "uint64":
                return uint64;
            case "float64":
                return float64;
            case "acc64":
                return acc64;
            case "enum16":
                return enum16;
            case "enum32":
                return enum32;
            case "bitfield16":
                return bitfield16;
            case "bitfield32":
                return bitfield32;
            case "sunssf":
                return sunssf;
            case "string":
                return string;
            case "pad":
                return pad;
            case "ipaddr":
                return ipaddr;
            case "ipv6addr":
                return ipv6addr;
            case "eui48":
                return eui48;
            default:
                return null;
        }
    }

}























