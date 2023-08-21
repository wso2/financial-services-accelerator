/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.wso2.openbanking.accelerator.keymanager;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.keymanager.internal.KeyManagerDataHolder;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * Util class for OB key manager.
 */
public class KeyManagerUtil {

    private static final Log log = LogFactory.getLog(KeyManagerUtil.class);

    /**
     * Method to get the session Cookie.
     *
     * @return Session cookie as a String
     * @throws APIManagementException When failed to obtain the session cookie
     */
    public static String getSessionCookie() throws APIManagementException {

        String sessionCookie = "";
        APIManagerConfiguration config = KeyManagerDataHolder.getInstance().getApiManagerConfigurationService()
                .getAPIManagerConfiguration();
        String adminUsername = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);

        char[] adminPassword = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD).toCharArray();
        try {
            if (KeyManagerDataHolder.getInstance().getAuthenticationAdminStub().login(adminUsername,
                    String.valueOf(adminPassword), "localhost")) {
                ServiceContext serviceContext = KeyManagerDataHolder.getInstance().getAuthenticationAdminStub()
                        ._getServiceClient().getLastOperationContext()
                        .getServiceContext();
                sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            }
        } catch (RemoteException e) {
            throw new APIManagementException("Error occurred while making remote call.", e);
        } catch (LoginAuthenticationExceptionException e) {
            throw new APIManagementException("Error occurred while authenticating user.", e);
        }
        return sessionCookie;
    }


    /**
     * Method to bind session cookie to Admin service client.
     *
     * @param serviceClient Admin service client
     * @param sessionCookie session cookie as a string
     */
    public static void setAdminServiceSession(ServiceClient serviceClient, String sessionCookie) {

        Options userAdminOption = serviceClient.getOptions();
        userAdminOption.setManageSession(true);
        userAdminOption.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
    }

    /**
     * Obtain OB Key Manage Extension Impl class from config.
     *
     * @return obKeyManagerExtensionInterface
     */
    public static OBKeyManagerExtensionInterface getOBKeyManagerExtensionImpl() throws APIManagementException {
        OBKeyManagerExtensionInterface obKeyManagerExtensionImpl;
        try {
            String obKeyManagerExtensionImplName = OpenBankingConfigParser.getInstance()
                    .getOBKeyManagerExtensionImpl();
            if (!StringUtils.isEmpty(obKeyManagerExtensionImplName)) {
                obKeyManagerExtensionImpl = (OBKeyManagerExtensionInterface)
                        Class.forName(obKeyManagerExtensionImplName).getDeclaredConstructor().newInstance();
                return obKeyManagerExtensionImpl;
            } else {
                return null;
            }

        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new APIManagementException("Failed to obtain OB Key Manager Extension Impl instance", e);
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
        Map<String, Map<String, String>> keyManagerAdditionalProperties = OpenBankingConfigParser.getInstance()
                .getKeyManagerAdditionalProperties();
        HashMap<String, String> additionalProperties = new HashMap<>();
        Object additionalPropertiesJSON;
        try {
            // Get values for additional properties given at key generation step
            additionalPropertiesJSON = new JSONParser(JSONParser.MODE_PERMISSIVE)
                    .parse((String) oauthAppRequest.getOAuthApplicationInfo()
                            .getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES));
            if (!(additionalPropertiesJSON instanceof JSONObject)) {
                log.error(APIConstants.JSON_ADDITIONAL_PROPERTIES + " is not a JSON object");
                throw new APIManagementException(ExceptionCodes.JSON_PARSE_ERROR.getErrorMessage(),
                        ExceptionCodes.JSON_PARSE_ERROR);
            }
        } catch (ParseException e) {
            throw new APIManagementException(ExceptionCodes.JSON_PARSE_ERROR.getErrorMessage(), e,
                    ExceptionCodes.JSON_PARSE_ERROR);
        }

        JSONObject additionalPropertiesJSONObject = (JSONObject) additionalPropertiesJSON;
        // Add values of additional properties defined in the config to the default additional property list JSON object
        for (String key : keyManagerAdditionalProperties.keySet()) {
            additionalProperties.put(key, additionalPropertiesJSONObject.getAsString(key));
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
    protected static void addApplicationRoleToAdmin(String applicationName) throws APIManagementException {

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
                        log.debug("The user: " + adminUsername + " is already having the role: " + roleName);
                    }
                } else {
                    realm.getUserStoreManager().updateRoleListOfUser(adminUsername, null, newRoles);
                    if (log.isDebugEnabled()) {
                        log.debug("Assigning application role : " + roleName + " to the user : " + adminUsername);
                    }
                }
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while assigning application role: " + roleName +
                    " to the user: " + adminUsername, e);
        }
    }

    @Generated(message = "separated for unit testing purposes")
    protected static UserRealm getUserRealm(String username) throws APIManagementException {

        try {
            int tenantId = IdentityTenantUtil.getTenantIdOfUser(username);
            return KeyManagerDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while obtaining user realm for user: " + username, e);
        }
    }

}
