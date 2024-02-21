package com.wso2.openbanking.accelerator.consent.extensions.authorize.vrp.retrieval.flow;

import org.testng.annotations.DataProvider;
/**
 *  Data Provider for Consent Executor Tests.
 */
public class ConsentExtensionDataProvider {

    @DataProvider(name = "PaymentConsentDataDataProvider")
    Object[][] getPaymentConsentDataDataProvider() {

        return new Object[][]{
                {ConsentAuthorizeTestConstants.PAYMENT_INITIATION},
                {ConsentAuthorizeTestConstants.INTERNATIONAL_PAYMENT_INITIATION}
        };
    }
}
