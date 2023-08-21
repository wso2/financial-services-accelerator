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

package com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator;

import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.validator.OpenBankingValidator;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models.OBRequestObject;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models.ValidationResponse;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;

import java.text.ParseException;
import java.util.Collections;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test for Default Open Banking object validator.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OpenBankingValidator.class, IdentityExtensionsDataHolder.class})
public class DefaultOBRequestObjectValidatorTest extends PowerMockTestCase {

    private static final String CLIENT_ID_1 = "2X0n9WSNmPiq3XTB8dtC0Shs5r8a";
    private static final String CLIENT_ID_2 = "owjqxsPTQ7zJIFeZRib5b03ufxsa";
    private static ConsentCoreServiceImpl consentCoreServiceMock;

    @BeforeClass
    public void initTest() {

        consentCoreServiceMock = PowerMockito.mock(ConsentCoreServiceImpl.class);
    }

    @BeforeMethod
    private void mockStaticClasses() throws ConsentManagementException {

        PowerMockito.mockStatic(IdentityExtensionsDataHolder.class);
        IdentityExtensionsDataHolder mock = PowerMockito.mock(IdentityExtensionsDataHolder.class);
        PowerMockito.when(IdentityExtensionsDataHolder.getInstance()).thenReturn(mock);
        PowerMockito.when(IdentityExtensionsDataHolder.getInstance().getConsentCoreService())
                .thenReturn(consentCoreServiceMock);
    }

    @Test
    public void testValidateOBConstraintsWithValidRequestObject() throws Exception {
        // mock
        DetailedConsentResource consentResourceMock = mock(DetailedConsentResource.class);
        doReturn(CLIENT_ID_1).when(consentResourceMock).getClientID();

        doReturn(consentResourceMock).when(consentCoreServiceMock).getDetailedConsent(anyString());

        OpenBankingValidator openBankingValidatorMock = mock(OpenBankingValidator.class);
        doReturn("").when(openBankingValidatorMock).getFirstViolation(Mockito.anyObject());

        PowerMockito.mockStatic(OpenBankingValidator.class);
        PowerMockito.when(OpenBankingValidator.getInstance()).thenReturn(openBankingValidatorMock);

        // act
        DefaultOBRequestObjectValidator uut = new DefaultOBRequestObjectValidator();

        OBRequestObject<?> obRequestObject = getObRequestObject(ReqObjectTestDataProvider.VALID_REQUEST);
        ValidationResponse validationResponse = uut.validateOBConstraints(obRequestObject, Collections.emptyMap());

        // assert
        Assert.assertTrue(validationResponse.isValid());
    }

    @Test
    public void testValidateOBConstraintsWhenNoClientId() throws Exception {
        // mock
        DetailedConsentResource consentResourceMock = mock(DetailedConsentResource.class);
        doReturn(null).when(consentResourceMock).getClientID();

        ConsentCoreServiceImpl consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentResourceMock).when(consentCoreServiceMock).getDetailedConsent(anyString());

        OpenBankingValidator openBankingValidatorMock = mock(OpenBankingValidator.class);
        doReturn("").when(openBankingValidatorMock).getFirstViolation(Mockito.anyObject());

        PowerMockito.mockStatic(OpenBankingValidator.class);
        PowerMockito.when(OpenBankingValidator.getInstance()).thenReturn(openBankingValidatorMock);

        // act
        DefaultOBRequestObjectValidator uut = new DefaultOBRequestObjectValidator();

        OBRequestObject<?> obRequestObject = getObRequestObject(ReqObjectTestDataProvider.NO_CLIENT_ID_REQUEST);
        ValidationResponse validationResponse = uut.validateOBConstraints(obRequestObject, Collections.emptyMap());

        // assert
        Assert.assertFalse(validationResponse.isValid());
        Assert.assertEquals(validationResponse.getViolationMessage(),
                "Client id or scope cannot be empty");
    }

    @Test
    public void testValidateOBConstraintsWhenOBRequestObjectHasErrors() throws Exception {
        // mock
        OpenBankingValidator openBankingValidatorMock = mock(OpenBankingValidator.class);
        doReturn("dummy-error").when(openBankingValidatorMock).getFirstViolation(Mockito.anyObject());

        PowerMockito.mockStatic(OpenBankingValidator.class);
        PowerMockito.when(OpenBankingValidator.getInstance()).thenReturn(openBankingValidatorMock);

        // act
        DefaultOBRequestObjectValidator uut = new DefaultOBRequestObjectValidator();

        OBRequestObject<?> obRequestObject = getObRequestObject(ReqObjectTestDataProvider.REQUEST_STRING);
        ValidationResponse validationResponse = uut.validateOBConstraints(obRequestObject, Collections.emptyMap());

        // assert
        Assert.assertFalse(validationResponse.isValid());
        Assert.assertEquals(validationResponse.getViolationMessage(), "dummy-error");
    }

    private OBRequestObject<?> getObRequestObject(String request) throws ParseException, RequestObjectException {

        RequestObject requestObject = new RequestObject();
        JOSEObject jwt = JOSEObject.parse(request);
        if (jwt.getHeader().getAlgorithm() == null || jwt.getHeader().getAlgorithm().equals(JWSAlgorithm.NONE)) {
            requestObject.setPlainJWT(PlainJWT.parse(request));
        } else {
            requestObject.setSignedJWT(SignedJWT.parse(request));
        }
        return new OBRequestObject<>(requestObject);
    }

}
