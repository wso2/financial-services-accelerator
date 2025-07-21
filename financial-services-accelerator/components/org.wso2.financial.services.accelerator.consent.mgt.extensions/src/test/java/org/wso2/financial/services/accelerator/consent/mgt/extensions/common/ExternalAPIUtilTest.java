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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.AmendedResources;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentPersistRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestUtil;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit test class for ExternalAPIUtil.
 */
public class ExternalAPIUtilTest {

    private static ExternalServiceResponse createErrorResponse(int code, ObjectNode dataNode) {
        ExternalServiceResponse response = new ExternalServiceResponse();
        response.setErrorCode(code);
        response.setData(dataNode);
        return response;
    }

    private static ExternalAPIConsentResourceResponseDTO.Authorization createAuthorization(String userId, String type,
        String status, ExternalAPIConsentResourceResponseDTO.Resource resource) {

        ExternalAPIConsentResourceResponseDTO.Authorization auth =
                new ExternalAPIConsentResourceResponseDTO.Authorization();
        auth.setUserId(userId);
        auth.setType(type);
        auth.setStatus(status);
        auth.setResources(List.of(resource));
        return auth;
    }

    private static ExternalAPIConsentResourceResponseDTO.Resource createResource(String accountId, String permission,
                                                                                 String status) {

        ExternalAPIConsentResourceResponseDTO.Resource resource = new ExternalAPIConsentResourceResponseDTO.Resource();
        resource.setAccountId(accountId);
        resource.setPermission(permission);
        resource.setStatus(status);
        return resource;
    }

