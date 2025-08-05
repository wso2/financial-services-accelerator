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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentManageData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRetrieveRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRevokeRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIConsentRevokeResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIModifiedResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPostFileUploadRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreConsentGenerateRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreConsentGenerateResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreFileUploadRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ExternalAPIPreFileUploadResponseDTO;

import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit test class for ExternalAPIConsentManageUtils.
 */
public class ExternalAPIConsentManageUtilsTest {

    private MockedStatic<ServiceExtensionUtils> staticMock;

    @BeforeClass
    public void setUp() {
        staticMock = mockStatic(ServiceExtensionUtils.class);
    }

    @AfterClass
    public void tearDown() {
        if (staticMock != null) {
            staticMock.close();
        }
    }

    @Test
    public void testCallExternalService_preConsent_success() throws Exception {
        ConsentManageData consentManageData = mock(ConsentManageData.class);
        when(consentManageData.getPayload()).thenReturn(new JSONObject().put("field", "value"));
        when(consentManageData.getRequestPath()).thenReturn("/test/path");
        when(consentManageData.getAllowedExtensionHeaders()).thenReturn(Map.of("x-api-key", "test-key"));

        ExternalAPIPreConsentGenerateRequestDTO requestDTO =
                new ExternalAPIPreConsentGenerateRequestDTO(consentManageData);

        JSONObject responseJson = new JSONObject().put("consentResource", new JSONObject().put("type", "accounts"));
        ExternalServiceResponse response = new ExternalServiceResponse();
        response.setStatus(StatusEnum.SUCCESS);
        response.setData(new ObjectMapper().readTree(responseJson.toString()));

        staticMock.when(() ->
                        ServiceExtensionUtils.invokeExternalServiceCall(any(ExternalServiceRequest.class),
                                eq(ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_CREATION)))
                .thenReturn(response);

        ExternalAPIPreConsentGenerateResponseDTO result =
                ExternalAPIConsentManageUtils.callExternalService(requestDTO);

        assertNotNull(result);
        assertNotNull(result.getConsentResource());
        assertEquals(result.getConsentResource().getType(), "accounts");
    }

    @Test
    public void testCallExternalService_postConsent_success() throws Exception {
        ExternalAPIConsentResourceRequestDTO consentResource = mock(ExternalAPIConsentResourceRequestDTO.class);
        when(consentResource.getId()).thenReturn("cid-123");

        ExternalAPIPostConsentGenerateRequestDTO requestDTO =
                new ExternalAPIPostConsentGenerateRequestDTO(consentResource, "/test/resource");

        JSONObject responseJson = new JSONObject().put("modifiedResponse", new JSONObject().put("info", "modified"));
        ExternalServiceResponse response = new ExternalServiceResponse();
        response.setStatus(StatusEnum.SUCCESS);
        response.setData(new ObjectMapper().readTree(responseJson.toString()));

        staticMock.when(() ->
                        ServiceExtensionUtils.invokeExternalServiceCall(any(ExternalServiceRequest.class),
                                eq(ServiceExtensionTypeEnum.ENRICH_CONSENT_CREATION_RESPONSE)))
                .thenReturn(response);

        ExternalAPIModifiedResponseDTO result =
                ExternalAPIConsentManageUtils.callExternalService(requestDTO);

        assertNotNull(result);
        JsonElement json = result.getModifiedResponse();
        assertEquals(json.getAsJsonObject().get("info").getAsString(), "modified");
    }

    @Test
    public void testCallExternalService_consentRevoke_success() throws Exception {
        ExternalAPIConsentRevokeRequestDTO requestDTO = mock(ExternalAPIConsentRevokeRequestDTO.class);
        when(requestDTO.toJson()).thenReturn(new JSONObject().put("consentId", "cid-123"));

        JSONObject responseJson = new JSONObject()
                .put("requireTokenRevocation", true)
                .put("revocationStatusName", "REVOKED");
        ExternalServiceResponse response = new ExternalServiceResponse();
        response.setStatus(StatusEnum.SUCCESS);
        response.setData(new ObjectMapper().readTree(responseJson.toString()));

        staticMock.when(() ->
                        ServiceExtensionUtils.invokeExternalServiceCall(any(ExternalServiceRequest.class),
                                eq(ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_REVOKE)))
                .thenReturn(response);

        ExternalAPIConsentRevokeResponseDTO result =
                ExternalAPIConsentManageUtils.callExternalService(requestDTO);

        assertNotNull(result);
        assertTrue(result.getRequireTokenRevocation());
        assertEquals(result.getRevocationStatusName(), "REVOKED");
    }

