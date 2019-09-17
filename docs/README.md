![Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License](by-nc-nd.eu.svg)

Energy systems reading toolkit
=======================
This is a Java library to make reading data from devices that have a Modbus interface easier.

For the actual modbus connection this library relies on https://github.com/steveohara/j2mod

This library does the mapping from the binary modbus registers to meaningful variables.

Currently two mappings have been written

- Solar inverters conforming to the SunSpec specification 
- The Eastron SDM630 Modbus powermeter


Status
====
Under development, unfinished, unstable, not yet released.

Or simply put: Works on my machine ... will probably melt yours ...

Based on the official specifications
===
Sunspec
--
Sunspec is really big, it contains over 90 Models each consisting of a lot of attirbutes. 
In addition some attributes are actually enums, bitmaps or variables that must be scaled based on the value in another variable.

To be as complete and as close to the specification as possible this project actually generates 
the parsing code straight from the official Sunspec specification XMLs hosted at 
https://github.com/sunspec/models .

To avoid build problems these specifications have been included in this repo using 'git subtree'.

Eastron SDM630 Modbus 
--
The Eastron SDM630 Modbus is based on these specifications: http://www.eastroneurope.com/media/_system/tech_specs/3924/SDM630%20Modbus-Protocol.pdf


SDM630 Usage


