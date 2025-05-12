package org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.constants.ConsentConstant;

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
