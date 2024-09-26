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
# ./configure.sh <WSO2_OB_IS_HOME>

source $(pwd)/../repository/conf/configure.properties
WSO2_OB_IS_HOME=$1

# set accelerator home
cd ../
ACCELERATOR_HOME=$(pwd)
echo "Accelerator Home: ${ACCELERATOR_HOME}"

# set product home
if [ "${WSO2_OB_IS_HOME}" == "" ]
  then
    cd ../
    WSO2_OB_IS_HOME=$(pwd)
    echo "Product Home: ${WSO2_OB_IS_HOME}"
fi

# validate product home
if [ ! -d "${WSO2_OB_IS_HOME}/repository/components" ]; then
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
            sed -i -e 's|DB_APIMGT_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_APIMGT}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_IS_CONFIG_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_IS_CONFIG}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_GOV_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_GOV}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER_STORE_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_USER_STORE}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_OB_STORE_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_OPEN_BANKING_STORE}'?autoReconnect=true\&amp;useSSL=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER|'${DB_USER}'|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_PASS|'${DB_PASS}'|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_DRIVER|'${DB_DRIVER}'|g' ${DEPLOYMENT_TOML_FILE}

        else
            # IS
            sed -i -e 's|DB_APIMGT_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_APIMGT}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_IS_CONFIG_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_IS_CONFIG}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_GOV_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_GOV}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_USER_STORE_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_USER_STORE}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
            sed -i -e 's|DB_OB_STORE_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_OPEN_BANKING_STORE}';encrypt=false|g' ${DEPLOYMENT_TOML_FILE}
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
        else
            echo -e "\nAssume MSSQL/Oracle databases have already created manually"
    fi
}

create_mysql_databases() {
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "DROP DATABASE IF EXISTS ${DB_IS_CONFIG}; CREATE DATABASE ${DB_IS_CONFIG};
    ALTER DATABASE ${DB_IS_CONFIG} CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    echo "Database Created: ${DB_IS_CONFIG}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "DROP DATABASE IF EXISTS ${DB_OPEN_BANKING_STORE}; CREATE DATABASE ${DB_OPEN_BANKING_STORE};
    ALTER DATABASE ${DB_OPEN_BANKING_STORE} CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    echo "Database Created: ${DB_OPEN_BANKING_STORE}"
};

create_mysql_database_tables() {
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -D${DB_IS_CONFIG} -h${DB_HOST} -e "SOURCE ${WSO2_OB_IS_HOME}/dbscripts/mysql.sql";
    echo "Database tables Created for: ${DB_IS_CONFIG}"
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -D${DB_OPEN_BANKING_STORE} -h${DB_HOST} -e "SOURCE ${WSO2_OB_IS_HOME}/dbscripts/open-banking/consent/mysql.sql";
    echo "Database tables Created for: ${DB_OPEN_BANKING_STORE}"
};

configure_iskm_connector() {
    wget https://apim.docs.wso2.com/en/3.2.0/assets/attachments/administer/${ISKM_CONNECTOR}.zip
    unzip "${ISKM_CONNECTOR}.zip"
    cp ${ISKM_CONNECTOR_FOLDER}/dropins/* ${WSO2_OB_IS_HOME}/repository/components/dropins/
    cp ${ISKM_CONNECTOR_FOLDER}/webapps/* ${WSO2_OB_IS_HOME}/repository/deployment/server/webapps
};

echo -e "\nReplace hostnames \n"
echo -e "================================================\n"
sed -i -e 's|IS_HOSTNAME|'${IS_HOSTNAME}'|g' ${DEPLOYMENT_TOML_FILE}
sed -i -e 's|APIM_HOSTNAME|'${APIM_HOSTNAME}'|g' ${DEPLOYMENT_TOML_FILE}
sed -i -e 's|BI_HOSTNAME|'${BI_HOSTNAME}'|g' ${DEPLOYMENT_TOML_FILE}


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
cp ${DEPLOYMENT_TOML_FILE} ${WSO2_OB_IS_HOME}/repository/conf/
rm ${DEPLOYMENT_TOML_FILE}
rm -f ${DEPLOYMENT_TOML_FILE}-e
