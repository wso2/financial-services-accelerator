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

import org.wso2.openbanking.test.framework.automation.AutomationMethod
import org.wso2.openbanking.test.framework.automation.BrowserAutomationStep
import org.wso2.openbanking.test.framework.automation.OBBrowserAutomation
import org.wso2.openbanking.test.framework.constant.OBConstants
import org.wso2.openbanking.test.framework.constant.OBPageObjects
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.PageObjects

class BasicAuthAutomationStep implements BrowserAutomationStep {

    private String authorizeUrl
    private ConfigurationService acceleratorConfiguration
//    private static final Log log = LogFactory.getLog(BasicAuthAutomationStep.class)

    BasicAuthAutomationStep(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl
        this.acceleratorConfiguration = new ConfigurationService()
    }

    @Override
    void execute(RemoteWebDriver webDriver, OBBrowserAutomation.AutomationContext context) {
        WebDriverWait wait = new WebDriverWait(webDriver, 30)
        AutomationMethod driver = new AutomationMethod(webDriver)
        webDriver.navigate().to(authorizeUrl)

        driver.executeTextField(OBPageObjects.USERNAME_FIELD_ID, acceleratorConfiguration.getUserPSUName())
        driver.executeTextField(OBPageObjects.PASSWORD_FIELD_ID, acceleratorConfiguration.getUserPSUPWD())
        driver.submitButtonXpath(OBPageObjects.AUTH_SIGNIN_XPATH)

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(OBPageObjects.PASSWORD_FIELD_ID)))

        //Second Factor Authentication Step
        try {
            if (webDriver.findElement(By.xpath(PageObjects.LBL_SMSOTP_AUTHENTICATOR)).isDisplayed()) {
                driver.executeSMSOTP(OBPageObjects.LBL_SMSOTP_AUTHENTICATOR, OBPageObjects.TXT_OTP_CODE_ID,
                        OBConstants.OTP_CODE)
                driver.clickButtonXpath(OBPageObjects.BTN_AUTHENTICATE)
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(OBPageObjects.BTN_AUTHENTICATE)))
            }
        } catch (NoSuchElementException e) {
//            log.info("Second Factor Authentication Step is not configured")
        }
    }
}
