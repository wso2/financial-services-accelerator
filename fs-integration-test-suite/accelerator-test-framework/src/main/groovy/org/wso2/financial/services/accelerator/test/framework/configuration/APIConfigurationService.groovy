/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.test.framework.configuration

import org.wso2.bfsi.test.framework.configuration.CommonConfigurationService
import org.yaml.snakeyaml.Yaml

/**
 * Class for provide configuration data to the API publishing steps.
 */
class APIConfigurationService extends CommonConfigurationService {

    public Map<String, Map<String, Map<String, String>>> readProvisioningConfigs() {

        InputStream input = new FileInputStream(new File(getProvisionFilePath()))
        Yaml yaml = new Yaml()
        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = (Map<String, Map<String, Map<String, String>>>) yaml.load(input)

        return lstYamlConfigs
    }

    /**
     * Get API File Path.
     */
    ArrayList<String> getApiFilePath() {
        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = readProvisioningConfigs()
        ArrayList<String> apiFilePaths = lstYamlConfigs.get("apis").getAt("apiFilePath")
        return apiFilePaths
    }

    /**
     * Get In Sequence File Path.
     */
    public ArrayList<String> getSequenceFilePath() {

        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = readProvisioningConfigs()
        ArrayList<String> sequenceFilePaths = lstYamlConfigs.get("apis").getAt("sequenceFilePath")
        return sequenceFilePaths
    }

    /**
     * Get API Name.
     */
    public ArrayList<String> getApiName() {

        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = readProvisioningConfigs()
        ArrayList<String> apiProperty = lstYamlConfigs.get("apis").getAt("apiName")
        return apiProperty
    }

    /**
     * Get API Context.
     */
    public ArrayList<String> getApiContext() {

        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = readProvisioningConfigs()
        ArrayList<String> apiProperty = lstYamlConfigs.get("apis").getAt("context")
        return apiProperty
    }

    /**
     * Get API Properties.
     */
    public ArrayList<String> getApiProperty() {

        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = readProvisioningConfigs()
        ArrayList<String> apiProperty = lstYamlConfigs.get("apis").getAt("apiProperty")
        return apiProperty
    }

    /**
     * Get API Endpoint Type.
     */
    public ArrayList<String> getApiEndpointType() {

        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = readProvisioningConfigs()
        ArrayList<String> apiProperty = lstYamlConfigs.get("apis").getAt("endpointType")
        return apiProperty
    }

    /**
     * Get Schema Validation Enabled Property.
     */
    public ArrayList<String> getEnableSchemaValidation() {

        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = readProvisioningConfigs()
        ArrayList<String> apiProperty = lstYamlConfigs.get("apis").getAt("enableSchemaValidation")
        return apiProperty
    }

    /**
     * Get Sandbox Endpoint.
     */
    public ArrayList<String> getSandboxEndpoint() {

        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = readProvisioningConfigs()
        ArrayList<String> apiProperty = lstYamlConfigs.get("apis").getAt("sandbox_endpoints")
        return apiProperty
    }

    /**
     * Get Production Endpoint.
     */
    public ArrayList<String> getProductionEndpoint() {

        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = readProvisioningConfigs()
        ArrayList<String> apiProperty = lstYamlConfigs.get("apis").getAt("production_endpoints")
        return apiProperty
    }

    /**
     * Policy File Path
     * @return
     */
    public ArrayList<String> getPolicyFilePath() {

        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = readProvisioningConfigs()
        ArrayList<String> apiProperty = lstYamlConfigs.get("policy").getAt("policyFilePath")
        return apiProperty
    }

    /**
     * Get Policy Name
     * @return
     */
    public ArrayList<String> getPolicyName() {

        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = readProvisioningConfigs()
        ArrayList<String> apiProperty = lstYamlConfigs.get("policy").getAt("policyName")
        return apiProperty
    }

    /**
     * Get Policy Attributes
     * @return
     */
    public ArrayList<String> getPolicyAttribute() {

        Map<String, Map<String, Map<String, String>>> lstYamlConfigs = readProvisioningConfigs()
        ArrayList<String> apiProperty = lstYamlConfigs.get("policy").getAt("policyAttribute")
        return apiProperty
    }

    /**
     * Get Policy List
     * @return
     */
    public List<Map> getPolicyList() {

        Map<String, Object> lstYamlConfigs = readProvisioningConfigs()
        return (List<Map>) lstYamlConfigs.get("policy")
    }

    /**
     * Get API List
     * @return
     */
    public List<Map> getApiList() {

        Map<String, Object> lstYamlConfigs = readProvisioningConfigs()
        return (List<Map>) lstYamlConfigs.get("apis")
    }
}
