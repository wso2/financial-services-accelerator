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
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.util.RegistrationTestConstants;
import com.wso2.openbanking.accelerator.identity.dcr.utils.ValidatorUtils;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DefaultRegistrationValidatorImpl;
import com.wso2.openbanking.accelerator.identity.dcr.validation.RegistrationValidator;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for DCR validation.
 */
public class DCRValidationTest {

    private static final Log log = LogFactory.getLog(DCRValidationTest.class);

    private RegistrationValidator registrationValidator;
    private RegistrationRequest registrationRequest;
    private static final String NULL = "null";

    @BeforeClass
    public void beforeClass() {

        Map<String, Object> confMap = new HashMap<>();
        Map<String, Map<String, Object>> dcrRegistrationConfMap = new HashMap<>();
        Map<String, Map<String, Object>> dcrRegistrationConfMap2 = new HashMap<>();
        List<String> registrationParams = Arrays.asList("Issuer:true:null",
                "TokenEndPointAuthentication:true:private_key_jwt", "ResponseTypes:true:code id_token",
                "GrantTypes:true:authorization_code,refresh_token", "ApplicationType:false:web",
                "IdTokenSignedResponseAlg:true:null", "SoftwareStatement:true:null", "Scope:false:accounts,payments");
        confMap.put(DCRCommonConstants.DCR_VALIDATOR, "com.wso2.openbanking.accelerator.identity.dcr" +
                ".validation.DefaultRegistrationValidatorImpl");
        List<String> validAlgorithms = new ArrayList<>();
        validAlgorithms.add("PS256");
        validAlgorithms.add("ES256");
        confMap.put(OpenBankingConstants.SIGNATURE_ALGORITHMS, validAlgorithms);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(confMap);
        String dcrValidator = confMap.get(DCRCommonConstants.DCR_VALIDATOR).toString();
        registrationValidator = getDCRValidator(dcrValidator);
        registrationRequest = getRegistrationRequestObject(RegistrationTestConstants.registrationRequestJson);
        //set registration parameter values for testing
        for (String param : registrationParams) {
            setParamConfig(param, dcrRegistrationConfMap);
        }
        IdentityExtensionsDataHolder.getInstance().setDcrRegistrationConfigMap(dcrRegistrationConfMap);
    }

