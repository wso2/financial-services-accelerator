/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.identity.app2app.utils;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.validator.OpenBankingValidator;
import com.wso2.openbanking.accelerator.identity.app2app.exception.JWTValidationException;
import com.wso2.openbanking.accelerator.identity.app2app.model.AppAuthValidationJWT;
import com.wso2.openbanking.accelerator.identity.app2app.validations.validationorder.App2AppValidationOrder;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.DeviceHandler;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerClientException;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerServerException;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.model.Device;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;
import java.util.Optional;

/**
 * Utils class for Authentication related logic implementations.
 */
public class App2AppAuthUtils {

    /**
     * Retrieves an authenticated user object based on the provided subject identifier.
     *
     * @param subjectIdentifier the subject identifier used to retrieve the authenticated user
     * @return an AuthenticatedUser object representing the authenticated user
     */
    public static AuthenticatedUser getAuthenticatedUserFromSubjectIdentifier(String subjectIdentifier) {

            return AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(subjectIdentifier);

    }

    /**
     * Retrieves the user realm associated with the provided authenticated user.
     *
     * @param authenticatedUser the authenticated user for whom to retrieve the user realm
     * @return the user realm associated with the authenticated user, or null if the user is not authenticated
     * @throws UserStoreException if an error occurs while retrieving the user realm
     */
    public static UserRealm getUserRealm(AuthenticatedUser authenticatedUser) throws UserStoreException {

        UserRealm userRealm = null;

        if (authenticatedUser != null) {
            String tenantDomain = authenticatedUser.getTenantDomain();
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            RealmService realmService = IdentityExtensionsDataHolder.getInstance().getRealmService();
            userRealm = realmService.getTenantUserRealm(tenantId);
        }

        return userRealm;

    }

    /**
     * Retrieves the user ID associated with the provided username from the specified user realm.
     *
     * @param username the username for which to retrieve the user ID
     * @param userRealm the user realm from which to retrieve the user ID
     * @return the user ID associated with the username
     * @throws UserStoreException if an error occurs while retrieving the user ID
     */
    public static String getUserIdFromUsername(String username, UserRealm userRealm) throws UserStoreException,
            OpenBankingException  {

        if (userRealm != null) {
            AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) userRealm.getUserStoreManager();
            return userStoreManager.getUserIDFromUserName(username);
        } else {
            throw new OpenBankingException("UserRealm service can not be null.");
        }

    }

    /**
     * Retrieves the registered device associated with the specified device ID and user ID.
     * TODO: Optimise this code to retrieve device by did and validate userID.
     * Github issue :{<a href="https://github.com/wso2-extensions/identity-outbound-auth-push/issues/144">...</a>}
     *
     * @param deviceId the ID of the device to retrieve
     * @param userId the ID of the user who owns the device
     * @return the registered device associated with the specified IDs
     * @throws PushDeviceHandlerServerException if an error occurs on the server side while handling the device
     * @throws IllegalArgumentException if the provided device identifier does not exist
     * @throws PushDeviceHandlerClientException if an error occurs on the client side while handling the device
     */
    public static Device getRegisteredDevice(String deviceId, String userId, DeviceHandler deviceHandler)
            throws PushDeviceHandlerServerException, IllegalArgumentException,
            PushDeviceHandlerClientException, OpenBankingException {

        /*
         It is important to verify the device is registered under the given user
         as public key is associated with device not the user.
         */
        List<Device> deviceList = deviceHandler.listDevices(userId);
        //If a device registered under the given user matches the specified deviceId return the device
        Optional<Device> optionalDevice = deviceList.stream()
                .filter(device -> StringUtils.equals(device.getDeviceId(), deviceId))
                .findFirst();
        //If no device found throw exception
        Device device =  optionalDevice.orElseThrow(() ->
                new OpenBankingException("Provided Device ID doesn't match any device registered under user."));
        //If a device is found set the public key
        device.setPublicKey(deviceHandler.getPublicKey(deviceId));
        return device;
    }

    /**
     * Retrieves the public key associated with the specified device.
     *
     * @param device the device from which to retrieve the public key
     * @return the public key associated with the device
     */
    public static String getPublicKeyFromDevice(Device device) {

        return device.getPublicKey();

    }

    /**
     * Retrieve Public key of the device specified if it is registered under specified user.
     *
     * @param deviceID deviceID of the device where the public key is required
     * @param userID userID of the user
     * @return the public key
     * @throws PushDeviceHandlerServerException if an error occurs on the server side while handling the device
     * @throws IllegalArgumentException if the provided device identifier does not exist
     * @throws PushDeviceHandlerClientException if an error occurs on the client side while handling the device
     */
    public static String getPublicKey(String deviceID, String userID, DeviceHandler deviceHandler)
            throws PushDeviceHandlerServerException, IllegalArgumentException, PushDeviceHandlerClientException,
            OpenBankingException {

        return getPublicKeyFromDevice(getRegisteredDevice(deviceID, userID, deviceHandler));

    }

    /**
     * Validator util to validate AppAuthValidationJWT model for given validationOrder.
     *
     * @param appAuthValidationJWT AppAuthValidationJWT object that needs to be validated
     * @throws JWTValidationException if validation failed
     */
    public static void validateSecret(AppAuthValidationJWT appAuthValidationJWT) throws JWTValidationException {
        /*
            App2AppValidationOrder validation order
                1.Required Params validation
                2.Validity Validations - Signature, JTI, Timeliness will be validated.
         */
        String error = OpenBankingValidator.getInstance()
                .getFirstViolation(appAuthValidationJWT, App2AppValidationOrder.class);

        //if there is a validation violation convert it to JWTValidationException
        if (error != null) {
            throw new JWTValidationException(error);
        }
    }

}

