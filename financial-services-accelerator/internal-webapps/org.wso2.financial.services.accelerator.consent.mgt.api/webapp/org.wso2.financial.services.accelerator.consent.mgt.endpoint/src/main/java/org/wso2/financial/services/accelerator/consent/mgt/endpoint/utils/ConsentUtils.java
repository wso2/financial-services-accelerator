package org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.constants.ConsentConstant;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceResponseBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;


/**
 * ConsentUtils.
 */
public class ConsentUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Validate the consent ID.
     *
     * @param consentId Consent Id to validate
     * @return Whether the consent ID is valid
     */
    public static boolean isConsentIdValid(String consentId) {
        return (Pattern.matches(ConsentConstant.UUID_REGEX,
                consentId));
    }


    public static Map<String, String> convertToMap(Object obj) throws
            ConsentMgtException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = objectMapper.convertValue(obj, Map.class);
            if (map == null) {
                return new HashMap<>();
            }
            return map.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
        } catch (IllegalArgumentException e) {
            throw new ConsentMgtException(Response.Status.BAD_REQUEST, "Invalid consent attributes");
        }
    }

    /**
     * copy propoerties from consentResource DTO to consentResource DAO
     *
     * @param consentResourceDTO
     */
    public static void copyPropertiesToConsentResource(
            org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource consentResource,
            ConsentResourceRequestBody consentResourceDTO) throws
            ConsentMgtException {
        consentResource.setConsentType(consentResourceDTO.getConsentType());
        consentResource.setClientId(consentResourceDTO.getClientId());
        consentResource.setRecurringIndicator(consentResourceDTO.getRecurringIndicator());
        consentResource.setExpiryTime(consentResourceDTO.getExpiryTime());
        consentResource.setConsentAttributes(ConsentUtils.convertToMap(consentResourceDTO.getConsentAttributes()));
        consentResource.setReceipt(consentResourceDTO.getReceipt());
        consentResource.setCurrentStatus(consentResourceDTO.getCurrentStatus());


    }

    /**
     * copy properties from authorizationResourceDTo to authorizationResource
     */
    public static void copyPropertiesToAuthorizationResource(
            org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource authorizationResource,
            AuthorizationResourceRequestBody authorizationResourceDTO) throws
            JsonProcessingException {
        authorizationResource.setAuthorizationType(authorizationResourceDTO.getAuthorizationType());
        authorizationResource.setAuthorizationStatus(authorizationResourceDTO.getAuthorizationStatus());
        authorizationResource.setUserId(authorizationResourceDTO.getUserId());
        authorizationResource.setResource(objectMapper.writeValueAsString(authorizationResourceDTO.getResource()));
    }


    /**
     * copy properties from consentResource to consentResponse
     */
    public static void buildAuthorizationResourceResponse(
            AuthorizationResourceResponseBody authorizationResourceResponse,
            AuthorizationResource authorizationResource)
            throws
            JsonProcessingException {
        authorizationResourceResponse.setAuthId(authorizationResource.getAuthorizationId());
        authorizationResourceResponse.setUserId(authorizationResource.getUserId());
        authorizationResourceResponse.setAuthorizationStatus(authorizationResource.getAuthorizationStatus());
        authorizationResourceResponse.setAuthorizationType(authorizationResource.getAuthorizationType());


        authorizationResourceResponse.setResource(
                new net.minidev.json.JSONObject(objectMapper.readValue(
                        authorizationResource.getResource(), new TypeReference<Map<String, Object>>() {
                        })));
    }

    /**
     * copy properties from consentResource to consentResponse
     */
    public static void buildAuthorizationResourceResponse(
            AuthorizationResource authorizationResourceResponseResponse,
            AuthorizationResource authorizationResource,
            ArrayList<ConsentMappingResource> consentMappingResources) {
        authorizationResourceResponseResponse.setAuthorizationId(authorizationResource.getAuthorizationId());
        authorizationResourceResponseResponse.setUserId(authorizationResource.getUserId());
        authorizationResourceResponseResponse.setAuthorizationStatus(authorizationResource.getAuthorizationStatus());
        authorizationResourceResponseResponse.setAuthorizationType(authorizationResource.getAuthorizationType());


    }


    /**
     * copy properties to consentResourceResponseBody from consentResource and authorizationResource
     */
    public static void buildConsentResourceResponse(ConsentResourceResponseBody consentResourceResponseBody,
                                                    DetailedConsentResource consentResource,
                                                    ArrayList<AuthorizationResource> authorizationResources) throws
            JsonProcessingException {
        consentResourceResponseBody.setConsentId(consentResource.getConsentId());
        consentResourceResponseBody.setClientId(consentResource.getClientId());
        consentResourceResponseBody.setConsentType(consentResource.getConsentType());
        consentResourceResponseBody.setRecurringIndicator(consentResource.isRecurringIndicator());
        consentResourceResponseBody.setCreatedTime((int) consentResource.getCreatedTime());
        consentResourceResponseBody.setExpiryTime((int) consentResource.getExpiryTime());
        consentResourceResponseBody.setCurrentStatus(consentResource.getCurrentStatus());
        consentResourceResponseBody.setUpdatedTime((int) consentResource.getUpdatedTime());
        consentResourceResponseBody.setReceipt(consentResource.getReceipt());
        consentResourceResponseBody.setConsentAttributes(consentResource.getConsentAttributes());

        if (authorizationResources != null) {
            ArrayList<AuthorizationResourceResponseBody> authResponses = new ArrayList<>();
            for (AuthorizationResource authorizationResource : authorizationResources) {

                AuthorizationResourceResponseBody authResponse = new AuthorizationResourceResponseBody();
                buildAuthorizationResourceResponse(authResponse, authorizationResource);
                authResponses.add(authResponse);
            }
            consentResourceResponseBody.setAuthorizationResources(authResponses);
        }
    }


    /**
     * Convert detailed consent resource to JSON.
     *
     * @param detailedConsentResource detailed consent resource
     * @return JSON object constructed from the detailed consent resource
     */
    public static JSONObject detailedConsentToJSON(DetailedConsentResource detailedConsentResource) {
        JSONObject consentResource = new JSONObject();

        consentResource.put(ConsentConstant.CC_CONSENT_ID,
                detailedConsentResource.getConsentId());
        consentResource.put(ConsentConstant.CLIENT_ID,
                detailedConsentResource.getClientId());
        consentResource.put(ConsentConstant.RECEIPT,
                detailedConsentResource.getReceipt());

        consentResource.put(ConsentConstant.CONSENT_TYPE,
                detailedConsentResource.getConsentType());
        consentResource.put(ConsentConstant.CURRENT_STATUS,
                detailedConsentResource.getCurrentStatus());

        consentResource.put(ConsentConstant.VALIDITY_PERIOD,
                detailedConsentResource.getExpiryTime());
        consentResource.put(ConsentConstant.CREATED_TIMESTAMP,
                detailedConsentResource.getCreatedTime());
        consentResource.put(ConsentConstant.UPDATED_TIMESTAMP,
                detailedConsentResource.getUpdatedTime());
        consentResource.put(ConsentConstant.RECURRING_INDICATOR,
                detailedConsentResource.isRecurringIndicator());
        JSONObject attributes = new JSONObject();
        Map<String, String> attMap = detailedConsentResource.getConsentAttributes();
        for (Map.Entry<String, String> entry : attMap.entrySet()) {
            attributes.put(entry.getKey(),
                    entry.getValue());
        }
        consentResource.put(ConsentConstant.CONSENT_ATTRIBUTES,
                attributes);
        JSONArray authorizationResources = new JSONArray();
        ArrayList<org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource> authArray =
                detailedConsentResource.getAuthorizationResources();
        for (org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource resource :
                authArray) {
            JSONObject resourceJSON = new JSONObject();
            resourceJSON.put(ConsentConstant.AUTH_ID,
                    resource.getAuthorizationId());
            resourceJSON.put(ConsentConstant.CC_CONSENT_ID,
                    resource.getConsentId());
            resourceJSON.put(ConsentConstant.USER_ID,
                    resource.getUserId());
            resourceJSON.put(ConsentConstant.AUTH_STATUS,
                    resource.getAuthorizationStatus());
            resourceJSON.put(ConsentConstant.AUTH_TYPE,
                    resource.getAuthorizationType());
            resourceJSON.put(ConsentConstant.UPDATE_TIME,
                    resource.getUpdatedTime());
            authorizationResources.put(resourceJSON);
        }
        consentResource.put(ConsentConstant.AUTH_RESOURCES,
                authorizationResources);
        JSONArray consentMappingResources = new JSONArray();
        ArrayList<ConsentMappingResource> mappingArray = detailedConsentResource.getConsentMappingResources();
        for (ConsentMappingResource resource : mappingArray) {
            JSONObject resourceJSON = new JSONObject();
            resourceJSON.put(ConsentConstant.MAPPING_ID,
                    resource.getMappingID());
            resourceJSON.put(ConsentConstant.AUTH_ID,
                    resource.getAuthorizationId());
            resourceJSON.put(ConsentConstant.ACCOUNT_ID,
                    resource.getAccountID());
            resourceJSON.put(ConsentConstant.PERMISSION,
                    resource.getPermission());
            resourceJSON.put(ConsentConstant.MAPPING_STATUS,
                    resource.getMappingStatus());
            consentMappingResources.put(resourceJSON);
        }
        consentResource.put(ConsentConstant.MAPPING_RESOURCES,
                consentMappingResources);
        return consentResource;
    }

    /**
     * Convert detailed consent resource to JSON.
     *
     * @param consentResource consent resource
     * @return JSON object constructed from the  consent resource
     */
    public static JSONObject consentResourceToJSON(
            org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource consentResource) {
        JSONObject consentResourceJSON = new JSONObject();
        consentResourceJSON.put(ConsentConstant.CONSENT_ID,
                consentResource.getConsentId());
        consentResourceJSON.put(ConsentConstant.CLIENT_ID,
                consentResource.getClientId());
        consentResourceJSON.put(ConsentConstant.CONSENT_TYPE,
                consentResource.getConsentType());
        consentResourceJSON.put(ConsentConstant.CURRENT_STATUS,
                consentResource.getCurrentStatus());
        consentResourceJSON.put(ConsentConstant.RECURRING_INDICATOR,
                consentResource.isRecurringIndicator());
        consentResourceJSON.put(ConsentConstant.CONSENT_ATTRIBUTES,
                consentResource.getConsentAttributes());
        consentResourceJSON.put(ConsentConstant.CREATED_TIME,
                consentResource.getCreatedTime());
        return consentResourceJSON;
    }


}
