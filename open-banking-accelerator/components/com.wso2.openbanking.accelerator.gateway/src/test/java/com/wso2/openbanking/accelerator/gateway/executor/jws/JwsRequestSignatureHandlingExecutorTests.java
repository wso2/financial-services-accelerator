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

package com.wso2.openbanking.accelerator.gateway.executor.jws;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Test class for JwsRequestSignatureHandlingExecutor.
 */

@PowerMockIgnore({"jdk.internal.reflect.*", "javax.management.*"})
@PrepareForTest({OpenBankingConfigParser.class})

public class JwsRequestSignatureHandlingExecutorTests {

    private static final Log log = LogFactory.getLog(JwsRequestSignatureHandlingExecutorTests.class);

    private OBAPIRequestContext obapiRequestContextMock;

    private MsgInfoDTO msgInfoDTO;

    JwsRequestSignatureHandlingExecutor jwsRequestSignatureHandlingExecutor;

    Map<String, String> sampleRequestHeaders = new HashMap<>();

    public JwsRequestSignatureHandlingExecutorTests() throws Exception {
        sampleRequestHeaders.put("Authorization", "Bearer 2YotnFZFEjr1zCsicMWpAA");
        sampleRequestHeaders.put("x-idempotency-key", "FRESCO.21302.GFX.20");
        sampleRequestHeaders.put("x-jws-signature", "TGlmZSdzIGEgam91cm5leSBub3QgYSBkZXN0aW5hdGlvbiA=" +
                "..T2ggZ29vZCBldmVuaW5nIG1yIHR5bGVyIGdvaW5nIGRvd24gPw==");
        sampleRequestHeaders.put("x-fapi-auth-date", "Sun, 10 Sep 2017 19:43:31 GMT");
        sampleRequestHeaders.put("x-fapi-customer-ip-address", "104.25.212.99");
        sampleRequestHeaders.put("x-fapi-interaction-id", "93bac548-d2de-4546-b106-880a5018460d");
        sampleRequestHeaders.put("Content-Type", "text/xml");
        sampleRequestHeaders.put("Accept", "application/json");
    }

    String samplejwsSignature = "V2hhdCB3ZSBnb3QgaGVyZQ0K..aXMgZmFpbHVyZSB0byBjb21tdW5pY2F0ZQ0K";

    String sampleJWEsignature = "V2hhdCB3ZSBnb3QgaGVyZQ0K.V2hhdCB3ZSBnb3QgaGVyZQ0K." +
            "V2hhdCB3ZSBnb3QgaGVyZQ0K.V2hhdCB3ZSBnb3QgaGVyZQ0K.V2hhdCB3ZSBnb3QgaGVyZQ0K";

    String sampleRequestPayload = "{\"Data\":{\"Initiation\":{\"InstructionIdentification\":" +
            "\"ACME412\",\"EndToEndIdentification\":\"FRESCO.21302.GFX.20\",\"InstructedAmount\":" +
            "{\"Amount\":\"165.88\",\"Currency\":\"GBP\"},\"CreditorAccount\":" +
            "{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"08080021325698\"" +
            "\"Name\":\"ACME Inc\",\"SecondaryIdentification\":\"0002\"}," +
            "\"RemittanceInformation\":{\"Reference\":\"FRESCO-101\",\"Unstructured\":\"" +
            "Internal ops code 5120101\"}}}," +
            "\"Risk\":{" +
            "{\"PaymentContextCode\":\"EcommerceGoods\",\"MerchantCategoryCode\":\"5967\"," +
            "\"MerchantCustomerIdentification\":\"053598653254\",\"DeliveryAddress\":{" +
            "\"AddressLine\":[\"Flat 7\",\"Acacia Lodge\"]," +
            "\"StreetName\":\"Acacia Avenue\",\"BuildingNumber\":\"27\",\"PostCode\":\"GU31 2ZZ\"" +
            "\"TownName\":\"Sparsholt,\"CountrySubDivision\":\"Wessex\",\"Country\":\"UK\"}}}";

