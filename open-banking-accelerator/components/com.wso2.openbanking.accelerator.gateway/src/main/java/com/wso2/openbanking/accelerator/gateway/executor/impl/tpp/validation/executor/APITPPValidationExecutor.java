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
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContent;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContentExtractor;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.executor.service.CertValidationService;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * TPP validation handler used to validate the TPP status using external validation
 * services for regular API requests.
 */
public class APITPPValidationExecutor implements OpenBankingGatewayExecutor {

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String PATCH = "PATCH";
    private static final String DELETE = "DELETE";
    private static final Log log = LogFactory.getLog(APITPPValidationExecutor.class);

    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

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
    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {
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

                    // extracting scopes from api swagger
                    final PathItem electedPath = obapiRequestContext.getOpenAPI().getPaths()
                            .get(obapiRequestContext.getMsgInfo().getElectedResource());
                    final String httpMethod = obapiRequestContext.getMsgInfo().getHttpMethod();

                    final Set<String> scopes = extractScopesFromSwaggerAPI(electedPath, httpMethod);

                    // retrieving allowed scopes from open-banking.xml
                    final Map<String, List<String>> allowedScopes = GatewayDataHolder.getInstance()
                            .getOpenBankingConfigurationService().getAllowedScopes();

                    CertificateContent certContent = CertificateContentExtractor.extract(transportCert.get());
                    List<String> certRoles = certContent.getPspRoles();
                    List<PSD2RoleEnum> requiredPSD2Roles = getRolesFromScopes(allowedScopes, scopes);

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
        } catch (TPPValidationException | CertificateValidationException e) {
            final String errorMsg = "Error occurred while validating the TPP status ";
            log.error(errorMsg, e);

            //catch errors and set to context
            OpenBankingExecutorError error = new OpenBankingExecutorError(
                    OpenBankingErrorCodes.TPP_VALIDATION_FAILED_CODE,
                    errorMsg, e.getMessage(), OpenBankingErrorCodes.FORBIDDEN_CODE);
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

    private List<PSD2RoleEnum> getRolesFromScopes(Map<String, List<String>> allowedScopes, Set<String> scopes) {
        List<PSD2RoleEnum> requiredPSD2Roles = new ArrayList<>();

        Set<String> distinctRoles = new HashSet<>();

        for (String scope : scopes) {
            for (Map.Entry<String, List<String>> allowedScopeEntry : allowedScopes.entrySet()) {
                if (scope.equalsIgnoreCase(allowedScopeEntry.getKey())) {
                    distinctRoles.addAll(allowedScopeEntry.getValue());
                }
            }

        }

        for (String distinctRole : distinctRoles) {
            requiredPSD2Roles.add(PSD2RoleEnum.fromValue(distinctRole));
        }

        return requiredPSD2Roles;
    }

    private Set<String> extractScopesFromSwaggerAPI(PathItem electedPath, String httpMethod) {

        List<SecurityRequirement> securityRequirements = null;
        Set<String> scopes = new HashSet<>();

        if (GET.equalsIgnoreCase(httpMethod)) {
            securityRequirements = electedPath.getGet().getSecurity();
        } else if (POST.equalsIgnoreCase(httpMethod)) {
            securityRequirements = electedPath.getPost().getSecurity();
        } else if (PUT.equalsIgnoreCase(httpMethod)) {
            securityRequirements = electedPath.getPut().getSecurity();
        } else if (PATCH.equalsIgnoreCase(httpMethod)) {
            securityRequirements = electedPath.getPatch().getSecurity();
        } else if (DELETE.equalsIgnoreCase(httpMethod)) {
            securityRequirements = electedPath.getDelete().getSecurity();
        }

        if (securityRequirements != null) {
            for (SecurityRequirement securityRequirement : securityRequirements) {
                for (Map.Entry<String, List<String>> securityRequirementEntry : securityRequirement.entrySet()) {
                    scopes.addAll(securityRequirementEntry.getValue());
                }
            }
        }
        return scopes;
    }

}
