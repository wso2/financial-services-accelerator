package org.wso2.financial.services.accelerator.data.publisher.util;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.data.publisher.DataPublisherPool;
import org.wso2.financial.services.accelerator.data.publisher.EventQueue;
import org.wso2.financial.services.accelerator.data.publisher.FinancialServicesDataPublisher;
import org.wso2.financial.services.accelerator.data.publisher.constants.DataPublishingConstants;
import org.wso2.financial.services.accelerator.data.publisher.internal.FSAnalyticsDataHolder;
import org.wso2.financial.services.accelerator.data.publisher.model.FSAnalyticsEvent;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FSDataPublisherUtilTest {

    private MockedStatic<FSAnalyticsDataHolder> dataHolderStatic;
    //private MockedStatic<FinancialServicesConfigParser> configParserStatic;

    private FSAnalyticsDataHolder dataHolderMock;
    private DataPublisherPool<FinancialServicesDataPublisher> poolMock;
    private FinancialServicesDataPublisher publisherMock;
    private EventQueue eventQueueMock;
    private FinancialServicesConfigParser configParserMock;

    @BeforeMethod
    public void setUp() {
        dataHolderMock = mock(FSAnalyticsDataHolder.class);
        poolMock = mock(DataPublisherPool.class);
        publisherMock = mock(FinancialServicesDataPublisher.class);
        eventQueueMock = mock(EventQueue.class);
        configParserMock = mock(FinancialServicesConfigParser.class);

        dataHolderStatic = Mockito.mockStatic(FSAnalyticsDataHolder.class);
        dataHolderStatic.when(FSAnalyticsDataHolder::getInstance).thenReturn(dataHolderMock);

        //configParserStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);
        //configParserStatic.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);
    }

    @AfterMethod
    public void tearDown() {
        dataHolderStatic.close();
       // configParserStatic.close();
    }

    @Test
    public void testGetDataPublisherInstanceSuccess() throws Exception {
        when(dataHolderMock.getDataPublisherPool()).thenReturn(poolMock);
        when(poolMock.borrowObject()).thenReturn(publisherMock);

        FinancialServicesDataPublisher result = FSDataPublisherUtil.getDataPublisherInstance();
        assert result == publisherMock;
    }

    @Test
    public void testGetDataPublisherInstanceException() throws Exception {
        when(dataHolderMock.getDataPublisherPool()).thenReturn(poolMock);
        when(poolMock.borrowObject()).thenThrow(new RuntimeException("fail"));

        FinancialServicesDataPublisher result = FSDataPublisherUtil.getDataPublisherInstance();
        assert result == null;
    }

    @Test
    public void testReleaseDataPublishingInstance() {
        when(dataHolderMock.getDataPublisherPool()).thenReturn(poolMock);

        FSDataPublisherUtil.releaseDataPublishingInstance(publisherMock);
        verify(poolMock, times(1)).returnObject(publisherMock);
    }

    @Test
    public void testPublishData_ELKAndPublishingEnabled() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(DataPublishingConstants.DATA_PUBLISHING_ENABLED, "true");
        when(dataHolderMock.getConfigurationMap()).thenReturn(configMap);
        when(dataHolderMock.getEventQueue()).thenReturn(eventQueueMock);

        Map<String, Object> parserConfig = new HashMap<>();
        parserConfig.put(DataPublishingConstants.ELK_ANALYTICS_ENABLED, "true");
        when(configParserMock.getConfiguration()).thenReturn(parserConfig);

        try (MockedStatic<LogsPublisherUtil> logsPublisherUtilStatic = Mockito.mockStatic(LogsPublisherUtil.class)) {
            FSDataPublisherUtil.publishData("stream", "1.0", new HashMap<>());
            logsPublisherUtilStatic.verify(() -> LogsPublisherUtil.addAnalyticsLogs(any(), any(), any(), any()),
                    times(1));
            verify(eventQueueMock, times(1)).put(any(FSAnalyticsEvent.class));
        }
    }

    @Test
    public void testPublishData_PublishingDisabled() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(DataPublishingConstants.DATA_PUBLISHING_ENABLED, "false");
        when(dataHolderMock.getConfigurationMap()).thenReturn(configMap);

        Map<String, Object> parserConfig = new HashMap<>();
        parserConfig.put(DataPublishingConstants.ELK_ANALYTICS_ENABLED, "false");
        when(configParserMock.getConfiguration()).thenReturn(parserConfig);

        FSDataPublisherUtil.publishData("stream", "1.0", new HashMap<>());
        // No eventQueue interaction expected
        verify(dataHolderMock, never()).getEventQueue();
    }

    @Test
    public void testPublishData_EventQueueNull() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(DataPublishingConstants.DATA_PUBLISHING_ENABLED, "true");
        when(dataHolderMock.getConfigurationMap()).thenReturn(configMap);
        when(dataHolderMock.getEventQueue()).thenReturn(null);

        Map<String, Object> parserConfig = new HashMap<>();
        parserConfig.put(DataPublishingConstants.ELK_ANALYTICS_ENABLED, "false");
        when(configParserMock.getConfiguration()).thenReturn(parserConfig);

        FSDataPublisherUtil.publishData("stream", "1.0", new HashMap<>());
        // No exception expected
    }

    @Test
    public void testPublishData_LogsPublisherThrowsException() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(DataPublishingConstants.DATA_PUBLISHING_ENABLED, "true");
        when(dataHolderMock.getConfigurationMap()).thenReturn(configMap);
        when(dataHolderMock.getEventQueue()).thenReturn(eventQueueMock);

        Map<String, Object> parserConfig = new HashMap<>();
        parserConfig.put(DataPublishingConstants.ELK_ANALYTICS_ENABLED, "true");
        when(configParserMock.getConfiguration()).thenReturn(parserConfig);

        try (MockedStatic<LogsPublisherUtil> logsPublisherUtilStatic = Mockito.mockStatic(LogsPublisherUtil.class)) {
            logsPublisherUtilStatic.when(() -> LogsPublisherUtil.addAnalyticsLogs(any(), any(), any(), any()))
                    .thenThrow(new FinancialServicesException("fail"));
            FSDataPublisherUtil.publishData("stream", "1.0", new HashMap<>());
            // Should log error, but not throw
            verify(eventQueueMock, times(1)).put(any(FSAnalyticsEvent.class));
        }
    }
}



