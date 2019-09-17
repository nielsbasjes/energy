#!/bin/bash

#
# Energy readers and parsers toolkit
# Copyright (C) 2019-2019 Niels Basjes
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

# -------------------------------------------------------
function showWelcome {

# http://patorjk.com/software/taag/#p=display&f=Slant&t=SunSpec
cat << "Welcome-message"
     _____            _____
    / ___/__  ______ / ___/____  ___  _____
    \__ \/ / / / __ \\__ \/ __ \/ _ \/ ___/
   ___/ / /_/ / / / /__/ / /_/ /  __/ /__
  /____/\__,_/_/ /_/____/ .___/\___/\___/
                       /_/
  SunSpec reading toolkit

This is the standard Sunspec build environment.
In here all tools needed to run a build are present.

Welcome-message
}

# -------------------------------------------------------

function showAbort {
# http://patorjk.com/software/taag/#p=display&f=Doom&t=Aborting...
  cat << "Abort-message"

    ___  _                _   _
   / _ \| |              | | (_)
  / /_\ \ |__   ___  _ __| |_ _ _ __   __ _
  |  _  | '_ \ / _ \| '__| __| | '_ \ / _` |
  | | | | |_) | (_) | |  | |_| | | | | (_| |_ _ _
  \_| |_/_.__/ \___/|_|   \__|_|_| |_|\__, (_|_|_)
                                       __/ |
                                      |___/

Abort-message
}

# -------------------------------------------------------

function failIfUserIsRoot {
    if [ "$(id -u)" -eq "0" ]; # If you are root then something went wrong.
    then
        cat <<End-of-message

Apparently you are inside this docker container as the user root.
Putting it simply:

   This should not occur.

Known possible causes of this are:
1) Running this script as the root user ( Just don't )
2) Running an old docker version ( upgrade to 1.4.1 or higher )

End-of-message

    showAbort

    logout

    fi
}

# -------------------------------------------------------

# Configurable low water mark in GiB
MINIMAL_MEMORY_GiB=2

function warnIfLowMemory {
    MINIMAL_MEMORY=$((MINIMAL_MEMORY_GiB*1024*1024)) # Convert to KiB
    INSTALLED_MEMORY=$(fgrep MemTotal /proc/meminfo | awk '{print $2}')
    if [ $((INSTALLED_MEMORY)) -le $((MINIMAL_MEMORY)) ];
    then
        cat << "End-of-message"
   _                    ___  ___
  | |                   |  \/  |
  | |     _____      __ | .  . | ___ _ __ ___   ___  _ __ _   _
  | |    / _ \ \ /\ / / | |\/| |/ _ \ '_ ` _ \ / _ \| '__| | | |
  | |___| (_) \ V  V /  | |  | |  __/ | | | | | (_) | |  | |_| |
  \_____/\___/ \_/\_/   \_|  |_/\___|_| |_| |_|\___/|_|   \__, |
                                                           __/ |
                                                          |___/
End-of-message
cat << "End-of-message"
Your system is running on very little memory.
This means it may work but it wil most likely be slower than needed.

If you are running this via boot2docker you can simply increase
the available memory to atleast ${MINIMAL_MEMORY_GiB} GiB (you have $((INSTALLED_MEMORY/(1024*1024))) GiB )
End-of-message
    fi
}

# -------------------------------------------------------

showWelcome
warnIfLowMemory
failIfUserIsRoot

# -------------------------------------------------------
