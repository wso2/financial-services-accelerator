/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.gateway.executor.test;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Constants for testing.
 */
public class TestConstants {

    public static final String INVALID_EXECUTOR_CLASS =
            "com.wso2.openbanking.accelerator.gateway.executor.test.executor.InvalidClass";
    public static final String VALID_EXECUTOR_CLASS =
            "com.wso2.openbanking.accelerator.gateway.executor.test.executor.MockOBExecutor";

    public static final Map<Integer, String> VALID_EXECUTOR_MAP = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>(1, VALID_EXECUTOR_CLASS))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    public static final Map<Integer, String> INVALID_EXECUTOR_MAP = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>(1, INVALID_EXECUTOR_CLASS))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    public static final String CUSTOM_PAYLOAD = "{\"custom\":\"payload\"}";
    public static final Map<String, Map<Integer, String>> FULL_VALIDATOR_MAP = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>("Default", VALID_EXECUTOR_MAP),
            new AbstractMap.SimpleImmutableEntry<>("DCR", VALID_EXECUTOR_MAP))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    public static String authHeader = "eyJ4NXQiOiJNell4TW1Ga09HWXdNV0kwWldObU5EY3hOR1l3WW1NNFpUQ" +
            "TNNV0kyTkRBelpHUXpOR00wWkdSbE5qSmtPREZrWkRSaU9URmtNV0ZoTXpVMlpHVmxOZyIsImtpZCI6Ik16" +
            "WXhNbUZrT0dZd01XSTBaV05tTkRjeE5HWXdZbU00WlRBM01XSTJOREF6WkdRek5HTTBaR1JsTmpKa09ERmt" +
            "aRFJpT1RGa01XRmhNelUyWkdWbE5nX1JTMjU2IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJhZG1pbiIsImF" +
            "1dCI6IkFQUExJQ0FUSU9OIiwiYXVkIjoiaENQUHFwbndTMWFKajd0Zkd6ckVVY3J0R2M0YSIsIm5iZiI6MT" +
            "YxNDU5MDE4NSwiYXpwIjoiaENQUHFwbndTMWFKajd0Zkd6ckVVY3J0R2M0YSIsInNjb3BlIjoicmVhZDpwZ" +
            "XRzIHdyaXRlOnBldHMiLCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0NDNcL29hdXRoMlwvdG9rZW4i" +
            "LCJleHAiOjE2MTQ1OTM3ODUsImlhdCI6MTYxNDU5MDE4NSwianRpIjoiZGU3M2E3MzUtNmQzZi00MWI2LWE" +
            "yYTYtN2U0ZTI1YWQxYTQxIn0.Jsz_0Wo79oBcVb2ibIVuJZ7pnsmIvU1r-RlFANiUYbjyTm8gF5b5CBf5uT" +
            "JvKBM5BkqOSRbfgZdCMKZ7l83yZ5OSYDckwJ7rYKlcyzz50DKXlNW2-4J6d87uC1EOA10mg4pPC9LAH7Zdm" +
            "MtN1JMY13xevKzoYB9FyuFgdLIHI4ALQOxeMAJZm_Y5_qBJgj1usE1FUmQUCdTc4aY3EbYkM9gZRO8Oc4HI" +
            "7nn8eLwVShQqEdVDdtzsn0GJXLUlljxCfSAkVmP3vkxW1ZyC9OmmroONhdeEoJmy4Dr3JWwMNNNuzzFbT8K" +
            "ycU1HwUqHTD5nL5Gs5jzSx8E-JxvdRotUCw";
}
