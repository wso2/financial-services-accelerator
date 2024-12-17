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

package com.wso2.openbanking.accelerator.ciba;

import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.token.DefaultTokenFilter;
import com.wso2.openbanking.accelerator.identity.token.TokenFilter;
import com.wso2.openbanking.accelerator.identity.token.validators.OBIdentityFilterValidator;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;

import static org.testng.Assert.assertEquals;

@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({IdentityCommonUtil.class, OAuthServerConfiguration.class})
public class OBCIBASignatureAlgorithmEnforcementValidatorTest extends PowerMockTestCase {



    MockHttpServletResponse response;
    MockHttpServletRequest request;
    FilterChain filterChain;

    @BeforeMethod
    public void beforeMethod() {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = Mockito.spy(FilterChain.class);

    }

    //Enable this test if you are building CIBA components along with this.
    @Test(description = "Test when registered algorithm and signed algorithm differ")
    public void fapiUnsupportedSignatureAlgorithmValidationTest() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        PowerMockito.mockStatic(IdentityCommonUtil.class);
        PowerMockito.mockStatic(OAuthServerConfiguration.class);

        OAuthServerConfiguration oAuthServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        configMap.put(IdentityCommonConstants.ENABLE_TRANSPORT_CERT_AS_HEADER, true);
        configMap.put(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, false);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(configMap);

        OBCIBASignatureAlgorithmEnforcementValidator validator =
                Mockito.spy(OBCIBASignatureAlgorithmEnforcementValidator.class);

        request.setParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION, TestConstants.CLIENT_ASSERTION);
        request.addHeader(TestConstants.CERTIFICATE_HEADER, TestConstants.CERTIFICATE_CONTENT);

        Mockito.doReturn("RS256").when(validator)
                .getRegisteredSigningAlgorithm("iYpRm64b2vmvmKDhdL6KZD9z6fca");
        Mockito.doReturn("RS256").when(validator)
                .getRequestSigningAlgorithm(TestConstants.CLIENT_ASSERTION);

        List<OBIdentityFilterValidator> validators = new ArrayList<>();
        validators.add(validator);

        TokenFilter filter = Mockito.spy(TokenFilter.class);
        Mockito.doReturn(new DefaultTokenFilter()).when(filter).getDefaultTokenFilter();
        Mockito.doReturn(validators).when(filter).getValidators();
        PowerMockito.when(IdentityCommonUtil.getRegulatoryFromSPMetaData("iYpRm64b2vmvmKDhdL6KZD9z6fca"))
                .thenReturn(true);
        PowerMockito.when(IdentityCommonUtil.getMTLSAuthHeader()).thenReturn(TestConstants.CERTIFICATE_HEADER);
        PowerMockito.when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        Mockito.when(oAuthServerConfiguration.isFapiCiba()).thenReturn(true);
        filter.doFilter(request, response, filterChain);

        Map<String, String> responseMap = getResponse(response.getOutputStream());
        assertEquals(response.getStatus(), HttpStatus.SC_UNAUTHORIZED);
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR), "invalid_client");
        assertEquals(responseMap.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION),
                "FAPI unsupported signing algorithm RS256 used to sign the JWT");
    }

    public static Map<String, String> getResponse(ServletOutputStream outputStream) {

        Map<String, String> response = new HashMap<>();
        JSONObject outputStreamMap = new JSONObject(outputStream);
        JSONObject targetStream = new JSONObject(outputStreamMap.get(TestConstants.TARGET_STREAM).toString());
        response.put(IdentityCommonConstants.OAUTH_ERROR,
                targetStream.get(IdentityCommonConstants.OAUTH_ERROR).toString());
        response.put(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION,
                targetStream.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION).toString());
        return response;
    }

}
