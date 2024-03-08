/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.utils.AuthServletTestConstants;
import org.json.JSONObject;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Test class for OB Auth Servlet.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
public class AuthServletTest {

    OBDefaultAuthServletImpl obAuthServlet;
    @Mock
    HttpServletRequest httpServletRequestMock;
    @Mock
    ResourceBundle resourceBundle;

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);

        obAuthServlet = new OBDefaultAuthServletImpl();
        httpServletRequestMock = Mockito.mock(HttpServletRequest.class);
        resourceBundle = Mockito.mock(ResourceBundle.class);
    }

    @Test
    public void testUpdateRequestAttributeForAccounts() {

        JSONObject accountObj = new JSONObject(AuthServletTestConstants.ACCOUNT_DATA);

        Map<String, Object> requestAttributes = obAuthServlet.updateRequestAttribute(httpServletRequestMock,
                accountObj, resourceBundle);

        Assert.assertFalse(requestAttributes.isEmpty());
        Assert.assertTrue(requestAttributes.containsKey(ConsentExtensionConstants.DATA_REQUESTED));
    }

    @Test
    public void testUpdateRequestAttributeForCOF() {

        JSONObject cofObj = new JSONObject(AuthServletTestConstants.COF_DATA);

        Map<String, Object> requestAttributes = obAuthServlet.updateRequestAttribute(httpServletRequestMock,
                cofObj, resourceBundle);

        Assert.assertFalse(requestAttributes.isEmpty());
        Assert.assertTrue(requestAttributes.containsKey(ConsentExtensionConstants.DATA_REQUESTED));
    }

    @Test
    public void testUpdateRequestAttributeForPayments() {

        JSONObject paymentObj = new JSONObject(AuthServletTestConstants.PAYMENT_DATA);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.doReturn(session).when(httpServletRequestMock).getSession();

        Map<String, Object> requestAttributes = obAuthServlet.updateRequestAttribute(httpServletRequestMock,
                paymentObj, resourceBundle);

        Assert.assertFalse(requestAttributes.isEmpty());
        Assert.assertTrue(requestAttributes.containsKey(ConsentExtensionConstants.DATA_REQUESTED));
    }

    @Test
    public void testUpdateRequestAttributeForPaymentsWithoutDebtorAccInPayload() {

        JSONObject paymentObj = new JSONObject(AuthServletTestConstants.PAYMENT_DATA_WITHOUT_DEBTOR_ACC);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.doReturn(session).when(httpServletRequestMock).getSession();

        Map<String, Object> requestAttributes = obAuthServlet.updateRequestAttribute(httpServletRequestMock,
                paymentObj, resourceBundle);

        Assert.assertTrue(requestAttributes.isEmpty());
        Assert.assertFalse(requestAttributes.containsKey(ConsentExtensionConstants.DATA_REQUESTED));
    }

    @Test
    public void testUpdateRequestAttributeForVRP() {

        JSONObject paymentObj = new JSONObject(AuthServletTestConstants.VRP_DATA);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.doReturn(session).when(httpServletRequestMock).getSession();

        Map<String, Object> requestAttributes = obAuthServlet.updateRequestAttribute(httpServletRequestMock,
                paymentObj, resourceBundle);

        Assert.assertFalse(requestAttributes.isEmpty());
        Assert.assertTrue(requestAttributes.containsKey(ConsentExtensionConstants.DATA_REQUESTED));
    }

    @Test
    public void testUpdateRequestAttributeForVRPWithoutDebtorAcc() {

        JSONObject paymentObj = new JSONObject(AuthServletTestConstants.VRP_DATA_WITHOUT_DEBTOR_ACC);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.doReturn(session).when(httpServletRequestMock).getSession();

        Map<String, Object> requestAttributes = obAuthServlet.updateRequestAttribute(httpServletRequestMock,
                paymentObj, resourceBundle);

        Assert.assertFalse(requestAttributes.isEmpty());
        Assert.assertTrue(requestAttributes.containsKey(ConsentExtensionConstants.DATA_REQUESTED));
    }

    @Test
    public void testUpdateRequestAttributeForNonExistingType() {

        JSONObject object = new JSONObject(AuthServletTestConstants.JSON_WITH_TYPE);

        Map<String, Object> requestAttributes = obAuthServlet.updateRequestAttribute(httpServletRequestMock,
                object, resourceBundle);

        Assert.assertTrue(requestAttributes.isEmpty());
    }

    @Test
    public void testUpdateConsentData() {

        String param = "Test_parameter";
        Mockito.doReturn(param).when(httpServletRequestMock).getParameter(Mockito.anyString());
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.doReturn(session).when(httpServletRequestMock).getSession();

        Map<String, Object> consentData = obAuthServlet.updateConsentData(httpServletRequestMock);
        Assert.assertFalse(consentData.isEmpty());
        Assert.assertTrue(consentData.containsKey(ConsentExtensionConstants.ACCOUNT_IDS));
        Assert.assertTrue(consentData.containsKey(ConsentExtensionConstants.PAYMENT_ACCOUNT));
        Assert.assertTrue(consentData.containsKey(ConsentExtensionConstants.COF_ACCOUNT));
    }

    @Test
    public void testUpdateConsentMetaData() {

        Map<String, String> consentMetadata = obAuthServlet.updateConsentMetaData(httpServletRequestMock);

        Assert.assertTrue(consentMetadata.isEmpty());
    }

    @Test
    public void testUpdateSessionAttribute() {

        Map<String, Object> sessionAttributes = obAuthServlet.updateSessionAttribute(httpServletRequestMock,
                new JSONObject(), resourceBundle);

        Assert.assertTrue(sessionAttributes.isEmpty());
    }
}
