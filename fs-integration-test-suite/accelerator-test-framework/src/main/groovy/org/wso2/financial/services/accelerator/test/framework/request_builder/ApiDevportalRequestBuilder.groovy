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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.testng.Assert
import org.wso2.bfsi.test.framework.automation.BrowserAutomation
import org.wso2.bfsi.test.framework.automation.BrowserAutomationStep
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PageObjects
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.util.concurrent.TimeUnit

/**
 * Class to contain steps to create a user role in API Manager Console.
 */
class ApiDevportalRequestBuilder extends FSAPIMConnectorTest{

    static ConfigurationService configurationService = new ConfigurationService()
    String devportalUrl = configurationService.getApimServerUrl() + ConnectorTestConstants.INTERNAL_APIM_DEVPORTAL_ENDPOINT

    /**
     * Retrieve the application IDs from the API Devportal.
     * @param accessToken
     * @return
     */
    List<String> retrieveApplicationIds(String accessToken) {

        Response getApplicationResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .get(devportalUrl + "/applications")

        def result = TestUtil.parseResponseBody(getApplicationResponse, "list.applicationId")

        String trimmed = result.replaceAll(/^\[|\]$/, "")

        List<String> applicationIds = trimmed.split(/\s*,\s*/).toList()

        return applicationIds
    }

    /**
     * Retrieve the API IDs from the API Devportal.
     * @param accessToken
     * @return
     */
    List<String> retrieveApiIds(String accessToken) {

        Response getApiResponse = FSRestAsRequestBuilder.buildBasicRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .get(devportalUrl + "/apis")

        def result = TestUtil.parseResponseBody(getApiResponse, "list.id")

        String trimmed = result.replaceAll(/^\[|\]$/, "")

        List<String> apiIds = trimmed.split(/\s*,\s*/).toList()

        return apiIds
    }

    /**
     * Subscribe to APIs for the given application IDs.
     * @param accessToken
     * @param applicationIds
     * @param apiIds
     */
    Response subscribeToApis(String accessToken, List<String> applicationIds, List<String> apiIds) {

        Response getApiResponse

        for (String applicationId : applicationIds) {
            for (String apiId : apiIds) {

                getApiResponse = FSRestAsRequestBuilder.buildBasicRequest()
                        .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken)
                        .contentType(ContentType.JSON)
                        .body(RequestPayloads.getApiSubscriptionPayload(applicationId, apiId))
                        .post(devportalUrl + "/subscriptions")
            }
        }

        return getApiResponse
    }
}
