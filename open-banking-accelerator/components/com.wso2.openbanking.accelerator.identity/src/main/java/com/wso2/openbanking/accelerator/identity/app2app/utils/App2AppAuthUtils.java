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

import com.wso2.openbanking.accelerator.common.validator.OpenBankingValidator;
import com.wso2.openbanking.accelerator.identity.app2app.exception.JWTValidationException;
import com.wso2.openbanking.accelerator.identity.app2app.model.AppAuthValidationJWT;
import com.wso2.openbanking.accelerator.identity.app2app.validations.validationgroups.App2AppValidationOrder;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerClientException;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerServerException;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.impl.DeviceHandlerImpl;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.model.Device;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;

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
     * @param realm the user realm from which to retrieve the user ID
     * @return the user ID associated with the username
     * @throws UserStoreException if an error occurs while retrieving the user ID
     */
    public static String getUserIdFromUsername(String username, UserRealm realm) throws UserStoreException  {

        AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) realm.getUserStoreManager();
        return userStoreManager.getUserIDFromUserName(username);

    }

    /**
     * Retrieves the registered device associated with the specified device ID and user ID.
     *
     * @param deviceID the ID of the device to retrieve
     * @param userID the ID of the user who owns the device
     * @return the registered device associated with the specified IDs
     * @throws PushDeviceHandlerServerException if an error occurs on the server side while handling the device
     * @throws IllegalArgumentException if the provided device identifier does not exist
     * @throws PushDeviceHandlerClientException if an error occurs on the client side while handling the device
     */
    public static Device getRegisteredDevice(String deviceID, String userID) throws PushDeviceHandlerServerException,
            IllegalArgumentException, PushDeviceHandlerClientException {

        DeviceHandlerImpl deviceHandler = new DeviceHandlerImpl();
        //Retrieving all the devices registered under userID
        List<Device> deviceList = deviceHandler.listDevices(userID);

        //Iterating and matching the deviceID with specified deviceID
        for (Device device : deviceList) {
            //If matches return the device
            if (StringUtils.equals(device.getDeviceId(), deviceID)) {
                String publicKey = deviceHandler.getPublicKey(deviceID);
                device.setPublicKey(publicKey);
                return device;
            }
        }

        //If no device registered for user matches specified deviceID throw exception
        throw new IllegalArgumentException("Provided device Identifier does not exist.");

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
    public static String getPublicKey(String deviceID, String userID) throws PushDeviceHandlerServerException,
            IllegalArgumentException, PushDeviceHandlerClientException {

        return getPublicKeyFromDevice(getRegisteredDevice(deviceID, userID));

    }

    /**
     * Validator util to validate AppAuthValidationJWT model for given validationOrder.
     *
     * @param appAuthValidationJWT AppAuthValidationJWT object that needs to be validated
     * @throws JWTValidationException if validation f
     */
    public static void validateSecret(AppAuthValidationJWT appAuthValidationJWT) throws JWTValidationException {
        /*
            App2AppValidationOrder validation order
                1.Required Params validation
                2.Validity Validations - Signature, JTI, Timeliness will be validated.
         */
        String error = OpenBankingValidator.getInstance().getFirstViolation(appAuthValidationJWT, App2AppValidationOrder.class);

        //if there is a validation violation convert it to JWTValidationException
        if (error != null) {
            throw new JWTValidationException(error);
        }

    }
}

