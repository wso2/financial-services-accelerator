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

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.financial.services.accelerator.common.validator.FinancialServicesValidator;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.FSRequestObject;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.ValidationResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Test for request object validator.
 */
public class FSRequestObjectValidationExtensionTest {

    @Test
    public void testValidate_ValidResponse() throws RequestObjectException {
        // Arrange
        ValidationResponse mockResponse = mock(ValidationResponse.class);
        when(mockResponse.isValid()).thenReturn(true);

        FSRequestObjectValidationExtension extension = new FSRequestObjectValidationExtension();

        // Act & Assert
        extension.validate(mockResponse); // Should not throw an exception
    }

    @Test(expectedExceptions = RequestObjectException.class)
    public void testValidate_InvalidResponse() throws RequestObjectException {
        // Arrange
        ValidationResponse mockResponse = mock(ValidationResponse.class);
        when(mockResponse.isValid()).thenReturn(false);
        when(mockResponse.getViolationMessage()).thenReturn("Invalid request object");

        FSRequestObjectValidationExtension extension = new FSRequestObjectValidationExtension();

        // Act
        extension.validate(mockResponse);

        // Assert
        // Exception is expected, so no further assertions are needed
    }

    @Test
    public void testDefaultValidateRequestObject_ValidRequest() {
        // Arrange
        FSRequestObject mockRequestObject = mock(FSRequestObject.class);
        FSRequestObjectValidationExtension extension = new FSRequestObjectValidationExtension();

        // Inject the mock FS request object validator
        FSRequestObjectValidator mockFsRequestObjectValidator = mock(FSRequestObjectValidator.class);
        FSRequestObjectValidationExtension.fsDefaultRequestObjectValidator = mockFsRequestObjectValidator;

        // Inject the mock FS validator
        FinancialServicesValidator mockFsValidator = mock(FinancialServicesValidator.class);
        when(mockFsValidator.getFirstViolation(Mockito.any())).thenReturn(null);
        FSRequestObjectValidationExtension.fsValidator = mockFsValidator;

        // Act
        ValidationResponse response = extension.defaultValidateRequestObject(mockRequestObject);

        // Assert
        assertTrue(response.isValid());
    }

    @Test
    public void testDefaultValidateRequestObject_InvalidRequest() {
        // Arrange
        FSRequestObject mockRequestObject = mock(FSRequestObject.class);
        FSRequestObjectValidationExtension extension = new FSRequestObjectValidationExtension();

        // Inject the mock FS request object validator
        FSRequestObjectValidator mockFsRequestObjectValidator = mock(FSRequestObjectValidator.class);
        FSRequestObjectValidationExtension.fsDefaultRequestObjectValidator = mockFsRequestObjectValidator;

        // Inject the mock FS validator
        FinancialServicesValidator mockFsValidator = mock(FinancialServicesValidator.class);
        when(mockFsValidator.getFirstViolation(Mockito.any())).thenReturn("Invalid");
        FSRequestObjectValidationExtension.fsValidator = mockFsValidator;

        // Act
        ValidationResponse response = extension.defaultValidateRequestObject(mockRequestObject);

        // Assert
        assertFalse(response.isValid());
        assertNotNull(response.getViolationMessage());
    }
}
