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

package com.wso2.openbanking.accelerator.service.activator.internal;

import com.wso2.openbanking.accelerator.service.activator.OBServiceObserver;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * ServiceObservableTest.
 * <p>
 * Tests for ServiceObservable class
 */
public class ServiceObservableTest {

    ServiceObservable uut;

    @BeforeClass
    public void init() {
        uut = ServiceObservable.getInstance();
    }

    @Test
    public void testActivateAllServiceObservers() {
        OBServiceObserver obServiceObserverMock = Mockito.mock(OBServiceObserver.class);
        Mockito.doNothing().when(obServiceObserverMock).activate();
        OBServiceObserver obServiceObserverMock1 = Mockito.mock(OBServiceObserver.class);
        Mockito.doNothing().when(obServiceObserverMock1).activate();

        uut.registerServiceObserver(obServiceObserverMock);
        uut.registerServiceObserver(obServiceObserverMock1);
        uut.activateAllServiceObservers();

        Mockito.verify(obServiceObserverMock, Mockito.times(1)).activate();
        Mockito.verify(obServiceObserverMock1, Mockito.times(1)).activate();
    }
}
