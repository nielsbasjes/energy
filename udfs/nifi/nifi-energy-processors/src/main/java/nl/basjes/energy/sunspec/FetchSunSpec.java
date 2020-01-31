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

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.PrimaryNodeOnly;
import org.apache.nifi.annotation.behavior.TriggerSerially;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnRemoved;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.annotation.lifecycle.OnUnscheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_STARTBASE;
import static nl.basjes.energy.sunspec.SunSpecModbusDataReader.SUNSPEC_STANDARD_UNITID;
import static org.apache.nifi.annotation.behavior.InputRequirement.Requirement.INPUT_FORBIDDEN;

@Tags({"input", "ingest", "fetch", "energy", "SunSpec"})
@CapabilityDescription("Fetch data from a SunSpec compliant device")
@SeeAlso({})
@TriggerSerially
@PrimaryNodeOnly // This may ONLY be read single threaded
@InputRequirement(INPUT_FORBIDDEN) // ONLY read data from the configured device
//@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
//@WritesAttributes({@WritesAttribute(attribute = "", description = "")})
public class FetchSunSpec extends AbstractProcessor {

    public static final PropertyDescriptor HOSTNAME = new PropertyDescriptor
        .Builder().name("HOSTNAME")
        .displayName("Hostname/ip address")
        .description("The hostname or IP address of the Sunspec compliant device.")
        .required(true)
        .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
        .build();

    public static final PropertyDescriptor PORT = new PropertyDescriptor
        .Builder().name("PORT")
        .displayName("TCP port")
        .description("The tcp port on which the device listens to SunSpec/Modbus.")
        .required(true)
        .defaultValue("502")
        .addValidator(StandardValidators.PORT_VALIDATOR)
        .build();

    public static final PropertyDescriptor SUNSPEC_REGISTER_BASE = new PropertyDescriptor
        .Builder().name("SUNSPEC_REGISTER_BASE")
        .displayName("The SunSpec start register")
        .description("The ModBus register address where the SunSpec header resides.")
        .required(true)
        .defaultValue(String.valueOf(SUNSPEC_STANDARD_STARTBASE))
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .build();

    public static final PropertyDescriptor SUNSPEC_UNITID = new PropertyDescriptor
        .Builder().name("SUNSPEC_UNITID")
        .displayName("The SunSpec unid id")
        .description("The ModBus unitid where the SunSpec registers reside.")
        .required(true)
        .defaultValue(String.valueOf(SUNSPEC_STANDARD_UNITID))
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .build();

    public static final PropertyDescriptor FETCH_INTERVAL = new PropertyDescriptor
        .Builder().name("FETCH_INTERVAL")
        .displayName("Fetch interval (ms)")
        .description("Every how many milliseconds should we retrieve the data from the Sunspec device.")
        .required(true)
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .allowableValues("500", "1000", "5000", "10000", "60000")
        .defaultValue("1000")
        .build();

    public static final Relationship SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Here we route all FlowFiles that have been analyzed.")
        .build();

    private List<PropertyDescriptor> descriptors;

    private Map<String, ModelParser> modelProperties;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        modelProperties = new LinkedHashMap<>();

        descriptors.add(HOSTNAME);
        descriptors.add(PORT);
        descriptors.add(FETCH_INTERVAL);
        descriptors.add(SUNSPEC_REGISTER_BASE);
        descriptors.add(SUNSPEC_UNITID);

        for (Map.Entry<Integer, ModelParser> entry : ParseSunSpec.modelParsers().entrySet()) {
            ModelParser m = entry.getValue();

            String description = "Model " + m.getId();
            if (m.getLabel() != null) { description += " | " + m.getLabel(); }
            if (m.getDescription() != null) { description += " | " + m.getDescription(); }
            if (m.getNotes() != null) { description += " | " + m.getNotes(); }

            String name = "SunSpec Model " + m.getId();
            String displayName = "Fetch model "+m.getId() + " : " + m.getLabel();
            PropertyDescriptor.Builder modelDescriptorBuilder = new PropertyDescriptor
                .Builder()
                .name(name)
                .displayName(displayName)
                .description(description)
                .required(true);

            boolean isMandatory = m.getId() == 1;

            if (isMandatory) {
                modelDescriptorBuilder
                    .displayName(displayName + " (Always fetched)")
                    .allowableValues("true")
                    .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
                    .defaultValue("true");
            } else {
                modelDescriptorBuilder
                    .allowableValues("true", "false")
                    .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
                    .defaultValue("false");
            }

            PropertyDescriptor modelDescriptor = modelDescriptorBuilder.build();
            modelProperties.put(name, m);
            descriptors.add(modelDescriptor);
        }
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(SUCCESS);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    private SunSpecFetcher fetcher = null;
    private SunSpecModbusDataReader dataReader = null;
    private long fetchInterval = 1000;

