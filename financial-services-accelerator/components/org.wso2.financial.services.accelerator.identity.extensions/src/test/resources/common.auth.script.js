var psuChannel = 'Online Banking';

function onLoginRequest(context) {
    reportingData(context, "AuthenticationAttempted", false, psuChannel);

    executeStep(1, {
        onSuccess: function (context) {
            var supportedAcrValues = ['urn:openbanking:psd2:sca', 'urn:openbanking:psd2:ca'];
            var selectedAcr = selectAcrFrom(context, supportedAcrValues);
            reportingData(context, "AuthenticationSuccessful", false, psuChannel);

            if (isACREnabled()) {

                context.selectedAcr = selectedAcr;
                if (isTRAEnabled()) {
                    if (selectedAcr === 'urn:openbanking:psd2:ca') {
                        executeTRAFunction(context);
                    } else {
                        executeStep(2, {
                            onSuccess: function (context) {
                                reportingData(context, "AuthenticationSuccessful", true, psuChannel);
                            },
                            onFail: function (context) {
                                reportingData(context, "AuthenticationFailed", false, psuChannel);
                            }
                        });
                    }
                } else {
                    if (selectedAcr == 'urn:openbanking:psd2:sca') {
                        executeStep(2, {
                            onSuccess: function (context) {
                                reportingData(context, "AuthenticationSuccessful", true, psuChannel);
                            },
                            onFail: function (context) {
                                reportingData(context, "AuthenticationFailed", false, psuChannel);
                            }
                        });
                    }
                }

            } else {
                if (isTRAEnabled()) {
                    executeTRAFunction(context);
                } else {
                    executeStep(2, {
                        onSuccess: function (context) {
                            reportingData(context, "AuthenticationSuccessful", true, psuChannel);
                        },
                        onFail: function (context) {
                            reportingData(context, "AuthenticationFailed", false, psuChannel);
                        }
                    });
                }
            }
        },
        onFail: function (context) { //basic auth fail
            reportingData(context, "AuthenticationFailed", false, psuChannel);
            //retry
        }
    });
}
