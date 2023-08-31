/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

import com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl.util.Constants;
import com.wso2.openbanking.accelerator.consent.extensions.authservlet.model.OBAuthServletInterface;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Test for consent management auth servlet implementation.
 */
public class ConsentMgrAuthServletImplTest {
    OBAuthServletInterface uut = new ConsentMgrAuthServletImpl();

    @Test
    public void testUpdateRequestAttribute() {
        JSONArray scopeArray = new JSONArray();
        scopeArray.put("scope1");
        scopeArray.put("scope2");
        scopeArray.put("scope3");

        JSONObject dataset = new JSONObject();
        dataset.put("openid_scopes", scopeArray);

        // mock
        HttpServletRequest servletRequestMock = Mockito.mock(HttpServletRequest.class);
        HttpSession httpSessionMock = Mockito.mock(HttpSession.class);
        ResourceBundle resourceBundleMock = Mockito.mock(ResourceBundle.class);

        // when
        Mockito.when(httpSessionMock.getAttribute("displayScopes")).thenReturn(true);
        Mockito.when(servletRequestMock.getSession()).thenReturn(httpSessionMock);

        // assert
        Map<String, Object> returnMap = uut
                .updateRequestAttribute(servletRequestMock, dataset, resourceBundleMock);
        Assert.assertNotNull(returnMap);
        List<String> oidScopes = (List<String>) returnMap.get(Constants.OIDC_SCOPES);
        Assert.assertTrue(oidScopes.contains("scope1"));
        Assert.assertTrue(oidScopes.contains("scope2"));
        Assert.assertTrue(oidScopes.contains("scope3"));
    }

    @Test(description = "when displayScopes is false, OIDCScopes should be null")
    public void testUpdateRequestAttributeWithFalseDisplayScope() {
        // mock
        HttpServletRequest servletRequestMock = Mockito.mock(HttpServletRequest.class);
        HttpSession httpSessionMock = Mockito.mock(HttpSession.class);
        ResourceBundle resourceBundleMock = Mockito.mock(ResourceBundle.class);

        // when
        Mockito.when(httpSessionMock.getAttribute("displayScopes")).thenReturn(false);
        Mockito.when(servletRequestMock.getSession()).thenReturn(httpSessionMock);

        // assert
        Map<String, Object> returnMap = uut
                .updateRequestAttribute(servletRequestMock, null, resourceBundleMock);
        Assert.assertNotNull(returnMap);
        Assert.assertNull(returnMap.get(Constants.OIDC_SCOPES));
    }
}
