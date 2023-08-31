 REM Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 REM
 REM WSO2 LLC. licenses this file to you under the Apache License,
 REM Version 2.0 (the "License"); you may not use this file except
 REM in compliance with the License.
 REM You may obtain a copy of the License at
 REM
 REM    http://www.apache.org/licenses/LICENSE-2.0
 REM
 REM Unless required by applicable law or agreed to in writing,
 REM software distributed under the License is distributed on an
 REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 REM KIND, either express or implied. See the License for the
 REM specific language governing permissions and limitations
 REM under the License.

REM  merge.bat script copy the WSO2 OB IS accelerator artifacts on top of WSO2 IS base product
REM 
REM  merge.bat <IS_HOME>

@echo off

SET IS_HOME=%1

REM set accelerator home
cd %CD%..\..
SET OB_IS_ACCELERATOR_HOME=%CD%
echo "Accelerator home is: %OB_IS_ACCELERATOR_HOME%"

REM set product home
if "%IS_HOME%" == "" (
	cd %CD%..\..
    SET IS_HOME=%CD%
    echo "Product home is: %IS_HOME%"
)

REM validate product home
if not exist %IS_HOME%\repository\components (
  echo "ERROR:specified product path is not a valid carbon product path"
) else (
  echo "Valid carbon product path."
)

echo "Remove old open banking artifacts from base product"
del %IS_HOME%\repository\components\dropins\com.wso2.openbanking.*
del %IS_HOME%\repository\components\lib\com.wso2.openbanking.*

echo  "Copying open banking artifacts"
echo  ================================================
robocopy %OB_IS_ACCELERATOR_HOME%\carbon-home /e %IS_HOME% 


DEL  %IS_HOME%\repository\components\dropins\org.wso2.carbon.identity.application.authentication.handler.identifier-6.3.11.6.jar
echo "Complete!"
