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

import com.nimbusds.jose.JWSObject
import groovy.json.JsonSlurper
import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.apache.commons.lang3.StringUtils
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.testng.Assert
import org.wso2.bfsi.test.framework.automation.BrowserAutomation
import org.wso2.bfsi.test.framework.automation.BrowserAutomationStep
import org.wso2.bfsi.test.framework.util.RestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PageObjects
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.ParseException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Client Registration Request Builder Class.
 */
class ManualClientRegistrationRequestBuilder implements BrowserAutomationStep{

    static ConfigurationService configurationService = new ConfigurationService()
    WebDriverWait wait
    private String url, applicationName
    private Boolean isRegulatory
    String clientId, clientSecret

    File xmlFile = new File(configurationService.getTestArtifactLocation()
            .concat("/../accelerator-test-framework/src/main/resources/TestConfiguration.xml"))

    ManualClientRegistrationRequestBuilder(String url, String applicationName, Boolean isRegulatory){
        this.url = url
        this.applicationName = applicationName
        this.isRegulatory = isRegulatory
    }

    /**
     * Log into Api Manager DevPortal.
     * @param webDriver object of RemoteWebDriver
     */
    void devportalLogin(RemoteWebDriver webDriver) {

        webDriver.get(url)

        //Navigate to login page
        wait = new WebDriverWait(webDriver, 100)
        wait.until(
                ExpectedConditions.elementToBeClickable(By.id(PageObjects.ID_DEVPORTAL_SIGN_IN)))
        webDriver.findElement(By.id(PageObjects.ID_DEVPORTAL_SIGN_IN)).click()

        //Enter username and password
        WebElement username = webDriver.findElement(By.id(PageObjects.TXT_DEVPORTAL_USERNAME))
        username.clear()
        username.sendKeys(configurationService.getUserPublisherName())

        WebElement password = webDriver.findElement(By.id(PageObjects.TXT_DEVPORTAL_PASSWORD))
        password.clear()
        password.sendKeys(configurationService.getUserPublisherPWD())

        wait = new WebDriverWait(webDriver, 100)
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_DEVPORTAL_CONTINUE)))
        webDriver.findElement(By.xpath(PageObjects.BTN_DEVPORTAL_CONTINUE)).click()
    }

    /**
     * Create a new application in the DevPortal.
     * @param webDriver
     * @param applicationName
     */
    void createApplication(RemoteWebDriver webDriver, String applicationName) {

        //Click on Applications Tab
        wait = new WebDriverWait(webDriver, 600)
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.TAB_APPLICATIONS)))
        webDriver.findElement(By.xpath(PageObjects.TAB_APPLICATIONS)).click()

        //Add New Application
        wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(PageObjects.BTN_ADD_NEW_APPLICATION)))
        webDriver.findElement(By.xpath(PageObjects.BTN_ADD_NEW_APPLICATION)).click()

        WebElement appName = webDriver.findElement(By.id(PageObjects.TXT_APPLICATION_NAME))
        appName.clear()
        appName.sendKeys(applicationName)

        WebElement dropdownQuota = wait.until(ExpectedConditions.elementToBeClickable(By.id(PageObjects.DD_APP_TOKEN_QUOTA)))
        dropdownQuota.click()

        WebElement unlimitedOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[@role='option' and text()='Unlimited']")))
        unlimitedOption.click()

        sleep(100)

        wait.until(
                ExpectedConditions.elementToBeClickable(By.id(PageObjects.BTN_CREATE_APP_SAVE)))
        webDriver.findElement(By.id(PageObjects.BTN_CREATE_APP_SAVE)).click()

        sleep(50)
    }

    /**
     * Generate application keys in the DevPortal.
     * @param webDriver
     * @param applicationName
     * @param isRegulatory
     */
    void generateApplicationKeys(RemoteWebDriver webDriver, String applicationName, Boolean isRegulatory){

        //Click on Sandbox_keys Tab
        wait = new WebDriverWait(webDriver, 100)
        wait.until(
                ExpectedConditions.elementToBeClickable(By.id(PageObjects.TAB_SANDBOX_KEYS)))
        webDriver.findElement(By.id(PageObjects.TAB_SANDBOX_KEYS)).click()

        //Enter Details
        webDriver.findElement(By.id(PageObjects.CHK_REFRESH_TOKEN)).click()
        webDriver.findElement(By.id(PageObjects.CHK_CODE)).click()

        WebElement redirectUrl = webDriver.findElement(By.id(PageObjects.TXT_REDIRECT_URL))
        redirectUrl.clear()
        redirectUrl.sendKeys(configurationService.getAppInfoRedirectURL())

        if(isRegulatory) {
            Path appCertificateLocation = Paths.get(configurationService.getTestArtifactLocation(),
                    "DynamicClientRegistration", "uk", "tpp1", "signing-keystore", "signing.pem")

            String certificate = new String(Files.readAllBytes(appCertificateLocation))
            WebElement txt_certificate = webDriver.findElement(By.id(PageObjects.TXT_SP_CERTIFICATE))
            txt_certificate.clear()
            txt_certificate.sendKeys(certificate)

            sleep(100)
        }

        WebElement dropdownRegulatory = wait.until(ExpectedConditions.elementToBeClickable(By.id(PageObjects.DD_REGULATORY_TYPE)))
        dropdownRegulatory.click()

        WebElement regulatoryOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[@role='option' and text()='${isRegulatory.toString()}']")))
        regulatoryOption.click()
        sleep(100)

        wait.until(
                ExpectedConditions.elementToBeClickable(By.id(PageObjects.BTN_GENERATE_KEYS)))
        webDriver.findElement(By.id(PageObjects.BTN_GENERATE_KEYS)).click()

        sleep(100)

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath(PageObjects.TXT_CONSUMER_KEY)))
        clientId = webDriver.findElement(By.xpath(PageObjects.TXT_CONSUMER_KEY)).getAttribute("value").toString()
        clientSecret = webDriver.findElement(By.xpath(PageObjects.TXT_CONSUMER_SECRET)).getAttribute("value").toString()

        Assert.assertNotNull(clientId)
        Assert.assertNotNull(clientSecret)

        if(isRegulatory) {
            TestUtil.writeXMLContent(xmlFile.toString(), "Application", "ClientID", clientId,
                    0)
            TestUtil.writeXMLContent(xmlFile.toString(), "Application", "ClientSecret", clientSecret,
                    0)
        } else {
            TestUtil.writeXMLContent(xmlFile.toString(), "NonRegulatoryApplication", "ClientID", clientId,
                    0)
            TestUtil.writeXMLContent(xmlFile.toString(), "NonRegulatoryApplication", "ClientSecret", clientSecret,
                    0)
        }
    }

    /**
     * Execute automation steps using driver
     * @param webDriver driver object.
     * @param context   automation context.
     */
    @Override
    void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {

        devportalLogin(webDriver)
        createApplication(webDriver, applicationName)
        generateApplicationKeys(webDriver, applicationName, isRegulatory)
    }
}
