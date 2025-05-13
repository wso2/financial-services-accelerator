package org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl;

import net.minidev.json.JSONObject;
import org.mockito.Mock;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentError;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl.utils.ConsentAPITestData;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.BulkConsentStatusUpdateResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentExpiryTimeUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentRevokeRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentStatusUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * ConsentAPIImplTest class.
 */
public class ConsentAPIImplTests {

    private ConsentAPIImpl consentAPIImpl;

    @Mock
    private ConsentCoreServiceImpl mockedConsentCoreServiceImpl;
    private String sampleConsentID;
    private String sampleOrgID;


    @BeforeClass
    public void setUp() {

        consentAPIImpl = new ConsentAPIImpl();
        sampleConsentID = String.valueOf(UUID.randomUUID());
        sampleOrgID = "org123";


        mockedConsentCoreServiceImpl = mock(ConsentCoreServiceImpl.class);

        // return mocked consent core service when the service is called
        consentAPIImpl.setConsentCoreService(mockedConsentCoreServiceImpl);

    }


    @Test
    public void testConsentConsentIdGetDetailedConsent() throws
            Exception {
        DetailedConsentResource detailedConsentResource = mock(DetailedConsentResource.class);
        doReturn(detailedConsentResource).when(mockedConsentCoreServiceImpl).getDetailedConsent(any(), any());
        when(detailedConsentResource.getOrgInfo()).thenReturn(sampleOrgID);

        // Act
        Response response =
                consentAPIImpl.consentConsentIdGet(sampleConsentID, sampleOrgID);

        // Assert
        assertNotNull(response, "Response should not be null");

    }


