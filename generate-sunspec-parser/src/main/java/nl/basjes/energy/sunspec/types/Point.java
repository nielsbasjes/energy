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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Point implements SunSpecType {
    public Block parent;

    public String id; //       <xs:attribute name="id" type="xs:string" use="required" />
    public Integer len; //   <xs:attribute name="len" type="xs:integer" />
    public Integer offset; //   <xs:attribute name="offset" type="xs:integer" />
    public PointType type; //   <xs:attribute name="type" type="PointTypeDefinition" />
    public String sf; //   <xs:attribute name="sf" type="xs:string" />
    public String units; //   <xs:attribute name="units" type="xs:string" />
    public PointAccess access = PointAccess.readonly; //   <xs:attribute name="access" type="PointAccessDefinition" default="r" />
    public Boolean mandatory = false; //   <xs:attribute name="mandatory" type="xs:boolean" default="false"/>
    public Category category = Category.measurement; //   <xs:attribute name="category" type="CategoryDefinition" default="measurement"/>

    public List<Symbol> symbols = new ArrayList<>();

    // Duplicate datastructure for faster lookup by name
    public Map<String, Symbol> symbolLookup = new HashMap<>(16);

    // Strings, only 1 language supported!
    public String label;
    public String description;
    public String notes;

    public String toYaml(String indent) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent).append("- Point\n");
        if (id          != null ) {sb.append(indent).append("  id          : ").append(id).append('\n');          }
        if (len         != null ) {sb.append(indent).append("  len         : ").append(len).append('\n');         }
        if (offset      != null ) {sb.append(indent).append("  offset      : ").append(offset).append('\n');      }
        if (type        != null ) {sb.append(indent).append("  type        : ").append(type).append('\n');        }
        if (sf          != null ) {sb.append(indent).append("  sf          : ").append(sf).append('\n');          }
        if (units       != null ) {sb.append(indent).append("  units       : ").append(units).append('\n');       }
        if (access      != null ) {sb.append(indent).append("  access      : ").append(access ).append('\n');     }
        if (mandatory   != null ) {sb.append(indent).append("  mandatory   : ").append(mandatory ).append('\n');  }
        if (category    != null ) {sb.append(indent).append("  category    : ").append(category ).append('\n');   }
        if (label       != null ) {sb.append(indent).append("  label       : ").append(label).append('\n');       }
        if (description != null ) {sb.append(indent).append("  description : ").append(description).append('\n'); }
        if (notes       != null ) {sb.append(indent).append("  notes       : ").append(notes).append('\n');       }

        symbols.forEach(p -> sb.append(p.toYaml(indent + "    ")));
        return sb.toString();
    }

}
