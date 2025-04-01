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
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentPersistResponseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    /**
     * Constructs a {@link DetailedConsentResource} by merging data from
     * {@link ExternalAPIPreConsentPersistResponseDTO} and {@link ConsentResource}.
     * <p>
     * Fields in the DTO are prioritized; if null, corresponding values from the base consent are used.
     *
     * @param dto             The DTO received from the external API pre-consent step.
     * @param consentResource The base consent resource from the persistence layer.
     * @return A fully populated {@link DetailedConsentResource}.
     */
    public static DetailedConsentResource constructDetailedConsentResource(
            ExternalAPIPreConsentPersistResponseDTO dto, ConsentResource consentResource) {

        String consentID = consentResource.getConsentID();
        String clientID = consentResource.getClientID();
        String receipt = consentResource.getReceipt();
        long createdTime = consentResource.getCreatedTime();
        long updatedTime = consentResource.getUpdatedTime();

        List<AuthorizationResource> authorizationResources =
                buildAuthorizationResources(dto.getAuthorizations(), consentID, updatedTime);

        List<ConsentMappingResource> consentMappingResources =
                buildConsentMappingResources(dto.getAuthorizations(), authorizationResources);

        return buildDetailedConsentResource(dto, consentResource, authorizationResources,
                consentMappingResources, consentID, clientID, receipt, createdTime, updatedTime);
    }

    /**
     * Builds a list of {@link AuthorizationResource} objects from the ResponseDTO's authorization list.
     *
     * @param authorizations List of DTO authorization entries.
     * @param consentID      The parent consent ID to associate.
     * @param updatedTime    Timestamp for the update.
     * @return A list of {@link AuthorizationResource} objects.
     */
    private static List<AuthorizationResource> buildAuthorizationResources(
            List<ExternalAPIPreConsentPersistResponseDTO.Authorization> authorizations,
            String consentID, long updatedTime) {

        List<AuthorizationResource> authResources = new ArrayList<>();

        if (authorizations != null) {
            for (ExternalAPIPreConsentPersistResponseDTO.Authorization authorization : authorizations) {
                AuthorizationResource auth = new AuthorizationResource();
                auth.setAuthorizationID(UUID.randomUUID().toString());
                auth.setConsentID(consentID);
                auth.setUserID(authorization.getUserId());
                auth.setAuthorizationStatus(authorization.getAuthorizationStatus());
                auth.setAuthorizationType(authorization.getAuthorizationType());
                auth.setUpdatedTime(updatedTime);
                authResources.add(auth);
            }
        }

        return authResources;
    }

    /**
     * Builds a list of {@link ConsentMappingResource} objects from the DTO and associated authorizations.
     *
     * @param authorizations List of DTO authorizations.
     * @param authResources  List of mapped authorization resources with generated UUIDs.
     * @return A list of {@link ConsentMappingResource} entries.
     */
    private static List<ConsentMappingResource> buildConsentMappingResources(
            List<ExternalAPIPreConsentPersistResponseDTO.Authorization> authorizations,
            List<AuthorizationResource> authResources) {

        List<ConsentMappingResource> mappingResources = new ArrayList<>();

        if (authorizations != null) {
            for (int i = 0; i < authorizations.size(); i++) {
                ExternalAPIPreConsentPersistResponseDTO.Authorization dtoAuth = authorizations.get(i);
                String authID = authResources.get(i).getAuthorizationID();

                if (dtoAuth.getConsentedResources() != null) {
                    for (ExternalAPIPreConsentPersistResponseDTO.Resource resource : dtoAuth.getConsentedResources()) {
                        ConsentMappingResource mapping = new ConsentMappingResource();
                        mapping.setAuthorizationID(authID);
                        mapping.setAccountID(resource.getResourceId());
                        mapping.setPermission(resource.getPermission());
                        mapping.setMappingStatus(resource.getStatus());
                        mappingResources.add(mapping);
                    }
                }
            }
        }

        return mappingResources;
    }

    /**
     * Combines all resolved data into a final {@link DetailedConsentResource}.
     *
     * @param dto              The DTO with potential override values.
     * @param consentResource  The fallback source of values.
     * @param authResources    List of resolved authorization entries.
     * @param mappingResources List of resolved account mapping entries.
     * @param consentID        Consent ID.
     * @param clientID         Client ID.
     * @param receipt          Receipt JSON string.
     * @param createdTime      Consent creation timestamp.
     * @param updatedTime      Consent update timestamp.
     * @return A fully constructed {@link DetailedConsentResource}.
     */
    private static DetailedConsentResource buildDetailedConsentResource(ExternalAPIPreConsentPersistResponseDTO dto,
                                                                        ConsentResource consentResource,
                                                                        List<AuthorizationResource> authResources,
                                                                        List<ConsentMappingResource> mappingResources,
                                                                        String consentID, String clientID,
                                                                        String receipt,
                                                                        long createdTime, long updatedTime) {

        String resolvedConsentType = (dto.getConsentType() != null) ? dto.getConsentType() :
                consentResource.getConsentType();
        String resolvedConsentStatus = (dto.getConsentStatus() != null) ? dto.getConsentStatus() :
                consentResource.getCurrentStatus();
        int resolvedFrequency = (dto.getConsentFrequency() != null) ? dto.getConsentFrequency() :
                consentResource.getConsentFrequency();
        long resolvedValidity = (dto.getValidityTime() != null) ? dto.getValidityTime() :
                consentResource.getValidityPeriod();
        boolean resolvedRecurring = (dto.getRecurringIndicator() != null) ? dto.getRecurringIndicator() :
                consentResource.isRecurringIndicator();

        return new DetailedConsentResource(consentID, clientID, receipt, resolvedConsentType, resolvedConsentStatus,
                resolvedFrequency, resolvedValidity, createdTime, updatedTime, resolvedRecurring,
                dto.getConsentAttributes(), new ArrayList<>(authResources), new ArrayList<>(mappingResources)
        );
    }

}
