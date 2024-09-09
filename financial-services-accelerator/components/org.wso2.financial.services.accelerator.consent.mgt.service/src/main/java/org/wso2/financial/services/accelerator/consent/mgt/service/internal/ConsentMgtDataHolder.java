/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.service.internal;

import org.wso2.carbon.identity.oauth2.OAuth2Service;

/**
 * Data holder for consent management service.
 */
public class ConsentMgtDataHolder {

    private OAuth2Service oAuth2Service;
    private static volatile ConsentMgtDataHolder instance;

    public static ConsentMgtDataHolder getInstance() {

        if (instance == null) {
            synchronized (ConsentMgtDataHolder.class) {
                if (instance == null) {
                    instance = new ConsentMgtDataHolder();
                }
            }
        }
        return instance;
    }

    public OAuth2Service getOAuth2Service() {

        return oAuth2Service;
    }

    public void setOAuth2Service(OAuth2Service oAuth2Service) {

        this.oAuth2Service = oAuth2Service;
    }
}
