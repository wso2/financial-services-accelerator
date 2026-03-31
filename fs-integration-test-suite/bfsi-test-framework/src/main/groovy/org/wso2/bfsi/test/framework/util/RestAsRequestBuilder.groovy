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

package org.wso2.bfsi.test.framework.util

import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.Filter
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.apache.http.conn.ssl.SSLSocketFactory
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.wso2.bfsi.test.framework.configuration.CommonConfigurationService
import org.wso2.bfsi.test.framework.constant.Constants
import org.wso2.bfsi.test.framework.exception.TestFrameworkException

import java.security.Security

/**
 * OB Class for provide Basic Rest-assured Request Objects
 */
class RestAsRequestBuilder {

    /**
     * Initialize Test Framework.
     */
    static void init() {

        List<Filter> filterList = new ArrayList<>();
        filterList.add(new RequestLoggingFilter(System.out));
        filterList.add(new ResponseLoggingFilter(System.out));
        Security.addProvider(new BouncyCastleProvider());
        RestAssured.filters(filterList);
    }

    private static CommonConfigurationService configurationService = new CommonConfigurationService()

    /**
     * Get Base Request Specification.
     *
     * @return request specification.
     */
    static RequestSpecification buildRequest() throws TestFrameworkException {

        if (configurationService.getAppTransportMLTSEnable()) {
            RestAssuredConfig config = null;
            SSLSocketFactory sslSocketFactory = CommonTestUtil.getSslSocketFactory()
            if (sslSocketFactory != null) {
                config = RestAssuredConfig.newConfig().sslConfig(RestAssured.config()
                        .getSSLConfig()
                        .sslSocketFactory(CommonTestUtil.getSslSocketFactory()));
            } else {
                throw new TestFrameworkException("Unable to retrieve the SSL socket factory");
            }
            return RestAssured.given()
                    .config(config.encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs(Constants.CONTENT_TYPE_APPLICATION_JWT
                            , ContentType.TEXT)))
                    .urlEncodingEnabled(true);
        } else {
            // Use relaxed HTTPS validation if MTLS is disabled.
            return RestAssured.given()
                    .relaxedHTTPSValidation()
                    .urlEncodingEnabled(true);
        }
    }

    /**
     * Get Base Request specification without MTLS
     *
     * @return request specification.
     */
    static RequestSpecification buildBasicRequest() {
        return RestAssured.given()
                .relaxedHTTPSValidation()
                .urlEncodingEnabled(true);
    }

    public static RequestSpecification buildRequestForEncryptedPayload() throws TestFrameworkException {

        if (configurationService.getAppTransportMLTSEnable()) {
            RestAssuredConfig config = null;
            SSLSocketFactory sslSocketFactory = CommonTestUtil.getSslSocketFactory()
            if (sslSocketFactory != null) {
                config = RestAssuredConfig.newConfig().sslConfig(RestAssured.config()
                        .getSSLConfig()
                        .sslSocketFactory(CommonTestUtil.getSslSocketFactory()));
            } else {
                throw new TestFrameworkException("Unable to retrieve the SSL socket factory");
            }
            return RestAssured.given()
                    .config(config.encoderConfig(EncoderConfig.encoderConfig()
                            .encodeContentTypeAs("application/jose+jwe"
                                    , ContentType.TEXT)))
                    .urlEncodingEnabled(true);
        } else {
            // Use relaxed HTTPS validation if MTLS is disabled.
            return RestAssured.given()
                    .relaxedHTTPSValidation()
                    .urlEncodingEnabled(true);
        }
    }
}

