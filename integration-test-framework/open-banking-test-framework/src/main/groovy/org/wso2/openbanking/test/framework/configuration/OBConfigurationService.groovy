/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.test.framework.configuration


import org.wso2.openbanking.test.framework.constant.OBConfigConstants

/**
 * Class for provide configuration data to the Open banking layer
 * and to the toolkit layers
 */
class OBConfigurationService {

    //Setting up path to Test Configuration file
    File configXml = new File(this.getClass().getClassLoader().getResource("TestConfiguration.xml").getFile())
    // Get Instance of Configuration Parser
    private OBConfigParser configParser = OBConfigParser.getInstance(configXml.path)

    /**
     *  Retrieve Configuration data from Config parser
     */

    private List<Object> applicationConfig = configParser.getApplicationConfig()
    private List<Object> psuConfig = configParser.getPsuConfig()

    protected Map<String, Object> configuration = configParser.getConfigurationMap()

    /**
     * Get OB Configuration File
     * @return
     */
    File getOBXMLFile() {
        return configParser.getOBXMLFile()
    }

    void setTppNumber(int tpp) {
        configParser.setTppNumber(tpp)
    }

    int getTppNumber() {
        return configParser.getTppNumber()
    }

    void setPsuNumber(int psu) {
        configParser.setPsuNumber(psu)
    }

    int getPsuNumber() {
        return configParser.getPsuNumber()
    }

    /**
     * Return Application Configuration Data
     */

    List<Object> getApplicationConfig() {
        return applicationConfig
    }

    /**
     * Return common configuration map
     * @return
     */
    Map<String, Object> getConfigurationData() {
        return configuration
    }

    /**
     * Get Solution Version
     */
    String getCommonSolutionVersion() {
        return configuration.get(OBConfigConstants.COMMON + "." + OBConfigConstants.COMMON_SOLUTION_VERSION)
    }

    /**
     * Get API version
     * @return
     */
    String getCommonApiVersion() {
        return configuration.get(OBConfigConstants.COMMON + "." + OBConfigConstants.COMMON_API_VERSION)
    }

    /**
     * Get Access Token expire time
     * @return
     */
    String getCommonAccessTokenExpireTime() {
        return configuration.get(OBConfigConstants.COMMON + "." + OBConfigConstants.COMMON_ACCESS_TOKEN_EXP)
    }

    /**
     * Get Tenant Domain
     * @return
     */
    String getCommonTenantDomain() {
        return configuration.get(OBConfigConstants.COMMON + "." + OBConfigConstants.COMMON_TENANT_DOMAIN)
    }

    /**
     * Get Signing Algorithm
     * @return
     */
    String getCommonSigningAlgorithm() {
        return configuration.get(OBConfigConstants.COMMON + "." + OBConfigConstants.COMMON_SIGNING_ALGO)
    }

    /**
     * Get Provisioning Enabled
     * @return
     */
    boolean getProvisioningEnabled() {
        if (configuration.get(OBConfigConstants.PROVISIONING + "." + OBConfigConstants.PROVISIONING_ENABLE).equals("true")) {
            return true
        }
        return false
    }

    /**
     * Get Provision File Path
     * @return
     */
    String getProvisionFilePath() {
        return configuration.get(OBConfigConstants.PROVISIONING + "." + OBConfigConstants.PROVISIONING_FILE_PATH)
    }

    /**
     * Get Server Base URL
     */
    String getServerBaseURL() {
        return configuration.get(OBConfigConstants.SERVER + "." + OBConfigConstants.SERVER_BASE_URL)
    }

    /**
     * Get Server Gateway URL
     */
    String getServerGatewayURL() {
        return configuration.get(OBConfigConstants.SERVER + "." + OBConfigConstants.SERVER_GATEWAY_URL)
    }

    /**
     * Get Server Authorization Server URL
     */
    String getServerAuthorisationServerURL() {
        return configuration.get(OBConfigConstants.SERVER + "." + OBConfigConstants.SERVER_AUTHORIZATION_SERVER_URL)
    }

    /**
     * Get IS Server URL
     */
    String getISServerUrl() {
        return configuration.get(OBConfigConstants.SERVER + "." + OBConfigConstants.IS_SERVER_URL)
    }

    /**
     * Get APIM Server IS URL
     */
    String getApimServerUrl() {
        return configuration.get(OBConfigConstants.SERVER + "." + OBConfigConstants.APIM_SERVER_URL)
    }

