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


#=== FUNCTION ==================================================================
# NAME: get_prop
# DESCRIPTION: Retrieve specific property from deployment.properties
# PARAMETER 1: property_value

#===============================================================================
function get_prop {
    local prop=$(grep -w "${1}" "${RUNNER_HOME}/test-automation/deployment.properties" | cut -d'=' -f2)
    echo $prop
}

TEST_HOME = "$RUNNER_HOME/test-automation"
#while getopts u:p:o:h flag
#do
#    case "${flag}" in
#        u) $WSO2_USERNAME=${OPTARG};;
#        p) PASSWORD=${OPTARG};;
#        o) TEST_HOME=${OPTARG};;
#    esac
#done


# ====== variables ======
# Username and Password for WSO2 Updates
# TEST_HOME : Folder to install IS server

echo "Password SHA256: $(echo -n "$WSO2_PASSWORD" | sha256sum)"

echo "Username: $WSO2_USERNAME"
echo "Password: $WSO2_PASSWORD"
echo "TEST_HOME:  $TEST_HOME"

# handle empty variables
#if [ -z "$WSO2_USERNAME" ]; then
#    echo "Username is empty. Please provide a username using the -u flag."
#    exit 1
#fi
#if [ -z "$WSO2_PASSWORD" ]; then
#    echo "Password is empty. Please provide a password using the -p flag."
#    exit 1
#fi
#if [ -z "$TEST_HOME" ]; then
#    echo "TEST_HOME is empty. Please provide a TEST_HOME using the -o flag."
#    exit 1
#fi


echo '=================== setup Firefox ==================='

if command -v firefox &> /dev/null
then
    echo "Firefox is installed"
else
    sudo install -d -m 0755 /etc/apt/keyrings
    wget -q https://packages.mozilla.org/apt/repo-signing-key.gpg -O- | sudo tee /etc/apt/keyrings/packages.mozilla.org.asc > /dev/null
    gpg -n -q --import --import-options import-show /etc/apt/keyrings/packages.mozilla.org.asc | awk '/pub/{getline; gsub(/^ +| +$/,""); if($0 == "35BAA0B33E9EB396F59CA838C0BA5CE6DC6315A3") print "\nThe key fingerprint matches ("$0").\n"; else print "\nVerification failed: the fingerprint ("$0") does not match the expected one.\n"}'
    echo "deb [signed-by=/etc/apt/keyrings/packages.mozilla.org.asc] https://packages.mozilla.org/apt mozilla main" | sudo tee -a /etc/apt/sources.list.d/mozilla.list > /dev/null
    echo '
        Package: *
        Pin: origin packages.mozilla.org
        Pin-Priority: 1000
          ' | sudo tee /etc/apt/preferences.d/mozilla
    sudo apt-get update && sudo apt-get install firefox
    firefox -version
fi



echo '=================== Install Java and Maven ==================='

if command -v java &> /dev/null
then
    echo "Java is installed"
    java -version
else
   wget https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.16+8/OpenJDK11U-jdk_x64_linux_hotspot_11.0.16_8.tar.gz
   tar -xvzf OpenJDK11U-jdk_x64_linux_hotspot_11.0.16_8.tar.gz
   sudo mv jdk-11.0.16+8 /opt/java
   echo "export JAVA_HOME=/opt/java/jdk-11.0.16+8" >> ~/.bashrc
   echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
   source ~/.bashrc
   java -version

fi

if command -v mvn &> /dev/null
then
    echo "Maven is installed"
    mvn --version
else
    sudo apt install -y maven
fi



echo '======================= Building packs ======================='

mvn -B install --file ${RUNNER_HOME}/pom.xml
MVNSTATE=$?

echo '======================= SetUp base Products ======================='

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
$TEST_HOME/wso2is-7.0.0/bin/wso2update_linux --username $WSO2_USERNAME --password $WSO2_PASSWORD ||  ($TEST_HOME/wso2is-7.0.0/bin/wso2update_linux --username $WSO2_USERNAME --password $WSO2_PASSWORD )
#
echo '======================= Moving Packs to RUNNER_HOME ======================='
unzip financial-services-accelerator/accelerators/fs-is/target/wso2-fsiam-accelerator-4.0.0-M3.zip -d $TEST_HOME/wso2is-7.0.0/
#wget https://github.com/ParameswaranSajeenthiran/files/raw/refs/heads/master/wso2-fsiam-accelerator-4.0.0-M3.zip -O wso2-fsiam-accelerator-4.0.0-M3.zip
#unzip wso2-fsiam-accelerator-4.0.0-M3.zip -d $TEST_HOME/wso2is-7.0.0/

