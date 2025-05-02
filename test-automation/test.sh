#!/bin/bash
# Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
#
# WSO2 LLC. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

MVNSTATE=1 #This variable is read by the test-grid to determine success or failure of the build. (0=Successful)
RUNNER_HOME=`pwd`


echo '======================= SetUp base Products ======================='
#
# Create the test home directory if it doesn't exist
if [ ! -d "$TEST_HOME" ]; then
    mkdir -p $TEST_HOME
fi



wget "https://github.com/wso2/product-is/releases/download/v7.0.0/wso2is-7.0.0.zip" -O $TEST_HOME/wso2is-7.0.0.zip
unzip $TEST_HOME/wso2is-7.0.0.zip -d $TEST_HOME

echo '======================= Installing WSO2 Updates ======================='
name=$(echo "$WSO2_USERNAME" | cut -d'@' -f1)
WSO2_UPDATES_HOME=home/$name/.wso2updates
sudo mkdir -p /home/$name/.wso2-updates/docker && sudo chmod -R 777 /home/$name/.wso2-updates


cp ${RUNNER_HOME}/test-automation/wso2update_linux $TEST_HOME/wso2is-7.0.0/bin/

chmod +x $TEST_HOME/wso2is-7.0.0/bin/wso2update_linux

$TEST_HOME/wso2is-7.0.0/bin/wso2update_linux --username $WSO2_USERNAME --password $WSO2_PASSWORD ||  ($TEST_HOME/wso2is-7.0.0/bin/wso2update_linux --username $WSO2_USERNAME --password $WSO2_PASSWORD )

printf "%s\n%s\n" "$WSO2_USERNAME" "$WSO2_PASSWORD" | $TEST_HOME/wso2is-7.0.0/bin/wso2update_linux


echo '======================= Setup Mail ======================='
sudo apt update && sudo apt install mailutils
sudo yum install mailx



sudo apt install -y mutt
sudo apt install -y ssmtp

sudo touch /etc/msmtprc
echo -e "root=${WSO2_USERNAME}\nmailhub=smtp.gmail.com:587\nAuthUser=${WSO2_USERNAME}\nAuthPass=${STMP_ROOT_PASSWORD}\nUseTLS=YES\nUseSTARTTLS=YES\nFromLineOverride=YES" > /etc/msmtprc


sudo apt install -y msmtp msmtp-mta
sudo touch /etc/msmtprc
sudo chmod 777 /etc/msmtprc
echo -e "account gmail
host smtp.gmail.com
port 587
auth on
user ${WSO2_USERNAME}
password ${WSO2_PASSWORD}
tls on
tls_starttls on
from ${WSO2_USERNAME}

account default : gmail
" | sudo tee -a /etc/msmtprc

cat /etc/msmtprc
cat /etc/ssmtp/ssmtp.conf
#
strip_html_wrappers() {
  sed '/<!DOCTYPE/,/<body[^>]*>/d; /<\/body>/,/<\/html>/d' "$1"
}


# Create temporary HTML body file
EMAIL_BODY=$(mktemp)

cat > "$EMAIL_BODY" <<EOF
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <style type="text/css">
    table {margin-bottom:10px;border-collapse:collapse;empty-cells:show}
    th,td {border:1px solid #009;padding:.25em .5em}
    th {vertical-align:bottom}
    td {vertical-align:top}
    table a {font-weight:bold}
    .stripe td {background-color: #E6EBF9}
    .num {text-align:right}
    .passedodd td {background-color: #3F3}
    .passedeven td {background-color: #0A0}
    .skippedodd td {background-color: #DDD}
    .skippedeven td {background-color: #CCC}
    .failedodd td,.attn {background-color: #F33}
    .failedeven td,.stripe .attn {background-color: #D00}
    .stacktrace {white-space:pre;font-family:monospace}
    .totop {font-size:85%;text-align:center;border-bottom:2px solid #000}
    .invisible {display:none}
  </style>
</head>
<body style="font-family: Arial, sans-serif; font-size: 14px; color: #333;">
  <h2>API PUBLISH</h2>
  <hr>

  <h2>DCR</h2>
  <hr>

  <h2>TOKEN</h2>
  <hr>

  <h2>CONSENT</h2>
  <hr>

  <h2>EVENT NOTIFICATION</h2>
</body>
</html>
EOF


# Convert .log to .txt (just a copy with new extension)
cp "${RUNNER_HOME}/wso2.log" "${RUNNER_HOME}/wso2ServerLogs.txt"


mutt -e "set content_type=text/html" \
  -s "Accelerator 4 M3 Test Reports" \
  -- $TEST_REPORT_RECIPIENT < "$EMAIL_BODY"