    @OnScheduled
    public void onScheduled(final ProcessContext context) throws ModbusException {
        if (fetcher == null) {
            String hostname = context.getProperty(HOSTNAME).getValue();
            Integer port = context.getProperty(PORT).asInteger();
            fetchInterval = context.getProperty(FETCH_INTERVAL).asLong();
            Integer registerBase = context.getProperty(SUNSPEC_REGISTER_BASE).asInteger();
            Integer unitId = context.getProperty(SUNSPEC_UNITID).asInteger();

            dataReader = new SunSpecModbusDataReader(
                new ModbusTCPMaster(hostname, port),
                registerBase, unitId
                );

            fetcher = new SunSpecFetcher(dataReader);

            for (PropertyDescriptor propertyDescriptor: descriptors) {
                if (context.getProperty(propertyDescriptor).asBoolean()) {
                    String name = propertyDescriptor.getName();
                    ModelParser modelParser = modelProperties.get(name);
                    if (modelParser != null) { // Should always pass
                        int modelId = modelParser.getId();
                        if (dataReader.getModelLocations().get(modelId) != null) {
                            fetcher.useModel(modelId);
                        }
                    }
                }
            }
        }
    }

    @OnRemoved
    @OnStopped
    @OnUnscheduled
    public void OnUnscheduled(ProcessContext context) {
        if (fetcher != null) {
            fetcher = null;
        }
        if (dataReader != null) {
            dataReader.disconnect();
            dataReader = null;
        }
    }

    public static final String ATTRIBUTE_PREFIX = "SunSpec|";

    public String cleanKey(String key) {
        return key
            .replace("%", "Pct")
            .replace("%", "Pct")
            .replace("%", "Pct")
            .replace("()", "")
            .replace("/", "_")
            .replaceAll("[^a-zA-Z0-9_]", "_");
    }


    private boolean first = true;

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        if (fetcher == null || dataReader == null) {
            System.err.println("Fatal error in onTrigger: The fetcher is null...");
            return;
        }

        long timestamp;
        try {
            fetcher.refresh(fetchInterval);

            if (fetcher == null || dataReader == null) {
                System.err.println("Aborting...");
                return;
            }

            timestamp = fetcher.getCurrentDataTimestamp();
        } catch (ModbusException e) {
            return; // FIXME: Handle this Oops.  For now ignore
        }

        Map<String, String> results = new LinkedHashMap<>();
        FlowFile flowFile = session.create();

        try {
            session.putAttribute(flowFile, "filename", fetcher.model_1.getManufacturer() + " " + fetcher.model_1.getModel() + " " + fetcher.model_1.getSerialNumber());
        } catch (MissingMandatoryFieldException | ModbusException e) {
            // FIXME: Handle this Oops. For now ignore
        }

        String timeKey = cleanKey(ATTRIBUTE_PREFIX + "0|-|TimeStamp|");
        results.put(timeKey, String.valueOf(timestamp));

        final Map<String, Object> rawResults = fetcher.toHashMap();
        rawResults.forEach((k, v) -> results.put(cleanKey(ATTRIBUTE_PREFIX + k), v.toString()));

        flowFile = session.putAllAttributes(flowFile, results);

        session.getProvenanceReporter().modifyAttributes(flowFile);

        session.transfer(flowFile, SUCCESS);


        // FIXME: Experimental: Output the InfluxDB line protocol on the first record.
        if (first) {
            StringBuilder sb = new StringBuilder();

            sb.append("\n========================================\n");

            sb.append("electricity,equipmentId=${SunSpec_1___Manufacturer_:replaceAll('[^a-zA-Z0-9_]','_')}_${SunSpec_1___Model_:replaceAll('[^a-zA-Z0-9_]','_')}_${SunSpec_1___SerialNumber_:replaceAll('[^a-zA-Z0-9_]','_')} ");

            boolean firstEntry = true;
            for (Map.Entry<String, Object> entry : rawResults.entrySet()) {
                if (!firstEntry) {
                    sb.append(',');
                }
                firstEntry=false;

                String k = cleanKey(ATTRIBUTE_PREFIX + entry.getKey());
                Object value = entry.getValue();

                if (value instanceof Short || value instanceof Integer || value instanceof Long) {
                    sb.append(k).append("=${").append(k).append(":isNull():ifElse('0',${").append(k).append("})}i");
                    continue;
                }

                if (value instanceof Float || value instanceof Double) {
                    sb.append(k).append("=${").append(k).append(":isNull():ifElse('0.0',${").append(k).append("})}");
                    continue;
                }

                // In all other cases we can only store it as a String (which is simply empty if null).
                sb.append(k).append("=\"${").append(k).append("}\"");

            }

            sb.append(" ${").append(timeKey).append("}000000");

            sb.append("\n========================================\n");

            System.out.println(sb);
        }
        first = false;
    }
}
