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

import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Test class for OBCIBARequestObjectValidationExtension.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({JWTClaimsSet.class, OAuth2Parameters.class, RequestObject.class, JSONObject.class})
public class OBCIBARequestObjectValidationExtensionTest extends PowerMockTestCase {

    private final String dummyString = "dummyString";

    @Test(expectedExceptions = RequestObjectException.class, description = "Empty intent key")
    public void validateRequestObjectInvalidIntentKeyTest() throws Exception {

        OBCIBARequestObjectValidationExtensionMock obcibaRequestObjectValidationExtensionMock =
                new OBCIBARequestObjectValidationExtensionMock();

        JSONObject intent = mock(JSONObject.class);

        RequestObject requestObject = mock(RequestObject.class);
        OAuth2Parameters oAuth2Parameters = mock(OAuth2Parameters.class);
        JWTClaimsSet claimsSet = Mockito.mock(JWTClaimsSet.class);

        Mockito.when(requestObject.getClaimsSet()).thenReturn(claimsSet);
        Mockito.when(claimsSet.getJSONObjectClaim(Mockito.anyString())).thenReturn(intent);

        when(intent.getAsString(dummyString)).thenReturn(dummyString);

        obcibaRequestObjectValidationExtensionMock.validateRequestObject(requestObject, oAuth2Parameters);

    }

    @Test(description = "success scenario")
    public void validateRequestObjectValidObjectTest() throws Exception {

        OBCIBARequestObjectValidationExtensionMock obcibaRequestObjectValidationExtensionMock =
                new OBCIBARequestObjectValidationExtensionMock();

        JSONObject intent = mock(JSONObject.class);

        RequestObject requestObject = mock(RequestObject.class);
        OAuth2Parameters oAuth2Parameters = mock(OAuth2Parameters.class);
        JWTClaimsSet claimsSet = Mockito.mock(JWTClaimsSet.class);

        Mockito.when(requestObject.getClaimsSet()).thenReturn(claimsSet);
        Mockito.when(claimsSet.getJSONObjectClaim(Mockito.anyString())).thenReturn(intent);

        when(intent.getAsString("value")).thenReturn(dummyString);

        obcibaRequestObjectValidationExtensionMock.validateRequestObject(requestObject, oAuth2Parameters);

    }
}

class OBCIBARequestObjectValidationExtensionMock extends OBCIBARequestObjectValidationExtension {

    @Override
    boolean validateIAMConstraints(RequestObject requestObject,
                                   OAuth2Parameters oAuth2Parameters) throws RequestObjectException {
        return Mockito.anyBoolean();
    }
}
