/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder

import java.nio.charset.Charset

/**
 * Consent Management Request Builder.
 */
class ConsentRequestBuilder {

    static configurationService = new ConfigurationService()

    /**
     * Build Request Specification for Key Manager.
     * @return
     */
    static RequestSpecification buildKeyManagerRequest(String clientId) {

        def authToken = "${configurationService.getUserKeyManagerAdminName()}:" +
                "${configurationService.getUserKeyManagerAdminPWD()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        return FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.X_WSO2_CLIENT_ID_KEY, clientId)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, basicHeader)
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
    }

    static final String GenerateBasicHeader() {

        def authToken = "${configurationService.getUserKeyManagerAdminName()}:" +
                "${configurationService.getUserKeyManagerAdminPWD()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"
        return basicHeader
    }

    /**
     * Build Request Specification for Key Manager.
     * @param accessToken
     * @return
     */
    static RequestSpecification buildBasicRequest(String accessToken) {

        return FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${accessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
    }
    /**
     * Build Request Specification for Key Manager without Authorization header.
     * @return
     */
    static RequestSpecification buildKeyManagerRequestWithoutAuthorizationHeader(String clientId) {

        return FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.X_WSO2_CLIENT_ID_KEY, clientId)
    }
    /**
     * Build Request Specification for Key Manager without Content Type.
     * @param accessToken
     * @return
     */
    static RequestSpecification buildBasicRequestWithoutContentType() {

        return FSRestAsRequestBuilder.buildRequest()
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)

    }

    /**
     * Build Request Specification for Key Manager.
     * @return
     */
    static RequestSpecification buildKeyManagerRequestForJWT(String clientId) {

        def authToken = "${configurationService.getUserKeyManagerAdminName()}:" +
                "${configurationService.getUserKeyManagerAdminPWD()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        return FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.CONTENT_TYPE_JWT)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.X_WSO2_CLIENT_ID_KEY, clientId)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, basicHeader)
    }


    /**
     * Build Request Specification for Key Manager with incorrect Request Payload
     * @param accessToken
     * @return
     */
    static RequestSpecification buildBasicRequestWithIncorrectRequestPayload() {

        return FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)

    }

    /**
     * Build Request Specification for Key Manager with Invalid Value for Content Type.
     * @param accessToken
     * @return
     */
    static RequestSpecification buildBasicRequestWithInvalidValueContentType() {

        return FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.XML)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)

    }

    /**
     * Build Request Specification for Key Manager with Invalid Value for Accept Header Type.
     * @param accessToken
     * @return
     */
    static RequestSpecification buildBasicRequestWithInvalidAcceptHeaderValue() {

        return FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.XML)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${GenerateBasicHeader()}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_MULTIPART)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)

    }

    /**
     * Build Request Specification for Key Manager without Read AccountsDetail Permission
     * @param accessToken
     * @return
     */
    static RequestSpecification buildBasicRequestWithoutReadAccountDetail(String accessToken) {

        return FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${AcceleratorTestConstants.BEARER} ${accessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)

    }

    /**
     * Build Request Specification for Key Manager with incorrect access token
     * @param accessToken
     * @return
     */
    static RequestSpecification buildBasicRetrievalRequestWithIncorrectAccessToken(String userAccessToken) {

        return FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${AcceleratorTestConstants.BEARER} ${userAccessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
    }

    /**
     * Build Request Specification for Key Manager without Authorization header.
     * @param accessToken
     * @return
     */
    static RequestSpecification buildBasicRequestWithDifferentSearchParams(String accessToken){

        return FSRestAsRequestBuilder.buildRequest()
                .queryParam("clientId", configurationService.getAppInfoClientID())
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "${AcceleratorTestConstants.BEARER} ${accessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
    }
}
