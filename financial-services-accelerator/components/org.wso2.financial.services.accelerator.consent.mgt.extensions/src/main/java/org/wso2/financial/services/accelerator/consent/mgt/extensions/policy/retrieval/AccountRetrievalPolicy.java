/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.financial.services.accelerator.consent.mgt.extensions.policy.retrieval;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.util.HTTPClientUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Financial Services Consent Retrieval Step Policy for Account Retrieval.
 */
public class AccountRetrievalPolicy extends ConsentRetrievalStepPolicy {

    private static final String ACCOUNTS_ENDPOINT = "sharable-accounts-endpoint";

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject, Map<String, Object> propertyMap,
                        Map<String, Object> retrievalContext) throws ConsentException {

        if (!propertyMap.containsKey(ACCOUNTS_ENDPOINT)) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Missing 'sharable-accounts-endpoint' " +
                    "in policy properties.");
        }
        String accountsEndpoint = propertyMap.get(ACCOUNTS_ENDPOINT).toString();

        if (accountsEndpoint == null || accountsEndpoint.isEmpty()) {
            // ToDo: Remove dummy account ID addition in milestone release.
            JSONArray accountsJSON = ConsentAuthorizeUtil.appendDummyAccountID();
            jsonObject.put("accounts", accountsJSON);
            return;
        }

        try {

            URIBuilder uriBuilder = new URIBuilder(accountsEndpoint);
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.setHeader(FinancialServicesConstants.CONTENT_TYPE_TAG,
                    FinancialServicesConstants.JSON_CONTENT_TYPE);
            httpGet.setHeader(FinancialServicesConstants.ACCEPT, FinancialServicesConstants.JSON_CONTENT_TYPE);

            CloseableHttpResponse response = HTTPClientUtils.getHttpClient().execute(httpGet);

            // Check response status
            if (response.getStatusLine().getStatusCode() != 200) {
                String errorMsg = IOUtils.toString(response.getEntity().getContent());
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error occurred while retrieving accounts data: " + errorMsg);
            }

            // Process response
            InputStream responseBody = response.getEntity().getContent();
            String responseString = IOUtils.toString(responseBody);
            JSONArray accountsJSON = new JSONArray(responseString);

            // Populate accounts in the provided JSON object
            jsonObject.put("accounts", accountsJSON);

        } catch (FinancialServicesException | IOException | URISyntaxException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Error occurred while retrieving accounts data from external service", e);
        }
    }

}
