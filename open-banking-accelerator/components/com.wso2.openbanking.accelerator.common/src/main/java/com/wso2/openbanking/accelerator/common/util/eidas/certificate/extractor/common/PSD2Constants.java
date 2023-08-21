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

package com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.common;

/**
 * PSD2Constants class.
 */
public class PSD2Constants {

    //Role Names on the certificate
    public static final String PSP_AS = "PSP_AS";
    public static final String PSP_PI = "PSP_PI";
    public static final String PSP_AI = "PSP_AI";
    public static final String PSP_IC = "PSP_IC";

    //PSD2 Role OIDs in the certificate
    public static final String PSP_AS_OID = "0.4.0.19495.1.1";
    public static final String PSP_PI_OID = "0.4.0.19495.1.2";
    public static final String PSP_AI_OID = "0.4.0.19495.1.3";
    public static final String PSP_IC_OID = "0.4.0.19495.1.4";

    //PSD2 Role Names
    public static final String ASPSP = "ASPSP";
    public static final String PISP = "PISP";
    public static final String AISP = "AISP";
    public static final String CBPII = "CBPII";
}
