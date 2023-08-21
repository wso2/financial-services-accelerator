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

package com.wso2.openbanking.accelerator.gateway.executor.service;

import com.wso2.openbanking.accelerator.gateway.executor.revocation.CRLValidator;
import com.wso2.openbanking.accelerator.gateway.executor.revocation.OCSPValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test for revocation validation service.
 */
public class RevocationValidatorFactoryTest {

    private RevocationValidatorFactory revocationValidatorFactory;

    @BeforeClass
    public void init() {
        this.revocationValidatorFactory = new RevocationValidatorFactory();
    }

    @Test(description = "when valid revocation validator, then return valid validator object")
    public void testGetValidatorWithValidStr() {
        Assert.assertTrue(revocationValidatorFactory
                .getValidator("OCSP", 1) instanceof OCSPValidator);
        Assert.assertTrue(revocationValidatorFactory
                .getValidator("CRL", 1) instanceof CRLValidator);
    }

    @Test(description = "when invalid revocation validator, then return null")
    public void testGetValidatorWithInvalidStr() {
        Assert.assertNull(revocationValidatorFactory
                .getValidator("INVALID", 1));
    }

    @Test(description = "when valid revocation validator, then return valid validator object")
    public void testUpdatedGetValidatorWithValidStr() {
        Assert.assertTrue(revocationValidatorFactory
                .getValidator("OCSP", 1, 5000, 5000, 5000) instanceof OCSPValidator);
        Assert.assertTrue(revocationValidatorFactory
                .getValidator("CRL", 1, 5000, 5000, 5000) instanceof CRLValidator);
    }

    @Test(description = "when invalid revocation validator, then return null")
    public void testUpdatedGetValidatorWithInvalidStr() {
        Assert.assertNull(revocationValidatorFactory
                .getValidator("INVALID", 1, 5000, 5000, 5000));
    }

}
