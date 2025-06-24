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
APIS_HOME=${PROJECT_HOME}/financial-services-accelerator/accelerators/fs-apim/repository/resources/apis
ACCELERATOR_TESTS_HOME=${PROJECT_HOME}/fs-integration-test-suite
TEST_FRAMEWORK_HOME=${ACCELERATOR_TESTS_HOME}/accelerator-test-framework
TEST_CONFIG_FILE=${TEST_FRAMEWORK_HOME}/src/main/resources/TestConfiguration.xml
TEST_ARTIFACTS=${ACCELERATOR_TESTS_HOME}/test-artifacts
GATEWAY_INTEGRATION_TEST_HOME=${ACCELERATOR_TESTS_HOME}/accelerator-tests/gateway-tests
IS_TEST_HOME=${ACCELERATOR_TESTS_HOME}/accelerator-tests/is-tests

#--------------Set configs in TestConfiguration.xml-----------------#
cp ${TEST_FRAMEWORK_HOME}/src/main/resources/TestConfigurationExample.xml ${TEST_CONFIG_FILE}

#--------------Server Configurations-----------------#
sed -i -e "s|Common.IS_Version|7.1.0|g" $TEST_CONFIG_FILE

sed -i -e "s|Server.ISServerUrl|https://$(get_prop "IsHostname"):9446|g" $TEST_CONFIG_FILE
sed -i -e "s|Server.BaseUrl|https://$(get_prop "ApimHostname"):8243|g" $TEST_CONFIG_FILE
sed -i -e "s|Server.APIMServerUrl|https://$(get_prop "ApimHostname"):9443|g" $TEST_CONFIG_FILE

#--------------Application Configurations-----------------#
sed -i -e "s|AppConfig.Application.KeyStore.Location|${TEST_ARTIFACTS}/dynamic-client-registration/uk/sample-client-resources/signing-keystore/signing.jks|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig.Application.KeyStore.Alias|signing|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig.Application.KeyStore.Password|wso2carbon|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig.Application.KeyStore.SigningKid|5dBuoFxTMIrwWd9hzUMVgF2jMbk|g" $TEST_CONFIG_FILE

sed -i -e "s|AppConfig.Transport.MTLSEnabled|true|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig.Transport.KeyStore.Location|${TEST_ARTIFACTS}/dynamic-client-registration/uk/sample-client-resources/transport-keystore/transport.jks|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig.Transport.KeyStore.Password|wso2carbon|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig.Transport.KeyStore.Alias|transport|g" $TEST_CONFIG_FILE

sed -i -e "s|AppConfig.DCR.SoftwareId|jFQuQ4eQbNCMSqdCog21nF|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig.DCR.RedirectUri|https://www.google.com/redirects/redirect1|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig.DCR.AlternateRedirectUri|https://www.google.com/redirects/redirect2|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig.DCR.DCRAPIVersion|0.1|g" $TEST_CONFIG_FILE

sed -i -e "s|AppConfig.Application.ClientID|Application.ClientID|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig.Application.RedirectURL|https://www.google.com/redirects/redirect1|g" $TEST_CONFIG_FILE

#--------------Application Configurations App 2-----------------#
sed -i -e "s|AppConfig2.Application.KeyStore.Location|${TEST_ARTIFACTS}/dynamic-client-registration/uk/sample-client-resources-2/signing-keystore/signing.jks|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Application.KeyStore.Alias|signing|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Application.KeyStore.Password|wso2carbon|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Application.KeyStore.SigningKid|sCekNgSWIauQ34klRhDGqfwpjc4|g" $TEST_CONFIG_FILE

sed -i -e "s|AppConfig2.Transport.MTLSEnabled|true|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Transport.KeyStore.Location|${TEST_ARTIFACTS}/dynamic-client-registration/uk/sample-client-resources-2/transport-keystore/transport.jks|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Transport.KeyStore.Password|wso2carbon|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Transport.KeyStore.Alias|transport|g" $TEST_CONFIG_FILE

sed -i -e "s|AppConfig2.DCR.SoftwareId|oQ4KoaavpOuoE7rvQsZEOV|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.DCR.RedirectUri|https://www.google.com/redirects/redirect1|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.DCR.AlternateRedirectUri|https://www.google.com/redirects/redirect2|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.DCR.DCRAPIVersion|0.1|g" $TEST_CONFIG_FILE

sed -i -e "s|AppConfig2.Application.ClientID|Application.ClientID|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig2.Application.RedirectURL|https://www.google.com/redirects/redirect1|g" $TEST_CONFIG_FILE

sed -i -e "s|Transport.Truststore.Location|${TEST_ARTIFACTS}/client-truststore/client-truststore.jks|g" $TEST_CONFIG_FILE
sed -i -e "s|Transport.Truststore.Password|wso2carbon|g" $TEST_CONFIG_FILE

sed -i -e "s|NonRegulatoryApplication.RedirectURL|https://www.google.com/redirects/redirect1|g" $TEST_CONFIG_FILE

