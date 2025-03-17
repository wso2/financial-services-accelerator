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

import org.wso2.openbanking.test.framework.automation.BrowserAutomationStep
import org.wso2.openbanking.test.framework.automation.OBBrowserAutomation
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService

/**
 * Class contains the automation steps redirecting to error page.
 */
class WaitForErrorRedirectAutomationStep implements BrowserAutomationStep {

    private String redirectURL
    private ConfigurationService configuration

    WaitForErrorRedirectAutomationStep(String redirect = null) {
        redirectURL = redirect
        configuration = new ConfigurationService()
    }

    private String getRedirectURL() {
        if (redirectURL == null) {
            redirectURL = configuration.getAppDCRRedirectUri()
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
    void execute(RemoteWebDriver webDriver, OBBrowserAutomation.AutomationContext context) {

        WebDriverWait wait = new WebDriverWait(webDriver, 10)
        wait.until(ExpectedConditions.urlContains("oauth2_error.do"))
    }
}
