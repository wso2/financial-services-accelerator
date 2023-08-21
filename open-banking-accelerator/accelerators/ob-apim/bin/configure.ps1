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

# command to execute
# ./configure.ps1 <WSO2_OB_APIM_HOME>

$Props = convertfrom-stringdata (get-content ./../repository/conf/configure.properties -raw)
$WSO2_OB_APIM_HOME = $args[0]

# set accelerator home
Set-Location ../
$ACCELERATOR_HOME = $pwd.path
Write-Output "Accelerator Home: $ACCELERATOR_HOME"

# set product home
if ($null -eq $WSO2_OB_APIM_HOME)
{
    Set-Location ../
    $WSO2_OB_APIM_HOME = $pwd.path
    Write-Output "Product Home: $WSO2_OB_APIM_HOME"
}

# validate product home
if (-NOT(Test-Path "$WSO2_OB_APIM_HOME/repository/components"))
{
    Write-Output "`n`aERROR:specified product path is not a valid carbon product path`n"
    exit 2
}
else
{
    Write-Output "`nValid carbon product path.`n"
}

# read deployment.toml file
$DEPLOYMENT_TOML_FILE = "$ACCELERATOR_HOME/repository/resources/deployment.toml"
Copy-Item -Path "$ACCELERATOR_HOME/$( $Props.'PRODUCT_CONF_PATH' )" $DEPLOYMENT_TOML_FILE

Function Configure-Datasources
{
    if ($Props.'DB_TYPE' -eq "mysql")
    {
        # APIM
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_APIMGT_URL", "jdbc:mysql://$( $Props.'DB_HOST' ):3306/$( $Props.'DB_APIMGT' )?autoReconnect=true&amp;useSSL=false" })
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_AM_CONFIG_URL", "jdbc:mysql://$( $Props.'DB_HOST' ):3306/$( $Props.'DB_AM_CONFIG' )?autoReconnect=true&amp;useSSL=false" })
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_GOV_URL", "jdbc:mysql://$( $Props.'DB_HOST' ):3306/$( $Props.'DB_GOV' )?autoReconnect=true&amp;useSSL=false" })
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_USER_STORE_URL", "jdbc:mysql://$( $Props.'DB_HOST' ):3306/$( $Props.'DB_USER_STORE' )?autoReconnect=true&amp;useSSL=false" })
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_USER", "$( $Props.'DB_USER' )" })
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_PASS", "$( $Props.'DB_PASS' )" })
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_DRIVER", "$( $Props.'DB_DRIVER' )" })
    }
    else
    {
        # IS
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_APIMGT_URL", "jdbc:sqlserver://$( $Props.'DB_HOST' ):1433;databaseName=$( $Props.'DB_APIMGT' );encrypt=false" })
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_AM_CONFIG_URL", "jdbc:sqlserver://$( $Props.'DB_HOST' ):1433;databaseName=$( $Props.'DB_AM_CONFIG' );encrypt=false" })
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_GOV_URL", "jdbc:sqlserver://$( $Props.'DB_HOST' ):1433;databaseName=$( $Props.'DB_GOV' );encrypt=false" })
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_USER_STORE_URL", "jdbc:sqlserver://$( $Props.'DB_HOST' ):1433;databaseName=$( $Props.'DB_USER_STORE' );encrypt=false" })
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_USER", "$( $Props.'DB_USER' )" })
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_PASS", "$( $Props.'DB_PASS' )" })
        Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "DB_DRIVER", "$( $Props.'DB_DRIVER' )" })
    }
}

