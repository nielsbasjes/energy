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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SunSpec implements SunSpecType {
    public String version;

    public List<Model> models = new ArrayList<>();

    // Duplicate datastructure for faster lookup by name
    public Map<Integer, Model> modelLookup = new TreeMap<>();

    public String toYaml(String indent) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent).append("- SunSpec\n");
        models.forEach(p -> sb.append(p.toYaml(indent + "    ")));
        return sb.toString();
    }

}
