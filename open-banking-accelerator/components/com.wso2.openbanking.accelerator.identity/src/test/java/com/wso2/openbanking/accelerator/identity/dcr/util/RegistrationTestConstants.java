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
package com.wso2.openbanking.accelerator.identity.dcr.util;

/**
 * Registration test constants.
 */
public class RegistrationTestConstants {

    public static final String SSA = "eyJhbGciOiJQUzI1NiIsImtpZCI6IkR3TUtkV01tajdQV2ludm9xZlF5WFZ6eVo2USIs" +
            "InR5cCI6IkpXVCJ9.eyJpc3MiOiJPcGVuQmFua2luZyBMdGQiLCJpYXQiOjE1OTIzNjQ1NjgsImp0aSI6IjNkMWIzNTk1ZWZh" +
            "YzRlMzYiLCJzb2Z0d2FyZV9lbnZpcm9ubWVudCI6InNhbmRib3giLCJzb2Z0d2FyZV9tb2RlIjoiVGVzdCIsInNvZnR3YXJlX2" +
            "lkIjoiOWI1dXNEcGJOdG14RGNUenM3R3pLcCIsInNvZnR3YXJlX2NsaWVudF9pZCI6IjliNXVzRHBiTnRteERjVHpzN0d6S3AiLC" +
            "Jzb2Z0d2FyZV9jbGllbnRfbmFtZSI6IlRlc3QgQVBQIE5ldyIsInNvZnR3YXJlX2NsaWVudF9kZXNjcmlwdGlvbiI6IlRoaXMgVFBQ" +
            "IElzIGNyZWF0ZWQgZm9yIHRlc3RpbmcgcHVycG9zZXMuICIsInNvZnR3YXJlX3ZlcnNpb24iOjEuNSwic29mdHdhcmVfY2xpZW50X3V" +
            "yaSI6Imh0dHBzOi8vd3NvMi5jb20iLCJzb2Z0d2FyZV9yZWRpcmVjdF91cmlzIjpbImh0dHBzOi8vd3NvMi5jb20iXSwic29mdHdhcmV" +
            "fcm9sZXMiOlsiQUlTUCIsIlBJU1AiXSwib3JnYW5pc2F0aW9uX2NvbXBldGVudF9hdXRob3JpdHlfY2xhaW1zIjp7ImF1dGhvcml0eV" +
            "9pZCI6Ik9CR0JSIiwicmVnaXN0cmF0aW9uX2lkIjoiVW5rbm93bjAwMTU4MDAwMDFIUVFyWkFBWCIsInN0YXR1cyI6IkFjdGl2ZSIsI" +
            "mF1dGhvcmlzYXRpb25zIjpbeyJtZW1iZXJfc3RhdGUiOiJHQiIsInJvbGVzIjpbIkFJU1AiLCJQSVNQIl19LHsibWVtYmVyX3N0YXRl" +
            "IjoiSUUiLCJyb2xlcyI6WyJBSVNQIiwiUElTUCJdfSx7Im1lbWJlcl9zdGF0ZSI6Ik5MIiwicm9sZXMiOlsiQUlTUCIsIlBJU1AiXX1" +
            "dfSwic29mdHdhcmVfbG9nb191cmkiOiJodHRwczovL3dzbzIuY29tL3dzbzIuanBnIiwib3JnX3N0YXR1cyI6IkFjdGl2ZSIsIm9yZ1" +
            "9pZCI6IjAwMTU4MDAwMDFIUVFyWkFBWCIsIm9yZ19uYW1lIjoiV1NPMiAoVUspIExJTUlURUQiLCJvcmdfY29udGFjdHMiOlt7Im5hb" +
            "WUiOiJUZWNobmljYWwiLCJlbWFpbCI6InNhY2hpbmlzQHdzbzIuY29tIiwicGhvbmUiOiIrOTQ3NzQyNzQzNzQiLCJ0eXBlIjoiVGVj" +
            "aG5pY2FsIn0seyJuYW1lIjoiQnVzaW5lc3MiLCJlbWFpbCI6InNhY2hpbmlzQHdzbzIuY29tIiwicGhvbmUiOiIrOTQ3NzQyNzQzNzQ" +
            "iLCJ0eXBlIjoiQnVzaW5lc3MifV0sIm9yZ19qd2tzX2VuZHBvaW50IjoiaHR0cHM6Ly9rZXlzdG9yZS5vcGVuYmFua2luZ3Rlc3Qub3" +
            "JnLnVrLzAwMTU4MDAwMDFIUVFyWkFBWC8wMDE1ODAwMDAxSFFRclpBQVguandrcyIsIm9yZ19qd2tzX3Jldm9rZWRfZW5kcG9pbnQiO" +
            "iJodHRwczovL2tleXN0b3JlLm9wZW5iYW5raW5ndGVzdC5vcmcudWsvMDAxNTgwMDAwMUhRUXJaQUFYL3Jldm9rZWQvMDAxNTgwMDAw" +
            "MUhRUXJaQUFYLmp3a3MiLCJzb2Z0d2FyZV9qd2tzX2VuZHBvaW50IjoiaHR0cHM6Ly9rZXlzdG9yZS5vcGVuYmFua2luZ3Rlc3Qub3J" +
            "nLnVrLzAwMTU4MDAwMDFIUVFyWkFBWC85YjV1c0RwYk50bXhEY1R6czdHektwLmp3a3MiLCJzb2Z0d2FyZV9qd2tzX3Jldm9rZWRfZW5" +
            "kcG9pbnQiOiJodHRwczovL2tleXN0b3JlLm9wZW5iYW5raW5ndGVzdC5vcmcudWsvMDAxNTgwMDAwMUhRUXJaQUFYL3Jldm9rZWQvOW" +
            "I1dXNEcGJOdG14RGNUenM3R3pLcC5qd2tzIiwic29mdHdhcmVfcG9saWN5X3VyaSI6Imh0dHBzOi8vd3NvMi5jb20iLCJzb2Z0d2FyZ" +
            "V90b3NfdXJpIjoiaHR0cHM6Ly93c28yLmNvbSIsInNvZnR3YXJlX29uX2JlaGFsZl9vZl9vcmciOiJXU08yIE9wZW4gQmFua2luZyJ9" +
            ".mkbNeMGPNPEGqZbm06__7rWG9RWeEZ8MKgdLZGPkF0HMXX6MoPrw3e5ymZ_kxtVe5cRM2IVFThN1VBSuafThMH0PYwwRY2_3NApUWa" +
            "f6BExL34Sbq_plmz8Ciq2zXYiYWPq2ReS1aPSJ-67nRF8Dnap5QLhqmowIDcGz1byTe2mukFc6CmBmwTBeDC_px56i4_n5xHXtrVBIf" +
            "jFYcv2VewJ7K050JMmdIvdODafGei61JQIDrRUT_w0yU4-8WG9IDBI7G4H_GCPWmckJFApZyCnIWeBaEmfe6l2_GQs9VkQq1U1xJXtd" +
            "WAfrzEjbMMnZSvqdoQAISq0y6mQofA0n5g";