    /**
     * Get Non-Regulatory Application ClientID
     */
    String getNonRegulatoryAppClientID() {
        return configuration.get(OBConfigConstants.NON_REGULATORY_APP + "." + OBConfigConstants.NON_REG_APP_CLIENT_ID)
    }

    /**
     * Get Non-Regulatory Application Client Secret
     */
    String getNonRegulatoryAppClientSecret() {
        return configuration.get(OBConfigConstants.NON_REGULATORY_APP + "." + OBConfigConstants.NON_REG_APP_CLIENT_SECRET)
    }

    /**
     * Get Non-Regulatory Application Redirect URL
     */
    String getNonRegulatoryAppRedirectURL() {
        return configuration.get(OBConfigConstants.NON_REGULATORY_APP + "." + OBConfigConstants.NON_REG_APP_REDIRECT_URL)
    }

    /**
     *  Get Users Name and Password
     *
     *  Publisher
     *  TPP
     *  CustomerCare
     *  Basic Auth
     *  Key Manager
     */
    String getUserPSUName(Integer psuIndex = getPsuNumber()) {
        if (psuIndex == null) {
            return psuConfig.get(0).get(OBConfigConstants.CREDENTIALS).(OBConfigConstants.USERS_USER_NAME)
        }
            return psuConfig.get(psuIndex).get(OBConfigConstants.CREDENTIALS).get(OBConfigConstants.USERS_USER_NAME)
        }

    String getUserPSUPWD(Integer psuIndex = getPsuNumber()) {
        if (psuIndex == null) {
            return psuConfig.get(0).get(OBConfigConstants.CREDENTIALS).(OBConfigConstants.USERS_PWD)
        }
        return psuConfig.get(psuIndex).get(OBConfigConstants.CREDENTIALS).(OBConfigConstants.USERS_PWD)
    }

    String getUserPublisherName() {
        return configuration.get(OBConfigConstants.PUBLISHER + "." + OBConfigConstants.USERS_USER_NAME)
    }

    String getUserPublisherPWD() {
        return configuration.get(OBConfigConstants.PUBLISHER + "." + OBConfigConstants.USERS_PWD)
    }

    String getUserTPPName() {
        return configuration.get(OBConfigConstants.TPP + "." + OBConfigConstants.USERS_USER_NAME)
    }

    String getUserTPPPWD() {
        return configuration.get(OBConfigConstants.TPP + "." + OBConfigConstants.USERS_PWD)
    }

    String getUserCustomerCareName() {
        return configuration.get(OBConfigConstants.CUSTOMER_CARE + "." + OBConfigConstants.USERS_USER_NAME)
    }

    String getUserCustomerCarePWD() {
        return configuration.get(OBConfigConstants.CUSTOMER_CARE + "." + OBConfigConstants.USERS_PWD)
    }

    String getUserBasicAuthName() {
        return configuration.get(OBConfigConstants.BASIC_AUTH + "." + OBConfigConstants.USERS_USER_NAME)
    }

    String getUserBasicAuthPWD() {
        return configuration.get(OBConfigConstants.BASIC_AUTH + "." + OBConfigConstants.USERS_PWD)
    }

    String getUserKeyManagerAdminName() {
        return configuration.get(OBConfigConstants.KEY_MANAGER + "." + OBConfigConstants.USERS_USER_NAME)
    }

    String getUserKeyManagerAdminPWD() {
        return configuration.get(OBConfigConstants.KEY_MANAGER + "." + OBConfigConstants.USERS_PWD)
    }

/**
 * Get Browser Automation  Browser Preference
 */
    String getBrowserPreference() {
        return configuration.get(OBConfigConstants.BROWSER_AUTOMATION + "." + OBConfigConstants.BROWSER_AUTOMATION_PREFERENCE)
    }

    /**
     * Get Browser Automation  Headless Enabled
     */
    boolean getBrowserHeadlessEnabled() {
        if (configuration.get(OBConfigConstants.BROWSER_AUTOMATION + "." + OBConfigConstants.BROWSER_AUTOMATION_HEADLESS_ENABLE)
                .equals("true")) {
            return true
        }
        return false
    }

    /**
     * Get Browser Automation  WebDriver Location
     */
    String getBrowserWebDriverLocation() {
        return configuration.get(OBConfigConstants.BROWSER_AUTOMATION + "." + OBConfigConstants.BROWSER_AUTOMATION_WEB_DRIVER_LOCATION)
    }

