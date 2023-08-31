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

package com.wso2.openbanking.accelerator.event.notifications.service.handler;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.response.EventCreationResponse;
import com.wso2.openbanking.accelerator.event.notifications.service.service.EventCreationService;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import com.wso2.openbanking.accelerator.event.notifications.service.utils.EventNotificationTestUtils;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
/**
 * Test class for DefaultEventCreationServiceHandler.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({EventNotificationServiceUtil.class, ConsentCoreServiceImpl.class})
public class DefaultEventCreationServiceHandlerTest extends PowerMockTestCase {

    @Mock
    ConsentCoreServiceImpl consentCoreServiceImpl;

    @Before
    public void setUp() throws SQLException, ConsentManagementException {

        ConsentResource consentResource = new ConsentResource();
        when(consentCoreServiceImpl.getConsent(anyString(), false)).thenReturn(consentResource);

    }

    DefaultEventCreationServiceHandler defaultEventCreationServiceHandler = new DefaultEventCreationServiceHandler();

    @Test
    public void testPublishOBEvents() throws Exception {

        EventCreationService eventCreationService = Mockito.mock(EventCreationService.class);
        Mockito.when(eventCreationService.publishOBEventNotification(Mockito.anyObject())).
                thenReturn(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

        defaultEventCreationServiceHandler.setEventCreationService(eventCreationService);
        ConsentResource consentResource = new ConsentResource();
        consentResource.setConsentID("0ba972a9-08cd-4cad-b7e2-20655bcbd9e0");
        when(consentCoreServiceImpl.getConsent(anyString(), anyBoolean())).thenReturn(consentResource);

        mockStatic(EventNotificationServiceUtil.class);
        PowerMockito.doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());
        PowerMockito.when(EventNotificationServiceUtil.getConsentCoreServiceImpl()).thenReturn(consentCoreServiceImpl);

        EventCreationResponse eventCreationResponse =
                defaultEventCreationServiceHandler.publishOBEvent(
                        EventNotificationTestUtils.getNotificationCreationDTO());

        Assert.assertEquals(eventCreationResponse.getStatus(), EventNotificationConstants.CREATED);
        Assert.assertEquals(eventCreationResponse.getResponseBody().get(EventNotificationConstants.NOTIFICATIONS_ID),
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

    }

    @Test
    public void testPublishOBEventConsentException() throws Exception {

        EventCreationService eventCreationService = Mockito.mock(EventCreationService.class);
        Mockito.when(eventCreationService.publishOBEventNotification(Mockito.anyObject())).
                thenReturn(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

        defaultEventCreationServiceHandler.setEventCreationService(eventCreationService);
        when(consentCoreServiceImpl.getConsent(anyString(), anyBoolean())).thenThrow(new ConsentManagementException(
                "Consent resource doesn't exist"));

        mockStatic(EventNotificationServiceUtil.class);
        PowerMockito.doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());
        PowerMockito.when(EventNotificationServiceUtil.getConsentCoreServiceImpl()).thenReturn(consentCoreServiceImpl);

        EventCreationResponse eventCreationResponse =
                defaultEventCreationServiceHandler.publishOBEvent(EventNotificationTestUtils.
                        getNotificationCreationDTO());

        Assert.assertEquals(eventCreationResponse.getStatus(), EventNotificationConstants.BAD_REQUEST);
    }

    @Test
    public void testPublishOBEventInvalidClient() throws Exception {

        EventCreationService eventCreationService = Mockito.mock(EventCreationService.class);
        Mockito.when(eventCreationService.publishOBEventNotification(Mockito.anyObject())).
                thenReturn(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

        defaultEventCreationServiceHandler.setEventCreationService(eventCreationService);
        ConsentResource consentResource = new ConsentResource();
        consentResource.setConsentID("0ba972a9-08cd-4cad-b7e2-20655bcbd9e0");
        when(consentCoreServiceImpl.getConsent(anyString(), anyBoolean())).thenReturn(consentResource);

        PowerMockito.mockStatic(EventNotificationServiceUtil.class);
        PowerMockito.doThrow(new OBEventNotificationException("Invalid client ID")).
                when(EventNotificationServiceUtil.class);
        EventNotificationServiceUtil.validateClientId(anyString());
        PowerMockito.when(EventNotificationServiceUtil.getConsentCoreServiceImpl()).thenReturn(consentCoreServiceImpl);

        EventCreationResponse eventCreationResponse =
                defaultEventCreationServiceHandler.publishOBEvent(
                        EventNotificationTestUtils.getNotificationCreationDTO());

        Assert.assertEquals(eventCreationResponse.getStatus(), EventNotificationConstants.BAD_REQUEST);
    }

    @Test
    public void testPublishOBEventsServiceException() throws Exception {

        EventCreationService eventCreationService = Mockito.mock(EventCreationService.class);
        Mockito.when(eventCreationService.publishOBEventNotification(Mockito.anyObject())).thenThrow(new
                        OBEventNotificationException("Error when persisting events"));

        defaultEventCreationServiceHandler.setEventCreationService(eventCreationService);
        ConsentResource consentResource = new ConsentResource();
        consentResource.setConsentID("0ba972a9-08cd-4cad-b7e2-20655bcbd9e0");
        when(consentCoreServiceImpl.getConsent(anyString(), anyBoolean())).thenReturn(consentResource);

        mockStatic(EventNotificationServiceUtil.class);
        PowerMockito.doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());
        PowerMockito.when(EventNotificationServiceUtil.getConsentCoreServiceImpl()).thenReturn(consentCoreServiceImpl);

        EventCreationResponse eventCreationResponse =
                defaultEventCreationServiceHandler.publishOBEvent(
                        EventNotificationTestUtils.getNotificationCreationDTO());

        Assert.assertEquals(eventCreationResponse.getStatus(), EventNotificationConstants.BAD_REQUEST);
    }
}
