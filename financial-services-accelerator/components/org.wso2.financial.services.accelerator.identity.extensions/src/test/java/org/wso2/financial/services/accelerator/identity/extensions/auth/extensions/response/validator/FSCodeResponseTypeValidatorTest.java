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

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * OBCodeResponseTypeValidator Test class.
 */
public class FSCodeResponseTypeValidatorTest {

    @Test
    public void checkValidCodeResponseTypeValidation() throws OAuthProblemException {

        try (MockedStatic<IdentityCommonUtils> mock = mockStatic(IdentityCommonUtils.class)) {
            // Mock
            HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
            when(httpServletRequestMock.getParameter("response_type")).thenReturn("code");
            when(httpServletRequestMock.getParameter("client_id")).thenReturn("1234567654321");

            mock.when(() -> IdentityCommonUtils.isRegulatoryApp(anyString())).thenReturn(false);

            FSCodeResponseTypeValidator uut = spy(new FSCodeResponseTypeValidator());

            // Act
            uut.validateRequiredParameters(httpServletRequestMock);
        }
    }
}
