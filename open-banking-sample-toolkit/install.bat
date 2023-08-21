@echo off
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

set PRODUCT_HOME=%1

rem Set product home
if "%PRODUCT_HOME%"=="" call :set_product_home

rem Validate product home
if exist "%PRODUCT_HOME%\repository\components\" (
    call :valid_carbon
) else (
    call :invalid_carbon
)

call :install_jar %PRODUCT_HOME%\repository\components\dropins\com.wso2.*
call :install_jar %PRODUCT_HOME%\repository\components\lib\com.wso2.*
call :install_jar %PRODUCT_HOME%\repository\components\plugins\com.wso2.*

call :install_jar %PRODUCT_HOME%\repository\components\dropins\org.wso2.*
call :install_jar %PRODUCT_HOME%\repository\components\lib\org.wso2.*
call :install_jar %PRODUCT_HOME%\repository\components\plugins\org.wso2.*

exit /b


:install_jar
for %%N IN (%1) do (
	mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=%%N -Dpackaging=jar
)
exit /b

:set_product_home
set PRODUCT_HOME=%cd%
echo.
echo "Product Home: "%PRODUCT_HOME%
exit /b

:invalid_carbon
echo.
echo "ERROR:specified product path is not a valid carbon product path"
echo.
exit /b

:valid_carbon
echo.
echo "Product Home: "%PRODUCT_HOME%
echo.
echo "Valid carbon product path."
echo.
exit /b

