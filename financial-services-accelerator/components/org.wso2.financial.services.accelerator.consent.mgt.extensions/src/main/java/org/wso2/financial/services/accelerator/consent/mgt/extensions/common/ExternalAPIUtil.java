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

import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;

/**
 * Util class for external API service related operations.
 */
public class ExternalAPIUtil {

    public static void handleResponseError(ExternalServiceResponse response) throws ConsentException {

        int httpErrorCode = Integer.parseInt(response.getErrorCode());
        if (httpErrorCode < 400 || httpErrorCode >= 500) {
            httpErrorCode = 500;
        }
        throw new ConsentException(ResponseStatus.fromStatusCode(httpErrorCode), response.getData()
                .get(FinancialServicesConstants.ERROR_DESCRIPTION)
                .asText(FinancialServicesConstants.DEFAULT_ERROR_DESCRIPTION));
    }
}