Function Create-Databases
{
    if ($Props.'DB_TYPE' -eq "mysql")
    {
        if ($Props.'DB_PASS' -eq "")
        {
            $DB_MYSQL_PASS = ""
        }
        else
        {
            $DB_MYSQL_PASS = $Props.'DB_PASS'
        }
        Write-Output "`nCreating MySQL databases"
        Write-Output "================================================`n"
        Create-Mysql-Databases

        Write-Output "`nUpdate am_application_registration table input field size (temporary)"
        Write-Output "=======================================================================`n"
        Set-Content -Path $WSO2_OB_APIM_HOME/dbscripts/apimgt/mysql.sql -Value (get-content $WSO2_OB_APIM_HOME/dbscripts/apimgt/mysql.sql | ForEach-Object{ $_ -replace "INPUTS VARCHAR(1000)", "INPUTS VARCHAR(7500)" })

        Write-Output "`nCreate database tables"
        Write-Output "================================================`n"
        Create-Mysql-DatabaseTables

        Write-Output "`nAlter SP_METADATA table VALUE field size (temporary)"
        Write-Output "=======================================================================`n"
        mysql -u"$( $Props.'DB_USER' )" -p"$DB_MYSQL_PASS" -h"$( $Props.'DB_HOST' )" -e"ALTER TABLE $( $Props.'DB_APIMGT' ).SP_METADATA MODIFY VALUE VARCHAR(4096);"
    }
    else
    {
        Write-Output "`nAssume MSSQL/Oracle databases have already created manually"

        Write-Output "`nUpdate idn_req_object_reference table foreign keys (temporary)"
        Write-Output "=======================================================================`n"
        Set-Content -Path $WSO2_OB_APIM_HOME/dbscripts/apimgt/mssql.sql -Value (get-content $WSO2_OB_APIM_HOME/dbscripts/apimgt/mssql.sql | ForEach-Object{ $_ -replace "FOREIGN KEY (CONSUMER_KEY_ID) REFERENCES IDN_OAUTH_CONSUMER_APPS(ID) ON DELETE CASCADE ,", "FOREIGN KEY (CONSUMER_KEY_ID) REFERENCES IDN_OAUTH_CONSUMER_APPS(ID)," })
        Set-Content -Path $WSO2_OB_APIM_HOME/dbscripts/apimgt/mssql.sql -Value (get-content $WSO2_OB_APIM_HOME/dbscripts/apimgt/mssql.sql | ForEach-Object{ $_ -replace "FOREIGN KEY (TOKEN_ID) REFERENCES IDN_OAUTH2_ACCESS_TOKEN(TOKEN_ID),", "FOREIGN KEY (TOKEN_ID) REFERENCES IDN_OAUTH2_ACCESS_TOKEN(TOKEN_ID) ON DELETE CASCADE," })
    }
}

Function Create-Mysql-Databases
{
    mysql -u"$( $Props.'DB_USER' )" -p"$DB_MYSQL_PASS" -h"$( $Props.'DB_HOST' )" -e"DROP DATABASE IF EXISTS $( $Props.'DB_APIMGT' ); CREATE DATABASE $( $Props.'DB_APIMGT' ); ALTER DATABASE $( $Props.'DB_APIMGT' ) CHARACTER SET latin1 COLLATE latin1_swedish_ci;"
    Write-Output "Database Created: $( $Props.'DB_APIMGT' )"
    mysql -u"$( $Props.'DB_USER' )" -p"$DB_MYSQL_PASS" -h"$( $Props.'DB_HOST' )" -e"DROP DATABASE IF EXISTS $( $Props.'DB_AM_CONFIG' ); CREATE DATABASE $( $Props.'DB_AM_CONFIG' ); ALTER DATABASE $( $Props.'DB_AM_CONFIG' ) CHARACTER SET latin1 COLLATE latin1_swedish_ci;"
    Write-Output "Database Created: $( $Props.'DB_AM_CONFIG' )"
    mysql -u"$( $Props.'DB_USER' )" -p"$DB_MYSQL_PASS" -h"$( $Props.'DB_HOST' )" -e"DROP DATABASE IF EXISTS $( $Props.'DB_GOV' ); CREATE DATABASE $( $Props.'DB_GOV' ); ALTER DATABASE $( $Props.'DB_GOV' ) CHARACTER SET latin1 COLLATE latin1_swedish_ci;"
    Write-Output "Database Created: $( $Props.'DB_GOV' )"
    mysql -u"$( $Props.'DB_USER' )" -p"$DB_MYSQL_PASS" -h"$( $Props.'DB_HOST' )" -e"DROP DATABASE IF EXISTS $( $Props.'DB_USER_STORE' ); CREATE DATABASE $( $Props.'DB_USER_STORE' ); ALTER DATABASE $( $Props.'DB_USER_STORE' ) CHARACTER SET latin1 COLLATE latin1_swedish_ci;"
    Write-Output "Database Created: $( $Props.'DB_USER_STORE' )"
}

