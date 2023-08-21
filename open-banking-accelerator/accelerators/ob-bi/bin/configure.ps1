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
# ./configure.ps1 <WSO2_OB_BI_HOME>

$Props = convertfrom-stringdata (get-content ./../repository/conf/configure.properties -raw)
$WSO2_OB_BI_HOME = $args[0]

# set accelerator home
Set-Location ../
$ACCELERATOR_HOME = $pwd.path
Write-Output "Accelerator Home: $ACCELERATOR_HOME"

# set product home
if ($null -eq $WSO2_OB_BI_HOME)
{
    Set-Location ../
    $WSO2_OB_BI_HOME = $pwd.path
    Write-Output "Product Home: $WSO2_OB_BI_HOME"
}

# validate product home
if (-NOT(Test-Path "$WSO2_OB_BI_HOME/deployment/siddhi-files"))
{
    Write-Output "`n`aERROR:specified product path is not a valid carbon product path`n"
    exit 2
}
else
{
    Write-Output "`nValid carbon product path.`n"
}

# read deployment.yaml file
$DEPLOYMENT_YAML_FILE = "$ACCELERATOR_HOME/repository/resources/deployment.yaml"
Copy-Item -Path "$ACCELERATOR_HOME/$( $Props.'PRODUCT_CONF_PATH' )" $DEPLOYMENT_YAML_FILE

Function Configure-Datasources
{
    if ($Props.'DB_TYPE' -eq "mysql")
    {
        # BI
        Set-Content -Path $DEPLOYMENT_YAML_FILE -Value (get-content $DEPLOYMENT_YAML_FILE | ForEach-Object{ $_ -replace "DB_OB_REPORTING_URL", "jdbc:mysql://$( $Props.'DB_HOST' ):3306/$( $Props.'DB_OB_REPORTING' )?autoReconnect=true&useSSL=false" })
        Set-Content -Path $DEPLOYMENT_YAML_FILE -Value (get-content $DEPLOYMENT_YAML_FILE | ForEach-Object{ $_ -replace "DB_OB_REPORTING_SUMMARIZED_URL", "jdbc:mysql://$( $Props.'DB_HOST' ):3306/$( $Props.'DB_OB_REPORTING_SUMMARIZED' )?autoReconnect=true&useSSL=false" })
        Set-Content -Path $DEPLOYMENT_YAML_FILE -Value (get-content $DEPLOYMENT_YAML_FILE | ForEach-Object{ $_ -replace "DB_USER", "$( $Props.'DB_USER' )" })
        Set-Content -Path $DEPLOYMENT_YAML_FILE -Value (get-content $DEPLOYMENT_YAML_FILE | ForEach-Object{ $_ -replace "DB_PASS", "$( $Props.'DB_PASS' )" })
        Set-Content -Path $DEPLOYMENT_YAML_FILE -Value (get-content $DEPLOYMENT_YAML_FILE | ForEach-Object{ $_ -replace "DB_DRIVER", "$( $Props.'DB_DRIVER' )" })
    }
    else
    {
        # BI
        Set-Content -Path $DEPLOYMENT_YAML_FILE -Value (get-content $DEPLOYMENT_YAML_FILE | ForEach-Object{ $_ -replace "DB_OB_REPORTING_URL", "jdbc:sqlserver://$( $Props.'DB_HOST' ):1433;databaseName=$( $Props.'DB_OB_REPORTING' );encrypt=false" })
        Set-Content -Path $DEPLOYMENT_YAML_FILE -Value (get-content $DEPLOYMENT_YAML_FILE | ForEach-Object{ $_ -replace "DB_OB_REPORTING_SUMMARIZED_URL", "jdbc:sqlserver://$( $Props.'DB_HOST' ):1433;databaseName=$( $Props.'DB_OB_REPORTING_SUMMARIZED' );encrypt=false" })
        Set-Content -Path $DEPLOYMENT_YAML_FILE -Value (get-content $DEPLOYMENT_YAML_FILE | ForEach-Object{ $_ -replace "DB_USER", "$( $Props.'DB_USER' )" })
        Set-Content -Path $DEPLOYMENT_YAML_FILE -Value (get-content $DEPLOYMENT_YAML_FILE | ForEach-Object{ $_ -replace "DB_PASS", "$( $Props.'DB_PASS' )" })
        Set-Content -Path $DEPLOYMENT_YAML_FILE -Value (get-content $DEPLOYMENT_YAML_FILE | ForEach-Object{ $_ -replace "DB_DRIVER", "$( $Props.'DB_DRIVER' )" })
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

    }
    else
    {
        Write-Output "`nAssume MSSQL/Oracle databases have already created manually"
    }
}

Function Create-Mysql-Databases
{
    mysql -u"$( $Props.'DB_USER' )" -p"$DB_MYSQL_PASS" -h"$( $Props.'DB_HOST' )" -e"DROP DATABASE IF EXISTS $( $Props.'DB_OB_REPORTING' ); CREATE DATABASE $( $Props.'DB_OB_REPORTING' ); ALTER DATABASE $( $Props.'DB_OB_REPORTING' ) CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    Write-Output "Database Created: $( $Props.'DB_OB_REPORTING' )";
    mysql -u"$( $Props.'DB_USER' )" -p"$DB_MYSQL_PASS" -h"$( $Props.'DB_HOST' )" -e"DROP DATABASE IF EXISTS $( $Props.'DB_OB_REPORTING_SUMMARIZED' ); CREATE DATABASE $( $Props.'DB_OB_REPORTING_SUMMARIZED' ); ALTER DATABASE $( $Props.'DB_OB_REPORTING_SUMMARIZED' ) CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    Write-Output "Database Created: $( $Props.'DB_OB_REPORTING_SUMMARIZED' )";
}

Write-Output "`nReplace hostnames `n"
Write-Output "================================================`n"
Set-Content -Path $DEPLOYMENT_YAML_FILE -Value (get-content $DEPLOYMENT_YAML_FILE | ForEach-Object{ $_ -replace "APIM_HOSTNAME", "$( $Props.'APIM_HOSTNAME' )" })

Write-Output "`nConfigure datasources `n"
Write-Output "================================================`n"
Configure-Datasources

Write-Output "`nCreate databases"
Write-Output "================================================`n"
Create-Databases

Write-Output "`nCopy deployment.yaml file to conf/server `n"
Write-Output "================================================`n"
Copy-Item $DEPLOYMENT_YAML_FILE $WSO2_OB_BI_HOME/conf/server/
Remove-Item $DEPLOYMENT_YAML_FILE
