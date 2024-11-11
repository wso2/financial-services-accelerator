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

import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import org.wso2.carbon.base.ServerConfiguration;

import java.io.File;

/**
 * Utility Class for WSO2 Carbon related functions.
 */
public class CarbonUtils {

    private static final String HTTPS = "https://";
    private static final String COLON = ":";

    /**
     * Method to obtain config directory of any carbon product.
     *
     * @return String indicating the location of conf directory.
     */
    public static String getCarbonConfigDirPath() {

        String carbonConfigDirPath = System.getProperty("carbon.config.dir.path");
        if (carbonConfigDirPath == null) {
            carbonConfigDirPath = System.getenv("CARBON_CONFIG_DIR_PATH");
            if (carbonConfigDirPath == null) {
                return getCarbonHome() + File.separator + "repository" + File.separator + "conf";
            }
        }
        return carbonConfigDirPath;
    }

    /**
     * Method to obtain home location of the carbon product.
     *
     * @return String indicating the home location of conf directory.
     */
    public static String getCarbonHome() {

        String carbonHome = System.getProperty(OpenBankingConstants.CARBON_HOME);
        if (carbonHome == null) {
            carbonHome = System.getenv("CARBON_HOME");
            System.setProperty(OpenBankingConstants.CARBON_HOME, carbonHome);
        }
        return carbonHome;
    }

    /**
     * Method to obtain hostname of the carbon product.
     *
     * @return String indicating the hostname of the server.
     */
    @Generated(message = "Ignoring because ServerConfiguration cannot be mocked")
    public static String getCarbonPort() {

        int offset = Integer.parseInt(ServerConfiguration.getInstance().getFirstProperty("Ports.Offset"));
        return String.valueOf(9443 + offset);
    }

    /**
     * Method to obtain port of the carbon product.
     *
     * @return String indicating the port of the server.
     */
    @Generated(message = "Ignoring because ServerConfiguration cannot be mocked")
    public static String getCarbonHostname() {
        return ServerConfiguration.getInstance().getFirstProperty("HostName");
    }

    /**
     * Method to obtain server url of the carbon product.
     *
     * @return String indicating the url of the server.
     */
    @Generated(message = "Ignoring because ServerConfiguration cannot be mocked")
    public static String getCarbonServerUrl() {
        return HTTPS + getCarbonHostname() + COLON + getCarbonPort();
    }
}
