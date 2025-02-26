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

package org.wso2.financial.services.accelerator.common.json.schema.validator;

import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.Draft;
import io.vertx.json.schema.JsonSchema;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.JsonSchemaValidationException;
import io.vertx.json.schema.OutputFormat;
import io.vertx.json.schema.OutputUnit;
import io.vertx.json.schema.SchemaRepository;
import io.vertx.json.schema.Validator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.exception.SchemaValidationException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


import javax.servlet.http.HttpServletResponse;

/**
 * Json Schema Validator.
 */
public class JsonSchemaValidator {

    private static final Log log = LogFactory.getLog(JsonSchemaValidator.class);
    private static final String BASE_URI = "https://wso2.com/financial-services/schemas";

    private final JsonSchemaOptions jsonSchemaOptions;
    private final SchemaRepository schemaRepository;

    public JsonSchemaValidator() {
        this.jsonSchemaOptions = new JsonSchemaOptions()
                .setBaseUri(BASE_URI)
                .setDraft(Draft.DRAFT202012)
                .setOutputFormat(OutputFormat.Basic);

        this.schemaRepository = SchemaRepository.create(this.jsonSchemaOptions)
                .preloadMetaSchema(Vertx.vertx().fileSystem());
    }

    /**
     * Validates whether the given schema is compliant with the JSON schema DRAFT202012 standard.
     *
     * @param schema the JSON schema as a string.
     * @return true if the schema is valid, false if the schema is invalid or empty.
     * @throws SchemaValidationException if the validation fails or an error occurs during validation.
     */
    public boolean isValidSchema(final String schema) throws SchemaValidationException {

        if (StringUtils.isEmpty(schema)) {
            log.debug("Schema validation failed. Schema cannot be null");
            return false;
        }

        final OutputUnit outputUnit = this.buildOutputUnit(null, this.parseJsonObject(schema));
        try {
            // Validates the schema itself against the DRAFT202012 schema standard
            outputUnit.checkValidity();
        } catch (JsonSchemaValidationException e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Validation failed against DRAFT202012 schema for input: %s. Caused by, ",
                        schema.replaceAll("[\r\n]", "")), e);
            }
            throw new SchemaValidationException(HttpServletResponse.SC_BAD_REQUEST, String.format(
                    FinancialServicesConstants.SCHEMA_VALIDATION_FAILED_ERR_MSG_FORMAT,
                    buildSchemaValidationErrorMessage(outputUnit, e)), e);
        }
        return true;
    }

    /**
     * Validates whether the given json payload complies with the provided JSON schema.
     *
     * @param schema         the JSON schema as a string.
     * @param payload        the payload to be validated.
     * @return true if the authorization is schema compliant, false if schema or authorization is invalid.
     * @throws SchemaValidationException if the validation fails or an error occurs during validation.
     */
    public boolean isSchemaCompliant(final String schema, final JSONObject payload)
            throws SchemaValidationException {

        if (StringUtils.isEmpty(schema) || payload == null) {
            log.debug("Schema validation failed. Inputs cannot be null");
            return false;
        }

        return this.isSchemaCompliant(this.parseJsonObject(schema), payload);
    }

    /**
     * Validates whether the given json payload complies with the provided JSON schema.
     *
     * @param schema         the JSON schema as a JsonObject {@link JsonObject}..
     * @param payload        the payload to be validated.
     * @return true if the authorization is schema compliant, false if schema or authorization is invalid.
     * @throws SchemaValidationException if the validation fails or an error occurs during validation.
     */
    public boolean isSchemaCompliant(final JsonObject schema, final JSONObject payload)
            throws SchemaValidationException {

        if (schema == null || payload == null) {
            log.debug("Schema validation failed. Inputs cannot be null");
            return false;
        }

        final OutputUnit outputUnit =
                this.buildOutputUnit(schema, this.parseJsonObject(payload.toString()));

        try {
            // Validates the authorization detail against the schema
            outputUnit.checkValidity();
        } catch (JsonSchemaValidationException e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Schema validation failed for payload %s. Caused by, ",
                        payload.toString().replaceAll("[\r\n]", "")), e);
            }
            throw new SchemaValidationException(HttpServletResponse.SC_BAD_REQUEST, String.format(
                    FinancialServicesConstants.VALIDATION_FAILED_ERR_MSG_FORMAT,
                   this.buildSchemaValidationErrorMessage(outputUnit, e)), e);
        }
        return true;
    }

    /**
     * Validates whether the given json payload complies with the provided JSON schema.
     * @param jsonSchema    the JSON schema as a JsonObject {@link JsonObject}..
     * @param jsonInput     the payload to be validated.
     * @return OutputUnit  the output unit of the validation.
     */
    private OutputUnit buildOutputUnit(final JsonObject jsonSchema, final JsonObject jsonInput) {

        // Validate the jsonSchema if present, otherwise validate the schema itself against json-schema DRAFT202012
        final Validator validator = (jsonSchema != null)
                ? this.schemaRepository.validator(JsonSchema.of(jsonSchema), this.jsonSchemaOptions)
                : this.schemaRepository.validator(this.jsonSchemaOptions.getDraft().getIdentifier());

        return validator.validate(jsonInput);
    }

    /**
     * Converts a JSON string into a {@link JsonObject}. If the input is invalid, throws an exception.
     *
     * @param jsonString The input JSON string to be converted.
     * @return A {@link JsonObject} created from the input string.
     * @throws SchemaValidationException if the input string is not valid JSON.
     */
    private JsonObject parseJsonObject(final String jsonString) throws SchemaValidationException {

        try {
            return new JsonObject(jsonString);
        } catch (DecodeException e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Failed to parse the JSON input: '%s'. Caused by, ",
                        jsonString.replaceAll("[\r\n]", "")), e);
            }
            throw new SchemaValidationException(HttpServletResponse.SC_BAD_REQUEST,
                    String.format("%s. Invalid JSON input received.",
                            FinancialServicesConstants.VALIDATION_FAILED_ERR_MSG.replaceAll("[\r\n]", "")), e);
        }
    }

    /**
     * Method to build the Schema Validation Error message.
     *
     * @param outputUnit  the output unit of the validation.
     * @param ex          the exception thrown during the validation.
     * @return          the error message.
     */
    private String buildSchemaValidationErrorMessage(final OutputUnit outputUnit,
                                                     final JsonSchemaValidationException ex) {

        // Extract the last validation error if available, otherwise use exception message.
        if (outputUnit == null || CollectionUtils.isEmpty(outputUnit.getErrors())) {
            return ex.getMessage();
        }
        final OutputUnit lastError = outputUnit.getErrors().get(outputUnit.getErrors().size() - 1);
        return lastError.getInstanceLocation() + StringUtils.SPACE + lastError.getError();
    }

    public String getJSonSchemaFromUrl(String url) throws FinancialServicesException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new FinancialServicesException(String.format("Error occurred while fetching JSON schema from URL: %s",
                    url.replaceAll("[\r\n]", "")), e);
        }
    }
}
