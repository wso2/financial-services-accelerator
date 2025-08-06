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

set -o xtrace

HOME=`pwd`
TEST_SCRIPT=test.sh
MVNSTATE=1 #This variable is read by the test-grid to determine success or failure of the build. (0=Successful)

function usage()
{
    echo "
    Usage bash test.sh --input-dir /workspace/data-bucket.....
    Following are the expected input parameters. all of these are optional
    --input-dir       | -i    : input directory for test.sh
    --output-dir      | -o    : output directory for test.sh
    "
}

#=== FUNCTION ==================================================================
# NAME: get_prop
# DESCRIPTION: Retrieve specific property from deployment.properties file
# PARAMETER 1: property_value
#===============================================================================
function get_prop {
    local prop=$(grep -w "${1}" "${INPUT_DIR}/deployment.properties" | cut -d'=' -f2)
    echo $prop
}

optspec=":hiom-:"
while getopts "$optspec" optchar; do
    case "${optchar}" in
        -)
            case "${OPTARG}" in
                input-dir)
                    val="${!OPTIND}"; OPTIND=$(( $OPTIND + 1 ))
                    INPUT_DIR=$val
                    ;;
                output-dir)
                    val="${!OPTIND}"; OPTIND=$(( $OPTIND + 1 ))
                    OUTPUT_DIR=$val
                    ;;
                mvn-opts)
                    val="${!OPTIND}"; OPTIND=$(( $OPTIND + 1 ))
                    MAVEN_OPTS=$val
                    ;;
                *)
                    usage
                    if [ "$OPTERR" = 1 ] && [ "${optspec:0:1}" != ":" ]; then
                        echo "Unknown option --${OPTARG}" >&2
                    fi
                    ;;
            esac;;
        h)
            usage
            exit 2
            ;;
        o)
            OUTPUT_DIR=$val
            ;;
        m)
            MVN_OPTS=$val
            ;;
        i)
            INPUT_DIR=$val
            ;;
        *)
            usage
            if [ "$OPTERR" != 1 ] || [ "${optspec:0:1}" = ":" ]; then
                echo "Non-option argument: '-${OPTARG}'" >&2
            fi
            ;;
    esac
done

export DATA_BUCKET_LOCATION=${INPUT_DIR}

cat ${INPUT_DIR}/deployment.properties

cd ../../
PROJECT_HOME=`pwd`

echo "--- Go to fs-integration-test-suite folder"
cd fs-integration-test-suite
ACCELERATOR_TESTS_HOME=${PROJECT_HOME}/fs-integration-test-suite
TEST_FRAMEWORK_HOME=${ACCELERATOR_TESTS_HOME}/accelerator-test-framework
TEST_CONFIG_FILE=${TEST_FRAMEWORK_HOME}/src/main/resources/TestConfiguration.xml
TEST_ARTIFACTS=${ACCELERATOR_TESTS_HOME}/test-artifacts
GATEWAY_INTEGRATION_TEST_HOME=${ACCELERATOR_TESTS_HOME}/accelerator-tests/gateway-tests
IS_TEST_HOME=${ACCELERATOR_TESTS_HOME}/accelerator-tests/is-tests

#--------------Set configs in TestConfiguration.xml-----------------#
cp ${TEST_FRAMEWORK_HOME}/src/main/resources/SampleTestConfiguration.xml ${TEST_CONFIG_FILE}

#--------------Server Configurations-----------------#
sed -i -e "s|Common.IS_Version|7.1.0|g" $TEST_CONFIG_FILE

# Get the hostname and convert it to lowercase using tr
IS_HOSTNAME_LC=$(get_prop "IsHostname" | tr '[:upper:]' '[:lower:]')

##----------------set hostnames for sequences -----------#
sed -i -e "s|{AM_HOST}|$(get_prop "ApimHostname")|g" $TEST_CONFIG_FILE
sed -i -e "s|{IS_HOST}|${IS_HOSTNAME_LC}|g" "$TEST_CONFIG_FILE"

##----------------set Directory Path-----------#
sed -i -e "s|{TestSuiteDirectoryPath}|${ACCELERATOR_TESTS_HOME}|g" $TEST_CONFIG_FILE