echo '======================= Setup MYSQL ======================='
sudo apt-get update
sudo apt-get install -y mysql-server
sudo systemctl start mysql
mysql --version

echo '======================= Download and install Drivers ======================='
wget -q https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.2.0/mysql-connector-j-9.2.0.jar
mv mysql-connector-j-9.2.0.jar $TEST_HOME/wso2is-7.0.0/repository/components/lib

echo '======================= Generate and Export Certificates ======================='
storepass=wso2carbon
declare -A servers
servers["wso2"]="$TEST_HOME/wso2is-7.0.0/repository/resources/security/wso2carbon.jks"

cert_dir="$TEST_HOME/certs"
mkdir -p $cert_dir

for alias in "${!servers[@]}"; do
  keystore="${servers[$alias]}"

  echo "removing old key pair if exists"
  # Remove old key pair if exists
  keytool -delete -alias wso2carbon -keystore $keystore -storepass wso2carbon

  echo "generating new key pair"

  # Generate new key pair
  keytool -genkey -alias wso2carbon -keystore $keystore -keysize 2048 -keyalg RSA -validity 9999 -dname   "CN=obiam, O=OB, L=WSO2, S=COL, C=LK, OU=OB" -ext san=ip:127.0.0.1,dns:localhost,dns:$alias -keypass  wso2carbon -storepass wso2carbon

  echo "exporting public certificate"
  # Export public certificate
  keytool -export -alias wso2carbon -keystore $keystore -file $cert_dir/$alias.pem -storepass wso2carbon
done

echo '======================= Import Certificates into the truststore ======================='

aliases=("wso2")

# Define the client truststore
truststores=(
  "$TEST_HOME/wso2is-7.0.0/repository/resources/security/client-truststore.jks"
)
# Import certificates into truststores
for alias in "${aliases[@]}"; do
  cert="$cert_dir/$alias.pem"
  for truststore in "${truststores[@]}"; do
    echo "Importing certificate for alias '$alias' into truststore: $truststore"
    keytool -import -alias $alias -file $cert_dir/$alias.pem -keystore $truststore -storepass wso2carbon -keypass  wso2carbon -noprompt
  done
done

echo '======================= Verify Exchanged Certificates ======================='

# Function to check if alias exists in the truststore
check_alias() {
  local truststore=$1
  local alias=$2
  echo "Checking alias '$alias' in truststore: $truststore"

  keytool -list -keystore $truststore -storepass "$storepass" -alias $alias
  if [ $? -eq 0 ]; then
    echo "[✔] Alias '$alias' found in truststore: $truststore"
  else
    echo "[✘] Alias '$alias' NOT found in truststore: $truststore"
    exit 1  # Fail the workflow if a certificate is missing
  fi
}

# Function to display certificate details
show_certificate_details() {
  local truststore=$1
  local alias=$2

  echo "-------------------------------"
  echo "Details for alias '$alias' in truststore: $truststore"
  keytool -list -v -keystore "$truststore" -storepass "$storepass" -alias "$alias" | grep -E "Alias|Valid from|Issuer|Subject"
  echo "-------------------------------"
}

# Verify imported certificates
for truststore in "${truststores[@]}"; do
  echo "Checking truststore: $truststore"



  for alias in "${aliases[@]}"; do
    check_alias "$truststore" "$alias"
    show_certificate_details "$truststore" "$alias"
  done
done

echo '======================= Import OB sandbox Root and Issuer Certificates ======================='

wget 'https://github.com/ParameswaranSajeenthiran/files/raw/refs/heads/master/OB_SandBox_PP_Root%20CA.cer' -O "${TEST_HOME}/OB_SandBox_PP_Root CA.cer"
keytool -import -alias root -file "${TEST_HOME}/OB_SandBox_PP_Root CA.cer" -keystore "${TEST_HOME}/wso2is-7.0.0/repository/resources/security/client-truststore.jks" -storepass wso2carbon -noprompt


