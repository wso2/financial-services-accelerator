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

package org.wso2.financial.services.accelerator.keymanager.internal;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;

import java.util.HashMap;
import java.util.Map;

/**
 * Data holder for key manager client extension.
 */
public class KeyManagerDataHolder {

    private APIManagerConfigurationService apiManagerConfigurationService;
    private static volatile KeyManagerDataHolder instance;
    private static final String IDENTITY_APPLICATION_MGT_SERVICE = "IdentityApplicationManagementService";
    public static final String AUTHENTICATION_ADMIN_SERVICE = "AuthenticationAdmin";
    public static final String USER_ADMIN_SERVICE = "UserAdmin";
    public static final String OAUTH_ADMIN_SERVICE = "OAuthAdminService";
    private AuthenticationAdminStub authenticationAdminStub;
    private OAuthAdminServiceStub oAuthAdminServiceStub;
    private UserAdminStub userAdminStub;
    private String backendServerURL = "";
    private RealmService realmService;
    private final Map<String, KeyManagerConnectorConfiguration> keyManagerConnectorConfigurationMap = new HashMap<>();

    public static KeyManagerDataHolder getInstance() {

        if (instance == null) {
            synchronized (KeyManagerDataHolder.class) {
                if (instance == null) {
                    instance = new KeyManagerDataHolder();
                }
            }
        }
        return instance;
    }

    public UserAdminStub getUserAdminStub() throws AxisFault {

        if (userAdminStub == null) {
            String userAdminServiceURL = backendServerURL + USER_ADMIN_SERVICE;
            userAdminStub = new UserAdminStub(userAdminServiceURL);
        }
        return userAdminStub;
    }

    public void setUserAdminStub(UserAdminStub userAdminStub) {

        this.userAdminStub = userAdminStub;
    }

    public AuthenticationAdminStub getAuthenticationAdminStub() throws AxisFault {

        if (authenticationAdminStub == null) {
            String authenticationServiceURL = backendServerURL + AUTHENTICATION_ADMIN_SERVICE;
            authenticationAdminStub = new AuthenticationAdminStub(authenticationServiceURL);
        }

        return authenticationAdminStub;
    }

    public void setAuthenticationAdminStub(AuthenticationAdminStub authenticationAdminStub) {

        this.authenticationAdminStub = authenticationAdminStub;
    }

    public OAuthAdminServiceStub getOauthAdminServiceStub() throws AxisFault {

        if (oAuthAdminServiceStub == null) {
            String oauthAdminServiceURL = backendServerURL + OAUTH_ADMIN_SERVICE;
            oAuthAdminServiceStub = new OAuthAdminServiceStub(oauthAdminServiceURL);
        }
        return oAuthAdminServiceStub;
    }


    public void setOauthAdminServiceStub(OAuthAdminServiceStub oAuthAdminServiceStub) {

        this.oAuthAdminServiceStub = oAuthAdminServiceStub;
    }

    public void setApiManagerConfiguration(APIManagerConfigurationService apiManagerConfigurationService) {

        this.apiManagerConfigurationService = apiManagerConfigurationService;
        backendServerURL = apiManagerConfigurationService.getAPIManagerConfiguration()
                .getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);

    }

    public APIManagerConfigurationService getApiManagerConfigurationService() {

        return apiManagerConfigurationService;
    }

    public String getBackendServerURL() {

        return backendServerURL;
    }

    public void addKeyManagerConnectorConfiguration(String type,
                                                    KeyManagerConnectorConfiguration keyManagerConnectorConfiguration) {

        keyManagerConnectorConfigurationMap.put(type, keyManagerConnectorConfiguration);
    }

    public void removeKeyManagerConnectorConfiguration(String type) {

        keyManagerConnectorConfigurationMap.remove(type);
    }

    public KeyManagerConnectorConfiguration getKeyManagerConnectorConfiguration(String type) {

        return keyManagerConnectorConfigurationMap.get(type);
    }

    public Map<String, KeyManagerConnectorConfiguration> getKeyManagerConnectorConfigurations() {

        return keyManagerConnectorConfigurationMap;
    }

    public RealmService getRealmService() {

        if (realmService == null) {
            throw new RuntimeException("Realm Service is not available. Component did not start correctly.");
        }
        return realmService;
    }

    void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

}
