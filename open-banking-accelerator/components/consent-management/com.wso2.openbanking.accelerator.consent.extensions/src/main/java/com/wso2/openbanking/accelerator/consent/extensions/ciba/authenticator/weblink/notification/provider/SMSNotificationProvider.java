/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink.notification.provider;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.util.Map;

/**
 * This class implements the SMS based CIBA notification provider.
 */
public class SMSNotificationProvider implements NotificationProvider {

    public static final String MOBILE_CLAIM = "http://wso2.org/claims/mobile";

    @Override
    public void send(String username, String webLink) throws OpenBankingException {

        AuthenticatedUser authenticatedUser =
                AuthenticatedUser.createFederateAuthenticatedUserFromSubjectIdentifier(username);
        String mobileNumber = null;
        try {
            int tenantId = IdentityTenantUtil.getTenantIdOfUser(username);
            UserRealm userRealm = ConsentExtensionsDataHolder.getInstance().getRealmService()
                    .getTenantUserRealm(tenantId);
            Map<String, String> claimValues =
                    userRealm.getUserStoreManager().getUserClaimValues(
                            MultitenantUtils.getTenantAwareUsername(authenticatedUser.toFullQualifiedUsername()),
                            new String[]{MOBILE_CLAIM}, null);
            if (claimValues != null && claimValues.containsKey(MOBILE_CLAIM) &&
                    !claimValues.get(MOBILE_CLAIM).isEmpty()) {
                mobileNumber = claimValues.get(MOBILE_CLAIM);
            } else {
                throw new OpenBankingException(String.format(
                        "Error could not resolve mobile number for user : %s", username));
            }
        } catch (UserStoreException e) {
            throw new OpenBankingException(String.format(
                    "Error could not resolve mobile number for user : %s", username), e);
        }

        try (CloseableHttpClient httpClient = HTTPClientUtils.getHttpsClient()) {
            String notificationServiceURL =
                    OpenBankingConfigParser.getInstance().getCibaWebLinkSMSNotificationServiceURL();
            if (notificationServiceURL == null) {
                throw new OpenBankingException("Error occurred while retrieving SMS service URL.");
            }
            HttpPost httpPost = new HttpPost(notificationServiceURL);
            StringEntity entity = new StringEntity(getPayload(username, webLink, mobileNumber));
            httpPost.setEntity(entity);
            setHeaders(httpPost);
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

            if (!((httpResponse.getStatusLine() != null
                    && httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) ||
                    (httpResponse.getStatusLine() != null
                            && httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED))
            ) {
                String error = String.format("Error while invoking rest api : %s %s",
                        httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
                throw new OpenBankingException(error);
            }
        } catch (IOException e) {
            throw new OpenBankingException("Error when creating http client.", e);
        }
    }

    /**
     * Method to set the http headers to the SMS request.
     *
     * @param httpPost httpPost
     */
    public void setHeaders(HttpPost httpPost) {
        Map<String, String> notificationRequestHeaders = OpenBankingConfigParser.getInstance()
                .getCibaWebLinkSMSNotificationRequestHeaders();
        for (Map.Entry<String, String> header : notificationRequestHeaders.entrySet()) {
            httpPost.setHeader(header.getKey(), header.getValue());
        }
    }

    /**
     * Method to get the payload of the SMS service request.
     *
     * @param username username
     * @param webLink  auth web link
     * @return payload in string
     */
    public String getPayload(String username, String webLink, String mobileNumber) {
        // SMS message payload to be sent to SMS service.
        return webLink;
    }
}
