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
package com.wso2.openbanking.accelerator.throttler.dao.model;

import java.sql.Timestamp;

/**
 * DTO class for throttle data.
 */
public class ThrottleDataModel {

    private String throttleGroup;
    private String throttleParam;
    private Timestamp lastUpdateTimestamp;
    private Timestamp unlockTimestamp;
    private int occurrences;

    /**
     * Constructor.
     *
     * @param throttleGroup       - Throttle group
     * @param throttleParam       - Throttle parameter
     * @param lastUpdateTimestamp - Updated timestamp
     * @param unlockTimestamp     - Parameter unlocking timestamp
     * @param occurrences         - number of occurrences of the parameter
     */
    public ThrottleDataModel(String throttleGroup, String throttleParam, Timestamp lastUpdateTimestamp,
                             Timestamp unlockTimestamp, int occurrences) {

        this.throttleGroup = throttleGroup;
        this.throttleParam = throttleParam;
        this.lastUpdateTimestamp = new Timestamp(lastUpdateTimestamp.getTime());
        this.unlockTimestamp = new Timestamp(unlockTimestamp.getTime());
        this.occurrences = occurrences;
    }

    public String getThrottleGroup() {

        return throttleGroup;
    }

    public void setThrottleGroup(String throttleGroup) {

        this.throttleGroup = throttleGroup;
    }

    public String getThrottleParam() {
        return throttleParam;
    }

    public void setThrottleParam(String throttleParam) {

        this.throttleParam = throttleParam;
    }

    public Timestamp getLastUpdateTimestamp() {

        return new Timestamp(lastUpdateTimestamp.getTime());
    }

    public void setLastUpdateTimestamp(Timestamp lastUpdateTimestamp) {

        this.lastUpdateTimestamp = new Timestamp(lastUpdateTimestamp.getTime());
    }

    public Timestamp getUnlockTimestamp() {

        return new Timestamp(unlockTimestamp.getTime());
    }

    public void setUnlockTimestamp(Timestamp unlockTimestamp) {

        this.unlockTimestamp = new Timestamp(unlockTimestamp.getTime());
    }

    public int getOccurrences() {

        return occurrences;
    }

    public void setOccurrences(int occurrences) {

        this.occurrences = occurrences;
    }
}
