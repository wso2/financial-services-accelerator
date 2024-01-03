package com.wso2.openbanking.accelerator.consent.extensions.manage.vrp;


import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentServiceUtil;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.extensions.manage.impl.ConsentManageRequestHandler;
import com.wso2.openbanking.accelerator.consent.extensions.manage.impl.VRPConsentRequestHandler;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.extensions.utils.ConsentExtensionTestUtils;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * comment.
 */
@PowerMockIgnore({"jdk.internal.reflect.*",
"com.wso2.openbanking.accelerator.consent.extensions.common.*"})
@PrepareForTest({OpenBankingConfigParser.class, ConsentServiceUtil.class,
        ConsentExtensionsDataHolder.class})
public class VRPConsentHandlerTest extends PowerMockTestCase {

    @Mock
    ConsentManageData consentManageDataMock;

    ConsentManageRequestHandler consentManageRequestHandler;
    public static final String DOMESTIC_VRP_CONSENT_PATH = "domestic-vrp-consents";

    private static final String INVALID_VRP_PATH = "domestic-vrp-consent/34567890987";

    public static final OffsetDateTime EXPIRATION_DATE = OffsetDateTime.now().plusDays(50);
    public static final OffsetDateTime TRANSACTION_FROM_DATE = OffsetDateTime.now();
    public static final OffsetDateTime TRANSACTION_TO_DATE = OffsetDateTime.now().plusDays(30);
    public static final OffsetDateTime COMPLETION_DATE = OffsetDateTime.now().plusDays(30);



    public static final String VRP_INITIATION = "{\n" +
            "  \"Data\": {\n" +
            "    \"ReadRefundAccount\": \"Yes\",\n" +
            "    \"ControlParameters\": {\n" +
            "      \"PSUAuthenticationMethods\": [ \"UK.OBIE.SCA\" ],\n" +
            "      \"PSUInteractionTypes\": [ \"OffSession\" ],\n" +
            "      \"VRPType\": [ \"UK.OBIE.VRPType.Sweeping\" ],\n" +
            "      \"ValidFromDateTime\": \"" + TRANSACTION_FROM_DATE + "\",\n" +
            "      \"ValidToDateTime\": \"" + TRANSACTION_TO_DATE + "\",\n" +
            "      \"MaximumIndividualAmount\": {\n" +
            "        \"Amount\": \"100.00\",\n" +
            "        \"Currency\": \"GBP\"\n" +
            "      },\n" +
            "      \"PeriodicLimits\": [\n" +
            "        {\n" +
            "          \"Amount\": \"200.00\",\n" +
            "          \"Currency\": \"GBP\",\n" +
            "          \"PeriodAlignment\": \"Consent\",\n" +
            "          \"PeriodType\": \"Week\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"Initiation\": {\n" +
            "      \"DebtorAccount\": {\n" +
            "        \"SchemeName\": \"UK.OBIE.IBAN\",\n" +
            "        \"Identification\": \"GB76LOYD30949301273801\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"CreditorAccount\": {\n" +
            "        \"SchemeName\": \"UK.OBIE.SortCodeAccountNumber\",\n" +
            "        \"Identification\": \"30949330000010\",\n" +
            "        \"SecondaryIdentification\": \"Roll 90210\",\n" +
            "        \"Name\": \"Marcus Sweepimus\"\n" +
            "      },\n" +
            "      \"RemittanceInformation\": {\n" +
            "        \"Reference\": \"Sweepco\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"Risk\": {\n" +
            "    \"PaymentContextCode\": \"TransferToThirdParty\"\n" +
            "  }\n" +
            "}";

    @InjectMocks
    private final VRPConsentRequestHandler handler = new VRPConsentRequestHandler();

    @Mock
    private ConsentManageData consentManageData;

    @Mock
    private ConsentResource consent;

    @Mock
    OpenBankingConfigParser openBankingConfigParser;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceImpl;

    @Mock
    ConsentExtensionsDataHolder consentExtensionsDataHolder;

    @Mock
    ConsentCoreService consentCoreService;

    private static Map<String, String> configMap;


    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void setUp() throws ReflectiveOperationException {
        MockitoAnnotations.initMocks(this);

        configMap = new HashMap<>();

        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        ConsentExtensionTestUtils.injectEnvironmentVariable("CARBON_HOME", ".");

        consentManageData = mock(ConsentManageData.class);
    }

    @BeforeMethod
    public void initMethod() {

        openBankingConfigParser = mock(OpenBankingConfigParser.class);
        doReturn(configMap).when(openBankingConfigParser).getConfiguration();

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

    }


    @Test(expectedExceptions = ConsentException.class)
    public void testHandleConsentManageGetWithValidConsentIdAndMatchingClientId() throws ConsentManagementException {
        UUID consentIdUUID = UUID.randomUUID();
        doReturn("vrp-consent/".concat(consentIdUUID.toString())).when(consentManageData).getRequestPath();
        ConsentResource consent = mock(ConsentResource.class);
        doReturn("5678").when(consent).getClientID();

        consentCoreServiceImpl = mock(ConsentCoreServiceImpl.class);
        doReturn(consent).when(consentCoreServiceImpl).getConsent(anyString(), anyBoolean());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceImpl);

        String expectedClientId = "matchingClientId";
        doReturn(expectedClientId).when(consentManageData).getClientId();

        handler.handleConsentManageGet(consentManageData);
    }


    @Test(expectedExceptions = ConsentException.class)
    public void testHandleConsentManageDeleteWithValidConsent() throws ConsentManagementException {

        UUID consentIdUUID = UUID.randomUUID();
        doReturn("vrp-consent/".concat(consentIdUUID.toString())).when(consentManageData).getRequestPath();
        ConsentResource consent = mock(ConsentResource.class);
        doReturn("5678").when(consent).getClientID();

        consentExtensionsDataHolder = mock(ConsentExtensionsDataHolder.class);
        doReturn(consent).when(consentExtensionsDataHolder).getConsentCoreService().
                getConsent(anyString(), anyBoolean());

        PowerMockito.mockStatic(ConsentExtensionsDataHolder.class);
        when(ConsentExtensionsDataHolder.getInstance()).thenReturn(consentExtensionsDataHolder);

        String expectedClientId = "6788";
        doReturn(expectedClientId).when(consentManageData).getClientId();

        handler.handleConsentManageDelete(consentManageData);
    }

//    @Test(expectedExceptions = ConsentException.class)
//    public void testHandleVRPConsentManagePostWithInvalidPayload() {
//
//        Mockito.doReturn("Vrp-response").when(consentManageDataMock).getPayload();
//        Mockito.doReturn(DOMESTIC_VRP_CONSENT_PATH).when(consentManageDataMock)
//                .getRequestPath();
//
//        consentManageRequestHandler.handleConsentManagePost(consentManageDataMock);
//    }

}
