package com.wso2.openbanking.accelerator.identity.app2app;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.app2app.utils.App2AppAuthUtils;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
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

import java.util.ArrayList;
import java.util.List;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

@PrepareForTest({AuthenticatedUser.class, IdentityTenantUtil.class, IdentityExtensionsDataHolder.class})
@PowerMockIgnore({"javax.net.ssl.*", "jdk.internal.reflect.*"})
public class App2AppAuthUtilsTest {

    @Test
    public void testGetAuthenticatedUserFromSubjectIdentifier() {
        mockStatic(AuthenticatedUser.class);
        // Prepare test data
        String subjectIdentifier = "admin@wso2.com";

        // Mock the AuthenticatedUser class
        AuthenticatedUser authenticatedUserMock = Mockito.mock(AuthenticatedUser.class);

        // Mock the behavior of AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier()
        Mockito.when(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(subjectIdentifier))
                .thenReturn(authenticatedUserMock);

        // Call the method under test
        AuthenticatedUser user = App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier(subjectIdentifier);

        // Verify the result
        Assert.assertNotNull(user, "Authenticated user should not be null");
        Assert.assertEquals(user, authenticatedUserMock, "Returned user should match the mocked user");
        // You may add more assertions based on the properties or behavior of AuthenticatedUser
    }

    @Test
    public void testGetUserRealm() throws UserStoreException {
        // Mock the AuthenticatedUser
        AuthenticatedUser authenticatedUserMock = Mockito.mock(AuthenticatedUser.class);
        Mockito.when(authenticatedUserMock.getTenantDomain()).thenReturn("testTenantDomain");

        // Mock IdentityTenantUtil
        mockStatic(IdentityTenantUtil.class);
        Mockito.when(IdentityTenantUtil.getTenantId(Mockito.anyString())).thenReturn(1234);

        // Mock RealmService and UserRealm
        RealmService realmServiceMock = Mockito.mock(RealmService.class);
        UserRealm userRealmMock = Mockito.mock(UserRealm.class);
        Mockito.when(realmServiceMock.getTenantUserRealm(1234)).thenReturn(userRealmMock);

        // Mock IdentityExtensionsDataHolder
        IdentityExtensionsDataHolder dataHolderMock = Mockito.mock(IdentityExtensionsDataHolder.class);
        Mockito.when(dataHolderMock.getRealmService()).thenReturn(realmServiceMock);
        mockStatic(IdentityExtensionsDataHolder.class);
        Mockito.when(IdentityExtensionsDataHolder.getInstance()).thenReturn(dataHolderMock);

        // Call the method under test
        UserRealm userRealm = App2AppAuthUtils.getUserRealm(authenticatedUserMock);

        // Verify the result
        Assert.assertEquals(userRealm, userRealmMock, "UserRealm should match the mocked UserRealm");
    }

    @Test
    public void testGetUserRealmWhenUserIsNull() throws UserStoreException {

        // Call the method under test
        UserRealm userRealm = App2AppAuthUtils.getUserRealm(null);

        // Verify the result
        Assert.assertNull(userRealm, "UserRealm should be null when the input is null.");
    }

    @Test
    public void testGetUserIdFromUsername() throws UserStoreException, OpenBankingException {
        // Prepare test data
        String username = "admin@wso2.com";
        String userIDMock = "354cd9f4-ae85-4ce9-8c42-dc1111ac8acf";
        // Mock the UserRealm
        UserRealm userRealmMock = Mockito.mock(UserRealm.class);

        // Mock the AbstractUserStoreManager
        AbstractUserStoreManager userStoreManagerMock = Mockito.mock(AbstractUserStoreManager.class);
        Mockito.when(userStoreManagerMock.getUserIDFromUserName(username)).thenReturn(userIDMock);

        // Mock the RealmService
        Mockito.when(userRealmMock.getUserStoreManager()).thenReturn(userStoreManagerMock);

        // Call the method under test
        String userId = App2AppAuthUtils.getUserIdFromUsername(username, userRealmMock);

        // Verify the result
        Assert.assertNotNull(userId, "User ID should not be null");
        Assert.assertEquals(userId, userIDMock ,
                "User ID should match the expected value");
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testGetUserIdFromUsernameWhenRealmNull() throws UserStoreException, OpenBankingException {
        // Prepare test data
        String username = "admin@wso2.com";
        // Mock the UserRealm
        UserRealm userRealmMock = null;
        // Call the method under test
        String userId = App2AppAuthUtils.getUserIdFromUsername(username, userRealmMock);

    }

    @Test
    public void testGetPublicKey() throws PushDeviceHandlerServerException, PushDeviceHandlerClientException, OpenBankingException {


        // Prepare test data
        String deviceID = "testDeviceID";
        String invalidDeviceId ="invalidDeviceID";
        String userID = "testUserID";
        String publicKey = "testPublicKey";

        // Mock DeviceHandlerImpl and Device
        DeviceHandlerImpl deviceHandlerMock = Mockito.mock(DeviceHandlerImpl.class);

        Device deviceMockI = Mockito.mock(Device.class);
        Device deviceMockII = Mockito.mock(Device.class);
        Mockito.when(deviceMockI.getPublicKey()).thenReturn(publicKey);
        Mockito.when(deviceMockI.getDeviceId()).thenReturn(deviceID);
        Mockito.when(deviceMockII.getPublicKey()).thenReturn(publicKey);
        Mockito.when(deviceMockII.getDeviceId()).thenReturn(invalidDeviceId);


        // Mock DeviceHandlerImpl.listDevices() to return a list with the mock device
        List<Device> deviceList = new ArrayList<>();
        deviceList.add(deviceMockI);
        deviceList.add(deviceMockII);
        Mockito.when(deviceHandlerMock.listDevices(userID)).thenReturn(deviceList);
        Mockito.when(deviceHandlerMock.getPublicKey(userID)).thenReturn(publicKey);

        // Call the method under test
        String result = App2AppAuthUtils.getPublicKey(deviceID, userID, deviceHandlerMock);

        // Verify the result
        Assert.assertEquals(result, publicKey, "Public key should match");

    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testGetPublicKeyInvalidDeviceID() throws PushDeviceHandlerServerException, PushDeviceHandlerClientException, OpenBankingException {

        spy(App2AppAuthUtils.class);

        // Prepare test data
        String deviceID = "testDeviceID";
        String invalidDeviceId ="invalidDeviceID";
        String userID = "testUserID";
        String publicKey = "testPublicKey";

        // Mock DeviceHandlerImpl and Device
        DeviceHandlerImpl deviceHandlerMock = Mockito.mock(DeviceHandlerImpl.class);


        Device deviceMock = Mockito.mock(Device.class);
        Mockito.when(deviceMock.getPublicKey()).thenReturn(publicKey);
        Mockito.when(deviceMock.getDeviceId()).thenReturn(invalidDeviceId);


        // Mock DeviceHandlerImpl.listDevices() to return a list with the mock device
        List<Device> deviceList = new ArrayList<>();
        deviceList.add(deviceMock);
        Mockito.when(deviceHandlerMock.listDevices(userID)).thenReturn(deviceList);
        Mockito.when(deviceHandlerMock.getPublicKey(userID)).thenReturn(publicKey);

        // Call the method under test
        String result = App2AppAuthUtils.getPublicKey(deviceID, userID, deviceHandlerMock);

    }



    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}
