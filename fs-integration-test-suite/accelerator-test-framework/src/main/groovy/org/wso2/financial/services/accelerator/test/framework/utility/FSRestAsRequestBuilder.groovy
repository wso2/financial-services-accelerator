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

package org.wso2.financial.services.accelerator.test.framework.utility

import org.wso2.bfsi.test.framework.exception.TestFrameworkException
import org.wso2.openbanking.test.framework.utility.RestAsRequestBuilder
import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.apache.http.conn.ssl.SSLSocketFactory
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants

/**
 * Accelerator Class to provide Basic Rest-assured Request Objects.
 * Used to inherit methods from OB layer to Accelerator layer.
 */
class FSRestAsRequestBuilder extends RestAsRequestBuilder {

    /**
     * Get Base Request specification without MTLS.
     *
     * @return request specification.
     */
    public static RequestSpecification buildBasicRequestWithoutTlsContext() {
        return RestAssured.given()
                .relaxedHTTPSValidation()
                .urlEncodingEnabled(true);
    }

    /**
     * Get Base Request Specification with defined keystore.
     * @param keystoreLocation keystore file path.
     * @param keystorePassword keystore password.
     * @return request specification.
     * @throws org.wso2.bfsi.test.framework.exception.TestFrameworkException exception.
     */
    public static RequestSpecification buildRequest(String keystoreLocation, String keystorePassword)
            throws TestFrameworkException {

        RestAssuredConfig config = null;
        SSLSocketFactory sslSocketFactory = TestUtil.getSslSocketFactory(keystoreLocation, keystorePassword);
        if (sslSocketFactory != null) {
            config = RestAssuredConfig.newConfig().sslConfig(RestAssured.config()
                    .getSSLConfig()
                    .sslSocketFactory(TestUtil.getSslSocketFactory(keystoreLocation, keystorePassword)));
        } else {
            throw new TestFrameworkException("Unable to retrieve the SSL socket factory");
        }
        return RestAssured.given()
                .config(config.encoderConfig(EncoderConfig.encoderConfig()
                        .encodeContentTypeAs(ConnectorTestConstants.CONTENT_TYPE_JWT, ContentType.TEXT)))
                .urlEncodingEnabled(true);
    }
}
