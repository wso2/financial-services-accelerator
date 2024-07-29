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

import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateIssuer;
import org.apache.commons.beanutils.BeanUtils;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.validation.ConstraintValidatorContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({JWTUtils.class, BeanUtils.class})
public class IssuerValidatorTest extends PowerMockTestCase {

    private IssuerValidator validator;

    @Mock
    private ValidateIssuer validateIssuer;

    @BeforeMethod
    public void setUp() {
        validator = new IssuerValidator();

        when(validateIssuer.issuerProperty()).thenReturn("issuer");
        when(validateIssuer.ssa()).thenReturn("ssa");

        validator.initialize(validateIssuer);
    }

    @Test
    public void testIsValid_ReturnsTrue_WhenIssuerOrSoftwareStatementIsNull() throws Exception {
        Object registrationRequest = mock(Object.class);
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        PowerMockito.mockStatic(BeanUtils.class);
        PowerMockito.when(BeanUtils.getProperty(registrationRequest, "issuer")).thenReturn(null);
        PowerMockito.when(BeanUtils.getProperty(registrationRequest, "ssa")).thenReturn(null);

        boolean result = validator.isValid(registrationRequest, context);
        Assert.assertTrue(result);
    }

    @Test
    public void testIsValid_ReturnsFalse_OnException() throws Exception {
        Object registrationRequest = mock(Object.class);
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        PowerMockito.mockStatic(BeanUtils.class);
        PowerMockito.when(BeanUtils.getProperty(registrationRequest, "issuer")).thenThrow(new NoSuchMethodException());
        PowerMockito.when(BeanUtils.getProperty(registrationRequest, "ssa")).thenReturn("dummy-ssa");

        boolean result = validator.isValid(registrationRequest, context);
        Assert.assertFalse(result);
    }
}
