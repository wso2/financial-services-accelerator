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

package org.wso2.openbanking.test.framework

import org.wso2.openbanking.test.framework.automation.NavigationAutomationStep
import org.wso2.openbanking.test.framework.automation.ScreenshotAutomationStep
import org.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import org.wso2.bfsi.test.framework.CommonTest
import io.restassured.response.Response
import org.wso2.openbanking.test.framework.automation.OBBrowserAutomation
import org.wso2.openbanking.test.framework.utility.OBTestUtil

/**
 * Base class for Open Banking Test
 * All common functions that directly required for test class are implemented in here
 */
class OBTest extends CommonTest {

    /**
     * Method for get browser automation steps that common for all toolkits
     */
    OBBrowserAutomation getBrowserAutomation(int stepDelaySeconds, boolean useSameBrowserSession = false) {
        return new OBBrowserAutomation(stepDelaySeconds, useSameBrowserSession)
    }

    NavigationAutomationStep getNavigationAutomationStep(String authorizeUrl, int timeOut = 10) {
        return new NavigationAutomationStep(authorizeUrl, timeOut)
    }

    ScreenshotAutomationStep getScreenshotAutomationStep() {
        return new ScreenshotAutomationStep()
    }

    WaitForRedirectAutomationStep getWaitForRedirectAutomationStep(String redirectURL = null) {
        return new WaitForRedirectAutomationStep(redirectURL)
    }

    String parseResponseBody(Response response, String jsonPath) {
        return OBTestUtil.parseResponseBody(response, jsonPath)
    }

}

