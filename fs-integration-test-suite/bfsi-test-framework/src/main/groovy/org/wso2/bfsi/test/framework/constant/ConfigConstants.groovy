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

package org.wso2.bfsi.test.framework.constant

/**
 * Class for keep Open-baning constants
 */
class ConfigConstants {

    public static final String OB_CONFIG_FILE_LOCATION =
            "/Module_Path/open-banking-test-framework/src/main/resources/OBTestConfig.xml";
    public static final String TEST_CONFIG_QNAME = "";

    //  Common config constants
    public static final String COMMON = "Common";
    public static final String COMMON_SOLUTION_VERSION = "SolutionVersion";
    public static final String COMMON_API_VERSION = "ApiVersion";
    public static final String COMMON_ACCESS_TOKEN_EXP = "AccessTokenExpireTime";
    public static final String COMMON_TENANT_DOMAIN = "TenantDomain";
    public static final String COMMON_SIGNING_ALGO = "SigningAlgorithm";
    public static final String COMMON_TEST_ARTIFACT_LOCATION = "TestArtifactLocation";

    // Server config constants
    public static final String SERVER = "Server";
    public static final String SERVER_BASE_URL = "BaseURL";
    public static final String SERVER_GATEWAY_URL = "GatewayURL";
    public static final String SERVER_AUTHORIZATION_SERVER_URL = "AuthorisationServerURL";
    public static final String IS_SERVER_URL = "ISServerUrl";
    public static final String APIM_SERVER_URL = "APIMServerUrl";

    // Provisioning config constants
    public static final String PROVISIONING = "Provisioning";
    public static final String PROVISIONING_ENABLE = "Enabled";
    public static final String PROVISIONING_FILE_PATH = "ProvisionFilePath";

    // Application config constants
    public static final String APPLICATION_LIST_CONFIG = "ApplicationConfigList";
    public static final String APPLICATION_CONFIG = "AppConfig";
    public static final String APPLICATION_KEYSTORE = "KeyStore";
    public static final String APPLICATION_KEYSTORE_LOCATION = "Location";
    public static final String APPLICATION_KEYSTORE_ALIAS = "Alias";
    public static final String APPLICATION_KEYSTORE_PWD = "Password";
    public static final String APPLICATION_KEYSTORE_DOMAIN_NAME = "DomainName";
    public static final String APPLICATION_KEYSTORE_SIGNING_KID = "SigningKid";
    public static final String APPLICATION_TRANSPORT = "Transport";
    public static final String APPLICATION_TRANSPORT_MLTS = "MTLSEnabled";
    public static final String APPLICATION_TRANSPORT_KEYSTORE = "KeyStore";
    public static final String APPLICATION_TRANSPORT_KEYSTORE_LOCATION = "Location";
    public static final String APPLICATION_TRANSPORT_KEYSTORE_TYPE = "Type";
    public static final String APPLICATION_TRANSPORT_KEYSTORE_PWD = "Password";
    public static final String APPLICATION_DCR = "DCR";
    public static final String APPLICATION_DCR_SSA_PATH = "SSAPath";
    public static final String APPLICATION_DCR_SELF_SIGNED_SSA = "SelfSignedSSAPath";
    public static final String APPLICATION_DCR_SOFTWARE_ID = "SoftwareId";
    public static final String APPLICATION_DCR_REDIRECT_URL = "RedirectUri";
    public static final String APPLICATION_DCR_ALT_REDIRECT_URL = "AlternateRedirectUri";
    public static final String APPLICATION_DCR_API_VERSION = "DCRAPIVersion";
    public static final String APPLICATION_APP_INFO = "Application";
    public static final String APPLICATION_APP_INFO_CLIENT_ID = "ClientID";
    public static final String APPLICATION_APP_INFO_CLIENT_SECRET = "ClientSecret";
    public static final String APPLICATION_APP_INFO_REDIRECT_URL = "RedirectURL";
    public static final String APPLICATION_TRANSPORT_KEYSTORE_ALIAS = "Alias";

    // Transport constants
    public static final String TRANSPORT = "Transport"
    public static final String TRANSPORT_TRUSTSTORE = "Truststore"
    public static final String TRANSPORT_TRUSTSTORE_LOCATION = "Location"
    public static final String TRANSPORT_TRUSTSTORE_TYPE = "Type"
    public static final String TRANSPORT_TRUSTSTORE_PWD = "Password"

    // NonRegulatoryApplication config constants
    public static final String NON_REGULATORY_APP = "NonRegulatoryApplication";
    public static final String NON_REG_APP_CLIENT_ID = "ClientID";
    public static final String NON_REG_APP_CLIENT_SECRET = "ClientSecret";
    public static final String NON_REG_APP_REDIRECT_URL = "RedirectURL";

    // users and passwords config constants
    public static final String USERS_USER_NAME = "User";
    public static final String USERS_PWD = "Password";
    public static final String PSU_INFO = "PSUInfo";
    public static final String CREDENTIALS = "Credentials";
    public static final String PUBLISHER = "PublisherInfo";
    public static final String TPP = "TPPInfo";
    public static final String CUSTOMER_CARE = "CustomerCareInfo";
    public static final String BASIC_AUTH = "BasicAuthInfo";
    public static final String KEY_MANAGER = "KeyManagerAdmin";
    public static final String PSU_LIST_CONFIG = "PSUList";

    // Browser automation config constants
    public static final String BROWSER_AUTOMATION = "BrowserAutomation";
    public static final String BROWSER_AUTOMATION_PREFERENCE = "BrowserPreference";
    public static final String BROWSER_AUTOMATION_HEADLESS_ENABLE = "HeadlessEnabled";
    public static final String BROWSER_AUTOMATION_WEB_DRIVER_LOCATION = "WebDriverLocation";

    // Consent API config constants
    public static final String CONSENT_API = "ConsentApi";
    public static final String CONSENT_API_AUDIENCE = "AudienceValue";
    public static final String CONSENT_API_REVOKE_AUDIENCE = "RevocationAudienceValue";

    // Internal API config constants
    public static final String INTERNAL_API = "InternalApiContext";
    public static final String INTERNAL_API_CONSENT_MGT = "Consent-Mgt";
}