    String requestHeaderb64False = "{\n" +
            "\"alg\": \"PS256\",\n" +
            "\"kid\": \"12345\",\n" +
            "\"b64\": false, \n" +
            "\"http://openbanking.org.uk/iat\": 1739485930,\n" +
            "\"http://openbanking.org.uk/iss\": \"http://openbanking.org.uk\", \n" +
            "\"crit\": [ \"b64\", \"http://openbanking.org.uk/iat\",\n" +
            "\"http://openbanking.org.uk/iss\"] \n" +
            "}";
    String requestHeaderb64None = "{\n" +
            "\"alg\": \"PS256\",\n" +
            "\"kid\": \"12345\",\n" +
            "\"http://openbanking.org.uk/iat\": 1739485930,\n" +
            "\"http://openbanking.org.uk/iss\": \"http://openbanking.org.uk\", \n" +
            "\"crit\": [ \"http://openbanking.org.uk/iat\",\n" +
            "\"http://openbanking.org.uk/iss\"] \n" +
            "}";

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);
        jwsRequestSignatureHandlingExecutor = new JwsRequestSignatureHandlingExecutor();
    }

    /**
     * Test a request with an error.
     * @throws Exception
     */
    @Test
    public void testWithErrorRequest() throws Exception {

        // Mocking request headers
        obapiRequestContextMock = mock(OBAPIRequestContext.class);
        msgInfoDTO = mock(MsgInfoDTO.class);
        PowerMockito.when(obapiRequestContextMock.isError()).thenReturn(true);
        PowerMockito.when(msgInfoDTO.getHttpMethod()).
                thenReturn(HttpMethod.POST);

        boolean isPreProcessValidationPassed = WhiteboxImpl.invokeMethod(
                this.jwsRequestSignatureHandlingExecutor,
                "preProcessValidation", obapiRequestContextMock, sampleRequestHeaders);
        log.debug("Preprocess Validation passed? " + isPreProcessValidationPassed);

        //should return false
        Assert.assertFalse(isPreProcessValidationPassed);
    }

    /**
     * Test if a JWS consisting of 3 parts are returned at reconstructing JWS.
     * @throws Exception
     */
    @Test
    public void testReconstructJWS() throws Exception {

        String reconstructedJWS = WhiteboxImpl.invokeMethod(this.jwsRequestSignatureHandlingExecutor,
                "reconstructJws", samplejwsSignature, sampleRequestPayload);
        log.debug("The reconstructed JWS,  " + reconstructedJWS);

        Assert.assertTrue(reconstructedJWS.split("\\.").length == 3);
    }

    /**
     * Test reconstructing a JWS with a Payload with empty string.
     * @throws Exception
     */
    @Test
    public void testReconstructJWSException() throws Exception {

        Method method = JwsRequestSignatureHandlingExecutor.class.getDeclaredMethod(
                "reconstructJws", String.class, String.class);
        method.setAccessible(true);

        Assert.expectThrows(Exception.class, ()->
                method.invoke(this.jwsRequestSignatureHandlingExecutor, samplejwsSignature, ""));

    }

    /**
     * Test reconstructing a JWS if the passed value is a JWE.
     * @throws Exception
     */
    @Test
    public void testReconstructJWE() throws Exception {

        Method method = JwsRequestSignatureHandlingExecutor.class.getDeclaredMethod(
                "reconstructJws", String.class, String.class);
        method.setAccessible(true);

        Assert.expectThrows(Exception.class, ()->
                method.invoke(this.jwsRequestSignatureHandlingExecutor,
                        sampleJWEsignature, sampleRequestPayload));
    }

    /**
     * Test a request with a JWS having b64 header claim set to false.
     * @throws Exception
     */
    @Test
    public void testb64FalseSigningInput() throws Exception {

        JWSAlgorithm signJWSAlg = JWSAlgorithm.parse("PS256");
        HashSet hs = new HashSet<String>();
        hs.add("crit");
        JWSHeader header = new JWSHeader(signJWSAlg, null, null, hs, null,
                null, null, null, null, null, "samplekid",
                null, null);
        byte[] input = WhiteboxImpl.invokeMethod(this.jwsRequestSignatureHandlingExecutor,
                "getSigningInput", header, sampleRequestPayload);
        Assert.assertNotNull(input);
    }

    /**
     * Test with a JOSE header b64 claim set to false.
     * @throws Exception
     */
    @Test
    public void testb64Verifiability() throws Exception {

        JWSHeader header = JWSHeader.parse(requestHeaderb64False);
        JWSObject jwsObject = new JWSObject(header, new Payload(sampleRequestPayload));
        Method method = JwsRequestSignatureHandlingExecutor.class.getDeclaredMethod(
                "isB64HeaderVerifiable", JWSObject.class);
        method.setAccessible(true);
        boolean isB64Verifiable = (boolean) method.invoke(this.jwsRequestSignatureHandlingExecutor, jwsObject);

        Assert.assertFalse(isB64Verifiable);
    }

    /**
     * Test a JOSE header with no b64 claim.
     * @throws Exception
     */
    @Test
    public void testb64VerifiabilityWithNoClaim() throws Exception {

        JWSHeader header = JWSHeader.parse(requestHeaderb64None);
        JWSObject jwsObject = new JWSObject(header, new Payload(sampleRequestPayload));
        Method method = JwsRequestSignatureHandlingExecutor.class.getDeclaredMethod(
                "isB64HeaderVerifiable", JWSObject.class);
        method.setAccessible(true);
        boolean isB64Verifiable = (boolean) method.invoke(this.jwsRequestSignatureHandlingExecutor, jwsObject);

        Assert.assertTrue(isB64Verifiable);
    }

    @Test
    public void testHandleRequestInternalServerError() {

        obapiRequestContextMock = mock(OBAPIRequestContext.class);

        GatewayUtils.handleRequestInternalServerError(obapiRequestContextMock, "Error",
                OpenBankingErrorCodes.SERVER_ERROR_CODE);
    }

    @Test
    public void testHandleJwsSignatureErrors() throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {

        obapiRequestContextMock = mock(OBAPIRequestContext.class);
        Method method = JwsRequestSignatureHandlingExecutor.class.getDeclaredMethod(
                "handleJwsSignatureErrors", OBAPIRequestContext.class, String.class, String.class);

        method.invoke(this.jwsRequestSignatureHandlingExecutor, obapiRequestContextMock, "Error",
                OpenBankingErrorCodes.BAD_REQUEST_CODE);
    }

    @Test
    public void testValidateClaims() throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {

        List<String> alg = new ArrayList<>();
        alg.add("PS256");
        OpenBankingConfigParser openBankingConfigParserMock = Mockito.mock(OpenBankingConfigParser.class);
        Mockito.doReturn(alg).when(openBankingConfigParserMock).getJwsRequestSigningAlgorithms();

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);

        obapiRequestContextMock = mock(OBAPIRequestContext.class);
        JWSAlgorithm signJWSAlg = JWSAlgorithm.parse("PS256");
        HashSet hs = new HashSet<String>();
        hs.add("crit");
        JWSHeader header = new JWSHeader(signJWSAlg, null, null, hs, null,
                null, null, null, null, null, "samplekid",
                null, null);
        Method method = JwsRequestSignatureHandlingExecutor.class.getDeclaredMethod(
                "validateClaims", OBAPIRequestContext.class, JWSHeader.class, String.class,
                String.class);

        boolean result = (boolean) method.invoke(this.jwsRequestSignatureHandlingExecutor,
                obapiRequestContextMock, header, "TestApp", null);

        Assert.assertTrue(result);
    }
}
