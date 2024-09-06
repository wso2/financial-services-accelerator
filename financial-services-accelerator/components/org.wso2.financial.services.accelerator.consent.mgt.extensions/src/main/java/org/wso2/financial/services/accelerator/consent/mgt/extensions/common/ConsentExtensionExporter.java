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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.common;

import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.builder.ConsentAdminBuilder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.builder.ConsentStepsBuilder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.builder.ConsentManageBuilder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.builder.ConsentValidateBuilder;

/**
 * Exporter service to facilitate access to loaded builder classes in data holder from other modules.
 */
public class ConsentExtensionExporter {

    private static volatile ConsentExtensionExporter consentExtExporter;
    private static ConsentManageBuilder consentManageBuilder;
    private static ConsentStepsBuilder consentStepsBuilder;
    private static ConsentValidateBuilder consentValidateBuilder;
    private static ConsentAdminBuilder consentAdminBuilder;

    private ConsentExtensionExporter() {
    }

    public static ConsentExtensionExporter getInstance() {
        if (consentExtExporter == null) {
            synchronized (ConsentExtensionExporter.class) {
                if (consentExtExporter == null) {
                    consentExtExporter = new ConsentExtensionExporter();
                }
            }
        }

        return consentExtExporter;
    }

    public static ConsentManageBuilder getConsentManageBuilder() {
        return consentManageBuilder;
    }

    public static void setConsentManageBuilder(ConsentManageBuilder consentManageBuilder) {
        ConsentExtensionExporter.consentManageBuilder = consentManageBuilder;
    }

    public static ConsentValidateBuilder getConsentValidateBuilder() {
        return consentValidateBuilder;
    }

    public static void setConsentValidateBuilder(ConsentValidateBuilder consentValidateBuilder) {
        ConsentExtensionExporter.consentValidateBuilder = consentValidateBuilder;
    }

    public static ConsentStepsBuilder getConsentStepsBuilder() {
        return consentStepsBuilder;
    }

    public static void setConsentStepsBuilder(ConsentStepsBuilder consentStepsBuilder) {
        ConsentExtensionExporter.consentStepsBuilder = consentStepsBuilder;
    }

    public static ConsentAdminBuilder getConsentAdminBuilder() {
        return consentAdminBuilder;
    }

    public static void setConsentAdminBuilder(ConsentAdminBuilder consentAdminBuilder) {
        ConsentExtensionExporter.consentAdminBuilder = consentAdminBuilder;
    }
}
