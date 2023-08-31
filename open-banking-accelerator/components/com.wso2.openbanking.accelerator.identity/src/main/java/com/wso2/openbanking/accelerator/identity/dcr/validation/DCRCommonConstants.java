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
package com.wso2.openbanking.accelerator.identity.dcr.validation;

/**
 * Common constants for dcr.
 */
public class DCRCommonConstants {

    public static final String SOFTWARE_ID = "software_id";
    public static final String INVALID_META_DATA = "invalid_client_metadata";
    public static final String INVALID_SSA = "invalid_software_statement";

    public static final String DCR_VALIDATOR = "DCR.Validator";
    public static final String DCR_JWKS_ENDPOINT_SANDBOX = "DCR.JwksUrlSandbox";
    public static final String DCR_JWKS_ENDPOINT_PRODUCTION = "DCR.JwksUrlProduction";
    public static final String DCR_JWKS_CONNECTION_TIMEOUT = "DCR.JWKS-Retriever.ConnectionTimeout";
    public static final String DCR_JWKS_READ_TIMEOUT = "DCR.JWKS-Retriever.ReadTimeout";
    public static final String ENVIRONMENT_PROD = "production";
    public static final String ENVIRONMENT_SANDBOX = "sandbox";
    public static final String ARRAY_ELEMENT_SEPERATOR = "#";
    public static final String DUPLICATE_APPLICATION_NAME = "CONFLICT_EXISTING_APPLICATION";
    public static final String DCR_REGISTRATION_PARAM_SCOPE = "scope";
    public static final String DCR_REGISTRATION_PARAM_REQUIRED = "Required";
    public static final String DCR_REGISTRATION_PARAM_ALLOWED_VALUES = "AllowedValues";
    public static final String DCR_REGISTRATION_PARAM_REQUIRED_TRUE = "true";

    public static final String POST_APPLICATION_LISTENER = "DCR.ApplicationUpdaterImpl";
    public static final String SOFTWARE_STATEMENT = "software_statement";
    public static final String REGULATORY_ISSUERS = "DCR.RegulatoryIssuers.Issuer";
}
