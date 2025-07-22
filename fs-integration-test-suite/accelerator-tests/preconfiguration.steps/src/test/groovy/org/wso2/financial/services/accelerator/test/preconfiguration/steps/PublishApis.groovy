package org.wso2.financial.services.accelerator.test.preconfiguration.steps

import groovy.json.JsonOutput
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.APIConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.ApiPublisherRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.charset.Charset

/**
 * This class contains tests to publish APIs in the WSO2 Financial Services Accelerator (FSA) environment.
 */
class PublishApis extends FSAPIMConnectorTest {

    String adminUserName, clientId, clientSecret, publisherUrl
    String accessToken
    List<String> scopesList
    ApiPublisherRequestBuilder apiPublisherRequestBuilder

    @BeforeClass
    void init() {
        dcrPath = configuration.getApimServerUrl() + ConnectorTestConstants.INTERNAL_APIM_DCR_ENDPOINT
        publisherUrl = configuration.getApimServerUrl() + ConnectorTestConstants.REST_API_PUBLISHER_ENDPOINT
        adminUserName = configuration.getUserIsAsKeyManagerAdminName()
        apiConfiguration = new APIConfigurationService()
        apiPublisherRequestBuilder = new ApiPublisherRequestBuilder()

        scopesList = Arrays.asList("apim:api_view apim:api_create apim:api_manage apim:api_delete apim:api_publish " +
                "apim:subscription_view apim:subscription_block apim:subscription_manage apim:external_services_discover " +
                "apim:threat_protection_policy_create apim:threat_protection_policy_manage apim:document_create " +
                "apim:document_manage apim:mediation_policy_view apim:mediation_policy_create apim:mediation_policy_manage " +
                "apim:client_certificates_view apim:client_certificates_add apim:client_certificates_update " +
                "apim:ep_certificates_view apim:ep_certificates_add apim:ep_certificates_update apim:publisher_settings " +
                "apim:pub_alert_manage apim:shared_scope_manage apim:app_import_export apim:api_import_export " +
                "apim:api_product_import_export apim:api_generate_key apim:common_operation_policy_view " +
                "apim:common_operation_policy_manage apim:comment_write apim:comment_view apim:admin " +
                "apim:subscription_approval_view apim:subscription_approval_manage apim:llm_provider_read")
    }

    /**
     * Create an internal application to access api-m publisher portal
     */
    @Test
    void "Create Application"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildIsAsKeyManagerRegistrationRequest()
                .body(ClientRegistrationRequestBuilder.getApimDcrClaims(adminUserName, ConnectorTestConstants.PUBLISHER_CLIENT_NAME))
                .post(dcrPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        clientId = TestUtil.parseResponseBody(registrationResponse, "clientId")
        clientSecret = TestUtil.parseResponseBody(registrationResponse, "clientSecret")
    }

    /**
     * Generate access token to access api-m publisher portal
     */
    @Test(dependsOnMethods = "Create Application")
    void "Generate Access Token"(){

        Response accessTokenResponse = TokenRequestBuilder.getAccessTokenInApim(clientId, clientSecret, scopesList)

        Assert.assertEquals(accessTokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        accessToken = TestUtil.parseResponseBody(accessTokenResponse, "access_token")
        Assert.assertNotNull(accessToken, "Access token should not be null")
    }

    @Test(dependsOnMethods = "Generate Access Token")
    void "Add Policies"(){

        Map<String, String> policyIdMap = apiPublisherRequestBuilder.createCommonOperationPolicy(accessToken)

        List<Map> policyList = apiConfiguration.getPolicyList()
        Assert.assertTrue(policyIdMap.size() == policyList.size(), "Policy ID map should not be empty")
    }

    @Test(dependsOnMethods = "Add Policies")
    void "Create and Publish APIs"() {

        List<Map> apiList = apiConfiguration.getApiList()

        apiList.each { apiInfo ->

            //Create APIs using API Definitions
            String apiId = apiPublisherRequestBuilder.createAPIs(accessToken, apiInfo)
            Assert.assertNotNull(apiId, "API ID should not be null for API: " + apiInfo.get("name"))

            //Update APIs to add operation level policies
            apiPublisherRequestBuilder.updateAPIs(accessToken, apiInfo, apiId)

            //Create Revision and Deploy API
            String revisionId = apiPublisherRequestBuilder.createRevision(accessToken, apiId)
            apiPublisherRequestBuilder.deployRevision(accessToken, apiId, revisionId)

            //Publish API
            apiPublisherRequestBuilder.publishAPI(accessToken, apiId)
        }
    }
}
