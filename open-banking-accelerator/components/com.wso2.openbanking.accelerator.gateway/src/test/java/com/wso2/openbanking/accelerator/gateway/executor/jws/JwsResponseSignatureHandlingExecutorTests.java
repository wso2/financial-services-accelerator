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

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.util.Base64URL;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.GatewaySignatureHandlingUtils;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.extensionlistener.PayloadHandler;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Test class for JwsRequestSignatureHandlingExecutor.
 */

@PowerMockIgnore({"jdk.internal.reflect.*", "javax.management.*"})
@PrepareForTest({OpenBankingConfigParser.class})
public class JwsResponseSignatureHandlingExecutorTests {

    JwsResponseSignatureHandlingExecutor jwsResponseSignatureHandlingExecutor;
    @Mock
    OBAPIRequestContext obapiRequestContext;
    @Mock
    OBAPIResponseContext obapiResponseContext;
    @Mock
    APIRequestInfoDTO apiRequestInfoDTO;
    @Mock
    MsgInfoDTO msgInfoDTO;
    @Mock
    PayloadHandler payloadHandler;
    Map<String, String> headers = new HashMap<>();

    private String kid = "1234";

    private HashMap<String, Object> criticalParameters = new HashMap<>();

    private String sampleResponsePayload = "{\n" +
            "    \"Data\": {\n" +
            "        \"Initiation\": {\n" +
            "            \"FileType\": \"UK.OBIE.pain.001.001.08\",\n" +
            "            \"FileHash\": \"sof6XBU7RAkxekFddW38uJ2h2TBknlgLLiRSCP7qVdw=\",\n" +
            "            \"FileReference\": \"test\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

    private String sampleXmlResponsePayload = "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.08\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema- instance\">\n" +
            "  <CstmrCdtTrfInitn>\n" +
            "    <GrpHdr>\n" +
            "      <MsgId>ABC/120928/CCT001</MsgId>\n" +
            "      <CreDtTm>2012-09-28T14:07:00</CreDtTm>\n" +
            "      <NbOfTxs>2</NbOfTxs>\n" +
            "      <CtrlSum>70</CtrlSum>\n" +
            "      <InitgPty>\n" +
            "        <Nm>ABC Corporation</Nm>\n" +
            "        <PstlAdr>\n" +
            "          <StrtNm>Times Square</StrtNm>\n" +
            "          <BldgNb>7</BldgNb>\n" +
            "          <PstCd>NY 10036</PstCd>\n" +
            "          <TwnNm>New York</TwnNm>\n" +
            "          <Ctry>US</Ctry>\n" +
            "        </PstlAdr>\n" +
            "      </InitgPty>\n" +
            "    </GrpHdr>\n" +
            "</Document>";

    private String sampleJWS = "eyJodHRwOlwvXC9vcGVuYmFua2luZy5vwvaXNzIjoiMDAxNTgwMDAwMUhRUXFMyNTYifQ." +
            "TQolWv8OZM90Wq6mqL2TZ_Sj6cQjefo5mCgWLP33qg5WH38oFh1YBaBQ7daAFALFIN6jMw." +
            "hgdyyUcKNoX8bYZzdQvyYBIMkoyxI39rpYUyumxKQEFbNzysihO_f4js5k4L";

    String requestHeaderb64False = "{\n" +
            "\"alg\": \"PS256\",\n" +
            "\"kid\": \"12345\",\n" +
            "\"b64\": false, \n" +
            "\"http://openbanking.org.uk/iat\": 1739485930,\n" +
            "\"http://openbanking.org.uk/iss\": \"http://openbanking.org.uk\", \n" +
            "\"crit\": [ \"b64\", \"http://openbanking.org.uk/iat\",\n" +
            "\"http://openbanking.org.uk/iss\"] \n" +
            "}";

    String requestHedaerb64True = "{\n" +
            "\"alg\": \"PS256\",\n" +
            "\"kid\": \"12345\",\n" +
            "\"b64\": true, \n" +
            "\"http://openbanking.org.uk/iat\": 1739485930,\n" +
            "\"http://openbanking.org.uk/iss\": \"http://openbanking.org.uk\", \n" +
            "\"crit\": [ \"b64\", \"http://openbanking.org.uk/iat\",\n" +
            "\"http://openbanking.org.uk/iss\"] \n" +
            "}";

    String requestHedaerb64None = "{\n" +
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

        jwsResponseSignatureHandlingExecutor = new JwsResponseSignatureHandlingExecutor();

        obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        apiRequestInfoDTO = Mockito.mock(APIRequestInfoDTO.class);
        msgInfoDTO = Mockito.mock(MsgInfoDTO.class);
        payloadHandler = Mockito.mock(PayloadHandler.class);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    /**
     * Test the returned JWS Header with the input parameter.
     */
    @Test
    public void testJWSHeader() {

        criticalParameters.put("iss", "issuer");
        criticalParameters.put("iat", 123456);
        criticalParameters.put("tan", "trustAnchor");
        JWSHeader jwsHeader = GatewaySignatureHandlingUtils.constructJWSHeader(kid, criticalParameters,
                JWSAlgorithm.parse("ES256"));
        Assert.assertTrue("ES256".equals(jwsHeader.getAlgorithm().getName()));
    }

    /**
     * Test make JWSObject.
     */
    @Test
    public void testConstructJWSObject() {

        criticalParameters.put("iss", "issuer");
        criticalParameters.put("iat", 123456);
        criticalParameters.put("tan", "trustAnchor");
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse("EC"))
                .keyID(kid)
                .type(JOSEObjectType.JOSE)
                .criticalParams(criticalParameters.keySet())
                .customParams(criticalParameters)
                .build();

        JWSObject jwsObject = GatewaySignatureHandlingUtils.constructJWSObject(jwsHeader, sampleResponsePayload);

        Assert.assertTrue("EC".equals(jwsObject.getHeader().getAlgorithm().getName()));
    }

    /**
     * Test input to sign when b64 claim set to true.
     */
    @Test
    public void getSigningInput() throws UnsupportedEncodingException {

        criticalParameters.put("iss", "issuer");
        criticalParameters.put("iat", 123456);
        criticalParameters.put("tan", "trustAnchor");
        criticalParameters.put("b64", true);
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse("EC"))
                .keyID(kid)
                .type(JOSEObjectType.JOSE)
                .criticalParams(criticalParameters.keySet())
                .customParams(criticalParameters)
                .build();
        Object signingInput = GatewaySignatureHandlingUtils.getSigningInput(jwsHeader, sampleResponsePayload);
        Assert.assertNotNull(signingInput);
    }

    /**
     * Test a header with b64 claim set to true.
     */
    @Test
    public void testHeaderWithB64True() throws ParseException {

        JWSHeader header = JWSHeader.parse(requestHedaerb64True);
        JWSObject jwsObject = new JWSObject(header, new Payload(sampleResponsePayload));
        boolean isB64Verifiable = GatewaySignatureHandlingUtils.isB64HeaderVerifiable(jwsObject);

        Assert.assertTrue(isB64Verifiable);
    }

    /**
     * Test a header with b64 claim not set.
     */
    @Test
    public void testHeaderWithB64NotSet() throws ParseException {

        JWSHeader header = JWSHeader.parse(requestHedaerb64None);
        JWSObject jwsObject = new JWSObject(header, new Payload(sampleResponsePayload));
        boolean isB64Verifiable = GatewaySignatureHandlingUtils.isB64HeaderVerifiable(jwsObject);

        Assert.assertTrue(isB64Verifiable);
    }

    /**
     * Test a header with b64 claim set to false.
     */
    @Test
    public void testHeaderWithB64False() throws ParseException {

        JWSHeader header = JWSHeader.parse(requestHeaderb64False);
        JWSObject jwsObject = new JWSObject(header, new Payload(sampleResponsePayload));
        boolean isB64Verifiable = GatewaySignatureHandlingUtils.isB64HeaderVerifiable(jwsObject);


        Assert.assertFalse(isB64Verifiable);
    }

    /**
     * Test creating a detached JWS with serialized JWS.
     */
    @Test
    public void testDetachedJWS() throws ParseException {

        JWSHeader header = JWSHeader.parse(requestHeaderb64False);
        Base64URL signature = Base64URL.encode("signature");
        String detachedJWS = GatewaySignatureHandlingUtils.createDetachedJws(header, signature);
        String[] jwsParts = detachedJWS.split("\\.");

        Assert.assertEquals("", jwsParts[1]);
    }

    @Test
    public void testExtractRequestPayloadForJsonPayloads() throws OpenBankingException {

        headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
        Mockito.doReturn(sampleResponsePayload).when(obapiRequestContext).getRequestPayload();

        Optional<String> payload = GatewayUtils.extractRequestPayload(obapiRequestContext, headers);
        Assert.assertNotNull(payload.get());
    }

    @Test
    public void testExtractResponsePayloadForJsonPayloads() throws OpenBankingException {

        headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
        Mockito.doReturn(sampleResponsePayload).when(obapiResponseContext).getResponsePayload();

        Optional<String> payload = GatewayUtils.extractResponsePayload(obapiResponseContext, headers);
        Assert.assertNotNull(payload.get());
    }

}
