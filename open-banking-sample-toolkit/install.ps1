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

$PRODUCT_HOME = $args[0]

# set product home
if ($null -eq $PRODUCT_HOME)
{
    $PRODUCT_HOME = $pwd.path
    Write-Output "Product Home: $PRODUCT_HOME"
}

# validate product home
if (-NOT(Test-Path "$PRODUCT_HOME/repository/components"))
{
    Write-Output "`n`aERROR:specified product path is not a valid carbon product path`n"
    exit 2
}
else
{
    Write-Output "`nValid carbon product path.s`n"
}

foreach ($item in (Get-ChildItem -path "$($PRODUCT_HOME)/repository/components/dropins" -Recurse -Filter 'com.wso2.*').'Name') {cmd.exe /c "mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=$($PRODUCT_HOME)/repository/components/dropins/$($item) -Dpackaging=jar"}
foreach ($item in (Get-ChildItem -path "$($PRODUCT_HOME)/repository/components/lib" -Recurse -Filter 'com.wso2.*').'Name') {cmd.exe /c "mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=$($PRODUCT_HOME)/repository/components/lib/$($item) -Dpackaging=jar"}
foreach ($item in (Get-ChildItem -path "$($PRODUCT_HOME)/repository/components/plugins" -Recurse -Filter 'com.wso2.*').'Name') {cmd.exe /c "mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=$($PRODUCT_HOME)/repository/components/plugins/$($item) -Dpackaging=jar"}

foreach ($item in (Get-ChildItem -path "$($PRODUCT_HOME)/repository/components/dropins" -Recurse -Filter 'org.wso2.*').'Name') {cmd.exe /c "mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=$($PRODUCT_HOME)/repository/components/dropins/$($item) -Dpackaging=jar"}
foreach ($item in (Get-ChildItem -path "$($PRODUCT_HOME)/repository/components/lib" -Recurse -Filter 'org.wso2.*').'Name') {cmd.exe /c "mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=$($PRODUCT_HOME)/repository/components/lib/$($item) -Dpackaging=jar"}
foreach ($item in (Get-ChildItem -path "$($PRODUCT_HOME)/repository/components/plugins" -Recurse -Filter 'org.wso2.*').'Name') {cmd.exe /c "mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=$($PRODUCT_HOME)/repository/components/plugins/$($item) -Dpackaging=jar"}

