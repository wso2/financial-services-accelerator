package org.wso2.financial.services.accelerator.consent.mgt.extensions.event.executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.event.executor.model.FSEvent;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.data.publisher.util.FSDataPublisherUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


public class ConsentLCEventExecutorTests {

    public static final String USER_ID_PRIMARY = "test-primary-user-id";
    public static final String AUTH_ID_PRIMARY = "test-primary-auth-id";
    private static final String CONSENT_ID_KEY = "consentId";
    private static final String CONSENT_DETAILS_KEY = "consentDetails";
    private static final String CURRENT_STATUS_KEY = "currentStatus";
    private static final String PREVIOUS_STATUS_KEY = "previousStatus";
    public static final String AUTH_RESOURCE_TYPE_PRIMARY = "primary_member";


    private static ByteArrayOutputStream outContent;
    private static Logger logger = null;
    private static PrintStream printStream;
    private AuthorizationResource authResource;

    @BeforeClass
    public void beforeTests() throws ConsentManagementException {

        outContent = new ByteArrayOutputStream();
        printStream = new PrintStream(outContent);
        System.setOut(printStream);
        logger = LogManager.getLogger(ConsentLCEventExecutorTests.class);

        authResource = new AuthorizationResource();
        authResource.setAuthorizationID(AUTH_ID_PRIMARY);
        authResource.setUserID(USER_ID_PRIMARY);
        authResource.setAuthorizationType(AUTH_RESOURCE_TYPE_PRIMARY);
    }

    @Test
    public void testProcessEventSuccess() throws Exception {
        ConsentLCEventExecutor consentLCExecutorSpy = Mockito.spy(new ConsentLCEventExecutor());

        outContent.reset();

        Map<String, Object> configs = new HashMap<>();
        configs.put("DataHolder.ClientId", "dummyHolderId");
        configs.put("DataPublishing.Enabled", "false");

        FinancialServicesConfigParser fsConfigParserMock = mock(FinancialServicesConfigParser.class);

        try (MockedStatic<FinancialServicesConfigParser> configParserMockedStatic = mockStatic(
                FinancialServicesConfigParser.class);
             MockedStatic<FSDataPublisherUtil> dataPublisherUtilMockedStatic = mockStatic(FSDataPublisherUtil.class)) {

            when(FinancialServicesConfigParser.getInstance()).thenReturn(fsConfigParserMock);
            when(fsConfigParserMock.getConfiguration()).thenReturn(configs);

            HashMap<String, Object> consentDataMap = new HashMap<>();
            HashMap<String, String> consentAttributes = new HashMap<>();
            consentAttributes.put("customerProfileType", "individual-profile");
            consentAttributes.put("sharing_duration_value", "6000");

            ConsentResource consentResource = new ConsentResource();
            consentResource.setConsentAttributes(consentAttributes);

            DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
            detailedConsentResource.setConsentAttributes(consentAttributes);
            detailedConsentResource.setAuthorizationResources(new ArrayList<>(Arrays.asList(authResource)));

            consentDataMap.put("ConsentResource", consentResource);
            consentDataMap.put("DetailedConsentResource", detailedConsentResource);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("ConsentDataMap", consentDataMap);
            eventData.put(CONSENT_ID_KEY, "dummyConsentId");
            eventData.put(CURRENT_STATUS_KEY, "REVOKED");
            eventData.put(PREVIOUS_STATUS_KEY, "AUTHORIZED");
            eventData.put(CONSENT_DETAILS_KEY, detailedConsentResource);

            FSEvent fsEvent = new FSEvent("revoked", eventData);
            consentLCExecutorSpy.processEvent(fsEvent);
            Assert.assertFalse(outContent.toString().contains("Publishing consent data for metrics."));
        }
    }

    @Test
    public void testProcessEventFailure() throws Exception {
        ConsentLCEventExecutor consentLCEventExecutorSpy = Mockito.spy(new ConsentLCEventExecutor());
        FinancialServicesConfigParser fsConfigParserMock = mock(FinancialServicesConfigParser.class);

        outContent.reset();
        Map<String, Object> configs = new HashMap<>();
        configs.put("DataPublishing.Enabled", "false");
        configs.put("DataHolder.ClientId", "dummyHolderId");

        try (MockedStatic<FinancialServicesConfigParser> configParserMockedStatic = mockStatic(
                FinancialServicesConfigParser.class);
             MockedStatic<FSDataPublisherUtil> dataPublisherUtilMockedStatic = mockStatic(FSDataPublisherUtil.class)) {

            when(FinancialServicesConfigParser.getInstance()).thenReturn(fsConfigParserMock);
            when(fsConfigParserMock.getConfiguration()).thenReturn(configs);

            HashMap<String, Object> consentDataMap = new HashMap<>();
            HashMap<String, String> consentAttributes = new HashMap<>();
            consentAttributes.put("customerProfileType", "individual-profile");
            consentAttributes.put("sharing_duration_value", "6000");

            ConsentResource consentResource = new ConsentResource();
            consentResource.setConsentAttributes(consentAttributes);

            DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
            detailedConsentResource.setConsentAttributes(consentAttributes);
            detailedConsentResource.setAuthorizationResources(new ArrayList<>(Arrays.asList(authResource)));

            consentDataMap.put("ConsentResource", consentResource);
            consentDataMap.put("DetailedConsentResource", detailedConsentResource);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("ConsentDataMap", consentDataMap);
            eventData.put(CONSENT_ID_KEY, "dummyConsentId");
            eventData.put(CURRENT_STATUS_KEY, "REVOKED");
            eventData.put(PREVIOUS_STATUS_KEY, "AUTHORIZED");
            eventData.put(CONSENT_DETAILS_KEY, detailedConsentResource);
            FSEvent obEvent = new FSEvent("revoked", eventData);

            consentLCEventExecutorSpy.processEvent(obEvent);

            Assert.assertFalse(outContent.toString().contains("Error while trying to retrieve consent data"));
        }
    }


}

