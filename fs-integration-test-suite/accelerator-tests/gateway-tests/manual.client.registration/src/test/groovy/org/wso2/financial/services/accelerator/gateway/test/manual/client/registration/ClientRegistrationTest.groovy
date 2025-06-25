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

package org.wso2.financial.services.accelerator.gateway.test.manual.client.registration

import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.bfsi.test.framework.automation.BrowserAutomation
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ManualClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Test class for manual client registration in WSO2 API Manager.
 */
class ClientRegistrationTest extends FSAPIMConnectorTest{

    String apimDevportalUrl
    String userId
    File xmlFile = ConnectorTestConstants.CONFIG_FILE

    @BeforeClass
    void init() {
        apimDevportalUrl = configuration.getApimServerUrl() + "/devportal"
        xmlFile = ConnectorTestConstants.CONFIG_FILE
    }

    @Test
    void "Manual client Registration regulatory application"() {

        def automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY, false)
                .addStep(new ManualClientRegistrationRequestBuilder(apimDevportalUrl, "TestApplication1",
                        true))
                .execute()

        //Write Client Id and Client Secret of TTP1 to config file.
        TestUtil.writeXMLContent(xmlFile.toString(), "Application", "ClientID", clientId,
                configuration.getTppNumber())
    }

    @Test
    void "Manual client Registration non-regulatory application"() {

        def automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY, false)
                .addStep(new ManualClientRegistrationRequestBuilder(apimDevportalUrl, "Non_Regulatory_Application1",
                        false))
                .execute()

        //Write Client Id and Client Secret of TTP1 to config file.
        TestUtil.writeXMLContent(xmlFile.toString(), "NonRegulatoryApplication", "ClientID", clientId,
                configuration.getTppNumber())
    }
}
