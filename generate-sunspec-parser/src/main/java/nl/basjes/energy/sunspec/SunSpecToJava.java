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

package nl.basjes.energy.sunspec;

import nl.basjes.energy.sunspec.types.Block;
import nl.basjes.energy.sunspec.types.Model;
import nl.basjes.energy.sunspec.types.Point;
import nl.basjes.energy.sunspec.types.SunSpec;
import nl.basjes.energy.sunspec.types.Symbol;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.basjes.energy.sunspec.types.BlockType.fixed;
import static nl.basjes.energy.sunspec.types.BlockType.repeating;
import static nl.basjes.energy.sunspec.types.PointType.bitfield16;
import static nl.basjes.energy.sunspec.types.PointType.bitfield32;
import static nl.basjes.energy.sunspec.types.PointType.enum16;
import static nl.basjes.energy.sunspec.types.PointType.enum32;
import static nl.basjes.energy.sunspec.types.PointType.eui48;
import static nl.basjes.energy.sunspec.types.PointType.float32;
import static nl.basjes.energy.sunspec.types.PointType.float64;
import static nl.basjes.energy.sunspec.types.PointType.pad;
import static nl.basjes.energy.sunspec.types.PointType.string;
import static nl.basjes.energy.sunspec.types.PointType.sunssf;

public class SunSpecToJava {

    public static void main(String... args) throws IOException {
        SunSpec tree = new SunSpec();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resourceArray = resolver.getResources("classpath*:**/smdx_*.xml");

            List<Resource> resources =
                Arrays
                    .stream(resourceArray)
                    .filter(r -> r.getFilename() != null)
                    .sorted(Comparator.comparing(Resource::getFilename))
                    .collect(Collectors.toList());

            for (Resource resource : resources) {
                String content = IOUtils.toString(resource.getInputStream(), UTF_8);
                ParseXMLSpec.parse(tree, content);
            }
        } catch (IOException e) {
            throw new IOException("Error reading resources: " + e.getMessage(), e);
        }

