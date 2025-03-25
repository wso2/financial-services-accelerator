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

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver
import java.util.concurrent.TimeUnit

/**
 * Class for keep frequently used common Automation methods
 */
class AutomationMethod {

    private static final Log log = LogFactory.getLog(AutomationMethod.class)
    private RemoteWebDriver webDriver

    AutomationMethod(RemoteWebDriver webDriver) {
        this.webDriver = webDriver
    }

    void resetWebdriver(RemoteWebDriver driver) {
        this.webDriver = driver
    }

    /**
     * Set value in a text field using Text field ID
     * @param fieldID
     * @param value
     */
    void executeTextField(String fieldID, String value) {
        try {
            WebElement element = webDriver.findElement(By.id(fieldID))
            element.clear()
            element.sendKeys(value)
        } catch (Exception e) {
            log.info("${fieldID} field cannot be found", e)
        }
    }

    /**
     * Set value in a text field using Text field Xpath
     * @param xpath
     * @param value
     */
    void executeTextFieldXpath(String xpath, String value) {
        try {
            WebElement element = webDriver.findElement(By.xpath(xpath))
            element.clear()
            element.sendKeys(value)
        } catch (Exception e) {
            log.info("${xpath} field cannot be found", e)
        }
    }

    /**
     * Execute SMS OTP validation part
     * @param optXpath
     * @param txtOTPId
     * @param otpCode
     */
    void executeSMSOTP(String optXpath, String txtOTPId, String otpCode) {
        //Second Factor Authentication Step
        try {
            if (webDriver.findElement(By.xpath(optXpath)).isDisplayed()) {
                webDriver.findElement(By.id(txtOTPId)).sendKeys(otpCode)
            }
        } catch (NoSuchElementException e) {
            log.info("Second Factor Authentication Step is not configured",e)
        }
    }

    /**
     * Browser wait for time range
     * @param timeRange
     */
    void waitTimeRange(int timeRange = 30) {
        webDriver.manage().timeouts().implicitlyWait(timeRange, TimeUnit.SECONDS)
    }

    /**
     * Automate Click button using Xpath of button
     * @param buttonXpath
     */
    void clickButtonXpath(String buttonXpath) {
        webDriver.findElement(By.xpath(buttonXpath)).click()
    }

    /**
     * Automate submission button using Xpath
     * @param buttonXpath
     */
    void submitButtonXpath(String buttonXpath) {
        webDriver.findElement(By.xpath(buttonXpath)).submit()
    }

    /**
     * Automate Get attribute of an element using Xpath.
     * @param elementXpath
     * @param attribute
     * @return attribute value
     */
    String getElementAttribute(String elementXpath, String attribute) {
        return webDriver.findElement(By.xpath(elementXpath)).getAttribute(attribute)
    }

    /**
     * Automate selection of option in checkbox or radio button using Xpath.
     * @param optionXpath
     * @return
     */
    String selectOption(String optionXpath) {
        webDriver.findElement(By.xpath(optionXpath)).click()
    }

    /**
     * Check whether an element is displayed using Xpath.
     * @param elementXpath - Xpath of the element
     * @return true or false
     */
    Boolean isElementDisplayed(String elementXpath) {
        return webDriver.findElement(By.xpath(elementXpath)).isDisplayed()
    }

    /**
     * Check whether an element is enabled using Xpath.
     * @param elementXpath - Xpath of the element
     * @return true or false
     */
    Boolean isElementEnabled(String elementXpath) {
        return webDriver.findElement(By.xpath(elementXpath)).isEnabled()
    }

    /**
     * Check whether an element is selected using Xpath.
     * @param elementXpath - Xpath of the element
     * @return true or false
     */
    Boolean isElementSelected(String elementXpath) {
        return webDriver.findElement(By.xpath(elementXpath)).isSelected()
    }

    /**
     * Get Text of the element.
     * @param elementXpath - Xpath of the element
     * @return text value
     */
    String getAttributeText(String elementXpath) {
        return webDriver.findElement(By.xpath(elementXpath)).getText().trim()
    }

    /**
     * Is element present in the page.
     * @param locator - Xpath of the element
     * @return true or false
     */
    boolean isElementPresent(String locator) {
        try {
            webDriver.findElement(By.xpath(locator))
            return true
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false
        }
    }
}
