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
package com.wso2.openbanking.accelerator.consent.extensions.event.executors;

import com.wso2.openbanking.accelerator.common.event.executor.OBEventExecutor;
import com.wso2.openbanking.accelerator.common.event.executor.model.OBEvent;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.PeriodicLimit;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for executing Variable Recurring Payments (VRP) events.
 * It implements the OBEventExecutor interface and overrides its methods to provide
 * specific implementations for VRP events.
 */
public class VRPEventExecutor implements OBEventExecutor {

    public static List<PeriodicLimit> validateInstructedAmountWithControlParameters(BigDecimal instructedAmount,
                                                                                    JSONObject controlParameters) {

        /**
         * Validates the instructed amount with control parameters and returns a list of PeriodicLimit objects.
         * If the instructed amount is greater than the maximum individual amount or the cyclic remaining amount,
         * an empty list is returned. If the JSON parsing fails, an empty list is also returned.
         *
         * @param instructedAmount The instructed amount to be validated
         * @param controlParameters The control parameters to be used for validation
         * @return A list of PeriodicLimit objects
         */
        List<PeriodicLimit> periodicLimitsList = new ArrayList<>();

        BigDecimal maxIndividualAmount = BigDecimal.valueOf(Double.parseDouble(controlParameters.
                getAsString(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT)));

        if (instructedAmount.compareTo(maxIndividualAmount) > 0) {
            return periodicLimitsList;
        }

        JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
        JSONArray periodicLimits;

        try {
            periodicLimits = (JSONArray) parser.parse(controlParameters.
                    getAsString(ConsentExtensionConstants.PERIODIC_LIMITS));
        } catch (ParseException e) {
            // Log the error or handle it as needed
            return periodicLimitsList;
        }

        long currentMoment = System.currentTimeMillis() / 1000;

        for (Object obj : periodicLimits) {
            JSONObject limit = (JSONObject) obj;
            BigDecimal amount = BigDecimal.
                    valueOf(Double.parseDouble(limit.getAsString(ConsentExtensionConstants.AMOUNT)));
            long cyclicExpiryTime = Long.parseLong(limit.getAsString(ConsentExtensionConstants.CYCLIC_EXPIRY_TIME));
            BigDecimal cyclicRemainingAmount = BigDecimal.
                    valueOf(Double.parseDouble(limit.getAsString(ConsentExtensionConstants.CYCLIC_REMAINING_AMOUNT)));

            String periodType = limit.getAsString(ConsentExtensionConstants.PERIOD_TYPE);
            String periodAlignment = limit.getAsString(ConsentExtensionConstants.PERIOD_ALIGNMENT);

            PeriodicLimit periodicLimit = new PeriodicLimit(periodType, amount, periodAlignment);

            if (currentMoment <= cyclicExpiryTime) {
                if (instructedAmount.compareTo(cyclicRemainingAmount) > 0) {
                    return periodicLimitsList;
                } else {
                    cyclicRemainingAmount = cyclicRemainingAmount.subtract(instructedAmount);
                }
            } else {
                while (currentMoment > periodicLimit.getCyclicExpiryTime()) {
                    periodicLimit.setCyclicExpiryTime();
                }
                cyclicRemainingAmount = amount;
                if (instructedAmount.compareTo(cyclicRemainingAmount) > 0) {
                    return periodicLimitsList;
                } else {
                    cyclicRemainingAmount = cyclicRemainingAmount.subtract(instructedAmount);
                }
            }
            periodicLimitsList.add(periodicLimit);
        }

        return periodicLimitsList;
    }

    /**
     * Processes the given OBEvent. This method is part of the OBEventExecutor interface and needs to be implemented.
     *
     * @param obEvent The OBEvent to be processed
     */
    @Override
    public void processEvent(OBEvent obEvent) {

    }
}
