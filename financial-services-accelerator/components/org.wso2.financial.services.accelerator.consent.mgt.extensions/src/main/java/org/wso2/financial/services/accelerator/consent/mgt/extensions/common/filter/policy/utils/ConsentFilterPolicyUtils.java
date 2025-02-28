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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.common.filter.policy.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class for consent filter policy.
 */
public class ConsentFilterPolicyUtils {

    private static final Log log = LogFactory.getLog(ConsentFilterPolicyUtils.class);

    /**
     * Extract string payload from request object.
     *
     * @param request The request object
     * @return String payload
     * @throws FinancialServicesException Payload read errors
     */
    public static String getStringPayload(HttpServletRequest request) throws FinancialServicesException {
        try {
            return IOUtils.toString(request.getInputStream());
        } catch (IOException e) {
            log.error("Error while extracting the payload", e);
            throw new FinancialServicesException("Error while extracting the payload", e);
        }
    }

}