Function Create-Mysql-DatabaseTables
{
    cmd.exe /c "mysql -u$( $Props.'DB_USER' ) -p$DB_MYSQL_PASS -D$( $Props.'DB_APIMGT' ) -h$( $Props.'DB_HOST' ) < $WSO2_OB_APIM_HOME\dbscripts\apimgt\mysql.sql"
    Write-Output "Database tables Created for: $( $Props.'DB_APIMGT' )"
    cmd.exe /c "mysql -u$( $Props.'DB_USER' ) -p$DB_MYSQL_PASS -D$( $Props.'DB_AM_CONFIG' ) -h$( $Props.'DB_HOST' ) < $WSO2_OB_APIM_HOME\dbscripts\mysql.sql"
    Write-Output "Database tables Created for: $( $Props.'DB_AM_CONFIG' )"
    cmd.exe /c "mysql -u$( $Props.'DB_USER' ) -p$DB_MYSQL_PASS -D$( $Props.'DB_GOV' ) -h$( $Props.'DB_HOST' ) < $WSO2_OB_APIM_HOME\dbscripts\mysql.sql"
    Write-Output "Database tables Created for: $( $Props.'DB_GOV' )"
    cmd.exe /c "mysql -u$( $Props.'DB_USER' ) -p$DB_MYSQL_PASS -D$( $Props.'DB_USER_STORE' ) -h$( $Props.'DB_HOST' ) < $WSO2_OB_APIM_HOME\dbscripts\mysql.sql"
    Write-Output "Database tables Created for: $( $Props.'DB_USER_STORE' )"
}

Function Add-Json-Fault-Sequence
{
    Set-Content -Path $WSO2_OB_APIM_HOME/repository/deployment/server/synapse-configs/default/sequences/_cors_request_handler_.xml -Value (get-content $WSO2_OB_APIM_HOME/repository/deployment/server/synapse-configs/default/sequences/_cors_request_handler_.xml | ForEach-Object{ $_ -replace "</sequence>", "`t<sequence key=`"jsonConverter`"/>`n</sequence>" })
}

Write-Output  "`nReplace hostnames `n"
Write-Output  "================================================`n"
Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "APIM_HOSTNAME", "$( $Props.'APIM_HOSTNAME' )" })
Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "IS_HOSTNAME", "$( $Props.'IS_HOSTNAME' )" })
Set-Content -Path $DEPLOYMENT_TOML_FILE -Value (get-content $DEPLOYMENT_TOML_FILE | ForEach-Object{ $_ -replace "BI_HOSTNAME", "$( $Props.'BI_HOSTNAME' )" })

Write-Output  "`nConfigure datasources `n"
Write-Output  "================================================`n"
Configure-Datasources

Write-Output  "`nCreate databases"
Write-Output  "================================================`n"
Create-Databases

Write-Output  "`nCopy deployment.toml file to repository/conf `n"
Write-Output  "================================================`n"
Copy-Item $DEPLOYMENT_TOML_FILE $WSO2_OB_APIM_HOME/repository/conf/
Remove-Item $DEPLOYMENT_TOML_FILE

Write-Output  "`nAdd json converter for fault sequences `n"
Write-Output  "================================================`n"
Add-Json-Fault-Sequence