    /**
     * Get Consent API Audience Value
     */
    String getConsentAudienceValue() {
        return configuration.get(OBConfigConstants.CONSENT_API + "." + OBConfigConstants.CONSENT_API_AUDIENCE)
    }

    /**
     * Get Consent API Revocation Audience Value
     */
    String getConsentRevocationAudValue() {
        return configuration.get(OBConfigConstants.CONSENT_API + "." + OBConfigConstants.CONSENT_API_REVOKE_AUDIENCE)
    }

    /**
     * Get Internal API Context Consent Management
     */
    String getInternalApiConsentMgt() {
        return configuration.get(OBConfigConstants.INTERNAL_API + "." + OBConfigConstants.INTERNAL_API_CONSENT_MGT)
    }

    /**
     *  Get Application OBKeyStore Location
     * @param appIndex
     */
    String getAppKeyStoreLocation(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_KEYSTORE)
                    .get(OBConfigConstants.APPLICATION_KEYSTORE_LOCATION)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_KEYSTORE).get(OBConfigConstants.APPLICATION_KEYSTORE_LOCATION)
    }

    /**
     *  Get Application OBKeyStore Alias
     * @param appIndex
     */
    String getAppKeyStoreAlias(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_KEYSTORE)
                    .get(OBConfigConstants.APPLICATION_KEYSTORE_ALIAS)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_KEYSTORE)
                .get(OBConfigConstants.APPLICATION_KEYSTORE_ALIAS)
    }

    /**
     *  Get Application OBKeyStore Password
     * @param appIndex
     */
    String getAppKeyStorePWD(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_KEYSTORE)
                    .get(OBConfigConstants.APPLICATION_KEYSTORE_PWD)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_KEYSTORE)
                .get(OBConfigConstants.APPLICATION_KEYSTORE_PWD)
    }

    /**
     *  Get Application OBKeyStore DomainName
     * @param appIndex
     */
    String getAppKeyStoreDomainName(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_KEYSTORE)
                    .get(OBConfigConstants.APPLICATION_KEYSTORE_DOMAIN_NAME)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_KEYSTORE)
                .get(OBConfigConstants.APPLICATION_KEYSTORE_DOMAIN_NAME)
    }

    /**
     *  Get Application OBKeyStore SigningKid
     * @param appIndex
     */
    String getAppKeyStoreSigningKid(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_KEYSTORE)
                    .get(OBConfigConstants.APPLICATION_KEYSTORE_SIGNING_KID)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_KEYSTORE)
                .get(OBConfigConstants.APPLICATION_KEYSTORE_SIGNING_KID)
    }

    /**
     *  Get Application Transport MLTS Enabled
     * @param appIndex
     */
    String getAppTransportMLTSEnable(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_TRANSPORT)
                    .get(OBConfigConstants.APPLICATION_TRANSPORT_MLTS)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_TRANSPORT)
                .get(OBConfigConstants.APPLICATION_TRANSPORT_MLTS)
    }


    /**
     *  Get Application Transport OBKeyStore Location
     * @param appIndex
     */
    String getAppTransportKeyStoreLocation(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_TRANSPORT)
                    .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE)
                    .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE_LOCATION)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_TRANSPORT)
                .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE)
                .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE_LOCATION)
    }

    /**
     *  Get Application Transport OBKeyStore Type
     * @param appIndex
     */
    String getAppTransportKeyStoreType(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_TRANSPORT)
                    .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE)
                    .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE_TYPE)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_TRANSPORT)
                .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE)
                .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE_TYPE)
    }

    /**
     *  Get Application Transport OBKeyStore Password
     * @param appIndex
     */
    String getAppTransportKeyStorePWD(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_TRANSPORT)
                    .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE)
                    .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE_PWD)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_TRANSPORT)
                .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE)
                .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE_PWD)
    }

    /**
     *  Get Application DCR SSAPath
     * @param appIndex
     */
    String getAppDCRSSAPath(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_DCR)
                    .get(OBConfigConstants.APPLICATION_DCR_SSA_PATH)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_DCR)
                .get(OBConfigConstants.APPLICATION_DCR_SSA_PATH)
    }

    /**
     *  Get Application DCR Self-Signed SSAPath
     * @param appIndex
     */
    String getAppDCRSelfSignedSSAPath(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_DCR)
                    .get(OBConfigConstants.APPLICATION_DCR_SELF_SIGNED_SSA)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_DCR)
                .get(OBConfigConstants.APPLICATION_DCR_SELF_SIGNED_SSA)
    }

    /**
     *  Get Application DCR Software Id
     * @param appIndex
     */
    String getAppDCRSoftwareId(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_DCR)
                    .get(OBConfigConstants.APPLICATION_DCR_SOFTWARE_ID)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_DCR).get(OBConfigConstants.APPLICATION_DCR_SOFTWARE_ID)
    }

    /**
     *  Get Application DCR Redirect Uri
     * @param appIndex
     */
    String getAppDCRRedirectUri(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_DCR)
                    .get(OBConfigConstants.APPLICATION_DCR_REDIRECT_URL)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_DCR)
                .get(OBConfigConstants.APPLICATION_DCR_REDIRECT_URL)
    }

    /**
     *  Get Application DCR AlternateRedirectUri
     * @param appIndex
     */
    String getAppDCRAlternateRedirectUri(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_DCR)
                    .get(OBConfigConstants.APPLICATION_DCR_ALT_REDIRECT_URL)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_DCR)
                .get(OBConfigConstants.APPLICATION_DCR_ALT_REDIRECT_URL)
    }

    /**
     *  Get Application DCR APIVersion
     * @param appIndex
     */
    String getAppDCRAPIVersion(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_DCR)
                    .get(OBConfigConstants.APPLICATION_DCR_API_VERSION)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_DCR)
                .get(OBConfigConstants.APPLICATION_DCR_API_VERSION)
    }

    /**
     *  Get Application Information :ClientID
     * @param appIndex
     */
    String getAppInfoClientID(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_APP_INFO)
                    .get(OBConfigConstants.APPLICATION_APP_INFO_CLIENT_ID)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_APP_INFO)
                .get(OBConfigConstants.APPLICATION_APP_INFO_CLIENT_ID)
    }

    /**
     *  Get Application Information : ClientSecret
     * @param appIndex
     */
    String getAppInfoClientSecret(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_APP_INFO)
                    .get(OBConfigConstants.APPLICATION_APP_INFO_CLIENT_SECRET)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_APP_INFO)
                .get(OBConfigConstants.APPLICATION_APP_INFO_CLIENT_SECRET)
    }

    /**
     *  Get Application Information : RedirectURL
     * @param appIndex
     */
    String getAppInfoRedirectURL(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_APP_INFO)
                    .get(OBConfigConstants.APPLICATION_APP_INFO_REDIRECT_URL)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_APP_INFO)
                .get(OBConfigConstants.APPLICATION_APP_INFO_REDIRECT_URL)
    }

    /**
     * Get Transport Truststore Location
     * @return
     */
    String getTransportTruststoreLocation() {
        return configuration.get(
                OBConfigConstants.TRANSPORT + "." + OBConfigConstants.TRANSPORT_TRUSTSTORE + "."
                        + OBConfigConstants.TRANSPORT_TRUSTSTORE_LOCATION)
    }

    /**
     * Get Transport Truststore Type
     * @return
     */
    String getTransportTruststoreType() {
        return configuration.get(
                OBConfigConstants.TRANSPORT + "." + OBConfigConstants.TRANSPORT_TRUSTSTORE + "."
                        + OBConfigConstants.TRANSPORT_TRUSTSTORE_TYPE)
    }

    /**
     * Get Transport Truststore Password
     * @return
     */
    String getTransportTruststorePWD() {
        return configuration.get(
                OBConfigConstants.TRANSPORT + "." + OBConfigConstants.TRANSPORT_TRUSTSTORE + "."
                        + OBConfigConstants.TRANSPORT_TRUSTSTORE_PWD)
    }

    /**
     * Read Test Artifact Location.
     *
     * @return test artifact folder location
     */
     String getTestArtifactLocation() {
         return configuration.get(OBConfigConstants.COMMON + "." + OBConfigConstants.COMMON_TEST_ARTIFACT_LOCATION)
    }

    /**
     *  Get Transport OBKeyStore Alias
     * @param appIndex
     */
    String getTransportKeystoreAlias(Integer appIndex = null) {
        if (appIndex == null) {
            return applicationConfig.get(configParser.getTppNumber()).get(OBConfigConstants.APPLICATION_TRANSPORT)
                    .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE)
                    .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE_ALIAS)
        }
        return applicationConfig.get(appIndex).get(OBConfigConstants.APPLICATION_TRANSPORT)
                .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE)
                .get(OBConfigConstants.APPLICATION_TRANSPORT_KEYSTORE_ALIAS)
    }

}

