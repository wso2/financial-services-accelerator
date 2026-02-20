package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model;

import java.util.List;

import javax.validation.Valid;

/**
 * Wrapper for pre-consent authorize flow external response data.
 * Now includes display data for UI sections like blocked accounts.
 */
public class PopulateConsentAuthorizeScreenDTO {

    @Valid
    private ConsentDataDTO consentData;

    @Valid
    private ConsumerDataDTO consumerData;

    @Valid
    private List<AdditionalDisplayDataDTO> additionalDisplayData;

    public ConsentDataDTO getConsentData() {
        return consentData;
    }

    public void setConsentData(ConsentDataDTO consentData) {
        this.consentData = consentData;
    }

    public ConsumerDataDTO getConsumerData() {
        return consumerData;
    }

    public void setConsumerData(ConsumerDataDTO consumerData) {
        this.consumerData = consumerData;
    }

    public List<AdditionalDisplayDataDTO> getAdditionalDisplayData() {
        return additionalDisplayData;
    }

    public void setAdditionalDisplayData(List<AdditionalDisplayDataDTO> additionalDisplayData) {
        this.additionalDisplayData = additionalDisplayData;
    }
}
