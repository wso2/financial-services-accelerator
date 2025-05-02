package org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl.utils;

import net.minidev.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;

import java.util.ArrayList;

public class ConsentAPITestData {

    public static String testConsentID = "testConsentID";
    public static String testClientID = "testClientID";
    public static String testUserID = "testUser";
    public static String testConsentType = "testType";
    public static String testConsentStatus = "testStatus";
    public static String testReceipt = "testReceipt";
    public static int testExpiryTime = 10;

    // authorization resource
    public static String testAuthStatus = "testAuthStatus";
    public static String testAuthId = "testAuthId";
    public static String testAuthType = "testAuthType";

    // detailed consent mapping
    public static String testMappingId = "testMappingId";
    public static String testMappingStatus = "testMappingStatus";
    public static JSONObject testMappingResource = new JSONObject(){
        {
            put("mappingId", testMappingId);
            put("mappingStatus", testMappingStatus);
            put("mappingResource", "testMappingResource");
        }
    };



    public static AuthorizationResource testStoredAuthorizationResource = getStoredAuthorizationResource();

    // mock DetailedConsentResource
    public static DetailedConsentResource getStoredDetailedConsentResource() {
        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setClientID(testClientID);
        detailedConsentResource.setReceipt(testReceipt);
        detailedConsentResource.setConsentID(testConsentID);
        detailedConsentResource.setConsentType(testConsentType);
        detailedConsentResource.setCurrentStatus(testConsentStatus);
        detailedConsentResource.setExpiryTime(testExpiryTime);
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(getStoredAuthorizationResource());
        detailedConsentResource.setAuthorizationResources(authorizationResources);
        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(getStoredConsentMappingResource());
        detailedConsentResource.setConsentMappingResources(consentMappingResources);
        return detailedConsentResource;
    }

    ;

    // mock AuthorizationResource
    public static AuthorizationResource getStoredAuthorizationResource() {
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationID(testAuthId);
        authorizationResource.setUserID(testUserID);
        authorizationResource.setAuthorizationType(testAuthType);
        authorizationResource.setAuthorizationStatus(testAuthStatus);
        authorizationResource.setResource(testMappingResource.toString());

        return authorizationResource;
    }

    // get stored consent mapping resource
    public static ConsentMappingResource getStoredConsentMappingResource() {
        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setMappingID(testMappingId);
        consentMappingResource.setResource(testMappingResource);

        return consentMappingResource;
    }

}