    private static ConsentResource createBaseConsent(String consentId, String clientId, String receipt, String type,
                                                     int freq, long validity, boolean recurring, String status) {

        return new ConsentResource(consentId, clientId, receipt, type, freq, validity, recurring, status,
                System.currentTimeMillis(), System.currentTimeMillis());
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleResponseError_withNullResponse() {

        ExternalAPIUtil.handleResponseError(null);
    }

    @Test
    public void testHandleResponseError_withInvalidErrorCode() {

        ObjectNode jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("message", "Invalid request").put("code", "1234");
        ExternalServiceResponse response = createErrorResponse(601, jsonNode);

        try {
            ExternalAPIUtil.handleResponseError(response);
            fail("Expected ConsentException was not thrown.");
        } catch (ConsentException ex) {
            assertEquals(ex.getStatus().getStatusCode(), 500);
            assertTrue(ex.getPayload().toString().contains("Invalid request"));
        }
    }

    @Test
    public void testHandleResponseError_withValidErrorCode() {

        ObjectNode jsonNode = new ObjectMapper().createObjectNode();
        jsonNode.put("error", "Unauthorized");
        ExternalServiceResponse response = createErrorResponse(401, jsonNode);

        try {
            ExternalAPIUtil.handleResponseError(response);
            fail("Expected ConsentException was not thrown.");
        } catch (ConsentException ex) {
            assertEquals(ex.getStatus().getStatusCode(), 401);
            assertTrue(ex.getPayload().toString().contains("Unauthorized"));
        }
    }

    @Test
    public void testConstructDetailedConsentResource_withAllFields() {

        ExternalAPIConsentResourceResponseDTO dto = new ExternalAPIConsentResourceResponseDTO();
        dto.setType("accounts");
        dto.setStatus("Authorised");
        dto.setFrequency(3);
        dto.setValidityTime(1723450000L);
        dto.setRecurringIndicator(true);
        dto.setReceipt(Map.of("scope", "openid accounts"));
        dto.setAttributes(Map.of("customKey", "customValue"));

        ExternalAPIConsentResourceResponseDTO.Resource res = createResource("acc1", "n/a", "active");
        dto.setAuthorizations(List.of(createAuthorization("user1", "authorization", "Created", res)));

        ConsentResource base = createBaseConsent("cid-001", "client-001", "{\"base\":\"receipt\"}",
                "accounts", 1, 3600L, false, "AwaitingAuthorisation");

        DetailedConsentResource result = ExternalAPIUtil.constructDetailedConsentResource(dto, base, "auth-001",
                "user1");

        assertNotNull(result);
        assertEquals(result.getConsentID(), "cid-001");
        assertEquals(result.getCurrentStatus(), "Authorised");
        assertEquals(result.getConsentFrequency(), 3);
        assertEquals(result.getValidityPeriod(), 1723450000L);
        assertTrue(result.isRecurringIndicator());
        assertEquals(result.getConsentAttributes().get("customKey"), "customValue");
        assertEquals(result.getAuthorizationResources().size(), 1);
        assertEquals(result.getConsentMappingResources().get(0).getPermission(), "n/a");
    }

    @Test
    public void testConstructDetailedConsentResource_withDifferentUserId_shouldGenerateNewAuthId() {

        ExternalAPIConsentResourceResponseDTO dto = new ExternalAPIConsentResourceResponseDTO();
        dto.setType("accounts");
        dto.setStatus("Authorised");
        dto.setReceipt(Map.of("scope", "openid accounts"));

        dto.setAuthorizations(List.of(createAuthorization("different-user", "primary", "Created",
                createResource("acc2", "n/a", "active"))));

        ConsentResource base = createBaseConsent("cid-002", "client-002", "{\"legacy\":\"yes\"}",
                "accounts", 2, 7200L, false, "Awaiting");

        DetailedConsentResource result = ExternalAPIUtil.constructDetailedConsentResource(dto, base, "auth-known",
                "known-user");

        assertNotNull(result);
        assertNotEquals(result.getAuthorizationResources().get(0).getAuthorizationID(), "auth-known");
    }

    @Test
    public void testConstructDetailedConsentResource_withNulls_shouldFallbackToBase() {

        ExternalAPIConsentResourceResponseDTO dto = new ExternalAPIConsentResourceResponseDTO();
        dto.setAttributes(Map.of());
        ConsentResource base = createBaseConsent("cid-003", "client-003", "{\"fallback\":\"receipt\"}",
                "base-type", 10, 9999L, true, "Pending");

        DetailedConsentResource result = ExternalAPIUtil.constructDetailedConsentResource(dto, base, null, null);

        assertNotNull(result);
        assertEquals(result.getConsentType(), "base-type");
        assertEquals(result.getCurrentStatus(), "Pending");
        assertEquals(result.getConsentFrequency(), 10);
        assertEquals(result.getValidityPeriod(), 9999L);
        assertTrue(result.getAuthorizationResources().isEmpty());
    }

    @Test
    public void testCreateExternalServiceRequest_withSampleConsentResource() {
        // Use TestUtil to get a valid sample ConsentResource
        ConsentResource consentResource = TestUtil.getSampleConsentResource("AwaitingAuthorisation");

        // Create ExternalAPIConsentResourceRequestDTO from the sample resource
        ExternalAPIConsentResourceRequestDTO consentResourceDTO =
                new ExternalAPIConsentResourceRequestDTO(consentResource);

        // Build the request DTO
        ExternalAPIPreConsentPersistRequestDTO requestDTO = new ExternalAPIPreConsentPersistRequestDTO(
                consentResource.getConsentID(), consentResourceDTO,
                new ExternalAPIPreConsentPersistRequestDTO.UserGrantedDataDTO(), true
        );

        // Create the ExternalServiceRequest
        ExternalServiceRequest serviceRequest = ExternalAPIUtil.createExternalServiceRequest(requestDTO);

        // Assertions
        assertNotNull(serviceRequest.getRequestId());
        JSONObject payload = serviceRequest.getData();

        assertEquals(payload.getString("consentId"), consentResource.getConsentID());
        assertTrue(payload.toString().contains("AwaitingAuthorisation"));
        assertTrue(payload.toString().contains("\"isApproved\":true"));
    }

    @Test
    public void testConstructAmendedResources_withValidInput() {
        // Given a sample amended authorization from TestUtil
        ExternalAPIConsentResourceResponseDTO.AmendedAuthorization amendedAuth =
                TestUtil.getSampleExternalAPIPreConsentGenerateResponseDTO()
                        .getConsentResource()
                        .getAmendments()
                        .get(0);

        // When: constructing amended resources
        AmendedResources result = ExternalAPIUtil.constructAmendedResources(List.of(amendedAuth));

        // Then: Assert that all sections are populated
        assertNotNull(result);
        assertEquals(result.getAmendedAuthResources().size(), 1);
        assertEquals(result.getNewMappingResources().size(), 1);
        assertEquals(result.getAmendedMappingResources().size(), 1);

        // Check specific field mappings
        AuthorizationResource authResource = result.getAmendedAuthResources().get(0);
        assertEquals(authResource.getAuthorizationID(), "amend-001");
        assertEquals(authResource.getAuthorizationType(), "CORRECTION");
        assertEquals(authResource.getAuthorizationStatus(), "APPROVED");

        ConsentMappingResource newMapping = result.getNewMappingResources().get(0);
        assertEquals(newMapping.getAccountID(), "acc-123");
        assertEquals(newMapping.getPermission(), "READ");

        ConsentMappingResource amendedMapping = result.getAmendedMappingResources().get(0);
        assertEquals(amendedMapping.getMappingID(), "am-res-001");
        assertEquals(amendedMapping.getPermission(), "WRITE");
        assertEquals(amendedMapping.getMappingStatus(), "UPDATED");
    }

}