    // Test the case when the Consent ID is not found
    @Test
    public void testConsentIdNotFound() throws
            Exception {
        // Arrange
        String consentId = "invalid";

        when(mockedConsentCoreServiceImpl.getDetailedConsent(any(), any())).thenThrow(
                new ConsentMgtException(Response.Status.NOT_FOUND,
                        ConsentError.CONSENT_NOT_FOUND));

        // Act
        Response response = consentAPIImpl.consentConsentIdGet(consentId, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }


    @Test
    public void testConsentConsentIdGetCoreServiceException() throws
            Exception {
        // Arrange
        String consentId = sampleConsentID;
        String orgInfo = "org123";


        doThrow(new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, ConsentError.UNKNOWN_ERROR))
                .when(mockedConsentCoreServiceImpl).getDetailedConsent(any(), any());

        // Act
        Response response = consentAPIImpl.consentConsentIdGet(consentId, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }


    @Test
    public void testConsentPostWithAuthResources() throws
            Exception {

        ConsentResourceRequestBody consentResourceDTO = mock(ConsentResourceRequestBody.class);

        Map<String, String> consentAttributes = new HashMap<>();
        when(consentResourceDTO.getConsentAttributes()).thenReturn(consentAttributes);
        AuthorizationResourceRequestBody
                authorizationResourceDTO = mock(AuthorizationResourceRequestBody.class);
        ArrayList<AuthorizationResourceRequestBody> authorizationResources = new ArrayList<>();
        authorizationResourceDTO.setAuthorizationStatus("testStatus");
        authorizationResourceDTO.setAuthorizationType("testType");
        authorizationResourceDTO.setUserId("testUser");
        authorizationResources.add(authorizationResourceDTO);
        when(consentResourceDTO.getAuthorizationResources()).thenReturn(authorizationResources);

        // Mock service call response
        DetailedConsentResource detailedConsentResource = mock(DetailedConsentResource.class);
        doReturn(detailedConsentResource).when(mockedConsentCoreServiceImpl)
                .createConsent(any(), any());


        // Act
        Response response = consentAPIImpl.consentPost(consentResourceDTO, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testConsentPostWithNullOrg() throws
            Exception {
        // Arrange
        ConsentResourceRequestBody consentResourceDTO = mock(ConsentResourceRequestBody.class);
        String orgInfo = null;
        boolean isImplicitAuth = true;

        // Mock data setup
        Map<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put("consentFrequency", "30");
        when(consentResourceDTO.getConsentAttributes()).thenReturn(consentAttributes);

        AuthorizationResourceRequestBody
                authorizationResourceDTO = mock(AuthorizationResourceRequestBody.class);
        ArrayList<AuthorizationResourceRequestBody> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResourceDTO);
        when(consentResourceDTO.getAuthorizationResources()).thenReturn(authorizationResources);

        // Mock service call response
        DetailedConsentResource detailedConsentResource = mock(DetailedConsentResource.class);
        when(mockedConsentCoreServiceImpl.createConsent(any(), any())).thenReturn(
                detailedConsentResource);

        // Act
        Response response = consentAPIImpl.consentPost(consentResourceDTO, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }


    @Test
    public void testConsentGetWithFilters() throws
            ConsentMgtException {

        String consentType = "TypeA,TypeB";
        String consentStatus = "Active,Inactive";
        String userId = "user123";
        long fromTimeValue = 1616160000L;
        long toTimeValue = 1616163600L;
        int limitValue = 10;
        int offsetValue = 0;

        // Mock response from service
        ArrayList<DetailedConsentResource> results = new ArrayList<>();
        DetailedConsentResource resource = mock(DetailedConsentResource.class);
        results.add(resource);
        when(mockedConsentCoreServiceImpl.searchDetailedConsents(
                eq(sampleOrgID),
                any(),
                any(),
                any(),
                any(),
                any(),
                eq(fromTimeValue),
                eq(toTimeValue),
                eq(limitValue),
                eq(offsetValue))).thenReturn(results);

        // Act
        Response response = consentAPIImpl.consentGet(sampleOrgID, consentType, consentStatus,
                userId, ConsentAPITestData.testClientID, fromTimeValue, toTimeValue, limitValue, offsetValue);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

    }

    @Test
    public void testConsentGetWithoutFilters() throws
            ConsentMgtException {

        String consentType = null;
        String consentStatus = null;
        String userId = null;
        long fromTimeValue = 0L;
        long toTimeValue = 0L;
        int limitValue = 0;
        int offsetValue = 0;

        // Mock response from service
        ArrayList<DetailedConsentResource> results = new ArrayList<>();
        DetailedConsentResource resource = mock(DetailedConsentResource.class);
        results.add(resource);
        doReturn(results).when(mockedConsentCoreServiceImpl).searchDetailedConsents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),

                any(),
                any(),
                any(),
                any());

        // Act
        Response response = consentAPIImpl.consentGet(sampleOrgID, consentType, consentStatus,
                userId, ConsentAPITestData.testClientID, fromTimeValue, toTimeValue, limitValue, offsetValue);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

    }

    @Test
    public void testConsentGetWithInvalidTimeRange() throws
            ConsentMgtException {

        String consentType = "TypeA";
        String consentStatus = "Active";
        String userId = "user123";
        long fromTimeValue = -1616160000L; // Invalid negative value
        long toTimeValue = -1616163600L; // Invalid negative value
        int limitValue = 10;
        int offsetValue = 0;

        // Mock response from service
        ArrayList<DetailedConsentResource> results = new ArrayList<>();
        DetailedConsentResource resource = mock(DetailedConsentResource.class);
        results.add(resource);
        when(mockedConsentCoreServiceImpl.searchDetailedConsents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(), any(),
                any(),
                any())).thenThrow(new ConsentMgtException(
                Response.Status.INTERNAL_SERVER_ERROR,
                ConsentError.CONSENT_SEARCH_ERROR));

        // Act
        Response response = consentAPIImpl.consentGet(sampleOrgID, consentType, consentStatus,
                userId, ConsentAPITestData.testClientID, fromTimeValue, toTimeValue, limitValue, offsetValue);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

    }

