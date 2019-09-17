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

public class Model implements SunSpecType {
    public SunSpec parent;

    public int id;
    public int len;
    public String name;

    public Status status;

    public List<Block> blocks = new ArrayList<>();

    // Strings, only 1 language supported!
    public String label;
    public String description;
    public String notes;

    public String toYaml(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("- Model\n");
        sb.append(indent).append("  id     : ").append(id).append('\n');
        sb.append(indent).append("  len    : ").append(len).append('\n');
        if (status != null ) { sb.append(indent).append("  status : ").append(status).append('\n'); }
        if (name   != null ) { sb.append(indent).append("  name   : ").append(name).append('\n');   }
        blocks.forEach(p -> sb.append(p.toYaml(indent + "    ")));
        return sb.toString();
    }

}
