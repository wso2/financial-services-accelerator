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

package com.wso2.openbanking.accelerator.identity.claims;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.openidconnect.ClaimProvider;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RoleClaimProviderImpl.
 * <p>
 * Adding Customer Care Officer user role to cater sso flow in consent mgt react app
 */
public class RoleClaimProviderImpl implements ClaimProvider {
    private static final Log LOG = LogFactory.getLog(RoleClaimProviderImpl.class);
    private static final String USER_ROLE = "user_role";
    private static final String OPENID_SCOPE = "openid";
    private static final String CUSTOMER_CARE_OFFICER = "customerCareOfficer";
    private static final String CUSTOMER_CARE_OFFICER_ROLE = "Internal/CustomerCareOfficerRole";
    private static final String CUSTOMER_CARE_OFFICER_SCOPE = "consents:read_all";

    @Generated(message = "Do not contain logics")
    @Override
    public Map<String, Object> getAdditionalClaims(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext,
                                                   OAuth2AuthorizeRespDTO oAuth2AuthorizeRespDTO)
            throws IdentityOAuth2Exception {
        return Collections.emptyMap();
    }

    /**
     * Method to add Role based claims for Token response to cater sso flow in consent mgt react app.
     *
     * @param oAuthTokenReqMessageContext token Request message context
     * @param oAuth2AccessTokenRespDTO    token Response DTO
     * @return Map of additional claims
     * @throws IdentityOAuth2Exception when failed to obtain claims
     */
    @Override
    public Map<String, Object> getAdditionalClaims(OAuthTokenReqMessageContext oAuthTokenReqMessageContext,
                                                   OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO)
            throws IdentityOAuth2Exception {
        Map<String, Object> claims = new HashMap<>();

        List<String> scopes = Arrays.asList(oAuthTokenReqMessageContext.getScope());
        if (scopes.contains(CUSTOMER_CARE_OFFICER_SCOPE) && scopes.contains(OPENID_SCOPE)) {
            final String userId = oAuthTokenReqMessageContext.getAuthorizedUser().getUserName();

            try {
                int tenantId = IdentityTenantUtil.getTenantIdOfUser(userId);
                RealmService realmService = IdentityExtensionsDataHolder.getInstance().getRealmService();
                UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();

                String[] roles = userStoreManager.getRoleListOfUser(userId);
                if (ArrayUtils.contains(roles, CUSTOMER_CARE_OFFICER_ROLE)) {
                    claims.put(USER_ROLE, CUSTOMER_CARE_OFFICER);
                }
            } catch (IdentityRuntimeException e) {
                LOG.error("Error in retrieving user tenant name for user: " + userId + ". Caused by,", e);
            } catch (UserStoreException e) {
                LOG.error("Error in retrieving user role. Caused by,", e);
            }

        }
        return claims;
    }
}
