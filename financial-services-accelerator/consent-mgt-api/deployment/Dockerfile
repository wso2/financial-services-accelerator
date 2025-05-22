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

# Use an official Maven image to build the WAR
FROM maven:3.9.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy project files
COPY . .
#COPY deployment/context.xml /app/org.wso2.financial.services.accelerator.consent.mgt.api.endpoint/src/main/webapp/META-INF/context.xml

#USER 10014

FROM tomcat:9.0-jdk11

COPY --from=builder /app/org.wso2.financial.services.accelerator.consent.mgt.api.endpoint/target/consent.war /usr/local/tomcat/consent.war
# Copy the startup script
COPY deployment/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/entrypoint.sh"]