wget 'https://github.com/ParameswaranSajeenthiran/files/raw/refs/heads/master/OB_SandBox_PP_Issuing%20CA.cer' -O "${TEST_HOME}/OB_SandBox_PP_Issuing CA.cer"
keytool -import -alias issuer -file "${TEST_HOME}/OB_SandBox_PP_Issuing CA.cer" -keystore "${TEST_HOME}/wso2is-7.0.0/repository/resources/security/client-truststore.jks" -storepass wso2carbon -noprompt



echo '======================= Run merge and Config scripts ======================='
cd $TEST_HOME/wso2is-7.0.0/wso2-fsiam-accelerator-4.0.0-M3/bin
bash merge.sh
bash configure.sh
SQL_SCRIPT="$RUNNER_HOME/financial-services-accelerator/accelerators/fs-is/carbon-home/dbscripts/financial-services/event-notifications/mysql.sql"

source $(pwd)/../repository/conf/configure.properties

mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "fs_consentdb" < "$SQL_SCRIPT"


echo '======================= Update deployment.toml ======================='

CONFIG_FILE="$TEST_HOME/wso2is-7.0.0/repository/conf/deployment.toml"

sed -i -e  's|username = ""|username = "is_admin@wso2.com"|g' ${CONFIG_FILE}
sed -i -e 's|password = ""|password = "wso2123"|g' ${CONFIG_FILE}
sed -i -e 's|base_url = ""|base_url = "http://localhost:9446/api/financialservices/uk/consent/endpoints"|g' ${CONFIG_FILE}
sed -i -e 's|allowed_extensions = \["post_token_generation"\]|extension_types = \["pre-consent-generation", "post-consent-generation", "pre-consent-retrieval", "pre-consent-revocation", "pre-consent-authorization", "consent-validation", "pre-user-authorization", "post-user-authorization", "pre-id-token-generation"\]|g' ${CONFIG_FILE}

sed -i '/name = "SSAJTIValidator"/,/priority = 8/ {
    s/enable = true/enable = false/
}' "$TEST_HOME/wso2is-7.0.0/repository/conf/deployment.toml"

echo "deployment.toml has been updated in place and a backup is saved as deployment.toml.bak."

rm $BACKUP_TOML
cd $TEST_HOME/wso2is-7.0.0/bin

./wso2server.sh  start

#nohup ./wso2server.sh > ${RUNNER_HOME}/wso2.log 2>&1 &
sleep 120

#cat ${RUNNER_HOME}/wso2.log
#./wso2server.sh
###
echo '======================= Test Setup ======================='

curl -X GET "https://localhost:9446/api/server/v1/applications?limit=30&offset=0" \
-H "accept: application/json" \
-H "Authorization: Basic aXNfYWRtaW5Ad3NvMi5jb206d3NvMjEyMw==" \
-k

echo '======================= Run Test Cases ======================='

cd $RUNNER_HOME/fs-integration-test-suite

