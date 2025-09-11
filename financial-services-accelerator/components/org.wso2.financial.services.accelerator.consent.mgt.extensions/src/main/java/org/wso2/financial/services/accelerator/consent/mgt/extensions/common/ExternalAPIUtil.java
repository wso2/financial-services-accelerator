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
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.AmendedResources;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentPersistData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentPersistRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentPersistResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceResponseDTO;

import java.util.ArrayList;
import java.util.HashMap;
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

        throw new ConsentException(ResponseStatus.fromStatusCode(httpErrorCode), responseData);
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

        if (response == null) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Error occurred while calling the external service");
        }

        try {
            httpErrorCode = response.getErrorCode();
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
     * @param responseConsentResource The consent resource received from the external API pre-consent step.
     * @param consentResource         The base consent resource from the persistence layer.
     * @return A fully populated {@link DetailedConsentResource}.
     */
    public static DetailedConsentResource constructDetailedConsentResource(
            ExternalAPIConsentResourceResponseDTO responseConsentResource, ConsentResource consentResource,
            String primaryAuthId, String primaryUserId) {

        String consentID = consentResource.getConsentID();
        long updatedTime = System.currentTimeMillis() / 1000;

        List<AuthorizationResource> authorizationResources =
                buildAuthorizationResources(responseConsentResource.getAuthorizations(), consentID, primaryAuthId,
                        primaryUserId, updatedTime);

        List<ConsentMappingResource> consentMappingResources =
                buildConsentMappingResources(responseConsentResource.getAuthorizations(), authorizationResources);

        return buildDetailedConsentResource(responseConsentResource, consentResource, authorizationResources,
                consentMappingResources);
    }

    /**
     * Constructs a {@link DetailedConsentResource} by merging data from
     * {@link ExternalAPIPreConsentPersistResponseDTO} and {@link ConsentPersistData}.
     * <p>
     * Fields in the DTO are prioritized; if null, corresponding values from the base consent are used.
     *
     * @param responseConsentResource The consent resource received from the external API pre-consent step.
     * @param consentData             Consent data.
     * @return A fully populated {@link DetailedConsentResource}.
     */
    public static DetailedConsentResource constructDetailedConsentResource(
            ExternalAPIConsentResourceResponseDTO responseConsentResource, ConsentData consentData) {

        String consentID = consentData.getConsentId();
        String clientID = consentData.getClientId();
        String receipt = new JSONObject(responseConsentResource.getReceipt()).toString();
        long createdTime = System.currentTimeMillis() / 1000;
        long updatedTime = System.currentTimeMillis() / 1000;

        // Add common auth ID to consent attributes if available.
        Object commonAuthId = consentData.getMetaDataMap().get(ConsentExtensionConstants.COMMON_AUTH_ID);
        if (commonAuthId != null) {
            if (responseConsentResource.getAttributes() == null) {
                responseConsentResource.setAttributes(new HashMap<>());
            }
            responseConsentResource.getAttributes().put(ConsentExtensionConstants.COMMON_AUTH_ID,
                    commonAuthId.toString());
        }

        List<AuthorizationResource> authorizationResources =
                buildAuthorizationResources(responseConsentResource.getAuthorizations(), consentID, null,
                        null, updatedTime);

        List<ConsentMappingResource> consentMappingResources =
                buildConsentMappingResources(responseConsentResource.getAuthorizations(), authorizationResources);

        return buildDetailedConsentResource(responseConsentResource, authorizationResources,
                consentMappingResources, consentID, clientID, receipt, createdTime, updatedTime);
    }

    /**
     * Constructs a {@link DetailedConsentResource} using {@link ExternalAPIPreConsentPersistResponseDTO}.
     * <p>
     *
     * @param responseConsentResource The consent resource received from the external API pre-consent step.
     * @return A fully populated {@link DetailedConsentResource}.
     */
    public static DetailedConsentResource constructDetailedConsentResource(
            ExternalAPIConsentResourceResponseDTO responseConsentResource, String clientId) {

        String consentID = UUID.randomUUID().toString();
        String receipt = new JSONObject(responseConsentResource.getReceipt()).toString();
        long createdTime = System.currentTimeMillis() / 1000;
        long updatedTime = System.currentTimeMillis() / 1000;

        List<AuthorizationResource> authorizationResources =
                buildAuthorizationResources(responseConsentResource.getAuthorizations(), consentID, null,
                        null, updatedTime);

        List<ConsentMappingResource> consentMappingResources =
                buildConsentMappingResources(responseConsentResource.getAuthorizations(), authorizationResources);

        return buildDetailedConsentResource(responseConsentResource, authorizationResources,
                consentMappingResources, consentID, clientId, receipt, createdTime, updatedTime);
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
            List<ExternalAPIConsentResourceResponseDTO.Authorization> authorizations,
            String consentID, String primaryAuthId, String primaryUserId, long updatedTime) {

        List<AuthorizationResource> authResources = new ArrayList<>();

        if (authorizations != null) {
            for (ExternalAPIConsentResourceResponseDTO.Authorization authorization : authorizations) {
                AuthorizationResource authResource = new AuthorizationResource();

                /* Set existing primary auth ID if available. This is for initiated consents
                   where an authorization is already available.*/
                if (primaryUserId != null && primaryAuthId != null && primaryUserId.equals(authorization.getUserId())) {
                    authResource.setAuthorizationID(primaryAuthId);
                } else {
                    authResource.setAuthorizationID(UUID.randomUUID().toString());
                }

                authResource.setConsentID(consentID);
                authResource.setUserID(authorization.getUserId());
                authResource.setAuthorizationStatus(authorization.getStatus());
                authResource.setAuthorizationType(authorization.getType());
                authResource.setUpdatedTime(updatedTime);
                authResources.add(authResource);
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
            List<ExternalAPIConsentResourceResponseDTO.Authorization> authorizations,
            List<AuthorizationResource> authResources) {

        List<ConsentMappingResource> mappingResources = new ArrayList<>();

        if (authorizations != null) {
            for (int i = 0; i < authorizations.size(); i++) {
                ExternalAPIConsentResourceResponseDTO.Authorization dtoAuth = authorizations.get(i);
                String authID = authResources.get(i).getAuthorizationID();

                if (dtoAuth.getResources() != null) {
                    for (ExternalAPIConsentResourceResponseDTO.Resource resource : dtoAuth.getResources()) {
                        ConsentMappingResource mapping = new ConsentMappingResource();
                        mapping.setAuthorizationID(authID);
                        mapping.setAccountID(resource.getAccountId());
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
     * @param responseConsentResource The consent resource received from the external API pre-consent step.
     * @param consentResource         The fallback source of values.
     * @param authResources           List of resolved authorization entries.
     * @param mappingResources        List of resolved account mapping entries.
     * @return A fully constructed {@link DetailedConsentResource}.
     */
    private static DetailedConsentResource buildDetailedConsentResource(
            ExternalAPIConsentResourceResponseDTO responseConsentResource, ConsentResource consentResource,
            List<AuthorizationResource> authResources, List<ConsentMappingResource> mappingResources) {

        String consentID = consentResource.getConsentID();
        String clientID = consentResource.getClientID();
        long createdTime = consentResource.getCreatedTime();
        long updatedTime = System.currentTimeMillis() / 1000;

        String resolvedConsentType = (responseConsentResource.getType() != null) ? responseConsentResource.getType() :
                consentResource.getConsentType();
        String resolvedConsentStatus = (responseConsentResource.getStatus() != null) ?
                responseConsentResource.getStatus() : consentResource.getCurrentStatus();
        String resolvedReceipt = (responseConsentResource.getReceipt() != null) ?
                new JSONObject(responseConsentResource.getReceipt()).toString() : consentResource.getReceipt();
        int resolvedFrequency = (responseConsentResource.getFrequency() != null) ?
                responseConsentResource.getFrequency() : consentResource.getConsentFrequency();
        long resolvedValidity = (responseConsentResource.getValidityTime() != null) ?
                responseConsentResource.getValidityTime() : consentResource.getValidityPeriod();
        boolean resolvedRecurring = (responseConsentResource.getRecurringIndicator() != null) ?
                responseConsentResource.getRecurringIndicator() : consentResource.isRecurringIndicator();

        return new DetailedConsentResource(consentID, clientID, resolvedReceipt, resolvedConsentType,
                resolvedConsentStatus, resolvedFrequency, resolvedValidity, createdTime, updatedTime,
                resolvedRecurring, responseConsentResource.getAttributes(), new ArrayList<>(authResources),
                new ArrayList<>(mappingResources)
        );
    }

    /**
     * Combines all resolved data into a final {@link DetailedConsentResource}.
     *
     * @param responseConsentResource The consent resource received from the external API pre-consent step.
     * @param authResources           List of resolved authorization entries.
     * @param mappingResources        List of resolved account mapping entries.
     * @param consentID               Consent ID.
     * @param clientID                Client ID.
     * @param receipt                 Receipt JSON string.
     * @param createdTime             Consent creation timestamp.
     * @param updatedTime             Consent update timestamp.
     * @return A fully constructed {@link DetailedConsentResource}.
     */
    private static DetailedConsentResource buildDetailedConsentResource(
            ExternalAPIConsentResourceResponseDTO responseConsentResource, List<AuthorizationResource> authResources,
            List<ConsentMappingResource> mappingResources, String consentID, String clientID, String receipt,
            long createdTime, long updatedTime) {

        String resolvedConsentType = responseConsentResource.getType();
        String resolvedConsentStatus = responseConsentResource.getStatus();
        int resolvedFrequency = responseConsentResource.getFrequency();
        long resolvedValidity = responseConsentResource.getValidityTime();
        boolean resolvedRecurring = responseConsentResource.getRecurringIndicator();

        return new DetailedConsentResource(consentID, clientID, receipt, resolvedConsentType, resolvedConsentStatus,
                resolvedFrequency, resolvedValidity, createdTime, updatedTime, resolvedRecurring,
                responseConsentResource.getAttributes(), new ArrayList<>(authResources),
                new ArrayList<>(mappingResources)
        );
    }

    /**
     * Create request object to be sent to the external service using ExternalAPIPreConsentPersistRequestDTO.
     *
     * @param requestDTO ExternalAPIPreConsentPersistRequestDTO object
     * @return ExternalServiceRequest object
     */
    public static ExternalServiceRequest createExternalServiceRequest(
            ExternalAPIPreConsentPersistRequestDTO requestDTO) {

        JSONObject requestJson = new JSONObject(requestDTO);
        return new ExternalServiceRequest(UUID.randomUUID().toString(), requestJson);
    }

    /**
     * Constructs the amended resources from the amended authorizations.
     *
     * @param amendedAuthorizations List of amended authorizations
     * @return AmendedResources object containing the amended authorizations and mapping resources
     */
    public static AmendedResources constructAmendedResources(
            List<ExternalAPIConsentResourceResponseDTO.AmendedAuthorization> amendedAuthorizations) {

        AmendedResources amendedResources = new AmendedResources();
        List<AuthorizationResource> amendedAuthResources = new ArrayList<>();
        List<ConsentMappingResource> newMappingResources = new ArrayList<>();
        List<ConsentMappingResource> amendedMappingResources = new ArrayList<>();

        for (ExternalAPIConsentResourceResponseDTO.AmendedAuthorization amendedAuthorization :
                amendedAuthorizations) {
            String authorizationId = amendedAuthorization.getId();
            AuthorizationResource amendedAuthResource = constructAmendedAuthorizationResource(amendedAuthorization);
            amendedAuthResources.add(amendedAuthResource);

            // New mapping resources
            for (ExternalAPIConsentResourceResponseDTO.Resource newMappingResource :
                    amendedAuthorization.getResources()) {
                ConsentMappingResource consentMappingResource = constructNewMappingResource(newMappingResource,
                        authorizationId);
                newMappingResources.add(consentMappingResource);
            }

            // Amended mapping resources
            for (ExternalAPIConsentResourceResponseDTO.AmendedResource amendedMappingResource :
                    amendedAuthorization.getAmendedResources()) {
                ConsentMappingResource consentMappingResource = constructAmendedMappingResource(
                        amendedMappingResource, authorizationId);
                amendedMappingResources.add(consentMappingResource);
            }
        }
        amendedResources.setAmendedAuthResources(amendedAuthResources);
        amendedResources.setNewMappingResources(newMappingResources);
        amendedResources.setAmendedMappingResources(amendedMappingResources);

        return amendedResources;
    }

    /**
     * Constructs the amended authorization resource from the amended authorization.
     *
     * @param amendedAuthorization The amended authorization
     * @return AuthorizationResource object
     */
    private static AuthorizationResource constructAmendedAuthorizationResource(
            ExternalAPIConsentResourceResponseDTO.AmendedAuthorization amendedAuthorization) {

        AuthorizationResource resource = new AuthorizationResource();
        resource.setAuthorizationID(amendedAuthorization.getId());
        resource.setAuthorizationType(amendedAuthorization.getType());
        resource.setAuthorizationStatus(amendedAuthorization.getStatus());
        return resource;
    }

    /**
     * Constructs a ConsentMappingResource object for new consent mappings.
     *
     * @param newMappingResource New mapping resource
     * @param authorizationId    Authorization ID the mapping belongs to
     * @return ConsentMappingResource object
     */
    private static ConsentMappingResource constructNewMappingResource(
            ExternalAPIConsentResourceResponseDTO.Resource newMappingResource, String authorizationId) {

        ConsentMappingResource resource = new ConsentMappingResource();
        resource.setAuthorizationID(authorizationId);
        resource.setAccountID(newMappingResource.getAccountId());
        resource.setPermission(newMappingResource.getPermission());
        resource.setMappingStatus(newMappingResource.getStatus());
        return resource;
    }

    /**
     * Constructs a ConsentMappingResource object for amended consent mappings.
     *
     * @param amendedMappingResource The amended mapping resource
     * @param authorizationId        Authorization ID the mapping belongs to
     * @return ConsentMappingResource object
     */
    private static ConsentMappingResource constructAmendedMappingResource(
            ExternalAPIConsentResourceResponseDTO.AmendedResource amendedMappingResource, String authorizationId) {

        ConsentMappingResource resource = new ConsentMappingResource();
        resource.setAuthorizationID(authorizationId);
        resource.setMappingID(amendedMappingResource.getId());
        resource.setPermission(amendedMappingResource.getPermission());
        resource.setMappingStatus(amendedMappingResource.getStatus());
        return resource;
    }

}
