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
package com.wso2.openbanking.accelerator.throttler.service.internal;

import com.wso2.openbanking.accelerator.throttler.service.OBThrottleService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * OBThrottler component.
 */
@Component(
        name = "open.banking.throttler.component",
        immediate = true
)
public class OBThrottlerServiceComponent {

    private static final Log log = LogFactory.getLog(OBThrottlerServiceComponent.class);

    public static RealmService getRealmService() {
        return (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(RealmService.class);
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
        OBThrottlerDataHolder.getInstance().setRealmService(realmService);
    }

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            OBThrottleService obThrottleService = OBThrottleService.getInstance();
            ctxt.getBundleContext().registerService(OBThrottleService.class.getName(),
                    obThrottleService, null);
            log.debug("OBThrottleService bundle is activated");

        } catch (Throwable e) {
            log.error("OBThrottleService bundle activation Failed", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        log.debug("OBThrottleService bundle is deactivated");
    }

    protected void unsetRealmService(RealmService realmService) {

        log.debug("UnSetting the Realm Service");
        OBThrottlerDataHolder.getInstance().setRealmService(null);
    }
}
