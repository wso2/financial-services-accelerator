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

package com.wso2.openbanking.accelerator.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Open Banking common utility class to publish analytics logs
 */
public class AnalyticsLogsUtils {

    private static final Log log = LogFactory.getLog(AnalyticsLogsUtils.class);
    private static final String LOG_FORMAT = "Data Stream : %s , Data Stream Version : %s , Data : {\"payload\":%s}";
    private static final String DATA_PROCESSING_ERROR = "Error occurred while processing the analytics dataset";

    /**
     * Method to add analytics logs to the OB analytics log file
     *
     * @param logFile       Name of the logger which is used to log analytics data to the log file
     * @param dataStream    Name of the data stream to which the data belongs
     * @param dataVersion   Version of the data stream to which the data belongs
     * @param analyticsData Data which belongs to the given data stream that needs to be logged via the given logger
     */
    public static void addAnalyticsLogs (String logFile, String dataStream, String dataVersion, Map<String,
            Object> analyticsData) throws OpenBankingException {
        Log customLog = LogFactory.getLog(logFile);
        try {
            customLog.info(String.format(LOG_FORMAT, dataStream,
                    dataVersion, new ObjectMapper().writeValueAsString(analyticsData)));
        } catch (JsonProcessingException e) {
            log.error(DATA_PROCESSING_ERROR);
            throw new OpenBankingException(DATA_PROCESSING_ERROR, e);
        }
    }

}
