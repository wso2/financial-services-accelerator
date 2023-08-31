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


package com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.handler.retrieval;


import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import net.minidev.json.JSONArray;


/**
 * Interface to handle Consent data retrieval for Authorize.
 */
public interface ConsentRetrievalHandler {

    /**
     * Abstract method defined to retrieve the consent related data in the authorization flow to send them to the
     * consent page to get PSU consent.
     *
     * @param consentResource Consent Resource parameter containing consent related information retrieved from database
     * @return
     * @throws ConsentException
     */
    JSONArray getConsentDataSet(ConsentResource consentResource) throws ConsentException;

}
