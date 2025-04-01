#!/bin/bash
MVNSTATE=1 #This variable is read by the test-grid to determine success or failure of the build. (0=Successful)
RUNNER_HOME=`pwd`

echo '#=================== Install Java and Maven ==================='

wget https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.16+8/OpenJDK11U-jdk_x64_linux_hotspot_11.0.16_8.tar.gz
tar -xvzf OpenJDK11U-jdk_x64_linux_hotspot_11.0.16_8.tar.gz
sudo mv jdk-11.0.16+8 /opt/java
echo "export JAVA_HOME=/opt/java/jdk-11.0.16+8" >> ~/.bashrc
echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
source ~/.bashrc
java -version

sudo apt install -y maven



#=== FUNCTION ==================================================================
# NAME: get_prop
# DESCRIPTION: Retrieve specific property from deployment.properties
# PARAMETER 1: property_value
#===============================================================================
function get_prop {
    local prop=$(grep -w "${1}" "${RUNNER_HOME}/deployment.properties" | cut -d'=' -f2)
    echo $prop
}

while getopts u:p:o:h flag
do
    case "${flag}" in
        u) USERNAME=${OPTARG};;
        p) PASSWORD=${OPTARG};;
        o) TEST_HOME=${OPTARG};;
        h) INPUT_DIR=${OPTARG};;
    esac
done

echo "Username: $USERNAME"
echo "Password: $PASSWORD"
echo "TEST_HOME:  $TEST_HOME"

INPUT_DIR=$TEST_HOME
echo "INPUT_DIR: $INPUT_DIR"
##
echo '##################### Building packs #####################'

mvn -B install --file pom.xml
#
echo '##################### SetUp base Products #####################'
wget "https://filebin.net/ezmc7r5vlk4al2t9/wso2is-7.0.0.zip" -O $TEST_HOME/wso2is-7.0.0.zip
unzip $TEST_HOME/wso2is-7.0.0.zip -d $TEST_HOME

echo '##################### Installing WSO2 Updates #####################'
name=$(echo "$USERNAME" | cut -d'@' -f1)
WSO2_UPDATES_HOME=home/$name/.wso2updates
sudo mkdir -p /home/$name/.wso2-updates/docker && sudo chmod -R 777 /home/$name/.wso2-updates

$TEST_HOME/wso2is-7.0.0/bin/wso2update_linux --username $USERNAME --password $PASSWORD ||  ($TEST_HOME/wso2is-7.0.0/bin/wso2update_linux --username $USERNAME --password $PASSWORD )
#
echo '##################### Moving Packs to RUNNER_HOME #####################'
unzip financial-services-accelerator/accelerators/fs-is/target/wso2-fsiam-accelerator-4.0.0-M3.zip -d $TEST_HOME/wso2is-7.0.0/
#wget https://github.com/ParameswaranSajeenthiran/files/raw/master/wso2-fsiam-accelerator-4.0.0-M3.zip -O wso2-fsiam-accelerator-4.0.0-M3.zip
#unzip wso2-fsiam-accelerator-4.0.0-M3.zip -d $TEST_HOME/wso2is-7.0.0/

echo '##################### Setup MYSQL #####################'
sudo apt-get update
sudo apt-get install -y mysql-server
sudo systemctl start mysql
mysql --version

echo '##################### Download and install Drivers #####################'
wget -q https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.2.0/mysql-connector-j-9.2.0.jar
mv mysql-connector-j-9.2.0.jar $TEST_HOME/wso2is-7.0.0/repository/components/lib

echo '##################### Generate and Export Certificates #####################'
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

echo '##################### Import Certificates into the truststore #####################'

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

echo '##################### Verify Exchanged Certificates #####################'

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

echo '##################### Import OB sandbox Root and Issuer Certificates #####################'

wget 'https://github.com/ParameswaranSajeenthiran/files/raw/refs/heads/master/OB_SandBox_PP_Root%20CA.cer' -O "${TEST_HOME}/OB_SandBox_PP_Root CA.cer"
keytool -import -alias root -file "${TEST_HOME}/OB_SandBox_PP_Root CA.cer" -keystore "${TEST_HOME}/wso2is-7.0.0/repository/resources/security/client-truststore.jks" -storepass wso2carbon -noprompt


wget 'https://github.com/ParameswaranSajeenthiran/files/raw/refs/heads/master/OB_SandBox_PP_Issuing%20CA.cer' -O "${TEST_HOME}/OB_SandBox_PP_Issuing CA.cer"
keytool -import -alias issuer -file "${TEST_HOME}/OB_SandBox_PP_Issuing CA.cer" -keystore "${TEST_HOME}/wso2is-7.0.0/repository/resources/security/client-truststore.jks" -storepass wso2carbon -noprompt

