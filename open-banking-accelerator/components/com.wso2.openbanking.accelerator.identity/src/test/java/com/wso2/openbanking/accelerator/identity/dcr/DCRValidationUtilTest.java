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
package com.wso2.openbanking.accelerator.identity.dcr;

import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.dcr.util.RegistrationTestConstants;
import com.wso2.openbanking.accelerator.identity.dcr.utils.ValidatorUtils;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.HashMap;

/**
 * Test for DCR validation util.
 */
public class DCRValidationUtilTest {

    private static final Log log = LogFactory.getLog(DCRValidationTest.class);

    @Test
    public void testJWTDecodeHead() {

        try {
            JSONObject decodedObject = JWTUtils.decodeRequestJWT(RegistrationTestConstants.SSA, "head");
            Assert.assertEquals(decodedObject.getAsString("alg"), "PS256");
        } catch (ParseException e) {
            log.error("Error while parsing the jwt", e);
        }

    }

    @Test
    public void testJWTDecodeBody() {

        try {
            JSONObject decodedObject = JWTUtils.decodeRequestJWT(RegistrationTestConstants.SSA, "body");
            Assert.assertEquals(decodedObject.getAsString("software_environment"), "sandbox");
        } catch (ParseException e) {
            log.error("Error while parsing the jwt", e);
        }
    }

    @Test
    public void testGetRegistrationClientURI() {
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(new HashMap<>());
        String registrationClientURI = ValidatorUtils.getRegistrationClientURI();
        Assert.assertEquals(registrationClientURI, "https://localhost:8243/open-banking/0.1/register/");
    }
}
