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


package com.wso2.openbanking.accelerator.identity.auth.extensions.response.validator;

import com.wso2.openbanking.accelerator.common.util.Generated;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.wso2.carbon.identity.oauth.ciba.handlers.CibaResponseTypeValidator;

import javax.servlet.http.HttpServletRequest;

/**
 * Validates authorize responses with cibaAuthCode as response type.
 */
@Generated(message = "Ignoring since method do not contain a logic")
public class OBCibaResponseTypeValidator extends CibaResponseTypeValidator {

    @Override
    @Generated(message = "Ignoring since method do not contain a logic")
    public void validateContentType(HttpServletRequest request) throws OAuthProblemException {
        // Overriding content type validation
        // This is for browser flow with cibaAuthCode response type. (Web-Auth link scenario)
    }

}
