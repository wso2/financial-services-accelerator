package org.wso2.financial.services.accelerator.authentication.endpoint.impl.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.util.HTTPClientUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP client for calling the SMS OTP service.
 */
public class OTPAPIClient {

    private static final String GENERATE_URL = "https://localhost:9446/api/identity/sms-otp/v1/smsotp/generate";
    private static final String VERIFY_URL = "https://localhost:9446/api/identity/sms-otp/v1/smsotp/validate";

    public static JSONObject generateOtp(String userId) throws IOException {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);

        CloseableHttpClient client = HTTPClientUtils.getHttpsClient();
        HttpPost post = new HttpPost(GENERATE_URL);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(payload.toString()));

        try (CloseableHttpResponse response = client.execute(post)) {
            String json = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            return new JSONObject(json);
        }
    }

    public static JSONObject verifyOtp(String userId, String transactionId, String otp) throws IOException {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);
        payload.put("transactionId", transactionId);
        payload.put("smsOTP", otp);

        CloseableHttpClient client = HTTPClientUtils.getHttpsClient();
        HttpPost post = new HttpPost(VERIFY_URL);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(payload.toString()));

        try (CloseableHttpResponse response = client.execute(post)) {
            String json = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            return new JSONObject(json);
        }
    }
}
