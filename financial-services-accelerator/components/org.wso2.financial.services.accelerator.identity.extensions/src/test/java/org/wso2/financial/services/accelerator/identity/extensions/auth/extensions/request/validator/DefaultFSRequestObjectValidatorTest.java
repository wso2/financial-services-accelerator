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

package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator;

import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.FSRequestObject;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.ValidationResponse;
import org.wso2.financial.services.accelerator.identity.extensions.validator.FinancialServicesValidator;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * Test for Default Open Banking object validator.
 */
public class DefaultFSRequestObjectValidatorTest {

    private static final Map<String, Object> ALLOWED_SCOPES = Map.of("scope", List.of("accounts"));

    @Test
    public void testValidateOBConstraintsWithValidRequestObject() throws Exception {
        try (MockedStatic<FinancialServicesValidator> fsServicesValidatorMock =
                     mockStatic(FinancialServicesValidator.class)) {
            // mock
            FinancialServicesValidator openBankingValidatorMock = mock(FinancialServicesValidator.class);
            fsServicesValidatorMock.when(FinancialServicesValidator::getInstance).thenReturn(openBankingValidatorMock);
            lenient().when(openBankingValidatorMock.getFirstViolation(any())).thenReturn("");

            // act
            DefaultFSRequestObjectValidator uut = new DefaultFSRequestObjectValidator();

            FSRequestObject<?> obRequestObject = getObRequestObject(ReqObjectTestDataProvider.VALID_REQUEST);
            ValidationResponse validationResponse = uut.validateFSConstraints(obRequestObject, ALLOWED_SCOPES);

            // assert
            Assert.assertTrue(validationResponse.isValid());
        }
    }

    @Test
    public void testValidateOBConstraintsWhenNoClientId() throws Exception {

        try (MockedStatic<FinancialServicesValidator> fsServicesValidatorMock =
                     mockStatic(FinancialServicesValidator.class)) {
            // mock
            FinancialServicesValidator openBankingValidatorMock = mock(FinancialServicesValidator.class);
            fsServicesValidatorMock.when(FinancialServicesValidator::getInstance).thenReturn(openBankingValidatorMock);
            lenient().when(openBankingValidatorMock.getFirstViolation(any())).thenReturn("");

            // act
            DefaultFSRequestObjectValidator uut = new DefaultFSRequestObjectValidator();

            FSRequestObject<?> obRequestObject = getObRequestObject(ReqObjectTestDataProvider.NO_CLIENT_ID_REQUEST);
            ValidationResponse validationResponse = uut.validateFSConstraints(obRequestObject, ALLOWED_SCOPES);

            // assert
            Assert.assertFalse(validationResponse.isValid());
            Assert.assertEquals(validationResponse.getViolationMessage(),
                    "Client id or scope cannot be empty");
        }
    }

    @Test
    public void testValidateOBConstraintsWhenOBRequestObjectHasErrors() throws Exception {
        try (MockedStatic<FinancialServicesValidator> fsServicesValidatorMock =
                     mockStatic(FinancialServicesValidator.class)) {
            // mock
            FinancialServicesValidator openBankingValidatorMock = mock(FinancialServicesValidator.class);
            fsServicesValidatorMock.when(FinancialServicesValidator::getInstance).thenReturn(openBankingValidatorMock);
            lenient().when(openBankingValidatorMock.getFirstViolation(any())).thenReturn("");


            // act
            DefaultFSRequestObjectValidator uut = new DefaultFSRequestObjectValidator();

            FSRequestObject<?> obRequestObject = getObRequestObject(ReqObjectTestDataProvider.REQUEST_STRING);
            ValidationResponse validationResponse = uut.validateFSConstraints(obRequestObject, ALLOWED_SCOPES);

            // assert
            Assert.assertFalse(validationResponse.isValid());
            Assert.assertEquals(validationResponse.getViolationMessage(),
                    "No valid scopes found in the request");
        }
    }

    private FSRequestObject<?> getObRequestObject(String request) throws ParseException, RequestObjectException {

        RequestObject requestObject = new RequestObject();
        JOSEObject jwt = JOSEObject.parse(request);
        if (jwt.getHeader().getAlgorithm() == null || jwt.getHeader().getAlgorithm().equals(JWSAlgorithm.NONE)) {
            requestObject.setPlainJWT(PlainJWT.parse(request));
        } else {
            requestObject.setSignedJWT(SignedJWT.parse(request));
        }
        return new FSRequestObject<>(requestObject);
    }

}
