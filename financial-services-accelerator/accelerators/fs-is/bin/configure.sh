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
# ./configure.sh <WSO2_IS_HOME>

source $(pwd)/../repository/conf/configure.properties
WSO2_IS_HOME=$1

# set accelerator home
cd ../
ACCELERATOR_HOME=$(pwd)
echo "Accelerator Home: ${ACCELERATOR_HOME}"

# set product home
if [ "${WSO2_IS_HOME}" == "" ]
  then
    cd ../
    WSO2_IS_HOME=$(pwd)
    echo "Product Home: ${WSO2_IS_HOME}"
fi

# validate product home
if [ ! -d "${WSO2_IS_HOME}/repository/components" ]; then
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
            # IS
            sed -i -e 's|DB_IDENTITY_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_IDENTITY}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_IS_CONFIG_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_IS_CONFIG}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_GOV_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_GOV}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER_STORE_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_USER_STORE}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_FS_STORE_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_FS_STORE}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER|'${DB_USER}'|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_PASS|'${DB_PASS}'|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_DRIVER|'${DB_DRIVER}'|g' ${DEPLOYMENT_TOML_FILE}

        else
            # IS
            sed -i -e 's|DB_IDENTITY_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_IDENTITY}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_IS_CONFIG_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_IS_CONFIG}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_GOV_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_GOV}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER_STORE_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_USER_STORE}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_FS_STORE_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_FS_STORE}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
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

            echo -e "\nCreate database tables"
            echo -e "================================================\n"
            create_mysql_database_tables;

            echo -e "\nAlter SP_METADATA table VALUE field size (temporary)"
            echo -e "=======================================================================\n"
            mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "ALTER TABLE ${DB_IDENTITY}.SP_METADATA MODIFY VALUE VARCHAR(4096)";
        else
            echo -e "\nAssume MSSQL/Oracle databases have already created manually"
            echo -e "\nUpdate idn_req_object_reference table foreign keys (temporary)"
            echo -e "=======================================================================\n"
            sed -i -e 's|FOREIGN KEY (CONSUMER_KEY_ID) REFERENCES IDN_OAUTH_CONSUMER_APPS(ID) ON DELETE CASCADE ,|FOREIGN KEY (CONSUMER_KEY_ID) REFERENCES IDN_OAUTH_CONSUMER_APPS(ID),|g' ${WSO2_APIM_HOME}/dbscripts/identity/mssql.sql
            sed -i -e 's|FOREIGN KEY (TOKEN_ID) REFERENCES IDN_OAUTH2_ACCESS_TOKEN(TOKEN_ID),|FOREIGN KEY (TOKEN_ID) REFERENCES IDN_OAUTH2_ACCESS_TOKEN(TOKEN_ID) ON DELETE CASCADE,|g' ${WSO2_APIM_HOME}/dbscripts/identity/mssql.sql

    fi
}

create_mysql_databases() {
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "DROP DATABASE IF EXISTS ${DB_IDENTITY}; CREATE DATABASE ${DB_IDENTITY};
    ALTER DATABASE ${DB_IDENTITY} CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    echo "Database Created: ${DB_IDENTITY}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "DROP DATABASE IF EXISTS ${DB_IS_CONFIG}; CREATE DATABASE ${DB_IS_CONFIG};
    ALTER DATABASE ${DB_IS_CONFIG} CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    echo "Database Created: ${DB_IS_CONFIG}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "DROP DATABASE IF EXISTS ${DB_FS_STORE}; CREATE DATABASE ${DB_FS_STORE};
    ALTER DATABASE ${DB_FS_STORE} CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    echo "Database Created: ${DB_FS_STORE}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "DROP DATABASE IF EXISTS ${DB_USER_STORE}; CREATE DATABASE ${DB_USER_STORE};
    ALTER DATABASE ${DB_USER_STORE} CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    echo "Database Created: ${DB_USER_STORE}"
};

create_mysql_database_tables() {
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -D${DB_IDENTITY} -h${DB_HOST} -e "SOURCE ${WSO2_IS_HOME}/dbscripts/identity/mysql.sql";
    echo "Database tables Created for: ${DB_APIMGT}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -D${DB_IS_CONFIG} -h${DB_HOST} -e "SOURCE ${WSO2_IS_HOME}/dbscripts/mysql.sql";
    echo "Database tables Created for: ${DB_IS_CONFIG}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -D${DB_FS_STORE} -h${DB_HOST} -e "SOURCE ${WSO2_IS_HOME}/dbscripts/financial-services/consent/mysql.sql";
    echo "Database tables Created for: ${DB_FS_STORE}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -D${DB_USER_STORE} -h${DB_HOST} -e "SOURCE ${WSO2_IS_HOME}/dbscripts/mysql.sql";
    echo "Database tables Created for: ${DB_USER_STORE}"
};

echo -e "\nReplace hostnames \n"
echo -e "================================================\n"
sed -i -e 's|IS_HOSTNAME|'${IS_HOSTNAME}'|g' ${DEPLOYMENT_TOML_FILE}
sed -i -e 's|APIM_HOSTNAME|'${APIM_HOSTNAME}'|g' ${DEPLOYMENT_TOML_FILE}

echo -e "\nReplace admin credentials \n"
echo -e "================================================\n"
sed -i -e 's|IS_ADMIN_USERNAME|'${IS_ADMIN_USERNAME}'|g' ${DEPLOYMENT_TOML_FILE}
sed -i -e 's|IS_ADMIN_PASSWORD|'${IS_ADMIN_PASSWORD}'|g' ${DEPLOYMENT_TOML_FILE}

echo -e "\nConfigure datasources \n"
echo -e "================================================\n"
configure_datasources;

echo -e "\nCreate databases"
echo -e "================================================\n"
create_databases;

echo -e "\nConfigure IS KM Connector"
echo -e "================================================\n"
#configure_iskm_connector;

echo -e "\nCopy deployment.toml file to repository/conf \n"
echo -e "================================================\n"
cp ${DEPLOYMENT_TOML_FILE} ${WSO2_IS_HOME}/repository/conf/
rm ${DEPLOYMENT_TOML_FILE}
rm -f ${DEPLOYMENT_TOML_FILE}-e
