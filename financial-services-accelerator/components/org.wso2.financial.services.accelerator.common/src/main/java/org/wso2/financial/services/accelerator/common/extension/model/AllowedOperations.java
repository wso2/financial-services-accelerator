/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.common.extension.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * Allowed Operations
 */
public class AllowedOperations {

    private AllowedOperationsEnum operation;

    public AllowedOperationsEnum getOperation() {
        return operation;
    }

    public void setOperation(AllowedOperationsEnum operation) {
        this.operation = operation;
    }

    /**
     * Allowed Operations enum
     */
    public enum AllowedOperationsEnum {

        VALIDATE("validate"),
        SAVE("save");

        private final String value;

        AllowedOperationsEnum(String enumValue) {
            value = enumValue;
        }

        public String value() {
            return value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        /**
         * Convert a String into String, as specified in the
         * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See
         * JAX RS 2.0 Specification, section 3.2, p. 12</a>
         */
        public static AllowedOperationsEnum fromString(String s) {
            for (AllowedOperationsEnum b : AllowedOperationsEnum.values()) {
                // using Objects.toString() to be safe if value type non-object type
                // because types like 'int' etc. will be auto-boxed
                if (Objects.toString(b.value).equals(s)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected string value '" + s + "'");
        }

        @JsonCreator
        public static AllowedOperationsEnum fromValue(String value) {
            for (AllowedOperationsEnum b : AllowedOperationsEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }
}
