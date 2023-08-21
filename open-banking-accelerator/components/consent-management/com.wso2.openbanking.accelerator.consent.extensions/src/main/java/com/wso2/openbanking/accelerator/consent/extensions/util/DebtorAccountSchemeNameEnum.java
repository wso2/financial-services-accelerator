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
package com.wso2.openbanking.accelerator.consent.extensions.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Specifies the Schema Names of Debtor Account.
 */
public enum DebtorAccountSchemeNameEnum {

    BBAN("OB.BBAN"),

    IBAN("OB.IBAN"),

    PAN("OB.PAN"),

    PAYM("OB.Paym"),

    SORT_CODE_NUMBER("OB.SortCodeAccountNumber");

    private String value;

    DebtorAccountSchemeNameEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static DebtorAccountSchemeNameEnum fromValue(String text) {

        List<DebtorAccountSchemeNameEnum> accountList = Arrays.asList(DebtorAccountSchemeNameEnum.values());
        Optional<DebtorAccountSchemeNameEnum> accountOpt = accountList
                .stream()
                .filter(i -> String.valueOf(i.value).equals(text))
                .findAny();

        return accountOpt.isPresent() ? accountOpt.get() : null;
    }

}