    @Test
    public void testInvalidAlgorithm() {

        registrationRequest.setIdTokenSignedResponseAlg("RS256");

        String decodedSSA = null;
        try {
            decodedSSA = JWTUtils
                    .decodeRequestJWT(registrationRequest.getSoftwareStatement(), "body").toJSONString();
        } catch (ParseException e) {
            log.error("Error while parsing the SSA", e);
        }
        registrationValidator.setSoftwareStatementPayload(registrationRequest, decodedSSA);
        try {
            ValidatorUtils.getValidationViolations(registrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Invalid signing algorithm sent"));
        }
    }

    @Test(dependsOnMethods = "testInvalidAlgorithm")
    public void testInvalidIssuer() {

        registrationRequest.setIdTokenSignedResponseAlg("PS256");
        registrationRequest.setIssuer("222");

        try {
            ValidatorUtils.getValidationViolations(registrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Invalid issuer"));
        }

    }

    @Test(dependsOnMethods = "testInvalidIssuer")
    public void testIssuerExists() {

        registrationRequest.setIssuer(null);

        try {
            ValidatorUtils.getValidationViolations(registrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Required parameter issuer cannot be null"));
        }

    }

    @Test(dependsOnMethods = "testIssuerExists")
    public void testTokenEndPointAuthMethodExists() {

        registrationRequest.setIssuer("9b5usDpbNtmxDcTzs7GzKp");
        registrationRequest.setTokenEndPointAuthentication("");

        try {
            ValidatorUtils.getValidationViolations(registrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription()
                    .contains("Required parameter tokenEndPointAuthentication cannot be empty"));
        }
    }

    @Test(dependsOnMethods = "testTokenEndPointAuthMethodExists")
    public void testResponseTypesExists() {

        registrationRequest.setTokenEndPointAuthentication("private_key_jwt");
        registrationRequest.setResponseTypes(new ArrayList<>());

        try {
            ValidatorUtils.getValidationViolations(registrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription()
                    .contains("Required parameter responseTypes cannot be empty"));
        }
    }

    @Test(dependsOnMethods = "testResponseTypesExists")
    public void testGrantTypesExists() {

        List responseTypeList = new ArrayList<String>();
        responseTypeList.add("code id_token");
        registrationRequest.setResponseTypes(responseTypeList);

        registrationRequest.setGrantTypes(null);

        try {
            ValidatorUtils.getValidationViolations(registrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Required parameter grantTypes cannot be null"));
        }
    }

    @Test(dependsOnMethods = "testGrantTypesExists")
    public void testIdTokenSignedResponseAlgExists() {

        List grantTypeList = new ArrayList<String>();
        grantTypeList.add("authorization_code");
        grantTypeList.add("refresh_token");
        registrationRequest.setGrantTypes(grantTypeList);

        registrationRequest.setIdTokenSignedResponseAlg(null);

        try {
            ValidatorUtils.getValidationViolations(registrationRequest);
        } catch (OpenBankingException e) {
            Assert.assertTrue(e.getMessage().contains("Required parameter idTokenSignedResponseAlg cannot be null"));
        }
    }

    @Test(dependsOnMethods = "testIdTokenSignedResponseAlgExists")
    public void testValidationViolations() {

        try {
            ValidatorUtils.getValidationViolations(registrationRequest);
        } catch (OpenBankingException e) {
            Assert.assertTrue(e.getMessage().contains("Required parameter idTokenSignedResponseAlg cannot be null"));
        }
    }

    @Test(dependsOnMethods = "testValidationViolations")
    public void testDefaultValidator() {

        try {
            registrationValidator.validatePost(registrationRequest);

            registrationValidator.validateUpdate(registrationRequest);

            registrationValidator.validateGet("1234");

            registrationValidator.validateDelete("1234");
        } catch (OpenBankingException e) {
            Assert.assertTrue(e.getMessage().contains("Required parameter idTokenSignedResponseAlg cannot be null"));
        }
    }

    @Test(dependsOnMethods = "testDefaultValidator")
    public void testSoftwareStatementExists() {

        registrationRequest.setIdTokenSignedResponseAlg("PS256");
        registrationRequest.setSoftwareStatement(null);

        try {
            ValidatorUtils.getValidationViolations(registrationRequest);
        } catch (OpenBankingException e) {
            Assert.assertTrue(e.getMessage().contains("Required parameter softwareStatement cannot be null"));
        }
    }

    @Test(dependsOnMethods = "testSoftwareStatementExists")
    public void testSSAParsingException() {

        registrationRequest.setSoftwareStatement("effff");
        try {
            ValidatorUtils.getValidationViolations(registrationRequest);
        } catch (OpenBankingException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid issuer"));
        }
    }

    @Test (dependsOnMethods = "testSSAParsingException")
    public void testResponseTypesAllowedValues() {

        List responseTypeList = new ArrayList<String>();
        responseTypeList.add("");
        registrationRequest.setResponseTypes(responseTypeList);
        try {
            ValidatorUtils.getValidationViolations(registrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Invalid responseTypes provided"));
        }
    }

    @Test (dependsOnMethods = "testResponseTypesAllowedValues")
    public void testApplicationTypeAllowedValues() {

        List responseTypeList = new ArrayList<String>();
        responseTypeList.add("code id_token");
        registrationRequest.setResponseTypes(responseTypeList);
        registrationRequest.setApplicationType("");
        try {
            ValidatorUtils.getValidationViolations(registrationRequest);
        } catch (DCRValidationException e) {
            Assert.assertTrue(e.getErrorDescription().contains("Invalid applicationType provided"));
        }
    }

    private static RegistrationRequest getRegistrationRequestObject(String request) {

        Gson gson = new Gson();
        return gson.fromJson(request, RegistrationRequest.class);
    }

    public static RegistrationValidator getDCRValidator(String dcrValidator)  {

        if (StringUtils.isEmpty(dcrValidator)) {
            return new DefaultRegistrationValidatorImpl();
        }
        try {
            return (RegistrationValidator) Class.forName(dcrValidator).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Error instantiating " + dcrValidator, e);
            return new DefaultRegistrationValidatorImpl();
        } catch (ClassNotFoundException e) {
            log.error("Cannot find class: " + dcrValidator, e);
            return new DefaultRegistrationValidatorImpl();
        }
    }

    private void setParamConfig(String configParam, Map<String, Map<String, Object>> dcrRegistrationConfMap) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put(DCRCommonConstants.DCR_REGISTRATION_PARAM_REQUIRED, configParam.split(":")[1]);
        if (!NULL.equalsIgnoreCase(configParam.split(":")[2])) {
            List<String> allowedValues = new ArrayList<>();
            allowedValues.addAll(Arrays.asList(configParam.split(":")[2].split(",")));
            parameterValues.put(DCRCommonConstants.DCR_REGISTRATION_PARAM_ALLOWED_VALUES, allowedValues);
        }
        dcrRegistrationConfMap.put(configParam.split(":")[0], parameterValues);
    }
}
