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

public class Symbol implements SunSpecType {
    public Point parent;

    public String id;
    public String value;

    // Strings, only 1 language supported!
    public String label;
    public String description;
    public String notes;

    public String toYaml(String indent) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent).append("- Symbol\n");
        sb.append(indent).append("  id          : ").append(id).append('\n');
        sb.append(indent).append("  value       : ").append(value).append('\n');
        if (label       !=null ) { sb.append(indent).append("  label       : ").append(label       ).append('\n'); }
        if (description !=null ) { sb.append(indent).append("  description : ").append(description ).append('\n'); }
        if (notes       !=null ) { sb.append(indent).append("  notes       : ").append(notes       ).append('\n'); }

        return sb.toString();
    }

}
