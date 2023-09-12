package com.wso2.openbanking.accelerator.event.notifications.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for EventNotificationServiceUtil.
 */
public class EventNotificationServiceUtilTests {
    @Test
    public void testGetCustomNotificationPayload() throws Exception {
        JsonNode jsonNode = new ObjectMapper().readTree("{\"key\": \"value\"}");
        String payload = EventNotificationServiceUtil.getCustomNotificationPayload(jsonNode);
        assertNotNull(payload);
        assertEquals("{\"key\":\"value\"}", payload);
    }
}
