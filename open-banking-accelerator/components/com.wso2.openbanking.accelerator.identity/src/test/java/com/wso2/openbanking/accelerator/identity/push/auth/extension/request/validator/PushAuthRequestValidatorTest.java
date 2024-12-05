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

package com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.ServiceProviderUtils;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.constants.PushAuthRequestConstants;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.exception.PushAuthRequestValidatorException;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.model.PushAuthErrorResponse;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.util.test.jwt.builder.TestJwtBuilder;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import net.minidev.json.JSONObject;
import org.junit.Assert;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientValidationResponseDTO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test for push authorization request validator.
 */
@PrepareForTest({IdentityExtensionsDataHolder.class, OAuth2Util.class, OAuthServerConfiguration.class,
        IdentityUtil.class, ServiceProviderUtils.class, IdentityCommonUtil.class, OpenBankingConfigParser.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class PushAuthRequestValidatorTest extends PowerMockTestCase {

    private Map<String, List<String>> parameterMap;
    private Map<String, Object> configMap;
    private PushAuthRequestValidator pushAuthRequestValidator;
    private HttpServletRequest httpServletRequestMock;
    private ServiceProviderProperty[] spProperties;
    private ServiceProvider serviceProviderMock;

    @BeforeClass
    public void setup() throws Exception {

        parameterMap = new HashMap<>();
        configMap = new HashMap<>();
        parameterMap.put("request", Arrays.asList(TestJwtBuilder.getValidSignedJWT()));
        configMap.put("SignatureValidation.AllowedAlgorithms.Algorithm",
                new ArrayList<>(Arrays.asList("PS256 ES256".split(" "))));
        httpServletRequestMock = mock(HttpServletRequest.class);
        pushAuthRequestValidator = new PushAuthRequestValidator();

        serviceProviderMock = new ServiceProvider();

        ServiceProviderProperty serviceProviderProperty = new ServiceProviderProperty();
        serviceProviderProperty.setName("scope");
        serviceProviderProperty.setValue("accounts payments");
        spProperties = new ServiceProviderProperty[1];
        spProperties[0] = serviceProviderProperty;
        serviceProviderMock.setSpProperties(spProperties);
    }

    @BeforeMethod
    public void initMethods() throws OpenBankingException, IdentityApplicationManagementException {

        IdentityExtensionsDataHolder identityExtensionsDataHolderMock = mock(IdentityExtensionsDataHolder.class);
        ApplicationManagementService applicationManagementServiceMock = mock(ApplicationManagementService.class);
        OpenBankingConfigParser openBankingConfigParserMock = mock(OpenBankingConfigParser.class);

        mockStatic(IdentityExtensionsDataHolder.class);
        mockStatic(ServiceProviderUtils.class);
        mockStatic(IdentityCommonUtil.class);
        mockStatic(OpenBankingConfigParser.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolderMock);
        when(identityExtensionsDataHolderMock.getConfigurationMap()).thenReturn(configMap);
        when(identityExtensionsDataHolderMock.getApplicationManagementService())
                .thenReturn(applicationManagementServiceMock);
        when(ServiceProviderUtils.getSpTenantDomain(Mockito.anyString())).thenReturn("dummyTenantDomain");
        when(applicationManagementServiceMock.getServiceProviderByClientId(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(serviceProviderMock);
        when(IdentityCommonUtil.getRegulatoryFromSPMetaData(Mockito.anyString())).thenReturn(true);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        when(openBankingConfigParserMock.isOpenIdScopeMandatoryForRegulatoryApps())
                .thenReturn(true);

        OAuthServerConfiguration oAuthServerConfigurationMock = mock(OAuthServerConfiguration.class);
        mockStatic(OAuthServerConfiguration.class);
        when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfigurationMock);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class)
    public void validateRepeatedParametersInRequest() throws Exception {

        parameterMap.put("client_assertion", Arrays.asList("firstParam,secondRepeatedParam".split(",")));
        pushAuthRequestValidator.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateRepeatedParametersInRequest")
    public void validateRequestUriParamInRequest() throws Exception {

        // remove previous invalid parameters
        parameterMap.remove("client_assertion");
        // add new parameters to be tested
        parameterMap.put("request_uri", Arrays.asList("dummyValue"));
        pushAuthRequestValidator.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateRequestUriParamInRequest")
    public void validateFormBodyParamsInRequest() throws Exception {

        // remove previous invalid parameters
        parameterMap.remove("request_uri");
        // add new parameters to be tested
        parameterMap.put("scope", Arrays.asList("dummyScope"));
        pushAuthRequestValidator.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateRequestUriParamInRequest")
    public void validateRequestObject() throws Exception {

        // remove previous invalid parameters
        parameterMap.remove("scope");
        // add new parameters to be tested
        parameterMap.put("request", Arrays.asList("invalidReqObj"));
        pushAuthRequestValidator.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateRequestObject")
    public void validateClientIdInRequest() throws Exception {

        // add parameters to be tested
        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getValidSignedJWT()));
        PushAuthRequestValidatorInvalidClientMock pushAuthRequestValidatorInvalidClientMock =
                new PushAuthRequestValidatorInvalidClientMock();
        pushAuthRequestValidatorInvalidClientMock.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateClientIdInRequest")
    public void validateSignatureAlgInRequestObject() throws Exception {

        // add parameters to be tested
        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getInvalidJWTWithUnsupportedAlgorithm()));
        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateSignatureAlgInRequestObject")
    public void validateNonceInRequestObject() throws Exception {

        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getInvalidJWTWithUnsupportedNonce()));
        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateNonceInRequestObject")
    public void validateScopeParameter() throws Exception {

        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getValidSignedJWT()));
        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class)
    public void validateScopeParameterWithoutOpenIdScopeForRegulatoryApps() throws Exception {

        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getValidSignedJWTWithoutOpenIdScope()));
        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test
    public void validateScopeParameterWithoutOpenIdScopeForNonRegulatoryApps() throws Exception {

        // remove previous invalid parameters
        parameterMap.remove("request_uri");

        when(IdentityCommonUtil.getRegulatoryFromSPMetaData(Mockito.anyString())).thenReturn(false);

        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getValidSignedJWTWithoutOpenIdScope()));
        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        ServiceProviderProperty[] serviceProviderProperties = serviceProviderMock.getSpProperties();
        serviceProviderProperties[0].setValue("accounts payments openid");

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateNonceInRequestObject")
    public void validateUnsupportedClaimsInSignedJWT() throws Exception {

        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getInvalidJWTWithUnsupportedClaims()));

        ServiceProviderProperty[] serviceProviderProperties = serviceProviderMock.getSpProperties();
        serviceProviderProperties[0].setValue("bank:accounts.basic:read bank:accounts.detail:read " +
                "bank:transactions:read bank:payees:read bank:regular_payments:read common:customer.basic:read " +
                "common:customer.detail:read cdr:registration openid");

        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(dependsOnMethods = "validateUnsupportedClaimsInSignedJWT")
    public void successfulParameterValidationFlowForSignedJWT() throws Exception {

        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getValidSignedJWT()));

        ServiceProviderProperty[] serviceProviderProperties = serviceProviderMock.getSpProperties();
        serviceProviderProperties[0].setValue("bank:accounts.basic:read bank:accounts.detail:read " +
                "bank:transactions:read bank:payees:read bank:regular_payments:read common:customer.basic:read " +
                "common:customer.detail:read cdr:registration openid");

        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        Map<String, Object> result = pushAuthRequestValidatorMockClass
                .validateParams(httpServletRequestMock, parameterMap);

        Assert.assertNotNull(result);
    }

    @Test(priority = 1)
    public void testErrorResponseCreation() {

        PushAuthErrorResponse result = pushAuthRequestValidator.createErrorResponse(400,
                PushAuthRequestConstants.INVALID_REQUEST, "Bad Request");

        Assert.assertEquals("Bad Request", result.getPayload().get("error_description").toString());
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateScopeParameter")
    public void validateMissingExpClaimInSignedJWT() throws Exception {
        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getInvalidJWTWithoutExpClaim()));

        ServiceProviderProperty[] serviceProviderProperties = serviceProviderMock.getSpProperties();
        serviceProviderProperties[0].setValue("bank:accounts.basic:read bank:accounts.detail:read " +
                "bank:transactions:read bank:payees:read bank:regular_payments:read common:customer.basic:read " +
                "common:customer.detail:read cdr:registration openid");

        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateMissingExpClaimInSignedJWT")
    public void validateExpClaimOver60MinInSignedJWT() throws Exception {
        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getInvalidJWTWithExpClaimOver60Min()));

        ServiceProviderProperty[] serviceProviderProperties = serviceProviderMock.getSpProperties();
        serviceProviderProperties[0].setValue("bank:accounts.basic:read bank:accounts.detail:read " +
                "bank:transactions:read bank:payees:read bank:regular_payments:read common:customer.basic:read " +
                "common:customer.detail:read cdr:registration openid");

        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateExpClaimOver60MinInSignedJWT")
    public void validateMissingNbfClaimInSignedJWT() throws Exception {
        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getInvalidJWTWithoutNbfClaim()));

        ServiceProviderProperty[] serviceProviderProperties = serviceProviderMock.getSpProperties();
        serviceProviderProperties[0].setValue("bank:accounts.basic:read bank:accounts.detail:read " +
                "bank:transactions:read bank:payees:read bank:regular_payments:read common:customer.basic:read " +
                "common:customer.detail:read cdr:registration openid");

        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateMissingNbfClaimInSignedJWT")
    public void validateNbfClaimOver60MinInSignedJWT() throws Exception {
        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getInvalidJWTWithNbfClaimOver60Min()));

        ServiceProviderProperty[] serviceProviderProperties = serviceProviderMock.getSpProperties();
        serviceProviderProperties[0].setValue("bank:accounts.basic:read bank:accounts.detail:read " +
                "bank:transactions:read bank:payees:read bank:regular_payments:read common:customer.basic:read " +
                "common:customer.detail:read cdr:registration openid");

        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateMissingNbfClaimInSignedJWT")
    public void validateMissingCodeChallengeInSignedJWT() throws Exception {

        // add parameters to be tested
        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getInvalidJWTWithoutCodeChallenge()));
        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateMissingCodeChallengeInSignedJWT")
    public void validateMissingCodeChallengeMethodInSignedJWT() throws Exception {

        // add parameters to be tested
        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getInvalidJWTWithoutCodeChallengeMethod()));
        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(expectedExceptions = PushAuthRequestValidatorException.class,
            dependsOnMethods = "validateMissingCodeChallengeMethodInSignedJWT")
    public void validateMissingResponseTypeInSignedJWT() throws Exception {

        // add parameters to be tested
        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getInvalidJWTWithoutResponseType()));
        PushAuthRequestValidatorMockClass pushAuthRequestValidatorMockClass = new PushAuthRequestValidatorMockClass();

        pushAuthRequestValidatorMockClass.validateParams(httpServletRequestMock, parameterMap);
    }

    @Test(priority = 2, expectedExceptions = PushAuthRequestValidatorException.class)
    public void testDecryptEncryptedReqObjFailure() throws Exception {

        parameterMap.put("request",
                Arrays.asList(TestJwtBuilder.getValidEncryptedJWT()));

        OAuthServerConfiguration oAuthServerConfigurationMock = mock(OAuthServerConfiguration.class);
        mockStatic(OAuthServerConfiguration.class);
        when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfigurationMock);

        mockStatic(OAuth2Util.class);
        OAuthAppDO oAuthAppDOMock = mock(OAuthAppDO.class);
        when(OAuth2Util.getAppInformationByClientId(Mockito.anyString())).thenReturn(oAuthAppDOMock);
        when(OAuth2Util.getTenantDomainOfOauthApp(oAuthAppDOMock)).thenReturn("dummyTenantDomain");

        String path = "src/test/resources";
        File file = new File(path);
        String absolutePathForTestResources = file.getAbsolutePath();
        String absolutePathForKeyStore = absolutePathForTestResources + "/wso2carbon.jks";
        String[] pathParts = absolutePathForKeyStore.split("/");
        String platformAbsolutePathForKeyStore = String.join(File.separator, pathParts);

        InputStream keystoreFile = new FileInputStream(platformAbsolutePathForKeyStore);
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(keystoreFile, "wso2carbon".toCharArray());

        String alias = "wso2carbon";

        // Get the private key. Password for the key store is 'wso2carbon'.
        RSAPrivateKey privateKey = (RSAPrivateKey) keystore.getKey(alias, "wso2carbon".toCharArray());

        when(OAuth2Util.getPrivateKey(Mockito.anyString(), Mockito.anyInt())).thenReturn(privateKey);

        pushAuthRequestValidator.validateParams(httpServletRequestMock, parameterMap);
    }
}

class PushAuthRequestValidatorMockClass extends PushAuthRequestValidator {

    @Override
    protected OAuth2ClientValidationResponseDTO getClientValidationInfo(JSONObject requestBodyJson) {

        OAuth2ClientValidationResponseDTO oAuth2ClientValidationResponseDTO = new OAuth2ClientValidationResponseDTO();
        oAuth2ClientValidationResponseDTO.setValidClient(true);
        return oAuth2ClientValidationResponseDTO;
    }

    @Override
    protected void validateSignature(String requestObjectString, JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

    }

    @Override
    protected void validateAudience(JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

    }
}

class PushAuthRequestValidatorInvalidClientMock extends PushAuthRequestValidator {

    @Override
    protected OAuth2ClientValidationResponseDTO getClientValidationInfo(JSONObject requestBodyJson) {

        OAuth2ClientValidationResponseDTO oAuth2ClientValidationResponseDTO = new OAuth2ClientValidationResponseDTO();
        oAuth2ClientValidationResponseDTO.setValidClient(false);
        return oAuth2ClientValidationResponseDTO;
    }
}
