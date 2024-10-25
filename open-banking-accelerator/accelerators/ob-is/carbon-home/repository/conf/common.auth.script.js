var psuChannel = 'Online Banking';

var onLoginRequest = function(context) {
    publishAuthData(context, "AuthenticationAttempted", {'psuChannel': psuChannel});
    executeStep(1, {
        onSuccess: function (context) {
            Log.info("Authentication Successful");
            context.selectedAcr = "urn:mace:incommon:iap:silver";
            publishAuthData(context, "AuthenticationSuccessful", {'psuChannel': psuChannel});
        },
        onFail: function (context) {
            Log.info("Authentication Failed");
            publishAuthData(context, "AuthenticationFailed", {'psuChannel': psuChannel});
        }
    });
};
