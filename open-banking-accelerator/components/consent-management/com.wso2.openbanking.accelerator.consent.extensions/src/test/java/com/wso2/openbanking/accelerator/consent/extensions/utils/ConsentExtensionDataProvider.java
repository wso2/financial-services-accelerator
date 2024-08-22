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
package com.wso2.openbanking.accelerator.consent.extensions.utils;

import org.testng.annotations.DataProvider;

/**
 *  Data Provider for Consent Executor Tests.
 */
public class ConsentExtensionDataProvider {

    @DataProvider(name = "VRPInvalidSubmissionPayloadsDataProvider")
    Object[][] getVRPInvalidSubmissionPayloadsDataProvider() {

        return new Object[][]{
                {ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_INSTRUCTION_IDENTIFICATION},
                {ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_END_TO_IDENTIFICATION},
                {ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_INSTRUCTED_AMOUNT},
                {ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_INSTRUCTION_CREDITOR_ACC},
                {ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_INSTRUCTION_REMITTANCE_INFO},
        };
    }

    @DataProvider(name = "VRPInvalidInitiationSubmissionPayloadsDataProvider")
    Object[][] getVRPInvalidInitiationSubmissionPayloadsDataProvider() {

        return new Object[][]{
                {ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_CREDITOR_ACC},
                {ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_REMITTANCE_INFO},
                {ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_DEBTOR_ACC},
        };
    }

}