        System.out.println(generateJavaParser(tree));
    }

    private static String cleanSymbol(Symbol symbol) {
        String result = symbol.id.replaceAll("-", "_").replaceAll("%", "Perc").replaceAll(" ", "_");
        if ("RESERVED".equals(result)) {
            result += '_' + symbol.value;
        }
        return result;
    }

    private static String genThrows(Set<String> exceptions, String... extraExceptions) {
        if (exceptions.isEmpty() && extraExceptions.length == 0) {
            return "";
        }

        Set<String> allExceptions = new HashSet<>(exceptions);
        allExceptions.addAll(Arrays.asList(extraExceptions));
        return "throws " + String.join(",", allExceptions);
    }

    private static String getCleanedLabel(String label) {
        return label
            .replaceAll(" ", "")
            .replaceAll("'", "")
            .replaceAll("/", "_")
            .replaceAll("\\(", "_")
            .replaceAll("\\)", "")
            .replaceAll("-h", "H") // Note: We really wanted something like this      s@-(.)@\U\1\E@g
            .replaceAll("-u", "U")
            .replaceAll("-([A-Z])", "$1");
    }

    private static String enumName(Point point) {
        String enumName = point.id;

        if (point.label != null && !point.label.isEmpty()) {
            enumName = getCleanedLabel(point.label);
        }

        return enumName;
    }

    private static String functionName(Point point) {
        String functionName = point.id;

        if (point.type == sunssf) {
            return functionName;
        }

        if (point.label != null && !point.label.isEmpty()) {
            functionName = getCleanedLabel(point.label);
        }

        if (point.parent.type == repeating) {
            functionName = "Repeating" + functionName;
        }
        return functionName;
    }

    private static String modelComment(Model model) {
        return getCommentString(model.label, model.description, model.notes);
    }

    private static String pointComment(Point point) {
        return getCommentString(point.label, point.description, point.notes);
    }

    private static String symbolComment(Symbol symbol) {
        return getCommentString(symbol.label, symbol.description, symbol.notes);
    }

    private static String getCommentString(String label, String description, String notes) {
        StringBuilder sb = new StringBuilder();
        if ((label       != null ) && !label       .isEmpty()) { sb.append(" | ") .append(label       .replaceAll("\"","'")) ; }
        if ((description != null ) && !description .isEmpty()) { sb.append(" | ") .append(description .replaceAll("\"","'")) ; }
        if ((notes       != null ) && !notes       .isEmpty()) { sb.append(" | ") .append(notes       .replaceAll("\"","'")) ; }

        String result = sb.toString();
        if (result.isEmpty()) {
            return null;
        }
        return result;
    }


    private static String generateJavaParser(SunSpec tree) {
        StringBuilder sb = new StringBuilder(128 * 1024);
        sb.append(header);
        sb.append("public class ParseSunSpec {\n");

        List<Integer> modelList = new ArrayList<>();


        for (Model model : tree.models) {

////             Skip the unwanted models
//            if (model.id ==   801 || // Deprecated Energy Storage Base Model
//                model.id == 63001 || // SunSpec Test Model 1
//                model.id == 63002) { // SunSpec Test Model 2
//                continue;
//            }

            modelList.add(model.id);

            sb
                .append("\n")
                .append("    // ================================================================================== \n")
                .append("    /**\n")
                .append("     * SunSpec Model ").append(model.id).append(" : ").append(model.label).append(" | ").append(model.description).append("\n")
                .append("     */\n");

            sb.append("    public static class Model_").append(model.id).append(" extends ModelFetcher {\n");

            sb.append("        public Model_").append(model.id).append("() { super(null, ").append(model.id).append("); };\n");
            sb.append("        public Model_").append(model.id).append("(SunSpecModbusDataReader dataReader){ super(dataReader, ").append(model.id).append("); };\n");

            sb.append("        public int    getId()          { return ").append(model.id).append("; }\n");

            sb.append("        public String getLabel()       { return \"").append(model.label).append("\";}\n");
            sb.append("        public String getDescription() { return \"").append(model.description).append("\";}\n");
            sb.append("        public String getNotes()       { return \"").append(model.notes).append("\";}\n");

            if (model.blocks.size() > 2) {
                throw new IllegalStateException("A model can only have 1 or 2 blocks");
            }

            int fixedBlockLen  = 0;
            int repeatBlockLen = 0;
            for (Block block : model.blocks) {
                switch (block.type) {
                    case fixed:
                        fixedBlockLen = block.len;
                        break;
                    case repeating:
                        repeatBlockLen = block.len;
                        break;
                }
            }

            // If there is no fixed block we still need the toString and toHashMap functions
            if (fixedBlockLen == 0) {
                // Create the toString function
                sb.append("        public String toString(byte[] dataBlock) throws MissingMandatoryFieldException {\n");
                sb.append("            StringBuilder sb = new StringBuilder();\n");
                sb.append("            sb.append(\"- Model_").append(model.id);

                String modelComment = modelComment(model);
                if (modelComment != null) {
                    sb.append("   // ").append(modelComment);
                }
                sb.append("\\n\");\n");

                sb.append("            for( int index = 0 ; index < (dataBlock.length/2)/").append(repeatBlockLen).append("; index++) { \n");
                sb.append("                sb.append(toString(dataBlock, index));\n");
                sb.append("            }\n");
                sb.append("            return sb.toString();\n");
                sb.append("        }\n");

                sb.append("        public Map<String, Object> toHashMap(byte[] dataBlock) throws MissingMandatoryFieldException {\n");
                sb.append("            Map<String, Object> result = new LinkedHashMap<>();\n");
                sb.append("            for( int index = 0 ; index < (dataBlock.length/2)/").append(repeatBlockLen).append("; index++) { \n");
                sb.append("                result.putAll(toHashMap(dataBlock, index));\n");
                sb.append("            }\n");
                sb.append("            return result;\n");
                sb.append("        }\n");

            }

            for (Block block : model.blocks) {
                Set<String> throwsToString = new HashSet<>();

                Set<String> scalingFactors = new HashSet<>();

                for (Point point : block.points) {
                    if (!NumberUtils.isNumber(point.sf)) {
                        scalingFactors.add(point.sf);
                    }
                    if (point.type == sunssf) {
                        scalingFactors.add(point.id);
                    }
                }

                String declRepeatParam       = "";
                String passRepeatParam       = "";
                String declRepeatParamSingle = "";
                String hashMapRepeatParam    = "|-";
                if (block.type == repeating) {
                    declRepeatParam = ", int index";
                    passRepeatParam = ", index";
                    declRepeatParamSingle = "int index";
                    hashMapRepeatParam = "|\"+index+\"";
                }

                for (Point point : block.points) {
                    String      returntype = "Long";
                    Set<String> throwsSpec = new HashSet<>();
                    switch (point.type) {
                        case int16:
                        case sunssf:
                            returntype = "Short";
                            break;

                        case uint16:
                        case acc16:
                        case count:
                        case int32:
                            returntype = "Integer";
                            break;

                        case pad:
                            continue; // We do NOT want to have a getter for padding.

                        case uint32:
                        case acc32:
                        case enum32: // We do not support a custom enum for this.
                        case int64:
                        case uint64:
                        case acc64:
                            returntype = "Long";
                            break;

                        case enum16:
                            if (point.symbols.isEmpty()) {
                                returntype = "Integer";
                            } else {
                                returntype = enumName(point);
                            }
                            break;

                        case bitfield16:
                            if (point.symbols.isEmpty()) {
                                returntype = "Integer";
                            } else {
                                returntype = "EnumSet<" + enumName(point) + ">";
                            }
                            break;

                        case bitfield32:
                            if (point.symbols.isEmpty()) {
                                returntype = "Long";
                            } else {
                                returntype = "EnumSet<" + enumName(point) + ">";
                            }
                            break;

                        case eui48: // Since eui48 is undocumented we return it as a String
                        case string:
                            returntype = "String";
                            break;

                        case float32:
                            returntype = "Float";
                            break;

                        case float64:
                            returntype = "Double";
                            break;

                        case ipaddr:
                        case ipv6addr:
                            returntype = "InetAddress";
                            throwsSpec.add("UnknownHostException");
                            throwsToString.add("UnknownHostException");
                            break;

                    }

                    if (point.mandatory) {
                        throwsToString.add("MissingMandatoryFieldException");
                        throwsSpec.add("MissingMandatoryFieldException");
                    }

                    String functionName = functionName(point);
                    String offset       = "" + point.offset;
                    if (block.type == repeating) {
                        offset = fixedBlockLen + "+(index*" + repeatBlockLen + ")+" + point.offset;
                    }

                    // Note: enum32 never seems to have any values because it is always a vendor field
                    if ((point.type == enum32) && !point.symbols.isEmpty()) {
                        // So this simply does not yet occur in the specs (it would be silly!)
                        // and actually implementing this causes troubles in the generated code.
                        throw new NotImplementedException("There is no support for a 32 bit enum and a list of symbols for that.");
                    }


                    if ((point.type == enum16) && !point.symbols.isEmpty()) {
                        sb.append("        public static enum ").append(enumName(point)).append(" {\n");
                        sb.append("            __INVALID__");
                        for (Symbol symbol : point.symbols) {
                            sb.append(", ").append(cleanSymbol(symbol));
                        }
                        sb.append(";\n");

                        sb.append("            public static ").append(enumName(point)).append(" of(Integer id) { \n");
                        sb.append("                if (id==null) { return null; }\n");
                        sb.append("                switch (id) {\n");
                        for (Symbol symbol : point.symbols) {
                            sb.append("                    case ").append(symbol.value).append(":  return ").append(cleanSymbol(symbol)).append(";");
                            String description = symbolComment(symbol);
                            if (description != null) {
                                sb.append(" // ").append(description);
                            }
                            sb.append("\n");
                        }
                        sb.append("                    default:   return null;\n");
                        sb.append("                }\n");
                        sb.append("            }\n");
                        sb.append("        }\n");
                    }

                    if ((point.type == bitfield16 || point.type == bitfield32) && !point.symbols.isEmpty()) {
                        sb.append("        public static enum ").append(enumName(point)).append(" {\n");
                        sb.append("            __INVALID__");

                        // Validate that we do not have conflicting ids (for an enum the Java code will fail).
                        Set<String> ids = new HashSet<>();
                        for (Symbol symbol : point.symbols) {
                            ids.add(symbol.value);
                        }
                        if (point.symbols.size() != ids.size()) {
                            throw new IllegalStateException(
                                "In " + model.id + " -> " + block.type + " -> " + point.id + "(" + point.type.name() + ")" +
                                    " has non-unique symbols.");
                        }

                        for (Symbol symbol : point.symbols) {
                            sb.append(", ").append(cleanSymbol(symbol));
                        }
                        sb.append(";\n");

                        if (point.type == bitfield16) {
                            sb.append("            public static ").append(returntype).append(" of(Integer bits) { \n");
                        }

                        if (point.type == bitfield32) {
                            sb.append("            public static ").append(returntype).append(" of(Long bits) { \n");
                        }
                        sb.append("                if (bits==null) { return null; }\n");
                        sb.append("                ").append(returntype).append(" result = EnumSet.noneOf(").append(enumName(point)).append(".class);\n");
                        for (Symbol symbol : point.symbols) {
                            sb.append("                if (((bits >>> ").append(symbol.value).append(") & 1) == 1) { result.add(").append(cleanSymbol(symbol)).append("); }\n");
                        }
                        sb.append("                return result;\n");
                        sb.append("            }\n");
                        sb.append("        }\n");
                    }
                    if ((point.label != null && !point.label.isEmpty()) ||
                        (point.description != null && !point.description.isEmpty()) ||
                        (point.notes != null && !point.notes.isEmpty())) {
                        sb.append("        /**\n");
                        if (point.label       != null && !point.label      .isEmpty()) { sb.append("         * ").append(point.label)       .append('\n'); }
                        if (point.description != null && !point.description.isEmpty()) { sb.append("         * ").append(point.description) .append('\n'); }
                        if (point.notes       != null && !point.notes      .isEmpty()) { sb.append("         * ").append(point.notes)       .append('\n'); }
                        sb.append("         */\n");
                    }

                    if ((point.type == bitfield16 || point.type == bitfield32 || point.type == enum16 || point.type == enum32) && !point.symbols.isEmpty()) {
                        sb
                            .append("        public ").append(returntype).append(" get").append(functionName)
                            .append("(").append(declRepeatParamSingle).append(") ").append(genThrows(throwsSpec, "ModbusException"))
                            .append(" { return ").append(" get").append(functionName).append("(getCurrentData()").append(passRepeatParam).append("); }\n");

                        sb.append("        public ").append(returntype).append(" get").append(functionName);
                        sb.append("(byte[] dataBlock").append(declRepeatParam).append(") ").append(genThrows(throwsSpec)).append(" { ")
                            .append(returntype).append(" result = ").append(enumName(point)).append(".of(");
                        sb.append(point.type).append("(dataBlock,  ").append(offset).append(")); ");

                        if (point.mandatory) {
                            if ((point.type == enum16 || point.type == enum32) && !point.symbols.isEmpty()) {
                                sb.append("result = throwIfNull(\"").append(model.id).append("\",\"").append(point.id).append("\", result, ").append(enumName(point)).append(".__INVALID__); ");
                            }
                            if ((point.type == bitfield16 || point.type == bitfield32) && !point.symbols.isEmpty()) {
                                sb.append("result = throwIfNull(\"").append(model.id).append("\",\"").append(point.id).append("\", result, EnumSet.of(").append(enumName(point)).append(".__INVALID__)); ");
                            }
                        }
                        sb.append("return result; }\n");
                    } else {
                        if (point.sf != null) {
                            Optional<Point> optionalSF = model.blocks.stream().map(b -> b.pointLookup.get(point.sf)).filter(Objects::nonNull).findFirst();

                            boolean mandatory = point.mandatory || (optionalSF.isPresent() && optionalSF.get().mandatory);
                            if (mandatory) {
                                throwsSpec.add("MissingMandatoryFieldException");
                                throwsToString.add("MissingMandatoryFieldException");
                            }

                            sb
                                .append("        public Double get").append(functionName)
                                .append("(").append(declRepeatParamSingle).append(") ").append(genThrows(throwsSpec, "ModbusException"))
                                .append(" { return ").append(" get").append(functionName).append("(getCurrentData()").append(passRepeatParam).append("); }\n");

                            sb
                                .append("        public Double get").append(functionName)
                                .append("(byte[] dataBlock").append(declRepeatParam).append(") ").append(genThrows(throwsSpec))
                                .append(" { Double result = calculateScaledValue(")
                                .append("get").append(functionName).append("__RAW(dataBlock").append(passRepeatParam).append("),");

                            if (NumberUtils.isNumber(point.sf)) {
                                sb.append("(short)").append(point.sf);
                            } else {
                                if (!optionalSF.isPresent()) {
                                    throw new IllegalStateException(
                                        "In " + model.id + " -> " + block.type + " -> " + point.id + "(" + point.type.name() + ")" +
                                            " requires SF \"" + point.sf + "\" which does not exist.");
                                }

                                Point sf = optionalSF.get();
                                if (sf.parent.type == repeating) {
                                    sb.append("get").append(functionName(sf)).append("(dataBlock, index)");
                                } else {
                                    sb.append("get").append(functionName(sf)).append("(dataBlock)");
                                }
                            }
                            sb.append(");");

                            if (point.mandatory) {
                                sb.append("result = throwIfNull(\"").append(model.id).append("\",\"").append(point.id).append("\", result); ");
                            }
                            sb.append("return result;}\n");
                            sb.append("        private ").append(returntype).append(" get").append(functionName).append("__RAW");
                        } else {

                            String access = "public";

                            if (scalingFactors.contains(point.id)) {
                                access = "private";
                            }
                            sb.append("        ").append(access).append(" ").append(returntype).append(" get").append(functionName)
                                .append("(").append(declRepeatParamSingle).append(") ").append(genThrows(throwsSpec, "ModbusException"))
                                .append(" { return ").append(" get").append(functionName).append("(getCurrentData()").append(passRepeatParam).append("); }\n");

                            sb.append("        ").append(access).append(" ").append(returntype).append(" get").append(functionName);
                        }
                        sb.append("(byte[] dataBlock").append(declRepeatParam).append(")").append(" ").append(genThrows(throwsSpec)).append(" { ");
                        sb.append(returntype).append(" result = ");
                        if (point.type == string) {
                            sb.append(point.type).append("(dataBlock,  ").append(offset).append(", ").append(point.len).append("); ");
                        } else {
                            sb.append(point.type).append("(dataBlock,  ").append(offset).append("); ");
                        }
                        if (point.mandatory) {
                            sb.append("result = throwIfNull(\"").append(model.id).append("\",\"").append(point.id).append("\", result); ");
                        }
                        sb.append(" return result; }\n");
                    }
                }

                // Create the toString function
                sb.append("        public String toString(byte[] dataBlock").append(declRepeatParam).append(") ").append(genThrows(throwsToString)).append(" {\n");
                sb.append("            StringBuilder sb = new StringBuilder();\n");

                String indent = "";
                if (block.type == fixed) {
                    sb.append("            sb.append(\"- Model_").append(model.id);
                } else {
                    sb.append("            sb.append(\"    - Repeat block \").append(index).append(\" of Model_").append(model.id);
                    indent = "    ";
                }

                String modelComment = modelComment(model);
                if (modelComment != null) {
                    sb.append("   // ").append(modelComment);
                }
                sb.append("\\n\");\n");

                int maxNameLen = 20;
//                for (Point point: block.points) {
//                    if (!scalingFactors.contains(point.id) && point.type != pad) {
//                        maxNameLen = Math.max(maxNameLen, point.id.length());
//                    }
//                }

                for (Point point : block.points) {
                    if (point.type == pad) {
                        continue;
                    }
                    if (scalingFactors.contains(point.id)) {
                        continue;
                    }
                    String functionName = functionName(point);

                    if (point.type == string) {
                        sb.append("            String ").append(point.id).append(" = get").append(functionName).append("(dataBlock").append(passRepeatParam).append(");");
                        if (point.mandatory) {
                            sb.append(" if (true) { ");
                        } else {
                            sb.append(" if (").append(point.id).append(" != null && !").append(point.id).append(".isEmpty()) { ");
                        }
                    } else {
                        sb.append("            Object ").append(point.id).append(" = get").append(functionName).append("(dataBlock").append(passRepeatParam).append(");");
                        if (point.mandatory) {
                            sb.append(" if (true) { ");
                        } else {
                            sb.append(" if (").append(point.id).append(" != null) { ");
                        }
                    }
                    sb.append(" sb.append(\"    ").append(indent).append(format(maxNameLen, point.id)).append("  : \")");

                    if (point.type == string ||
                        point.type == eui48 ||
                        point.type == enum16 ||
                        point.type == enum32 ||
                        point.type == bitfield16 ||
                        point.type == bitfield32) {
                        sb.append(".append(String.format(\"%-21s\",").append(point.id).append("))");
                    } else {
                        if (point.type == float32 || point.type == float64 || point.sf != null) {
                            sb.append(".append(String.format(\"%10.2f\",").append(point.id).append("))");
                        } else {
                            sb.append(".append(String.format(\"%10d\",").append(point.id).append("))");
                        }

                        if (point.units != null) {
                            sb.append(".append(\"").append(String.format(" %-10s", point.units)).append("\")");
                        } else {
                            sb.append(".append(\"           \")");
                        }
                    }

                    String pointComment = pointComment(point);
                    if (pointComment != null) {
                        sb.append(".append(\"   // ").append(pointComment).append("\\n\")");
                    } else {
                        sb.append(".append(\"\\n\")");
                    }
                    sb.append("; }\n");
                }

                if (block.type == fixed && repeatBlockLen != 0) {
                    sb.append("            for( int index = 0 ; index < ((dataBlock.length/2) - ").append(fixedBlockLen).append(")/").append(repeatBlockLen).append("; index++) {\n");
                    sb.append("                sb.append(toString(dataBlock, index));\n");
                    sb.append("            }\n");
                }
                sb.append("            return sb.toString();\n");
                sb.append("        }\n");

                // Create the toHashMap function
                sb.append("        /**\n");
                sb.append("         * The returned value can be Short, Integer, Long, String, Float, Double, InetAddress and in some cases a custom enum or EnumSet<custom enum>.\n");
                sb.append("         */\n");
                sb.append("        public Map<String, Object> toHashMap(byte[] dataBlock").append(declRepeatParam).append(") ").append(genThrows(throwsToString)).append(" {\n");
                sb.append("            Map<String, Object> result = new LinkedHashMap<>();\n");

                for (Point point : block.points) {
                    if (point.type == pad) {
                        continue;
                    }

                    if (scalingFactors.contains(point.id)) {
                        continue;
                    }
                    String functionName = functionName(point);

                    sb.append("            Object ").append(point.id).append(" = get").append(functionName).append("(dataBlock").append(passRepeatParam).append("); ");
                    sb.append("if (").append(point.id).append(" != null) { result.put(\"").append(model.id).append(hashMapRepeatParam).append("|").append(enumName(point));
                    sb.append("|");
                    if (point.units != null && !point.units.isEmpty()) {
                        sb.append(point.units.replaceAll(Pattern.quote("|"), "_"));
                    }
                    sb.append("\", ").append(point.id).append("); }\n");
                }
                if (block.type == fixed && repeatBlockLen != 0) {
                    sb.append("            for( int index = 0 ; index < ((dataBlock.length/2) - ").append(fixedBlockLen).append(")/").append(repeatBlockLen).append("; index++) {\n");
                    sb.append("                result.putAll(toHashMap(dataBlock, index));\n");
                    sb.append("            }\n");
                }
                sb.append("            return result;\n");
                sb.append("        }\n");
            }
            sb.append("    }\n");
        }


        sb.append("\n");
        sb.append("\n");
        sb.append("    private static Map<Integer, ModelParser> MODEL_PARSERS = null;\n");
        sb.append("    /**\n");
        sb.append("     * Create a map with all parsers\n");
        sb.append("     */\n");
        sb.append("    public static Map<Integer, ModelParser> modelParsers() {\n");
        sb.append("        if (MODEL_PARSERS == null) {\n");
        sb.append("            MODEL_PARSERS = new LinkedHashMap<>();\n");
        for (Integer modelId : modelList) {
            sb.append("            MODEL_PARSERS.put(").append(modelId).append(", new Model_").append(modelId).append("());\n");
        }
        sb.append("        }\n");
        sb.append("        return MODEL_PARSERS;\n");
        sb.append("    }\n");

        sb.append("\n");
        sb.append("    public static class ModelParserHolder {\n");
        sb.append("        protected Map<Integer, ModelParser> usedModelParsers = null;\n");
        sb.append("        public ModelParserHolder() {\n");
        sb.append("            usedModelParsers = new LinkedHashMap<>();\n");
        sb.append("        }\n");

        for (Integer modelId : modelList) {
            sb.append("        public Model_").append(modelId).append(" model_").append(modelId).append(" = null;\n");
        }

        sb.append("        public ModelFetcher getModelFetcher(SunSpecModbusDataReader dataReader, int modelId) {\n");
        sb.append("            switch(modelId) {\n");
        for (Integer modelId : modelList) {
            sb.append("            case ").append(modelId).append(": model_").append(modelId).append(" = new Model_").append(modelId).append("(dataReader); return model_").append(modelId).append(";\n");
        }
        sb.append("            default:  throw new UnsupportedOperationException(\"The requested SunSpec model \" + modelId + \" does not exist.\");\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("    }\n");


        sb.append("}\n");
        return sb.toString();
    }

    private static String format(int len, String value) {
        return String.format("%-" + len + "s", value);
    }


    private static final String header = "/*\n" +
        " *\n" +
        " * Energy readers and parsers toolkit\n" +
        " * Copyright (C) 2019-2023 Niels Basjes\n" +
        " *\n" +
        " * This work is licensed under the Creative Commons\n" +
        " * Attribution-NonCommercial-NoDerivatives 4.0 International License.\n" +
        " *\n" +
        " * You may not use this file except in compliance with the License.\n" +
        " * You may obtain a copy of the License at\n" +
        " *\n" +
        " *    https://creativecommons.org/licenses/by-nc-nd/4.0/\n" +
        " *\n" +
        " * Unless required by applicable law or agreed to in writing, software\n" +
        " * distributed under the License is distributed on an AS IS BASIS,\n" +
        " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
        " * See the License for the specific language governing permissions and\n" +
        " * limitations under the License.\n" +
        " */\n" +
        "\n" +
        "// ===========================================================\n" +
        "//               !!! THIS IS GENERATED CODE !!!\n" +
        "// -----------------------------------------------------------\n" +
        "//       EVERY TIME THE SOFTWARE IS BUILD THIS FILE IS \n" +
        "//        REGENERATED AND ALL MANUAL CHANGES ARE LOST\n" +
        "// ===========================================================\n" +
        "\n" +
        "package nl.basjes.energy.sunspec;\n" +
        "\n" +
        "import java.net.InetAddress;\n" +
        "import java.net.UnknownHostException;\n" +
        "import java.util.EnumSet;\n" +
        "import java.util.LinkedHashMap;\n" +
        "import java.util.Map;\n" +
        "\n" +
        "import com.ghgande.j2mod.modbus.ModbusException;\n" +
        "\n";

}
