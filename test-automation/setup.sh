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
RUNNER_HOME=`pwd`
#echo "RUNNER_HOME: $RUNNER_HOME"
#
#echo '======================= SetUp base Products ======================='
## Create the test home directory if it doesn't exist
#if [ ! -d "$TEST_HOME" ]; then
#    mkdir -p $TEST_HOME
#fi
#wget "https://github.com/wso2/product-is/releases/download/v7.0.0/wso2is-7.0.0.zip" -O $TEST_HOME/wso2is-7.0.0.zip
#unzip $TEST_HOME/wso2is-7.0.0.zip -d $TEST_HOME
#
#echo '======================= Installing WSO2 Updates ======================='
#name=$(echo "$WSO2_USERNAME" | cut -d'@' -f1)
#WSO2_UPDATES_HOME=home/$name/.wso2updates
#sudo mkdir -p /home/$name/.wso2-updates/docker && sudo chmod -R 777 /home/$name/.wso2-updates
#cp ${RUNNER_HOME}/test-automation/wso2update_linux $TEST_HOME/wso2is-7.0.0/bin/
#chmod +x $TEST_HOME/wso2is-7.0.0/bin/wso2update_linux
#$TEST_HOME/wso2is-7.0.0/bin/wso2update_linux --username $WSO2_USERNAME --password $WSO2_PASSWORD ||  ($TEST_HOME/wso2is-7.0.0/bin/wso2update_linux --username $WSO2_USERNAME --password $WSO2_PASSWORD )
#printf "%s\n%s\n" "$WSO2_USERNAME" "$WSO2_PASSWORD" | $TEST_HOME/wso2is-7.0.0/bin/wso2update_linux
#
#echo '=================== setup Firefox ==================='
#
#if command -v firefox &> /dev/null
#then
#    echo "Firefox is installed"
#else
#    sudo install -d -m 0755 /etc/apt/keyrings
#    wget -q https://packages.mozilla.org/apt/repo-signing-key.gpg -O- | sudo tee /etc/apt/keyrings/packages.mozilla.org.asc > /dev/null
#    gpg -n -q --import --import-options import-show /etc/apt/keyrings/packages.mozilla.org.asc | awk '/pub/{getline; gsub(/^ +| +$/,""); if($0 == "35BAA0B33E9EB396F59CA838C0BA5CE6DC6315A3") print "\nThe key fingerprint matches ("$0").\n"; else print "\nVerification failed: the fingerprint ("$0") does not match the expected one.\n"}'
#    echo "deb [signed-by=/etc/apt/keyrings/packages.mozilla.org.asc] https://packages.mozilla.org/apt mozilla main" | sudo tee -a /etc/apt/sources.list.d/mozilla.list > /dev/null
#    echo '
#        Package: *
#        Pin: origin packages.mozilla.org
#        Pin-Priority: 1000
#          ' | sudo tee /etc/apt/preferences.d/mozilla
#    sudo apt-get update && sudo apt-get install firefox
#    firefox -version
#fi
#
#echo '=================== Install Java and Maven ==================='
#
##if command -v java &> /dev/null
##then
##    echo "Java is installed"
##    java -version
##else
##   wget https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.16+8/OpenJDK11U-jdk_x64_linux_hotspot_11.0.16_8.tar.gz
##   tar -xvzf OpenJDK11U-jdk_x64_linux_hotspot_11.0.16_8.tar.gz
##   sudo mv jdk-11.0.16+8 /opt/java
##   echo "export JAVA_HOME=/opt/java/jdk-11.0.16+8" >> ~/.bashrc
##   echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
##   source ~/.bashrc
##   java -version
##
##fi
#
##if command -v mvn &> /dev/null
##then
##    echo "Maven is installed"
##    mvn --version
##else
##    sudo apt install -y maven
##fi
#
#echo '======================= Building packs ======================='
##
#mvn -B install --file ${RUNNER_HOME}/pom.xml
#MVNSTATE=$?
##
#echo '======================= Moving Packs to RUNNER_HOME ======================='
#zip_file_name=$(find financial-services-accelerator/accelerators/fs-is/target -maxdepth 1 -name "*.zip" -exec basename {} .zip \;)
#echo "$zip_file_name"
#
#
#unzip "financial-services-accelerator/accelerators/fs-is/target/$zip_file_name.zip" -d $TEST_HOME/wso2is-7.0.0/
##wget https://github.com/ParameswaranSajeenthiran/files/raw/refs/heads/master/wso2-fsiam-accelerator-4.0.0-M3.zip -O wso2-fsiam-accelerator-4.0.0-M3.zip
##unzip wso2-fsiam-accelerator-4.0.0-M3.zip -d $TEST_HOME/wso2is-7.0.0/
#
#echo '======================= Setup MYSQL ======================='
#sudo apt-get update
#sudo apt-get install -y mysql-server
#sudo systemctl start mysql
#mysql --version
#
#echo '======================= Download and install Drivers ======================='
#wget -q https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.2.0/mysql-connector-j-9.2.0.jar
#mv mysql-connector-j-9.2.0.jar $TEST_HOME/wso2is-7.0.0/repository/components/lib
#
#echo '======================= Generate and Export Certificates ======================='
#storepass=wso2carbon
#declare -A servers
#servers["wso2"]="$TEST_HOME/wso2is-7.0.0/repository/resources/security/wso2carbon.jks"
#cert_dir="$TEST_HOME/certs"
#mkdir -p $cert_dir
#
#for alias in "${!servers[@]}"; do
#  keystore="${servers[$alias]}"
#
#  echo "removing old key pair if exists"
#  # Remove old key pair if exists
#  keytool -delete -alias wso2carbon -keystore $keystore -storepass wso2carbon
#
#  echo "generating new key pair"
#
#  # Generate new key pair
#  keytool -genkey -alias wso2carbon -keystore $keystore -keysize 2048 -keyalg RSA -validity 9999 -dname   "CN=obiam, O=OB, L=WSO2, S=COL, C=LK, OU=OB" -ext san=ip:127.0.0.1,dns:localhost,dns:$alias -keypass  wso2carbon -storepass wso2carbon
#
#  echo "exporting public certificate"
#  # Export public certificate
#  keytool -export -alias wso2carbon -keystore $keystore -file $cert_dir/$alias.pem -storepass wso2carbon
#done
#
#echo '======================= Import Certificates into the truststore ======================='
#aliases=("wso2")
## Define the client truststore
#truststores=(
#  "$TEST_HOME/wso2is-7.0.0/repository/resources/security/client-truststore.jks"
#)
## Import certificates into truststores
#for alias in "${aliases[@]}"; do
#  cert="$cert_dir/$alias.pem"
#  for truststore in "${truststores[@]}"; do
#    echo "Importing certificate for alias '$alias' into truststore: $truststore"
#    keytool -import -alias $alias -file $cert_dir/$alias.pem -keystore $truststore -storepass wso2carbon -keypass  wso2carbon -noprompt
#  done
#done
#
#echo '======================= Verify Exchanged Certificates ======================='
#
## Function to check if alias exists in the truststore
#check_alias() {
#  local truststore=$1
#  local alias=$2
#  echo "Checking alias '$alias' in truststore: $truststore"
#
#  keytool -list -keystore $truststore -storepass "$storepass" -alias $alias
#  if [ $? -eq 0 ]; then
#    echo "[✔] Alias '$alias' found in truststore: $truststore"
#  else
#    echo "[✘] Alias '$alias' NOT found in truststore: $truststore"
#    exit 1  # Fail the workflow if a certificate is missing
#  fi
#}
#
## Function to display certificate details
#show_certificate_details() {
#  local truststore=$1
#  local alias=$2
#
#  echo "-------------------------------"
#  echo "Details for alias '$alias' in truststore: $truststore"
#  keytool -list -v -keystore "$truststore" -storepass "$storepass" -alias "$alias" | grep -E "Alias|Valid from|Issuer|Subject"
#  echo "-------------------------------"
#}
#
## Verify imported certificates
#for truststore in "${truststores[@]}"; do
#  echo "Checking truststore: $truststore"
#  for alias in "${aliases[@]}"; do
#    check_alias "$truststore" "$alias"
#    show_certificate_details "$truststore" "$alias"
#  done
#done
#
#echo '======================= Import OB sandbox Root and Issuer Certificates ======================='
#wget 'https://github.com/ParameswaranSajeenthiran/files/raw/refs/heads/master/OB_SandBox_PP_Root%20CA.cer' -O "${TEST_HOME}/OB_SandBox_PP_Root CA.cer"
#keytool -import -alias root -file "${TEST_HOME}/OB_SandBox_PP_Root CA.cer" -keystore "${TEST_HOME}/wso2is-7.0.0/repository/resources/security/client-truststore.jks" -storepass wso2carbon -noprompt
#wget 'https://github.com/ParameswaranSajeenthiran/files/raw/refs/heads/master/OB_SandBox_PP_Issuing%20CA.cer' -O "${TEST_HOME}/OB_SandBox_PP_Issuing CA.cer"
#keytool -import -alias issuer -file "${TEST_HOME}/OB_SandBox_PP_Issuing CA.cer" -keystore "${TEST_HOME}/wso2is-7.0.0/repository/resources/security/client-truststore.jks" -storepass wso2carbon -noprompt
#
#echo '======================= Run merge and Config scripts ======================='
#cd $TEST_HOME/wso2is-7.0.0/$zip_file_name/bin
#bash merge.sh
#bash configure.sh
SQL_SCRIPT="financial-services/accelerators/fs-is/carbon-home/dbscripts/financial-services/event-notifications/mysql.sql"

#source $(pwd)/../repository/conf/configure.properties

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

sleep 120

echo '======================= Test Setup ======================='

curl -X GET "https://localhost:9446/api/server/v1/applications?limit=30&offset=0" \
-H "accept: application/json" \
-H "Authorization: Basic aXNfYWRtaW5Ad3NvMi5jb206d3NvMjEyMw==" \
-k
