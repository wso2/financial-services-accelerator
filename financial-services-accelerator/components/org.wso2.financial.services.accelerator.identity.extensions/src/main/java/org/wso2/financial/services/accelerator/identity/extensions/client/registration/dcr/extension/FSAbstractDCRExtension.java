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

package org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.extension;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;

import java.util.List;
import java.util.Map;

/**
 * Abstract class for extending methods to be invoked by FS Additional Attribute Filter.
 * These methods can be used to validate the DCR request.
 */
public abstract class FSAbstractDCRExtension {

    /**
     * Validate additional attributes in the DCR registration request. Additional attributes passed
     * as SSA claims also can be accessed here. The attributes returned from this method will be stored as service
     * provider metadata as String values. Of the returned attributes, the keys returned from the
     * getResponseAttributeKeys method will be sent back in the DCR register response.
     *
     * @param appRegistrationRequest  DCR registration request.
     * @param ssaClaims               SSA claims.
     *
     * @return Map of additional attributes to be stored as service provider metadata.
     * @throws FinancialServicesException In case of validation failure or any other blocking error.
     */
    public abstract Map<String, Object> validateDCRRegisterAttributes(JSONObject appRegistrationRequest,
                                                      Map<String, Object> ssaClaims)
            throws FinancialServicesException;

    /**
     * Validate additional attributes in the DCR update request. Additional attributes passed as SSA
     * claims also can be accessed here. The attributes returned from this method will be stored as service provider
     * metadata as String values. If any of the keys already exists as metadata, they will be updated. Of the returned
     * attributes, the keys returned from the getResponseAttributeKeys method will be sent back in the
     * DCR update response.
     *
     * @param applicationUpdateRequest  DCR update request.
     * @param ssaClaims                 SSA claims.
     * @param spProperties              Existing service provider properties.
     *
     * @return Map of additional attributes to be stored as service provider metadata.
     * @throws FinancialServicesException In case of validation failure or any other blocking error.
     */
    public abstract Map<String, Object> validateDCRUpdateAttributes(JSONObject applicationUpdateRequest,
                                                      Map<String, Object> ssaClaims, List<JSONObject> spProperties)
            throws FinancialServicesException;
}
