/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.job.scheduler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;

import java.io.File;
import java.nio.file.Paths;

/**
 * Periodic consent job scheduler class.
 * This class initialize the scheduler and schedule configured jobs and triggers.
 */
public class PeriodicalConsentJobScheduler {

    private static volatile PeriodicalConsentJobScheduler instance;
    private static final String QUARTZ_PROPERTY_FILE = "quartz.properties";

    private static volatile Scheduler scheduler;
    private static Log log = LogFactory.getLog(PeriodicalConsentJobScheduler.class);

    private PeriodicalConsentJobScheduler() {

        initScheduler();
    }

    /**
     * Get an instance of the PeriodicalConsentJobScheduler. It implements a double checked locking initialization.
     *
     * @return PeriodicalConsentJobScheduler instance
     */
    @Generated(message = "Ignoring since method contains no unit testable logics")
    public static synchronized PeriodicalConsentJobScheduler getInstance() {

        if (instance == null) {
            synchronized (PeriodicalConsentJobScheduler.class) {
                if (instance == null) {
                    instance = new PeriodicalConsentJobScheduler();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize the scheduler
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    // Suppressed content - Paths.get(CarbonUtils.getCarbonConfigDirPath()).toString() + "/" + QUARTZ_PROPERTY_FILE;
    // Suppression reason - False Positive : Carbon config Dir path is obtained internally. So it can be marked
    //                      as a trusted filepath
    // Suppressed warning count - 1
    @Generated(message = "Ignoring since method contains no unit testable logics")
    private void initScheduler() {

        if (instance != null) {
            return;
        }
        synchronized (PeriodicalConsentJobScheduler.class) {
            try {
                String quartzConfigFile = Paths.get(CarbonUtils.getCarbonConfigDirPath()).toString() + "/"
                        + QUARTZ_PROPERTY_FILE;
                boolean exists = new File(quartzConfigFile).exists();
                if (exists) {
                    StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
                    stdSchedulerFactory.initialize(quartzConfigFile);
                    scheduler = stdSchedulerFactory.getScheduler();
                } else {
                    scheduler = StdSchedulerFactory.getDefaultScheduler();
                }
                scheduler.start();
            } catch (SchedulerException e) {
                log.error("Exception while initializing the scheduler", e);
            }
        }
    }

    /**
     * Returns the scheduler
     *
     * @return Scheduler scheduler.
     */
    @Generated(message = "Ignoring since method contains no unit testable logics")
    public Scheduler getScheduler() {

        return scheduler;
    }
}
