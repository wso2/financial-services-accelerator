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

package org.wso2.financial.services.accelerator.gateway.executor.impl.api.resource.access.validation;

import org.wso2.financial.services.accelerator.common.constant.FinancialServicesErrorCodes;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.gateway.executor.core.FinancialServicesGatewayExecutor;
import org.wso2.financial.services.accelerator.gateway.executor.exception.FSExecutorException;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIRequestContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIResponseContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSExecutorError;
import org.wso2.financial.services.accelerator.gateway.util.GatewayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API Resource Access Validation executor.
 * This executor validates the grant type.
 */
public class APIResourceAccessValidationExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(APIResourceAccessValidationExecutor.class);

    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Method to handle post response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Method to handle post request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {
        // Skip the executor if previous executors failed.
        if (obapiRequestContext.isError()) {
            return;
        }

        // Get allowed security definitions
        List<String> allowedOAuthFlows = GatewayUtils.getAllowedOAuthFlows(obapiRequestContext);

        // Return if the end point is not secured
        if (allowedOAuthFlows.isEmpty()) {
            log.debug("Requested resource does not require authentication.");
            return;
        }

        // Retrieve grant types of the access token
        Map<String, String> transportHeaders = obapiRequestContext.getMsgInfo().getHeaders();
        try {
            String bearerTokenPayload = GatewayUtils.getBearerTokenPayload(transportHeaders);
            String tokenType = GatewayUtils.getTokenType(bearerTokenPayload);

            //validation
            GatewayUtils.validateGrantType(tokenType, allowedOAuthFlows);
        } catch (OpenBankingExecutorException e) {
            //catch errors and set to context
            OpenBankingExecutorError error = new OpenBankingExecutorError(e.getErrorCode(), e.getMessage(),
                    e.getErrorPayload(), OpenBankingErrorCodes.UNAUTHORIZED_CODE);
            ArrayList<OpenBankingExecutorError> executorErrors = obapiRequestContext.getErrors();
            executorErrors.add(error);
            obapiRequestContext.setError(true);
            obapiRequestContext.setErrors(executorErrors);
        }
    }
}
