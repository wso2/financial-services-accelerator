/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.keymanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.keymanager.dto.ExternalAPIApplicationCreationResponseDTO;
import org.wso2.financial.services.accelerator.keymanager.dto.ExternalAPIApplicationUpdateResponseDTO;
import org.wso2.financial.services.accelerator.keymanager.util.KeyManagerTestConstants;
import org.wso2.financial.services.accelerator.keymanager.utils.FSKeyManagerUtil;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class for FSKeyManagerUtil.
 */
public class FSKeyManagerUtilsTest {


    static MockedStatic<FinancialServicesConfigParser> configParserMockedStatic;
    static MockedStatic<ServiceExtensionUtils> serviceExtensionUtilsMockedStatic;

    @BeforeClass
    public static void setUp() {

        // Initialize the mocked static methods for ServiceExtensionUtils
        serviceExtensionUtilsMockedStatic = Mockito.mockStatic(ServiceExtensionUtils.class);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(Mockito.any()))
                .thenReturn(true);
    }

    @AfterClass
    public static void tearDown() {

        configParserMockedStatic.close();
        serviceExtensionUtilsMockedStatic.close();
    }

    @Test
    public void testGetValuesForAdditionalProperties() throws APIManagementException {

        configParserMockedStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);
        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(KeyManagerTestConstants.KEY_MANAGER_CONFIGS).when(configParserMock)
                .getKeyManagerAdditionalProperties();
        configParserMockedStatic.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        OAuthApplicationInfo oAuthApplicationInfo = Mockito.mock(OAuthApplicationInfo.class);
        Mockito.doReturn(KeyManagerTestConstants.JSON_ADDITIONAL_PROPERTIES)
                .when(oAuthApplicationInfo).getParameter(anyString());

        OAuthAppRequest oauthAppRequest = Mockito.mock(OAuthAppRequest.class);
        Mockito.doReturn(oAuthApplicationInfo).when(oauthAppRequest).getOAuthApplicationInfo();
        HashMap<String, String> values = FSKeyManagerUtil.getValuesForAdditionalProperties(oauthAppRequest);
        Assert.assertNotNull(values);
    }

    @Test
    public void testGetValueForAdditionalProperty() throws APIManagementException {

        String value = FSKeyManagerUtil.getValueForAdditionalProperty(
                KeyManagerTestConstants.getFSAdditionalProperties(), "regulatory");
        Assert.assertNotNull(value);
    }

    @Test
    public void testGetValueForAdditionalPropertyForNonExistence() {

        try {
            FSKeyManagerUtil.getValueForAdditionalProperty(
                    KeyManagerTestConstants.getFSAdditionalProperties(), "test");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("test property not found in additional properties"));
        }
    }

    @Test
    public void testGetValueForAdditionalPropertyForEmptyValues() {

        try {
            FSKeyManagerUtil.getValueForAdditionalProperty(
                    KeyManagerTestConstants.getFSAdditionalProperties(), "sp_certificate");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("No value found for additional property: sp_certificate"));
        }
    }

    @Test
    public void testCallExternalServiceForAppCreation() throws APIManagementException {

        ExternalAPIApplicationCreationResponseDTO responseDTO = new ExternalAPIApplicationCreationResponseDTO();
        responseDTO.setAdditionalAppData(Map.of("key1", "value1"));
        responseDTO.setClientId("test-client-id");

        ExternalServiceResponse externalServiceResponse = new ExternalServiceResponse();
        externalServiceResponse.setStatus(StatusEnum.SUCCESS);
        externalServiceResponse.setData(new ObjectMapper().valueToTree(responseDTO));

        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(Mockito.any(),
                        Mockito.any())).thenReturn(externalServiceResponse);

        OAuthAppRequest oauthAppRequest = new OAuthAppRequest();
        OAuthApplicationInfo oauthApplicationInfo = new OAuthApplicationInfo();
        oauthAppRequest.setOAuthApplicationInfo(oauthApplicationInfo);
        HashMap<String, String> additionalProperties = new HashMap<>();
        // Call the method to be tested
        FSKeyManagerUtil.callExternalService(oauthAppRequest, additionalProperties,
                new JSONObject(), ServiceExtensionTypeEnum.PRE_PROCESS_APPLICATION_CREATION);

        Assert.assertEquals(oauthAppRequest.getOAuthApplicationInfo().getClientId(), "test-client-id");
        Assert.assertFalse(additionalProperties.isEmpty());
    }

    @Test
    public void testCallExternalServiceForAppUpdate() throws APIManagementException {

        ExternalAPIApplicationUpdateResponseDTO responseDTO = new ExternalAPIApplicationUpdateResponseDTO();
        responseDTO.setAdditionalAppData(Map.of("key1", "value1"));

        ExternalServiceResponse externalServiceResponse = new ExternalServiceResponse();
        externalServiceResponse.setStatus(StatusEnum.SUCCESS);
        externalServiceResponse.setData(new ObjectMapper().valueToTree(responseDTO));

        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(Mockito.any(),
                Mockito.any())).thenReturn(externalServiceResponse);

        OAuthAppRequest oauthAppRequest = new OAuthAppRequest();
        OAuthApplicationInfo oauthApplicationInfo = new OAuthApplicationInfo();
        oauthAppRequest.setOAuthApplicationInfo(oauthApplicationInfo);
        HashMap<String, String> additionalProperties = new HashMap<>();
        // Call the method to be tested
        FSKeyManagerUtil.callExternalService(oauthAppRequest, additionalProperties,
                new JSONObject(), ServiceExtensionTypeEnum.PRE_PROCESS_APPLICATION_UPDATE);

        Assert.assertFalse(additionalProperties.isEmpty());
    }

    @Test
    public void testCallExternalServiceForAppCreationForError() {

        ExternalAPIApplicationCreationResponseDTO responseDTO = new ExternalAPIApplicationCreationResponseDTO();
        responseDTO.setAdditionalAppData(Map.of("key1", "value1"));
        responseDTO.setClientId("test-client-id");

        ExternalServiceResponse externalServiceResponse = new ExternalServiceResponse();
        externalServiceResponse.setStatus(StatusEnum.ERROR);
        externalServiceResponse.setData(new ObjectMapper().valueToTree(responseDTO));

        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(Mockito.any(),
                Mockito.any())).thenReturn(externalServiceResponse);

        OAuthAppRequest oauthAppRequest = new OAuthAppRequest();
        OAuthApplicationInfo oauthApplicationInfo = new OAuthApplicationInfo();
        oauthAppRequest.setOAuthApplicationInfo(oauthApplicationInfo);
        HashMap<String, String> additionalProperties = new HashMap<>();
        // Call the method to be tested
        try {
            FSKeyManagerUtil.callExternalService(oauthAppRequest, additionalProperties,
                    new JSONObject(), ServiceExtensionTypeEnum.PRE_PROCESS_APPLICATION_CREATION);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Unexpected error occurred");
        }
    }

    @Test
    public void testCallExternalServiceForAppCreationWithException() throws APIManagementException {

        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(Mockito.any(),
                Mockito.any())).thenThrow(FinancialServicesException.class);

        OAuthAppRequest oauthAppRequest = new OAuthAppRequest();
        OAuthApplicationInfo oauthApplicationInfo = new OAuthApplicationInfo();
        oauthAppRequest.setOAuthApplicationInfo(oauthApplicationInfo);
        HashMap<String, String> additionalProperties = new HashMap<>();
        // Call the method to be tested
        try {
            FSKeyManagerUtil.callExternalService(oauthAppRequest, additionalProperties,
                    new JSONObject(), ServiceExtensionTypeEnum.PRE_PROCESS_APPLICATION_CREATION);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error Occurred while invoking the external service");
        }
    }
}
