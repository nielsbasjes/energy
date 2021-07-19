Energy systems reading toolkit
=======================
[![Travis Build status](https://api.travis-ci.com/nielsbasjes/energy.png?branch=master)](https://travis-ci.com/nielsbasjes/energy)
[![Coverage Status](https://coveralls.io/repos/github/nielsbasjes/energy/badge.svg?branch=master)](https://coveralls.io/github/nielsbasjes/energy?branch=master)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=nielsbasjes_energy&metric=alert_status)](https://sonarcloud.io/dashboard?id=nielsbasjes_energy)
[![Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License](https://img.shields.io/badge/license-CC--BY--NC--ND-yellow)](https://creativecommons.org/licenses/by-nc-nd/4.0/)

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

Further info:  https://energy.basjes.nl

License
=======

![Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License](docs/by-nc-nd.eu.svg)

    Energy readers and parsers toolkit
    Copyright (C) 2019-2021 Niels Basjes

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

