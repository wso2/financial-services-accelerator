package com.wso2.openbanking.accelerator.identity.app2app;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCache;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCacheKey;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.push.PushAuthenticator;
import org.wso2.carbon.identity.application.authenticator.push.common.exception.PushAuthTokenValidationException;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerClientException;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerServerException;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.impl.DeviceHandlerImpl;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.model.Device;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
        AuthenticatedUser user = this.getAuthenticatedUser(httpServletRequest);
        authenticationContext.setSubject(user);
        String jwtString = httpServletRequest.getParameter(App2AppAuthenticatorConstants.SECRET);
        try {
            SignedJWT signedJWT = App2AppAuthUtil.getSignedJWT(jwtString);
            JWTClaimsSet jwtClaimsSet = App2AppAuthUtil.getJWTClaimsSet(signedJWT);
            String jti = App2AppAuthUtil.getClaim(jwtClaimsSet,App2AppAuthenticatorConstants.JTI);
            if (!validateJTI(jti)){
                throw new AuthenticationFailedException("JTI value "+jti+" has been replayed.");
            };
            if (!validateAppAuthAttempt(user,signedJWT,jwtClaimsSet)){
                throw new AuthenticationFailedException("JWT validation failed.");
            };
        } catch (PushDeviceHandlerServerException e) {
            throw new AuthenticationFailedException("Error occurred when trying to redirect to the registered devices"
                + " page. Devices were not found for user: " + user.toFullQualifiedUsername() + ".", e);
        } catch (PushAuthTokenValidationException e) {
            throw new AuthenticationFailedException("JWT Token validation Failed",e);
        } catch (UserStoreException e) {
            throw new AuthenticationFailedException("Error occurred when trying to get the user ID for user: "
                    + user.toFullQualifiedUsername() + ".", e);
        } catch (PushDeviceHandlerClientException e) {
            throw new AuthenticationFailedException("Error occurred when trying to get user claims for user: "
                    + user.toFullQualifiedUsername() + ".", e);
        } catch (ParseException e) {
            throw new AuthenticationFailedException("Error while parsing JWT.",e);
        } catch (IllegalArgumentException e){
            throw new AuthenticationFailedException(e.getMessage(),e);
        }

    }

    private boolean validateAppAuthAttempt(AuthenticatedUser user, SignedJWT signedJWT,
                                        JWTClaimsSet jwtClaimsSet)
            throws AuthenticationFailedException, UserStoreException,
                PushDeviceHandlerServerException, PushDeviceHandlerClientException,
                PushAuthTokenValidationException {
        String userID = getUserIdFromUsername(user.getUserName(), getUserRealm(user));
        String deviceID = App2AppAuthUtil.getClaim(jwtClaimsSet,App2AppAuthenticatorConstants.DEVICE_IDENTIFIER);
        String publicKey;
        if (!StringUtils.isBlank(deviceID)) {
            publicKey = getPublicKey(deviceID, userID);
        }else{
            throw new IllegalArgumentException("Required Parameter deviceId is null or empty.");
        }
        return App2AppAuthUtil.validateJWT(signedJWT,publicKey,
                App2AppAuthenticatorConstants.SIGNING_ALGORITHM);
    }

    private Device getRegisteredDevice(String deviceId, String userID)
            throws PushDeviceHandlerServerException, IllegalArgumentException, PushDeviceHandlerClientException {
        DeviceHandlerImpl deviceHandler = new DeviceHandlerImpl();
        List<Device> deviceList = deviceHandler.listDevices(userID);
        for (Device device : deviceList) {
            if (StringUtils.equals(device.getDeviceId(),deviceId)){
                String publicKey = deviceHandler.getPublicKey(deviceId);
                device.setPublicKey(publicKey);
                return device;
            }
        }
        throw new IllegalArgumentException("Provided device Identifier does not exist.");
    }

    private boolean validateJTI(String jti) throws IllegalArgumentException {
        if (!StringUtils.isBlank(jti)){
            IdentityCacheKey jtiCacheKey = new IdentityCacheKey(jti);
            if (getFromCache(jtiCacheKey) != null){
                return false;
            }
            addToCache(jtiCacheKey,jti);
            return true;
        }else{
            throw new IllegalArgumentException("Required parameter jti null or empty in JWT.");
        }
    }

    private Object getFromCache(IdentityCacheKey identityCacheKey){
        if (identityCache == null){
            identityCache = new IdentityCache();
        }
        return identityCache.getFromCache(identityCacheKey);
    }

    private void addToCache(IdentityCacheKey identityCacheKey, Object value){
        if (identityCache == null){
            identityCache = new IdentityCache();
        }
        identityCache.addToCache(identityCacheKey,value);
    }

    private String getPublicKeyFromDevice(Device device){
        return device.getPublicKey();
    }

    private String getPublicKey(String deviceID, String userID)
            throws PushDeviceHandlerServerException,
            IllegalArgumentException, PushDeviceHandlerClientException {
        return getPublicKeyFromDevice(getRegisteredDevice(deviceID,userID));
    }

    private String getUserIdFromUsername(String username, UserRealm realm) throws UserStoreException {

        AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) realm.getUserStoreManager();
        return userStoreManager.getUserIDFromUserName(username);
    }


    private UserRealm getUserRealm(AuthenticatedUser authenticatedUser) throws AuthenticationFailedException {

        UserRealm userRealm = null;
        try {
            if (authenticatedUser != null) {
                String tenantDomain = authenticatedUser.getTenantDomain();
                int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                RealmService realmService = IdentityExtensionsDataHolder.getInstance().getRealmService();
                userRealm = realmService.getTenantUserRealm(tenantId);
            }
        } catch (UserStoreException e) {
            throw new AuthenticationFailedException("Error occurred when trying to get the user realm for user: "
                    + authenticatedUser.toFullQualifiedUsername() + ".", e);
        }
        return userRealm;
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
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {
        throw new AuthenticationFailedException("Mandatory parameter secret null or empty in request.");

    }

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
    protected AuthenticatedUser getAuthenticatedUser(HttpServletRequest request) {
        String secretJWT = request.getParameter(App2AppAuthenticatorConstants.SECRET);
        String loginHint;
        try {
            loginHint = App2AppAuthUtil.getClaim(secretJWT,App2AppAuthenticatorConstants.LOGIN_HINT);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        if (!StringUtils.isBlank(loginHint)){
            return AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(loginHint);
        }else{
            throw new IllegalArgumentException("Required Parameter Login Hint is null or empty");
        }

    }
    protected Optional<String> getAdditionalInfo(HttpServletRequest request, HttpServletResponse response,
                                                 java.lang.String sessionDataKey) throws AuthenticationFailedException {
        return Optional.empty();
    }


}
