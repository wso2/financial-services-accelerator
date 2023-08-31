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
package com.wso2.openbanking.accelerator.keymanager.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Map;

/**
 * Service component for key manager client.
 */
@Component(
        name = "com.wso2.open.banking.keymanager",
        immediate = true
)
public class KeyManagerServiceComponent {

    private static final Log log = LogFactory.getLog(KeyManagerServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        log.debug("Open banking key manager extension component is activated ");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Open banking key manager extension is deactivated ");
    }

    @Reference(
            service = APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetAPIMConfigs"
    )
    public void setAPIMConfig(APIManagerConfigurationService apManagerConfigurationService) {

        KeyManagerDataHolder.getInstance().setApiManagerConfiguration(apManagerConfigurationService);
    }

    public void unSetAPIMConfigs(APIManagerConfigurationService apManagerConfigurationService) {

        KeyManagerDataHolder.getInstance().setApiManagerConfiguration(apManagerConfigurationService);
    }

    /**
     * Initialize the KeyManager Connector configuration Service Service dependency.
     *
     * @param keyManagerConnectorConfiguration {@link KeyManagerConnectorConfiguration} service reference.
     */
    @Reference(
            name = "keyManager.connector.service",
            service = KeyManagerConnectorConfiguration.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeKeyManagerConnectorConfiguration")
    protected void addKeyManagerConnectorConfiguration(
            KeyManagerConnectorConfiguration keyManagerConnectorConfiguration) {

        KeyManagerDataHolder.getInstance()
                .addKeyManagerConnectorConfiguration(keyManagerConnectorConfiguration.getType(),
                        keyManagerConnectorConfiguration);

    }

    /**
     * De-reference the JWTTransformer service.
     *
     * @param keyManagerConnectorConfiguration
     */
    protected void removeKeyManagerConnectorConfiguration(
            KeyManagerConnectorConfiguration keyManagerConnectorConfiguration, Map<String, Object> properties) {
        if (properties.containsKey(APIConstants.KeyManager.KEY_MANAGER_TYPE)) {
            String type = (String) properties.get(APIConstants.KeyManager.KEY_MANAGER_TYPE);
            KeyManagerDataHolder.getInstance().removeKeyManagerConnectorConfiguration(type);
        }
    }

    @Reference(
            name = "realm.service",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        log.debug("Setting the Realm Service");
        KeyManagerDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        log.debug("UnSetting the Realm Service");
        KeyManagerDataHolder.getInstance().setRealmService(null);
    }

}
