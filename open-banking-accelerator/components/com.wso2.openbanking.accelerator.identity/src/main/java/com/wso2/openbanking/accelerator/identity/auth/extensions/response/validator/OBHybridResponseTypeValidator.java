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

package com.wso2.openbanking.accelerator.identity.auth.extensions.response.validator;

import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.as.validator.TokenValidator;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

/**
   Validator for hybrid flow code token requests.
 */
public class OBHybridResponseTypeValidator extends TokenValidator {

    private static final Log log = LogFactory.getLog(OBHybridResponseTypeValidator.class);

    private static boolean isContainOIDCScope(String scope) {

        String[] scopeArray = scope.split("\\s+");
        for (String anyScope : scopeArray) {
            if (anyScope.equals(OAuthConstants.Scope.OPENID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validateRequiredParameters(HttpServletRequest request) throws OAuthProblemException {

        String openIdScope;
        if (StringUtils.isNotBlank(request.getParameter(IdentityCommonConstants.REQUEST_URI))) {

            this.requiredParams = new ArrayList(Arrays.asList(OAuth.OAUTH_CLIENT_ID,
                    IdentityCommonConstants.REQUEST_URI));
            this.notAllowedParams.add(IdentityCommonConstants.REQUEST);
            openIdScope = IdentityCommonUtil.decodeRequestObjectAndGetKey(request, OAuth.OAUTH_SCOPE);
        } else {
            openIdScope = request.getParameter(OAuth.OAUTH_SCOPE);
        }

        super.validateRequiredParameters(request);

        if (StringUtils.isBlank(openIdScope) || !isContainOIDCScope(openIdScope)) {
            String clientID = request.getParameter(OAuth.OAUTH_CLIENT_ID);
            throw OAuthProblemException.error(OAuthError.CodeResponse.INVALID_REQUEST)
                    .description("Request with \'client_id\' = \'" + clientID +
                            "\' has \'response_type\' for \'hybrid flow\'; but \'openid\' scope not found.");
        }

    }

    @Override
    public void validateMethod(HttpServletRequest request) throws OAuthProblemException {

        String method = request.getMethod();
        if (!OAuth.HttpMethod.GET.equals(method) && !OAuth.HttpMethod.POST.equals(method)) {
            throw OAuthProblemException.error(OAuthError.CodeResponse.INVALID_REQUEST)
                    .description("Method not correct.");
        }
    }

    @Override
    public void validateContentType(HttpServletRequest request) throws OAuthProblemException {

    }

}