    @Test
    public void testConsentGetWithNoResults() throws
            ConsentMgtException {

        String consentType = "TypeX";
        String consentStatus = "Inactive";
        String userId = "user123";
        long fromTimeValue = 1616160000L;
        long toTimeValue = 1616163600L;
        int limitValue = 10;
        int offsetValue = 0;

        // Mock empty response
        ArrayList<DetailedConsentResource> results = new ArrayList<>();
        doReturn(results).when(mockedConsentCoreServiceImpl).searchDetailedConsents(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(), any(),
                any(),
                any());


        // Act
        Response response = consentAPIImpl.consentGet(sampleOrgID, consentType, consentStatus,
                userId, ConsentAPITestData.testClientID, fromTimeValue, toTimeValue, limitValue, offsetValue);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentGetWithLimitAndOffset() throws
            ConsentMgtException {

        String consentType = "TypeA";
        String consentStatus = "Active";
        String userId = "user123";
        long fromTimeValue = 1616160000L;
        long toTimeValue = 1616163600L;
        int limitValue = 10;
        int offsetValue = 20;

        // Mock response from service
        ArrayList<DetailedConsentResource> results = new ArrayList<>();
        DetailedConsentResource resource = mock(DetailedConsentResource.class);
        results.add(resource);

        doReturn(results).when(mockedConsentCoreServiceImpl).searchDetailedConsents(
                eq(sampleOrgID),
                any(),
                any(),
                any(),
                any(),
                any(),

                eq(fromTimeValue),
                eq(toTimeValue),
                eq(limitValue),
                eq(offsetValue));


        // Act
        Response response = consentAPIImpl.consentGet(sampleOrgID, consentType, consentStatus,
                userId, ConsentAPITestData.testClientID, fromTimeValue, toTimeValue, limitValue, offsetValue);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

    }


    @Test
    public void testConsentConsentIdStatusPutSuccess() throws
            ConsentMgtException {

        ConsentStatusUpdateRequestBody updateResource = new ConsentStatusUpdateRequestBody();

        doNothing().when(mockedConsentCoreServiceImpl).updateConsentStatus(
                sampleConsentID, updateResource.getStatus(), updateResource.getReason(), updateResource.getUserId(),
                sampleOrgID);

        // Act
        Response response = consentAPIImpl.consentConsentIdStatusPut(sampleConsentID, updateResource, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

    }


    @Test
    public void testConsentConsentIdStatusPutConsentNotFound() throws
            Exception {

        ConsentStatusUpdateRequestBody updateResource = new ConsentStatusUpdateRequestBody();


        doThrow(new ConsentMgtException(Response.Status.NOT_FOUND,
                ConsentError.CONSENT_NOT_FOUND))
                .when(mockedConsentCoreServiceImpl).updateConsentStatus(
                        sampleConsentID, updateResource.getStatus(), updateResource.getReason(),
                        updateResource.getUserId()
                        , sampleOrgID);

        // Act & Assert
        Response response = consentAPIImpl.consentConsentIdStatusPut(sampleConsentID, updateResource, sampleOrgID);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());

    }


    // unit tests for consentStatusPut
    @Test
    public void testConsentStatusPutBulkSuccess() throws
            ConsentMgtException {

        doNothing().when(mockedConsentCoreServiceImpl).bulkUpdateConsentStatus(any(), any(), any(), any(),
                any(), any(), any());
        BulkConsentStatusUpdateResourceRequestBody
                bulkConsentStatusUpdateResource = mock(BulkConsentStatusUpdateResourceRequestBody.class);

        Response response = consentAPIImpl.consentStatusPut(bulkConsentStatusUpdateResource, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(response.getEntity(), "Status Updated");


    }


    @Test
    public void testConsentConsentIdDeleteSuccess() throws
            ConsentMgtException {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(consentResource).when(mockedConsentCoreServiceImpl).getDetailedConsent(any(), any());
        when(consentResource.getOrgInfo()).thenReturn(sampleOrgID); // Simulate mismatch

        doReturn(true).when(mockedConsentCoreServiceImpl).deleteConsent(any());

        // Act
        Response response = consentAPIImpl.consentConsentIdDelete(sampleConsentID, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentConsentIdDeleteConsentNotFound() throws
            ConsentMgtException {

        when(mockedConsentCoreServiceImpl.getDetailedConsent(sampleConsentID, sampleOrgID)).
                thenThrow(new ConsentMgtException(
                        Response.Status.NOT_FOUND, ConsentError.CONSENT_NOT_FOUND));

        // Act & Assert
        Response response = consentAPIImpl.consentConsentIdDelete(sampleConsentID, sampleOrgID);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());

    }

    @Test
    public void testConsentDeleteReturnsFalse() throws
            Exception {
        String orgInfo = "orgXYZ";
        String consentId = sampleConsentID;

        ConsentResource consentResource = mock(ConsentResource.class);
        when(consentResource.getOrgInfo()).thenReturn(orgInfo);
        when(mockedConsentCoreServiceImpl.deleteConsent(consentId)).thenReturn(false);

        Response response = consentAPIImpl.consentConsentIdDelete(consentId, orgInfo);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }


    // Test for consentRevokeConsentIdPut
    @Test
    public void testConsentRevokeConsentIdPutSuccess() throws
            ConsentMgtException {
        // Arrange
        String orgInfo = "orgXYZ";
        String userId = "user456";
        ConsentRevokeRequestBody updateResource = new ConsentRevokeRequestBody();
        updateResource.setReason("User request");
        updateResource.setUserId(userId);

        ConsentResource consentResource = new ConsentResource();
        consentResource.setOrgID(orgInfo);
        consentResource.setCurrentStatus("ACTIVE");



        doReturn(true).when(mockedConsentCoreServiceImpl).revokeConsent(any(), any(), any(), any());

        // Act
        Response response = consentAPIImpl.consentRevokeConsentIdPost(sampleConsentID, updateResource, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    // missing orgInfo
    @Test
    public void testConsentRevokeConsentIdPutMissingOrgInfo() throws
            ConsentMgtException {
        // Arrange
        String orgInfo = null;
        String userId = "user456";
        ConsentRevokeRequestBody updateResource = new ConsentRevokeRequestBody();
        updateResource.setReason("User request");
        updateResource.setUserId(userId);

        ConsentResource consentResource = new ConsentResource();
        consentResource.setOrgID(orgInfo);

        consentResource.setCurrentStatus("ACTIVE");


        doThrow(new ConsentMgtException(Response.Status.NOT_FOUND,
                ConsentError.DETAILED_CONSENT_NOT_FOUND)).when(mockedConsentCoreServiceImpl).revokeConsent(any(),
                any(), any(), any());

        // Act
        Response response = consentAPIImpl.consentRevokeConsentIdPost(sampleConsentID, updateResource, orgInfo);
        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());


    }


    @Test
    public void testConsentPostWithoutAuthorizationResources() throws
            Exception {
        // Arrange
        ConsentResourceRequestBody consentResourceDTO = mock(ConsentResourceRequestBody.class);
        String orgInfo = "org123";

        // Mock data setup
        Map<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put("consentFrequency", "30");
        when(consentResourceDTO.getConsentAttributes()).thenReturn(consentAttributes);
        when(consentResourceDTO.getAuthorizationResources()).thenReturn(null); // Missing resources
        // Act
        DetailedConsentResource detailedConsentResource = ConsentAPITestData.getStoredDetailedConsentResource();
        when(mockedConsentCoreServiceImpl.createConsent(any(), any())).thenReturn(
                detailedConsentResource);
        Response response = consentAPIImpl.consentPost(consentResourceDTO, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }


    @Test
    public void testConsentAuthorizationAuthorizationIdGetSuccess() throws
            Exception {
        // Arrange
        String authorizationId = "auth123";
        String orgInfo = "orgXYZ";
        String consentId = "consent456";

        ConsentResource consentResource = mock(ConsentResource.class);
        AuthorizationResource authorizationResource = new AuthorizationResource();
        JSONObject responseJson = new JSONObject();
        responseJson.put("accountId", "test");
        authorizationResource.setResource(responseJson.toString());
        when(consentResource.getOrgInfo()).thenReturn(orgInfo);
        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgInfo)).thenReturn(
                authorizationResource);

        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, orgInfo, consentId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationAuthorizationIdGetNullOrgInfo() throws
            Exception {
        // Arrange
        String authorizationId = "auth123";
        String orgInfo = null; // Should default to `DEFAULT_ORG`
        String consentId = "consent456";

        ConsentResource consentResource = mock(ConsentResource.class);
        AuthorizationResource authorizationResource = new AuthorizationResource();
        JSONObject responseJson = new JSONObject();
        responseJson.put("accountId", "test");
        authorizationResource.setResource(responseJson.toString());
        when(consentResource.getOrgInfo()).thenReturn(ConsentMgtDAOConstants.DEFAULT_ORG);
        when(mockedConsentCoreServiceImpl.getAuthorizationResource(any(), any()))
                .thenReturn(authorizationResource);


        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, orgInfo, consentId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationAuthorizationIdGetInvalidAuthorizationId() throws
            Exception {
        // Arrange
        String authorizationId = "invalid-auth-id";
        String orgInfo = "orgXYZ";
        String consentId = "consent456";

        ConsentResource consentResource = mock(ConsentResource.class);
        when(consentResource.getOrgInfo()).thenReturn(orgInfo);

        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgInfo))
                .thenThrow(new ConsentMgtException(Response.Status.NOT_FOUND,
                        ConsentError.AUTHORIZATION_RESOURCE_NOT_FOUND));

        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, consentId, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationAuthorizationIdGetCoreServiceException() throws
            Exception {
        // Arrange
        String authorizationId = "auth123";
        String orgInfo = "orgXYZ";
        String consentId = "consent456";

        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgInfo))
                .thenThrow(new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentError.AUTHORIZATION_RESOURCE_RETRIEVAL_ERROR));

        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, consentId, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }


    @Test
    public void testConsentAuthorizationIdGetSuccess() throws
            Exception {
        String authorizationId = "auth123";
        String orgInfo = "orgXYZ";
        String consentId = sampleConsentID;

        ConsentResource consentResource = mock(ConsentResource.class);
        AuthorizationResource authorizationResource = new AuthorizationResource();
        JSONObject responseJson = new JSONObject();
        responseJson.put("accountId", "test");
        authorizationResource.setResource(responseJson.toString());
        when(consentResource.getOrgInfo()).thenReturn(orgInfo);
        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgInfo)).thenReturn(
                authorizationResource);

        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, orgInfo, consentId);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }




    @Test
    public void testConsentRevokeAlreadyRevoked() throws
            Exception {
        String consentId = sampleConsentID;
        String orgInfo = "orgXYZ";

        ConsentRevokeRequestBody updateResource = new ConsentRevokeRequestBody();
        updateResource.setReason("Already revoked");
        updateResource.setUserId("user123");

        ConsentResource consentResource = new ConsentResource();
        consentResource.setCurrentStatus("revoked");
        consentResource.setOrgID(orgInfo);


        doThrow(new ConsentMgtException(Response.Status.BAD_REQUEST, ConsentError.CONSENT_ALREADY_REVOKED_ERROR)).when(
                mockedConsentCoreServiceImpl).revokeConsent(any(),
                any(),
                any(),
                any());
        Response response = consentAPIImpl.consentRevokeConsentIdPost(consentId, updateResource, orgInfo);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationIdPostSuccess() throws
            Exception {
        String consentId = "consent123";
        String orgInfo = "orgXYZ";

        AuthorizationResourceRequestBody authorizationResourceDTO = mock(AuthorizationResourceRequestBody.class);
        ArrayList<AuthorizationResourceRequestBody> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResourceDTO);

        DetailedConsentResource detailedConsentResource = mock(DetailedConsentResource.class);
        doReturn(detailedConsentResource).when(mockedConsentCoreServiceImpl).getDetailedConsent(consentId, orgInfo);

        doReturn(mock(AuthorizationResource.class)).when(mockedConsentCoreServiceImpl)
                .createConsentAuthorization(any());

        Response response = consentAPIImpl.consentAuthorizationIdPost(consentId, authorizationResources, orgInfo);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }


    @Test
    public void testGetAuthorizationInvalidId() throws
            Exception {
        // Arrange
        String authorizationId = "invalid-auth-id";
        String orgInfo = "orgXYZ";
        String consentId = "consent456";

        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgInfo))
                .thenThrow(new ConsentMgtException(Response.Status.NOT_FOUND,
                        ConsentError.AUTHORIZATION_RESOURCE_NOT_FOUND));


        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId,  consentId, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAuthorizationInternalServerError() throws
            Exception {
        // Arrange
        String authorizationId = "auth123";
        String orgInfo = "orgXYZ";
        String consentId = "consent456";

        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgInfo))
                .thenThrow(new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentError.AUTHORIZATION_RESOURCE_RETRIEVAL_ERROR));

        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, consentId, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationIdPutSuccess() throws
            Exception {
        String authorizationId = "auth123";
        String consentId = "consent123";
        String orgInfo = "orgXYZ";

        AuthorizationResourceRequestBody authorizationResourceDTO = mock(AuthorizationResourceRequestBody.class);
        ConsentResource consentResource = mock(ConsentResource.class);
        when(consentResource.getOrgInfo()).thenReturn(orgInfo);
        when(mockedConsentCoreServiceImpl.getDetailedConsent(consentId, orgInfo)).thenReturn(
                mock(DetailedConsentResource.class));

        doNothing().when(mockedConsentCoreServiceImpl)
                .updateAuthorizationResource(eq(authorizationId), any(), eq(orgInfo));

        Response response =
                consentAPIImpl.consentAuthorizationIdPut(authorizationId, consentId, authorizationResourceDTO, orgInfo);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationIdPutException() throws
            Exception {
        String authorizationId = "auth123";
        String consentId = "consent123";
        String orgInfo = "orgXYZ";

        AuthorizationResourceRequestBody authorizationResourceDTO = mock(AuthorizationResourceRequestBody.class);

        // Simulate an exception being thrown by the mocked service
        doThrow(new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, ConsentError.UNKNOWN_ERROR))
                .when(mockedConsentCoreServiceImpl)
                .updateAuthorizationResource(eq(authorizationId), any(), eq(orgInfo));

        // Act
        Response response =
                consentAPIImpl.consentAuthorizationIdPut(authorizationId, consentId, authorizationResourceDTO, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }


    @Test
    public void testConsentAuthorizationIdDeleteSuccess() throws
            Exception {
        String authorizationId = "auth123";
        String consentId = "consent123";
        String orgInfo = "orgXYZ";

        when(mockedConsentCoreServiceImpl.getDetailedConsent(consentId, orgInfo)).
                thenReturn(mock(DetailedConsentResource.class));

        when(mockedConsentCoreServiceImpl.deleteAuthorizationResource(authorizationId)).thenReturn(true);

        Response response = consentAPIImpl.consentAuthorizationIdDelete(authorizationId, consentId, orgInfo);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentConsentIdExpiryTimePutException() throws
            Exception {
        String consentId = "consent123";
        String orgInfo = "orgXYZ";

        ConsentExpiryTimeUpdateRequestBody expiryTimeUpdateDTO = new ConsentExpiryTimeUpdateRequestBody();
        expiryTimeUpdateDTO.setExpiryTime(1672531200000L); // Example timestamp

        // Simulate an exception being thrown by the mocked service
        doThrow(new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, ConsentError.UNKNOWN_ERROR))
                .when(mockedConsentCoreServiceImpl)
                .updateConsentExpiryTime(consentId, expiryTimeUpdateDTO.getExpiryTime(), orgInfo);

        // Act
        Response response = consentAPIImpl.consentConsentIdExpiryTimePut(consentId, expiryTimeUpdateDTO, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testConsentConsentIdExpiryTimePutInvalidConsentId() throws
            Exception {
        String invalidConsentId = "invalid-consent-id";
        String orgInfo = "orgXYZ";

        ConsentExpiryTimeUpdateRequestBody expiryTimeUpdateDTO = new ConsentExpiryTimeUpdateRequestBody();
        expiryTimeUpdateDTO.setExpiryTime(1672531200000L); // Example timestamp

        // Simulate an exception being thrown for an invalid consent ID
        doThrow(new ConsentMgtException(Response.Status.NOT_FOUND, ConsentError.CONSENT_NOT_FOUND))
                .when(mockedConsentCoreServiceImpl)
                .updateConsentExpiryTime(invalidConsentId, expiryTimeUpdateDTO.getExpiryTime(), orgInfo);

        // Act
        Response response =
                consentAPIImpl.consentConsentIdExpiryTimePut(invalidConsentId, expiryTimeUpdateDTO, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }


    @Test
    public void testHandleConsentMgtExceptionBadRequest() throws
            Exception {
        // Arrange
        ConsentAPIImpl consentAPIImpl = new ConsentAPIImpl();
        ConsentMgtException exception = new ConsentMgtException(Response.Status.BAD_REQUEST,
                ConsentError.INVALID_CONSENT_EXPIRY_TIME);

        // Invoke private method via reflection
        Method method = ConsentAPIImpl.class.getDeclaredMethod("handleConsentMgtException", ConsentMgtException.class);
        method.setAccessible(true);
        Response response = (Response) method.invoke(consentAPIImpl, exception);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void testHandleConsentMgtExceptionNotFound() throws
            Exception {
        // Arrange
        ConsentAPIImpl consentAPIImpl = new ConsentAPIImpl();
        ConsentMgtException exception = new ConsentMgtException(Response.Status.NOT_FOUND,
                ConsentError.CONSENT_NOT_FOUND);

        // Invoke private method via reflection
        Method method = ConsentAPIImpl.class.getDeclaredMethod("handleConsentMgtException", ConsentMgtException.class);
        method.setAccessible(true);
        Response response = (Response) method.invoke(consentAPIImpl, exception);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());

    }

    @Test
    public void testHandleConsentMgtExceptionInternalServerError() throws
            Exception {
        // Arrange
        ConsentAPIImpl consentAPIImpl = new ConsentAPIImpl();
        ConsentMgtException exception =
                new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, ConsentError.UNKNOWN_ERROR);

        // Invoke private method via reflection
        Method method = ConsentAPIImpl.class.getDeclaredMethod("handleConsentMgtException", ConsentMgtException.class);
        method.setAccessible(true);
        Response response = (Response) method.invoke(consentAPIImpl, exception);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

    }


}






