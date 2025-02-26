package org.wso2.financial.services.accelerator.consent.mgt.extensions.common;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.exception.SchemaValidationException;
import org.wso2.financial.services.accelerator.common.json.schema.validator.JsonSchemaValidator;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * Consent schema validator.
 */
public class ConsentSchemaValidator {

    private static final Log log = LogFactory.getLog(ConsentSchemaValidator.class);

    public static void validateRequest(ServletRequest servletRequest, ServletResponse servletResponse) {

        JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator();
        log.info("ConsentAPISchemaValidationFilter filter is called");

        String url = "https://gist.githubusercontent.com/Ashi1993/108436ac035b06e2e35604b66791f656" +
                "/raw/4bd9d25fda6b2e9f634fb5ec882cca91c46fac9b/account-json-schema.json";

        String jsonSchema = null;
        try {
            jsonSchema = jsonSchemaValidator.getJSonSchemaFromUrl(url);
            boolean isValidJsonSchema = jsonSchemaValidator.isValidSchema(jsonSchema);
            if (!isValidJsonSchema) {
                log.error("Provided schema is not a valid schema");
                handleValidationFailure((HttpServletResponse) servletResponse, HttpServletResponse.SC_BAD_REQUEST,
                        "invalid_schema", "Provided schema is not a valid schema");
                return;
            }
        } catch (SchemaValidationException e) {
            log.error(e.getMessage().replaceAll("[\n\r]", ""));
            handleValidationFailure((HttpServletResponse) servletResponse, e.getErrorCode(),
                    "invalid_schema", e.getMessage());
            return;
        } catch (FinancialServicesException e) {
            log.error(e.getMessage().replaceAll("[\n\r]", ""));
            handleValidationFailure((HttpServletResponse) servletResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "invalid_schema", e.getMessage());
            return;
        }

        try {
            String payload = getStringPayload((HttpServletRequest) servletRequest);
            JSONObject payloadObj = new JSONObject(payload);
            boolean isSchemaCompliant = jsonSchemaValidator.isSchemaCompliant(jsonSchema, payloadObj);
            if (!isSchemaCompliant) {
                log.error("Payload is not compliant with the schema");
                handleValidationFailure((HttpServletResponse) servletResponse, HttpServletResponse.SC_BAD_REQUEST,
                        "schema_validation_failure", "Payload is not compliant with the schema");
                return;
            }
        } catch (FinancialServicesException | JSONException e) {
            log.error(e.getMessage().replaceAll("[\n\r]", ""));
            handleValidationFailure((HttpServletResponse) servletResponse,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "schema_validation_failure", e.getMessage());
            return;
        } catch (SchemaValidationException e) {
            log.error(e.getMessage());
            handleValidationFailure((HttpServletResponse) servletResponse, e.getErrorCode(),
                    "schema_validation_failure", e.getMessage());
            return;
        }
    }

    /**
     * Extract string payload from request object.
     *
     * @param request The request object
     * @return String payload
     * @throws FinancialServicesException Payload read errors
     */
    private static String getStringPayload(HttpServletRequest request) throws FinancialServicesException {
        try {
            return IOUtils.toString(request.getInputStream());
        } catch (IOException e) {
            log.error("Error while extracting the payload", e);
            throw new FinancialServicesException("Error while extracting the payload", e);
        }
    }

    protected static void handleValidationFailure(HttpServletResponse response, int status,
                                                  String error, String errorMessage) {

        JSONObject errorJSON = new JSONObject();
        errorJSON.put(FinancialServicesConstants.OAUTH_ERROR, error);
        errorJSON.put(FinancialServicesConstants.OAUTH_ERROR_DESCRIPTION, errorMessage);

        try (OutputStream outputStream = response.getOutputStream()) {
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON);
            outputStream.write(errorJSON.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Error while writing the error response", e);
        }
    }
}
