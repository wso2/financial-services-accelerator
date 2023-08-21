#!/bin/bash
 # Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

PRODUCT_HOME=$1

# set product home
if [ "${PRODUCT_HOME}" == "" ]; then
  PRODUCT_HOME=$(pwd)
  echo "Product home is: ${PRODUCT_HOME}"
fi

# validate product home
if [ ! -d "${PRODUCT_HOME}/repository/components" ]; then
  echo -e "\n\aERROR:specified product path is not a valid carbon product path\n"
  exit 2
else
  echo -e "\nValid carbon product path.\n"
fi

find "${PRODUCT_HOME}"/repository/components/dropins -name "com.wso2.*" -exec mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile={} -Dpackaging=jar \;
find "${PRODUCT_HOME}"/repository/components/lib -name "com.wso2.*" -exec mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile={} -Dpackaging=jar \;
find "${PRODUCT_HOME}"/repository/components/plugins -name "com.wso2.*" -exec mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile={} -Dpackaging=jar \;

find "${PRODUCT_HOME}"/repository/components/dropins -name "org.wso2.*" -exec mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile={} -Dpackaging=jar \;
find "${PRODUCT_HOME}"/repository/components/lib -name "org.wso2.*" -exec mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile={} -Dpackaging=jar \;
find "${PRODUCT_HOME}"/repository/components/plugins -name "org.wso2.*" -exec mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile={} -Dpackaging=jar \;
