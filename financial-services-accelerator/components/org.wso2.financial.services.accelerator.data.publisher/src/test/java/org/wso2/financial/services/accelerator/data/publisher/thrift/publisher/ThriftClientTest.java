package org.wso2.financial.services.accelerator.data.publisher.thrift.publisher;


import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;

import java.util.HashMap;
import java.util.Map;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ThriftClientTest {
    @Mock
    private DataPublisher mockDataPublisher;
    @Mock
    private ThriftMetricEventBuilder mockBuilder;
    @Mock
    private ThriftStream mockStream;
    @Mock
    private Logger mockLogger;

    private ThriftClient client;

    @BeforeMethod
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        client = new ThriftClient("type", "serverURL", "authURL", "user",
                "pass") {

            protected DataPublisher createDataPublisher(String type, String serverURL, String authURL,
                                                        String serverUser, String serverPassword) {
                return mockDataPublisher;
            }
        };
    }

    @Test
    void testPublishSuccess() throws Exception {
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("key", "value");
        when(mockBuilder.build()).thenReturn(eventMap);
        when(mockBuilder.getThriftStream()).thenReturn(ThriftStream.REQUEST_STREAM);
        when(mockDataPublisher.tryPublish(any(Event.class))).thenReturn(true);

        client.publish(mockBuilder);

        verify(mockDataPublisher, times(1)).tryPublish(any(Event.class));
    }

    @Test
    void testPublishQueueFullLogsError() throws Exception {
        Map<String, Object> eventMap = new HashMap<>();
        when(mockBuilder.build()).thenReturn(eventMap);
        when(mockBuilder.getThriftStream()).thenReturn(ThriftStream.REQUEST_STREAM);
        when(mockDataPublisher.tryPublish(any(Event.class))).thenReturn(false);

        client.publish(mockBuilder);

        verify(mockDataPublisher, times(1)).tryPublish(any(Event.class));
        // Error is logged, but not thrown
    }

    @Test
    void testPublishHandlesException() throws Exception {
        try {
            Map<String, Object> eventMap = new HashMap<>();
            when(mockBuilder.build()).thenReturn(eventMap);
            when(mockBuilder.getThriftStream()).thenReturn(ThriftStream.REQUEST_STREAM);
            when(mockDataPublisher.tryPublish(any(Event.class))).thenThrow(new RuntimeException("fail"));
            client.publish(mockBuilder);
        } catch (Exception e) {
            Assert.fail("Exception was thrown: " + e.getMessage());
        }


    }
}
