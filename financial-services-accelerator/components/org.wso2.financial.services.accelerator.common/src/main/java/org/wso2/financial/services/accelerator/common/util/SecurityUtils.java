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

package org.wso2.financial.services.accelerator.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Common Security Utils class.
 */
public class SecurityUtils {

    private static final String specialChars = "!@#$%&*()'+,-./:;<=>?[]^_`{|}";

    /**
     * Method to remove new line characters to avoid potential CRLF injection for logs.
     * Bug kind and pattern: SECCRLFLOG - CRLF_INJECTION_LOGS
     *
     * @param string string
     * @return string without new line characters
     */
    public static String sanitizeString(String string) {
        return string.replaceAll("[\r\n]", "");
    }

    /**
     * Method to remove new line characters from a list of strings to avoid potential CRLF injection for logs.
     * Bug kind and pattern: SECCRLFLOG - CRLF_INJECTION_LOGS
     *
     * @param stringList String List
     * @return string without new line characters
     */
    public static List<String> sanitize(List<String> stringList) {
        return stringList.stream()
                .map(SecurityUtils::sanitizeString)
                .collect(Collectors.toList());
    }

    /**
     * Method to remove new line characters from a setmvn  of strings to avoid potential CRLF injection for logs.
     * Bug kind and pattern: SECCRLFLOG - CRLF_INJECTION_LOGS
     *
     * @param stringSet String Set
     * @return string without new line characters
     */
    public static Set<String> sanitize(Set<String> stringSet) {
        return stringSet.stream()
                .map(SecurityUtils::sanitizeString)
                .collect(Collectors.toSet());
    }

    /**
     * Method to validate a string does not contain special characters.
     *
     * @param string String
     * @return whether the string does not contain any special characters
     */
    public static boolean containSpecialChars(String string) {
        return StringUtils.containsAny(string, specialChars);
    }

}
