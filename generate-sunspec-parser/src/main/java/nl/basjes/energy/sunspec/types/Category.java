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

public enum Category {
    none,
    measurement,
    metered,
    status,
    event,
    setting,
    control;

    public static Category of(String name) {
        switch (name) {
            case "none":
                return none;
            case "measurement":
                return measurement;
            case "metered":
                return metered;
            case "status":
                return status;
            case "event":
                return event;
            case "setting":
                return setting;
            case "control":
                return control;
            default:
                return null;
        }
    }

}























