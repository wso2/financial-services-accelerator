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

package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.response.handler;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.openidconnect.RequestObjectService;
import org.wso2.carbon.identity.openidconnect.model.RequestedClaim;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.response.handler.impl.FSDefaultResponseTypeHandlerImpl;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * test for response type handler.
 */
public class ResponseTypeHandlerTest {

    @Test
    public void checkValidHybridResponseTypeHandling() throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        FSResponseTypeHandler fsResponseTypeHandler = mock(FSDefaultResponseTypeHandlerImpl.class);
        when(fsResponseTypeHandler.getRefreshTokenValidityPeriod(any())).thenReturn(999L);
        when(fsResponseTypeHandler.getApprovedScopes(any())).thenReturn(new String[]{"Asd", "addd"});

        FSHybridResponseTypeHandlerExtension uut = spy(new FSHybridResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        FSHybridResponseTypeHandlerExtension.fsResponseTypeHandler = fsResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
        assertEquals(999L, argument.getValue().getRefreshTokenvalidityPeriod());
        assertEquals(2, argument.getValue().getApprovedScope().length);
    }

    @Test
    public void checkValidHybridResponseTypeHandlingForNonRegulatory()
            throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        FSResponseTypeHandler fsResponseTypeHandler = mock(FSDefaultResponseTypeHandlerImpl.class);
        when(fsResponseTypeHandler.getRefreshTokenValidityPeriod(any())).thenReturn(999L);
        when(fsResponseTypeHandler.getApprovedScopes(any())).thenReturn(new String[]{"Asd", "addd"});

