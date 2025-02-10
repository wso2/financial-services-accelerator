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

package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.response.validator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.as.validator.TokenValidator;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;

import javax.servlet.http.HttpServletRequest;

/**
   Validator for hybrid flow code token requests.
 */
public class FSHybridResponseTypeValidator extends TokenValidator {

    private static final Log log = LogFactory.getLog(FSHybridResponseTypeValidator.class);

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
    @SuppressFBWarnings("SERVLET_PARAMETER")
    // Suppressed content - request.getParameter("response_type")
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 3
    public void validateRequiredParameters(HttpServletRequest request) throws OAuthProblemException {

        super.validateRequiredParameters(request);
        String openIdScope = request.getParameter(OAuth.OAUTH_SCOPE);

        if (StringUtils.isBlank(openIdScope) || !isContainOIDCScope(openIdScope)) {
            String clientID = request.getParameter(OAuth.OAUTH_CLIENT_ID).replaceAll("[\r\n]", "");
            String errorMsg = String.format("Request with \'client_id\' = \'%s\' has \'response_type\' for " +
                    "\'hybrid flow\'; but \'openid\' scope not found.", clientID);
            log.error(errorMsg);
            throw OAuthProblemException.error(OAuthError.CodeResponse.INVALID_REQUEST)
                    .description(errorMsg);
        }

    }

    @Override
    public void validateMethod(HttpServletRequest request) throws OAuthProblemException {

        String method = request.getMethod();
        if (!OAuth.HttpMethod.GET.equals(method) && !OAuth.HttpMethod.POST.equals(method)) {
            log.error("HTTP Method not correct.");
            throw OAuthProblemException.error(OAuthError.CodeResponse.INVALID_REQUEST)
                    .description("HTTP Method not correct.");
        }
    }

    @Override
    public void validateContentType(HttpServletRequest request) throws OAuthProblemException {

    }

}
