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
package nl.basjes.energy.sdm630;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import nl.basjes.energy.sdm630.SDM630Reader.SDM630Values;
import nl.basjes.energy.sdm630.SDM630Reader.SDM630Values.Value;
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
import org.apache.nifi.components.AllowableValue;
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

import static com.ghgande.j2mod.modbus.Modbus.SERIAL_ENCODING_ASCII;
import static com.ghgande.j2mod.modbus.Modbus.SERIAL_ENCODING_RTU;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.EVEN_PARITY;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.FLOW_CONTROL_CTS_ENABLED;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.FLOW_CONTROL_DISABLED;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.FLOW_CONTROL_DSR_ENABLED;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.FLOW_CONTROL_DTR_ENABLED;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.FLOW_CONTROL_RTS_ENABLED;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.FLOW_CONTROL_XONXOFF_IN_ENABLED;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.FLOW_CONTROL_XONXOFF_OUT_ENABLED;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.MARK_PARITY;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.NO_PARITY;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.ODD_PARITY;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.ONE_POINT_FIVE_STOP_BITS;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.ONE_STOP_BIT;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.SPACE_PARITY;
import static com.ghgande.j2mod.modbus.net.AbstractSerialConnection.TWO_STOP_BITS;
import static org.apache.nifi.annotation.behavior.InputRequirement.Requirement.INPUT_FORBIDDEN;

@Tags({"input", "ingest", "fetch", "energy", "sdm630"})
@CapabilityDescription("Fetch data from an Eastron SDM 630 via a RS845 serial connection.")
@SeeAlso({})
@TriggerSerially
@PrimaryNodeOnly // This may ONLY be read single threaded
@InputRequirement(INPUT_FORBIDDEN) // ONLY read data from the configured device
//@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
//@WritesAttributes({@WritesAttribute(attribute = "", description = "")})
public class FetchSDM630 extends AbstractProcessor {

    public static final PropertyDescriptor PORT_NAME = new PropertyDescriptor
        .Builder().name("PORTNAME")
        .displayName("portName")
        .description("The name of the serial device on which the SDM630 modbus interface has been connected.")
        .required(true)
        .defaultValue("/dev/ttyUSB0")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor BAUD_RATE = new PropertyDescriptor
        .Builder().name("BAUD_RATE")
        .displayName("Baud rate")
        .description("The connection speed")
        .required(true)
        .defaultValue("9600")
        .allowableValues("2400", "4800", "9600", "19200", "38400" )
        .build();


    public static final PropertyDescriptor FLOW_CONTROL_IN = new PropertyDescriptor
        .Builder().name("FLOW_CONTROL_IN")
        .displayName("Flow Control In")
        .description("Flow Control In")
        .required(true)
        .defaultValue(""+FLOW_CONTROL_DISABLED)
        .allowableValues(
            new AllowableValue(""+FLOW_CONTROL_DISABLED              , "Disabled"),
            new AllowableValue(""+FLOW_CONTROL_RTS_ENABLED           , "RTS Enabled"),
            new AllowableValue(""+FLOW_CONTROL_CTS_ENABLED           , "CTS Enabled"),
            new AllowableValue(""+FLOW_CONTROL_DSR_ENABLED           , "DSR Enabled"),
            new AllowableValue(""+FLOW_CONTROL_DTR_ENABLED           , "DTR Enabled"),
            new AllowableValue(""+FLOW_CONTROL_XONXOFF_IN_ENABLED    , "XonXoff IN Enabled"),
            new AllowableValue(""+FLOW_CONTROL_XONXOFF_OUT_ENABLED   , "XonXoff OUT Enabled"))
        .build();

    public static final PropertyDescriptor FLOW_CONTROL_OUT = new PropertyDescriptor
        .Builder().name("FLOW_CONTROL_OUT")
        .displayName("Flow Control Out")
        .description("Flow Control Out")
        .required(true)
        .defaultValue(""+FLOW_CONTROL_DISABLED)
        .allowableValues(
            new AllowableValue(""+FLOW_CONTROL_DISABLED              , "Disabled"),
            new AllowableValue(""+FLOW_CONTROL_RTS_ENABLED           , "RTS Enabled"),
            new AllowableValue(""+FLOW_CONTROL_CTS_ENABLED           , "CTS Enabled"),
            new AllowableValue(""+FLOW_CONTROL_DSR_ENABLED           , "DSR Enabled"),
            new AllowableValue(""+FLOW_CONTROL_DTR_ENABLED           , "DTR Enabled"),
            new AllowableValue(""+FLOW_CONTROL_XONXOFF_IN_ENABLED    , "XonXoff IN Enabled"),
            new AllowableValue(""+FLOW_CONTROL_XONXOFF_OUT_ENABLED   , "XonXoff OUT Enabled"))
        .build();

