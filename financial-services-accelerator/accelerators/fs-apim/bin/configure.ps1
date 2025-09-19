 # Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
#   If your accelerator is located inside of the base product you can just call .\configure.ps1
#   If your accelerator is in a different location you can call .\configure.ps1 <YOUR_BASE_PRODUCT_HOME_DIR>

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

# A utility function to Find and Replace texts in a file.
Function Find-Replace {
    param(
        [string]$FILE_PATH,
        [string]$OLD_TEXT,
        [string]$NEW_TEXT
    )

    # Read the file content
    $CONTENT = Get-Content $FILE_PATH -Raw

    # Escape special characters in OLD_TEXT for the regex
    $escapedOldText = [regex]::Escape($OLD_TEXT)

    # Define the regex pattern to consider non-word characters as boundaries
    $regex = "(?<![a-zA-Z0-9_])" + $escapedOldText + "(?![a-zA-Z0-9_])"

    # Replace the whole words
    $NEW_CONTENT = [regex]::Replace($CONTENT, $regex, $NEW_TEXT)

    # Write the new content back to the file
    $NEW_CONTENT | Set-Content $FILE_PATH
}


# Get the root directory location of the accelerator. Which is <BASE_PRODUCT>/<ACCELERATOR>/
Set-Location (Join-Path $PSScriptRoot ".\..\")
$WSO2_FS_ACCELERATOR_HOME = (Get-Location).path
Write-Output "[INFO] Accelerator Home : $WSO2_FS_ACCELERATOR_HOME"

# Get the root directory of the base product.
if ($null -eq $WSO2_BASE_PRODUCT_HOME) {
    Set-Location (Join-Path $WSO2_FS_ACCELERATOR_HOME ".\..\")
    $WSO2_BASE_PRODUCT_HOME = (Get-Location).path
}
Write-Output "[INFO] Base Product Home : $WSO2_BASE_PRODUCT_HOME"

# Check whether the extracted base product location contains a valid WSO2 carbon product by checking whether this location
# contains the "repository/components" directory.
if (-NOT(Test-Path (Join-Path $WSO2_BASE_PRODUCT_HOME "repository\components"))) {
    Write-Output "[ERROR] $WSO2_BASE_PRODUCT_HOME does NOT contain a valid carbon product!"
    # The current path does not contain a valid carbon product.
    # Set the current working directory to the original location and exit.
    Exit-Clean
} else {
    Write-Output "[INFO] $WSO2_BASE_PRODUCT_HOME is a valid carbon product home."
}

# Get the location of the configure.properties
$CONFIG_PROPERTIES_PATH = Join-Path $WSO2_FS_ACCELERATOR_HOME "repository\conf\configure.properties"
Write-Output "[INFO] configure.properties location : $CONFIG_PROPERTIES_PATH"

# Load the variables in the configure.properties file
$PROPERTIES = ConvertFrom-StringData (Get-Content $CONFIG_PROPERTIES_PATH -raw)

$SELECTED_DEPLOYMENT_TOML_FILE = Join-Path $WSO2_FS_ACCELERATOR_HOME $PROPERTIES.'PRODUCT_CONF_PATH'
Write-Output "[INFO] Selected deployment.toml location : $SELECTED_DEPLOYMENT_TOML_FILE"

$DEPLOYMENT_TOML_FILE = Join-Path $WSO2_FS_ACCELERATOR_HOME "repository\resources\deployment.toml"
# Temporary copy the selected toml file so we can make changes to it.
Copy-Item -Path $SELECTED_DEPLOYMENT_TOML_FILE $DEPLOYMENT_TOML_FILE
Write-Output "[INFO] Temporary deployment.toml location : $DEPLOYMENT_TOML_FILE"

# A function to replace the database related variables in the temp deployment.toml with their actual values from configure.properties
Function Set-Datasources
{
    if ($PROPERTIES.'DB_TYPE' -eq "mysql")
    {
        # MySQL
        Find-Replace $DEPLOYMENT_TOML_FILE "DB_APIMGT_URL" "jdbc:mysql://$( $PROPERTIES.'DB_HOST' ):3306/$( $PROPERTIES.'DB_APIMGT' )?allowPublicKeyRetrieval=true&amp;autoReconnect=true&amp;useSSL=false"
        Find-Replace $DEPLOYMENT_TOML_FILE "DB_AM_CONFIG_URL" "jdbc:mysql://$( $PROPERTIES.'DB_HOST' ):3306/$( $PROPERTIES.'DB_AM_CONFIG' )?allowPublicKeyRetrieval=true&amp;autoReconnect=true&amp;useSSL=false"
        Find-Replace $DEPLOYMENT_TOML_FILE "DB_USER_STORE_URL" "jdbc:mysql://$( $PROPERTIES.'DB_HOST' ):3306/$( $PROPERTIES.'DB_USER_STORE' )?allowPublicKeyRetrieval=true&amp;autoReconnect=true&amp;useSSL=false"
        Find-Replace $DEPLOYMENT_TOML_FILE "DB_USER" "$( $PROPERTIES.'DB_USER' )"
        Find-Replace $DEPLOYMENT_TOML_FILE "DB_PASS" "$( $PROPERTIES.'DB_PASS' )"
        Find-Replace $DEPLOYMENT_TOML_FILE "DB_DRIVER" "$( $PROPERTIES.'DB_DRIVER' )"
    }
    elseif($PROPERTIES.'DB_TYPE' -eq "mssql")
    {
        # Microsoft SQL Server
        Find-Replace $DEPLOYMENT_TOML_FILE "DB_APIMGT_URL" "jdbc:sqlserver://$( $PROPERTIES.'DB_HOST' ):1433;databaseName=$( $PROPERTIES.'DB_APIMGT' );encrypt=false"
        Find-Replace $DEPLOYMENT_TOML_FILE "DB_AM_CONFIG_URL" "jdbc:sqlserver://$( $PROPERTIES.'DB_HOST' ):1433;databaseName=$( $PROPERTIES.'DB_AM_CONFIG' );encrypt=false"
        Find-Replace $DEPLOYMENT_TOML_FILE "DB_USER_STORE_URL" "jdbc:sqlserver://$( $PROPERTIES.'DB_HOST' ):1433;databaseName=$( $PROPERTIES.'DB_USER_STORE' );encrypt=false"
        Find-Replace $DEPLOYMENT_TOML_FILE "DB_USER" "$( $PROPERTIES.'DB_USER' )"
        Find-Replace $DEPLOYMENT_TOML_FILE "DB_PASS" "$( $PROPERTIES.'DB_PASS' )"
        Find-Replace $DEPLOYMENT_TOML_FILE "DB_DRIVER" "$( $PROPERTIES.'DB_DRIVER' )"
    }
    else {
        Write-Output "[ERROR] Unsupported Database Type!"
        Exit-Clean
    }
}

# A function to replace the hostname related variables in the temp deployment.toml with their actual values from configure.properties
Function Set-Hostnames {
    Find-Replace $DEPLOYMENT_TOML_FILE "APIM_HOSTNAME" "$( $PROPERTIES.'APIM_HOSTNAME' )"
    Find-Replace $DEPLOYMENT_TOML_FILE "IS_HOSTNAME" "$( $PROPERTIES.'IS_HOSTNAME' )"
}

# A utility function to create a database.
Function Add-Database {
    param ([string]$DB_USER, [string]$DB_PASS, [string]$DB_HOST, [string]$DB_NAME)
    mysql -u"$($DB_USER)" -p"$($DB_PASS)" -h"$($DB_HOST)" -e "DROP DATABASE IF EXISTS $($DB_NAME); CREATE DATABASE $( $DB_NAME ) DEFAULT CHARACTER SET latin1;"
}

# A utility function to create a table inside a given database.
Function Add-TablesToDatabase {
    param ([string]$DB_USER, [string]$DB_PASS, [string]$DB_HOST, [string]$DB_NAME, [string]$DB_SOURCE)
    mysql -u"$($DB_USER)" -p"$($DB_PASS)" -h"$($DB_HOST)" -D"$($DB_NAME)" -e "SOURCE $($DB_SOURCE)"
}

# A function to create the databases. ONLY SUPPORTED FOR THE MYSQL
Function Add-Databases {
    if ($PROPERTIES.'DB_TYPE' -eq "mysql") {
        $DB_MYSQL_PASS = ""
        if (-NOT($PROPERTIES.'DB_PASS' -eq "")) {
            $DB_MYSQL_PASS = $PROPERTIES.'DB_PASS'
        }

        Add-Database "$( $PROPERTIES.'DB_USER' )" $DB_MYSQL_PASS "$( $PROPERTIES.'DB_HOST' )" "$( $PROPERTIES.'DB_APIMGT' )"
        Write-Output "[INFO] Database Created: $( $PROPERTIES.'DB_APIMGT' )"

        Add-Database "$( $PROPERTIES.'DB_USER' )" $DB_MYSQL_PASS "$( $PROPERTIES.'DB_HOST' )" "$( $PROPERTIES.'DB_AM_CONFIG' )"
        Write-Output "[INFO] Database Created: $( $PROPERTIES.'DB_AM_CONFIG' )"

        Add-Database "$( $PROPERTIES.'DB_USER' )" $DB_MYSQL_PASS "$( $PROPERTIES.'DB_HOST' )" "$( $PROPERTIES.'DB_USER_STORE' )"
        Write-Output "[INFO] Database Created: $( $PROPERTIES.'DB_USER_STORE' )"
    }
    else {
        Write-Output "[INFO] The databases must be created manually for non mysql DBMSs."
    }
}

# A function to create the database tables. ONLY SUPPORTED FOR THE MYSQL
Function Add-DatabaseTables {
    if ($PROPERTIES.'DB_TYPE' -eq "mysql") {

        Write-Output "[NOTE] Update am_application_registration table input field size (temporary)"
        Find-Replace (Join-Path $WSO2_BASE_PRODUCT_HOME "dbscripts\apimgt\mysql.sql") "INPUTS VARCHAR(1000)," "INPUTS VARCHAR(7500),"

        $DB_MYSQL_PASS = ""
        if (-NOT($PROPERTIES.'DB_PASS' -eq "")) {
            $DB_MYSQL_PASS = $PROPERTIES.'DB_PASS'
        }

        Add-TablesToDatabase "$( $PROPERTIES.'DB_USER' )" $DB_MYSQL_PASS "$( $PROPERTIES.'DB_HOST' )" "$( $PROPERTIES.'DB_APIMGT' )" "$(Join-Path $WSO2_BASE_PRODUCT_HOME "dbscripts\apimgt\mysql.sql")"
        Write-Output "[NOTE] Database tables Created for: $( $PROPERTIES.'DB_APIMGT' )"

        Add-TablesToDatabase "$( $PROPERTIES.'DB_USER' )" $DB_MYSQL_PASS "$( $PROPERTIES.'DB_HOST' )" "$( $PROPERTIES.'DB_AM_CONFIG' )" "$(Join-Path $WSO2_BASE_PRODUCT_HOME "dbscripts\mysql.sql")"
        Write-Output "[NOTE] Database tables Created for: $( $PROPERTIES.'DB_AM_CONFIG' )"

        Add-TablesToDatabase "$( $PROPERTIES.'DB_USER' )" $DB_MYSQL_PASS "$( $PROPERTIES.'DB_HOST' )" "$( $PROPERTIES.'DB_USER_STORE' )" "$(Join-Path $WSO2_BASE_PRODUCT_HOME "dbscripts\mysql.sql")"
        Write-Output "[NOTE] Database tables Created for: $( $PROPERTIES.'DB_USER_STORE' )"

        mysql -u"$( $PROPERTIES.'DB_USER' )" -p"$DB_MYSQL_PASS" -h"$( $PROPERTIES.'DB_HOST' )" -e "ALTER TABLE $( $PROPERTIES.'DB_APIMGT' ).SP_METADATA MODIFY VALUE VARCHAR(4096);"
    }
    else {
        Write-Output "[INFO] The database tables must be created manually for non mysql DBMSs."
        Find-Replace (Join-Path $WSO2_BASE_PRODUCT_HOME "dbscripts\apimgt\mssql.sql") "FOREIGN KEY (CONSUMER_KEY_ID) REFERENCES IDN_OAUTH_CONSUMER_APPS(ID) ON DELETE CASCADE ," "FOREIGN KEY (CONSUMER_KEY_ID) REFERENCES IDN_OAUTH_CONSUMER_APPS(ID),"
        Find-Replace (Join-Path $WSO2_BASE_PRODUCT_HOME "dbscripts\apimgt\mssql.sql") "FOREIGN KEY (TOKEN_ID) REFERENCES IDN_OAUTH2_ACCESS_TOKEN(TOKEN_ID)," "FOREIGN KEY (TOKEN_ID) REFERENCES IDN_OAUTH2_ACCESS_TOKEN(TOKEN_ID) ON DELETE CASCADE,"
    }
}

Write-Output "============================================"
Write-Output "[INFO] Configuring the hostnames..."
Set-Hostnames
Write-Output "[INFO] Hostnames configurations completed!"

Write-Output "============================================"
Write-Output "[INFO] Configuring the datasources..."
Set-Datasources
Write-Output "[INFO] Datasources configurations completed!"

Write-Output "============================================"
Copy-Item $DEPLOYMENT_TOML_FILE (Join-Path $WSO2_BASE_PRODUCT_HOME "repository\conf\deployment.toml")
Write-Output "[INFO] Copied temp toml to the $(Join-Path $WSO2_BASE_PRODUCT_HOME "repository\conf\deployment.toml")"

Remove-Item $DEPLOYMENT_TOML_FILE
Write-Output "[INFO] Deleted temp toml $DEPLOYMENT_TOML_FILE"

Write-Output "============================================"
Add-Databases

Write-Output "============================================"
Add-DatabaseTables

$DEFAULT_JSON_FILE_PATH = Join-Path $WSO2_BASE_PRODUCT_HOME "repository\resources\conf\default.json"
Find-Replace $DEFAULT_JSON_FILE_PATH "`"keystore.listener_profile.bind_address`": `"0.0.0.0`"" "`"keystore.listener_profile.bind_address`": `"0:0:0:0:0:0:0:0`""

Exit-Clean
