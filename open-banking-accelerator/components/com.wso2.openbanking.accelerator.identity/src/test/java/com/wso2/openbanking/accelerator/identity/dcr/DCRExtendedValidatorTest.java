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

import com.google.gson.Gson;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.model.SoftwareStatementBody;
import com.wso2.openbanking.accelerator.identity.dcr.util.ExtendedSoftwareStatementBody;
import com.wso2.openbanking.accelerator.identity.dcr.util.ExtendedValidatorImpl;
import com.wso2.openbanking.accelerator.identity.dcr.util.RegistrationTestConstants;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for DCR extended validator.
 */
@PrepareForTest({OpenBankingConfigParser.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class DCRExtendedValidatorTest extends PowerMockTestCase {

    OpenBankingConfigParser openBankingConfigParserMock;
    private RegistrationRequest registrationRequest;
    private ExtendedValidatorImpl extendedValidator = new ExtendedValidatorImpl();

    private static final Log log = LogFactory.getLog(DCRValidationTest.class);

    @BeforeClass
    public void beforeClass() {

        mockOpenBankingConfigParser();
        Map<String, Object> confMap = new HashMap<>();
        Map<String, Map<String, Object>> dcrRegistrationConfMap = new HashMap<>();
        Gson gson = new Gson();
        registrationRequest = gson.fromJson(RegistrationTestConstants.extendedRegistrationRequestJson,
                RegistrationRequest.class);
        String decodedSSA = null;
        try {
            decodedSSA = JWTUtils
                    .decodeRequestJWT(registrationRequest.getSoftwareStatement(), "body").toJSONString();
        } catch (ParseException e) {
            log.error("Error while parsing the SSA", e);
        }
        extendedValidator.setSoftwareStatementPayload(registrationRequest, decodedSSA);

        List<String> validAlgorithms = new ArrayList<>();
        validAlgorithms.add("PS256");
        validAlgorithms.add("ES256");
        confMap.put(OpenBankingConstants.SIGNATURE_ALGORITHMS, validAlgorithms);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(confMap);
        IdentityExtensionsDataHolder.getInstance().setDcrRegistrationConfigMap(dcrRegistrationConfMap);
    }

    private void mockOpenBankingConfigParser() {
        openBankingConfigParserMock = Mockito.mock(OpenBankingConfigParser.class);
        Map<String, Object> configMap = new HashMap<>();
        Mockito.when(openBankingConfigParserMock.getConfiguration()).thenReturn(configMap);
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
    }

    @Test
    public void testExtendedValidatorFailure() {

        try {
            extendedValidator.validatePost(registrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Redirect URIs can not be null"));

        }
    }

    @Test
    public void testExtendedSSAAttributes() {

        SoftwareStatementBody softwareStatementBody = registrationRequest.getSoftwareStatementBody();
        Assert.assertEquals(((ExtendedSoftwareStatementBody) softwareStatementBody).getLogURI(),
                "https://wso2.com/wso2.jpg");
    }

    @Test
    public void testExtendedRegistrationResponse() {
        String additionalAttributes = "\"additional_attribute_1\":\"111111\",\"additional_attribute_2\":\"222222\"";
        String registrationResponse = extendedValidator.getRegistrationResponse(new HashMap<>());
        Assert.assertTrue(registrationResponse.contains(additionalAttributes));
    }

}
