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

package com.wso2.openbanking.accelerator.data.publisher.common;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.data.publisher.common.internal.OBAnalyticsDataHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test for Open Banking thrift data publisher.
 */
public class OBThriftDataPublisherTest {

    public static final Map<Integer, String> ATTRIBUTE_MAP = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>(2, "SampleStringAttribute"),
            new AbstractMap.SimpleImmutableEntry<>(3, "SampleIntAttribute"),
            new AbstractMap.SimpleImmutableEntry<>(1, "SampleBooleanAttribute"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    public static final Map<Integer, String> ATTRIBUTE_MAP2 = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>(2, "SampleFloatAttribute"),
            new AbstractMap.SimpleImmutableEntry<>(3, "SampleLongAttribute"),
            new AbstractMap.SimpleImmutableEntry<>(1, "SampleDoubleAttribute"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Map<String, Map<Integer, String>> STREAM_ATTRIBUTE_MAP = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>("testStream", ATTRIBUTE_MAP),
            new AbstractMap.SimpleImmutableEntry<>("testStream2", ATTRIBUTE_MAP2))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    OBThriftDataPublisher thriftDataPublisher;
    private static ByteArrayOutputStream outContent;
    private static Logger logger = null;
    private static PrintStream printStream;
    private float floatNum = 4f;
    private long longNum = 2L;
    private double doubleNum = 5.5;

    @BeforeClass
    public void initializeConfigurations() {

        OpenBankingConfigurationService openBankingConfigurationService =
                Mockito.mock(OpenBankingConfigurationService.class);
        Mockito.when(openBankingConfigurationService.getDataPublishingStreams()).thenReturn(STREAM_ATTRIBUTE_MAP);
        Map<String, Object> configs = new HashMap<>();
        configs.put("DataPublishing.Username", "admin");
        configs.put("DataPublishing.Password", "admin");
        configs.put("DataPublishing.ServerURL", "{tcp://localhost:7612}");
        configs.put("DataPublishing.Thrift.PublishingTimeout", "2000");
        Mockito.when(openBankingConfigurationService.getConfigurations()).thenReturn(configs);
        OBAnalyticsDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
        OBThriftDataPublisherTest.outContent = new ByteArrayOutputStream();
        OBThriftDataPublisherTest.printStream = new PrintStream(OBThriftDataPublisherTest.outContent);
        System.setOut(OBThriftDataPublisherTest.printStream);
        OBThriftDataPublisherTest.logger = LogManager.getLogger(OBThriftDataPublisherTest.class);
    }

    @Test
    public void init() {

        OBThriftDataPublisher thriftDataPublisher = new MockedOBThriftDataPublisher();
        DataPublisher dataPublisher = Mockito.mock(DataPublisher.class);
        thriftDataPublisher.setDataPublisher(dataPublisher);
        thriftDataPublisher.init();
    }

    @Test
    public void tryInitWithoutRequiredConfigs()
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, DataEndpointException,
            DataEndpointConfigurationException, TransportException {
        outContent.reset();
        OpenBankingConfigurationService openBankingConfigurationService =
                Mockito.mock(OpenBankingConfigurationService.class);
        Mockito.when(openBankingConfigurationService.getDataPublishingStreams()).thenReturn(STREAM_ATTRIBUTE_MAP);
        Map<String, Object> configs = new HashMap<>();
        configs.put("DataPublishing.Thrift.PublishingTimeout", "2000");
        Mockito.when(openBankingConfigurationService.getConfigurations()).thenReturn(configs);
        OBAnalyticsDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
        OBThriftDataPublisher thriftDataPublisher = Mockito.spy(OBThriftDataPublisher.class);
        Mockito.doReturn(Mockito.mock(DataPublisher.class)).when(thriftDataPublisher)
                .getDataPublisher(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        thriftDataPublisher.init();
        Assert.assertTrue(OBThriftDataPublisherTest.outContent.toString().contains("ERROR : Error while retrieving " +
                "publisher server configs"));
    }

    @Test(priority = 1)
    public void testStreamAttributeMapCreation() {

        thriftDataPublisher = new MockedOBThriftDataPublisher();
        thriftDataPublisher.buildStreamAttributeMap();

        List<String> attributes = new ArrayList<>();
        attributes.add("SampleBooleanAttribute");
        attributes.add("SampleStringAttribute");
        attributes.add("SampleIntAttribute");
        List<String> attributesSet2 = new ArrayList<>();
        attributesSet2.add("SampleDoubleAttribute");
        attributesSet2.add("SampleFloatAttribute");
        attributesSet2.add("SampleLongAttribute");
        Map<String, List<String>> expectedMap = new HashMap<>();
        expectedMap.put("testStream", attributes);
        expectedMap.put("testStream2", attributesSet2);
        Assert.assertEquals(thriftDataPublisher.getStreamAttributeMap(), expectedMap);

    }

    @Test(priority = 2)
    public void setPayload() {

        String streamName = "testStream";
        Map<String, Object> data = new HashMap<>();
        data.put("SampleStringAttribute", "StringValue1");
        data.put("SampleIntAttribute", 2);
        data.put("SampleBooleanAttribute", true);

        Object[] result = thriftDataPublisher.setPayload(streamName, data);
        Object[] expectedOutput = new Object[]{
                true, "StringValue1", 2
        };

        Assert.assertEquals(result, expectedOutput);
    }

    @Test(priority = 2)
    public void setPayloadWithoutRequiredAttributes() {

        outContent.reset();
        String streamName = "testStream";
        Map<String, Object> data = new HashMap<>();
        data.put("SampleStringAttribute", "StringValue1");
        data.put("SampleIntAttribute", 2);

        Object[] result = thriftDataPublisher.setPayload(streamName, data);
        Object[] expectedOutput = new Object[]{};

        Assert.assertEquals(result, expectedOutput);
        Assert.assertTrue(OBThriftDataPublisherTest.outContent.toString().contains("is missing in data map for "));
    }

    @Test(priority = 2)
    public void setPayloadWithoutRequiredAttributes2() {

        outContent.reset();
        String streamName = "testStream";
        Map<String, Object> data = new HashMap<>();
        data.put("SampleStringAttribute", "StringValue1");
        data.put("SampleIntAttribute", 2);
        data.put("SampleBooleanAttribute", null);

        Object[] result = thriftDataPublisher.setPayload(streamName, data);
        Object[] expectedOutput = new Object[]{};

        Assert.assertEquals(result, expectedOutput);
        Assert.assertTrue(OBThriftDataPublisherTest.outContent.toString().contains("is missing in data map for "));
    }

    @Test(priority = 2)
    public void setPayloadWithInvalidBooleanData() {

        outContent.reset();
        String streamName = "testStream";
        Map<String, Object> data = new HashMap<>();
        data.put("SampleStringAttribute", "StringValue1");
        data.put("SampleIntAttribute", 2);
        data.put("SampleBooleanAttribute", "true");

        Object[] result = thriftDataPublisher.setPayload(streamName, data);
        Object[] expectedOutput = new Object[]{};

        Assert.assertEquals(result, expectedOutput);
        Assert.assertTrue(OBThriftDataPublisherTest.outContent.toString().contains(" is expecting a " +
                Boolean.class.getName() + " type attribute while attribute of type "));
    }

    @Test(priority = 2)
    public void setPayloadWithInvalidIntegerData() {

        outContent.reset();
        String streamName = "testStream";
        Map<String, Object> data = new HashMap<>();
        data.put("SampleStringAttribute", "StringValue1");
        data.put("SampleIntAttribute", 2.14);
        data.put("SampleBooleanAttribute", true);

        Object[] result = thriftDataPublisher.setPayload(streamName, data);
        Object[] expectedOutput = new Object[]{};

        Assert.assertEquals(result, expectedOutput);
        Assert.assertTrue(OBThriftDataPublisherTest.outContent.toString().contains(" is expecting a " +
                Integer.class.getName() + " type attribute while attribute of type "));
    }

    @Test(priority = 2)
    public void setPayloadWithInvalidStringData() {

        outContent.reset();
        String streamName = "testStream";
        Map<String, Object> data = new HashMap<>();
        data.put("SampleStringAttribute", 1);
        data.put("SampleIntAttribute", 2);
        data.put("SampleBooleanAttribute", true);

        Object[] result = thriftDataPublisher.setPayload(streamName, data);
        Object[] expectedOutput = new Object[]{};

        Assert.assertEquals(result, expectedOutput);
        Assert.assertTrue(OBThriftDataPublisherTest.outContent.toString().contains(" is expecting a " +
                String.class.getName() + " type attribute while attribute of type "));
    }

    @Test(priority = 2)
    public void setPayloadWithInvalidFloatData() {

        outContent.reset();
        String streamName = "testStream2";
        Map<String, Object> data = new HashMap<>();
        data.put("SampleFloatAttribute", 4);
        data.put("SampleLongAttribute", longNum);
        data.put("SampleDoubleAttribute", doubleNum);


        Object[] result = thriftDataPublisher.setPayload(streamName, data);
        Object[] expectedOutput = new Object[]{};

        Assert.assertEquals(result, expectedOutput);
        Assert.assertTrue(OBThriftDataPublisherTest.outContent.toString().contains(" is expecting a " +
                Float.class.getName() + " type attribute while attribute of type "));
    }

    @Test(priority = 2)
    public void setPayloadWithInvalidLongData() {

        outContent.reset();
        String streamName = "testStream2";
        Map<String, Object> data = new HashMap<>();
        data.put("SampleFloatAttribute", floatNum);
        data.put("SampleLongAttribute", 2.2);
        data.put("SampleDoubleAttribute", doubleNum);

        Object[] result = thriftDataPublisher.setPayload(streamName, data);
        Object[] expectedOutput = new Object[]{};

        Assert.assertEquals(result, expectedOutput);
        Assert.assertTrue(OBThriftDataPublisherTest.outContent.toString().contains(" is expecting a " +
                Long.class.getName() + " type attribute while attribute of type "));
    }

    @Test(priority = 2)
    public void setPayloadWithInvalidDoubleData() {

        outContent.reset();
        String streamName = "testStream2";
        Map<String, Object> data = new HashMap<>();
        data.put("SampleFloatAttribute", floatNum);
        data.put("SampleLongAttribute", longNum);
        data.put("SampleDoubleAttribute", 5);

        Object[] result = thriftDataPublisher.setPayload(streamName, data);
        Object[] expectedOutput = new Object[]{};

        Assert.assertEquals(result, expectedOutput);
        Assert.assertTrue(OBThriftDataPublisherTest.outContent.toString().contains(" is expecting a " +
                Double.class.getName() + " type attribute while attribute of type "));
    }

    @Test
    public void publish() {

        outContent.reset();
        OBThriftDataPublisher thriftDataPublisher = new MockedOBThriftDataPublisher();
        DataPublisher dataPublisher = Mockito.mock(DataPublisher.class);
        thriftDataPublisher.setDataPublisher(dataPublisher);
        Mockito.doReturn(true).when(dataPublisher).tryPublish(Mockito.any(), Mockito.anyLong());
        Map<String, Object> data = new HashMap<>();
        data.put("SampleStringAttribute", "StringValue1");
        data.put("SampleIntAttribute", 2);
        data.put("SampleBooleanAttribute", true);
        thriftDataPublisher.publish("testStream", "1.0", data);
        Assert.assertFalse(OBThriftDataPublisherTest.outContent.toString().contains("ERROR"));
    }

    @Test
    public void tryPublishWhenAttributesNotDefined() {

        outContent.reset();
        OpenBankingConfigurationService openBankingConfigurationService =
                Mockito.mock(OpenBankingConfigurationService.class);
        Mockito.when(openBankingConfigurationService.getDataPublishingStreams()).thenReturn(STREAM_ATTRIBUTE_MAP);
        Map<String, Object> configs = new HashMap<>();
        configs.put("DataPublishing.Username", "admin");
        configs.put("DataPublishing.Password", "admin");
        configs.put("DataPublishing.ServerURL", "{tcp://localhost:7612}");
        configs.put("DataPublishing.Thrift.PublishingTimeout", "2000");
        Mockito.when(openBankingConfigurationService.getConfigurations()).thenReturn(configs);
        OBAnalyticsDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
        OBThriftDataPublisher thriftDataPublisher = new MockedOBThriftDataPublisher();
        DataPublisher dataPublisher = Mockito.mock(DataPublisher.class);
        thriftDataPublisher.setDataPublisher(dataPublisher);
        Mockito.doReturn(true).when(dataPublisher).tryPublish(Mockito.any(), Mockito.anyLong());
        Map<String, Object> data = new HashMap<>();
        thriftDataPublisher.publish("testStream2", "1.0", data);
        Assert.assertTrue(OBThriftDataPublisherTest.outContent.toString().contains("ERROR : Error while setting " +
                "payload to publish data."));
    }

    private class MockedOBThriftDataPublisher extends OBThriftDataPublisher {

        private Map<String, Map<String, Object>> validationMap;

        public MockedOBThriftDataPublisher() {
            validationMap = new HashMap<>();
            Map<String, Object> metadata1 = new HashMap<>();
            Map<String, Object> metadata2 = new HashMap<>();
            Map<String, Object> metadata3 = new HashMap<>();
            Map<String, Object> metadata4 = new HashMap<>();
            Map<String, Object> metadata5 = new HashMap<>();
            Map<String, Object> metadata6 = new HashMap<>();
            metadata1.put("required", true);
            metadata1.put("type", "int");
            validationMap.put("testStream_SampleIntAttribute", metadata1);
            metadata2.put("required", true);
            metadata2.put("type", "boolean");
            validationMap.put("testStream_SampleBooleanAttribute", metadata2);
            metadata3.put("required", false);
            metadata3.put("type", "string");
            validationMap.put("testStream_SampleStringAttribute", metadata3);
            metadata4.put("required", true);
            metadata4.put("type", "float");
            validationMap.put("testStream2_SampleFloatAttribute", metadata4);
            metadata5.put("required", true);
            metadata5.put("type", "long");
            validationMap.put("testStream2_SampleLongAttribute", metadata5);
            metadata6.put("required", false);
            metadata6.put("type", "double");
            validationMap.put("testStream2_SampleDoubleAttribute", metadata6);
        }

        @Override
        protected DataPublisher getDataPublisher(String serverURL, String authURLSet, String serverUser,
                                                 String serverPassword) {

            return Mockito.mock(DataPublisher.class);
        }

        @Override
        protected Map<String, Map<String, Object>> getAttributeValidationMap() {

            return validationMap;
        }
    }
}