echo '======================= Configure TestConfigurationExample ======================='
ACCELERATION_INTEGRATION_TESTS_HOME=${RUNNER_HOME}/fs-integration-test-suite
ACCELERATION_INTEGRATION_TESTS_CONFIG=${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-test-framework/src/main/resources/TestConfiguration.xml
TEST_ARTIFACTS="${ACCELERATION_INTEGRATION_TESTS_HOME}/test-artifacts"


cp ${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-test-framework/src/main/resources/TestConfigurationExample.xml ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

sed -i -e "s|Server.BaseUrl|$(get_prop "BaseUrl")|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
echo $ACCELERATION_INTEGRATION_TESTS_CONFIG
sed -i -e "s|Server.ISServerUrl|$(get_prop "ISServerUrl")|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|Server.APIMServerUrl|$(get_prop "APIMServerUrl")|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#--------------IS Setup Configurations-----------------#
sed -i -e "s|ISSetup.ISAdminUserName|is_admin@wso2.com|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|ISSetup.ISAdminPassword|wso2123|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

##--------------Application Configurations-----------------#
#sed -i -e "s|{TestArtifactDirectoryPath}|${TEST_ARTIFACTS}|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
##sed -i -e "s|AppConfig.Application.ClientID|Application.ClientID|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
#sed -i -e "s|{ISDirectoryPath}|${TEST_HOME}/wso2is-7.0.0|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}


#----------------- Common -----------------#
sed -i -e "s|Common.SolutionVersion|1.0.0|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|Common.AccessTokenExpireTime|200|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|Common.TenantDomain|carbon.super|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|Common.SigningAlgorithm|PS256|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|Common.TestArtifactLocation|${TEST_ARTIFACTS}|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#----------------- Provisioning -----------------#
sed -i -e "s|Provisioning.Enabled|false|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|Provisioning.ProvisionFilePath|${TEST_ARTIFACTS}/provisioningFiles/api-config-provisioning.yaml|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#----------------- AppConfig1 -----------------#
sed -i -e "s|AppConfig1.KeyStore.Location|${TEST_ARTIFACTS}/DynamicClientRegistration/uk/tpp1/signing-keystore/signing.jks|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.KeyStore.Alias|signing|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.KeyStore.Password|wso2carbon|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.KeyStore.SigningKid|cIYo-5zX4OTWZpHrmmiZDVxACJM|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.Transport.MTLSEnabled|true|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.Transport.KeyStore.Location|${TEST_ARTIFACTS}/DynamicClientRegistration/uk/tpp1/transport-keystore/transport.jks|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.Transport.KeyStore.Password|wso2carbon|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.Transport.KeyStore.Alias|transport|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.DCR.SSAPath|${TEST_ARTIFACTS}/DynamicClientRegistration/uk/tpp1/ssa.txt|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.DCR.SelfSignedSSAPath|${TEST_ARTIFACTS}/DynamicClientRegistration/uk/tpp1/self_ssa.txt|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.DCR.SoftwareId|oQ4KoaavpOuoE7rvQsZEOV|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.DCR.RedirectUri|https://www.google.com/redirects/redirect1|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.DCR.AlternateRedirectUri|https://www.google.com/redirects/redirect2|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.DCR.DCRAPIVersion|0.1|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.Application.ClientID|bS_mDjiQ5RdMg7t9upCWCeN7mhoa|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.Application.ClientSecret|fVcOLPd9gEnrkLfKs9qijFkjtxlS_4YNMMXfwJJqUeca|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig1.Application.RedirectURL|https://www.google.com/redirects/redirect1|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#----------------- AppConfig2 (copy same values for now) -----------------#
sed -i -e "s|AppConfig2.KeyStore.Location|${TEST_ARTIFACTS}/DynamicClientRegistration/uk/tpp2/signing-keystore/signing.jks|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.KeyStore.Alias|signing|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.KeyStore.Password|wso2carbon|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.KeyStore.SigningKid|BkHxeIHKyMKF6SgGwqYzLUvTQfk|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.Transport.MTLSEnabled|true|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.Transport.KeyStore.Location|${TEST_ARTIFACTS}/DynamicClientRegistration/uk/tpp2/transport-keystore/transport.jks|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.Transport.KeyStore.Password|wso2carbon|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.Transport.KeyStore.Alias|transport|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.DCR.SSAPath|${TEST_ARTIFACTS}/DynamicClientRegistration/uk/tpp2/ssa.txt|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.DCR.SoftwareId|9ZzFFBxSLGEjPZogRAbvFd|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.DCR.RedirectUri|https://www.google.com/redirects/redirect1|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.DCR.AlternateRedirectUri|https://www.google.com/redirects/redirect2|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.DCR.DCRAPIVersion|0.1|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.Application.ClientID|kuT7f9R1YDWm37_PaqlaRjFALv8a|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.Application.ClientSecret|inAOnTuyQwOdz3AbATl_L_qURTHtuI9bJQL7D0DOfxwa|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|AppConfig2.Application.RedirectURL|https://www.google.com/redirects/redirect1|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#----------------- Transport Truststore -----------------#
sed -i -e "s|Transport.Truststore.Location|${TEST_HOME}/wso2is-7.0.0/repository/resources/security/client-truststore.jks|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|Transport.Truststore.Password|wso2carbon|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#----------------- Non-Regulatory Application -----------------#
sed -i -e "s|NonRegulatoryApplication.ClientID|kuT7f9R1YDWm37_PaqlaRjFALv8a|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|NonRegulatoryApplication.ClientSecret|inAOnTuyQwOdz3AbATl_L_qURTHtuI9bJQL7D0DOfxwa|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|NonRegulatoryApplication.RedirectURL|https://www.google.com/redirects/redirect1|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#----------------- Browser Automation -----------------#
sed -i -e "s|BrowserAutomation.BrowserPreference|firefox|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|BrowserAutomation.HeadlessEnabled|true|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|BrowserAutomation.WebDriverLocation|${TEST_ARTIFACTS}/selenium-libs/ubuntu/geckodriver|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#----------------- Consent API -----------------#
sed -i -e "s|ConsentApi.AudienceValue|https://localhost:9446/oauth2/token|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

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
export DEBIAN_FRONTEND=noninteractive
wget https://github.com/mozilla/geckodriver/releases/download/v0.29.1/geckodriver-v0.29.1-linux64.tar.gz
tar xvzf geckodriver*
rm ${TEST_ARTIFACTS}/selenium-libs/ubuntu/geckodriver
cp geckodriver ${TEST_ARTIFACTS}/selenium-libs/ubuntu/
chmod +x ${TEST_ARTIFACTS}/selenium-libs/ubuntu/geckodriver


echo '======================= Setup Mail ======================='
sudo apt update && sudo apt install mailutils
sudo yum install mailx

cat ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
#
echo '======================= Build the Test framework ======================='
mvn clean install  -Dmaven.test.skip=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
MVNSTATE=$((MVNSTATE+$?))

echo '======================= API Publish and Subscribe Step ======================='
cd ${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-tests/is-tests/is-setup
mvn clean test -X
MVNSTATE=$((MVNSTATE+$?))

cd ${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-test-framework

mvn clean install
MVNSTATE=$((MVNSTATE+$?))


echo '======================= DCR ======================='
cd ${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-tests/is-tests/dcr

mvn clean test -X > ${TEST_HOME}/DCR.txt 2>&1
MVNSTATE=$((MVNSTATE+$?))

echo '======================= Token ======================='
cd ${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-tests/is-tests/token
mvn clean test -X > ${TEST_HOME}/TokenTest.txt 2>&1
MVNSTATE=$((MVNSTATE+$?))


echo '======================= Consent Management ======================='
cd ${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-tests/is-tests/consent-management
mvn clean test -X > ${TEST_HOME}/ConsentTest.txt 2>&1
MVNSTATE=$((MVNSTATE+$?))


echo '======================= Event Notification ======================='
cd ${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-tests/is-tests/event-notification
mvn clean test -X > ${TEST_HOME}/EventNotification.txt  2>&1
MVNSTATE=$((MVNSTATE+$?))


sudo apt install -y mutt
sudo apt install -y ssmtp

sudo touch /etc/msmtprc
echo -e "root=psajeendran@gmail.com\nmailhub=smtp.gmail.com:587\nAuthUser=psajeendran@gmail.com\nAuthPass=${STMP_ROOT_PASSWORD}\nUseTLS=YES\nUseSTARTTLS=YES\nFromLineOverride=YES" > /etc/msmtprc

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


# simle test

sudo apt install -y msmtp msmtp-mta
sudo touch /etc/msmtprc
sudo chmod 777 /etc/msmtprc
echo -e "account gmail
host smtp.gmail.com
port 587
auth on
user psajeendran@gmail.com
password ${STMP_ROOT_PASSWORD}
tls on
tls_starttls on
from psajeendran@gmail.com

account default : gmail
" | sudo tee -a /etc/msmtprc

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

# Convert .log to .txt (just a copy with new extension)
cp "${RUNNER_HOME}/wso2.log" "${RUNNER_HOME}/wso2ServerLogs.txt"

# Send the email with mutt
mutt -e "set content_type=text/html" \
  -s "Accelerator 4 M3 Test Reports" \
  -a "${TEST_HOME}/API_Publish_Report.html" "${TEST_HOME}/DCR_Report.html" "${TEST_HOME}/Consent_Report.html" "${TEST_HOME}/Token_Report.html" "${TEST_HOME}/Event_Notification_Report.html" "$CONFIG_FILE" "$ACCELERATION_INTEGRATION_TESTS_CONFIG" "${TEST_HOME}/wso2is-7.0.0/repository/logs/wso2carbon.log" "${TEST_HOME}/DCR.txt" "${TEST_HOME}/TokenTest.txt" "${TEST_HOME}/ConsentTest.txt" "${TEST_HOME}/EventNotification.txt" \
  -- ${$WSO2_USERNAME} < "$EMAIL_BODY"


$TEST_HOME/wso2is-7.0.0/bin/wso2server.sh  stop

if [ $MVNSTATE -ne 0 ]; then
  exist 1
else
  exist 0
fi


