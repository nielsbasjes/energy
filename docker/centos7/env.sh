#!/bin/bash
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

. "/scripts/build_env_checks.sh"
. "/scripts/bashcolors.sh"

export JDK_VERSION="???"

function __INTERNAL__SwitchJDK {
    JDK=$1
    echo -e ${IRed}${On_Black}'Setting JDK to version '${JDK}${Color_Off}
    sudo alternatives --set java java-${JDK}-openjdk.x86_64;
    sudo alternatives --set javac java-${JDK}-openjdk.x86_64;
    export JDK_VERSION="JDK ${JDK}"
}
echo "Use switch-jdk8 or switch-jdk11 to select the desired JDK"
alias switch-jdk8="__INTERNAL__SwitchJDK 1.8.0; export JDK_VERSION=JDK-8"
alias switch-jdk11="__INTERNAL__SwitchJDK 11; export JDK_VERSION=JDK-11"

switch-jdk8

. "/usr/share/git-core/contrib/completion/git-prompt.sh"
export PS1='\['${IBlue}${On_Black}'\] \u@\['${IWhite}${On_Red}'\][Sunspec Builder \['${BWhite}${On_IBlue}'\]<'\${JDK_VERSION}'>\['${IWhite}${On_Red}'\]]\['${IBlue}${On_Black}'\]:\['${Cyan}${On_Black}'\]\w$(declare -F __git_ps1 &>/dev/null && __git_ps1 " \['${BIPurple}'\]{\['${BIGreen}'\]%s\['${BIPurple}'\]}")\['${BIBlue}'\] ]\['${Color_Off}'\]\n$ '

alias documentation-build="gitbook build ~/sunspec/src ~/sunspec/docs"
alias documentation-serve="gitbook serve ~/sunspec/src ~/sunspec/docs"
