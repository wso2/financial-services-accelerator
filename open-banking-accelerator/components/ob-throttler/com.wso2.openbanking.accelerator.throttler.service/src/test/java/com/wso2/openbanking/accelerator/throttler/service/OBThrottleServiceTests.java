/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
package com.wso2.openbanking.accelerator.throttler.service;

import com.wso2.openbanking.accelerator.common.exception.OBThrottlerException;
import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import com.wso2.openbanking.accelerator.throttler.dao.OBThrottlerDAO;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataDeletionException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataInsertionException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataRetrievalException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataUpdationException;
import com.wso2.openbanking.accelerator.throttler.dao.model.ThrottleDataModel;
import com.wso2.openbanking.accelerator.throttler.dao.persistence.DataStoreInitializer;
import com.wso2.openbanking.accelerator.throttler.service.util.OBThrottleServiceTestData;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Test for Open banking throttle service.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({DatabaseUtil.class, DataStoreInitializer.class})
public class OBThrottleServiceTests {

    private OBThrottleService obThrottleService;
    private OBThrottlerDAO mockedOBThrottlerDAO;
    private Connection mockedConnection;
    private ThrottleDataModel throttleDataModel;

    @BeforeClass
    public void initTest() {

        obThrottleService = OBThrottleService.getInstance();
        mockedOBThrottlerDAO = Mockito.mock(OBThrottlerDAO.class);
        mockedConnection  = Mockito.mock(Connection.class);
        throttleDataModel = Mockito.mock(ThrottleDataModel.class);
    }