        FSHybridResponseTypeHandlerExtension uut = spy(new FSHybridResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        FSHybridResponseTypeHandlerExtension.fsResponseTypeHandler = fsResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
        assertEquals(999L, argument.getValue().getRefreshTokenvalidityPeriod());
    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void checkValidHybridResponseTypeHandlingForIdentityOAuth2Exception()
            throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        FSResponseTypeHandler fsResponseTypeHandler = mock(FSDefaultResponseTypeHandlerImpl.class);
        when(fsResponseTypeHandler.getRefreshTokenValidityPeriod(any())).thenReturn(999L);
        when(fsResponseTypeHandler.getApprovedScopes(any())).thenReturn(null);

        FSHybridResponseTypeHandlerExtension uut = spy(new FSHybridResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        FSHybridResponseTypeHandlerExtension.fsResponseTypeHandler = fsResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
    }

    @Test
    public void checkValidCodeResponseTypeHandling() throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        FSResponseTypeHandler fsResponseTypeHandler = mock(FSDefaultResponseTypeHandlerImpl.class);
        when(fsResponseTypeHandler.getRefreshTokenValidityPeriod(any())).thenReturn(109L);
        when(fsResponseTypeHandler.getApprovedScopes(any())).thenReturn(new String[]{"Asd", "addd", "rr"});

        FSCodeResponseTypeHandlerExtension uut = spy(new FSCodeResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        FSCodeResponseTypeHandlerExtension.fsResponseTypeHandler = fsResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

       // Assert
        verify(uut).issue(argument.capture());
        assertEquals(109L, argument.getValue().getRefreshTokenvalidityPeriod());
        assertEquals(3, argument.getValue().getApprovedScope().length);
    }

    @Test
    public void checkValidCodeResponseTypeHandlingForNonRegulatory()
            throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        FSResponseTypeHandler fsResponseTypeHandler = mock(FSDefaultResponseTypeHandlerImpl.class);
        when(fsResponseTypeHandler.getRefreshTokenValidityPeriod(any())).thenReturn(109L);
        when(fsResponseTypeHandler.getApprovedScopes(any())).thenReturn(new String[]{"Asd", "addd", "rr"});

        FSCodeResponseTypeHandlerExtension uut = spy(new FSCodeResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        FSCodeResponseTypeHandlerExtension.fsResponseTypeHandler = fsResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
        assertEquals(109L, argument.getValue().getRefreshTokenvalidityPeriod());
    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void checkCodeResponseTypeHandlingForRequestObjectError()
            throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        FSResponseTypeHandler fsResponseTypeHandler = mock(FSDefaultResponseTypeHandlerImpl.class);
        when(fsResponseTypeHandler.getRefreshTokenValidityPeriod(any())).thenReturn(109L);
        when(fsResponseTypeHandler.getApprovedScopes(any())).thenReturn(new String[]{"Asd", "addd", "rr"});

        FSCodeResponseTypeHandlerExtension uut = spy(new FSCodeResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        FSCodeResponseTypeHandlerExtension.fsResponseTypeHandler = fsResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void checkCodeResponseTypeHandlingForIdentityOAuth2Exception()
            throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        FSResponseTypeHandler fsResponseTypeHandler = mock(FSDefaultResponseTypeHandlerImpl.class);
        when(fsResponseTypeHandler.getRefreshTokenValidityPeriod(any())).thenReturn(109L);
        when(fsResponseTypeHandler.getApprovedScopes(any())).thenReturn(null);

        FSCodeResponseTypeHandlerExtension uut = spy(new FSCodeResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        FSCodeResponseTypeHandlerExtension.fsResponseTypeHandler = fsResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
    }

    @Test
    public void checkHandlerLogic()  {

        OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTO = new OAuth2AuthorizeReqDTO();
        oAuth2AuthorizeReqDTO.setSessionDataKey("123");
        OAuthAuthzReqMessageContext oAuthAuthzContext = new OAuthAuthzReqMessageContext(oAuth2AuthorizeReqDTO);
        oAuthAuthzContext.setRefreshTokenvalidityPeriod(6666L);
        oAuthAuthzContext.setApprovedScope(new String[]{"1"});

        FSResponseTypeHandler uut = new FSDefaultResponseTypeHandlerImpl();

        assertEquals(6666L, uut.getRefreshTokenValidityPeriod(oAuthAuthzContext));

    }

    @Test
    public void checkHandlerUpdateApprovedScopes() throws RequestObjectException {

        try (MockedStatic<IdentityExtensionsDataHolder> identityDataHolderMock =
                     mockStatic(IdentityExtensionsDataHolder.class)) {

            OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTO = new OAuth2AuthorizeReqDTO();
            oAuth2AuthorizeReqDTO.setSessionDataKey("123");
            OAuthAuthzReqMessageContext oAuthAuthzContext = new OAuthAuthzReqMessageContext(oAuth2AuthorizeReqDTO);
            oAuthAuthzContext.setRefreshTokenvalidityPeriod(6666L);
            oAuthAuthzContext.setApprovedScope(new String[]{"1"});

            RequestedClaim claim = new RequestedClaim();
            claim.setName("openbanking_intent_id");
            claim.setValue("123");

            List<RequestedClaim> requestedClaims = List.of(claim);

            RequestObjectService mockRequestObjectService = mock(RequestObjectService.class);
            when(mockRequestObjectService.getRequestedClaimsForSessionDataKey(anyString(), anyBoolean()))
                    .thenReturn(requestedClaims);
            IdentityExtensionsDataHolder dataHolder = mock(IdentityExtensionsDataHolder.class);
            identityDataHolderMock.when(IdentityExtensionsDataHolder::getInstance).thenReturn(dataHolder);
            when(dataHolder.getRequestObjectService()).thenReturn(mockRequestObjectService);
            when(dataHolder.getConfigurationMap())
                    .thenReturn(Map.of("Identity.ConsentIDClaimName", "consent_id"));

            FSResponseTypeHandler uut = new FSDefaultResponseTypeHandlerImpl();
            String[] result = uut.getApprovedScopes(oAuthAuthzContext);

            assertEquals(2, result.length);
        }
    }

    @Test
    public void checkHandlerUpdateApprovedScopesWithoutConsentId() throws RequestObjectException {

        try (MockedStatic<IdentityExtensionsDataHolder> identityDataHolderMock =
                     mockStatic(IdentityExtensionsDataHolder.class)) {

            OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTO = new OAuth2AuthorizeReqDTO();
            oAuth2AuthorizeReqDTO.setSessionDataKey("123");
            OAuthAuthzReqMessageContext oAuthAuthzContext = new OAuthAuthzReqMessageContext(oAuth2AuthorizeReqDTO);
            oAuthAuthzContext.setRefreshTokenvalidityPeriod(6666L);
            oAuthAuthzContext.setApprovedScope(new String[]{"1"});

            RequestedClaim claim = new RequestedClaim();
            claim.setName("validity");
            claim.setValue("123");

            List<RequestedClaim> requestedClaims = List.of(claim);

            RequestObjectService mockRequestObjectService = mock(RequestObjectService.class);
            when(mockRequestObjectService.getRequestedClaimsForSessionDataKey(anyString(), anyBoolean()))
                    .thenReturn(requestedClaims);
            IdentityExtensionsDataHolder dataHolder = mock(IdentityExtensionsDataHolder.class);
            when(dataHolder.getRequestObjectService()).thenReturn(mockRequestObjectService);
            identityDataHolderMock.when(IdentityExtensionsDataHolder::getInstance).thenReturn(dataHolder);

            FSResponseTypeHandler uut = new FSDefaultResponseTypeHandlerImpl();
            String[] result = uut.getApprovedScopes(oAuthAuthzContext);

            assertEquals(1, result.length);
        }
    }

    @Test
    public void checkHandlerUpdateApprovedScopesForNullConsentId()  {

        OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTO = new OAuth2AuthorizeReqDTO();
        oAuth2AuthorizeReqDTO.setSessionDataKey("123");
        OAuthAuthzReqMessageContext oAuthAuthzContext = new OAuthAuthzReqMessageContext(oAuth2AuthorizeReqDTO);
        oAuthAuthzContext.setRefreshTokenvalidityPeriod(6666L);
        oAuthAuthzContext.setApprovedScope(new String[]{"1"});

        FSResponseTypeHandler uut = new FSDefaultResponseTypeHandlerImpl();
        String[] result = uut.getApprovedScopes(oAuthAuthzContext);

        assertEquals(1, result.length);

    }
}