    public static final PropertyDescriptor DATA_BITS       = new PropertyDescriptor
        .Builder().name("DATA_BITS")
        .displayName("Data bits")
        .description("Data bits")
        .required(true)
        .defaultValue("8")
        .allowableValues("5", "6", "7", "8", "9") // https://en.wikipedia.org/wiki/Serial_port#Data_bits
        .build();

    public static final PropertyDescriptor STOP_BITS = new PropertyDescriptor
        .Builder().name("STOP_BITS")
        .displayName("Stop bits")
        .required(true)
        .defaultValue(""+ONE_STOP_BIT)
        .allowableValues(
            new AllowableValue(""+ONE_STOP_BIT             , "1 Stop bit"),
            new AllowableValue(""+ONE_POINT_FIVE_STOP_BITS , "1.5 Stop bits"),
            new AllowableValue(""+TWO_STOP_BITS            , "2 Stop bits")
            )
        .build();

    public static final PropertyDescriptor PARITY = new PropertyDescriptor
        .Builder().name("PARITY")
        .displayName("Parity")
        .required(true)
        .defaultValue("")
        .defaultValue(""+NO_PARITY)
        .allowableValues(
            new AllowableValue(""+NO_PARITY    , "No Parity"),
            new AllowableValue(""+ODD_PARITY   , "Odd Parity"),
            new AllowableValue(""+EVEN_PARITY  , "Even Parity"),
            new AllowableValue(""+MARK_PARITY  , "Mark Parity"),
            new AllowableValue(""+SPACE_PARITY , "Space Parity")
        )
        .build();

    public static final PropertyDescriptor OPEN_DELAY = new PropertyDescriptor
        .Builder().name("OPEN_DELAY")
        .displayName("Open delay")
        .required(true)
        .defaultValue("0")
        .addValidator(StandardValidators.NON_NEGATIVE_INTEGER_VALIDATOR)
        .build();


    public static final PropertyDescriptor ENCODING = new PropertyDescriptor
        .Builder().name("ENCODING")
        .displayName("Encoding")
        .required(true)
        .defaultValue(""+SERIAL_ENCODING_RTU)
        .allowableValues(
            new AllowableValue(""+SERIAL_ENCODING_ASCII, "ASCII Encoding"),
            new AllowableValue(""+SERIAL_ENCODING_RTU,   "RTU Encoding")
        )
        .build();

    public static final PropertyDescriptor UNIT_ID = new PropertyDescriptor
        .Builder().name("UNIT_ID")
        .displayName("Modbus unit id")
        .description("The modbus unit id that needs to be fetched.")
//        .displayName("Modbus unit id list")
//        .description("A comma separated list of all modbus unit ids that need to be fetched.")
        .required(true)
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
//        .addValidator(createListValidator(true, true, StandardValidators.POSITIVE_INTEGER_VALIDATOR))
        .defaultValue("1")
        .build();


    public static final PropertyDescriptor FETCH_INTERVAL = new PropertyDescriptor
        .Builder().name("FETCH_INTERVAL")
        .displayName("Fetch interval (ms)")
        .description("Every how many milliseconds should we retrieve the data from the Sunspec device.")
        .required(true)
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .allowableValues("1000", "5000", "10000", "60000")
        .defaultValue("1000")
        .build();

    public static final Relationship SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Here we route all FlowFiles that have been analyzed.")
        .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();

        descriptors.add(PORT_NAME);
        descriptors.add(BAUD_RATE);
        descriptors.add(FLOW_CONTROL_IN);
        descriptors.add(FLOW_CONTROL_OUT);
        descriptors.add(DATA_BITS);
        descriptors.add(STOP_BITS);
        descriptors.add(PARITY);
        descriptors.add(OPEN_DELAY);
        descriptors.add(ENCODING);
        descriptors.add(UNIT_ID);

        descriptors.add(FETCH_INTERVAL);

        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<>();
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

