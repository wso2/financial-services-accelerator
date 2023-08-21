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

package com.wso2.openbanking.accelerator.gateway.executor.impl.selfcare.portal;

import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * UserPermissionValidationExecutor.
 * <p>
 * Validates access token scopes against users
 */
public class UserPermissionValidationExecutor implements OpenBankingGatewayExecutor {

    private static final Log LOG = LogFactory.getLog(UserPermissionValidationExecutor.class);

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {
        try {
            // Skip the executor if previous executors failed.
            if (obapiRequestContext.isError()) {
                return;
            }

            String authToken = obapiRequestContext.getMsgInfo().getHeaders().get(HttpHeaders.AUTHORIZATION);
            JSONObject tokenBody = JWTUtils.decodeRequestJWT(authToken.replace("Bearer ", ""), "body");
            String tokenScopes = tokenBody.getAsString("scope");

            if (!isCustomerCareOfficer(tokenScopes)) {
                // user is not a customer care officer
                Optional<String> optUserId = getUserIdsFromQueryParams(obapiRequestContext.getMsgInfo().getResource());
                String tokenSubject = GatewayUtils.getUserNameWithTenantDomain(tokenBody.getAsString("sub"));
                if (!optUserId.isPresent() || !isUserIdMatchesTokenSub(optUserId.get(), tokenSubject)) {
                    // token subject and user id do not match, invalid request
                    final String errorMsg = "Invalid self care portal request received. " +
                            "UserId and token subject do not match.";
                    LOG.error(errorMsg + " userIDs: " + optUserId.orElse(" ") + " sub: " + tokenSubject);
                    OpenBankingExecutorError error = new OpenBankingExecutorError(
                            OpenBankingErrorCodes.SCP_USER_VALIDATION_FAILED_CODE, "Unauthorized Request", errorMsg,
                            OpenBankingErrorCodes.UNAUTHORIZED_CODE);

                    obapiRequestContext.setError(true);

                    ArrayList<OpenBankingExecutorError> executorErrors = obapiRequestContext.getErrors();
                    executorErrors.add(error);
                    obapiRequestContext.setErrors(executorErrors);

                    Map<String, String> contextProps = new HashMap<>();
                    contextProps.put(GatewayConstants.ERROR_STATUS_PROP, OpenBankingErrorCodes.UNAUTHORIZED_CODE);
                    obapiRequestContext.setContextProps(contextProps);
                }
            }
        } catch (ParseException e) {
            final String errorMsg = "Error occurred while validating self care portal user permissions";

            LOG.error(errorMsg + ". Caused by, ", e);
            //catch errors and set to context
            OpenBankingExecutorError error = new OpenBankingExecutorError(
                    OpenBankingErrorCodes.SCP_USER_VALIDATION_FAILED_CODE, e.getMessage(), errorMsg,
                    OpenBankingErrorCodes.BAD_REQUEST_CODE);

            ArrayList<OpenBankingExecutorError> executorErrors = obapiRequestContext.getErrors();
            executorErrors.add(error);

            obapiRequestContext.setError(true);
            obapiRequestContext.setErrors(executorErrors);
        }

    }

    @Generated(message = "Ignoring since empty")
    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {
        // do not need to handle
    }

    @Generated(message = "Ignoring since empty")
    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {
        // do not need to handle
    }

    @Generated(message = "Ignoring since empty")
    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {
        // do not need to handle
    }

    /**
     * Method to extract userID from the request URL.
     *
     * @param url requested URL
     * @return Optional String: if userID found return userID, else return empty
     */
    protected Optional<String> getUserIdsFromQueryParams(String url) {
        if (StringUtils.isNotEmpty(url) && url.contains("?")) {
            final String queryParams = url.split("\\?")[1];
            final String[] queryParamPairs = queryParams.split("&");

            for (String pair : queryParamPairs) {
                if (pair.contains("userIDs") || pair.contains("userID")) {
                    final String[] userIds = pair.split("=");
                    if (userIds.length > 1) { // to prevent indexOutOfBoundException
                        return Optional.of(GatewayUtils.getUserNameWithTenantDomain(userIds[1]));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Method to match customer care officer scopes.
     *
     * @param scopes scopes received from access token
     * @return if customer care officer scope found return true else false
     */
    protected boolean isCustomerCareOfficer(String scopes) {
        if (StringUtils.isNotEmpty(scopes)) {
            return scopes.contains(GatewayConstants.CUSTOMER_CARE_OFFICER_SCOPE);
        }
        return false;
    }

    /**
     * Method to match user id and token subject.
     *
     * @param userId   received from query parameter
     * @param tokenSub received from access token body
     * @return if user id matches with token subject return true else false
     */
    protected boolean isUserIdMatchesTokenSub(String userId, String tokenSub) {
        if (StringUtils.isNotEmpty(tokenSub)) {
            return tokenSub.equalsIgnoreCase(userId);
        }
        return false;
    }
}