InfluxDB Line protocol

    electricity,equipmentId=SDM630 SDM630_Phase_1_line_to_neutral_volts_Volts=${SDM630_Phase_1_line_to_neutral_volts_Volts},SDM630_Phase_2_line_to_neutral_volts_Volts=${SDM630_Phase_2_line_to_neutral_volts_Volts},SDM630_Phase_3_line_to_neutral_volts_Volts=${SDM630_Phase_3_line_to_neutral_volts_Volts},SDM630_Phase_1_current_Amps=${SDM630_Phase_1_current_Amps},SDM630_Phase_2_current_Amps=${SDM630_Phase_2_current_Amps},SDM630_Phase_3_current_Amps=${SDM630_Phase_3_current_Amps},SDM630_Phase_1_power_Watts=${SDM630_Phase_1_power_Watts},SDM630_Phase_2_power_Watts=${SDM630_Phase_2_power_Watts},SDM630_Phase_3_power_Watts=${SDM630_Phase_3_power_Watts},SDM630_Phase_1_volt_amps_VA=${SDM630_Phase_1_volt_amps_VA},SDM630_Phase_2_volt_amps_VA=${SDM630_Phase_2_volt_amps_VA},SDM630_Phase_3_volt_amps_VA=${SDM630_Phase_3_volt_amps_VA},SDM630_Phase_1_volt_amps_reactive_VAr=${SDM630_Phase_1_volt_amps_reactive_VAr},SDM630_Phase_2_volt_amps_reactive_VAr=${SDM630_Phase_2_volt_amps_reactive_VAr},SDM630_Phase_3_volt_amps_reactive_VAr=${SDM630_Phase_3_volt_amps_reactive_VAr},SDM630_Phase_1_power_factor__1_=${SDM630_Phase_1_power_factor__1_},SDM630_Phase_2_power_factor__1_=${SDM630_Phase_2_power_factor__1_},SDM630_Phase_3_power_factor__1_=${SDM630_Phase_3_power_factor__1_},SDM630_Phase_1_phase_angle_Degrees=${SDM630_Phase_1_phase_angle_Degrees},SDM630_Phase_2_phase_angle_Degrees=${SDM630_Phase_2_phase_angle_Degrees},SDM630_Phase_3_phase_angle_Degrees=${SDM630_Phase_3_phase_angle_Degrees},SDM630_Average_line_to_neutral_volts_Volts=${SDM630_Average_line_to_neutral_volts_Volts},SDM630_Average_line_current_Amps=${SDM630_Average_line_current_Amps},SDM630_Sum_of_line_currents_Amps=${SDM630_Sum_of_line_currents_Amps},SDM630_Total_system_power_Watts=${SDM630_Total_system_power_Watts},SDM630_Total_system_volt_amps_VA=${SDM630_Total_system_volt_amps_VA},SDM630_Total_system_VAr_VAr=${SDM630_Total_system_VAr_VAr},SDM630_Total_system_power_factor__1_=${SDM630_Total_system_power_factor__1_},SDM630_Total_system_phase_angle_Degrees=${SDM630_Total_system_phase_angle_Degrees},SDM630_Frequency_of_supply_voltages_Hz=${SDM630_Frequency_of_supply_voltages_Hz},SDM630_Import_Wh_since_last_reset_2__kWh_MWh=${SDM630_Import_Wh_since_last_reset_2__kWh_MWh},SDM630_Export_Wh_since_last_reset_2__kWH_MWh=${SDM630_Export_Wh_since_last_reset_2__kWH_MWh},SDM630_Import_VArh_since_last_reset_2__kVArh_MVArh=${SDM630_Import_VArh_since_last_reset_2__kVArh_MVArh},SDM630_Export_VArh_since_last_reset_2__kVArh_MVArh=${SDM630_Export_VArh_since_last_reset_2__kVArh_MVArh},SDM630_VAh_since_last_reset__2__kVAh_MVAh=${SDM630_VAh_since_last_reset__2__kVAh_MVAh},SDM630_Ah_since_last_reset__3__Ah_kAh=${SDM630_Ah_since_last_reset__3__Ah_kAh},SDM630_Total_system_power_demand__4__W=${SDM630_Total_system_power_demand__4__W},SDM630_Maximum_total_system_power_demand_4__VA=${SDM630_Maximum_total_system_power_demand_4__VA},SDM630_Total_system_VA_demand_VA=${SDM630_Total_system_VA_demand_VA},SDM630_Maximum_total_VA_system_demand_VA=${SDM630_Maximum_total_VA_system_demand_VA},SDM630_Neutral_current_demand_Amps=${SDM630_Neutral_current_demand_Amps},SDM630_Maximum_neutral_current_demand_Amps=${SDM630_Maximum_neutral_current_demand_Amps},SDM630_Line_1_to_Line_2_volts_Volts=${SDM630_Line_1_to_Line_2_volts_Volts},SDM630_Line_2_to_Line_3_volts_Volts=${SDM630_Line_2_to_Line_3_volts_Volts},SDM630_Line_3_to_Line_1_volts_Volts=${SDM630_Line_3_to_Line_1_volts_Volts},SDM630_Average_line_to_line_volts_Volts=${SDM630_Average_line_to_line_volts_Volts},SDM630_Neutral_current_Amps=${SDM630_Neutral_current_Amps},SDM630_Phase_1_L_N_volts_THD_Pct=${SDM630_Phase_1_L_N_volts_THD_Pct},SDM630_Phase_2_L_N_volts_THD_Pct=${SDM630_Phase_2_L_N_volts_THD_Pct},SDM630_Phase_3_L_N_volts_THD_Pct=${SDM630_Phase_3_L_N_volts_THD_Pct},SDM630_Phase_1_Current_THD_Pct=${SDM630_Phase_1_Current_THD_Pct},SDM630_Phase_2_Current_THD_Pct=${SDM630_Phase_2_Current_THD_Pct},SDM630_Phase_3_Current_THD_Pct=${SDM630_Phase_3_Current_THD_Pct},SDM630_Average_line_to_neutral_volts_THD_Pct=${SDM630_Average_line_to_neutral_volts_THD_Pct},SDM630_Average_line_current_THD_Pct=${SDM630_Average_line_current_THD_Pct},SDM630_Total_system_power_factor__5__Degrees=${SDM630_Total_system_power_factor__5__Degrees},SDM630_Phase_1_current_demand_Amps=${SDM630_Phase_1_current_demand_Amps},SDM630_Phase_2_current_demand_Amps=${SDM630_Phase_2_current_demand_Amps},SDM630_Phase_3_current_demand_Amps=${SDM630_Phase_3_current_demand_Amps},SDM630_Maximum_phase_1_current_demand_Amps=${SDM630_Maximum_phase_1_current_demand_Amps},SDM630_Maximum_phase_2_current_demand_Amps=${SDM630_Maximum_phase_2_current_demand_Amps},SDM630_Maximum_phase_3_current_demand_Amps=${SDM630_Maximum_phase_3_current_demand_Amps},SDM630_Line_1_to_line_2_volts_THD_Pct=${SDM630_Line_1_to_line_2_volts_THD_Pct},SDM630_Line_2_to_line_3_volts_THD_Pct=${SDM630_Line_2_to_line_3_volts_THD_Pct},SDM630_Line_3_to_line_1_volts_THD_Pct=${SDM630_Line_3_to_line_1_volts_THD_Pct},SDM630_Average_line_to_line_volts_THD_Pct=${SDM630_Average_line_to_line_volts_THD_Pct},SDM630_Total_kWh_kWh=${SDM630_Total_kWh_kWh},SDM630_Total_kVArh_kVArh=${SDM630_Total_kVArh_kVArh},SDM630_L1_import_kWh_kWh=${SDM630_L1_import_kWh_kWh},SDM630_L2_import_kWh_kWh=${SDM630_L2_import_kWh_kWh},SDM630_L3_import_kWh_kWh=${SDM630_L3_import_kWh_kWh},SDM630_L1_export_kWh_kWh=${SDM630_L1_export_kWh_kWh},SDM630_L2_export_kWh_kWh=${SDM630_L2_export_kWh_kWh},SDM630_L3_export_kWh_kWh=${SDM630_L3_export_kWh_kWh},SDM630_L1_total_kWh_kWh=${SDM630_L1_total_kWh_kWh},SDM630_L2_total_kWh_kWh=${SDM630_L2_total_kWh_kWh},SDM630_L3_total_kWh_kWh=${SDM630_L3_total_kWh_kWh},SDM630_L1_import_kVArh_kVArh=${SDM630_L1_import_kVArh_kVArh},SDM630_L2_import_kVArh_kVArh=${SDM630_L2_import_kVArh_kVArh},SDM630_L3_import_kVArh_kVArh=${SDM630_L3_import_kVArh_kVArh},SDM630_L1_export_kVArh_kVArh=${SDM630_L1_export_kVArh_kVArh},SDM630_L2_export_kVArh_kVArh=${SDM630_L2_export_kVArh_kVArh},SDM630_L3_export_kVArh_kVArh=${SDM630_L3_export_kVArh_kVArh},SDM630_L1_total_kVArh_kVArh=${SDM630_L1_total_kVArh_kVArh},SDM630_L2_total_kVArh_kVArh=${SDM630_L2_total_kVArh_kVArh},SDM630_L3_total_kVArh_kVArh=${SDM630_L3_total_kVArh_kVArh} ${SDM630_TimeStamp}000000