    public static final String SSA_WITHOUT_MANDATORY_PARAM = "eyJ0eXAiOiJKV1QiLCJhbGciOiJQUzI1NiIsImtpZCI6ImNJWW8tN" +
            "XpYNE9UV1pwSHJtbWlaRFZ4QUNKTSJ9.eyJpYXQiOjE3NDQyODQyNDMsIm5iZiI6MTc0NDI4NDIzOSwiZXhwIjoxNzQ0Mjg3ODQ0LCJ" +
            "qdGkiOiIyNDdlNjdmNjBmODA0YTk5MTc0NDI4NDI0MyIsImlzcyI6Ik9wZW5CYW5raW5nIEx0ZCIsInNvZnR3YXJlX2Vudmlyb25tZW" +
            "50Ijoic2FuZGJveHgiLCJzb2Z0d2FyZV9tb2RlIjoiVGVzdCIsInNvZnR3YXJlX2lkIjoiMTJaekZGQnhTTEdFalBab2dSQWJ2RmRzM" +
            "TE3NDQyODQyNDMiLCJzb2Z0d2FyZV9jbGllbnRfaWQiOiIxMFp6RkZCeFNMR0VqUFpvZ1JBYnZGZHMxIiwic29mdHdhcmVfY2xpZW50" +
            "X25hbWUiOiJXU08yIE9wZW4gQmFua2luZyBUUFAgKFNhbmRib3gpIiwic29mdHdhcmVfY2xpZW50X2Rlc2NyaXB0aW9uIjoiV1NPMiB" +
            "PcGVuIEJhbmtpbmcgVFBQIGZvciB0ZXN0aW5nIiwic29mdHdhcmVfdmVyc2lvbiI6MS41LCJzb2Z0d2FyZV9jbGllbnRfdXJpIjoiaH" +
            "R0cHM6Ly93d3cuZ29vZ2xlLmNvbSIsInNvZnR3YXJlX3JlZGlyZWN0X3VyaXMiOlsiaHR0cHM6Ly93d3cuZ29vZ2xlLmNvbS9yZWRpc" +
            "mVjdHMvcmVkaXJlY3QxIl0sInNvZnR3YXJlX3JvbGVzIjpbIlBJU1AiLCJBSVNQIiwiQ0JQSUkiXSwidGVzdF9wYXJhbTEiOiIiLCJ0" +
            "ZXN0X3BhcmFtMiI6bnVsbCwidGVzdF9wYXJhbTMiOjAsInRlc3RfcGFyYW00Ijp7fSwidGVzdF9wYXJhbTUiOltdLCJsb2dvX3VyaSI" +
            "6InRlc3QiLCJqd2tzX3VyaSI6InRlc3QiLCJzY29wZSI6InRlc3QiLCJvcmdhbmlzYXRpb25fY29tcGV0ZW50X2F1dGhvcml0eV9jbG" +
            "FpbXMiOnsiYXV0aG9yaXR5X2lkIjoiT0JHQlIiLCJyZWdpc3RyYXRpb25faWQiOiJVbmtub3duMDAxNTgwMDAwMUhRUXJaQUFYIiwic" +
            "3RhdHVzIjoiQWN0aXZlIiwiYXV0aG9yaXNhdGlvbnMiOlt7Im1lbWJlcl9zdGF0ZSI6IkdCIiwicm9sZXMiOlsiUElTUCIsIkFJU1Ai" +
            "LCJDQlBJSSJdfSx7Im1lbWJlcl9zdGF0ZSI6IklFIiwicm9sZXMiOlsiUElTUCIsIkNCUElJIiwiQUlTUCJdfSx7Im1lbWJlcl9zdGF" +
            "0ZSI6Ik5MIiwicm9sZXMiOlsiUElTUCIsIkFJU1AiLCJDQlBJSSJdfV19LCJzb2Z0d2FyZV9sb2dvX3VyaSI6Imh0dHBzOi8vd3d3Lm" +
            "dvb2dsZS5jb20iLCJvcmdfc3RhdHVzIjoiQWN0aXZlIiwib3JnX2lkIjoiMDAxNTgwMDAwMUhRUXJaQUFYIiwib3JnX25hbWUiOiJXU" +
            "08yIChVSykgTElNSVRFRCIsIm9yZ19jb250YWN0cyI6W3sibmFtZSI6IlRlY2huaWNhbCIsImVtYWlsIjoic2FjaGluaXNAd3NvMi5j" +
            "b20iLCJwaG9uZSI6Iis5NDc3NDI3NDM3NCIsInR5cGUiOiJUZWNobmljYWwifSx7Im5hbWUiOiJCdXNpbmVzcyIsImVtYWlsIjoic2F" +
            "jaGluaXNAd3NvMi5jb20iLCJwaG9uZSI6Iis5NDc3NDI3NDM3NCIsInR5cGUiOiJCdXNpbmVzcyJ9XSwib3JnX2p3a3NfZW5kcG9pbn" +
            "QiOiJodHRwczovL2tleXN0b3JlLm9wZW5iYW5raW5ndGVzdC5vcmcudWsvMDAxNTgwMDAwMUhRUXJaQUFYLzAwMTU4MDAwMDFIUVFyW" +
            "kFBWC5qd2tzIiwib3JnX2p3a3NfcmV2b2tlZF9lbmRwb2ludCI6Imh0dHBzOi8va2V5c3RvcmUub3BlbmJhbmtpbmd0ZXN0Lm9yZy51" +
            "ay8wMDE1ODAwMDAxSFFRclpBQVgvcmV2b2tlZC8wMDE1ODAwMDAxSFFRclpBQVguandrcyIsInNvZnR3YXJlX2p3a3NfZW5kcG9pbnQ" +
            "iOiJodHRwczovL2tleXN0b3JlLm9wZW5iYW5raW5ndGVzdC5vcmcudWsvMDAxNTgwMDAwMUhRUXJaQUFYLzAwMTU4MDAwMDFIUVFyWk" +
            "FBWC5qd2tzIiwic29mdHdhcmVfandrc19yZXZva2VkX2VuZHBvaW50IjoiaHR0cHM6Ly9rZXlzdG9yZS5vcGVuYmFua2luZ3Rlc3Qub" +
            "3JnLnVrLzAwMTU4MDAwMDFIUVFyWkFBWC9yZXZva2VkLzlaekZGQnhTTEdFalBab2dSQWJ2RmQuandrcyIsInNvZnR3YXJlX3BvbGlj" +
            "eV91cmkiOiJodHRwczovL3d3dy5nb29nbGUuY29tIiwic29mdHdhcmVfdG9zX3VyaSI6Imh0dHBzOi8vd3d3Lmdvb2dsZS5jb20iLCJ" +
            "zb2Z0d2FyZV9vbl9iZWhhbGZfb2Zfb3JnIjpudWxsfQ.W1SPCi5K3Z1_vboOVNQIpNM648pIAu5TKM0CmvvVfTaC3J9HzqpsePF_jsZ" +
            "5FFmkAcLJbBx5DOhhxbFIuRBQ-zObxdT0dTCKJfG00OuoRZbefebMzQdzbhqkX304Da0FV03J7YDs4LIMnUk0r_GubdfmOhZtHpfrdY" +
            "icURlJiR4UvsXc5wub2VmyjFiAriQNsOI137vx0gTHTaAYCazia0C9gQQIy6SW8G0ck53SqoEdrowHlFVUpnrfzI0Y5LGc0zGftpDT7" +
            "nqpq3dJt7NpxGEzuUO8r3xPDFj6RgTXqpdpQooZ3jfvjHiVFjkM36aGvz8bPSMoWu2uub7DUrlqWA";

