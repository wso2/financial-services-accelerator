/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.impl.FSDefaultAuthServletImpl;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.utils.Constants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestConstants;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Default Auth Servlet Impl Test.
 */
public class FSDefaultAuthServletImplTest {

    FSDefaultAuthServletImpl servletImpl;
    @Mock
    HttpServletRequest httpServletRequestMock;
    @Mock
    ResourceBundle resourceBundle;

    private static MockedStatic<FinancialServicesConfigParser> configParser;

    @BeforeClass
    public void initClass() {

        httpServletRequestMock = mock(HttpServletRequest.class);
        resourceBundle = mock(ResourceBundle.class);
        configParser = Mockito.mockStatic(FinancialServicesConfigParser.class);
        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        Map<String, Object> configs = new HashMap<String, Object>();
        Mockito.doReturn(configs).when(configParserMock).getConfiguration();
        Mockito.doReturn(true).when(configParserMock).isPreInitiatedConsent();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        servletImpl = new FSDefaultAuthServletImpl();
    }

    @AfterClass
    public void tearDown() {
        configParser.close();
    }

    @Test
    public void testUpdateRequestAttributeForAccounts() {

        JSONObject accountObj = new JSONObject(TestConstants.ACCOUNT_AUTH_SERVLET_DATA);

        Map<String, Object> requestAttributes = servletImpl.updateRequestAttribute(httpServletRequestMock,
                accountObj, resourceBundle);

        assertFalse(requestAttributes.isEmpty());
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.BASIC_CONSENT_DATA));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.PERMISSIONS));
        assertTrue(requestAttributes.containsKey(Constants.CONSUMER_ACCOUNTS));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.ALLOW_MULTIPLE_ACCOUNTS));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.IS_REAUTHORIZATION));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.INITIATED_ACCOUNTS_FOR_CONSENT));
    }

    @Test
    public void testUpdateRequestAttributeForCOF() {

        JSONObject cofObj = new JSONObject(TestConstants.COF_AUTH_SERVLET_DATA);

        Map<String, Object> requestAttributes = servletImpl.updateRequestAttribute(httpServletRequestMock,
                cofObj, resourceBundle);

        assertFalse(requestAttributes.isEmpty());
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.BASIC_CONSENT_DATA));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.PERMISSIONS));
        assertTrue(requestAttributes.containsKey(Constants.CONSUMER_ACCOUNTS));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.ALLOW_MULTIPLE_ACCOUNTS));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.IS_REAUTHORIZATION));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.INITIATED_ACCOUNTS_FOR_CONSENT));
    }

    @Test
    public void testUpdateRequestAttributeForPayments() {

        JSONObject paymentObj = new JSONObject(TestConstants.PAYMENT_AUTH_SERVLET_DATA);
        HttpSession session = mock(HttpSession.class);
        doReturn(session).when(httpServletRequestMock).getSession();

        Map<String, Object> requestAttributes = servletImpl.updateRequestAttribute(httpServletRequestMock,
                paymentObj, resourceBundle);

        assertFalse(requestAttributes.isEmpty());
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.BASIC_CONSENT_DATA));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.PERMISSIONS));
        assertTrue(requestAttributes.containsKey(Constants.CONSUMER_ACCOUNTS));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.ALLOW_MULTIPLE_ACCOUNTS));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.IS_REAUTHORIZATION));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.INITIATED_ACCOUNTS_FOR_CONSENT));
    }

    @Test
    public void testUpdateRequestAttributeForNonExistingType() {

        JSONObject object = new JSONObject(TestConstants.AUTH_SERVLET_JSON_WITH_TYPE);

        Map<String, Object> requestAttributes = servletImpl.updateRequestAttribute(httpServletRequestMock,
                object, resourceBundle);

        assertTrue(requestAttributes.containsKey(ConsentExtensionConstants.TYPE));
        assertNull(requestAttributes.get(ConsentExtensionConstants.TYPE));
        assertTrue(requestAttributes.containsKey(ConsentAuthorizeConstants.BASIC_CONSENT_DATA));
    }

    @Test
    public void testUpdateSessionAttribute() {

        Map<String, Object> sessionAttributes = servletImpl.updateSessionAttribute(httpServletRequestMock,
                new JSONObject(), resourceBundle);

        assertTrue(sessionAttributes.isEmpty());
    }

    @Test
    public void testUpdateConsentData_withPermissionsAndSelectedConsumerAccounts() {
        // isReauthorization = "true"
        doReturn("true").when(httpServletRequestMock)
                .getParameter(ConsentAuthorizeConstants.IS_REAUTHORIZATION);

        // Base64-encoded JSON for encodedAccountsPermissionsData
        JSONObject mockData = new JSONObject();
        mockData.put(ConsentAuthorizeConstants.PERMISSIONS, new JSONArray()
                .put(new JSONObject().put(ConsentAuthorizeConstants.DISPLAY_VALUES, new JSONArray()
                        .put("Read Account Information"))));
        mockData.put(ConsentAuthorizeConstants.CONSUMER_ACCOUNTS, new JSONArray()
                .put(new JSONObject().put("id", "acc1"))
                .put(new JSONObject().put("id", "acc2")));

        String encodedMockData = Base64.getUrlEncoder().encodeToString(mockData.toString()
                .getBytes(StandardCharsets.UTF_8));
        doReturn(encodedMockData)
                .when(httpServletRequestMock).getParameter(ConsentAuthorizeConstants.ENCODED_ACCOUNTS_PERMISSIONS_DATA);

        // Request parameter map (simulates form submissions like accountsOpt-0=0,1)
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("accountsOpt-0", new String[]{"0", "1"});

        doReturn(parameterMap).when(httpServletRequestMock).getParameterMap();

        // Call the method
        Map<String, Object> consentData = servletImpl.updateConsentData(httpServletRequestMock);

        assertFalse(consentData.isEmpty());

        assertTrue(consentData.containsKey(ConsentAuthorizeConstants.IS_REAUTHORIZATION));
        assertEquals(consentData.get(ConsentAuthorizeConstants.IS_REAUTHORIZATION), true);

        assertTrue(consentData.containsKey(ConsentAuthorizeConstants.AUTHORIZED_DATA));
        JSONArray authorizedData = (JSONArray) consentData.get(ConsentAuthorizeConstants.AUTHORIZED_DATA);
        assertEquals(authorizedData.length(), 1);
        JSONObject authEntry = authorizedData.getJSONObject(0);
        assertTrue(authEntry.has(ConsentAuthorizeConstants.PERMISSIONS));
        assertTrue(authEntry.has(ConsentAuthorizeConstants.ACCOUNTS));
        assertFalse(authEntry.getJSONArray(ConsentAuthorizeConstants.ACCOUNTS).isEmpty());
    }

    @Test
    public void testUpdateConsentData_withPermissionsAndPermissionInitiatedAccounts() {
        // isReauthorization = "false"
        doReturn("false").when(httpServletRequestMock)
                .getParameter(ConsentAuthorizeConstants.IS_REAUTHORIZATION);

        // Base64-encoded JSON for encodedAccountsPermissionsData
        JSONObject mockData = new JSONObject();
        mockData.put(ConsentAuthorizeConstants.PERMISSIONS, new JSONArray()
                .put(new JSONObject().put(ConsentAuthorizeConstants.DISPLAY_VALUES, new JSONArray()
                        .put("Read Account Information"))
                    .put(ConsentAuthorizeConstants.INITIATED_ACCOUNTS, new JSONArray()
                        .put(new JSONObject().put("id", "acc1"))
                        .put(new JSONObject().put("id", "acc2")))));

        String encodedMockData = Base64.getUrlEncoder().encodeToString(mockData.toString()
                .getBytes(StandardCharsets.UTF_8));
        doReturn(encodedMockData)
                .when(httpServletRequestMock).getParameter(ConsentAuthorizeConstants.ENCODED_ACCOUNTS_PERMISSIONS_DATA);

        // Request parameter map (simulates form submissions like accountsOpt-0=0,1)
        Map<String, String[]> parameterMap = new HashMap<>();

        doReturn(parameterMap).when(httpServletRequestMock).getParameterMap();

        // Call the method
        Map<String, Object> consentData = servletImpl.updateConsentData(httpServletRequestMock);

        assertFalse(consentData.isEmpty());

        assertTrue(consentData.containsKey(ConsentAuthorizeConstants.IS_REAUTHORIZATION));
        assertEquals(consentData.get(ConsentAuthorizeConstants.IS_REAUTHORIZATION), false);

        assertTrue(consentData.containsKey(ConsentAuthorizeConstants.AUTHORIZED_DATA));
        JSONArray authorizedData = (JSONArray) consentData.get(ConsentAuthorizeConstants.AUTHORIZED_DATA);
        assertEquals(authorizedData.length(), 1);
        JSONObject authEntry = authorizedData.getJSONObject(0);
        assertTrue(authEntry.has(ConsentAuthorizeConstants.PERMISSIONS));
        assertTrue(authEntry.has(ConsentAuthorizeConstants.ACCOUNTS));
        assertFalse(authEntry.getJSONArray(ConsentAuthorizeConstants.ACCOUNTS).isEmpty());
    }

    @Test
    public void testUpdateConsentData_withPermissionsAndConsentInitiatedAccounts() {
        // isReauthorization = "true"
        doReturn("true").when(httpServletRequestMock)
                .getParameter(ConsentAuthorizeConstants.IS_REAUTHORIZATION);

        // Base64-encoded JSON for encodedAccountsPermissionsData
        JSONObject mockData = new JSONObject();
        mockData.put(ConsentAuthorizeConstants.PERMISSIONS, new JSONArray()
                .put(new JSONObject().put(ConsentAuthorizeConstants.DISPLAY_VALUES, new JSONArray()
                        .put("Read Account Information"))));
        mockData.put(ConsentAuthorizeConstants.INITIATED_ACCOUNTS_FOR_CONSENT, new JSONArray()
                .put(new JSONObject().put("id", "acc1"))
                .put(new JSONObject().put("id", "acc2")));

        String encodedMockData = Base64.getUrlEncoder().encodeToString(mockData.toString()
                .getBytes(StandardCharsets.UTF_8));
        doReturn(encodedMockData)
                .when(httpServletRequestMock).getParameter(ConsentAuthorizeConstants.ENCODED_ACCOUNTS_PERMISSIONS_DATA);

        // Request parameter map (simulates form submissions like accountsOpt-0=0,1)
        Map<String, String[]> parameterMap = new HashMap<>();

        doReturn(parameterMap).when(httpServletRequestMock).getParameterMap();

        // Call the method
        Map<String, Object> consentData = servletImpl.updateConsentData(httpServletRequestMock);

        assertFalse(consentData.isEmpty());

        assertTrue(consentData.containsKey(ConsentAuthorizeConstants.IS_REAUTHORIZATION));
        assertEquals(consentData.get(ConsentAuthorizeConstants.IS_REAUTHORIZATION), true);

        assertTrue(consentData.containsKey(ConsentAuthorizeConstants.AUTHORIZED_DATA));
        JSONArray authorizedData = (JSONArray) consentData.get(ConsentAuthorizeConstants.AUTHORIZED_DATA);
        assertEquals(authorizedData.length(), 1);
        JSONObject authEntry = authorizedData.getJSONObject(0);
        assertTrue(authEntry.has(ConsentAuthorizeConstants.PERMISSIONS));
        assertTrue(authEntry.has(ConsentAuthorizeConstants.ACCOUNTS));
        assertFalse(authEntry.getJSONArray(ConsentAuthorizeConstants.ACCOUNTS).isEmpty());
    }

    @Test
    public void testUpdateConsentData_withoutPermissionsWithConsumerAccounts() {
        // isReauthorization = "true"
        doReturn("true").when(httpServletRequestMock)
                .getParameter(ConsentAuthorizeConstants.IS_REAUTHORIZATION);

        // Base64-encoded JSON for encodedAccountsPermissionsData
        JSONObject mockData = new JSONObject();
        mockData.put(ConsentAuthorizeConstants.CONSUMER_ACCOUNTS, new JSONArray()
                .put(new JSONObject().put("id", "acc1")).put(new JSONObject().put("id", "acc2")));

        String encodedMockData = Base64.getUrlEncoder().encodeToString(mockData.toString()
                .getBytes(StandardCharsets.UTF_8));
        doReturn(encodedMockData)
                .when(httpServletRequestMock).getParameter(ConsentAuthorizeConstants.ENCODED_ACCOUNTS_PERMISSIONS_DATA);

        // Request parameter map (simulates form submissions like accountsOpt-0=0,1)
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("accountsOpt", new String[]{"0", "1"});

        doReturn(parameterMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))).when(httpServletRequestMock)
                .getParameterMap();

        // Call the method
        Map<String, Object> consentData = servletImpl.updateConsentData(httpServletRequestMock);

        assertFalse(consentData.isEmpty());

        assertTrue(consentData.containsKey(ConsentAuthorizeConstants.IS_REAUTHORIZATION));
        assertEquals(consentData.get(ConsentAuthorizeConstants.IS_REAUTHORIZATION), true);

        assertTrue(consentData.containsKey(ConsentAuthorizeConstants.AUTHORIZED_DATA));
        JSONArray authorizedData = (JSONArray) consentData.get(ConsentAuthorizeConstants.AUTHORIZED_DATA);
        assertEquals(authorizedData.length(), 1);
        JSONObject authEntry = authorizedData.getJSONObject(0);
        assertFalse(authEntry.has(ConsentAuthorizeConstants.PERMISSIONS));
        assertTrue(authEntry.has(ConsentAuthorizeConstants.ACCOUNTS));
        assertFalse(authEntry.getJSONArray(ConsentAuthorizeConstants.ACCOUNTS).isEmpty());
    }

    @Test
    public void testUpdateConsentData_withoutPermissionsWithConsentInitiatedAccounts() {
        // isReauthorization = "true"
        doReturn("true").when(httpServletRequestMock)
                .getParameter(ConsentAuthorizeConstants.IS_REAUTHORIZATION);

        // Base64-encoded JSON for encodedAccountsPermissionsData
        JSONObject mockData = new JSONObject();
        mockData.put(ConsentAuthorizeConstants.INITIATED_ACCOUNTS_FOR_CONSENT, new JSONArray()
                .put(new JSONObject().put("id", "acc1"))
                .put(new JSONObject().put("id", "acc2")));

        String encodedMockData = Base64.getUrlEncoder().encodeToString(mockData.toString()
                .getBytes(StandardCharsets.UTF_8));
        doReturn(encodedMockData)
                .when(httpServletRequestMock).getParameter(ConsentAuthorizeConstants.ENCODED_ACCOUNTS_PERMISSIONS_DATA);

        // Request parameter map (simulates form submissions like accountsOpt-0=0,1)
        Map<String, String[]> parameterMap = new HashMap<>();

        doReturn(parameterMap).when(httpServletRequestMock).getParameterMap();

        // Call the method
        Map<String, Object> consentData = servletImpl.updateConsentData(httpServletRequestMock);

        assertFalse(consentData.isEmpty());

        assertTrue(consentData.containsKey(ConsentAuthorizeConstants.IS_REAUTHORIZATION));
        assertEquals(consentData.get(ConsentAuthorizeConstants.IS_REAUTHORIZATION), true);

        assertTrue(consentData.containsKey(ConsentAuthorizeConstants.AUTHORIZED_DATA));
        JSONArray authorizedData = (JSONArray) consentData.get(ConsentAuthorizeConstants.AUTHORIZED_DATA);
        assertEquals(authorizedData.length(), 1);
        JSONObject authEntry = authorizedData.getJSONObject(0);
        assertFalse(authEntry.has(ConsentAuthorizeConstants.PERMISSIONS));
        assertTrue(authEntry.has(ConsentAuthorizeConstants.ACCOUNTS));
        assertFalse(authEntry.getJSONArray(ConsentAuthorizeConstants.ACCOUNTS).isEmpty());
    }

    @Test
    public void testUpdateConsentData_withoutPermissionsWithoutAccounts() {
        // isReauthorization = "true"
        doReturn("true").when(httpServletRequestMock)
                .getParameter(ConsentAuthorizeConstants.IS_REAUTHORIZATION);

        // Base64-encoded JSON for encodedAccountsPermissionsData
        JSONObject mockData = new JSONObject();

        String encodedMockData = Base64.getUrlEncoder().encodeToString(mockData.toString()
                .getBytes(StandardCharsets.UTF_8));
        doReturn(encodedMockData)
                .when(httpServletRequestMock).getParameter(ConsentAuthorizeConstants.ENCODED_ACCOUNTS_PERMISSIONS_DATA);

        // Request parameter map (simulates form submissions like accountsOpt-0=0,1)
        Map<String, String[]> parameterMap = new HashMap<>();

        doReturn(parameterMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))).when(httpServletRequestMock)
                .getParameterMap();

        // Call the method
        Map<String, Object> consentData = servletImpl.updateConsentData(httpServletRequestMock);

        assertFalse(consentData.isEmpty());

        assertTrue(consentData.containsKey(ConsentAuthorizeConstants.IS_REAUTHORIZATION));
        assertEquals(consentData.get(ConsentAuthorizeConstants.IS_REAUTHORIZATION), true);

        assertTrue(consentData.containsKey(ConsentAuthorizeConstants.AUTHORIZED_DATA));
        JSONArray authorizedData = (JSONArray) consentData.get(ConsentAuthorizeConstants.AUTHORIZED_DATA);
        assertEquals(authorizedData.length(), 0);
    }

    @Test
    public void testUpdateConsentMetaData() {

        Map<String, String> consentMetadata = servletImpl.updateConsentMetaData(httpServletRequestMock);

        assertTrue(consentMetadata.isEmpty());
    }

    @Test
    public void testGetJSPPath() {

        assertEquals("/fs_default.jsp", servletImpl.getJSPPath());
    }
}
