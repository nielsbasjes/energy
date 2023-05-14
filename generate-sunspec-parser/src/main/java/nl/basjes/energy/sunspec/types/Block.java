/*
 * Energy readers and parsers toolkit
 * Copyright (C) 2019-2023 Niels Basjes
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Block implements SunSpecType {
    public Model parent;

    public int len;
    public BlockType type = BlockType.fixed;
    public String name;

    public List<Point> points = new ArrayList<>(16);

    // Duplicate datastructure for faster lookup by name
    public Map<String, Point> pointLookup = new HashMap<>(16);

    public String toYaml(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("- Block\n");
        sb.append(indent).append("  len: ").append(len).append('\n');
        sb.append(indent).append("  type: ").append(type).append('\n');
        sb.append(indent).append("  name: ").append(name).append('\n');
        points.forEach(p -> sb.append(p.toYaml(indent + "    ")));
        return sb.toString();
    }
}
