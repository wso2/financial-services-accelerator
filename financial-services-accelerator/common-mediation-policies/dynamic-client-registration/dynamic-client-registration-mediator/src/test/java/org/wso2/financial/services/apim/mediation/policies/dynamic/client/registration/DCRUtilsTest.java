/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration;

import org.apache.commons.lang3.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.util.DCRConstants;
import org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.util.DCRUtil;
import org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.utils.DCRTestConstants;

import java.text.ParseException;

/**
 * Unit tests for DCRUtils.
 */
public class DCRUtilsTest {

    private static final org.apache.axis2.context.MessageContext axis2MC = Mockito.spy(
            org.apache.axis2.context.MessageContext.class);

    @Test
    public void testIsMessageContextBuilt() {

        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, true);
        boolean isMessageContextBuilt = DCRUtil.isMessageContextBuilt(axis2MC);
        Assert.assertTrue(isMessageContextBuilt);
    }

    @Test
    public void testIsMessageContextBuiltForFalse() {

        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, false);
        boolean isMessageContextBuilt = DCRUtil.isMessageContextBuilt(axis2MC);
        Assert.assertFalse(isMessageContextBuilt);
    }

    @Test
    public void testDecodeRequestJWT() throws ParseException {

        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6I" +
                "kpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTc0MTk0MzM0Nn0.NVWtXLE-41ykk2xkiVnWrN_f" +
                "InvonW6KeD6GfrtxmVDnJHuMXuro8qS-4iAN9Jzgh_j0R-ZiM-p_6kGqxsYVB4z3tcJ85kgVep2PpV-4" +
                "ZaDzoNPb24yEiV4Wr36SLLomnssqFuqNVe_LkJgoAMlT8zJpsLyNW9LAOmsmoEqQIR9r0T8nbLvZWsUN" +
                "fUbPjDMrijUcPh3z5TeAs0dqW938AGNJO93b_PB6qWWio7m9QtWkE95jhZVboYgrj3JYtqlycQN9Dj5F" +
                "BZ82WfQAL1tqkzFicKPErKXnlyoPreOGKJXUzT_m5OHnfybc_F78NqaFetIy8F0n-X9j7QMj2vRj_w";
        String header = "{\"typ\":\"JWT\",\"alg\":\"RS256\"}";
        String body = "{\"sub\":\"1234567890\",\"name\":\"John Doe\",\"admin\":true,\"iat\":1741943346}";

        String decodeJwtBody = DCRUtil.decodeRequestJWT(jwt, DCRConstants.JWT_BODY);
        Assert.assertEquals(decodeJwtBody.trim(), body);

        String decodeJwtHeader = DCRUtil.decodeRequestJWT(jwt, DCRConstants.JWT_HEAD);
        Assert.assertEquals(decodeJwtHeader, header);

        String decodeValue = DCRUtil.decodeRequestJWT(jwt, "test");
        Assert.assertEquals(decodeValue, StringUtils.EMPTY);
    }

    @Test
    public void testAppendISDcrRequestPayloadAttributes() {
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        DCRUtil.appendISDcrRequestPayloadAttributes(messageContext,
                new JSONObject(DCRTestConstants.DECODED_DCR_REQUEST), new JSONObject(DCRTestConstants.DECODED_SSA),
                true, "software_client_name", "software_jwks_endpoint");

        Mockito.verify(messageContext).setProperty(DCRConstants.CLIENT_NAME, "9ZzFFBxSLGEjPZogRAbvFd");
        Mockito.verify(messageContext).setProperty(DCRConstants.JWKS_URI,
                "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/9ZzFFBxSLGEjPZogRAbvFd.jwks");
        Mockito.verify(messageContext).setProperty(DCRConstants.APP_DISPLAY_NAME, "WSO2_Open_Banking_TPP__Sandbox_");
    }

    @Test
    public void testAppendISDcrRequestPayloadAttributesWithoutSSA() {
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        DCRUtil.appendISDcrRequestPayloadAttributes(messageContext,
                new JSONObject(DCRTestConstants.DECODED_DCR_REQUEST), null,
                true, "software_id", "token_endpoint_auth_method");

        Mockito.verify(messageContext).setProperty(DCRConstants.CLIENT_NAME, "9ZzFFBxSLGEjPZogRAbvFd");
        Mockito.verify(messageContext).setProperty(DCRConstants.JWKS_URI, "private_key_jwt");
        Mockito.verify(messageContext).setProperty(DCRConstants.APP_DISPLAY_NAME, "9ZzFFBxSLGEjPZogRAbvFd");
    }

    @Test
    public void testGetErrorResponse() {
        String errorResponse = DCRUtil.getErrorResponse("400", "Invalid request parameters");

        Assert.assertTrue(errorResponse.contains("400"));
        Assert.assertTrue(errorResponse.contains("Invalid request parameters"));
    }

    @Test
    public void testGetApplicationName() {
        String appName = DCRUtil.getApplicationName(new JSONObject(DCRTestConstants.DECODED_DCR_REQUEST),
                new JSONObject(DCRTestConstants.DECODED_SSA), false, "software_id");

        Assert.assertEquals(appName, "9ZzFFBxSLGEjPZogRAbvFd");
    }

    @Test
    public void testGetApplicationNameWithoutSSA() {
        String appName = DCRUtil.getApplicationName(new JSONObject(DCRTestConstants.DECODED_DCR_REQUEST),
                null, false, "software_id");

        Assert.assertEquals(appName, "9ZzFFBxSLGEjPZogRAbvFd");
    }
}
