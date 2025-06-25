/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.test.preconfiguration.steps

import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.bfsi.test.framework.automation.BrowserAutomation
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.request_builder.ApiConsoleRequestBuilder

import java.nio.charset.Charset

/**
 * Test class for creating a user role in APIM Console.
 */
class AddUserRole extends FSAPIMConnectorTest {

    String apimCarbonConsoleUrl
    String userId
    def basicHeader
    ApiConsoleRequestBuilder apiConsoleRequestBuilder

    @BeforeClass
    void init() {
        apimCarbonConsoleUrl = configuration.getApimServerUrl() + "/carbon"
    }

    @Test
    void "Add User Role in API Manager console"() {

        //Log into APIM Carbon Console
        def automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY, false)
                .addStep(new ApiConsoleRequestBuilder(apimCarbonConsoleUrl))
                .execute()
    }
}
