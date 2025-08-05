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

set -e
MVNSTATE=1 #This variable is read by the test-grid to determine success or failure of the build. (0=Successful)
RUNNER_HOME=`pwd`
#=== FUNCTION ==================================================================
# NAME: get_prop
# DESCRIPTION: Retrieve specific property from deployment.properties.sample
# PARAMETER 1: property_value
#===============================================================================
function get_prop {
    local prop=$(grep -w "${1}" "${RUNNER_HOME}/test-automation/deployment.properties" | cut -d'=' -f2)
    echo $prop
}

while getopts i:p:o:h flag
do
    case "${flag}" in
        i) TEST_HOME=${OPTARG};;
        p) PASSWORD=${OPTARG};;
        u) USERNAME=${OPTARG};;
    esac
done


# ====== variables ======
# Username and Password for WSO2 Updates
# TEST_HOME : Folder to install IS server

echo "Username: $USERNAME"
echo "Password: $PASSWORD"
echo "TEST_HOME:  $TEST_HOME"

# handle empty variables

if [ -z "$TEST_HOME" ]; then
    echo "TEST_HOME is empty. Please provide a TEST_HOME using the -o flag."
    exit 1
fi
echo '======================= Run Test Cases ======================='

cd $RUNNER_HOME/fs-integration-test-suite

