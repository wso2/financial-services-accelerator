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

package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator;

import org.apache.commons.lang3.StringUtils;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.FSRequestObject;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.ValidationResponse;
import org.wso2.financial.services.accelerator.identity.extensions.validator.FinancialServicesValidator;

import java.util.Map;

/**
 * The extension class for enforcing BFSI Request Object Validations. For Tool kits to extend.
 */
public class FSRequestObjectValidator {

    /**
     * Extension point for tool kits. Perform validation and return the error message if any, else null.
     *
     * @param obRequestObject request object
     * @param dataMap         provides scope related data needed for validation from service provider meta data
     * @return the response object with error message.
     */
    public ValidationResponse validateFSConstraints(FSRequestObject obRequestObject, Map<String, Object> dataMap) {

        String violation = FinancialServicesValidator.getInstance().getFirstViolation(obRequestObject);

        if (StringUtils.isEmpty(violation)) {
            return new ValidationResponse(true);
        } else {
            return new ValidationResponse(false, violation);
        }
    }

}
