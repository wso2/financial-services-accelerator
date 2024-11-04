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

package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.response.validator;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * OBHybridResponseTypeValidator Test class.
 */
public class FSHybridResponseTypeValidatorTest {

    @Test
    public void checkValidHybridResponseTypeValidationWithRequestURI() throws OAuthProblemException {

        // Mock
        HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.REQUEST_URI)).thenReturn("test");
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.CLIENT_ID)).thenReturn("1234567");
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.RESPONSE_TYPE)).thenReturn("code id_token");
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.REDIRECT_URI))
                .thenReturn("https://google.com");
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.SCOPE)).thenReturn("openid accounts");

        FSHybridResponseTypeValidator uut = spy(new FSHybridResponseTypeValidator());

        // Act
        uut.validateRequiredParameters(httpServletRequestMock);
    }

    @Test(expectedExceptions = OAuthProblemException.class)
    public void checkValidHybridResponseTypeValidationWithoutScope() throws OAuthProblemException {

        // Mock
        HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.REQUEST_URI)).thenReturn("test");
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.CLIENT_ID)).thenReturn("1234567");
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.RESPONSE_TYPE)).thenReturn("code id_token");
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.REDIRECT_URI))
                .thenReturn("https://google.com");

        FSHybridResponseTypeValidator uut = spy(new FSHybridResponseTypeValidator());

        // Act
        uut.validateRequiredParameters(httpServletRequestMock);
    }

    @Test
    public void checkValidHybridResponseTypeValidationWithoutRequestURI() throws OAuthProblemException {

        // Mock
        HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        when(httpServletRequestMock.getParameter(OAuth.OAUTH_SCOPE)).thenReturn("openid");
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.CLIENT_ID)).thenReturn("1234567");
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.RESPONSE_TYPE)).thenReturn("code id_token");
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.REDIRECT_URI)).thenReturn("abc.com");
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.REQUEST)).thenReturn("sample-request-object");

        FSHybridResponseTypeValidator uut = spy(new FSHybridResponseTypeValidator());

        // Act
        uut.validateRequiredParameters(httpServletRequestMock);
    }

    @Test
    public void testValidateMethod() throws OAuthProblemException {

        // Mock
        HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        when(httpServletRequestMock.getMethod()).thenReturn("POST");

        FSHybridResponseTypeValidator uut = spy(new FSHybridResponseTypeValidator());

        // Act
        uut.validateMethod(httpServletRequestMock);
    }

    @Test(expectedExceptions = OAuthProblemException.class)
    public void testInvalidMethodScenario() throws OAuthProblemException {

        // Mock
        HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        when(httpServletRequestMock.getMethod()).thenReturn("PUT");

        FSHybridResponseTypeValidator uut = spy(new FSHybridResponseTypeValidator());

        // Act
        uut.validateMethod(httpServletRequestMock);
    }
}