echo '======================= Configure TestConfigurationExample ======================='
ACCELERATION_INTEGRATION_TESTS_HOME=${RUNNER_HOME}/fs-integration-test-suite
ACCELERATION_INTEGRATION_TESTS_CONFIG=${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-test-framework/src/main/resources/TestConfiguration.xml
TEST_ARTIFACTS="${ACCELERATION_INTEGRATION_TESTS_HOME}/test-artifacts"


cp ${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-test-framework/src/main/resources/SampleTestConfiguration.xml ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#--------------Server Configurations-----------------#
sed -i -e "s|Common.IS_Version|7.1.0|g" $TEST_CONFIG_FILE

##----------------set hostnames for sequences -----------#
sed -i -e "s|{AM_HOST}|$(get_prop "ApimHostname")|g" $TEST_CONFIG_FILE
sed -i -e "s|{IS_HOST}|$(get_prop "IsHostname")|g" $TEST_CONFIG_FILE

##----------------set Directory Path-----------#
sed -i -e "s|{TestSuiteDirectoryPath}|${ACCELERATOR_TESTS_HOME}|g" $TEST_CONFIG_FILE

#--------------Provisioning Configurations-----------------#
sed -i -e "s|Provisioning.Enabled|true|g" $TEST_CONFIG_FILE
sed -i -e "s|Provisioning.ProvisionFilePath|${ACCELERATOR_TESTS_HOME}/accelerator-tests/preconfiguration.steps/src/test/resources/api-config-provisioning.yaml|g" $TEST_CONFIG_FILE

# Set Web Browser Configuration
sed -i -e "s|BrowserAutomation.HeadlessEnabled|$(get_prop "BrowserAutomation.HeadlessEnabled")|g" $TEST_CONFIG_FILE
if [ $(get_prop "OSName") == "mac" ]; then
    sed -i -e "s|BrowserAutomation.WebDriverLocation|${TEST_ARTIFACTS}/selenium-libs/mac/geckodriver|g" $TEST_CONFIG_FILE
else
    sed -i -e "s|BrowserAutomation.WebDriverLocation|${TEST_ARTIFACTS}/selenium-libs/ubuntu/geckodriver|g" $TEST_CONFIG_FILE
fi
echo '======================= Updating PSUList, TPPInfo, KeyManagerAdmin ======================='

#----------------- Update PSUInfo Users and Passwords -----------------#

# Update all PSUInfo <User> tags
sed -i '/<PSUList>/,/<\/PSUList>/ s|<User>.*</User>|<User>testUser@wso2.com</User>|g' ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
# Update all PSUInfo <Password> tags
sed -i '/<PSUList>/,/<\/PSUList>/ s|<Password>.*</Password>|<Password>testUser@wso2123</Password>|g' ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#----------------- Update TPPInfo -----------------#
sed -i '/<TPPInfo>/,/<\/TPPInfo>/ s|<User>.*</User>|<User>testUser@wso2.com</User>|' ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i '/<TPPInfo>/,/<\/TPPInfo>/ s|<Password>.*</Password>|<Password>testUser@wso2123</Password>|' ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#----------------- Update KeyManagerAdmin User -----------------#
sed -i '/<KeyManagerAdmin>/,/<\/KeyManagerAdmin>/ s|<User>.*</User>|<User>is_admin@wso2.com</User>|' ${ACCELERATION_INTEGRATION_TESTS_CONFIG}


#----------------- IS Setup Configurations -----------------#
# Add the new ISSetup XML snippet after the </ConsentApi> tag
sed -i '/<\/ConsentApi>/a \
    <ISSetup> \
        <ISAdminUserName>is_admin@wso2.com</ISAdminUserName> \
        <ISAdminPassword>wso2123</ISAdminPassword> \
    </ISSetup>' ${ACCELERATION_INTEGRATION_TESTS_CONFIG}


#----------------Install geckodriver------------------------#
# check if geckodriver is already installed
if [ -f "$TEST_HOME/geckodriver" ]; then
    echo "geckodriver is already installed."
else
    echo "Installing geckodriver..."
    export DEBIAN_FRONTEND=noninteractive
    wget https://github.com/mozilla/geckodriver/releases/download/v0.29.1/geckodriver-v0.29.1-linux64.tar.gz -O $TEST_HOME/geckodriver.tar.gz
    tar -xvzf "$TEST_HOME/geckodriver.tar.gz" -C "$TEST_HOME"
    chmod +x $TEST_HOME/geckodriver
fi


cat ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
#
echo '======================= Build the Test framework ======================='
mvn clean install  -Dmaven.test.skip=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
MVNSTATE=$((MVNSTATE+$?))


echo '======================= Run the Test Cases ======================='
cd ${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-tests/is-tests
mvn clean install
MVNSTATE=$((MVNSTATE+$?))

#echo '======================= Setup Mail ======================='
#sudo apt update && sudo apt install mailutils
#sudo apt install -y mutt
#sudo apt install -y ssmtp

sudo touch /etc/msmtprc
echo -e "root=${WSO2_USERNAME}\nmailhub=smtp.gmail.com:587\nAuthUser=${WSO2_USERNAME}\nAuthPass=${STMP_ROOT_PASSWORD}\nUseTLS=YES\nUseSTARTTLS=YES\nFromLineOverride=YES" > /etc/msmtprc

API_PUBLISH="${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-tests/is-tests/is-setup/target/surefire-reports/emailable-report.html"
DCR="${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-tests/is-tests/dcr/target/surefire-reports/emailable-report.html"
TOKEN="${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-tests/is-tests/token/target/surefire-reports/emailable-report.html"
CONSENT="${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-tests/is-tests/consent-management/target/surefire-reports/emailable-report.html"
EVENT_NOTIFICATION="${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-tests/is-tests/event-notification/target/surefire-reports/emailable-report.html"

cp "$API_PUBLISH" ${TEST_HOME}/API_Publish_Report.html
cp "$DCR" ${TEST_HOME}/DCR_Report.html
cp "$TOKEN" ${TEST_HOME}/Token_Report.html
cp "$CONSENT" ${TEST_HOME}/Consent_Report.html
cp "$EVENT_NOTIFICATION" ${TEST_HOME}/Event_Notification_Report.html

#

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
  $(strip_html_wrappers "$API_PUBLISH")
  <hr>

  <h2>DCR</h2>
  $(strip_html_wrappers "$DCR")
  <hr>

  <h2>TOKEN</h2>
  $(strip_html_wrappers "$TOKEN")
  <hr>

  <h2>CONSENT</h2>
  $(strip_html_wrappers "$CONSENT")
  <hr>

  <h2>EVENT NOTIFICATION</h2>
  $(strip_html_wrappers "$EVENT_NOTIFICATION")
</body>
</html>
EOF

XT_FILE="${LOG_FILE%.log}.txt"

# Convert .log to .txt
cp "${RUNNER_HOME}/wso2.log" "${RUNNER_HOME}/wso2ServerLogs.txt"

## TODO : mail the results
## Send the email with mutt
#mutt -e "set content_type=text/html" \
#  -s "Accelerator 4 M3 Test Reports" \
#  -a "${TEST_HOME}/API_Publish_Report.html" "${TEST_HOME}/DCR_Report.html" "${TEST_HOME}/Consent_Report.html" "${TEST_HOME}/Token_Report.html" "${TEST_HOME}/Event_Notification_Report.html" "$CONFIG_FILE" "$ACCELERATION_INTEGRATION_TESTS_CONFIG" "${TEST_HOME}/wso2is-7.0.0/repository/logs/wso2carbon.log" "${TEST_HOME}/DCR.txt" "${TEST_HOME}/TokenTest.txt" "${TEST_HOME}/ConsentTest.txt" "${TEST_HOME}/EventNotification.txt" \
#  -- ${$WSO2_USERNAME} < "$EMAIL_BODY"
#mutt -e "set content_type=text/html" \
#  -s "Accelerator 4 M3 Test Reports" \
#  -- sajeenthiran@wso2.com < "$EMAIL_BODY"

echo "======================== Stop Server ======================="
$TEST_HOME/wso2is-7.0.0/bin/wso2server.sh  stop

if [ $MVNSTATE -ne 0 ]; then
  exist 1
else
  exist 0
fi