    @Test
    public void testCallExternalService_consentRetrieve_success() throws Exception {

        ExternalAPIConsentResourceRequestDTO consentResource = mock(ExternalAPIConsentResourceRequestDTO.class);
        when(consentResource.getId()).thenReturn("cid-xyz");

        ConsentManageData consentManageData = mock(ConsentManageData.class);
        when(consentManageData.getRequestPath()).thenReturn("/request/path");
        when(consentManageData.getAllowedExtensionHeaders()).thenReturn(Map.of("header1", "val1"));

        ExternalAPIConsentRetrieveRequestDTO requestDTO = new ExternalAPIConsentRetrieveRequestDTO(consentResource,
                consentManageData);

        JSONObject responseJson = new JSONObject().put("modifiedResponse", new JSONObject().put("status", "RETRIEVED"));
        ExternalServiceResponse response = new ExternalServiceResponse();
        response.setStatus(StatusEnum.SUCCESS);
        response.setData(new ObjectMapper().readTree(responseJson.toString()));

        staticMock.when(() ->
                        ServiceExtensionUtils.invokeExternalServiceCall(any(ExternalServiceRequest.class),
                                eq(ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_RETRIEVAL)))
                .thenReturn(response);

        ExternalAPIModifiedResponseDTO result =
                ExternalAPIConsentManageUtils.callExternalService(requestDTO);

        assertNotNull(result);
        JsonElement json = result.getModifiedResponse();
        assertEquals(json.getAsJsonObject().get("status").getAsString(), "RETRIEVED");
    }

    @Test
    public void testCallExternalService_preFileUpload_mocked() throws Exception {
        ExternalAPIPreFileUploadRequestDTO requestDTO = mock(ExternalAPIPreFileUploadRequestDTO.class);
        when(requestDTO.toJson()).thenReturn(new JSONObject().put("dummyKey", "dummyValue"));

        ExternalServiceResponse response = new ExternalServiceResponse();
        response.setStatus(StatusEnum.SUCCESS);
        staticMock.when(() ->
                        ServiceExtensionUtils.invokeExternalServiceCall(any(ExternalServiceRequest.class),
                                eq(ServiceExtensionTypeEnum.PRE_PROCESS_CONSENT_FILE_UPLOAD)))
                .thenReturn(response);

        ExternalAPIPreFileUploadResponseDTO result =
                ExternalAPIConsentManageUtils.callExternalService(requestDTO);

        assertNotNull(result);
    }

    @Test
    public void testCallExternalService_postFileUpload_mocked() throws Exception {
        ExternalAPIPostFileUploadRequestDTO requestDTO = mock(ExternalAPIPostFileUploadRequestDTO.class);

        ExternalServiceResponse response = new ExternalServiceResponse();
        response.setStatus(StatusEnum.SUCCESS);
        staticMock.when(() ->
                        ServiceExtensionUtils.invokeExternalServiceCall(any(ExternalServiceRequest.class),
                                eq(ServiceExtensionTypeEnum.ENRICH_CONSENT_FILE_RESPONSE)))
                .thenReturn(response);

        ExternalAPIModifiedResponseDTO result =
                ExternalAPIConsentManageUtils.callExternalService(requestDTO);

        assertNotNull(result);
    }

    @Test
    public void testCallExternalService_fileRetrieval_mocked() throws Exception {
        ExternalAPIConsentRetrieveRequestDTO requestDTO = mock(ExternalAPIConsentRetrieveRequestDTO.class);
        when(requestDTO.toJson()).thenReturn(new JSONObject().put("dummyKey", "dummyValue"));

        ExternalServiceResponse response = new ExternalServiceResponse();
        response.setStatus(StatusEnum.SUCCESS);
        staticMock.when(() ->
                        ServiceExtensionUtils.invokeExternalServiceCall(any(ExternalServiceRequest.class),
                                eq(ServiceExtensionTypeEnum.VALIDATE_CONSENT_FILE_RETRIEVAL)))
                .thenReturn(response);

        ExternalAPIModifiedResponseDTO result =
                ExternalAPIConsentManageUtils.callExternalServiceForFileRetrieval(requestDTO);

        assertNotNull(result);
    }

}
