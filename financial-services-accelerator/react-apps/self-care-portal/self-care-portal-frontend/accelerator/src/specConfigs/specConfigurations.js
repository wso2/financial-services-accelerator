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

import {consentTypes, dataOrigins, dataTypes, keyDateTypes, permissionBindTypes} from "./common";

export const specConfigurations =
    {
        // key wordings for the relevant statuses.
        status: {
            authorised: "authorised",
            expired: "Expired",
            revoked: "Revoked",
            consumed: "Consumed",
        },
        consent: {
            // if consent is in `authorised` state, `expirationTimeAttribute` parameter from consent data
            // will provide the expirationTime for UI validations.
            expirationTimeAttribute: "receipt.Data.ExpirationDateTime",
            expirationTimeDataType: dataTypes.date,
            // permissionBindTypes status the type of permission binding to the account
            permissionsView: {
                permissionBindType: permissionBindTypes.samePermissionSetForAllAccounts,
                permissionsAttribute: "receipt.Data.Permissions",
            }
        },
        application: {
            logoURLAttribute: "software_logo_uri",
            displayNameAttribute: "software_client_name",
            failOverDisplayNameAttribute: "software_id"
        }
    };
export const account_lang = [
    {
        id: "authorised",
        label: "Active",
        labelBadgeVariant: "success",
        isRevocableConsent: true,
        description:
            "A list of applications that have requested access to your account information.",
        tableHeaders: [
            {
                heading: "Applications",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "softwareClientName",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
            {
                heading: "Consented Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "createdTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Expiry Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "receipt.Data.ExpirationDateTime",
                failOverDataParameterKey: "",
                dataType: dataTypes.date,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Status",
                dataOrigin: dataOrigins.status,
                dataParameterKey: "currentStatus",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
            {
                heading: "Action",
                dataOrigin: dataOrigins.action,
                dataParameterKey: "",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
        ],
        profile: {
            confirmation: "View confirmation of consent >"
        },
        keyDatesInfoLabel: "Key Dates",
        keyDates: [
            {
                title: "You granted consent on",
                type: keyDateTypes.date,
                dateParameterKey: "createdTimestamp",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "Your consent will expire on",
                type: keyDateTypes.date,
                dateParameterKey: "receipt.Data.ExpirationDateTime",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "How often your data will be shared",
                type: keyDateTypes.text,
                dateParameterKey: "",
                dateFormat: "",
                text: "Ongoing"
            }
        ],
        accountsInfoLabel: "Accounts",
        dataSharedLabel: "We share the following data via the",
        accreditation: {
            accreditationLabel: "Accreditation",
            accreditWebsite: "is an accredited API consumer application. You can check at",
            accreditWebsiteLinkText: "website",
            accreditWebsiteLink: "https://www.test.co.uk/",
            accreditDR: "API Consumer Application:"
        }
    },
    {
        id: "expired,revoked",
        label: "Inactive",
        labelBadgeVariant: "secondary",
        isRevocableConsent: true,
        description:
            "A list of applications that have requested access to your account information.",
        tableHeaders: [
            {
                heading: "Applications",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "softwareClientName",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
            {
                heading: "Consented Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "createdTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Expiry Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "receipt.Data.ExpirationDateTime",
                failOverDataParameterKey: "",
                dataType: dataTypes.date,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Status",
                dataOrigin: dataOrigins.status,
                dataParameterKey: "currentStatus",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
            {
                heading: "Action",
                dataOrigin: dataOrigins.action,
                dataParameterKey: "",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
        ],
        profile: {
            confirmation: "Download confirmation of consent"
        },
        keyDatesInfoLabel: "Key Dates",
        keyDates: [
            {
                title: "You granted consent on",
                type: keyDateTypes.date,
                dateParameterKey: "createdTimestamp",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "Your consent will expire on",
                type: keyDateTypes.date,
                dateParameterKey: "receipt.Data.ExpirationDateTime",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "How often your data will be shared",
                type: keyDateTypes.text,
                dateParameterKey: "",
                dateFormat: "",
                text: "Ongoing"
            }
        ],
        accountsInfoLabel: "Accounts",
        dataSharedLabel: "We share the following data via the",
        accreditation: {
            accreditationLabel: "Accreditation",
            accreditWebsite: "is an accredited API consumer application. You can check at",
            accreditWebsiteLinkText: "website",
            accreditWebsiteLink: "https://www.test.co.uk/",
            accreditDR: "API Consumer Application:"
        }
    },
];
export const cof_lang = [
    {
        id: "authorised",
        label: "Active",
        labelBadgeVariant: "success",
        isRevocableConsent: true,
        description:
            "A list of applications that have requested access to your confirmation of funds.",
        tableHeaders: [
            {
                heading: "Applications",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "softwareClientName",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
            {
                heading: "Consented Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "createdTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Expiry Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "receipt.Data.ExpirationDateTime",
                failOverDataParameterKey: "",
                dataType: dataTypes.date,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Status",
                dataOrigin: dataOrigins.status,
                dataParameterKey: "currentStatus",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
            {
                heading: "Action",
                dataOrigin: dataOrigins.action,
                dataParameterKey: "",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
        ],
        profile: {
            confirmation: "View confirmation of consent >"
        },
        keyDatesInfoLabel: "Key Dates",
        keyDates: [
            {
                title: "Account Name",
                type: keyDateTypes.value,
                valueParameterKey: "receipt.Data.DebtorAccount.Name",
            },
            {
                title: "Account Scheme Name",
                type: keyDateTypes.value,
                valueParameterKey: "receipt.Data.DebtorAccount.SchemeName",
            },
            {
                title: "When you gave consent",
                type: keyDateTypes.date,
                dateParameterKey: "createdTimestamp",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "When consent was expired",
                type: keyDateTypes.date,
                dateParameterKey: "receipt.Data.ExpirationDateTime",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "How often your data will be shared",
                type: keyDateTypes.text,
                dateParameterKey: "",
                dateFormat: "",
                text: "Ongoing"
            }
        ],
        accountsInfoLabel: "Accounts",
        accreditation: {
            accreditationLabel: "Accreditation",
            accreditWebsite: "is an accredited API consumer application. You can check at",
            accreditWebsiteLinkText: "website",
            accreditWebsiteLink: "https://www.test.co.uk/",
            accreditDR: "API Consumer Application:"
        }
    },
    {
        id: "expired,revoked",
        label: "Inactive",
        labelBadgeVariant: "secondary",
        description:
            "A list of applications that have requested access to your confirmation of funds.",
        tableHeaders: [
            {
                heading: "Applications",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "softwareClientName",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
            {
                heading: "Consented Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "createdTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Expiry Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "receipt.Data.ExpirationDateTime",
                failOverDataParameterKey: "",
                dataType: dataTypes.date,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Status",
                dataOrigin: dataOrigins.status,
                dataParameterKey: "currentStatus",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
            {
                heading: "Action",
                dataOrigin: dataOrigins.action,
                dataParameterKey: "",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
        ],
        profile: {
            confirmation: "View consent expiry confirmation >"
        },
        keyDatesInfoLabel: "Key Dates",
        keyDates: [
            {
                title: "Account Name",
                type: keyDateTypes.value,
                valueParameterKey: "receipt.Data.DebtorAccount.Name",
            },
            {
                title: "Account Scheme Name",
                type: keyDateTypes.value,
                valueParameterKey: "receipt.Data.DebtorAccount.SchemeName",
            },
            {
                title: "When you gave consent",
                type: keyDateTypes.date,
                dateParameterKey: "createdTimestamp",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "When consent was expired",
                type: keyDateTypes.date,
                dateParameterKey: "receipt.Data.ExpirationDateTime",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "How often your data will be shared",
                type: keyDateTypes.text,
                dateParameterKey: "",
                dateFormat: "",
                text: "Ongoing"
            }
        ],
        accountsInfoLabel: "Accounts",
        accreditation: {
            accreditationLabel: "Accreditation",
            accreditWebsite: "is an accredited API consumer application. You can check at",
            accreditWebsiteLinkText: "website",
            accreditWebsiteLink: "https://www.test.co.uk/",
            accreditDR: "API Consumer Application:"
        }
    },
];
export const payments_lang = [
    {
        id: "authorised,consumed,expired,revoked",
        labelBadgeVariant: "secondary",
        isRevocableConsent: true,
        description:
            "A list of applications that have requested access to your payments.",
        tableHeaders: [
            {
                heading: "Applications",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "softwareClientName",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
            {
                heading: "Consented Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "createdTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Status",
                dataOrigin: dataOrigins.status,
                dataParameterKey: "currentStatus",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
            {
                heading: "Action",
                dataOrigin: dataOrigins.action,
                dataParameterKey: "",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
        ],
        profile: {
            confirmation: "View confirmation of consent >"
        },
        keyDatesInfoLabel: "Payment Details",
        keyDates: [
            {
                title: "Amount",
                type: keyDateTypes.value,
                valueParameterKey: "receipt.Data.Initiation.InstructedAmount",
            },
            {
                title: "Payer Scheme Name",
                type: keyDateTypes.value,
                valueParameterKey: "receipt.Data.Initiation.DebtorAccount.SchemeName",
            },
            {
                title: "Payer Identification",
                type: keyDateTypes.value,
                valueParameterKey: "receipt.Data.Initiation.DebtorAccount.Identification",
            },
            {
                title: "Payer Name",
                type: keyDateTypes.value,
                valueParameterKey: "receipt.Data.Initiation.DebtorAccount.Name",
            },
            {
                title: "Payee Scheme Name",
                type: keyDateTypes.value,
                valueParameterKey: "receipt.Data.Initiation.CreditorAccount.SchemeName",
            },
            {
                title: "Payee Identification",
                type: keyDateTypes.value,
                valueParameterKey: "receipt.Data.Initiation.CreditorAccount.Identification",
            },
            {
                title: "Payee Name",
                type: keyDateTypes.value,
                valueParameterKey: "receipt.Data.Initiation.CreditorAccount.Name",
            },
        ],
        accountsInfoLabel: "Accounts",
        accreditation: {
            accreditationLabel: "Accreditation",
            accreditWebsite: "is an accredited API consumer application. You can check at",
            accreditWebsiteLinkText: "website",
            accreditWebsiteLink: "https://www.test.co.uk/",
            accreditDR: "API Consumer Application:"
        }
    },
];

export const lang = {
    [consentTypes[0].id]: account_lang,
    [consentTypes[1].id]: payments_lang,
    [consentTypes[2].id]: cof_lang
}
