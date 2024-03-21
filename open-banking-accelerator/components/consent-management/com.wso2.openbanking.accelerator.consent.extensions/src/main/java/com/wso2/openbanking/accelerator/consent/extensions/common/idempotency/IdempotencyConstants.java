/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.accelerator.consent.extensions.common.idempotency;

/**
 * Constants related to idempotency operations.
 */
public class IdempotencyConstants {

    public static final String IDEMPOTENCY_IS_ENABLED = "Consent.Idempotency.Enabled";
    public static final String IDEMPOTENCY_ALLOWED_TIME = "Consent.Idempotency.AllowedTimeDuration";
    public static final String ERROR_PAYLOAD_NOT_SIMILAR = "Payloads are not similar. Hence this is not a valid" +
            " idempotent request";
    public static final String ERROR_AFTER_ALLOWED_TIME = "Request received after the allowed time., Hence this is" +
            " not a valid idempotent request";
    public static final String ERROR_MISMATCHING_CLIENT_ID = "Client ID sent in the request does not match with the" +
            " client ID in the retrieved consent. Hence this is not a valid idempotent request";
    public static final String ERROR_NO_CONSENT_DETAILS = "No consent details found for the consent ID, Hence this" +
            " is not a valid idempotent request";
    public static final String JSON_COMPARING_ERROR = "Error occurred while comparing JSON payloads";
}
