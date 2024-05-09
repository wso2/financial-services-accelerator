package com.wso2.openbanking.accelerator.identity.app2app;


import com.wso2.openbanking.accelerator.identity.app2app.exception.SecretValidationException;
import com.wso2.openbanking.accelerator.identity.app2app.model.Secret;
import com.wso2.openbanking.accelerator.identity.app2app.utils.App2AppAuthUtils;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCache;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.push.PushAuthenticator;
import org.wso2.carbon.identity.application.common.model.Property;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * App2App authenticator for authenticating users from native auth attempt.
 */
public class App2AppAuthenticator extends PushAuthenticator {

    private static IdentityCache identityCache;
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
    protected void processAuthenticationResponse(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse,
                                                 AuthenticationContext authenticationContext)
            throws AuthenticationFailedException {

        String jwtString = httpServletRequest.getParameter(App2AppAuthenticatorConstants.SECRET);
        try {
            Secret secret = new Secret(jwtString);
            App2AppAuthUtils.getValidationViolations(secret);
            AuthenticatedUser user = secret.getAuthenticatedUser();
            authenticationContext.setSubject(user);
        } catch (SecretValidationException e) {
            throw new AuthenticationFailedException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new AuthenticationFailedException("Illegal Argument exception: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new AuthenticationFailedException("Run Time exception: " + e.getMessage(), e);
        }

    }

    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {

        return !StringUtils.isBlank(httpServletRequest.getParameter(App2AppAuthenticatorConstants.SECRET));

    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {

        return request.getParameter(App2AppAuthenticatorConstants.SESSION_DATA_KEY);

    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {

        throw new AuthenticationFailedException("Mandatory parameter secret null or empty in request.");

    }

    //TODO : remove this configuration properties.
    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();
        String firebaseServerKey = "Firebase Server Key";
        Property serverKeyProperty = new Property();
        serverKeyProperty.setName("ServerKey");
        serverKeyProperty.setDisplayName(firebaseServerKey);
        serverKeyProperty.setDescription("Enter the firebase server key ");
        serverKeyProperty.setDisplayOrder(0);
        serverKeyProperty.setRequired(true);
        configProperties.add(serverKeyProperty);
        return configProperties;

    }
}

