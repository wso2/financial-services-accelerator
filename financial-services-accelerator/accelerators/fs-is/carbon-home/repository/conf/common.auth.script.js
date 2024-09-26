var psuChannel = 'Online Banking';

var onLoginRequest = function(context) {
    publishAuthData(context, "AuthenticationAttempted", {'psuChannel': psuChannel});
    executeStep(1, {
        onSuccess: function (context) {
            Log.info("Authentication Successful");
            publishAuthData(context, "AuthenticationSuccessful", {'psuChannel': psuChannel});
        },
        onFail: function (context) {
            Log.info("Authentication Failed");
            publishAuthData(context, "AuthenticationFailed", {'psuChannel': psuChannel});
        }
    });
};
