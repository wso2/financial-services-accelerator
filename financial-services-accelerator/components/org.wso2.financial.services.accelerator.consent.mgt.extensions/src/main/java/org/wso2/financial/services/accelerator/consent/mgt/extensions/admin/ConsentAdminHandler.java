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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.admin;


import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ConsentAdminData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;

/**
 * Consent admin handler interface.
 */
public interface ConsentAdminHandler {

    /**
     * This method is used to handle the consent search request.
     *
     * @param consentAdminData Data wrapper for consent admin data that holds the request context data
     * @throws ConsentException  thrown if any error occurs in the process
     */
    public void handleSearch(ConsentAdminData consentAdminData) throws ConsentException;

    /**
     * This method is used to handle the consent revoke request.
     *
     * @param consentAdminData Data wrapper for consent admin data that holds the request context data
     * @throws ConsentException  thrown if any error occurs in the process
     */
    public void handleRevoke(ConsentAdminData consentAdminData) throws ConsentException;

    /**
     * This method is used to handle the consent amendment history retrieval request.
     *
     * @param consentAdminData Data wrapper for consent admin data that holds the request context data
     * @throws ConsentException  thrown if any error occurs in the process
     */
    public void handleConsentAmendmentHistoryRetrieval(ConsentAdminData consentAdminData) throws ConsentException;

    /**
     * This method is used to handle the consent expiry request.
     *
     * @param consentAdminData Data wrapper for consent admin data that holds the request context data
     * @throws ConsentException thrown if any error occurs in the process
     */
    public void handleConsentExpiry(ConsentAdminData consentAdminData) throws ConsentException;

    /**
     * Method to handle the consent status audit search.
     * @param consentAdminData consentAdminData
     * @throws ConsentException if any error occurs while searching the consent status audit
     */
    public void handleConsentStatusAuditSearch(ConsentAdminData consentAdminData) throws ConsentException;

    /**
     * Method to handle the consent file search.
     * @param consentAdminData consentAdminData
     * @throws ConsentException if any error occurs while searching the consent file
     */
    public void handleConsentFileSearch(ConsentAdminData consentAdminData) throws ConsentException;

}
