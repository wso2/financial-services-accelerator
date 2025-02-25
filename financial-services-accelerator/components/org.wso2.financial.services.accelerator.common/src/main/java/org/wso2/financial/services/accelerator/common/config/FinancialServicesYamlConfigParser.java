/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.financial.services.accelerator.common.config;

import org.wso2.financial.services.accelerator.common.exception.FinancialServicesRuntimeException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Config parser to read the financial-services.yaml.
 */
public class FinancialServicesYamlConfigParser {

    private static final String CONFIG_FILE_NAME = "financial-services.yaml";

    public static Map<String, Object> parseConfig() {
        // Get the Carbon Home system property
        String carbonHome = System.getProperty("carbon.home");
        if (carbonHome == null) {
            throw new FinancialServicesRuntimeException("Carbon home is not set.");
        }

        // Construct the file path
        Path filePath = Paths.get(carbonHome, "repository", "conf", CONFIG_FILE_NAME);

        // Ensure the file exists before attempting to read
        if (!Files.exists(filePath)) {
            throw new FinancialServicesRuntimeException("Financial Services YAML configuration file not found at: " +
                    filePath);
        }

        try {
            // Read YAML file into a String
            String yamlContent = Files.readString(filePath);
            LoaderOptions options = new LoaderOptions();
            Yaml yaml = new Yaml(options);
            return yaml.load(yamlContent);
        } catch (IOException e) {
            throw new FinancialServicesRuntimeException("Error reading YAML configuration file: " + filePath, e);
        }
    }
}
