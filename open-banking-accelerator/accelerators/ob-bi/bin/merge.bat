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

REM merge.bat script copy the WSO2 OB BI accelerator artifacts on top of WSO2 SI base product

REM merge.bat <SI_HOME>

@echo off

SET SI_HOME=%1

REM set accelerator home
cd %CD%..\..
SET OB_BI_ACCELERATOR_HOME=%CD%
echo "Accelerator home is: %OB_BI_ACCELERATOR_HOME%"

REM set product home
if "%SI_HOME%" == "" (
	cd %CD%..\..
	SET SI_HOME=%CD%
	echo "Product home is: %SI_HOME%"
)
REM validate product home
if not exist %SI_HOME%\deployment\siddhi-files (
  echo "ERROR:specified product path is not a valid carbon product path"
) else (
  echo "Valid carbon product path."
)

echo  "Copying open banking artifacts"
echo  ================================================

robocopy %OB_BI_ACCELERATOR_HOME%\carbon-home /e %SI_HOME%
echo "Complete!"
