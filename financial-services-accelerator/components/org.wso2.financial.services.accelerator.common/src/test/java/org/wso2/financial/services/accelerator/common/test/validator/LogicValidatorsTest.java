/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.common.test.validator;

import com.nimbusds.jwt.JWTClaimsSet;
import org.hibernate.validator.HibernateValidator;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.test.validator.resources.SampleChildRequestObject;
import org.wso2.financial.services.accelerator.common.test.validator.resources.SampleRequestObject;
import org.wso2.financial.services.accelerator.common.test.validator.resources.ValidatorTestDataProvider;
import org.wso2.financial.services.accelerator.common.validator.FinancialServicesValidator;

import java.text.ParseException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;


import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

/**
 * Logic validators test.
 */
public class LogicValidatorsTest {

    private SampleRequestObject sampleRequestObject = new SampleRequestObject();
    private static Validator uut = Validation.byProvider(HibernateValidator.class).configure().addProperty(
            "hibernate.uut.fail_fast", "true").buildValidatorFactory().getValidator();

    @Test(dataProvider = "dp-checkValidScopeFormat", dataProviderClass = ValidatorTestDataProvider.class)
    public void checkValidScopeFormat(String claimsString) throws ParseException {

        //Assign
        sampleRequestObject.setClaimSet(JWTClaimsSet.parse(claimsString));

        //Act
        Set<ConstraintViolation<SampleRequestObject>> violations = uut.validate(sampleRequestObject);

        //Assert
        String violation = violations.stream().findFirst().map(ConstraintViolation::getMessage).orElse(null);
        assertNull("Valid scope formats should pass", violation);

    }

    @Test(dataProvider = "dp-checkValidationsInherited", dataProviderClass = ValidatorTestDataProvider.class)
    public void checkValidationsInherited(String claimsString) throws ParseException {

        SampleChildRequestObject sampleChildRequestObject = new SampleChildRequestObject();

        sampleChildRequestObject.setClaimSet(JWTClaimsSet.parse(claimsString));
        Set<ConstraintViolation<SampleRequestObject>> violations = uut.validate(sampleChildRequestObject);
        String violation = violations.stream().findFirst().map(ConstraintViolation::getMessage).orElse(null);
        assertNotNull("Inherited validations should work", violation);
    }

    @Test(dataProvider = "dp-checkValidScopeFormat", dataProviderClass = ValidatorTestDataProvider.class)
    public void checkOpenBankingValidator(String claimsString) throws ParseException {

        //Assign
        sampleRequestObject.setClaimSet(JWTClaimsSet.parse(claimsString));

        //Act
        String violation = FinancialServicesValidator.getInstance().getFirstViolation(sampleRequestObject);

        //Assert
        assertNull("Valid scope formats should pass", violation);

    }
}
