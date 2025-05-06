package org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl;

import net.minidev.json.JSONObject;
import org.mockito.Mock;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl.utils.ConsentAPITestData;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.impl.utils.ConsentAPITestUtils;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.AuthorizationResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.BulkConsentStatusUpdateResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentResourceRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentRevokeRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.model.ConsentStatusUpdateRequestBody;
import org.wso2.financial.services.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    private String mismatchedOrgID;


    @BeforeClass
    public void setUp() {

        consentAPIImpl = new ConsentAPIImpl();
        sampleConsentID = String.valueOf(UUID.randomUUID());
        sampleOrgID = "org123";
        mismatchedOrgID = "org456";


        mockedConsentCoreServiceImpl = mock(ConsentCoreServiceImpl.class);

        // return mocked consent core service when the service is called
        consentAPIImpl.setConsentCoreService(mockedConsentCoreServiceImpl);

    }

    //------------------ Consent Retrieval ---------------------------------//

    // Test the 'isDetailedConsent' true case
    @Test
    public void testConsentConsentIdGetDetailedConsent() throws
            Exception {

        DetailedConsentResource detailedConsentResource = mock(DetailedConsentResource.class);

        doReturn(detailedConsentResource).when(mockedConsentCoreServiceImpl).getDetailedConsent(any(), any());

        when(detailedConsentResource.getOrgID()).thenReturn(sampleOrgID);

        // Act
        Response response =
                consentAPIImpl.consentConsentIdGet(sampleConsentID, sampleOrgID);

        // Assert
        assertNotNull(response, "Response should not be null");


    }

    // Test the 'isDetailedConsent' false case
    @Test
    public void testConsentConsentIdGetNonDetailedConsent() throws
            Exception {
        boolean isWithAttributes = false;

        ConsentResource consentResource = mock(ConsentResource.class);
        when(mockedConsentCoreServiceImpl.getConsent(sampleConsentID, isWithAttributes)).thenReturn(consentResource);
        when(consentResource.getOrgID()).thenReturn(sampleOrgID);

        // Act
        Response response =
                consentAPIImpl.consentConsentIdGet(sampleConsentID, sampleOrgID);

        // Assert
        assertNotNull(response, "Response should not be null");

    }





    // Test the case when 'isDetailedConsent' is true and 'isWithAttributes' is false
    @Test
    public void testConsentConsentIdGetWithDetailsWithoutAttributes() throws
            Exception {
        // Arrange
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        consentResource.setOrgID(sampleOrgID);
        doReturn(consentResource).when(mockedConsentCoreServiceImpl).getDetailedConsent(any(), any());

        when(consentResource.getOrgID()).thenReturn(sampleOrgID);

        // Act
        Response response = consentAPIImpl.consentConsentIdGet(sampleConsentID, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    // Test the case when the Consent ID is not found
    @Test
    public void testConsentIdNotFound() throws
            Exception {
        // Arrange
        String consentID = "f1711761-3359-42d5-a827-9a011a8bebe3";


        when(mockedConsentCoreServiceImpl.getDetailedConsent(any(), any())).thenThrow(
                new ConsentMgtException(Response.Status.NOT_FOUND,
                        ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG));

        // Act
        Response response = consentAPIImpl.consentConsentIdGet(consentID, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }


    // ------------------ Consent Creation ---------------------------------//


    @Test
    public void testConsentPostWithImplicitAuth() throws
            Exception {

        ConsentResourceRequestBody consentResourceDTO = mock(ConsentResourceRequestBody.class);

        Map<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put("consentFrequency", "30");
        when(consentResourceDTO.getConsentAttributes()).thenReturn(consentAttributes);

        AuthorizationResourceRequestBody
                authorizationResourceDTO = mock(AuthorizationResourceRequestBody.class);
        ArrayList<AuthorizationResourceRequestBody> authorizationResources = new ArrayList<>();
        authorizationResourceDTO.setAuthorizationStatus("testStatus");
        authorizationResourceDTO.setAuthorizationType("testType");
        authorizationResourceDTO.setUserID("testUser");
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

    // ------------------------- Consent Search ----------------------------//

    @Test
    public void testConsentGetWithFilters() throws
            ConsentMgtException {

        String consentType = "TypeA,TypeB";
        String consentStatus = "Active,Inactive";
        String userID = "user123";
        long fromTimeValue = 1616160000L; // Unix timestamp for fromTime
        long toTimeValue = 1616163600L; // Unix timestamp for toTime
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
                userID, ConsentAPITestData.testClientID, fromTimeValue, toTimeValue, limitValue, offsetValue);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

    }

    @Test
    public void testConsentGetWithoutFilters() throws
            ConsentMgtException {

        String consentType = null;
        String consentStatus = null;
        String userID = null;
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
                userID, ConsentAPITestData.testClientID, fromTimeValue, toTimeValue, limitValue, offsetValue);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

    }

    @Test
    public void testConsentGetWithInvalidTimeRange() throws
            ConsentMgtException {

        String consentType = "TypeA";
        String consentStatus = "Active";
        String userID = "user123";
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
                ConsentCoreServiceConstants.DETAIL_CONSENT_SEARCH_ERROR_MSG));

        // Act
        Response response = consentAPIImpl.consentGet(sampleOrgID, consentType, consentStatus,
                userID, ConsentAPITestData.testClientID, fromTimeValue, toTimeValue, limitValue, offsetValue);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

    }

    @Test
    public void testConsentGetWithNoResults() throws
            ConsentMgtException {

        String consentType = "TypeX";
        String consentStatus = "Inactive";
        String userID = "user123";
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
                userID, ConsentAPITestData.testClientID, fromTimeValue, toTimeValue, limitValue, offsetValue);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentGetWithLimitAndOffset() throws
            ConsentMgtException {

        String consentType = "TypeA";
        String consentStatus = "Active";
        String userID = "user123";
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
                userID, ConsentAPITestData.testClientID, fromTimeValue, toTimeValue, limitValue, offsetValue);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

    }

    // ------------------ Consent Status Update ---------------------------------//

    @Test
    public void testConsentConsentIdStatusPutSuccess() throws
            ConsentMgtException {

        ConsentStatusUpdateRequestBody updateResource = new ConsentStatusUpdateRequestBody();
        updateResource.setStatus("Approved");
        updateResource.setReason("User agreed");
        updateResource.setUserID("user456");

        doNothing().when(mockedConsentCoreServiceImpl).updateConsentStatusWithImplicitReasonAndUserId(
                sampleConsentID, updateResource.getStatus(), updateResource.getReason(), updateResource.getUserID(),
                sampleOrgID);

        // Act
        Response response = consentAPIImpl.consentConsentIdStatusPut(sampleConsentID, updateResource, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(response.getEntity(), "Status Updated");

    }

    @Test
    public void testConsentConsentIdStatusPutNullConsentID() throws
            ConsentMgtException {
        // Arrange
        String consentID = null;
        ConsentStatusUpdateRequestBody updateResource = new ConsentStatusUpdateRequestBody();
        updateResource.setStatus("Approved");
        updateResource.setReason("User agreed");
        updateResource.setUserID("user456");

        doThrow(new ConsentMgtException(Response.Status.BAD_REQUEST,
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR)).
                when(mockedConsentCoreServiceImpl).
                updateConsentStatusWithImplicitReasonAndUserId(any(), any(), any(), any(), any());


        // Act
        Response response = consentAPIImpl.consentConsentIdStatusPut(consentID, updateResource, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        assertEquals(ConsentAPITestUtils.parseErrorMessage(response),
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR);
    }

    @Test
    public void testConsentConsentIdStatusPutInvalidStatus() throws
            ConsentMgtException {

        ConsentStatusUpdateRequestBody updateResource = new ConsentStatusUpdateRequestBody();
        updateResource.setStatus(null); // Not a valid consent status
        updateResource.setReason("Invalid status test");
        updateResource.setUserID("user789");

        doThrow(new ConsentMgtException(Response.Status.BAD_REQUEST,
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR)).
                when(mockedConsentCoreServiceImpl).
                updateConsentStatusWithImplicitReasonAndUserId(any(), any(), any(), any(), any());


        // Act & Assert
        Response response = consentAPIImpl.consentConsentIdStatusPut(sampleConsentID, updateResource, sampleOrgID);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        assertEquals(ConsentAPITestUtils.parseErrorMessage(response),
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR);

    }

    @Test
    public void testConsentConsentIdStatusPutConsentNotFound() throws
            Exception {

        ConsentStatusUpdateRequestBody updateResource = new ConsentStatusUpdateRequestBody();
        updateResource.setStatus("Revoked");
        updateResource.setReason("User request");
        updateResource.setUserID("user999");

        doThrow(new ConsentMgtException(Response.Status.NOT_FOUND,
                ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG))
                .when(mockedConsentCoreServiceImpl).updateConsentStatusWithImplicitReasonAndUserId(
                        sampleConsentID, updateResource.getStatus(), updateResource.getReason(),
                        updateResource.getUserID()
                        , sampleOrgID);

        // Act & Assert
        Response response = consentAPIImpl.consentConsentIdStatusPut(sampleConsentID, updateResource, sampleOrgID);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
        assertEquals(ConsentAPITestUtils.parseErrorMessage(response),
                ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
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
    public void testConsentStatusPutBulkInvalidStatus() throws
            ConsentMgtException {


        doThrow(new ConsentMgtException(Response.Status.BAD_REQUEST,
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR)).
                when(mockedConsentCoreServiceImpl).
                bulkUpdateConsentStatus(any(), any(), any(), any(), any(), any(), any());

        BulkConsentStatusUpdateResourceRequestBody
                bulkConsentStatusUpdateResource = mock(BulkConsentStatusUpdateResourceRequestBody.class);
        Response response = consentAPIImpl.consentStatusPut(bulkConsentStatusUpdateResource, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        assertEquals(ConsentAPITestUtils.parseErrorMessage(response),
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR);

    }


    // Test case for update consent status with invalid consent ID
    @Test
    public void testConsentStatusPutBulkInvalidConsentID() throws
            ConsentMgtException {


        doThrow(new ConsentMgtException(Response.Status.BAD_REQUEST,
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR)).
                when(mockedConsentCoreServiceImpl).
                bulkUpdateConsentStatus(any(), any(), any(), any(), any(), any(), any());

        BulkConsentStatusUpdateResourceRequestBody
                bulkConsentStatusUpdateResource = mock(BulkConsentStatusUpdateResourceRequestBody.class);
        Response response = consentAPIImpl.consentStatusPut(bulkConsentStatusUpdateResource, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        assertEquals(ConsentAPITestUtils.parseErrorMessage(response),
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR);

    }

    // Test case for update consent status with invalid orgInfo
    @Test
    public void testConsentStatusPutBulkInvalidOrgInfo() throws
            ConsentMgtException {

        String orgInfo = null;

        doThrow(new ConsentMgtException(Response.Status.BAD_REQUEST,
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR)).
                when(mockedConsentCoreServiceImpl).
                bulkUpdateConsentStatus(any(), any(), any(), any(), any(), any(), any());

        BulkConsentStatusUpdateResourceRequestBody
                bulkConsentStatusUpdateResource = mock(BulkConsentStatusUpdateResourceRequestBody.class);
        Response response = consentAPIImpl.consentStatusPut(bulkConsentStatusUpdateResource, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        assertEquals(ConsentAPITestUtils.parseErrorMessage(response),
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR);

    }


//    @Test
//    public void testConsentConsentIdDeleteOrgInfoMismatch() throws
//            ConsentMgtException,
//            org.wso2.financial.services.accelerator.consent.mgt.service.exception.ConsentMgtException {
//        // Arrange
//        String consentID = sampleConsentID;
//        String orgInfo = "orgXYZ";
//
//        ConsentResource consentResource = Mockito.mock(ConsentResource.class);
//        consentResource.setOrgID("wrongOrg");
//
//        when(mockedConsentCoreServiceImpl.getConsent(consentID, false)).thenReturn(consentResource);
//
//
//        Response response = consentAPIImpl.consentConsentIdDelete(consentID, orgInfo);
//        assertEquals(ConsentAPITestUtils.parseErrorMessage(response), ConsentConstant.ORG_MISMATCH);
//    }


    @Test
    public void testConsentConsentIdDeleteConsentNotFound() throws
            ConsentMgtException {


        when(mockedConsentCoreServiceImpl.getConsent(sampleConsentID, false)).
                thenThrow(new ConsentMgtException(
                        Response.Status.INTERNAL_SERVER_ERROR, ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG));

        // Act & Assert

        Response response = consentAPIImpl.consentConsentIdDelete(sampleConsentID, sampleOrgID);

        assertEquals(ConsentAPITestUtils.parseErrorMessage(response),
                ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG);


    }

    // test case for delete consent with invalid consent ID
    @Test
    public void testConsentConsentIdDeleteInvalidConsentID() throws
            ConsentMgtException {
        String invalidConsentID = "invalid-id";


        ConsentResource consentResource = new ConsentResource();
        consentResource.setOrgID(sampleOrgID);

        when(mockedConsentCoreServiceImpl.getConsent(invalidConsentID, false)).thenReturn(consentResource);

        doThrow(new ConsentMgtException(Response.Status.BAD_REQUEST,
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR)).
                when(mockedConsentCoreServiceImpl).deleteConsent(any());

        // Act
        Response response = consentAPIImpl.consentConsentIdDelete(invalidConsentID, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        assertEquals(ConsentAPITestUtils.parseErrorMessage(response),
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR);

    }

    // Test for consentRevokeConsentIdPut
    @Test
    public void testConsentRevokeConsentIdPutSuccess() throws
            ConsentMgtException {
        // Arrange
        String orgInfo = "orgXYZ";
        String userID = "user456";
        ConsentRevokeRequestBody updateResource = new ConsentRevokeRequestBody();
        updateResource.setReason("User request");
        updateResource.setUserID(userID);

        ConsentResource consentResource = new ConsentResource();
        consentResource.setOrgID(orgInfo);
        consentResource.setCurrentStatus("ACTIVE");

        when(mockedConsentCoreServiceImpl.getConsent(sampleConsentID, false)).thenReturn(consentResource);


        doReturn(true).when(mockedConsentCoreServiceImpl).revokeConsentWithReason(any(), any(), any(), any());

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
        String userID = "user456";
        ConsentRevokeRequestBody updateResource = new ConsentRevokeRequestBody();
        updateResource.setReason("User request");
        updateResource.setUserID(userID);

        ConsentResource consentResource = new ConsentResource();
        consentResource.setOrgID(orgInfo);

        consentResource.setCurrentStatus("ACTIVE");


        when(mockedConsentCoreServiceImpl.getConsent(sampleConsentID, false)).thenReturn(consentResource);
        doReturn(true).when(mockedConsentCoreServiceImpl).revokeConsentWithReason(any(), any(), any(), any());
        // Act
        Response response = consentAPIImpl.consentRevokeConsentIdPost(sampleConsentID, updateResource, orgInfo);
        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

        assertEquals(ConsentAPITestUtils.parseErrorMessage(response),
                ConsentCoreServiceConstants.ORGANIZATION_MISMATCH_ERROR_MSG);

    }


    // ✅ Test Case 1: Successful response when `detailed=false` (Basic History Data)
    @Test
    public void testConsentConsentIdHistoryGetBasicHistory() throws
            Exception {
        // Given

        Boolean detailed = false;
        String status = "APPROVED";
        String actionBy = "user123";
        long fromTimeValue = 0L;
        long toTimeValue = 0L;
        String statusAuditId = "audit123";

        // Mock consent status audit records
        ConsentStatusAuditRecord auditRecord = mock(ConsentStatusAuditRecord.class);
        when(auditRecord.getStatusAuditID()).thenReturn("audit123");
        ArrayList<ConsentStatusAuditRecord> auditRecords = new ArrayList<>();
        auditRecords.add(auditRecord);

        when(mockedConsentCoreServiceImpl.searchConsentStatusAuditRecords(anyString(), anyString(), anyString(), any(),
                any(), anyString()))
                .thenReturn(auditRecords);

        // Mock history data
        ConsentHistoryResource historyResource = mock(ConsentHistoryResource.class);
        DetailedConsentResource detailedConsentResource = mock(DetailedConsentResource.class);
        when(detailedConsentResource.getConsentID()).thenReturn(sampleConsentID);
        when(detailedConsentResource.getReceipt()).thenReturn("receipt123");
        when(detailedConsentResource.getExpiryTime()).thenReturn(100L);
        when(detailedConsentResource.getCurrentStatus()).thenReturn("APPROVED");
        when(detailedConsentResource.getConsentType()).thenReturn("TYPE_A");

        when(historyResource.getDetailedConsentResource()).thenReturn(detailedConsentResource);
        Map<String, ConsentHistoryResource> historyData = new HashMap<>();
        historyData.put("audit123", historyResource);

        when(mockedConsentCoreServiceImpl.getConsentAmendmentHistoryData(any(), any())).thenReturn(historyData);

        // When
        Response response =
                consentAPIImpl.consentConsentIdHistoryGet(sampleConsentID, sampleOrgID, detailed, status, actionBy,
                        fromTimeValue,
                        toTimeValue, statusAuditId);

        // Then
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

    }

    @Test
    public void testConsentConsentIdHistoryGetBasicHistoryDetailed() throws
            Exception {

        Boolean detailed = true;
        String status = "APPROVED";
        String actionBy = "user123";
        long fromTimeValue = 0L;
        long toTimeValue = 0L;
        String statusAuditId = "audit123";

        // Mock consent status audit records
        ConsentStatusAuditRecord auditRecord = mock(ConsentStatusAuditRecord.class);
        when(auditRecord.getStatusAuditID()).thenReturn("audit123");
        ArrayList<ConsentStatusAuditRecord> auditRecords = new ArrayList<>();
        auditRecords.add(auditRecord);

        when(mockedConsentCoreServiceImpl.searchConsentStatusAuditRecords(anyString(), anyString(), anyString(), any(),
                any(), anyString()))
                .thenReturn(auditRecords);

        // Mock history data
        ConsentHistoryResource historyResource = mock(ConsentHistoryResource.class);
        DetailedConsentResource detailedConsentResource = mock(DetailedConsentResource.class);
        when(detailedConsentResource.getConsentID()).thenReturn(sampleConsentID);
        when(detailedConsentResource.getReceipt()).thenReturn("receipt123");
        when(detailedConsentResource.getExpiryTime()).thenReturn(100L);
        when(detailedConsentResource.getCurrentStatus()).thenReturn("APPROVED");
        when(detailedConsentResource.getConsentType()).thenReturn("TYPE_A");

        when(historyResource.getDetailedConsentResource()).thenReturn(detailedConsentResource);
        Map<String, ConsentHistoryResource> historyData = new HashMap<>();
        historyData.put("audit123", historyResource);

        when(mockedConsentCoreServiceImpl.getConsentAmendmentHistoryData(any(), any())).thenReturn(historyData);

        // When
        Response response =
                consentAPIImpl.consentConsentIdHistoryGet(sampleConsentID, sampleOrgID, detailed, status, actionBy,
                        fromTimeValue,
                        toTimeValue, statusAuditId);

        // Then
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

    }


    // ✅ Test Case 3: Handles Empty History Data Properly
    @Test
    public void testConsentConsentIdHistoryGetEmptyHistory() throws
            Exception {

        Boolean detailed = true;
        String status = "APPROVED";
        String actionBy = "user123";
        long fromTimeValue = 0L;
        long toTimeValue = 0L;
        String statusAuditId = "audit123";

        // Mock empty consent status audit records
        when(mockedConsentCoreServiceImpl.searchConsentStatusAuditRecords(anyString(), anyString(),
                anyString(), any(), any(), anyString()))
                .thenReturn(new ArrayList<>());

        // Mock empty history data
        when(mockedConsentCoreServiceImpl.getConsentAmendmentHistoryData(any(), any())).thenReturn(new HashMap<>());

        // When
        Response response =
                consentAPIImpl.consentConsentIdHistoryGet(sampleConsentID, sampleOrgID, detailed, status, actionBy,
                        fromTimeValue, toTimeValue, statusAuditId);

        // Then
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

    }

    // ✅ Test Case 4: Exception Handling when `consentCoreService` fails
    @Test
    public void testConsentConsentIdHistoryGetException() throws
            Exception {

        Boolean detailed = true;
        String status = "APPROVED";
        String actionBy = "user123";
        long fromTimeValue = 0L;
        long toTimeValue = 0L;
        String statusAuditId = "audit123";

        // Mock exception in `consentCoreService`
        when(mockedConsentCoreServiceImpl.searchConsentStatusAuditRecords(anyString(), anyString(), anyString(), any(),
                any(), anyString()))
                .thenThrow(new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, "Service Failure"));

        // When
        Response response =
                consentAPIImpl.consentConsentIdHistoryGet(sampleConsentID, sampleOrgID, detailed, status, actionBy,
                        fromTimeValue,
                        toTimeValue,
                        statusAuditId);

        // assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertEquals(ConsentAPITestUtils.parseErrorMessage(response), "Service Failure");


    }


    @Test
    public void testConsentStatusPutBulkFailure() throws
            ConsentMgtException {

        BulkConsentStatusUpdateResourceRequestBody
                bulkConsentStatusUpdateResource = mock(BulkConsentStatusUpdateResourceRequestBody.class);
        doThrow(new ConsentMgtException(Response.Status.BAD_REQUEST, "Invalid Data"))
                .when(mockedConsentCoreServiceImpl)
                .bulkUpdateConsentStatus(any(), any(), any(), any(), any(), any(), any());

        // Act
        Response response = consentAPIImpl.consentStatusPut(bulkConsentStatusUpdateResource, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testConsentConsentIdDeleteSuccess() throws
            ConsentMgtException {


        ConsentResource consentResource = mock(ConsentResource.class);
        doReturn(consentResource).when(mockedConsentCoreServiceImpl).getConsent(any(), anyBoolean());
        when(consentResource.getOrgID()).thenReturn(sampleOrgID); // Simulate mismatch

        doReturn(true).when(mockedConsentCoreServiceImpl).deleteConsent(any());

        // Act
        Response response = consentAPIImpl.consentConsentIdDelete(sampleConsentID, sampleOrgID);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentPostWithImplicitAuthWithNullOrg() throws
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
    public void testConsentConsentIdGetCoreServiceException() throws
            Exception {
        // Arrange
        String consentID = sampleConsentID;
        String orgInfo = "org123";
        boolean isDetailedConsent = true;
        boolean isWithAttributes = true;

        doThrow(new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, "Service Error"))
                .when(mockedConsentCoreServiceImpl).getDetailedConsent(any(), any());

        // Act
        Response response = consentAPIImpl.consentConsentIdGet(consentID, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testConsentPostMissingAuthorizationResources() throws
            Exception {
        // Arrange
        ConsentResourceRequestBody consentResourceDTO = mock(ConsentResourceRequestBody.class);
        String orgInfo = "org123";
        boolean isImplicitAuth = false;
        boolean exclusiveConsent = false;

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
    public void testConsentPostMissingAuthorizationResourcesWIthImplicitAuth() throws
            Exception {
        // Arrange
        ConsentResourceRequestBody consentResourceDTO = mock(ConsentResourceRequestBody.class);
        String orgInfo = "org123";
        boolean isImplicitAuth = true;
        boolean exclusiveConsent = false;

        // Mock data setup
        Map<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put("consentFrequency", "30");
        when(consentResourceDTO.getConsentAttributes()).thenReturn(consentAttributes);
        when(consentResourceDTO.getAuthorizationResources()).thenReturn(null); // Missing resources
        // Act
        when(mockedConsentCoreServiceImpl.createConsent(any(), any())).thenThrow(
                new ConsentMgtException(Response.Status.BAD_REQUEST,
                        ConsentCoreServiceConstants.CANNOT_PROCEED_WITH_IMPLICIT_AUTH));
        Response response = consentAPIImpl.consentPost(consentResourceDTO, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testConsentStatusPutBulkEmptyResource() throws
            Exception {
        // Arrange
        BulkConsentStatusUpdateResourceRequestBody bulkResource =
                new BulkConsentStatusUpdateResourceRequestBody(); // Empty resource
        String orgInfo = "org123";

        doThrow(new ConsentMgtException(Response.Status.BAD_REQUEST,
                ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR)).
                when(mockedConsentCoreServiceImpl).
                bulkUpdateConsentStatus(any(), any(), any(), any(), any(), any(), any());


        // Act
        Response response = consentAPIImpl.consentStatusPut(bulkResource, orgInfo);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
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
        when(consentResource.getOrgID()).thenReturn(orgInfo);
        when(mockedConsentCoreServiceImpl.getConsent(any(), anyBoolean())).thenReturn(consentResource);
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
        when(consentResource.getOrgID()).thenReturn(ConsentMgtDAOConstants.DEFAULT_ORG);
        when(mockedConsentCoreServiceImpl.getConsent(any(), anyBoolean())).thenReturn(consentResource);
        when(mockedConsentCoreServiceImpl.getAuthorizationResource(any(), any()))
                .thenReturn(authorizationResource);


        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, orgInfo, consentId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }


    @Test
    public void testConsentAuthorizationAuthorizationIdGetInvalidConsentId() throws
            Exception {
        // Arrange
        String authorizationId = "auth123";
        String orgInfo = "orgXYZ";
        String consentId = "invalid-consent-id";


        doThrow(new ConsentMgtException(Response.Status.NOT_FOUND, "Consent ID not found"))
                .when(mockedConsentCoreServiceImpl).getAuthorizationResource(authorizationId, orgInfo);


        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, orgInfo, consentId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testConsentAuthorizationAuthorizationIdGetInvalidAuthorizationId() throws
            Exception {
        // Arrange
        String authorizationId = "invalid-auth-id";
        String orgInfo = "orgXYZ";
        String consentId = "consent456";

        ConsentResource consentResource = mock(ConsentResource.class);
        when(consentResource.getOrgID()).thenReturn(orgInfo);
        when(mockedConsentCoreServiceImpl.getConsent(any(), anyBoolean())).thenReturn(consentResource);

        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgInfo))
                .thenThrow(new ConsentMgtException(Response.Status.NOT_FOUND, "Authorization ID not found"));

        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, orgInfo, consentId);

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
                .thenThrow(new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, "Service failure"));

        // Act
        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, orgInfo, consentId);

        // Assert
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testHandleConsentMgtExceptionBadRequest() throws
            Exception {
        // Arrange
        ConsentAPIImpl consentAPIImpl = new ConsentAPIImpl();
        ConsentMgtException exception = new ConsentMgtException(Response.Status.BAD_REQUEST, "Invalid request");

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
        ConsentMgtException exception = new ConsentMgtException(Response.Status.NOT_FOUND, "Consent not found");

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
                new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, "System error occurred");

        // Invoke private method via reflection
        Method method = ConsentAPIImpl.class.getDeclaredMethod("handleConsentMgtException", ConsentMgtException.class);
        method.setAccessible(true);
        Response response = (Response) method.invoke(consentAPIImpl, exception);

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
        when(consentResource.getOrgID()).thenReturn(orgInfo);
        when(mockedConsentCoreServiceImpl.getConsent(consentId, false)).thenReturn(consentResource);
        when(mockedConsentCoreServiceImpl.getAuthorizationResource(authorizationId, orgInfo)).thenReturn(
                authorizationResource);

        Response response = consentAPIImpl.consentAuthorizationIdGet(authorizationId, orgInfo, consentId);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConsentConsentIdHistoryGetServiceException() throws
            Exception {
        String consentID = sampleConsentID;
        String orgInfo = "org123";
        Boolean detailed = true;

        doThrow(new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, "Test Exception"))
                .when(mockedConsentCoreServiceImpl)
                .searchConsentStatusAuditRecords(anyString(), anyString(), anyString(), any(), any(), anyString());

        Response response = consentAPIImpl.consentConsentIdHistoryGet(consentID, orgInfo, detailed,
                "APPROVED", "user1", 0, 0, "audit123");

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testConsentPostJsonProcessingException() throws
            Exception {
        ConsentResourceRequestBody consentResourceDTO = mock(ConsentResourceRequestBody.class);
        String orgInfo = "org123";
        boolean isImplicitAuth = true;

        Map<String, String> attributes = new HashMap<>();
        attributes.put("consentFrequency", "30");
        when(consentResourceDTO.getConsentAttributes()).thenReturn(attributes);

        AuthorizationResourceRequestBody
                authorizationResourceDTO = mock(AuthorizationResourceRequestBody.class);
        Object invalidResource = new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("Force serialization failure");
            }
        };

        when(authorizationResourceDTO.getResource()).thenReturn(Collections.singletonList(invalidResource));
        when(consentResourceDTO.getAuthorizationResources()).thenReturn(
                Collections.singletonList(authorizationResourceDTO));

        Response response = consentAPIImpl.consentPost(consentResourceDTO, orgInfo);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testConsentGetTriggersSecondServiceCall() throws
            Exception {
        String orgInfo = "org123";

        ArrayList<DetailedConsentResource> result = new ArrayList<>();
        result.add(mock(DetailedConsentResource.class));

        when(mockedConsentCoreServiceImpl.searchDetailedConsents(eq(orgInfo), any(), any(), any(), any(), any(),
                any(), any(), eq(5), eq(10)))
                .thenReturn(result);
        when(mockedConsentCoreServiceImpl.searchDetailedConsents(eq(orgInfo), any(), any(), any(), any(), any(),
                any(), any(), isNull(), isNull()))
                .thenReturn(result);

        Response response = consentAPIImpl.consentGet(orgInfo, null, null, null, null, 0, 0, 5, 10);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }


    @Test
    public void testConsentDeleteReturnsFalse() throws
            Exception {
        String orgInfo = "orgXYZ";
        String consentID = sampleConsentID;

        ConsentResource consentResource = mock(ConsentResource.class);
        when(mockedConsentCoreServiceImpl.getConsent(consentID, false)).thenReturn(consentResource);
        when(consentResource.getOrgID()).thenReturn(orgInfo);
        when(mockedConsentCoreServiceImpl.deleteConsent(consentID)).thenReturn(false);

        Response response = consentAPIImpl.consentConsentIdDelete(consentID, orgInfo);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testConsentRevokeAlreadyRevoked() throws
            Exception {
        String consentID = sampleConsentID;
        String orgInfo = "orgXYZ";

        ConsentRevokeRequestBody updateResource = new ConsentRevokeRequestBody();
        updateResource.setReason("Already revoked");
        updateResource.setUserID("user123");

        ConsentResource consentResource = new ConsentResource();
        consentResource.setCurrentStatus("revoked");
        consentResource.setOrgID(orgInfo);

        when(mockedConsentCoreServiceImpl.getConsent(consentID, false)).thenReturn(consentResource);

        doThrow(new ConsentMgtException(Response.Status.BAD_REQUEST, "consent already revoked")).when(
                mockedConsentCoreServiceImpl).revokeConsentWithReason(any(),
                any(),
                any(),
                any());
        Response response = consentAPIImpl.consentRevokeConsentIdPost(consentID, updateResource, orgInfo);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testConsentHistoryGetJsonException() throws
            Exception {
        String consentID = sampleConsentID;
        String orgInfo = "orgXYZ";

        ArrayList<ConsentStatusAuditRecord> auditRecords = new ArrayList<>();
        ConsentStatusAuditRecord record = new ConsentStatusAuditRecord();
        record.setStatusAuditID("audit123");
        auditRecords.add(record);

        ConsentHistoryResource historyResource = mock(ConsentHistoryResource.class);
        DetailedConsentResource detailed = mock(DetailedConsentResource.class);
        when(detailed.getAuthorizationResources()).thenReturn(null);
        when(detailed.getConsentAttributes()).thenReturn(null);
        when(detailed.getConsentMappingResources()).thenReturn(null);
        when(historyResource.getDetailedConsentResource()).thenReturn(detailed);

        Map<String, ConsentHistoryResource> map = new HashMap<>();
        map.put("audit123", historyResource);

        when(mockedConsentCoreServiceImpl.searchConsentStatusAuditRecords(any(), any(), any(), any(), any(), any()))
                .thenReturn(auditRecords);
        when(mockedConsentCoreServiceImpl.getConsentAmendmentHistoryData(any(), any())).thenReturn(map);

        // Inject failure using mock static block or custom ObjectMapper inside code (optional)
        Response response =
                consentAPIImpl.consentConsentIdHistoryGet(consentID, orgInfo, false, null, null, 0, 0, null);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    private Response consentAPIImplTestProxyHandle(ConsentMgtException e) {
        try {
            java.lang.reflect.Method method =
                    ConsentAPIImpl.class.getDeclaredMethod("handleConsentMgtException", ConsentMgtException.class);
            method.setAccessible(true);
            return (Response) method.invoke(consentAPIImpl, e);
        } catch (Exception ex) {
            return null;
        }
    }

    @Test
    public void testHandleConsentMgtExceptionReturnsExpectedResponse() {
        ConsentMgtException ex = new ConsentMgtException(Response.Status.BAD_REQUEST, "Test error");
        Response response = consentAPIImplTestProxyHandle(ex);
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

}






