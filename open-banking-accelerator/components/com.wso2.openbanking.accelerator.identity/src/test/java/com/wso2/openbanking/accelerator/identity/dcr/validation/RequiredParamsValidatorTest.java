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
package com.wso2.openbanking.accelerator.identity.dcr.validation;

import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateRequiredParams;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.validation.ConstraintValidatorContext;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({IdentityExtensionsDataHolder.class})
public class RequiredParamsValidatorTest extends PowerMockTestCase {

    private RequiredParamsValidator validator;

    @Mock
    private ValidateRequiredParams validateRequiredParams;

    private IdentityExtensionsDataHolder identityExtensionsDataHolderMock;

    @BeforeMethod
    public void setUp() {
        validator = new RequiredParamsValidator();
        validator.initialize(validateRequiredParams);

        PowerMockito.mockStatic(IdentityExtensionsDataHolder.class);
        identityExtensionsDataHolderMock = PowerMockito.mock(IdentityExtensionsDataHolder.class);
        PowerMockito.when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolderMock);

        // Mock the DCR registration config map with some test data
        Map<String, Map<String, Object>> configMap = new HashMap<>();
        Map<String, Object> paramConfig = new HashMap<>();
        paramConfig.put(DCRCommonConstants.DCR_REGISTRATION_PARAM_REQUIRED, "true");
        configMap.put("tokenEndPointAuthentication", paramConfig);

        Map<String, Object> scopeAllowedValuesConfig = new HashMap<>();
        scopeAllowedValuesConfig.put(DCRCommonConstants.DCR_REGISTRATION_PARAM_ALLOWED_VALUES, Arrays.asList(
                "scope1", "scope2"));
        configMap.put("scope", scopeAllowedValuesConfig);


        Map<String, Object> appTypeAllowedValuesConfig = new HashMap<>();
        appTypeAllowedValuesConfig.put(DCRCommonConstants.DCR_REGISTRATION_PARAM_ALLOWED_VALUES, Arrays.asList(
                "web", "mobile"));
        configMap.put("applicationType", appTypeAllowedValuesConfig);

        PowerMockito.when(identityExtensionsDataHolderMock.getDcrRegistrationConfigMap()).thenReturn(configMap);
    }

    @Test
    public void testIsValid_ReturnsTrue_WhenAllRequestObjectIsEmpty() {
        PowerMockito.when(identityExtensionsDataHolderMock.getDcrRegistrationConfigMap()).thenReturn(new HashMap<>());
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        doReturn(getConstraintViolationBuilder()).when(context).buildConstraintViolationWithTemplate(anyString());
        RegistrationRequest request = new RegistrationRequest();
        boolean result = validator.isValid(request, context);
        Assert.assertTrue(result);
    }

    @Test
    public void testIsValid_ReturnsTrue_WhenRequiredParametersArePresent() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        doReturn(getConstraintViolationBuilder()).when(context).buildConstraintViolationWithTemplate(anyString());
        RegistrationRequest request = getSampleRegistrationRequestWithRequiredParams();
        boolean result = validator.isValid(request, context);
        Assert.assertTrue(result);
    }

    @Test
    public void testIsValid_ReturnsFalse_WhenRequiredParameterIsBlank() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        doReturn(getConstraintViolationBuilder()).when(context).buildConstraintViolationWithTemplate(anyString());
        RegistrationRequest request = getSampleRegistrationRequestWithBlankRequiredParams();
        boolean result = validator.isValid(request, context);
        Assert.assertFalse(result);
    }

    @Test
    public void testIsValid_ReturnsFalse_WhenScopeNotAllowed() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        doReturn(getConstraintViolationBuilder()).when(context).buildConstraintViolationWithTemplate(anyString());
        RegistrationRequest request = getSampleRegistrationRequestWithScope();
        boolean result = validator.isValid(request, context);
        Assert.assertFalse(result);
    }

    private ConstraintValidatorContext.ConstraintViolationBuilder getConstraintViolationBuilder() {

        PathImpl propertyPath = PathImpl.createPathFromString("example.path");
        ConstraintValidatorContextImpl context = new ConstraintValidatorContextImpl(
                null,
                null,
                propertyPath,
                null,
                null
        );
        return context.buildConstraintViolationWithTemplate("message");
    }

    private RegistrationRequest getSampleRegistrationRequestWithRequiredParams() {

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setApplicationType("web");
        registrationRequest.setTokenEndPointAuthentication("auth_method");
        registrationRequest.setScope("scope1 scope2");
        return registrationRequest;
    }

    private RegistrationRequest getSampleRegistrationRequestWithBlankRequiredParams() {

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setApplicationType("web");
        registrationRequest.setTokenEndPointAuthentication("");
        registrationRequest.setScope("scope1 scope2");
        return registrationRequest;
    }

    private RegistrationRequest getSampleRegistrationRequestWithScope() {

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setTokenEndPointAuthentication("auth_method");
        registrationRequest.setScope("scope1 scope3");
        return registrationRequest;
    }

}
