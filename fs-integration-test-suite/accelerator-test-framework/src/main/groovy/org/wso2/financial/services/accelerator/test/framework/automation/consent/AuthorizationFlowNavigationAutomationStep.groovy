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

package org.wso2.financial.services.accelerator.test.framework.automation.consent

import org.wso2.bfsi.test.framework.automation.BrowserAutomationStep
import org.wso2.bfsi.test.framework.automation.BrowserAutomation
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openqa.selenium.remote.RemoteWebDriver
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import java.util.concurrent.TimeUnit

/**
 * Test class contains the step to open up the firefox browser and navigate to the authorize URL.
 */
class AuthorizationFlowNavigationAutomationStep implements BrowserAutomationStep {

    private String authorizeUrl
    private ConfigurationService acceleratorConfiguration
    private static final Log log = LogFactory.getLog(AuthorizationFlowNavigationAutomationStep.class)

    /**
     * Initialize Basic Auth Flow.
     *
     * @param authorizeUrl authorise URL.
     */
    public AuthorizationFlowNavigationAutomationStep(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl
        this.acceleratorConfiguration = new ConfigurationService()
    }

    @Override
    void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {

        webDriver.navigate().to(authorizeUrl)
        webDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS)
    }
}
