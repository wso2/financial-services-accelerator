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
import org.testng.Assert
import org.wso2.bfsi.test.framework.automation.BrowserAutomation
import org.wso2.bfsi.test.framework.automation.BrowserAutomationStep
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.PageObjects

import java.util.concurrent.TimeUnit

/**
 * Class to contain steps to create a user role in API Publisher Portal.
 */
class ApiPublisherUiPortalBuilder implements BrowserAutomationStep{

    WebDriverWait wait
    private String url
    static ConfigurationService configurationService = new ConfigurationService()

    /**
     * Initialize Api Publishing Flow.
     *
     * @param url URL.
     */
    ApiPublisherUiPortalBuilder(String url) {

        this.url = url
    }

    /**
     * Log into Api Publisher.
     * @param webDriver object of RemoteWebDriver
     */
    void apimPublisherLogin(RemoteWebDriver webDriver) {

        webDriver.get(url)

        WebElement username = webDriver.findElement(By.xpath(PageObjects.PUBLISHER_TXT_USERNAME))
        username.clear()
        username.sendKeys(configurationService.getUserIsAsKeyManagerAdminName())

        WebElement password = webDriver.findElement(By.xpath(PageObjects.PUBLISHER_TXT_PASSWORD))
        password.clear()
        password.sendKeys(configurationService.getUserIsAsKeyManagerAdminPWD())

        wait = new WebDriverWait(webDriver, 600)
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_PUBLISHER_CONTINUE)))
        webDriver.findElement(By.xpath(PageObjects.BTN_PUBLISHER_CONTINUE)).click()
        driverWait(webDriver, 20)
    }

    /**
     * Publish Sample API.
     * @param webDriver object of RemoteWebDriver
     * @param webDriver
     */
    void publisherSampleApi(RemoteWebDriver webDriver){

        //Click on Create API
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_CREATE_API)))
        webDriver.findElement(By.xpath(PageObjects.BTN_CREATE_API)).click()

        //Click on Deploy Sample API
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_DEPLOY_SAMPLE_API)))
        webDriver.findElement(By.xpath(PageObjects.BTN_DEPLOY_SAMPLE_API)).click()

        driverWait(webDriver, 30)

        //Verify API is created
        String apiName = webDriver.findElement(By.xpath(PageObjects.LBL_API_NAME)).getText()

        Assert.assertTrue(apiName.contains("PizzaShackAPI"),
                "API Name should contain 'PizzaShackAPI', but found: " + apiName)
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

        apimPublisherLogin(webDriver)
        publisherSampleApi(webDriver)
    }
}
