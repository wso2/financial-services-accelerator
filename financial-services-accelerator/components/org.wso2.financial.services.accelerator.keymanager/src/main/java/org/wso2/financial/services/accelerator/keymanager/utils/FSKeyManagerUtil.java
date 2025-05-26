/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.keymanager.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.util.CertificateUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.keymanager.FSKeyManagerExtensionInterface;
import org.wso2.financial.services.accelerator.keymanager.internal.KeyManagerDataHolder;

import java.lang.reflect.InvocationTargetException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class for FS key manager.
 */
public class FSKeyManagerUtil {

    private static final Log log = LogFactory.getLog(FSKeyManagerUtil.class);

    /**
     * Obtain FS Key Manager Extension Impl class from config.
     *
     * @return FSKeyManagerExtensionInterface
     */
    public static FSKeyManagerExtensionInterface getKeyManagerExtensionImpl() throws APIManagementException {
        FSKeyManagerExtensionInterface keyManagerExtensionImpl;
        try {
            String keyManagerExtensionImplName = FinancialServicesConfigParser.getInstance()
                    .getKeyManagerExtensionImpl();
            if (!StringUtils.isEmpty(keyManagerExtensionImplName)) {
                keyManagerExtensionImpl = (FSKeyManagerExtensionInterface)
                        Class.forName(keyManagerExtensionImplName).getDeclaredConstructor().newInstance();
                return keyManagerExtensionImpl;
            } else {
                return null;
            }

        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new APIManagementException("Failed to tain FS Key Manager Extension Impl instance", e);
        }
    }

    /**
     * Extract values for additional properties from input.
     *
     * @param oauthAppRequest OAuthAppRequest object
     * @return Additional Property Map
     * @throws APIManagementException
     */
    public static HashMap<String, String> getValuesForAdditionalProperties(OAuthAppRequest oauthAppRequest)
            throws APIManagementException {
        // Get additional properties defined in the config
        Map<String, Map<String, String>> keyManagerAdditionalProperties = FinancialServicesConfigParser.getInstance()
                .getKeyManagerAdditionalProperties();
        HashMap<String, String> additionalProperties = new HashMap<>();
        JSONObject additionalPropertiesJSON;
        try {
            // Get values for additional properties given at key generation step
            additionalPropertiesJSON = new JSONObject((String) oauthAppRequest.getOAuthApplicationInfo()
                            .getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES));
        } catch (JSONException e) {
            log.error(APIConstants.JSON_ADDITIONAL_PROPERTIES + " is not a JSON object");
            throw new APIManagementException(ExceptionCodes.JSON_PARSE_ERROR.getErrorMessage(),
                    ExceptionCodes.JSON_PARSE_ERROR);
        }

        // Add values of additional properties defined in the config to the default additional property list JSON object
        for (String key : keyManagerAdditionalProperties.keySet()) {
            if (additionalPropertiesJSON.has(key)) {
                additionalProperties.put(key, additionalPropertiesJSON.getString(key));
            }
        }
        return additionalProperties;
    }

    /**
     * Obtain Application role name using application name.
     * @param applicationName Application name
     * @return Application role name
     */
    protected static String getAppRoleName(String applicationName) {

        return org.wso2.carbon.identity.application.mgt.ApplicationConstants.APPLICATION_DOMAIN +
                UserCoreConstants.DOMAIN_SEPARATOR + applicationName;
    }

    /**
     * Add the application role to the admin so that admin can manipulate app data.
     * @param applicationName Application Name
     * @throws APIManagementException
     */
    @Generated(message = "excluding from coverage because it is a void method with external calls")
    public static void addApplicationRoleToAdmin(String applicationName) throws APIManagementException {

        APIManagerConfiguration config = KeyManagerDataHolder.getInstance().getApiManagerConfigurationService()
                .getAPIManagerConfiguration();
        String adminUsername = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String roleName = getAppRoleName(applicationName);
        String[] newRoles = {roleName};

        try {
            // assign new application role to the user.
            UserRealm realm = getUserRealm(adminUsername);
            if (realm != null) {
                if (((AbstractUserStoreManager) realm.getUserStoreManager()).isUserInRole(adminUsername, roleName)) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("The user: %s is already having the role: %s",
                                adminUsername.replaceAll("[\r\n]", ""), roleName.replaceAll("[\r\n]", "")));
                    }
                } else {
                    realm.getUserStoreManager().updateRoleListOfUser(adminUsername, null, newRoles);
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Assigning application role : %s to the user : %s",
                                roleName.replaceAll("[\r\n]", ""), adminUsername.replaceAll("[\r\n]", "")));
                    }
                }
            }
        } catch (UserStoreException e) {
            String errorMessage = String.format("Error while assigning application role: %s to the user: %s",
                    roleName.replaceAll("[\r\n]", ""), adminUsername.replaceAll("[\r\n]", ""));
            throw new APIManagementException(errorMessage, e);
        }
    }

    @Generated(message = "separated for unit testing purposes")
    protected static UserRealm getUserRealm(String username) throws APIManagementException {

        try {
            int tenantId = getTenantIdOfUser(username);
            return KeyManagerDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while obtaining user realm for user: " + username, e);
        }
    }

    /**
     * Get the tenant id of the user.
     * @param username
     * @return
     * @throws IdentityRuntimeException
     */
    public static int getTenantIdOfUser(String username) throws IdentityRuntimeException {
        int tenantId = -1;
        String domainName = MultitenantUtils.getTenantDomain(username);
        if (domainName != null) {
            try {
                TenantManager tenantManager = KeyManagerDataHolder.getInstance().getRealmService().getTenantManager();
                tenantId = tenantManager.getTenantId(domainName);
            } catch (UserStoreException e) {
                String errorMsg = "Error when getting the tenant id from the tenant domain : " + domainName;
                throw IdentityRuntimeException.error(errorMsg, e);
            }
        }

        if (tenantId == -1) {
            throw IdentityRuntimeException.error("Invalid tenant domain of user " + username);
        } else {
            return tenantId;
        }
    }

    /**
     * Obtain the value from Configuration DTO object.
     * @param obAdditionalProperties Additional Property Map
     * @param propertyName Property Name
     * @return value for given property
     * @throws APIManagementException
     */
    public static String getValueForAdditionalProperty(Map<String, ConfigurationDto> obAdditionalProperties,
                                                       String propertyName) throws APIManagementException {
        ConfigurationDto property = obAdditionalProperties.get(propertyName);
        if (property != null) {
            List<Object> values = property.getValues();
            if (values.size() > 0) {
                return (String) values.get(0);
            } else {
                String msg = "No value found for additional property: " + propertyName;
                log.error(msg);
                throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
            }
        } else {
            String msg = propertyName + " property not found in additional properties";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
    }

    /**
     * Validate certificate provided as user input.
     * @param cert Certificate string
     * @throws APIManagementException if the certificate is invalid or expired
     */
    @Generated(message = "Excluding from code coverage since it is covered from other method")
    public static void validateCertificate(String cert) throws APIManagementException {
        X509Certificate certificate;
        try {
            certificate = CertificateUtils.parseCertificate(cert);
        } catch (FinancialServicesException e) {
            String msg = "Certificate unavailable";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
        if (CertificateUtils.isExpired(certificate)) {
            String msg = "Provided certificate expired";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
        log.debug("Provided certificate successfully validated");
    }
}
