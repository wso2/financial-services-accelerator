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
package org.wso2.financial.services.accelerator.authentication.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.smsotp.common.SMSOTPServiceImpl;
import org.wso2.carbon.identity.smsotp.common.dto.FailureReasonDTO;
import org.wso2.carbon.identity.smsotp.common.dto.GenerationResponseDTO;
import org.wso2.carbon.identity.smsotp.common.dto.ValidationResponseDTO;
import org.wso2.carbon.identity.smsotp.common.exception.SMSOTPException;
import org.wso2.financial.services.accelerator.authentication.endpoint.util.Constants;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * FSVerifyServlet handles OTP generation and verification for user consent.
 * Flow:
 * - POST with otpAction=init: saves form parameters in session as pendingConsent (removes otpAction), calls
 *   placeholder SMS-OTP generate API, stores an otpToken in session, forwards to consent_smsotp.jsp
 *
 * - POST with otpAction=verify: verifies the provided otp against the placeholder API, if ok forwards to
 *   submit_consent.jsp (which posts to /oauth2_authz_confirm.do), otherwise redisplay consent_smsotp.jsp with error
 */
public class FSVerifyServlet extends HttpServlet {

    private static final String SESSION_PENDING_CONSENT = "pendingConsent";
    private static final String SESSION_OTP_TRANSACTION_ID = "otpTransactionId";
    private static final String SESSION_USER_ID = "userId";
    private static final String SESSION_OTP_VERIFIED = "otpVerified";
    private static final String OTP_ACTION = "otpAction";
    private static final String OTP_INIT = "init";
    private static final String OTP_VERIFY = "verify";
    private static final String CONSENT_SMSOTP_PAGE = "/consent_smsotp.jsp";
    private static final String SUBMIT_CONSENT_PAGE = "/submit_consent.jsp";
    private static Logger log = LoggerFactory.getLogger(FSVerifyServlet.class);
    private static SMSOTPServiceImpl smsotpService = new SMSOTPServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String otpAction = req.getParameter(OTP_ACTION);
        if (otpAction == null) {
            otpAction = OTP_INIT;
        }

        if (OTP_INIT.equalsIgnoreCase(otpAction)) {
            handleInit(req, resp);
        } else if (OTP_VERIFY.equalsIgnoreCase(otpAction)) {
            handleVerify(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown otpAction");
        }
    }

    /**
     * Handle the init action: store pending consent in session, call OTP generate API, forward to consent_smsotp.jsp.
     *
     * @param req - HttpServletRequest
     * @param resp - HttpServletResponse
     * @throws ServletException - ServletException
     * @throws IOException - IOException
     */
    private void handleInit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();

        /* Collect all parameters from the form and store in a map as the pending consent.
           This will be submitted if the otp verification is successful.*/
        Map<String, List<String>> params = new HashMap<>();
        req.getParameterMap().forEach((k, v) -> params.put(k, Arrays.asList(v)));
        params.remove(OTP_ACTION);
        session.setAttribute(SESSION_PENDING_CONSENT, params);

        // Get userId (SCIM ID) from HTTP session (stored by FSConsentServlet from consent retrieval response)
        String userIdFromSession = (String) session.getAttribute(Constants.USER_ID);
        if (userIdFromSession == null || userIdFromSession.isEmpty()) {
            req.setAttribute("otpError", "Unable to resolve user identifier. Please start over.");
            req.getRequestDispatcher(CONSENT_SMSOTP_PAGE).forward(req, resp);
            return;
        }

        // Extract SCIM ID from the format "scimId@tenant.domain"
        String userId = userIdFromSession;
        if (userIdFromSession.contains("@")) {
            userId = userIdFromSession.substring(0, userIdFromSession.indexOf("@"));
        }
        session.setAttribute(SESSION_USER_ID, userId);

        try {

            GenerationResponseDTO otpResponse = smsotpService.generateSMSOTP(userId);
            String transactionId = otpResponse.getTransactionId();
            session.setAttribute(SESSION_OTP_TRANSACTION_ID, transactionId);
            req.getRequestDispatcher(CONSENT_SMSOTP_PAGE).forward(req, resp);
        } catch (IOException | SMSOTPException e) {
            req.setAttribute("otpError", "Error generating OTP. Please try again later.");
            req.getRequestDispatcher(CONSENT_SMSOTP_PAGE).forward(req, resp);
        }
    }

    private void handleVerify(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            req.setAttribute("otpError", "Session expired. Please start over.");
            req.getRequestDispatcher(CONSENT_SMSOTP_PAGE).forward(req, resp);
            return;
        }

        String userId = (String) session.getAttribute(SESSION_USER_ID);
        String transactionId = (String) session.getAttribute(SESSION_OTP_TRANSACTION_ID);
        String providedOtp = req.getParameter("smsOtp");

        if (userId == null || transactionId == null) {
            req.setAttribute("otpError", "No pending OTP transaction found. Please start over.");
            req.getRequestDispatcher(CONSENT_SMSOTP_PAGE).forward(req, resp);
            return;
        }

        try {
            ValidationResponseDTO validationResponse = smsotpService.validateSMSOTP(transactionId, userId, providedOtp);
            if (validationResponse.isValid()) {
                session.removeAttribute(SESSION_OTP_TRANSACTION_ID);
                session.removeAttribute(SESSION_USER_ID);
                // Set flag indicating OTP was successfully verified
                session.setAttribute(SESSION_OTP_VERIFIED, Boolean.TRUE);
                req.getRequestDispatcher(SUBMIT_CONSENT_PAGE).forward(req, resp);
            } else {
                FailureReasonDTO failureReason = validationResponse.getFailureReason();
                String message = "Invalid OTP. Please try again.";
                if (failureReason != null) {
                    log.error("OTP verification failed: {} - {} - {}",
                            failureReason.getCode(), failureReason.getMessage(), failureReason.getDescription());
                    message = failureReason.getMessage();
                }
                req.setAttribute("otpError", message);
                req.getRequestDispatcher(CONSENT_SMSOTP_PAGE).forward(req, resp);
            }
        } catch (IOException | SMSOTPException e) {
            // Log the error
            req.setAttribute("otpError", "Error verifying OTP. Please try again later.");
            req.getRequestDispatcher(CONSENT_SMSOTP_PAGE).forward(req, resp);
        }
    }
}
