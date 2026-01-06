package org.wso2.financial.services.accelerator.data.publisher.thrift;

import org.apache.synapse.MessageContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThriftAPIMAnalyticsDataProviderTest {
    private ThriftAPIMAnalyticsDataProvider provider;

    @BeforeMethod
    void setUp() {
        provider = new ThriftAPIMAnalyticsDataProvider();
    }

    @Test
    void testGetApplicationConsumerKey() {
        AuthenticationContext authContext = mock(AuthenticationContext.class);
        when(authContext.getConsumerKey()).thenReturn("key123");
        Assert.assertEquals("key123", provider.getApplicationConsumerKey(authContext));
        Assert.assertNull(provider.getApplicationConsumerKey(null));
    }

    @Test
    void testGetApplicationId() {
        AuthenticationContext authContext = mock(AuthenticationContext.class);
        when(authContext.getApplicationId()).thenReturn("appId");
        Assert.assertEquals("appId", provider.getApplicationId(authContext));
        Assert.assertNull(provider.getApplicationId(null));
    }

    @Test
    void testGetUserId() {
        AuthenticationContext authContext = mock(AuthenticationContext.class);
        when(authContext.getUsername()).thenReturn("user1");
        Assert.assertEquals("user1", provider.getUserId(authContext));
        Assert.assertEquals("", provider.getUserId(null));
    }
    @Test
    void testGetConsentId() {
        MessageContext messageContext = mock(MessageContext.class);
        when(messageContext.getProperty("CONSENT_ID")).thenReturn("consent123");
        Assert.assertEquals("consent123", provider.getConsentId(messageContext));
        Assert.assertNull(provider.getConsentId(null));
    }

    @Test
    void testGetCustomPropertiesWithNullContext() {
        Map<String, Object> result = provider.getCustomProperties(null);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }


    // Add more tests for getResponseBodyDetails and getCustomProperties as needed
}