    private SDM630Reader fetcher = null;
    private long fetchInterval = 1000;
    private int unitId;

    @OnScheduled
    public void onScheduled(final ProcessContext context) throws ModbusException {
        if (fetcher == null) {
            String  portName        = context.getProperty(PORT_NAME).getValue();
            Integer baudRate        = context.getProperty(BAUD_RATE).asInteger();
            Integer flowControlIn   = context.getProperty(FLOW_CONTROL_IN).asInteger();
            Integer flowControlOut  = context.getProperty(FLOW_CONTROL_OUT).asInteger();
            Integer dataBits        = context.getProperty(DATA_BITS).asInteger();
            Integer stopBits        = context.getProperty(STOP_BITS).asInteger();
            Integer parity          = context.getProperty(PARITY).asInteger();
            Integer openDelay       = context.getProperty(OPEN_DELAY).asInteger();
            String  encoding        = context.getProperty(ENCODING).getValue();
            unitId                  = context.getProperty(UNIT_ID).asInteger();
//            List<Integer> unitIds   = context.getProperty(UNIT_ID).getValue().split(",");

            fetchInterval = context.getProperty(FETCH_INTERVAL).asLong();

            SerialParameters serialParameters = new SerialParameters();
            serialParameters.setPortName(portName);
            serialParameters.setBaudRate(baudRate);
            serialParameters.setFlowControlIn(flowControlIn);
            serialParameters.setFlowControlOut(flowControlOut);
            serialParameters.setDatabits(dataBits);
            serialParameters.setStopbits(stopBits);
            serialParameters.setParity(parity);
            serialParameters.setOpenDelay(openDelay);
            serialParameters.setEncoding(encoding);

            fetcher = new SDM630Reader(new ModbusSerialMaster(serialParameters), unitId);
//            fetcher = new SDM630Reader(new ModbusTCPMaster(InetAddress.getLoopbackAddress().getHostAddress(), 44444), unitId);
        }
    }

    @OnRemoved
    @OnStopped
    @OnUnscheduled
    public void OnUnscheduled(ProcessContext context) {
        if (fetcher != null) {
            fetcher.disconnect();
            fetcher = null;
        }
    }

    public static final String ATTRIBUTE_PREFIX = "SDM630|";

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
        if (fetcher == null) {
            System.err.println("Fatal error in onTrigger: The fetcher is null...");
            return;
        }

        try {
            fetcher.connect();
        } catch (Exception e) {
            throw new ProcessException("Unable to connect", e);
        }

        final SDM630Values values;
        long timestamp;
        try {
            values = fetcher.read(fetchInterval);

            if (fetcher == null) {
                System.err.println("Aborting...");
                return;
            }

            timestamp = values.getTimestamp();
        } catch (ModbusException e) {
            return; // FIXME: Handle this Oops.  For now ignore
        }

        Map<String, String> results = new LinkedHashMap<>();
        FlowFile flowFile = session.create();

        session.putAttribute(flowFile, "filename", "SDM630_" + unitId);

        String timeKey = cleanKey(ATTRIBUTE_PREFIX + "TimeStamp");
        results.put(timeKey, String.valueOf(timestamp));

        final Map<String, Value> rawResults = values.toMap();
        rawResults.forEach((k, v) -> results.put(cleanKey(ATTRIBUTE_PREFIX + k + (v.unit.isEmpty()?"":"_"+v.unit)), v.value.toString()));

        flowFile = session.putAllAttributes(flowFile, results);

        session.getProvenanceReporter().modifyAttributes(flowFile);

        session.transfer(flowFile, SUCCESS);


        // FIXME: Experimental: Output the InfluxDB line protocol on the first record.
        if (first) {
            StringBuilder sb = new StringBuilder();

            sb.append("\n========================================\n");

            sb.append("electricity,equipmentId=SDM630 ");

            boolean firstEntry = true;
            for (Map.Entry<String, Value> entry : rawResults.entrySet()) {
                if (!firstEntry) {
                    sb.append(',');
                }
                firstEntry=false;

                Value value = entry.getValue();
                String k = cleanKey(ATTRIBUTE_PREFIX + value.name + (value.unit.isEmpty()?"":"_"+value.unit));

                sb.append(k).append("=${").append(k).append("}");
            }

            sb.append(" ${").append(timeKey).append("}000000");

            sb.append("\n========================================\n");

            System.out.println(sb);
        }
        first = false;
    }
}
