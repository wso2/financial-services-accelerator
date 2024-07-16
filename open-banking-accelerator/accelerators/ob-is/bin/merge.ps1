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

# How to execute :
#   If your accelerator is located inside of the base product you can just call .\merge.ps1
#   If your accelerator is in a different location you can call .\merge.ps1 <YOUR_BASE_PRODUCT_HOME_DIR>

# IMPORTANT : 
#   Please note that these powershell files are not digitally signed yet. So, powershell will not allow these scripts under any of their execution policies.
#   You may need to run these scripts on an execution policy bypassed powershell instance. You can do that using the following command.
#       powershell.exe -executionpolicy bypass <SCRIPT_FILEPATH>

# Get the current working directory of the powershell session, so we can set to this directory after the script finishes.
$CURRENT_DIRECTORY = (Get-Location).path

# Some black magic to get the fully qualified path of the WSO2 Base Product if it was given as an argument.
$WSO2_BASE_PRODUCT_HOME = $args[0]
if (-NOT($null -eq $WSO2_BASE_PRODUCT_HOME)) {
    if (Test-Path $WSO2_BASE_PRODUCT_HOME) {
        Set-Location $WSO2_BASE_PRODUCT_HOME
        $WSO2_BASE_PRODUCT_HOME = (Get-Location).path
        Set-Location $CURRENT_DIRECTORY
    }
}

Function Exit-Clean {
    Set-Location $CURRENT_DIRECTORY
    exit 1
}

# Get the root directory location of the accelerator. Which is <BASE_PRODUCT>/<ACCELERATOR>/
Set-Location (Join-Path $PSScriptRoot ".\..\")
$WSO2_OB_ACCELERATOR_HOME = (Get-Location).path
Write-Output "[INFO] Accelerator Home : $WSO2_OB_ACCELERATOR_HOME"

# Get the root directory of the base product.
if ($null -eq $WSO2_BASE_PRODUCT_HOME) {
    Set-Location (Join-Path $WSO2_OB_ACCELERATOR_HOME ".\..\")
    $WSO2_BASE_PRODUCT_HOME = (Get-Location).path
}
Write-Output "[INFO] Base Product Home : $WSO2_BASE_PRODUCT_HOME"

# Check whether the extracted base product location contains a valid WSO2 carbon product by checking whether this location
# contains the "repository/components" directory.
if (-NOT(Test-Path (Join-Path $WSO2_BASE_PRODUCT_HOME "repository\components"))) {
    Write-Error "[ERROR] $WSO2_BASE_PRODUCT_HOME does NOT contain a valid carbon product!"
    # The current path does not contain a valid carbon product.
    # Set the current working directory to the original location and exit.
    Exit-Clean
} else {
    Write-Output "[INFO] $WSO2_BASE_PRODUCT_HOME is a valid carbon product home."
}

# Remove old open-banking artifacts
Write-Output "[INFO] Removing old open-banking artifacts..."
Get-ChildItem (Join-Path $WSO2_BASE_PRODUCT_HOME "repository\components\dropins") | Where-Object{$_.Name -Match "com.wso2.openbanking.accelerator.*"} | Remove-Item
Get-ChildItem (Join-Path $WSO2_BASE_PRODUCT_HOME "repository\components\lib") | Where-Object{$_.Name -Match "com.wso2.openbanking.accelerator.*"} | Remove-Item
Write-Output "[INFO] All previous OB artifacts have been deleted!"

# Copying all the new OB artifacts to the base product
# Copy-Item -Force -Recurse -Verbose (Join-Path $WSO2_OB_ACCELERATOR_HOME "carbon-home\*") -Destination $WSO2_BASE_PRODUCT_HOME
# Using Robocopy.exe becuase powershell Copy-Item cmdlet doesn't do recursive copying after a certain number of subdirectories.
Write-Output "[INFO] Copying new open-banking artifacts..."
Robocopy.exe (Join-Path $WSO2_OB_ACCELERATOR_HOME "carbon-home") $WSO2_BASE_PRODUCT_HOME * /E /NFL /NDL /NJH /NJS /nc /ns /np
Write-Output "[INFO] All the new OB artifacts has been copied!"

$WEB_APPS_PATH = (Join-Path $WSO2_BASE_PRODUCT_HOME "repository\deployment\server\webapps")

# Delete the consentmgr web app if it exists.
$CONSENTMGR_PATH = (Join-Path $WEB_APPS_PATH "consentmgr")
$CONSENTMGR_RUNTIME_JS_PATH = (Join-Path $CONSENTMGR_PATH "runtime-config.js")
$CONSENTMGR_RUNTIME_JS_BACKUP_PATH = (Join-Path $WEB_APPS_PATH "runtime-config.js")
if (Test-Path $CONSENTMGR_PATH) {
    Write-Output "[INFO] consentmgr web app detected at `"$CONSENTMGR_PATH`""
    
    if (Test-Path $CONSENTMGR_RUNTIME_JS_PATH -PathType Leaf) {
        # If the consentmgr/runtime-config,js file exists, backup it to the webapps directory before deleting the directory
        Write-Output "[INFO] Copying $CONSENTMGR_RUNTIME_JS_PATH to $CONSENTMGR_RUNTIME_JS_BACKUP_PATH..."
        Copy-Item $CONSENTMGR_RUNTIME_JS_PATH -Destination $CONSENTMGR_RUNTIME_JS_BACKUP_PATH -Force
    }

    Write-Output "[INFO] Deleting the current consentmgr web app..."
    Remove-Item -LiteralPath $CONSENTMGR_PATH -Force -Recurse
    Write-Output "[INFO] Deleted the current consentmgr web app!"
}

$CONSENTMGR_PATH_WAR = (Join-Path $WEB_APPS_PATH "consentmgr.war")
$CONSENTMGR_PATH_ZIP = (Join-Path $WEB_APPS_PATH "consentmgr.zip")

# Expand-Archive cmdlet cannot directly extract the .war files. Since .war files are just zip files with some custom file headers and a fancy extension
# we can just rename the war file and extract it.
Move-Item -Path $CONSENTMGR_PATH_WAR -Destination $CONSENTMGR_PATH_ZIP -Force

# Extract the consentmgr.zip
Write-Output "[INFO] Extracting the new consentmgr..."
Expand-Archive -LiteralPath $CONSENTMGR_PATH_ZIP -DestinationPath $CONSENTMGR_PATH -Force
Write-Output "[INFO] New consentmgr extracted!"

if (Test-Path $CONSENTMGR_RUNTIME_JS_BACKUP_PATH -PathType Leaf) {
    # Copy back the backed up runtime-config.js
    Write-Output "[INFO] Copying $CONSENTMGR_RUNTIME_JS_BACKUP_PATH to $CONSENTMGR_RUNTIME_JS_PATH..."
    Copy-Item $CONSENTMGR_RUNTIME_JS_BACKUP_PATH -Destination $CONSENTMGR_RUNTIME_JS_PATH -Force
    Remove-Item -LiteralPath $CONSENTMGR_RUNTIME_JS_BACKUP_PATH -Force
}

Remove-Item -LiteralPath $CONSENTMGR_PATH_ZIP -Force

Get-ChildItem (Join-Path $WSO2_BASE_PRODUCT_HOME "repository\components\dropins") | Where-Object{$_.Name -Match "org.wso2.carbon.identity.application.authentication.handler.identifier-*"} | Remove-Item

Write-Output "[INFO] Completed!"

Exit-Clean
