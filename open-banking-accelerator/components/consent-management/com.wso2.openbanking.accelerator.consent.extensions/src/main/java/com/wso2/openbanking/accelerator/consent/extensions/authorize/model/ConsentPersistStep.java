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

package com.wso2.openbanking.accelerator.consent.extensions.authorize.model;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;

/**
 * Consent persist step interface.
 */
public interface ConsentPersistStep {

    /**
     * Method to be implemented as a step for consent persistence. Once implemented add the step to the configuration.
     *
     * @param consentPersistData Includes all the generic data of the consents.
     * @throws ConsentException Exception thrown in case of failure.
     */
    void execute(ConsentPersistData consentPersistData) throws ConsentException;
}
