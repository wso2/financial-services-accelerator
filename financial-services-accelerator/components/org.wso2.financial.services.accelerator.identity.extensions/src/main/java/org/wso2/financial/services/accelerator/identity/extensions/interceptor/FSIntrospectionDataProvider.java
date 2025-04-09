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

package org.wso2.financial.services.accelerator.identity.extensions.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.core.handler.AbstractIdentityHandler;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.IntrospectionDataProvider;
import org.wso2.carbon.identity.oauth2.dto.OAuth2IntrospectionResponseDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.financial.services.accelerator.common.constant.ErrorConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * FS specific introspection data provider
 */
public class FSIntrospectionDataProvider extends AbstractIdentityHandler implements IntrospectionDataProvider {

    private static final Log log = LogFactory.getLog(FSIntrospectionDataProvider.class);
    private static IntrospectionDataProvider introspectionDataProvider;

    @Override
    public Map<String, Object> getIntrospectionData(OAuth2TokenValidationRequestDTO oAuth2TokenValidationRequestDTO,
                                                    OAuth2IntrospectionResponseDTO oAuth2IntrospectionResponseDTO)
            throws IdentityOAuth2Exception {

        Map<String, Object> additionalDataMap = new HashMap<>();

        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum
                .PRE_TOKEN_INTROSPECTION)) {
            // Perform FS customized behaviour with service extension
            try {
                additionalDataMap = getIntrospectionDataWithServiceExtension(
                        oAuth2TokenValidationRequestDTO, oAuth2IntrospectionResponseDTO);
            } catch (FinancialServicesException e) {
                log.error(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR, e);
                throw new IdentityOAuth2Exception(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR);
            }
        } else if (getIntrospectionDataProvider() != null) {
            // Perform FS customized behaviour
            additionalDataMap = getIntrospectionDataProvider()
                    .getIntrospectionData(oAuth2TokenValidationRequestDTO, oAuth2IntrospectionResponseDTO);
        } else {
            // Perform FS default behaviour
            additionalDataMap = getDefaultIntrospectionData(oAuth2TokenValidationRequestDTO,
                    oAuth2IntrospectionResponseDTO);
        }

        String[] nonInternalScopes = IdentityCommonUtils.removeInternalScopes(oAuth2IntrospectionResponseDTO.getScope()
                .split(IdentityCommonConstants.SPACE_SEPARATOR));
        oAuth2IntrospectionResponseDTO.setScope(StringUtils.join(nonInternalScopes,
                IdentityCommonConstants.SPACE_SEPARATOR));
        additionalDataMap.put(IdentityCommonConstants.SCOPE, StringUtils.join(nonInternalScopes,
                IdentityCommonConstants.SPACE_SEPARATOR));
        oAuth2IntrospectionResponseDTO.setProperties(additionalDataMap);
        return additionalDataMap;
    }

    private Map<String, Object> getIntrospectionDataWithServiceExtension(
            OAuth2TokenValidationRequestDTO oAuth2TokenValidationRequestDTO,
            OAuth2IntrospectionResponseDTO oAuth2IntrospectionResponseDTO)
            throws FinancialServicesException, IdentityOAuth2Exception {

        // Construct the payload
        JSONObject data = new JSONObject();
        data.put(IdentityCommonConstants.SCOPES, oAuth2IntrospectionResponseDTO.getScope()
                .split(IdentityCommonConstants.SPACE_SEPARATOR));

        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(UUID.randomUUID().toString(), data);

        // Invoke external service
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.PRE_TOKEN_INTROSPECTION);

        return processResponseAndGetData(response);
    }

    private Map<String, Object> processResponseAndGetData(ExternalServiceResponse response)
            throws IdentityOAuth2Exception {

        IdentityCommonUtils.serviceExtensionActionStatusValidation(response);

        JsonNode responseData = response.getData();
        if (responseData == null || !responseData.has("attributes")) {
            throw new IdentityOAuth2Exception("Missing attributes in response payload.");
        }

        Map<String, Object> additionalAttributes = new HashMap<>();
        for (JsonNode attributeNode : responseData.get("attributes")) {
            if (!attributeNode.hasNonNull("key") || !attributeNode.hasNonNull("value")) {
                continue;
            }

            String key = attributeNode.get("key").asText();
            Object value = attributeNode.get("value").asText();

            // Add only if key is not empty
            if (!key.isEmpty()) {
                additionalAttributes.put(key, value);
            }
        }

        return additionalAttributes;
    }

    private Map<String, Object> getDefaultIntrospectionData(
            OAuth2TokenValidationRequestDTO oAuth2TokenValidationRequestDTO,
            OAuth2IntrospectionResponseDTO oAuth2IntrospectionResponseDTO) {

        if (oAuth2IntrospectionResponseDTO.isActive()) {
            return oAuth2IntrospectionResponseDTO.getProperties();
        } else {
            return new HashMap<>();
        }
    }

    public static IntrospectionDataProvider getIntrospectionDataProvider() {

        return introspectionDataProvider;
    }

    public static void setIntrospectionDataProvider(IntrospectionDataProvider introspectionDataProvider) {

        FSIntrospectionDataProvider.introspectionDataProvider = introspectionDataProvider;
    }
}
