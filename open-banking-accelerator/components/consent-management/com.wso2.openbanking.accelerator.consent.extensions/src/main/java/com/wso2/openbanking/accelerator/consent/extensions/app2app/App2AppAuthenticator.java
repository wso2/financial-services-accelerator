package com.wso2.openbanking.accelerator.consent.extensions.app2app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class App2AppAuthenticator extends AbstractApplicationAuthenticator implements LocalApplicationAuthenticator {


    private static final Log log = LogFactory.getLog(App2AppAuthenticator.class);
    private static final long serialVersionUID = -5439464372188473141L;

    @Override
    public String getName() {
        return App2AppAuthenticatorConstants.AUTHENTICATOR_NAME;
    }

    @Override
    public String getFriendlyName() {
        return App2AppAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
    }
    @Override
    protected void processAuthenticationResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationContext authenticationContext) throws AuthenticationFailedException {

    }

    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {
        return false;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest httpServletRequest) {
        return null;
    }
}
