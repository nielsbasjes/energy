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

# Native Linux (direct or via sudo)
USER_NAME=$1
USER_ID=$2
GROUP_ID=$3


groupadd --non-unique -g "${GROUP_ID}" "${USER_NAME}"
useradd -g "${GROUP_ID}" -u "${USER_ID}" -k /root -m "${USER_NAME}"

chown "${USER_NAME}" "/home/${USER_NAME}"

{
echo "export HOME=/home/${USER_NAME}"
echo "export USER=${USER_NAME}"
echo '. /scripts/env.sh'
} >> "/home/${USER_NAME}/.bashrc"

VBOXSF_GROUP_LINE=$4
if [[ -n "${VBOXSF_GROUP_LINE}" ]];
then
    echo "${VBOXSF_GROUP_LINE}" >> /etc/group
    usermod -aG vboxsf "${USER_NAME}"
fi

echo "${USER_NAME}    ALL=(ALL) NOPASSWD: ALL" >> "/etc/sudoers.d/${USER_NAME}"
