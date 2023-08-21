package com.wso2.openbanking.accelerator.common.test.validator;
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

import com.nimbusds.jwt.JWTClaimsSet;
import com.wso2.openbanking.accelerator.common.test.validator.resources.SampleDifferentClass;
import com.wso2.openbanking.accelerator.common.test.validator.resources.SampleRequestObject;
import com.wso2.openbanking.accelerator.common.test.validator.resources.ValidatorTestDataProvider;
import com.wso2.openbanking.accelerator.common.validator.OpenBankingValidator;
import org.hibernate.validator.HibernateValidator;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

/**
 * Model validators test.
 */
public class ModelValidatorsTest {

   private SampleRequestObject sampleRequestObject = new SampleRequestObject();
    private static Validator validator = Validation.byProvider(HibernateValidator.class).configure().addProperty(
            "hibernate.validator.fail_fast", "true").buildValidatorFactory().getValidator();

    private static Validator validator2 = Validation.byProvider(HibernateValidator.class).configure().addProperty(
            "hibernate.validator.fail_fast", "true").buildValidatorFactory().getValidator();

    private static Validator validator3 = Validation.byProvider(HibernateValidator.class).configure().addProperty(
            "hibernate.validator.fail_fast", "true").buildValidatorFactory().getValidator();


    @Test(dataProvider = "dp-checkValidScopeFormat", dataProviderClass = ValidatorTestDataProvider.class)
    public void checkValidScopeFormat(String claimsString) throws ParseException {

        sampleRequestObject.setClaimSet(JWTClaimsSet.parse(claimsString));
        Set<ConstraintViolation<SampleRequestObject>> violations = validator.validate(sampleRequestObject);
        String violation = violations.stream().findFirst().map(ConstraintViolation::getMessage).orElse(null);
        assertNull("Valid scope formats should pass", violation);
    }

    @Test(dataProvider = "dp-checkValidSingleScopes", dataProviderClass = ValidatorTestDataProvider.class)
    public void checkValidSingleScopes(String claimsString) throws ParseException {

        sampleRequestObject.setClaimSet(JWTClaimsSet.parse(claimsString));
        Set<ConstraintViolation<SampleRequestObject>> violations = validator.validate(sampleRequestObject);
        String violation = violations.stream().findFirst().map(ConstraintViolation::getMessage).orElse(null);
        assertNull("Valid single scope should pass", violation);
    }

    @Test
    public void checkMandatoryParamsValidationFailing() {

        SampleDifferentClass sampleRequestObject = new SampleDifferentClass();
        sampleRequestObject.setName("name");
        sampleRequestObject.setMale(true);

        Set<ConstraintViolation<SampleDifferentClass>> violations = validator.validate(sampleRequestObject);
        String violation = violations.stream().findFirst().map(ConstraintViolation::getMessage).orElse(null);
        assertEquals("age failed", violation);

        //
        sampleRequestObject = new SampleDifferentClass();
        sampleRequestObject.setName("name");
        sampleRequestObject.setAge(70);

        violations = validator2.validate(sampleRequestObject);
        violation = violations.stream().findFirst().map(ConstraintViolation::getMessage).orElse(null);
        assertEquals("male failed", violation);

        //
        sampleRequestObject = new SampleDifferentClass();
        sampleRequestObject.setMale(true);
        sampleRequestObject.setAge(70);

        violations = validator3.validate(sampleRequestObject);
        violation = violations.stream().findFirst().map(ConstraintViolation::getMessage).orElse(null);
        assertEquals("name failed", violation);

    }

    @Test
    public void checkOpenBankingValidator() {

        SampleDifferentClass sampleRequestObject = new SampleDifferentClass();
        sampleRequestObject.setName("name");
        sampleRequestObject.setMale(true);

        String violation = OpenBankingValidator.getInstance().getFirstViolation(sampleRequestObject);
        assertEquals("age failed", violation);

        //
        sampleRequestObject = new SampleDifferentClass();
        sampleRequestObject.setName("name");
        sampleRequestObject.setAge(70);

        violation = OpenBankingValidator.getInstance().getFirstViolation(sampleRequestObject);
        assertEquals("male failed", violation);

        //
        sampleRequestObject = new SampleDifferentClass();
        sampleRequestObject.setMale(true);
        sampleRequestObject.setAge(70);

        violation = OpenBankingValidator.getInstance().getFirstViolation(sampleRequestObject);
        assertEquals("name failed", violation);

    }

}
