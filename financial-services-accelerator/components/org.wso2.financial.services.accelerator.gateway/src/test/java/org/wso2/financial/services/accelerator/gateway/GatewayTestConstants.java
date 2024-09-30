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

package org.wso2.financial.services.accelerator.gateway;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Constants for Gateway test cases.
 */
public class GatewayTestConstants {

    public static final String VALID_EXECUTOR_CLASS =
            "org.wso2.financial.services.accelerator.gateway.executor.core.MockOBExecutor";
    public static final Map<Integer, String> VALID_EXECUTOR_MAP = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>(1, VALID_EXECUTOR_CLASS))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    public static final Map<String, Map<Integer, String>> FULL_VALIDATOR_MAP = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>("Default", VALID_EXECUTOR_MAP),
                    new AbstractMap.SimpleImmutableEntry<>("DCR", VALID_EXECUTOR_MAP))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    public static final String CUSTOM_PAYLOAD = "{\"custom\":\"payload\"}";
    public static final String B64_PAYLOAD = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva" +
            "G4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ";
    public static final String TEST_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwI" +
            "iwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    public static final String XML_PAYLOAD = "<soapenv:Body " +
            "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<text><content>Test Content</content><date>2024-09-30</date></text>" +
            "</soapenv:Body>";
}
