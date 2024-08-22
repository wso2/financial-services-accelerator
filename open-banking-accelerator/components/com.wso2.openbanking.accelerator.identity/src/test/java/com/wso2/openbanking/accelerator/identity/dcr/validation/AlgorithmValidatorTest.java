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

import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateAlgorithm;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.beanutils.BeanUtils;
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
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintValidatorContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({BeanUtils.class, IdentityExtensionsDataHolder.class})
public class AlgorithmValidatorTest extends PowerMockTestCase {

    private AlgorithmValidator validator;

    @Mock
    private ValidateAlgorithm validateAlgorithm;

    @BeforeMethod
    public void setUp() {
        validator = new AlgorithmValidator();

        when(validateAlgorithm.idTokenAlg()).thenReturn("idTokenAlg");
        when(validateAlgorithm.reqObjAlg()).thenReturn("reqObjAlg");
        when(validateAlgorithm.tokenAuthAlg()).thenReturn("tokenAuthAlg");

        validator.initialize(validateAlgorithm);
    }

    @Test
    public void testIsValid_ReturnsTrue_WhenAlgorithmsAreAllowed() throws Exception {
        Object requestObject = mock(Object.class);
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        PowerMockito.mockStatic(BeanUtils.class);
        PowerMockito.when(BeanUtils.getProperty(requestObject, "idTokenAlg")).thenReturn("RS256");
        PowerMockito.when(BeanUtils.getProperty(requestObject, "reqObjAlg")).thenReturn("RS256");
        PowerMockito.when(BeanUtils.getProperty(requestObject, "tokenAuthAlg")).thenReturn("RS256");

        List<String> allowedAlgorithms = Arrays.asList("RS256", "HS256");

        PowerMockito.mockStatic(IdentityExtensionsDataHolder.class);
        IdentityExtensionsDataHolder dataHolder = PowerMockito.mock(IdentityExtensionsDataHolder.class);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(OpenBankingConstants.SIGNATURE_ALGORITHMS, allowedAlgorithms);
        when(dataHolder.getConfigurationMap()).thenReturn(configMap);
        PowerMockito.when(IdentityExtensionsDataHolder.getInstance()).thenReturn(dataHolder);

        boolean result = validator.isValid(requestObject, context);
        Assert.assertTrue(result);
    }

    @Test
    public void testIsValid_ReturnsFalse_WhenAlgorithmsAreNotAllowed() throws Exception {
        Object requestObject = mock(Object.class);
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        PowerMockito.mockStatic(BeanUtils.class);
        PowerMockito.when(BeanUtils.getProperty(requestObject, "idTokenAlg")).thenReturn("RS512");
        PowerMockito.when(BeanUtils.getProperty(requestObject, "reqObjAlg")).thenReturn("RS512");
        PowerMockito.when(BeanUtils.getProperty(requestObject, "tokenAuthAlg")).thenReturn("RS512");

        List<String> allowedAlgorithms = Arrays.asList("RS256", "HS256");

        PowerMockito.mockStatic(IdentityExtensionsDataHolder.class);
        IdentityExtensionsDataHolder dataHolder = PowerMockito.mock(IdentityExtensionsDataHolder.class);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(OpenBankingConstants.SIGNATURE_ALGORITHMS, allowedAlgorithms);
        when(dataHolder.getConfigurationMap()).thenReturn(configMap);
        PowerMockito.when(IdentityExtensionsDataHolder.getInstance()).thenReturn(dataHolder);

        boolean result = validator.isValid(requestObject, context);
        Assert.assertFalse(result);
    }
}
