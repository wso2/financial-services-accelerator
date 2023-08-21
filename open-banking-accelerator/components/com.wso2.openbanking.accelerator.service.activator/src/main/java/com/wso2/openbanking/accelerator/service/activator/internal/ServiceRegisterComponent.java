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

package com.wso2.openbanking.accelerator.service.activator.internal;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.service.activator.OBServiceObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Objects;

/**
 * ServiceRegisterComponent.
 *
 * OSGI Component class to register and activate subscriber (observer) classes
 */
@Component
public class ServiceRegisterComponent {

    private static final Log LOG = LogFactory.getLog(ServiceRegisterComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        ServiceObservable serviceObservable = ServiceObservable.getInstance();

        OpenBankingConfigParser.getInstance().getServiceActivatorSubscribers()
                .stream()
                .map(this::getInstanceFromFQN)
                .filter(Objects::nonNull)
                .forEach(serviceObservable::registerServiceObserver);

        serviceObservable.activateAllServiceObservers();
        LOG.debug("All OB service observers are activated");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        LOG.debug("Metadata Updater bundle is deactivated");
    }

    private OBServiceObserver getInstanceFromFQN(String fqn) {
        try {
            return (OBServiceObserver) Class.forName(fqn).newInstance();
        } catch (ClassNotFoundException e) {
            LOG.error("Unable to find the OBServiceObserver class implementation", e);
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Error occurred while loading the OBServiceObserver class implementation", e);
        }
        return null;
    }
}
