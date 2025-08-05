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

package org.wso2.financial.services.accelerator.test.framework.request_builder

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.wso2.bfsi.test.framework.automation.BrowserAutomation
import org.wso2.bfsi.test.framework.automation.BrowserAutomationStep
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.PageObjects

import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * Class to contain steps to create a user role in API Manager Console.
 */
class ApiConsoleRequestBuilder implements BrowserAutomationStep{

    WebDriverWait wait
    private String url
    static ConfigurationService configurationService = new ConfigurationService()

    /**
     * Initialize Api Publishing Flow.
     *
     * @param url URL.
     */
    ApiConsoleRequestBuilder(String url) {

        this.url = url
    }

    /**
     * Log into Api Console.
     * @param webDriver object of RemoteWebDriver
     */
    void apimConsoleLogin(RemoteWebDriver webDriver) {

        webDriver.get(url)

        WebElement username = webDriver.findElement(By.xpath(PageObjects.CONSOLE_USERNAME))
        username.clear()
        username.sendKeys(configurationService.getUserIsAsKeyManagerAdminName())

        WebElement password = webDriver.findElement(By.xpath(PageObjects.CONSOLE_PASSWORD))
        password.clear()
        password.sendKeys(configurationService.getUserIsAsKeyManagerAdminPWD())

        wait = new WebDriverWait(webDriver, 600)
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_CONSOLE_SIGN_IN)))
        webDriver.findElement(By.xpath(PageObjects.BTN_CONSOLE_SIGN_IN)).click()
        driverWait(webDriver, 20)
    }

    /**
     * Add User Role in API Manager Console.
     * @param webDriver object of RemoteWebDriver
     * @param webDriver
     */
    void addUserRole(RemoteWebDriver webDriver){

        //Click Add Users and Roles button
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_ADD_USERS_ROLES)))
        webDriver.findElement(By.xpath(PageObjects.BTN_ADD_USERS_ROLES)).click()

        //Click Add New Role button
        webDriver.findElement(By.xpath(PageObjects.BTN_ADD_NEW_ROLE)).click()

        //Select Domain
        WebElement dropdownDomain = webDriver.findElement(By.xpath(PageObjects.DD_DOMAIN))
        Select domain = new Select(dropdownDomain)
        domain.selectByVisibleText("INTERNAL")

        //Enter Role Name
        WebElement txtRoleName = webDriver.findElement(By.xpath(PageObjects.TXT_ROLE_NAME))
        txtRoleName.clear()
        txtRoleName.sendKeys("consumer")

        //Click Finish button
        webDriver.findElement(By.xpath(PageObjects.BTN_FINISH)).click()
        driverWait(webDriver, 20)
    }

    /**
     * Implicit Wait
     *
     * @param webDriver
     * @param time
     */
    void driverWait(RemoteWebDriver webDriver, int time) {
        webDriver.manage().timeouts().implicitlyWait(time, TimeUnit.SECONDS)
    }

    /**
     * Execute automation steps using driver
     * @param webDriver driver object.
     * @param context   automation context.
     */
    @Override
    void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {

        apimConsoleLogin(webDriver)
        addUserRole(webDriver)
    }
}