sed -i -e "s|BrowserAutomation.BrowserPreference|firefox|g" $TEST_CONFIG_FILE
sed -i -e "s|BrowserAutomation.HeadlessEnabled|true|g" $TEST_CONFIG_FILE
sed -i -e "s|BrowserAutomation.WebDriverLocation|${TEST_ARTIFACTS}/selenium-libs/geckodriver|g" $TEST_CONFIG_FILE

sed -i -e "s|Common.SigningAlgorithm|PS256|g" $TEST_CONFIG_FILE
sed -i -e "s|Common.TestArtifactLocation|${TEST_ARTIFACTS}|g" $TEST_CONFIG_FILE

sed -i -e "s|ConsentApi.AudienceValue|https://$(get_prop "IsHostname"):9446/oauth2/token|g" $TEST_CONFIG_FILE

#--------------Gateway Integration Specific Test Configurations-----------------#

#--------------Server Configurations-----------------#
sed -i -e "s|Server.BaseUrl|https://$(get_prop "ApimHostname"):8243|g" $TEST_CONFIG_FILE
sed -i -e "s|Server.APIMServerUrl|https://$(get_prop "ApimHostname"):9443|g" $TEST_CONFIG_FILE

#--------------Provisioning Configurations-----------------#
sed -i -e "s|Provisioning.Enabled|true|g" $TEST_CONFIG_FILE
sed -i -e "s|Provisioning.ProvisionFilePath|${ACCELERATOR_TESTS_HOME}/ob-gateway-integration-tests/common.integration.test/src/test/resources/provisioningFiles/api-config-provisioning.yaml|g" $TEST_CONFIG_FILE

#--------------DCR Configurations App 1-----------------#
sed -i -e "s|AppConfig.DCR.SSAPath|${TEST_ARTIFACTS}/dynamic-client-registration/uk/sample-client-resources/ssa.txt|g" $TEST_CONFIG_FILE
sed -i -e "s|AppConfig.DCR.SelfSignedSSAPath|${TEST_ARTIFACTS}/dynamic-client-registration/uk/sample-client-resources/self_ssa.txt|g" $TEST_CONFIG_FILE

#--------------DCR Configurations App 2-----------------#
sed -i -e "s|AppConfig2.DCR.SSAPath|${TEST_ARTIFACTS}/dynamic-client-registration/uk/sample-client-resources-2/ssa.txt|g" $TEST_CONFIG_FILE

#----------------Set hostnames for sequences -----------#
#__replace hostname before deploy
sed -i -e "s|localhost:9446|$(get_prop "IsHostname"):9446|g" ${APIS_HOME}/Accounts/accounts-dynamic-endpoint-insequence.xml


#----------------Install geckodriver------------------------#
export DEBIAN_FRONTEND=noninteractive
wget https://github.com/mozilla/geckodriver/releases/download/v0.29.1/geckodriver-v0.29.1-linux64.tar.gz
tar xvzf geckodriver*
cp geckodriver ${TEST_ARTIFACTS}/selenium-libs/
chmod +x ${TEST_ARTIFACTS}/selenium-libs/geckodriver

#--------------Build the test framework-----------------#
mvn clean install -Dprofile=open-banking-test-suite -Dmaven.test.skip=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

#--------------API Publish and Subscribe Step-----------------#
cd ${GATEWAY_INTEGRATION_TEST_HOME}/common.integration.test
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
MVNSTATE=$?

#=== FUNCTION ==================================================================
# NAME: get_test_prop
# DESCRIPTION: Retrieve specific property from test.properties file
# PARAMETER 1: property_value
#===============================================================================
function get_test_prop {
    local prop=$(grep -w "${1}" "target/test.properties" | cut -d'=' -f2)
    echo $prop
}

CLIENT_ID=$(get_test_prop ClientID)
CLIENT_SECRET=$(get_test_prop ClientSecret)

if [ "$(get_test_prop "ClientID")" == "" ]; then
    mkdir -p ${OUTPUT_DIR}/scenarios/com.wso2.openbanking.common.integration.test
    find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/com.wso2.openbanking.common.integration.test \;
    return
fi

sed -i -e "s|NonRegulatoryApplication.ClientID|${CLIENT_ID}|g" $IS_TEST_CONFIG_FILE $GATEWAY_TEST_CONFIG_FILE
sed -i -e "s|NonRegulatoryApplication.ClientSecret|${CLIENT_SECRET}|g" $IS_TEST_CONFIG_FILE $GATEWAY_TEST_CONFIG_FILE

#--------------run IS tests-----------------#
cd ${IS_TEST_HOME}
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/com.wso2.openbanking.is.accelerator.test
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/com.wso2.openbanking.is.accelerator.test \;

sleep 30m

#--------------run Gateway tests-----------------#
cd ${GATEWAY_INTEGRATION_TEST_HOME}
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -fae
MVNSTATE=$((MVNSTATE+$?))
mkdir -p ${OUTPUT_DIR}/scenarios/com.wso2.openbanking.apim.accelerator.test
find . -name "surefire-reports" -exec cp --parents -r {} ${OUTPUT_DIR}/scenarios/com.wso2.openbanking.apim.accelerator.test \;