    public static String registrationRequestJson = "{\n" +
            "  \"iss\": \"9b5usDpbNtmxDcTzs7GzKp\",\n" +
            "  \"iat\": 1593752054,\n" +
            "  \"exp\": 1743573565,\n" +
            "  \"jti\": \"92713892-5514-11e9-8647-d663bd873d93\",\n" +
            "  \"aud\": \"https://localbank.com\",\n" +
            "  \"scope\": \"accounts payments\",\n" +
            "  \"token_endpoint_auth_method\": \"private_key_jwt\",\n" +
            "  \"grant_types\": [\n" +
            "    \"authorization_code\",\n" +
            "    \"refresh_token\"\n" +
            "  ],\n" +
            "  \"response_types\": [\n" +
            "    \"code id_token\"\n" +
            "  ],\n" +
            "  \"id_token_signed_response_alg\": \"PS256\",\n" +
            "  \"request_object_signing_alg\": \"PS256\",\n" +
            "  \"software_id\": \"9b5usDpbNtmxDcTzs7GzKp\",\n" +
            "  \"application_type\": \"web\",\n" +
            "  \"redirect_uris\": [\n" +
            "    \"https://wso2.com\"\n" +
            "  ],\n" +
            "  \"software_statement\" : " + RegistrationTestConstants.SSA +
            "}";

    public static String extendedRegistrationRequestJson =  "{\n" +
            "  \"iss\": \"9b5usDpbNtmxDcTzs7GzKp\",\n" +
            "  \"iat\": 1593752054,\n" +
            "  \"exp\": 1743573565,\n" +
            "  \"jti\": \"92713892-5514-11e9-8647-d663bd873d93\",\n" +
            "  \"aud\": \"https://localbank.com\",\n" +
            "  \"scope\": \"accounts payments\",\n" +
            "  \"token_endpoint_auth_method\": \"private_key_jwt\",\n" +
            "  \"grant_types\": [\n" +
            "    \"authorization_code\",\n" +
            "    \"refresh_token\"\n" +
            "  ],\n" +
            "  \"response_types\": [\n" +
            "    \"code id_token\"\n" +
            "  ],\n" +
            "  \"id_token_signed_response_alg\": \"PS256\",\n" +
            "  \"request_object_signing_alg\": \"PS256\",\n" +
            "  \"software_id\": \"9b5usDpbNtmxDcTzs7GzKp\",\n" +
            "  \"application_type\": \"web\",\n" +
            "  \"software_statement\" : " + RegistrationTestConstants.SSA +
            "}";
}
