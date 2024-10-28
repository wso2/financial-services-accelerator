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

package com.wso2.openbanking.accelerator.gateway.executor.impl.tpp.validation.executor;

import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.exception.TPPValidationException;
import com.wso2.openbanking.accelerator.common.model.PSD2RoleEnum;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContent;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContentExtractor;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.executor.service.CertValidationService;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * TPP validation handler used to validate the TPP status using external validation services
 * for DCR API requests.
 */
@Deprecated
public class DCRTPPValidationExecutor implements OpenBankingGatewayExecutor {

    private static final String BODY = "body";
    private static final String GET_METHOD_TYPE = "GET";
    private static final String DELETE_METHOD_TYPE = "DELETE";
    private static final String SOFTWARE_ROLES = "software_roles";
    private static final String SOFTWARE_STATEMENT = "software_statement";

    private static final Log log = LogFactory.getLog(DCRTPPValidationExecutor.class);

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {
        // Skip the executor if previous executors failed.
        if (obapiRequestContext.isError()) {
            return;
        }

        try {
            Certificate[] clientCerts = obapiRequestContext.getClientCertsLatest();
            if (clientCerts != null && clientCerts.length > 0) {
                Optional<X509Certificate> transportCert =
                        CertificateValidationUtils.convertCertToX509Cert(clientCerts[0]);

                // Only Do Validation if Mutual TLS is used.
                if (transportCert.isPresent()) {

                    String httpMethod = obapiRequestContext.getMsgInfo().getHttpMethod();

                    // During DCR request, skip validation if the method is GET or DELETE as the application roles
                    // cannot be updated through GET or DELETE.
                    // Since there is no SSA during these calls, we cannot find the applicable roles as well.
                    if (GET_METHOD_TYPE.equals(httpMethod) || DELETE_METHOD_TYPE.equals(httpMethod)) {
                        return;
                    }

                    CertificateContent certContent = CertificateContentExtractor.extract(transportCert.get());
                    /* Getting PSD2 roles here because UK SSA contains the roles in AISP, PISP, CBPII, etc format.
                       This class will be moved to the UK Toolkit in the future. */
                    List<String> certRoles = certContent.getPsd2Roles();
                    String softwareStatement = getSSAFromPayload(obapiRequestContext.getRequestPayload());
                    List<PSD2RoleEnum> requiredPSD2Roles = getRolesFromSSA(softwareStatement);

                    if (requiredPSD2Roles.isEmpty()) {
                        throw new TPPValidationException("No roles found associated with the request. Hence, cannot " +
                                "continue with TPP validation");
                    }

                    if (CertValidationService.getInstance().validateTppRoles(transportCert.get(), requiredPSD2Roles,
                            certRoles)) {
                        log.debug("TPP validation service returned a success response");
                    } else {
                        log.error("TPP validation service returned invalid TPP status");
                        throw new TPPValidationException("TPP validation service returned invalid TPP status");
                    }
                } // cert validation executor validates the certificate validity
            } // enforcement executor validates the certificate presence
        } catch (TPPValidationException | CertificateValidationException | ParseException e) {
            final String errorMsg = "Error occurred while validating the TPP status ";
            log.error(errorMsg, e);

            //catch errors and set to context
            OpenBankingExecutorError error = new OpenBankingExecutorError(
                    OpenBankingErrorCodes.TPP_VALIDATION_FAILED_CODE,
                    e.getMessage(), errorMsg, OpenBankingErrorCodes.FORBIDDEN_CODE);
            CertificateValidationUtils.handleExecutorErrors(error, obapiRequestContext);
        } catch (CertificateException e) {
            String errorMsg = "Error occurred while converting the client certificate to X509Certificate ";
            log.error(errorMsg, e);
            OpenBankingExecutorError error = new OpenBankingExecutorError(
                    OpenBankingErrorCodes.TPP_VALIDATION_FAILED_CODE, errorMsg,
                    e.getMessage(), OpenBankingErrorCodes.FORBIDDEN_CODE);
            CertificateValidationUtils.handleExecutorErrors(error, obapiRequestContext);
        }
    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {
        // Do not need to handle the response
    }

    /**
     * Method to handle post response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Method to handle post request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

    }

    private String getSSAFromPayload(String requestPayload) throws ParseException {
        // decode request body and get payload
        JSONObject requestBody = JWTUtils.decodeRequestJWT(requestPayload, BODY);
        // extract software statement
        return requestBody.getAsString(SOFTWARE_STATEMENT);
    }

    /**
     * Extract PSD2 roles from SSA.
     *
     * @param softwareStatement software statement extracted from request payload
     * @return list of PSD2RoleEnum
     * @throws TPPValidationException when an error occurs when generating PSD2 roles list
     */
    public List<PSD2RoleEnum> getRolesFromSSA(String softwareStatement) throws TPPValidationException {

        List<PSD2RoleEnum> requiredPSD2Roles = new ArrayList<>();
        try {
            // decode software statement and get payload
            JSONObject softwareStatementBody = JWTUtils.decodeRequestJWT(softwareStatement, BODY);

            String softwareRolesStr = softwareStatementBody.getAsString(SOFTWARE_ROLES);

            if (StringUtils.isNotBlank(softwareRolesStr) && softwareRolesStr.contains("[")) {
                JSONArray softwareRoles = (JSONArray) JSONValue.parseStrict(softwareRolesStr);

                softwareRoles.stream()
                        .map(softwareRole -> PSD2RoleEnum.fromValue((String) softwareRole))
                        .filter(Objects::nonNull)
                        .forEach(requiredPSD2Roles::add);
            } else {
                log.error("Invalid SSA software roles received. Expected array of software roles. Received: "
                        + softwareRolesStr);
            }
        } catch (net.minidev.json.parser.ParseException | ParseException e) {
            log.error("Error while parsing the message to json", e);
            throw new TPPValidationException("Error while parsing the message to json", e);

        }

        return requiredPSD2Roles;
    }
}
