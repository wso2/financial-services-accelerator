package org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.impl;

import net.minidev.json.JSONObject;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.constants.ConsentError;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.constants.ConsentMgtDAOConstants;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.model.AuthorizationResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.model.BulkConsentStatusUpdateResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.model.ConsentExpiryTimeUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.model.ConsentResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.model.ConsentRevokeRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.model.ConsentStatusUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.api.endpoint.utils.ConsentAPITestData;
import org.wso2.financial.services.accelerator.consent.mgt.api.service.impl.ConsentCoreServiceImpl;

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
import static org.mockito.Mockito.mockStatic;
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
    MockedStatic<ConsentCoreServiceImpl> consentCoreServiceImplMockedStatic;


    @BeforeClass
    public void setUp() throws
            Exception {
        mockedConsentCoreServiceImpl = mock(ConsentCoreServiceImpl.class);

        consentCoreServiceImplMockedStatic = mockStatic(ConsentCoreServiceImpl.class);
        consentCoreServiceImplMockedStatic.when(ConsentCoreServiceImpl::getInstance)
                .thenReturn(mockedConsentCoreServiceImpl);
        consentAPIImpl = ConsentAPIImpl.getInstance();
        sampleConsentID = String.valueOf(UUID.randomUUID());
        sampleOrgID = "org123";


        // return mocked consent core service when the service is called
//        consentAPIImpl.setConsentCoreService(mockedConsentCoreServiceImpl);

    }

    @Test
    public void testConsentConsentIdGetDetailedConsent() throws
            Exception {
        DetailedConsentResource detailedConsentResource = mock(DetailedConsentResource.class);
        doReturn(detailedConsentResource).when(mockedConsentCoreServiceImpl).getDetailedConsent(any(), any());
        when(detailedConsentResource.getOrgId()).thenReturn(sampleOrgID);

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
                new ConsentMgtException(ConsentError.CONSENT_NOT_FOUND));

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
        String orgId = "org123";

        doThrow(new ConsentMgtException(ConsentError.UNKNOWN_ERROR))
                .when(mockedConsentCoreServiceImpl).getDetailedConsent(any(), any());

        // Act
        Response response = consentAPIImpl.consentConsentIdGet(consentId, orgId);

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
                .createConsent(any());

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
        String orgId = null;
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
        when(mockedConsentCoreServiceImpl.createConsent(any())).thenReturn(
                detailedConsentResource);

        // Act
        Response response = consentAPIImpl.consentPost(consentResourceDTO, orgId);

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
                any())).thenThrow(new ConsentMgtException(ConsentError.CONSENT_SEARCH_ERROR));

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

        doThrow(new ConsentMgtException(ConsentError.CONSENT_NOT_FOUND))
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

        doNothing().when(mockedConsentCoreServiceImpl).consentStatusBulkUpdate(any(), any(), any(), any(),
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
        when(consentResource.getOrgId()).thenReturn(sampleOrgID); // Simulate mismatch

        doReturn(true).when(mockedConsentCoreServiceImpl).deleteConsent(any(), any());

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
                thenThrow(new ConsentMgtException(ConsentError.CONSENT_NOT_FOUND));

        // Act & Assert
        Response response = consentAPIImpl.consentConsentIdDelete(sampleConsentID, sampleOrgID);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());

    }

    @Test
    public void testConsentDeleteReturnsFalse() throws
            Exception {
        String orgId = "orgXYZ";
        String consentId = sampleConsentID;

        ConsentResource consentResource = mock(ConsentResource.class);
        when(consentResource.getOrgId()).thenReturn(orgId);
        when(mockedConsentCoreServiceImpl.deleteConsent(consentId, orgId)).thenReturn(false);

        Response response = consentAPIImpl.consentConsentIdDelete(consentId, orgId);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    // Test for consentRevokeConsentIdPut
    @Test
    public void testConsentRevokeConsentIdPutSuccess() throws
            ConsentMgtException {
        // Arrange
        String orgId = "orgXYZ";
        String userId = "user456";
        ConsentRevokeRequestBody updateResource = new ConsentRevokeRequestBody();
        updateResource.setReason("User request");
        updateResource.setUserId(userId);

        ConsentResource consentResource = new ConsentResource();
        consentResource.setOrgId(orgId);
        consentResource.setCurrentStatus("ACTIVE");

        doReturn(true).when(mockedConsentCoreServiceImpl).revokeConsent(any(), any(), any(), any());

        // Act
        Response response = consentAPIImpl.consentRevokeConsentIdPost(sampleConsentID, updateResource, orgId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    // missing orgId
    @Test
    public void testConsentRevokeConsentIdPutMissingOrgInfo() throws
            ConsentMgtException {
        // Arrange
        String orgId = null;
        String userId = "user456";
        ConsentRevokeRequestBody updateResource = new ConsentRevokeRequestBody();
        updateResource.setReason("User request");
        updateResource.setUserId(userId);

        ConsentResource consentResource = new ConsentResource();
        consentResource.setOrgId(orgId);

        consentResource.setCurrentStatus("ACTIVE");

        doThrow(new ConsentMgtException(ConsentError.DETAILED_CONSENT_NOT_FOUND)).when(mockedConsentCoreServiceImpl)
                .revokeConsent(any(),
                        any(), any(), any());

        // Act
        Response response = consentAPIImpl.consentRevokeConsentIdPost(sampleConsentID, updateResource, orgId);
        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());

    }

    @Test
    public void testConsentPostWithoutAuthorizationResources() throws
            Exception {
        // Arrange
        ConsentResourceRequestBody consentResourceDTO = mock(ConsentResourceRequestBody.class);
        String orgId = "org123";

        // Mock data setup
        Map<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put("consentFrequency", "30");
        when(consentResourceDTO.getConsentAttributes()).thenReturn(consentAttributes);
        when(consentResourceDTO.getAuthorizationResources()).thenReturn(null); // Missing resources
        // Act
        DetailedConsentResource detailedConsentResource = ConsentAPITestData.getStoredDetailedConsentResource();
        when(mockedConsentCoreServiceImpl.createConsent(any())).thenReturn(
                detailedConsentResource);
        Response response = consentAPIImpl.consentPost(consentResourceDTO, orgId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationAuthorizationIdGetSuccess() throws
            Exception {
        // Arrange
        String authorizationId = "auth123";
        String orgId = "orgXYZ";
        String consentId = "consent456";

        ConsentResource consentResource = mock(ConsentResource.class);
        AuthorizationResource authorizationResource = new AuthorizationResource();
        JSONObject responseJson = new JSONObject();
        responseJson.put("accountId", "test");
        authorizationResource.setResource(responseJson.toString());
        when(consentResource.getOrgId()).thenReturn(orgId);
        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgId)).thenReturn(
                authorizationResource);

        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, orgId, consentId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationAuthorizationIdGetNullOrgInfo() throws
            Exception {
        // Arrange
        String authorizationId = "auth123";
        String orgId = null; // Should default to `DEFAULT_ORG`
        String consentId = "consent456";

        ConsentResource consentResource = mock(ConsentResource.class);
        AuthorizationResource authorizationResource = new AuthorizationResource();
        JSONObject responseJson = new JSONObject();
        responseJson.put("accountId", "test");
        authorizationResource.setResource(responseJson.toString());
        when(consentResource.getOrgId()).thenReturn(ConsentMgtDAOConstants.DEFAULT_ORG);
        when(mockedConsentCoreServiceImpl.getAuthorizationResource(any(), any()))
                .thenReturn(authorizationResource);

        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, orgId, consentId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationAuthorizationIdGetInvalidAuthorizationId() throws
            Exception {
        // Arrange
        String authorizationId = "invalid-auth-id";
        String orgId = "orgXYZ";
        String consentId = "consent456";

        ConsentResource consentResource = mock(ConsentResource.class);
        when(consentResource.getOrgId()).thenReturn(orgId);

        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgId))
                .thenThrow(new ConsentMgtException(ConsentError.AUTHORIZATION_RESOURCE_NOT_FOUND));

        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, consentId, orgId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationAuthorizationIdGetCoreServiceException() throws
            Exception {
        // Arrange
        String authorizationId = "auth123";
        String orgId = "orgXYZ";
        String consentId = "consent456";

        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgId))
                .thenThrow(new ConsentMgtException(ConsentError.AUTHORIZATION_RESOURCE_RETRIEVAL_ERROR));

        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, consentId, orgId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationIdGetSuccess() throws
            Exception {
        String authorizationId = "auth123";
        String orgId = "orgXYZ";
        String consentId = sampleConsentID;

        ConsentResource consentResource = mock(ConsentResource.class);
        AuthorizationResource authorizationResource = new AuthorizationResource();
        JSONObject responseJson = new JSONObject();
        responseJson.put("accountId", "test");
        authorizationResource.setResource(responseJson.toString());
        when(consentResource.getOrgId()).thenReturn(orgId);
        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgId)).thenReturn(
                authorizationResource);

        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, orgId, consentId);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentRevokeAlreadyRevoked() throws
            Exception {
        String consentId = sampleConsentID;
        String orgId = "orgXYZ";

        ConsentRevokeRequestBody updateResource = new ConsentRevokeRequestBody();
        updateResource.setReason("Already revoked");
        updateResource.setUserId("user123");

        ConsentResource consentResource = new ConsentResource();
        consentResource.setCurrentStatus("revoked");
        consentResource.setOrgId(orgId);

        doThrow(new ConsentMgtException(ConsentError.CONSENT_ALREADY_REVOKED_ERROR)).when(
                mockedConsentCoreServiceImpl).revokeConsent(any(),
                any(),
                any(),
                any());
        Response response = consentAPIImpl.consentRevokeConsentIdPost(consentId, updateResource, orgId);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationIdPostSuccess() throws
            Exception {
        String consentId = "consent123";
        String orgId = "orgXYZ";

        AuthorizationResourceRequestBody authorizationResourceDTO = mock(AuthorizationResourceRequestBody.class);
        ArrayList<AuthorizationResourceRequestBody> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResourceDTO);

        DetailedConsentResource detailedConsentResource = mock(DetailedConsentResource.class);
        doReturn(detailedConsentResource).when(mockedConsentCoreServiceImpl).getDetailedConsent(consentId, orgId);

        ArrayList<AuthorizationResource> authorizationResources1 = new ArrayList<>();

        doReturn(authorizationResources1).when(mockedConsentCoreServiceImpl)
                .createConsentAuthorizations(any(), any());

        Response response = consentAPIImpl.consentAuthorizationIdPost(consentId, authorizationResources, orgId);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetAuthorizationInvalidId() throws
            Exception {
        // Arrange
        String authorizationId = "invalid-auth-id";
        String orgId = "orgXYZ";
        String consentId = "consent456";

        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgId))
                .thenThrow(new ConsentMgtException(ConsentError.AUTHORIZATION_RESOURCE_NOT_FOUND));

        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, consentId, orgId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAuthorizationInternalServerError() throws
            Exception {
        // Arrange
        String authorizationId = "auth123";
        String orgId = "orgXYZ";
        String consentId = "consent456";

        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgId))
                .thenThrow(new ConsentMgtException(ConsentError.AUTHORIZATION_RESOURCE_RETRIEVAL_ERROR));

        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, consentId, orgId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationIdPutSuccess() throws
            Exception {
        String authorizationId = "auth123";
        String consentId = "consent123";
        String orgId = "orgXYZ";

        AuthorizationResourceRequestBody authorizationResourceDTO = mock(AuthorizationResourceRequestBody.class);
        ConsentResource consentResource = mock(ConsentResource.class);
        when(consentResource.getOrgId()).thenReturn(orgId);
        when(mockedConsentCoreServiceImpl.getDetailedConsent(consentId, orgId)).thenReturn(
                mock(DetailedConsentResource.class));

        doNothing().when(mockedConsentCoreServiceImpl)
                .updateAuthorizationResource(eq(authorizationId), any(), eq(orgId));

        Response response =
                consentAPIImpl.consentAuthorizationIdPut(authorizationId, consentId, authorizationResourceDTO, orgId);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationIdPutException() throws
            Exception {
        String authorizationId = "auth123";
        String consentId = "consent123";
        String orgId = "orgXYZ";

        AuthorizationResourceRequestBody authorizationResourceDTO = mock(AuthorizationResourceRequestBody.class);

        // Simulate an exception being thrown by the mocked service
        doThrow(new ConsentMgtException(ConsentError.UNKNOWN_ERROR))
                .when(mockedConsentCoreServiceImpl)
                .updateAuthorizationResource(eq(authorizationId), any(), eq(orgId));

        // Act
        Response response =
                consentAPIImpl.consentAuthorizationIdPut(authorizationId, consentId, authorizationResourceDTO, orgId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationIdDeleteSuccess() throws
            Exception {
        String authorizationId = "auth123";
        String consentId = "consent123";
        String orgId = "orgXYZ";

        when(mockedConsentCoreServiceImpl.getDetailedConsent(consentId, orgId)).
                thenReturn(mock(DetailedConsentResource.class));

        when(mockedConsentCoreServiceImpl.deleteAuthorizationResource(authorizationId)).thenReturn(true);

        Response response = consentAPIImpl.consentAuthorizationIdDelete(authorizationId, consentId, orgId);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentConsentIdExpiryTimePutException() throws
            Exception {
        String consentId = "consent123";
        String orgId = "orgXYZ";

        ConsentExpiryTimeUpdateRequestBody expiryTimeUpdateDTO = new ConsentExpiryTimeUpdateRequestBody();
        expiryTimeUpdateDTO.setExpiryTime(1672531200000L); // Example timestamp

        // Simulate an exception being thrown by the mocked service
        doThrow(new ConsentMgtException(ConsentError.UNKNOWN_ERROR))
                .when(mockedConsentCoreServiceImpl)
                .updateConsentExpiryTime(consentId, expiryTimeUpdateDTO.getExpiryTime(), orgId);

        // Act
        Response response = consentAPIImpl.consentConsentIdExpiryTimePut(consentId, expiryTimeUpdateDTO, orgId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testConsentConsentIdExpiryTimePutInvalidConsentId() throws
            Exception {
        String invalidConsentId = "invalid-consent-id";
        String orgId = "orgXYZ";

        ConsentExpiryTimeUpdateRequestBody expiryTimeUpdateDTO = new ConsentExpiryTimeUpdateRequestBody();
        expiryTimeUpdateDTO.setExpiryTime(1672531200000L); // Example timestamp

        // Simulate an exception being thrown for an invalid consent ID
        doThrow(new ConsentMgtException(ConsentError.CONSENT_NOT_FOUND))
                .when(mockedConsentCoreServiceImpl)
                .updateConsentExpiryTime(invalidConsentId, expiryTimeUpdateDTO.getExpiryTime(), orgId);

        // Act
        Response response =
                consentAPIImpl.consentConsentIdExpiryTimePut(invalidConsentId, expiryTimeUpdateDTO, orgId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testHandleConsentMgtExceptionBadRequest() throws
            Exception {
        // Arrange
        ConsentMgtException exception = new ConsentMgtException(ConsentError.INVALID_CONSENT_EXPIRY_TIME);

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
        ConsentMgtException exception = new ConsentMgtException(ConsentError.CONSENT_NOT_FOUND);

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
        ConsentMgtException exception =
                new ConsentMgtException(ConsentError.UNKNOWN_ERROR);

        // Invoke private method via reflection
        Method method = ConsentAPIImpl.class.getDeclaredMethod("handleConsentMgtException", ConsentMgtException.class);
        method.setAccessible(true);
        Response response = (Response) method.invoke(consentAPIImpl, exception);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

    }

}






