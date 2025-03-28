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

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxBinary
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.wso2.bfsi.test.framework.configuration.CommonConfigurationService
import org.wso2.bfsi.test.framework.constant.Constants

import java.util.concurrent.TimeUnit

/**
 * Class for keeping and executing Automation steps
 */
class BrowserAutomation {

    private CommonConfigurationService obConfiguration = new CommonConfigurationService()
    private String FIREFOX_DRIVER_NAME = "webdriver.gecko.driver"
    private String CHROME_DRIVER_NAME = "webdriver.chrome.driver"
    private RemoteWebDriver driver
    private final LinkedHashSet<BrowserAutomationStep> automationSteps = new LinkedHashSet<>()
    private final AutomationContext context = new AutomationContext()
    public static final int DEFAULT_DELAY = 5;

    /**
     * * Initialize the Browser Automation Context.
     */
    static class AutomationContext {

        public Optional<String> currentUrl = Optional.empty()
        public int timeoutSeconds
    }

    /**
     * Method for get Chrome Web Driver
     * @return
     */
    RemoteWebDriver getChromeWebDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        if (obConfiguration.getBrowserHeadlessEnabled()) {
            chromeOptions.addArguments(Constants.HEADLESS_TAG, "--window-size=1920,1200")
        }
        return new ChromeDriver(chromeOptions)
    }

    /**
     * Method for get Firefox Web driver
     * @return
     */
    RemoteWebDriver getFireFoxWebDriver() {
        FirefoxBinary firefoxBinary = new FirefoxBinary()
        if (obConfiguration.getBrowserHeadlessEnabled()) {
            firefoxBinary.addCommandLineOptions(Constants.HEADLESS_TAG)
        }
        FirefoxOptions firefoxOptions = new FirefoxOptions()
        firefoxOptions.setBinary(firefoxBinary)
        return new FirefoxDriver(firefoxOptions)
    }

    /**
     * BrowserAutomation constructor with the params
     * @param stepDelaySeconds : int
     * @param useSameBrowserSession : boolean
     */
    BrowserAutomation(int stepDelaySeconds, boolean useSameBrowserSession) {
        switch (obConfiguration.getBrowserPreference()) {
            case Constants.BROWSER_CHROME:
                System.setProperty(CHROME_DRIVER_NAME, obConfiguration.getBrowserWebDriverLocation())
                if (!useSameBrowserSession) {
                    driver = getChromeWebDriver()
                }
                break
            default:
                System.setProperty(FIREFOX_DRIVER_NAME, obConfiguration.getBrowserWebDriverLocation());
                if (!useSameBrowserSession) {
                    driver = getFireFoxWebDriver()
                }
                break
        }
        context.timeoutSeconds = stepDelaySeconds
    }

    /**
     * Add automation step.
     *
     * @param automationStep automation step.
     * @return self.
     */
    BrowserAutomation addStep(BrowserAutomationStep automationStep) {
        automationSteps.add(automationStep)
        return this
    }

    /**
     * Execute Automation Steps.
     */
    AutomationContext execute(boolean closeSession = true) {
        driver.manage().timeouts().implicitlyWait(context.timeoutSeconds, TimeUnit.SECONDS);
        try {
            for (BrowserAutomationStep automationStep : automationSteps) {
                automationStep.execute(driver, context);
                context.currentUrl = Optional.ofNullable(driver.getCurrentUrl());
            }
        } catch (Exception e) {
            new ScreenshotAutomationStep("Point of error").execute(driver, context);
            throw new RuntimeException(e);
        } finally {
            if (closeSession) {
                driver.quit();
            }
        }
        return context;
    }

    /**
     * Get Automation Context.
     *
     * @return automation context.
     */
    AutomationContext getContext() {
        return context
    }

}

