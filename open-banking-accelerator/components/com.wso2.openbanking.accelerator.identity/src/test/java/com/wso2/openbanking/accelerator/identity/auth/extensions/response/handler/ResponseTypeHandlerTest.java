/**
 * Copyright (c) 2023-2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler.impl.OBDefaultResponseTypeHandlerImpl;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;

/**
 * test for response type handler.
 */
public class ResponseTypeHandlerTest {

    @Test
    public void checkValidHybridResponseTypeHandling() throws IdentityOAuth2Exception, OpenBankingException {

        // Mock
        OBResponseTypeHandler obResponseTypeHandler = mock(OBDefaultResponseTypeHandlerImpl.class);
        when(obResponseTypeHandler.updateRefreshTokenValidityPeriod(anyObject())).thenReturn(999L);
        when(obResponseTypeHandler.updateApprovedScopes(anyObject())).thenReturn(new String[]{"Asd", "addd"});

        OBHybridResponseTypeHandlerExtension uut = spy(new OBHybridResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(anyObject());
        doReturn(true).when(uut).isRegulatory(anyObject());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        OBHybridResponseTypeHandlerExtension.obResponseTypeHandler = obResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issueCode(argument.capture());
        assertEquals(999L, argument.getValue().getRefreshTokenvalidityPeriod());
        assertEquals(2, argument.getValue().getApprovedScope().length);

    }

    @Test
    public void checkValidCodeResponseTypeHandling() throws IdentityOAuth2Exception, OpenBankingException {

        // Mock
        OBResponseTypeHandler obResponseTypeHandler = mock(OBDefaultResponseTypeHandlerImpl.class);
        when(obResponseTypeHandler.updateRefreshTokenValidityPeriod(anyObject())).thenReturn(109L);
        when(obResponseTypeHandler.updateApprovedScopes(anyObject())).thenReturn(new String[]{"Asd", "addd", "rr"});

        OBCodeResponseTypeHandlerExtension uut = spy(new OBCodeResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(anyObject());
        doReturn(true).when(uut).isRegulatory(anyObject());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        OBCodeResponseTypeHandlerExtension.obResponseTypeHandler = obResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issueCode(argument.capture());
        assertEquals(109L, argument.getValue().getRefreshTokenvalidityPeriod());
        assertEquals(3, argument.getValue().getApprovedScope().length);

    }

    @Test
    public void checkHandlerLogic() {

        OAuthAuthzReqMessageContext mock = mock(OAuthAuthzReqMessageContext.class);
        when(mock.getRefreshTokenvalidityPeriod()).thenReturn(6666L);
        when(mock.getApprovedScope()).thenReturn(new String[]{"1"});

        OBResponseTypeHandler uut = new OBDefaultResponseTypeHandlerImpl();

        assertEquals(6666L, uut.updateRefreshTokenValidityPeriod(mock));

    }

    @Test
    public void checkExceptionHandling_WhenIsRegulatoryThrowsOpenBankingException() throws Exception {

        OAuthAuthzReqMessageContext mockAuthzReqMsgCtx = mock(OAuthAuthzReqMessageContext.class);
        OAuth2AuthorizeReqDTO mockAuthorizeReqDTO = mock(OAuth2AuthorizeReqDTO.class);
        when(mockAuthzReqMsgCtx.getAuthorizationReqDTO()).thenReturn(mockAuthorizeReqDTO);
        when(mockAuthorizeReqDTO.getConsumerKey()).thenReturn("dummyClientId");
        OBCodeResponseTypeHandlerExtension uut = spy(new OBCodeResponseTypeHandlerExtension());
        doThrow(new OpenBankingException("Simulated isRegulatory exception"))
                .when(uut).isRegulatory("dummyClientId");

        try {
            uut.issue(mockAuthzReqMsgCtx);
            fail("Expected IdentityOAuth2Exception was not thrown.");
        } catch (IdentityOAuth2Exception e) {
            // Verify that the IdentityOAuth2Exception is thrown with the expected message
            assertEquals("Error while reading regulatory property", e.getMessage());
        }
    }
}