echo '##################### Run merge and Config scripts #####################'

cd $TEST_HOME/wso2is-7.0.0/wso2-fsiam-accelerator-4.0.0-M3/bin
bash merge.sh
bash configure.sh

echo '##################### Update deployment.toml #####################'

sed -i '/\[oauth\.oidc\]/,/^\s*$/d' $TEST_HOME/wso2is-7.0.0/repository/conf/deployment.toml
sed -i '/\[financial_services\.service\.extensions\.endpoint]/,/^\s*$/d' $TEST_HOME/wso2is-7.0.0/repository/conf/deployment.toml
sed -i '/\[financial_services\.service\.extensions\.endpoint\.security]/,/^\s*$/d' $TEST_HOME/wso2is-7.0.0/repository/conf/deployment.toml

cat <<EOL >> $TEST_HOME/wso2is-7.0.0/repository/conf/deployment.toml
[financial_services.service.extensions.endpoint]
enabled = true
base_url = "http://<hostname of external service>:<port of the external service>/api/financialservices/uk/consent/endpoints"
extension_types = ["pre-consent-generation", "post-consent-generation", "pre-consent-retrieval", "pre-consent-revocation", "pre-consent-authorization", "consent-validation", "pre-user-authorization", "post-user-authorization", "pre-id-token-generation"]

[financial_services.service.extensions.endpoint.security]
type = "Basic-Auth"
username = "is_admin@wso2.com"
password = "wso2123"

[oauth.oidc]
id_token.signature_algorithm="PS256"
enable_claims_separation_for_access_tokens = false
EOL

cat $TEST_HOME/wso2is-7.0.0/repository/conf/deployment.toml

# shellcheck disable=SC2164
cd $TEST_HOME/wso2is-7.0.0/bin
nohup ./wso2server.sh > ${RUNNER_HOME}/wso2.log 2>&1 &
#./wso2server.sh
sleep 120

echo '##################### Test Setup #####################'

curl -X GET "https://localhost:9446/api/server/v1/applications?limit=30&offset=0" \
-H "accept: application/json" \
-H "Authorization: Basic aXNfYWRtaW5Ad3NvMi5jb206d3NvMjEyMw==" \
-k

echo '##################### Run Test Cases #####################'

cd $RUNNER_HOME/fs-integration-test-suite

echo '##################### Configure TestConfigurationExample #####################'
ACCELERATION_INTEGRATION_TESTS_HOME=${RUNNER_HOME}/fs-integration-test-suite
ACCELERATION_INTEGRATION_TESTS_CONFIG=${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-test-framework/src/main/resources/TestConfiguration.xml
TEST_ARTIFACTS=${ACCELERATION_INTEGRATION_TESTS_HOME}/test-artifacts


cp ${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-test-framework/src/main/resources/TestConfigurationExample.xml ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#--------------Server Configurations-----------------#
sed -i -e "s|Server.BaseUrl|$(get_prop "BaseUrl")|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
echo $ACCELERATION_INTEGRATION_TESTS_CONFIG
sed -i -e "s|Server.ISServerUrl|$(get_prop "ISServerUrl")|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|Server.APIMServerUrl|$(get_prop "APIMServerUrl")|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#--------------IS Setup Configurations-----------------#
sed -i -e "s|ISSetup.ISAdminUserName|$(get_prop "ISAdminUserName")|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|ISSetup.ISAdminPassword|$(get_prop "ISAdminPassword")|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

#--------------Application Configurations-----------------#
sed -i -e "s|{TestArtifactDirectoryPath}|${TEST_ARTIFACTS}|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
#sed -i -e "s|AppConfig.Application.ClientID|Application.ClientID|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
sed -i -e "s|{ISDirectoryPath}|${TEST_HOME}/wso2is-7.0.0|g" ${ACCELERATION_INTEGRATION_TESTS_CONFIG}

cat ${ACCELERATION_INTEGRATION_TESTS_CONFIG}
#
#echo '################### Build the Test framework ####################'
mvn clean install  -Dmaven.test.skip=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
#
#
#echo '################### API Publish and Subscribe Step ##################'
cd ${ACCELERATION_INTEGRATION_TESTS_HOME}/accelerator-tests/is-tests/is-setup
mvn clean install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
MVNSTATE=$?

#tail -1000f ${RUNNER_HOME}/wso2.log

sleep 20

exit 1


