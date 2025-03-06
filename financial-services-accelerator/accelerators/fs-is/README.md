### **WSO2 Financial Services IS Accelerator 5.0.0**

**Try Locally:**

Prerequisites:
1. Download WSO2 Identity Server product 
2. Build the repository completely using the below command to create accelerator zip files.
   <code>mvn clean install -P Solution</code>
3. Locate the Accelerator Zip file (wso2-fsiam-accelerator-4.0.0.zip) from
   the <code>target</code> folder in this directory
4. Setup MySQL database server
5. Install Java on your local machine

Below are the simplified steps to install the WSO2 Financial Services Accelerator. For more information,
please refer to [Quick Start Guide](https://ob.docs.wso2.com/en/latest/get-started/quick-start-guide/) or
to [ Complete install and Setup Guide ](https://ob.docs.wso2.com/en/latest/install-and-setup/)


Steps:
1. Extract the Base product (WSO2 Identity Server product) to a preferred location (<WSO2_IS_HOME>)
2. Extract WSO2 FS IAM Accelerator (wso2-fsiam-accelerator-4.0.0.zip) to WSO2_IS_HOME
3. Run <WSO2_FS_IS_ACC_HOME>/bin/merge.sh. This will copy the artifacts to the WSO2 IS
4. Run <WSO2_FS_IS_ACC_HOME>/bin/configure.sh. This will configure the server and create databases and  tables.
5. Run <WSO2_IS_HOME>/bin/wso2server.sh to start the server

