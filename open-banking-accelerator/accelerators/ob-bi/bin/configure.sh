#!/bin/bash
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
# ./configure.sh <WSO2_OB_BI_HOME>

source $(pwd)/../repository/conf/configure.properties
WSO2_OB_BI_HOME=$1

# set accelerator home
cd ../
ACCELERATOR_HOME=$(pwd)
echo "Accelerator Home: ${ACCELERATOR_HOME}"

# set product home
if [ "${WSO2_OB_BI_HOME}" == "" ]
  then
    cd ../
    WSO2_OB_BI_HOME=$(pwd)
    echo "Product home is: ${WSO2_OB_BI_HOME}"
fi

# validate product home
if [ ! -d "${WSO2_OB_BI_HOME}/deployment/siddhi-files" ]; then
  echo -e "\n\aERROR:specified product path is not a valid carbon product path\n";
  exit 2;
else
  echo -e "\nValid carbon product path.\n";
fi

# read deployment.toml file
DEPLOYMENT_YAML_FILE=${ACCELERATOR_HOME}/repository/resources/deployment.yaml;
cp ${ACCELERATOR_HOME}/${PRODUCT_CONF_PATH} ${DEPLOYMENT_YAML_FILE};


configure_datasources() {
    if [ "${DB_TYPE}" == "mysql" ]
        then
            # BI
            sed -i -e 's|DB_OB_REPORTING_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_OB_REPORTING}'?autoReconnect=true\&useSSL=false|g' ${DEPLOYMENT_YAML_FILE}
            sed -i -e 's|DB_OB_REPORTING_SUMMARIZED_URL|jdbc:mysql://'${DB_HOST}':3306/'${DB_OB_REPORTING_SUMMARIZED}'?autoReconnect=true\&useSSL=false|g' ${DEPLOYMENT_YAML_FILE}
            sed -i -e 's|DB_USER|'${DB_USER}'|g' ${DEPLOYMENT_YAML_FILE}
            sed -i -e 's|DB_PASS|'${DB_PASS}'|g' ${DEPLOYMENT_YAML_FILE}
            sed -i -e 's|DB_DRIVER|'${DB_DRIVER}'|g' ${DEPLOYMENT_YAML_FILE}

        else
            # BI
            sed -i -e 's|DB_OB_REPORTING_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_OB_REPORTING}';encrypt=false|g' ${DEPLOYMENT_YAML_FILE}
            sed -i -e 's|DB_OB_REPORTING_SUMMARIZED_URL|jdbc:sqlserver://'${DB_HOST}':1433;databaseName='${DB_OB_REPORTING_SUMMARIZED}';encrypt=false|g' ${DEPLOYMENT_YAML_FILE}
            sed -i -e 's|DB_USER|'${DB_USER}'|g' ${DEPLOYMENT_YAML_FILE}
            sed -i -e 's|DB_PASS|'${DB_PASS}'|g' ${DEPLOYMENT_YAML_FILE}
            sed -i -e 's|DB_DRIVER|'${DB_DRIVER}'|g' ${DEPLOYMENT_YAML_FILE}
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

        else
            echo -e "\nAssume MSSQL/Oracle databases have already created manually"
    fi
}

create_mysql_databases() {
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "DROP DATABASE IF EXISTS ${DB_OB_REPORTING}; CREATE DATABASE ${DB_OB_REPORTING};
    ALTER DATABASE ${DB_OB_REPORTING} CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    echo "Database Created: ${DB_OB_REPORTING}";
    mysql -u${DB_USER} ${DB_MYSQL_PASS} -h${DB_HOST} -e "DROP DATABASE IF EXISTS ${DB_OB_REPORTING_SUMMARIZED}; CREATE DATABASE ${DB_OB_REPORTING_SUMMARIZED};
    ALTER DATABASE ${DB_OB_REPORTING_SUMMARIZED} CHARACTER SET latin1 COLLATE latin1_swedish_ci";
    echo "Database Created: ${DB_OB_REPORTING_SUMMARIZED}";
};

echo -e "\nReplace hostnames \n"
echo -e "================================================\n"
sed -i -e 's|APIM_HOSTNAME|'${APIM_HOSTNAME}'|g' ${DEPLOYMENT_YAML_FILE}

echo -e "\nConfigure datasources \n"
echo -e "================================================\n"
configure_datasources;

echo -e "\nCreate databases"
echo -e "================================================\n"
create_databases;

echo -e "\nCopy deployment.yaml file to conf/server \n"
echo -e "================================================\n"
cp ${DEPLOYMENT_YAML_FILE} ${WSO2_OB_BI_HOME}/conf/server/
rm ${DEPLOYMENT_YAML_FILE}
rm -f ${DEPLOYMENT_YAML_FILE}-e