#--------------Provisioning Configurations-----------------#
sed -i -e "s|Provisioning.Enabled|true|g" $TEST_CONFIG_FILE
sed -i -e "s|Provisioning.ProvisionFilePath|${ACCELERATOR_TESTS_HOME}/accelerator-tests/preconfiguration.steps/src/test/resources/api-config-provisioning.yaml|g" $TEST_CONFIG_FILE

# Set Web Browser Configuration
sed -i -e "s|BrowserAutomation.BrowserPreference|firefox|g" $TEST_CONFIG_FILE
sed -i -e "s|BrowserAutomation.HeadlessEnabled|true|g" $TEST_CONFIG_FILE
if [ $(get_prop "OSName") == "mac" ]; then
    sed -i -e "s|BrowserAutomation.WebDriverLocation|${TEST_ARTIFACTS}/selenium-libs/mac/geckodriver|g" $TEST_CONFIG_FILE
else
    sed -i -e "s|BrowserAutomation.WebDriverLocation|${TEST_ARTIFACTS}/selenium-libs/ubuntu/geckodriver|g" $TEST_CONFIG_FILE
fi

#----------------Install geckodriver------------------------#
export DEBIAN_FRONTEND=noninteractive
if [ $(get_prop "InstallGeckodriver") == true ]; then
    wget https://github.com/mozilla/geckodriver/releases/download/v0.29.1/geckodriver-v0.29.1-linux64.tar.gz
    tar xvzf geckodriver*
    cp geckodriver ${TEST_ARTIFACTS}/selenium-libs/ubuntu/
    chmod +x ${TEST_ARTIFACTS}/selenium-libs/ubuntu/geckodriver
else
        echo "Not Required to install geckodriver"
fi

#--------------Build the test framework and the project-----------------#
cd ${ACCELERATOR_TESTS_HOME}
mvn clean install -Dmaven.test.skip=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

##--------------API Publish and Subscribe Step-----------------#
cd ${ACCELERATOR_TESTS_HOME}/accelerator-tests/preconfiguration.steps
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
MVNSTATE=$?

echo "-----------------Executing IS tests----------------"
cd ${IS_TEST_HOME}/dcr
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/is-tests/dcr
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/is-tests/dcr \;

cd ${IS_TEST_HOME}/token
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/is-tests/token
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/is-tests/token \;

cd ${IS_TEST_HOME}/pre-configuration-step
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/is-tests/pre-configuration-step
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/is-tests/pre-configuration-step \;

cd ${IS_TEST_HOME}/consent-management
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/is-tests/consent-management
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/is-tests/consent-management \;

cd ${IS_TEST_HOME}/event-notification
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/is-tests/event-notification
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/is-tests/event-notification \;

echo "-----------------End of IS tests----------------"

sleep 2h

echo "-----------------Executing Gateway tests----------------"
echo "-----------------Executing Accelerator Scenarios with Dynamic Client Registration----------------"
cd ${GATEWAY_INTEGRATION_TEST_HOME}/dcr
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/gateway-tests/dcr
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/gateway-tests/dcr \;

echo "-----------------Rebuild the Accelerator Framework to fetch configuration changes------------------"
cd ${TEST_FRAMEWORK_HOME}
mvn clean install -Dmaven.test.skip=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

cd ${GATEWAY_INTEGRATION_TEST_HOME}/accounts
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/gateway-tests/accounts
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/gateway-tests/accounts \;

cd ${GATEWAY_INTEGRATION_TEST_HOME}/cof
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/gateway-tests/cof
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/gateway-tests/cof \;

cd ${GATEWAY_INTEGRATION_TEST_HOME}/payments
mvn clean install -DdcrEnabled=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/gateway-tests/payments
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/gateway-tests/payments \;

cd ${GATEWAY_INTEGRATION_TEST_HOME}/schema.validation
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/gateway-tests/schema.validation
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/gateway-tests/schema.validation \;

cd ${GATEWAY_INTEGRATION_TEST_HOME}/token
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae -B -f pom.xml
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/gateway-tests/token
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/gateway-tests/token \;

echo "-----------------End of Accelerator Scenarios with Dynamic Client Registration----------------"
echo "-----------------End of Gateway tests----------------"

find . -name "aggregate-surefire-report" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios \;
