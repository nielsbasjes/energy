[![Github actions Build status](https://img.shields.io/github/actions/workflow/status/nielsbasjes/energy/build.yml?branch=main)](https://github.com/nielsbasjes/energy/actions)
[![Coverage Status](https://img.shields.io/codecov/c/github/nielsbasjes/energy)](https://app.codecov.io/gh/nielsbasjes/energy)
[![License: CC BY-NC-ND 4.0](https://img.shields.io/badge/License-CC%20BY--NC--ND%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc-nd/4.0/)
[![Maven Central](https://img.shields.io/maven-central/v/nl.basjes.energy/energy-parent.svg)](https://central.sonatype.com/namespace/nl.basjes.energy)
[![GitHub stars](https://img.shields.io/github/stars/nielsbasjes/energy?label=GitHub%20stars)](https://github.com/nielsbasjes/energy/stargazers)
[![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Donations-via%20Paypal-blue.svg)](https://www.paypal.me/nielsbasjes)
[![Website](https://img.shields.io/badge/https://-energy.basjes.nl-blue.svg)](https://energy.basjes.nl/)

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

Or simply put: **Works on my machine ... will probably melt yours ...**

Further info:  https://energy.basjes.nl

License
=======

![Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License](docs/by-nc-nd.eu.svg)

    Energy readers and parsers toolkit
    Copyright (C) 2019-2023 Niels Basjes

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

