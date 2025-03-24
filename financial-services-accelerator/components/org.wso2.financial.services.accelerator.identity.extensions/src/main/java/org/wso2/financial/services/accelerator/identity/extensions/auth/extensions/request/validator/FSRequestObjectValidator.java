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

package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator;

import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.FSRequestObject;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.ValidationResponse;

import java.util.Map;

/**
 * Defines a contract for validating FSRequestObject instances.
 * Implementations should provide a validation logic.
 */
public interface FSRequestObjectValidator {

    /**
     * Validates the given FSRequestObject and returns a validation response.
     *
     * @param fsRequestObject The request object to validate.
     * @param dataMap         Additional data required for validation, such as scope-related information.
     * @return A ValidationResponse indicating whether the request is valid or contains errors.
     */
    ValidationResponse validateRequestObject(FSRequestObject fsRequestObject,
                                                             Map<String, Object> dataMap);

}
