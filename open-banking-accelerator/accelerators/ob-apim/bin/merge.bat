 REM !/bin/bash
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

REM merge.bat script copy the WSO2 OB APIM accelerator artifacts on top of WSO2 APIM base product
REM
REM merge.bat <APIM_HOME>

@echo off

SET APIM_HOME=%1

REM set accelerator home
cd %CD%..\..
SET WSO2_OB_ACCELERATOR_HOME=%CD%
echo "Accelerator home is: %WSO2_OB_ACCELERATOR_HOME%"

REM set product home
if "%APIM_HOME%" == "" (
	cd %CD%..\..
	SET APIM_HOME=%CD%
	echo "Product home is: %APIM_HOME%"
)
   
REM validate product home
if not exist %APIM_HOME%\repository\components (
	  echo "ERROR:specified product path is not a valid carbon product path"
	) else (
	  echo "Valid carbon product path."
	)

echo "Remove old open banking artifacts from base product"
del %APIM_HOME%\repository\components\dropins\com.wso2.openbanking.*
del %APIM_HOME%\repository\components\lib\com.wso2.openbanking.*


echo  "Copying open banking artifacts"
echo  ================================================
robocopy %WSO2_OB_ACCELERATOR_HOME%\carbon-home /e %APIM_HOME%
echo  "Complete!"
