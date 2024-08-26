#!/bin/bash
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

# command to execute
# ./merge.sh <WSO2_OB_APIM_HOME>

source $(pwd)/../repository/conf/configure.properties
WSO2_OB_APIM_HOME=$1

# set accelerator home
cd ../
ACCELERATOR_HOME=$(pwd)
echo "Accelerator Home: ${ACCELERATOR_HOME}"

# set product home
if [ "${WSO2_OB_APIM_HOME}" == "" ]
  then
    cd ../
    WSO2_OB_APIM_HOME=$(pwd)
    echo "Product Home: ${WSO2_OB_APIM_HOME}"
fi

# validate product home
if [ ! -d "${WSO2_OB_APIM_HOME}/repository/components" ]; then
  echo -e "\n\aERROR:specified product path is not a valid carbon product path\n";
  exit 2;
else
  echo -e "\nValid carbon product path.\n";
fi

# read deployment.toml file
DEPLOYMENT_TOML_FILE=${ACCELERATOR_HOME}/repository/resources/deployment.toml;
cp ${ACCELERATOR_HOME}/${PRODUCT_CONF_PATH} ${DEPLOYMENT_TOML_FILE};

configure_datasources() {
    if [ "${DB_TYPE}" == "mysql" ]
        then
            # APIM
            sed -i -e 's|DB_APIMGT_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_APIMGT}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_AM_CONFIG_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_AM_CONFIG}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_GOV_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_GOV}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER_STORE_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_USER_STORE}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER|'${DB_USER}'|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_PASS|'${DB_PASS}'|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_DRIVER|'${DB_DRIVER}'|g' ${DEPLOYMENT_TOML_FILE}

        else
            # IS
            sed -i -e 's|DB_APIMGT_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_APIMGT}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_AM_CONFIG_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_AM_CONFIG}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_GOV_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_GOV}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER_STORE_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_USER_STORE}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER|'${DB_USER}'|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_PASS|'${DB_PASS}'|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_DRIVER|'${DB_DRIVER}'|g' ${DEPLOYMENT_TOML_FILE}
    fi
}

create_databases() {
    if [ "${DB_TYPE}" == "mysql" ]
        then
            if [ "${DB_PASS}" == "" ]
                then
                DB_MYSQL_PASS=""
            else
                DB_MYSQL_PASS="-p${DB_PASS}"
            fi

            echo -e "\nCreating MySQL databases"
            echo -e "================================================\n"
            create_mysql_databases;

            echo -e "\nUpdate am_application_registration table input field size (temporary)"
            echo -e "=======================================================================\n"
            sed -i -e 's|INPUTS VARCHAR(1000)|INPUTS VARCHAR(7500)|g' ${WSO2_OB_APIM_HOME}/dbscripts/apimgt/mysql.sql


            echo -e "\nCreate database tables"
            echo -e "================================================\n"
            create_mysql_database_tables;

            echo -e "\nAlter SP_METADATA table VALUE field size (temporary)"
            echo -e "=======================================================================\n"
            mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "ALTER TABLE ${DB_APIMGT}.SP_METADATA MODIFY VALUE VARCHAR(4096)";
        else
            echo -e "\nAssume MSSQL/Oracle databases have already created manually"

            echo -e "\nUpdate idn_req_object_reference table foreign keys (temporary)"
            echo -e "=======================================================================\n"
            sed -i -e 's|FOREIGN KEY (CONSUMER_KEY_ID) REFERENCES IDN_OAUTH_CONSUMER_APPS(ID) ON DELETE CASCADE ,|FOREIGN KEY (CONSUMER_KEY_ID) REFERENCES IDN_OAUTH_CONSUMER_APPS(ID),|g' ${WSO2_OB_APIM_HOME}/dbscripts/apimgt/mssql.sql
            sed -i -e 's|FOREIGN KEY (TOKEN_ID) REFERENCES IDN_OAUTH2_ACCESS_TOKEN(TOKEN_ID),|FOREIGN KEY (TOKEN_ID) REFERENCES IDN_OAUTH2_ACCESS_TOKEN(TOKEN_ID) ON DELETE CASCADE,|g' ${WSO2_OB_APIM_HOME}/dbscripts/apimgt/mssql.sql

    fi
}

create_mysql_databases() {
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "DROP DATABASE IF EXISTS ${DB_APIMGT}; CREATE DATABASE ${DB_APIMGT};
    ALTER DATABASE ${DB_APIMGT} CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    echo "Database Created: ${DB_APIMGT}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "DROP DATABASE IF EXISTS ${DB_AM_CONFIG}; CREATE DATABASE ${DB_AM_CONFIG};
    ALTER DATABASE ${DB_AM_CONFIG} CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    echo "Database Created: ${DB_AM_CONFIG}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "DROP DATABASE IF EXISTS ${DB_GOV}; CREATE DATABASE ${DB_GOV};
    ALTER DATABASE ${DB_GOV} CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    echo "Database Created: ${DB_GOV}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "DROP DATABASE IF EXISTS ${DB_USER_STORE}; CREATE DATABASE ${DB_USER_STORE};
    ALTER DATABASE ${DB_USER_STORE} CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    echo "Database Created: ${DB_USER_STORE}"
};

create_mysql_database_tables() {
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -D${DB_APIMGT} -h${DB_HOST} -e "SOURCE ${WSO2_OB_APIM_HOME}/dbscripts/apimgt/mysql.sql";
    echo "Database tables Created for: ${DB_APIMGT}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -D${DB_AM_CONFIG} -h${DB_HOST} -e "SOURCE ${WSO2_OB_APIM_HOME}/dbscripts/mysql.sql";
    echo "Database tables Created for: ${DB_AM_CONFIG}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -D${DB_GOV} -h${DB_HOST} -e "SOURCE ${WSO2_OB_APIM_HOME}/dbscripts/mysql.sql";
    echo "Database tables Created for: ${DB_GOV}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -D${DB_USER_STORE} -h${DB_HOST} -e "SOURCE ${WSO2_OB_APIM_HOME}/dbscripts/mysql.sql";
    echo "Database tables Created for: ${DB_USER_STORE}"
};

add_json_fault_sequence() {
  sed -i -e 's|</sequence>|\t<sequence key="jsonConverter"/>\n</sequence>|g' ${WSO2_OB_APIM_HOME}/repository/deployment/server/synapse-configs/default/sequences/_cors_request_handler_.xml
}

echo -e "\nReplace hostnames \n"
echo -e "================================================\n"
sed -i -e 's|APIM_HOSTNAME|'${APIM_HOSTNAME}'|g' ${DEPLOYMENT_TOML_FILE}
sed -i -e 's|IS_HOSTNAME|'${IS_HOSTNAME}'|g' ${DEPLOYMENT_TOML_FILE}
sed -i -e 's|BI_HOSTNAME|'${BI_HOSTNAME}'|g' ${DEPLOYMENT_TOML_FILE}

echo -e "\nConfigure datasources \n"
echo -e "================================================\n"
configure_datasources;

echo -e "\nCreate databases"
echo -e "================================================\n"
create_databases;

echo -e "\nCopy deployment.toml file to repository/conf \n"
echo -e "================================================\n"
cp ${DEPLOYMENT_TOML_FILE} ${WSO2_OB_APIM_HOME}/repository/conf/
rm ${DEPLOYMENT_TOML_FILE}
rm -f ${DEPLOYMENT_TOML_FILE}-e

echo -e "\nAdd json converter for fault sequences \n"
echo -e "================================================\n"
add_json_fault_sequence
