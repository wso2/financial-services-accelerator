var psuChannel = 'Online Banking';

var onLoginRequest = function(context) {
    executeStep(1, {
        onSuccess: function (context) {
            Log.info("Authentication Successful");
            context.selectedAcr = "urn:mace:incommon:iap:silver";
        },
        onFail: function (context) {
            Log.info("Authentication Failed");
        }
    });
};
