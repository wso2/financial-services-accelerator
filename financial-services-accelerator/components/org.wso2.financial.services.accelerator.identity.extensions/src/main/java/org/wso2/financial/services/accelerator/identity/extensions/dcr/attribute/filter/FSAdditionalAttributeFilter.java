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

package org.wso2.financial.services.accelerator.identity.extensions.dcr.attribute.filter;

import com.google.gson.Gson;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationRegistrationRequest;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationUpdateRequest;
import org.wso2.carbon.identity.oauth.dcr.exception.DCRMClientException;
import org.wso2.carbon.identity.oauth.dcr.handler.AdditionalAttributeFilter;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.identity.extensions.dcr.validators.IssuerValidator;
import org.wso2.financial.services.accelerator.identity.extensions.dcr.validators.RequiredParamsValidator;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Additional attribute filter for financial services accelerator.
 */
public class FSAdditionalAttributeFilter implements AdditionalAttributeFilter {

    @Override
    public Map<String, Object> filterDCRRegisterAttributes(ApplicationRegistrationRequest appRegistrationRequest,
                                                           Map<String, Object> ssaParams) throws DCRMClientException {

        try {
            validateApplicationCreateRequest(appRegistrationRequest, ssaParams);
        } catch (FinancialServicesException e) {
            throw new DCRMClientException("Error occurred while registering the application", e);
        }
        Map<String, Object> filteredAttributes = new HashMap<>();

        Map<String, Object> additionalAttributes = appRegistrationRequest.getAdditionalAttributes();
        filteredAttributes.put(IdentityCommonConstants.SOFTWARE_STATEMENT,
                appRegistrationRequest.getSoftwareStatement());
        filteredAttributes.put(IdentityCommonConstants.SOFTWARE_ID,
                additionalAttributes.get(IdentityCommonConstants.SOFTWARE_ID));
        filteredAttributes.put(IdentityCommonConstants.SCOPE,
                additionalAttributes.get(IdentityCommonConstants.SCOPE));
        filteredAttributes.put(IdentityCommonConstants.ISS, additionalAttributes.get(IdentityCommonConstants.ISS));

        getResponseParamsFromConfig().forEach((key) -> {
            if (additionalAttributes.containsKey(key)) {
                filteredAttributes.put(key, additionalAttributes.get(key));
            }
        });
        return filteredAttributes;
    }

    @Override
    public Map<String, Object> filterDCRUpdateAttributes(ApplicationUpdateRequest applicationUpdateRequest,
                                                         Map<String, Object> ssaParams,
                                                         ServiceProviderProperty[] serviceProviderProperties)
            throws DCRMClientException {

        try {
            validateApplicationUpdateRequest(applicationUpdateRequest, ssaParams);
        } catch (FinancialServicesException e) {
            throw new DCRMClientException("Error occurred while updating the application", e);
        }

        Map<String, Object> filteredAttributes = new HashMap<>();

        Map<String, Object> additionalAttributes = applicationUpdateRequest.getAdditionalAttributes();
        filteredAttributes.put(IdentityCommonConstants.SOFTWARE_STATEMENT,
                applicationUpdateRequest.getSoftwareStatement());
        filteredAttributes.put(IdentityCommonConstants.SOFTWARE_ID,
                additionalAttributes.get(IdentityCommonConstants.SOFTWARE_ID));
        filteredAttributes.put(IdentityCommonConstants.SCOPE,
                additionalAttributes.get(IdentityCommonConstants.SCOPE));
        filteredAttributes.put(IdentityCommonConstants.ISS, additionalAttributes.get(IdentityCommonConstants.ISS));

        getResponseParamsFromConfig().forEach((key) -> {
            if (additionalAttributes.containsKey(key)) {
                filteredAttributes.put(key, additionalAttributes.get(key));
            }
        });
        return filteredAttributes;
    }

    @Override
    public Map<String, Object> processDCRGetAttributes(Map<String, String> ssaParams) throws DCRMClientException {
        Map<String, Object> filteredAttributes = new HashMap<>();
        filteredAttributes.putAll(ssaParams);
        return filteredAttributes;

    }

    private void validateApplicationCreateRequest(ApplicationRegistrationRequest applicationRegistrationRequest,
                                                  Map<String, Object> ssaParams) throws FinancialServicesException {

        Gson gson = new Gson();
        Map<String, Object> requestParameterMap = gson.fromJson(gson.toJson(applicationRegistrationRequest), Map.class);

        RequiredParamsValidator.validate(requestParameterMap, ssaParams);
        IssuerValidator.validate(applicationRegistrationRequest, ssaParams);
    }

    private void validateApplicationUpdateRequest(ApplicationUpdateRequest applicationUpdateRequest,
                                          Map<String, Object> ssaParams) throws FinancialServicesException {

        Gson gson = new Gson();
        Map<String, Object> requestParameterMap = gson.fromJson(gson.toJson(applicationUpdateRequest), Map.class);

        RequiredParamsValidator.validate(requestParameterMap, ssaParams);
        IssuerValidator.validate(applicationUpdateRequest, ssaParams);
    }

    @Override
    public List<String> getResponseAttributeKeys() {

        List<String> responseAttributeKeys = getResponseParamsFromConfig();
        responseAttributeKeys.add(IdentityCommonConstants.SOFTWARE_STATEMENT);
        responseAttributeKeys.add(IdentityCommonConstants.SOFTWARE_ID);
        responseAttributeKeys.add(IdentityCommonConstants.SCOPE);
        return responseAttributeKeys;
    }

    /**
     * Get the response parameters from the configuration.
     * @return List of response parameters.
     */
    private List<String> getResponseParamsFromConfig() {

        List<String> responseParams = new ArrayList<>();

        Map<String, Map<String, Object>> dcrConfigs = FinancialServicesConfigParser.getInstance()
                .getDCRParamsConfig();

        dcrConfigs.forEach((key, value) -> {
            if (Boolean.parseBoolean(value.get("IncludeInResponse").toString())) {
                responseParams.add(value.get("Key").toString());
            }
        });
        return responseParams;
    }
}