    @BeforeMethod
    public void mock() throws OBThrottlerException {

        mockStaticClasses();
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void testThrottledOutScenario() throws Exception {

        Mockito.doNothing().when(mockedOBThrottlerDAO).deleteThrottleData(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());

        obThrottleService.throttleDataMap.put(OBThrottleServiceTestData.THROTTLE_GROUP,
                new HashMap<String, Timestamp>() {
            {
                put(OBThrottleServiceTestData.THROTTLE_PARAM,
                        OBThrottleServiceTestData.UNLOCK_TIMESTAMP_GREATER_THAN_CURRENT_TIMESTAMP);
            }
        });

        Boolean isThrottled = obThrottleService.isThrottled(OBThrottleServiceTestData.THROTTLE_GROUP,
                OBThrottleServiceTestData.THROTTLE_PARAM);

        Assert.assertTrue(isThrottled);
    }

    @Test
    public void testNotThrottledOutScenario() throws Exception {

        Mockito.doNothing().when(mockedOBThrottlerDAO).deleteThrottleData(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());

        obThrottleService.throttleDataMap.put(OBThrottleServiceTestData.THROTTLE_SECOND_GROUP,
                new HashMap<String, Timestamp>() {
            {
                put(OBThrottleServiceTestData.THROTTLE_PARAM,
                        OBThrottleServiceTestData.UNLOCK_TIMESTAMP_LESS_THAN_CURRENT_TIMESTAMP);
            }
        });

        Boolean isThrottled = obThrottleService.isThrottled(
                OBThrottleServiceTestData.THROTTLE_SECOND_GROUP, OBThrottleServiceTestData.THROTTLE_PARAM);

        Assert.assertFalse(isThrottled);
    }

    @Test(priority = 1)
    public void testThrottleGroupNotInThrottleDataMap() throws Exception {

        Mockito.doNothing().when(mockedOBThrottlerDAO).deleteThrottleData(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());

        Boolean isThrottled = obThrottleService.isThrottled(
                OBThrottleServiceTestData.THROTTLE_GROUP_BASIC_AUTH, OBThrottleServiceTestData.THROTTLE_PARAM);

        Assert.assertFalse(isThrottled);
    }

    @Test(priority = 1)
    public void testThrottleParamNotInThrottleDataMap() throws Exception {

        Mockito.doNothing().when(mockedOBThrottlerDAO).deleteThrottleData(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());

        Boolean isThrottled = obThrottleService.isThrottled(
                OBThrottleServiceTestData.THROTTLE_SECOND_GROUP, OBThrottleServiceTestData.THROTTLE_SECOND_PARAM);

        Assert.assertFalse(isThrottled);
    }

    @Test(priority = 2)
    public void testThrottleGroupInUpdateThrottleDataMap() {

        obThrottleService.updateThrottleDataMap(OBThrottleServiceTestData.THROTTLE_SECOND_GROUP,
                OBThrottleServiceTestData.THROTTLE_PARAM,
                OBThrottleServiceTestData.UNLOCK_TIMESTAMP_GREATER_THAN_CURRENT_TIMESTAMP);

        Assert.assertTrue(obThrottleService.throttleDataMap
                .containsKey(OBThrottleServiceTestData.THROTTLE_SECOND_GROUP));
    }

    @Test(priority = 2)
    public void testThrottleGroupNotInUpdateThrottleDataMap() {

        obThrottleService.updateThrottleDataMap(OBThrottleServiceTestData.THROTTLE_GROUP_BASIC_AUTH,
                OBThrottleServiceTestData.THROTTLE_SECOND_PARAM,
                OBThrottleServiceTestData.UNLOCK_TIMESTAMP_GREATER_THAN_CURRENT_TIMESTAMP);

        Assert.assertTrue(obThrottleService.throttleDataMap
                .containsKey(OBThrottleServiceTestData.THROTTLE_GROUP_BASIC_AUTH));
    }

    @Test
    public void testUpdateThrottleData() throws Exception {

        Mockito.doReturn(true).when(mockedOBThrottlerDAO).isThrottleDataExists(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(OBThrottleServiceTestData.getSampleTestThrottleData()).when(mockedOBThrottlerDAO)
                .getThrottleData(Mockito.anyObject(),
                        Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(OBThrottleServiceTestData.getSampleUpdateTestThrottleData()).when(mockedOBThrottlerDAO)
                .updateThrottleData(Mockito.anyObject(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyObject(),
                        Mockito.anyObject(), Mockito.anyInt());
        Mockito.doReturn(OBThrottleServiceTestData.getSampleUpdateTestThrottleData().getOccurrences())
                .when(throttleDataModel).getOccurrences();

        obThrottleService.updateThrottleData(OBThrottleServiceTestData.THROTTLE_GROUP,
                OBThrottleServiceTestData.THROTTLE_SECOND_PARAM, 3, 180);

    }

    @Test
    public void testStoreThrottleData() throws Exception {

        Mockito.doReturn(false).when(mockedOBThrottlerDAO).isThrottleDataExists(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(OBThrottleServiceTestData.getSampleUpdateTestThrottleData()).when(mockedOBThrottlerDAO)
                .storeThrottleData(Mockito.anyObject(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyObject(),
                        Mockito.anyObject());
        Mockito.doReturn(OBThrottleServiceTestData.getSampleUpdateTestThrottleData().getOccurrences())
                .when(throttleDataModel).getOccurrences();

        obThrottleService.updateThrottleData(OBThrottleServiceTestData.THROTTLE_GROUP,
                OBThrottleServiceTestData.THROTTLE_SECOND_PARAM, 3, 180);

    }

    @Test(expectedExceptions = OBThrottlerException.class)
    public void testStoreThrottleDataError() throws Exception {

        Mockito.doReturn(false).when(mockedOBThrottlerDAO).isThrottleDataExists(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doThrow(OBThrottlerDataInsertionException.class).when(mockedOBThrottlerDAO)
                .storeThrottleData(Mockito.anyObject(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyObject(),
                        Mockito.anyObject());
        Mockito.doReturn(OBThrottleServiceTestData.getSampleUpdateTestThrottleData().getOccurrences())
                .when(throttleDataModel).getOccurrences();

        obThrottleService.updateThrottleData(OBThrottleServiceTestData.THROTTLE_GROUP,
                OBThrottleServiceTestData.THROTTLE_SECOND_PARAM, 3, 180);

    }

    @Test(expectedExceptions = OBThrottlerException.class)
    public void testUpdateThrottleDataError() throws Exception {

        Mockito.doReturn(true).when(mockedOBThrottlerDAO).isThrottleDataExists(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(OBThrottleServiceTestData.getSampleTestThrottleData()).when(mockedOBThrottlerDAO)
                .getThrottleData(Mockito.anyObject(),
                        Mockito.anyString(), Mockito.anyString());
        Mockito.doThrow(OBThrottlerDataUpdationException.class).when(mockedOBThrottlerDAO)
                .updateThrottleData(Mockito.anyObject(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyObject(),
                        Mockito.anyObject(), Mockito.anyInt());
        Mockito.doReturn(OBThrottleServiceTestData.getSampleUpdateTestThrottleData().getOccurrences())
                .when(throttleDataModel).getOccurrences();

        obThrottleService.updateThrottleData(OBThrottleServiceTestData.THROTTLE_GROUP,
                OBThrottleServiceTestData.THROTTLE_SECOND_PARAM, 3, 180);

    }

    @Test(expectedExceptions = OBThrottlerException.class)
    public void testRetrievalThrottleDataError() throws Exception {

        Mockito.doReturn(true).when(mockedOBThrottlerDAO).isThrottleDataExists(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doThrow(OBThrottlerDataRetrievalException.class).when(mockedOBThrottlerDAO)
                .getThrottleData(Mockito.anyObject(),
                        Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(OBThrottleServiceTestData.getSampleUpdateTestThrottleData()).when(mockedOBThrottlerDAO)
                .updateThrottleData(Mockito.anyObject(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyObject(),
                        Mockito.anyObject(), Mockito.anyInt());
        Mockito.doReturn(OBThrottleServiceTestData.getSampleUpdateTestThrottleData().getOccurrences())
                .when(throttleDataModel).getOccurrences();

        obThrottleService.updateThrottleData(OBThrottleServiceTestData.THROTTLE_GROUP,
                OBThrottleServiceTestData.THROTTLE_SECOND_PARAM, 3, 180);

    }

    @Test
    public void testDeleteRecordOnSuccessAttempt() throws Exception {

        Mockito.doReturn(true).when(mockedOBThrottlerDAO).isThrottleDataExists(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());

        obThrottleService.deleteRecordOnSuccessAttempt(OBThrottleServiceTestData.THROTTLE_GROUP,
                OBThrottleServiceTestData.THROTTLE_SECOND_PARAM);

    }

    @Test(expectedExceptions = OBThrottlerException.class)
    public void testRetrievalThrottleDataErrorWhenDeleteRecordOnSuccessAttempt() throws Exception {

        Mockito.doThrow(OBThrottlerDataRetrievalException.class).when(mockedOBThrottlerDAO)
                .isThrottleDataExists(Mockito.anyObject(),
                        Mockito.anyString(), Mockito.anyString());

        obThrottleService.deleteRecordOnSuccessAttempt(OBThrottleServiceTestData.THROTTLE_GROUP,
                OBThrottleServiceTestData.THROTTLE_SECOND_PARAM);

    }

    @Test(expectedExceptions = OBThrottlerException.class)
    public void testDeleteThrottleDataErrorWhenDeleteRecordOnSuccessAttempt() throws Exception {

        Mockito.doReturn(true).when(mockedOBThrottlerDAO).isThrottleDataExists(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doThrow(OBThrottlerDataDeletionException.class).when(mockedOBThrottlerDAO)
                .deleteThrottleData(Mockito.anyObject(),
                        Mockito.anyString(), Mockito.anyString());

        obThrottleService.deleteRecordOnSuccessAttempt(OBThrottleServiceTestData.THROTTLE_GROUP,
                OBThrottleServiceTestData.THROTTLE_SECOND_PARAM);

    }

    private void mockStaticClasses() throws OBThrottlerException {

        PowerMockito.mockStatic(DatabaseUtil.class);
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(Mockito.mock(Connection.class));

        PowerMockito.mockStatic(DataStoreInitializer.class);
        PowerMockito.when(DataStoreInitializer.initializeOBThrottlerDAO()).thenReturn(mockedOBThrottlerDAO);
    }
}
