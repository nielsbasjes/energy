#!/usr/bin/env bash
#
# Energy readers and parsers toolkit
# Copyright (C) 2019-2023 Niels Basjes
#
# This work is licensed under the Creative Commons
# Attribution-NonCommercial-NoDerivatives 4.0 International License.
#
# You may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    https://creativecommons.org/licenses/by-nc-nd/4.0/
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an AS IS BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
TARGETDIR=target/generated-sources/java/nl/basjes/energy/sunspec/
mkdir -p ${TARGETDIR}
java -jar ../generate-sunspec-parser/target/sunspec-parser-generator-*.jar > ${TARGETDIR}/ParseSunSpec.java
