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

package com.wso2.openbanking.accelerator.authentication.webapp;

//import com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl.ISDefaultAuthServletImpl;
//import com.wso2.openbanking.accelerator.consent.extensions.authservlet.model.OBAuthServletInterface;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.mock.web.MockServletConfig;
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.ResourceBundle;
//import javax.servlet.ServletException;
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//
//import static org.mockito.Matchers.anyObject;
//import static org.mockito.Matchers.anyString;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.spy;

/**
 * Test for consent servlets.
 */
public class ConsentServletsTest {

    //Consent data with key does not return JSON anymore. need to update

//    @Test
//    public void testAcceleratorServlet() throws IOException, ServletException {
//
//        // Mock & Spy & Assign
//        MockHttpServletRequest mockHttpRequest = new MockHttpServletRequest();
//        MockHttpServletResponse mockHttpResponse = new MockHttpServletResponse();
//        MockServletConfig mockServletConfig = new MockServletConfig();
//        JSONObject mockConsentData = new JSONObject();
//
//        ResourceBundle mockResourceBundle = new ResourceBundle() {
//            @Override
//            protected Object handleGetObject(String key) {
//                return "fake_translated_value";
//            }
//
//            @Override
//            public Enumeration<String> getKeys() {
//                return Collections.emptyEnumeration();
//            }
//        };
//
//        OBAuthServletInterface mockServletExtension = new OBAuthServletInterface() {
//            @Override
//            public Map<String, Object> updateRequestAttribute(HttpServletRequest request, JSONObject dataSet,
//                                                              ResourceBundle resourceBundle) {
//                return new HashMap<>();
//            }
//
//            @Override
//            public Map<String, Object> updateSessionAttribute(HttpServletRequest request, JSONObject dataSet,
//                                                              ResourceBundle resourceBundle) {
//                return new HashMap<>();
//            }
//
//            @Override
//            public Map<String, Object> updateConsentData(HttpServletRequest request) {
//                return new HashMap<>();
//            }
//
//            @Override
//            public Map<String, String> updateConsentMetaData(HttpServletRequest request) {
//                return new HashMap<>();
//            }
//
//            @Override
//            public String getJSPPath() {
//                return "/accelerated_jsp.jsp";
//            }
//        };
//
//        mockHttpRequest.addParameter("sessionDataKeyConsent", "SessDK_oo1");
//        mockServletConfig.addInitParameter("displayScopes", "true");
//        mockConsentData.put("app", "client_id_001");
//        mockConsentData.put("user_id", "user_id_001");
//        mockConsentData.put("type", "type_001");
//        mockConsentData.put("application", "type_001");
//
//        OBConsentServlet obConsentServlet = spy(new OBConsentServlet());
//        obConsentServlet.init(mockServletConfig);
//        obConsentServlet.obAuthServletTK = mockServletExtension;
//        doNothing().when(obConsentServlet).setAuthExtension();
//        doReturn(mockConsentData).when(obConsentServlet).getConsentDataWithKey(anyString(), anyObject());
//        doReturn(mockResourceBundle).when(obConsentServlet).getResourceBundle(anyObject());
//
//        // Act
//        obConsentServlet.doGet(mockHttpRequest, mockHttpResponse);
//
//        // Assert
//        Assert.assertEquals(mockHttpRequest.getAttribute("app"), "type_001");
//
//        //Scenario 2
//        mockConsentData.put("isError", "empty error");
//        obConsentServlet.doGet(mockHttpRequest, mockHttpResponse);
//        Assert.assertEquals(mockHttpResponse.getRedirectedUrl(),
//                "retry.do?status=Error&statusMsg=empty error");
//
//    }
//
//    @Test
//    public void testAcceleratorConfirmServlet() throws IOException, ServletException {
//
//        // Mock & Spy & Assign
//        MockHttpServletRequest mockHttpRequest = new MockHttpServletRequest();
//        MockHttpServletResponse mockHttpResponse = new MockHttpServletResponse();
//        MockServletConfig mockServletConfig = new MockServletConfig();
//        OBAuthServletInterface mockServletExtension = new OBAuthServletInterface() {
//            @Override
//            public Map<String, Object> updateRequestAttribute(HttpServletRequest request, JSONObject dataSet,
//                                                              ResourceBundle resourceBundle) {
//                return new HashMap<>();
//            }
//
//            @Override
//            public Map<String, Object> updateSessionAttribute(HttpServletRequest request, JSONObject dataSet,
//                                                              ResourceBundle resourceBundle) {
//                return new HashMap<>();
//            }
//
//            @Override
//            public Map<String, Object> updateConsentData(HttpServletRequest request) {
//                return new HashMap<>();
//            }
//
//            @Override
//            public Map<String, String> updateConsentMetaData(HttpServletRequest request) {
//                return new HashMap<>();
//            }
//
//            @Override
//            public String getJSPPath() {
//                return "/accelerated_jsp.jsp";
//            }
//        };
//
//        mockHttpRequest.setCookies(new Cookie[]{
//                new Cookie("commonAuthId", "test_value")
//        });
//
//        mockHttpRequest.addParameter("authorisationId", "authorisationId_oo1");
//        mockHttpRequest.addParameter("type", "authorisationId_oo1");
//        mockHttpRequest.addParameter("consent", "authorisationId_oo1");
//        mockHttpRequest.addParameter("id", "Id_oo1");
//        mockHttpRequest.addParameter("user", "Id_oo1");
//        mockHttpRequest.addParameter("hasApprovedAlways", "Id_999");
//        mockHttpRequest.addParameter("sessionDataKeyConsent", "Id_oo1");
//        mockHttpRequest.getSession().setAttribute("username", "username_001");
//
//        OBConsentConfirmServlet obConsentConfirmServlet = spy(new OBConsentConfirmServlet());
//        obConsentConfirmServlet.init(mockServletConfig);
//        obConsentConfirmServlet.obAuthServletTK = mockServletExtension;
//        doNothing().when(obConsentConfirmServlet).setAuthExtension();
//        doReturn("dummy").when(obConsentConfirmServlet).persistConsentData(
//                anyObject(), anyString(), anyObject());
//
//
//        // Act
//        obConsentConfirmServlet.doPost(mockHttpRequest, mockHttpResponse);
//
//        // Assert
//        Assert.assertEquals(mockHttpResponse.getRedirectedUrl(), "dummy");
//
//    }
//
//    @Test
//    public void testAcceleratorServletWithDefaultExtension() throws IOException, ServletException {
//
//        // Mock & Spy & Assign
//        MockHttpServletRequest mockHttpRequest = new MockHttpServletRequest();
//        MockHttpServletResponse mockHttpResponse = new MockHttpServletResponse();
//        MockServletConfig mockServletConfig = new MockServletConfig();
//        JSONObject mockConsentData = new JSONObject();
//        ResourceBundle mockResourceBundle = new ResourceBundle() {
//            @Override
//            protected Object handleGetObject(String key) {
//                return "fake_translated_value";
//            }
//
//            @Override
//            public Enumeration<String> getKeys() {
//                return Collections.emptyEnumeration();
//            }
//        };
//
//        mockHttpRequest.addParameter("sessionDataKeyConsent", "SessDK_oo1");
//        mockHttpRequest.addParameter("requestedClaims", "a_f, b_g, c_h, d_i");
//        mockHttpRequest.addParameter("mandatoryClaims", "ob_x, ob_y, ob_z");
//        mockHttpRequest.addParameter("userClaimsConsentOnly", "true");
//        mockServletConfig.addInitParameter("displayScopes", "true");
//        mockConsentData.put("client_id", "client_id_001");
//        mockConsentData.put("user_id", "user_id_001");
//        mockConsentData.put("type", "type_001");
//        mockConsentData.put("application", "type_001");
//        mockConsentData.put("openid_scopes", new JSONArray("[p,q,r,s]"));
//
//        OBConsentServlet obConsentServlet = spy(new OBConsentServlet());
//        obConsentServlet.init(mockServletConfig);
//        obConsentServlet.obAuthServletTK = new ISDefaultAuthServletImpl();
//        doNothing().when(obConsentServlet).setAuthExtension();
//        doReturn(mockConsentData).when(obConsentServlet).getConsentDataWithKey(anyString(), anyObject());
//        doReturn(mockResourceBundle).when(obConsentServlet).getResourceBundle(anyObject());
//
//
//        // Act
//        obConsentServlet.doGet(mockHttpRequest, mockHttpResponse);
//
//        // Assert
//        Assert.assertEquals(mockHttpRequest.getAttribute("continueDefault"), "fake_translated_value");
//
//    }
//
//    @Test
//    public void testServletServiceMethods() throws IOException {
//
//        // Assign
//        StatusLine statusLine = mock(StatusLine.class);
//        CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
//        ServletContext servletContext = mock(ServletContext.class);
//
//        when(statusLine.getStatusCode()).thenReturn(200);
//        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
//        when(closeableHttpResponse.getEntity()).thenReturn(EntityBuilder.create().setText(
//                "{description: \"John\", age: 31, city: \"New York\"}}").build());
//        when(servletContext.getInitParameter(anyString())).thenReturn("dummyParameter");
//
//        OBConsentServlet obConsentServlet = spy(new OBConsentServlet());
//        doReturn(closeableHttpResponse).when(obConsentServlet).getHttpResponse(anyObject());
//
//        // Act
//        JSONObject response = obConsentServlet.getConsentDataWithKey("dummy", servletContext);
//
//        // Assert
//        Assert.assertTrue(response.has("city"));
//
//
//        // Scenario 2
//        when(statusLine.getStatusCode()).thenReturn(401);
//        response = obConsentServlet.getConsentDataWithKey("dummy", servletContext);
//        Assert.assertEquals(response.getString("isError"), "John");

//    }

    //Consent data with key does not return JSON anymore. need to update
//    @Test
//    public void testGetConsentDataWithKey() throws IOException {
//
//        StatusLine statusLine = Mockito.mock(StatusLine.class);
//        ServletContext servletContext = Mockito.mock(ServletContext.class);
//        doReturn("https://localhost:9446/api/openbanking/consent/authorize/retrieve").when(servletContext)
//                .getInitParameter(anyString());
//        CloseableHttpResponse closeableHttpResponse = Mockito.mock(CloseableHttpResponse.class);
//        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
//        Mockito.when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
//        Mockito.when(closeableHttpResponse.getEntity()).thenReturn(EntityBuilder.create().setText(
//                "{description: \"John\", age: 31, city: \"New York\"}}").build());
//        OBConsentServlet obConsentServlet = spy(new OBConsentServlet());
//        obConsentServlet.getConsentDataWithKey("dummy", servletContext);
//    }
}
