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

#!/bin/bash
set -e
export TRIVY_DISABLE_VEX_NOTICE=true

# Ensure /usr/local/tomcat/webapps exists
mkdir -p /usr/local/tomcat/webapps

# Copy the WAR file as ROOT.war if it doesn't already exist
if [ ! -f "/usr/local/tomcat/webapps/ROOT.war" ]; then
    echo "Deploying ROOT.war..."
    cp /usr/local/tomcat/consent.war /usr/local/tomcat/webapps/ROOT.war
fi

# Start Tomcat
exec /usr/local/tomcat/bin/catalina.sh run
