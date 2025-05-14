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

package org.wso2.bfsi.test.framework.automation

import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.wso2.bfsi.test.framework.configuration.CommonConfigurationService

/**
 * Class for automate waiting for redirect
 */
class WaitForRedirectAutomationStep implements BrowserAutomationStep {

    private String redirectURL
    private CommonConfigurationService obConfiguration

    WaitForRedirectAutomationStep(String redirect = null) {
        redirectURL = redirect
        obConfiguration = new CommonConfigurationService()
    }

    private String getRedirectURL() {
        if (redirectURL == null) {
            redirectURL = obConfiguration.getAppInfoRedirectURL()
        }
        return redirectURL
    }

    /**
     * Execute automation using driver.
     *
     * @param webDriver driver object.
     * @param context automation context.
     */
    @Override
    void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {
        WebDriverWait wait = new WebDriverWait(webDriver, 10)
        wait.until(ExpectedConditions.urlContains(getRedirectURL()))
    }

}

