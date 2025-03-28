/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.financial.services.accelerator.consent.mgt.extensions.common;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;

import java.util.Map;

/**
 * Util class for external API service related operations.
 */
public class ExternalAPIUtil {

    /**
     * Handle the error response from the external service.
     * This method sends a response to the caller with the 'status-code' and 'data' from the external service response.
     *
     * @param response ExternalServiceResponse
     * @throws ConsentException ConsentException
     */
    public static void handleResponseError(ExternalServiceResponse response) throws ConsentException {

        int httpErrorCode = getHttpErrorCode(response);

        JSONObject responseData = new JSONObject();

        if (response.getData() != null) {
            Object data = response.getData();

            if (data instanceof Map) {
                responseData = new JSONObject((Map<?, ?>) data);
            } else {
                responseData = new JSONObject(data.toString());
            }
        }

        throw new ConsentException(ResponseStatus.fromStatusCode(httpErrorCode), response.getData()
                .path(FinancialServicesConstants.ERROR_DESCRIPTION)
                .asText(FinancialServicesConstants.DEFAULT_ERROR_DESCRIPTION));
    }

    /**
     * Get the HTTP error code from the external service response.
     * If the error code is not available, not a number or not in the valid range, return 500.
     *
     * @param response ExternalServiceResponse
     * @return HTTP error code
     * @throws ConsentException ConsentException
     */
    private static int getHttpErrorCode(ExternalServiceResponse response) throws ConsentException {
        int httpErrorCode;

        if (response == null || response.getErrorCode() == null) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Error occurred while calling the external service");
        }

        try {
            httpErrorCode = Integer.parseInt(response.getErrorCode());
        } catch (NumberFormatException e) {
            httpErrorCode = 500;
        }
        if (httpErrorCode < 400 || httpErrorCode >= 600) {
            httpErrorCode = 500;
        }
        return httpErrorCode;
    }

}