Sunspec Usage
===
The Sunspec specification is build around a chain of models, each model consists of one or more blocks.

This library retrieves the data per model.

Using it in your project

    <dependency>
      <groupId>nl.basjes.sunspec</groupId>
      <artifactId>sunspec</artifactId>
      <version>0.1-SNAPSHOT</version>
    </dependency>
  
Make the connection
    
    try(SunSpecModbusDataReader dataReader = new SunSpecModbusDataReader(new ModbusTCPMaster(hostname))) {

Determine the available models

    dataReader.getModelLocations();

Create a SunSpecFetcher to retrieve the models you need.

    SunSpecFetcher fetcher = new SunSpecFetcher(dataReader)
        .useModel(1)
        .useModel(101)
        .useModel(132); // Which has a repeating block

Actually get the data

    fetcher.refresh();

Or like this to wait for the next 'second'

    fetcher.refresh(1000);

Then get the data as a HashMap

    final Map<String, Object> result = fetcher.toHashMap();

Or directly get the values you are looking for

    fetcher.model_1.getSerialNumber()

Or a field from a repeating block like this (note the first one has index 0):

    fetcher.model_132.getRepeatingActPt(1);

The returned values
===
The values have been interpreted and cleaned.

So for example the scalefactors are hidden and have been applied to the appropriate values (returning a Double).

All bitmasks and enums have been generated into specific Java enums. 
A bitmask is returned as an EnumSet of those enum values.

UDFs
===
In addition processors for Apache Nifi are included.

Thanks
===
For the Modbus connection this project relies on https://github.com/steveohara/j2mod



@@@@@
FIXME: Note about Minifi 0.5.0, Raspbian Java 11 --> Java 8
@@@@@

License
=======

    Energy readers and parsers toolkit
    Copyright (C) 2019-2019 Niels Basjes

    This work is licensed under the Creative Commons 
    Attribution-NonCommercial-NoDerivatives 4.0 International License. 

    You may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       https://creativecommons.org/licenses/by-nc-nd/4.0/

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an AS IS BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

