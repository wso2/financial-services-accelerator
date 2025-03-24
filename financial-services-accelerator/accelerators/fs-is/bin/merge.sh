#!/bin/bash
 # Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

WSO2_IS_HOME=$1

# set accelerator home
cd ../
ACCELERATOR_HOME=$(pwd)
echo "Accelerator home is: ${ACCELERATOR_HOME}"

# set product home
if [ "${WSO2_IS_HOME}" == "" ];
  then
    cd ../
    WSO2_IS_HOME=$(pwd)
    echo "Product home is: ${WSO2_IS_HOME}"
fi

# validate product home
if [ ! -d "${WSO2_IS_HOME}/repository/components" ]; then
  echo -e "\n\aERROR:specified product path is not a valid carbon product path\n";
  exit 2;
else
  echo -e "\nValid carbon product path.\n";
fi

echo -e "================================================\n"

echo -e "\nRemove old financial services artifacts from base product\n"
find "${WSO2_APIM_HOME}"/repository/components/dropins -name "org.wso2.financial.services.accelerator.*" -exec rm -rf {} \;
find "${WSO2_APIM_HOME}"/repository/components/lib -name "org.wso2.financial.services.accelerator.*" -exec rm -rf {} \;


# Setting path for webapps folder and consentmgr folder
WEBAPPS_PATH=${WSO2_IS_HOME}/repository/deployment/server/webapps
CONSENTMGR_PATH=${WEBAPPS_PATH}/consentmgr

# Checking if consent manager app exists
if [ -d "${CONSENTMGR_PATH}" ]; then

  # Backing up runtime-config.js if exists
  if [ -f "${CONSENTMGR_PATH}/runtime-config.js" ]; then
    mv -f "${CONSENTMGR_PATH}/runtime-config.js" "${WEBAPPS_PATH}"
    echo -e "Backup of the runtime-config.js file complete! \n"
    echo -e "================================================\n"
  fi

  # Deleting existing consentmgr
  echo -e "\nDeleting the existing consentmgr artifacts from base product\n"
  echo -e "================================================\n"
  rm -rf "${CONSENTMGR_PATH}"
fi

echo -e "\nCopying open banking artifacts\n"
echo -e "================================================\n"
cp -r ${ACCELERATOR_HOME}/carbon-home/* "${WSO2_IS_HOME}"/

# TODO: Uncomment after adding the consent manager app
# Updating consent manager app
#echo -e "\nUpdating Consentmgr webapp...\n"
#echo -e "\nWARNING: This will replace the current consentmgr web-app with the updated one\n"
#echo -e "\nMake sure to rebuild the web app if you have any customizations in the toolkit/src directory.\n"
#echo -e "\nPlease refer to the ReadMe file in ${WSO2_IS_HOME}/repository/deployment/server/webapps/consentmgr/self-care-portal-frontend\n"

#echo -e "\nExtracting consentmgr.war\n"
#echo -e "================================================\n"
#unzip -q "${WSO2_IS_HOME}/repository/deployment/server/webapps/consentmgr.war" -d "${WSO2_IS_HOME}/repository/deployment/server/webapps/consentmgr"
#rm -f "${WSO2_IS_HOME}/repository/deployment/server/webapps/consentmgr.war"

# Restoring runtime-config.js
#if [ -f "${WEBAPPS_PATH}/runtime-config.js" ]; then
  #mv -f ${WEBAPPS_PATH}/runtime-config.js ${CONSENTMGR_PATH}
  #echo -e "Restoring backup runtime-config.js file complete!"
#fi
