package org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.constants.ConsentConstant;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthResponse;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceDTO;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceDTO;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResponse;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ReauthorizeResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.Resource;

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
    public static void copyPropertiesToConsentResource(ConsentResource consentResource,
                                                       ConsentResourceDTO consentResourceDTO) throws
            ConsentMgtException {
        consentResource.setConsentType(consentResourceDTO.getConsentType());
        consentResource.setClientID(consentResourceDTO.getClientID());
        consentResource.setRecurringIndicator(consentResourceDTO.getRecurringIndicator());
        consentResource.setValidityPeriod(consentResourceDTO.getValidityPeriod());
        consentResource.setConsentAttributes(ConsentUtils.convertToMap(consentResourceDTO.getConsentAttributes()));
        consentResource.setReceipt(consentResourceDTO.getReceipt());
        consentResource.setCurrentStatus(consentResourceDTO.getCurrentStatus());


    }

    /**
     * copy properties from authorizationResourceDTo to authorizationResource
     */
    public static void copyPropertiesToAuthorizationResource(AuthorizationResource authorizationResource,
                                                             AuthorizationResourceDTO authorizationResourceDTO) {
        authorizationResource.setAuthorizationType(authorizationResourceDTO.getAuthorizationType());
        authorizationResource.setAuthorizationStatus(authorizationResourceDTO.getAuthorizationStatus());
        authorizationResource.setUserID(authorizationResourceDTO.getUserId());
    }

    /**
     * copy properties from authorizationResourceDTo to authorizationResource
     */
    public static void copyPropertiesToAuthorizationResource(AuthorizationResource authorizationResource,
                                                             ReauthorizeResource authorizationResourceDTO) {
        authorizationResource.setAuthorizationID(authorizationResourceDTO.getAuthorizationId());
        authorizationResource.setAuthorizationType(authorizationResourceDTO.getAuthorizationType());
        authorizationResource.setAuthorizationStatus(authorizationResourceDTO.getAuthorizationStatus());
        authorizationResource.setUserID(authorizationResourceDTO.getUserId());
        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        for (Resource resource : authorizationResourceDTO.getResources()) {
            ConsentMappingResource consentMappingResource = new ConsentMappingResource();
            consentMappingResource.setResource(resource.getResource());
            consentMappingResource.setMappingID(resource.getResourceMappingId());
            consentMappingResource.setMappingStatus(resource.getConsentMappingStatus());
            consentMappingResources.add(consentMappingResource);

        }
        authorizationResource.setConsentMappingResource(consentMappingResources);

    }


    /**
     * copy properties from consentResource to consentResponse
     */
    public static void buildAuthorizationResourceResponse(AuthResponse authorizationResourceResponseResponse,
                                                          AuthorizationResource authorizationResource,
                                                          ArrayList<ConsentMappingResource> consentMappingResources) {
        authorizationResourceResponseResponse.setAuthId(authorizationResource.getAuthorizationID());
        authorizationResourceResponseResponse.setUserId(authorizationResource.getUserID());
        authorizationResourceResponseResponse.setAuthorizationStatus(authorizationResource.getAuthorizationStatus());
        authorizationResourceResponseResponse.setAuthorizationType(authorizationResource.getAuthorizationType());

        ArrayList<Resource> resources = new ArrayList<>();

        if (consentMappingResources != null) {
            for (ConsentMappingResource consentMappingResource : consentMappingResources) {
                Resource res = new Resource();
                buildConsentMappingResourceResponse(res, consentMappingResource);
                resources.add(res);
            }
        }

        authorizationResourceResponseResponse.setResources(resources);


    }

    /**
     * copy properties from consentResource to consentResponse
     */
    public static void buildAuthorizationResourceResponse(AuthorizationResource authorizationResourceResponseResponse,
                                                          AuthorizationResource authorizationResource,
                                                          ArrayList<ConsentMappingResource> consentMappingResources) {
        authorizationResourceResponseResponse.setAuthorizationID(authorizationResource.getAuthorizationID());
        authorizationResourceResponseResponse.setUserID(authorizationResource.getUserID());
        authorizationResourceResponseResponse.setAuthorizationStatus(authorizationResource.getAuthorizationStatus());
        authorizationResourceResponseResponse.setAuthorizationType(authorizationResource.getAuthorizationType());


        authorizationResourceResponseResponse.setConsentMappingResource(consentMappingResources);


    }

    /**
     * copy properties from consentResourceMapping  to consentResourceMappingResponse
     */
    public static void buildConsentMappingResourceResponse(Resource consentMappingResourceResponse,
                                                           ConsentMappingResource consentMappingResource) {
        consentMappingResourceResponse.setResourceMappingId(consentMappingResource.getMappingID());
        consentMappingResourceResponse.setResource(consentMappingResource.getResource());
        consentMappingResourceResponse.setConsentMappingStatus(consentMappingResource.getMappingStatus());

    }

    /**
     * copy properties to consentResourceResponse
     */
    public static void buildConsentResourceResponse(ConsentResponse consentResourceResponse,
                                                    DetailedConsentResource consentResource,
                                                    ArrayList<AuthorizationResource> authorizationResources,
                                                    ArrayList<ConsentMappingResource> consentMappingResources,
                                                    boolean withAttributes) {
        consentResourceResponse.setConsentID(consentResource.getConsentID());
        consentResourceResponse.setClientID(consentResource.getClientID());
        consentResourceResponse.setConsentType(consentResource.getConsentType());
        consentResourceResponse.setRecurringIndicator(consentResource.isRecurringIndicator());
        consentResourceResponse.setCreatedTime((int) consentResource.getCreatedTime());
        consentResourceResponse.setValidityPeriod((int) consentResource.getValidityPeriod());
        consentResourceResponse.setCurrentStatus(consentResource.getCurrentStatus());
        consentResourceResponse.setUpdatedTime((int) consentResource.getUpdatedTime());
        consentResourceResponse.setReceipt(consentResource.getReceipt());

        if (withAttributes) {
            consentResourceResponse.setConsentAttributes(consentResource.getConsentAttributes());

        }

        // get consent mapping resources for each AuthorizationResource
        Map<String, ArrayList<ConsentMappingResource>> consentMappingResourcesMap = new HashMap<>();
        for (ConsentMappingResource consentMappingResource : consentMappingResources) {
            if (!consentMappingResourcesMap.containsKey(consentMappingResource.getAuthorizationID())) {
                consentMappingResourcesMap.put(consentMappingResource.getAuthorizationID(),
                        new ArrayList<>());
            }
            consentMappingResourcesMap.get(consentMappingResource.getAuthorizationID()).add(consentMappingResource);
        }

        if (authorizationResources != null) {
            ArrayList<AuthResponse> authResponses = new ArrayList<>();
            for (AuthorizationResource authorizationResource : authorizationResources) {

                AuthResponse authResponse = new AuthResponse();
                buildAuthorizationResourceResponse(authResponse, authorizationResource,
                        consentMappingResourcesMap.get(authorizationResource.getAuthorizationID()));
                authResponses.add(authResponse);
            }
            consentResourceResponse.setAuthorizationResources(authResponses);
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
                detailedConsentResource.getConsentID());
        consentResource.put(ConsentConstant.CLIENT_ID,
                detailedConsentResource.getClientID());
        consentResource.put(ConsentConstant.RECEIPT,
                detailedConsentResource.getReceipt());

        consentResource.put(ConsentConstant.CONSENT_TYPE,
                detailedConsentResource.getConsentType());
        consentResource.put(ConsentConstant.CURRENT_STATUS,
                detailedConsentResource.getCurrentStatus());
        consentResource.put(ConsentConstant.CONSENT_FREQUENCY,
                detailedConsentResource.getConsentFrequency());
        consentResource.put(ConsentConstant.VALIDITY_PERIOD,
                detailedConsentResource.getValidityPeriod());
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
        ArrayList<AuthorizationResource> authArray = detailedConsentResource.getAuthorizationResources();
        for (AuthorizationResource resource : authArray) {
            JSONObject resourceJSON = new JSONObject();
            resourceJSON.put(ConsentConstant.AUTH_ID,
                    resource.getAuthorizationID());
            resourceJSON.put(ConsentConstant.CC_CONSENT_ID,
                    resource.getConsentID());
            resourceJSON.put(ConsentConstant.USER_ID,
                    resource.getUserID());
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
                    resource.getAuthorizationID());
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
    public static JSONObject consentResourceToJSON(ConsentResource consentResource) {
        JSONObject consentResourceJSON = new JSONObject();
        consentResourceJSON.put(ConsentConstant.CONSENT_ID,
                consentResource.getConsentID());
        consentResourceJSON.put(ConsentConstant.CLIENT_ID,
                consentResource.getClientID());
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
