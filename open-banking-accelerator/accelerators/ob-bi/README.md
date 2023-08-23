### **WSO2 Open Banking BI Accelerator 3.0.0**

**Try Locally:**

Prerequisites:
1. Download WSO2 SI product 
2. Build the repository completely using the below command to create accelerator zip files.
   <code>mvn clean install -PSolution</code>
3. Locate the Accelerator Zip file (wso2-obbi-accelerator-3.0.0.zip) from
      the <code>target</code> folder in this directory
3. Setup MySQL database server
4. Install Java on your local machine

Below are the simplified steps to install the WSO2 Open Banking Accelerator. For more information,
please refer to [Quick Start Guide](https://ob.docs.wso2.com/en/latest/get-started/quick-start-guide/) or
to [ Complete install and Setup Guide ](https://ob.docs.wso2.com/en/latest/install-and-setup/)

Steps:
1. Extract the Base product (WSO2 Streaming Integrator product) to a preferred location (<WSO2_BI_HOME>)
2. Extract WSO2 OB BI Accelerator (wso2-obbi-accelerator-3.0.0.zip) to WSO2_BI_HOME 
3. Run <WSO2_OB_BI_ACC_HOME>/bin/merge.sh. This will copy the artifacts to the WSO2 SI
4. Run <WSO2_OB_BI_ACC_HOME>/bin/configure.sh. This will configure the server and create databases.
5. Run <WSO2_BI_HOME>/bin/server.sh to start the server
