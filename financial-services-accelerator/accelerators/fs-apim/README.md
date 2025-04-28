### **WSO2 Financial Services APIM Accelerator 4.0.0**

**Try Locally:**

Prerequisites:
1. [Download](https://wso2.com/api-manager/#) the WSO2 API Manager product 
2. Build the repository completely using the below command to create accelerator zip files.
<code>mvn clean install -P Solution</code>
3. Locate the Accelerator Zip file (wso2-fsam-accelerator-4.0.0.zip) from 
the <code>target</code> folder in this directory
4. Setup MySQL database server
5. Install Java on your local machine

Below are the simplified steps to install the WSO2 Financial Services Accelerator. For more information, 
please refer to [Quick Start Guide](https://ob.docs.wso2.com/en/latest/get-started/quick-start-guide/) or 
to [ Complete install and Setup Guide ](https://ob.docs.wso2.com/en/latest/install-and-setup/)

Steps:
1. Extract the Base product (WSO2 API Manager product) to a preferred location (<WSO2_APIM_HOME>)
2. Extract WSO2 FS APIM Accelerator (wso2-fsam-accelerator-4.0.0.zip) to WSO2_APIM_HOME 
3. Run <WSO2_FS_APIM_ACC_HOME>/bin/merge.sh. This will copy/install the Financial Services artifacts to 
the WSO2 API Manager product

```
sh /<path_to_wso2_apim_accelerator>/bin/merge.sh <path_to_wso2_apim>
```
4. Run <WSO2_FS_APIM_ACC_HOME>/bin/configure.sh. This will configure the server and create databases and  tables.

```
sh /<path_to_wso2_apim_accelerator>/bin/configure.sh <path_to_wso2_apim>
```
5. Run <WSO2_APIM_HOME>/bin/wso2server.sh to start the server.



