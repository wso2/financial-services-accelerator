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

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
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
 * OBCodeResponseTypeValidator Test class.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({IdentityCommonUtil.class})
public class OBCodeResponseTypeValidatorTest extends PowerMockTestCase {

    @Test
    public void checkValidCodeResponseTypeValidation() throws OAuthProblemException, OpenBankingException {

        // Mock
        HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        when(httpServletRequestMock.getParameter("response_type")).thenReturn("code");
        when(httpServletRequestMock.getParameter("client_id")).thenReturn("1234567654321");

        PowerMockito.mockStatic(IdentityCommonUtil.class);
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("test")).thenReturn(true);

        OBCodeResponseTypeValidator uut = spy(new OBCodeResponseTypeValidator());

        // Act
        uut.validateRequiredParameters(httpServletRequestMock);
    }
}
