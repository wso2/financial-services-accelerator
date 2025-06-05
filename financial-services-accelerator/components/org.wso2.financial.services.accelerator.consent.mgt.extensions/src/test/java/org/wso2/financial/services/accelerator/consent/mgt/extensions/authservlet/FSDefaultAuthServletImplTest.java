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

import org.json.JSONObject;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.impl.FSDefaultAuthServletImpl;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
        assertTrue(requestAttributes.containsKey(ConsentExtensionConstants.CONSENT_DATA));
        assertTrue(requestAttributes.containsKey(ConsentExtensionConstants.CONSUMER_DATA));
    }

    @Test
    public void testUpdateRequestAttributeForCOF() {

        JSONObject cofObj = new JSONObject(TestConstants.COF_AUTH_SERVLET_DATA);

        Map<String, Object> requestAttributes = servletImpl.updateRequestAttribute(httpServletRequestMock,
                cofObj, resourceBundle);

        assertFalse(requestAttributes.isEmpty());
        assertTrue(requestAttributes.containsKey(ConsentExtensionConstants.CONSENT_DATA));
    }

    @Test
    public void testUpdateRequestAttributeForPayments() {

        JSONObject paymentObj = new JSONObject(TestConstants.PAYMENT_AUTH_SERVLET_DATA);
        HttpSession session = mock(HttpSession.class);
        doReturn(session).when(httpServletRequestMock).getSession();

        Map<String, Object> requestAttributes = servletImpl.updateRequestAttribute(httpServletRequestMock,
                paymentObj, resourceBundle);

        assertFalse(requestAttributes.isEmpty());
        assertTrue(requestAttributes.containsKey(ConsentExtensionConstants.CONSENT_DATA));
    }

    @Test
    public void testUpdateRequestAttributeForNonExistingType() {

        JSONObject object = new JSONObject(TestConstants.AUTH_SERVLET_JSON_WITH_TYPE);

        Map<String, Object> requestAttributes = servletImpl.updateRequestAttribute(httpServletRequestMock,
                object, resourceBundle);

        assertTrue(requestAttributes.containsKey(ConsentExtensionConstants.TYPE));
    }

    @Test
    public void testUpdateSessionAttribute() {

        Map<String, Object> sessionAttributes = servletImpl.updateSessionAttribute(httpServletRequestMock,
                new JSONObject(), resourceBundle);

        assertTrue(sessionAttributes.isEmpty());
    }

    @Test
    public void testUpdateConsentData() {

        String param = "Test_parameter";
        doReturn(param).when(httpServletRequestMock).getParameter(anyString());
        HttpSession session = mock(HttpSession.class);
        doReturn(session).when(httpServletRequestMock).getSession();

        Map<String, Object> consentData = servletImpl.updateConsentData(httpServletRequestMock);
        assertFalse(consentData.isEmpty());
        assertTrue(consentData.containsKey(ConsentExtensionConstants.ACCOUNT_IDS));
        assertTrue(consentData.containsKey(ConsentExtensionConstants.PAYMENT_ACCOUNT));
        assertTrue(consentData.containsKey(ConsentExtensionConstants.COF_ACCOUNT));
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
