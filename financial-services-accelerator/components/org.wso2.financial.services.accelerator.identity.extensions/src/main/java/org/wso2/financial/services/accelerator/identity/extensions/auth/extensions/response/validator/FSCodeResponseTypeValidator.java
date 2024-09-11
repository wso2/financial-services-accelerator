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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.validators.AbstractValidator;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Validator to validate whether the response type in the request object is allowed.
 * Validate whether the correct response type is sent for regulatory applications. By default, code response type
 * is not allowed for regulatory apps.
 */
public class FSCodeResponseTypeValidator extends AbstractValidator<HttpServletRequest> {

    private static Log log = LogFactory.getLog(FSCodeResponseTypeValidator.class);
    private static final String CODE = "code";

    @Override
    public void validateMethod(HttpServletRequest request) throws OAuthProblemException {

    }

    @Override
    public void validateContentType(HttpServletRequest request) throws OAuthProblemException {

    }

    @Override
    @SuppressFBWarnings("SERVLET_PARAMETER")
    // Suppressed content - request.getParameter("response_type")
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 2
    public void validateRequiredParameters(HttpServletRequest request) throws OAuthProblemException {
        String responseType = request.getParameter("response_type").replaceAll("[\r\n]", "");
        String clientId = request.getParameter("client_id").replaceAll("[\r\n]", "");
        if (!isValidResponseType(clientId, responseType)) {
            log.error("Unsupported Response Type");
            throw OAuthProblemException.error("Unsupported Response Type");
        }
    }

    /**
     * Validate whether the correct response type is sent for regulatory applications. By default code response type
     * is not allowed for regulatory apps.
     *
     * @param clientId          Client Id received from Request Object
     * @param responseType      Response Type received from Request Object
     * @return
     */
    private boolean isValidResponseType(String clientId, String responseType) {

        try {
            if (FinancialServicesUtils.isRegulatoryApp(clientId) && CODE.equals(responseType)) {
                return false;
            }
        } catch (RequestObjectException e) {
            log.error("Error while retrieving service provider metadata", e);
            return false;
        }
        return true;
    }
}
