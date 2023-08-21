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

package com.wso2.openbanking.accelerator.identity.auth.extensions.response.validator;

import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * OBHybridResponseTypeValidator Test class.
 */
@PrepareForTest({IdentityCommonUtil.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class OBHybridResponseTypeValidatorTest extends PowerMockTestCase {

    @Test
    public void checkValidHybridResponseTypeValidationWithRequestURI() throws OAuthProblemException {

        // Mock
        HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.REQUEST_URI)).thenReturn("test");
        when(httpServletRequestMock.getParameter(IdentityCommonConstants.CLIENT_ID)).thenReturn("1234567");

        PowerMockito.mockStatic(IdentityCommonUtil.class);
        PowerMockito.when(IdentityCommonUtil.decodeRequestObjectAndGetKey(httpServletRequestMock, OAuth.OAUTH_SCOPE))
                .thenReturn("openid");

        OBHybridResponseTypeValidator uut = spy(new OBHybridResponseTypeValidator());

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

        OBHybridResponseTypeValidator uut = spy(new OBHybridResponseTypeValidator());

        // Act
        uut.validateRequiredParameters(httpServletRequestMock);
    }

    @Test
    public void testValidateMethod() throws OAuthProblemException {

        // Mock
        HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        when(httpServletRequestMock.getMethod()).thenReturn("POST");

        OBHybridResponseTypeValidator uut = spy(new OBHybridResponseTypeValidator());

        // Act
        uut.validateMethod(httpServletRequestMock);
    }

    @Test(expectedExceptions = OAuthProblemException.class)
    public void testInvalidMethodScenario() throws OAuthProblemException {

        // Mock
        HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        when(httpServletRequestMock.getMethod()).thenReturn("PUT");

        OBHybridResponseTypeValidator uut = spy(new OBHybridResponseTypeValidator());

        // Act
        uut.validateMethod(httpServletRequestMock);
    }
}
