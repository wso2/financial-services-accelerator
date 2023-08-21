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

package com.wso2.openbanking.sample.aggregator.identity.util;

import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;

import javax.servlet.http.Cookie;

/**
 * Application Updater for Aggregator Sample.
 */
public class AggregatorIdentityUtil {

    /**
     * Method to extract CommonAuthID from message Context
     * @param context OAuthAuthzReqMessageContext object
     * @return Common Auth ID
     */
    public static String getCommonAuthId(OAuthAuthzReqMessageContext context) {
        Cookie[] cookies = context.getAuthorizationReqDTO().getCookie();
        String commonAuthId = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(AggregatorConstants.COMMON_AUTH_ID_TAG)) {
                commonAuthId = cookie.getValue();
            }
        }
        return commonAuthId;
    }
}
