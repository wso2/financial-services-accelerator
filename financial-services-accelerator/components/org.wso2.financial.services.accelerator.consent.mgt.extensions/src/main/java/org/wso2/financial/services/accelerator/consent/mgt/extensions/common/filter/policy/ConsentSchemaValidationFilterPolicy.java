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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.common.filter.policy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.exception.SchemaValidationException;
import org.wso2.financial.services.accelerator.common.json.schema.validator.JsonSchemaValidator;
import org.wso2.financial.services.accelerator.common.policy.FSPolicyExecutionException;
import org.wso2.financial.services.accelerator.common.policy.filter.FSFilterPolicy;
import org.wso2.financial.services.accelerator.common.policy.utils.FilterPolicyUtils;

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Consent schema validator.
 */
public class ConsentSchemaValidationFilterPolicy extends FSFilterPolicy {

    private static final Log log = LogFactory.getLog(ConsentSchemaValidationFilterPolicy.class);

    @Override
    public void processRequest(ServletRequest servletRequest, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

        JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator();
        log.info("Executing ConsentSchemaValidationFilterPolicy");

        String url = null;
        if (propertyMap.get("json_schema_url") instanceof String) {
            url = (String) propertyMap.get("json_schema_url");
        } else {
            throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST,
                    "invalid_schema", "Only one schema is accepted");
        }

        String jsonSchema = null;
        try {
            jsonSchema = jsonSchemaValidator.getJSonSchemaFromUrl(url);
            boolean isValidJsonSchema = jsonSchemaValidator.isValidSchema(jsonSchema);
            if (!isValidJsonSchema) {
                log.error("Provided schema is not a valid schema");
                throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST,
                        "invalid_schema", "Provided schema is not a valid schema");
            }
        } catch (SchemaValidationException e) {
            log.error(e.getMessage().replaceAll("[\n\r]", ""));
            throw new FSPolicyExecutionException(e.getErrorCode(), "invalid_schema", e.getMessage(), e);
        } catch (FinancialServicesException e) {
            log.error(e.getMessage().replaceAll("[\n\r]", ""));
            throw new FSPolicyExecutionException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "invalid_schema", e.getMessage(), e);
        }

        try {
            String payload = FilterPolicyUtils.getStringPayload((HttpServletRequest) servletRequest);
            JSONObject payloadObj = new JSONObject(payload);
            boolean isSchemaCompliant = jsonSchemaValidator.isSchemaCompliant(jsonSchema, payloadObj);
            if (!isSchemaCompliant) {
                log.error("Payload is not compliant with the schema");
                throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST,
                        "schema_validation_failure", "Payload is not compliant with the schema");
            }
        } catch (FinancialServicesException | JSONException e) {
            log.error(e.getMessage().replaceAll("[\n\r]", ""));
            throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST,
                    "schema_validation_failure", e.getMessage(), e);
        } catch (SchemaValidationException e) {
            log.error(e.getMessage().replaceAll("[\n\r]", ""));
            throw new FSPolicyExecutionException(e.getErrorCode(), "schema_validation_failure", e.getMessage(), e);
        }
    }

    @Override
    public void processResponse(ServletResponse servletResponse, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

    }
}
