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

package org.wso2.bfsi.test.framework

import org.apache.coyote.Response
import org.wso2.bfsi.test.framework.automation.BrowserAutomation
import org.wso2.bfsi.test.framework.automation.NavigationAutomationStep
import org.wso2.bfsi.test.framework.automation.ScreenshotAutomationStep
import org.wso2.bfsi.test.framework.automation.WaitForRedirectAutomationStep
import org.wso2.bfsi.test.framework.util.CommonTestUtil
import org.wso2.bfsi.test.framework.util.TestContext

/**
 * Class that can keeps common functions for test cases
 * Should be extended from any product common test class
 * All bfsi layer common functions that directly required for test classes are implemented in here
 */
class CommonTest {

    private TestContext context = TestContext.getInstance()

    /**
     * Save data in Context class
     * @param key
     * @param object
     */
    void addToContext(String key, Object object) {
        context.addContext(key, object)
    }

    /**
     * Retrieve data from Context class
     * @param key
     * @return
     */
    Object getFromContext(String key) {
        return context.getContext(key)
    }

    /**
     * Retrive data from Context class as String
     * @param key
     * @return
     */
    String getStrFromContext(String key) {
        return context.getContext(key) as String
    }

    /**
     * Retrieve context Map from Context class
     * @return
     */
    Map getContextMap() {
        return context.getContextMap()
    }

    /**
     * Method for get browser automation steps that common for all toolkits
     */
    BrowserAutomation getBrowserAutomation(int stepDelaySeconds, boolean useSameBrowserSession = false) {
        return new BrowserAutomation(stepDelaySeconds, useSameBrowserSession)
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
        return CommonTestUtil.parseResponseBody(response, jsonPath)
    }

}

