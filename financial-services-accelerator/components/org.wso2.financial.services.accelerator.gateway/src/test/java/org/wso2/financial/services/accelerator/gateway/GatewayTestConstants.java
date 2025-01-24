/**
* Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.gateway;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Constants for Gateway test cases.
 */
public class GatewayTestConstants {

    public static final String VALID_EXECUTOR_CLASS =
            "org.wso2.financial.services.accelerator.gateway.executor.core.MockFSExecutor";
    public static final Map<Integer, String> VALID_EXECUTOR_MAP = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>(1, VALID_EXECUTOR_CLASS))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    public static final Map<String, Map<Integer, String>> FULL_VALIDATOR_MAP = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>("Default", VALID_EXECUTOR_MAP),
                    new AbstractMap.SimpleImmutableEntry<>("DCR", VALID_EXECUTOR_MAP))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    public static final String CUSTOM_PAYLOAD = "{\"custom\":\"payload\"}";
    public static final String B64_PAYLOAD = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva" +
            "G4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ";
    public static final String TEST_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwI" +
            "iwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    public static final String XML_PAYLOAD = "<soapenv:Body " +
            "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<text><content>Test Content</content><date>2024-09-30</date></text>" +
            "</soapenv:Body>";

    public static final String DCR_PAYLOAD = "eyJ0eXAiOiJKV1QiLCJhbGciOiJQUzI1NiIsImtpZCI6ImNJWW8tNXpYNE9UV1pwSHJ" +
            "tbWlaRFZ4QUNKTSJ9.eyJpc3MiOiJvUTRLb2FhdnBPdW9FN3J2UXNaRU9WIiwiaWF0IjoxNzMzODMxNzcyMDQwLCJleHAiOjE3Mz" +
            "QwOTA5NzIwNDIsImp0aSI6IjE3MzM4MzE3NzIwNDIiLCJhdWQiOiJodHRwczovL2xvY2FsYmFuay5jb20iLCJyZWRpcmVjdF91cml" +
            "zIjpbImh0dHBzOi8vd3d3Lmdvb2dsZS5jb20vcmVkaXJlY3RzL3JlZGlyZWN0MSIsImh0dHBzOi8vd3d3Lmdvb2dsZS5jb20vcmVk" +
            "aXJlY3RzL3JlZGlyZWN0MiJdLCJ0b2tlbl9lbmRwb2ludF9hdXRoX3NpZ25pbmdfYWxnIjoiUFMyNTYiLCJ0b2tlbl9lbmRwb2lud" +
            "F9hdXRoX21ldGhvZCI6InByaXZhdGVfa2V5X2p3dCIsImdyYW50X3R5cGVzIjpbImF1dGhvcml6YXRpb25fY29kZSIsImNsaWVudF" +
            "9jcmVkZW50aWFscyIsInJlZnJlc2hfdG9rZW4iXSwicmVzcG9uc2VfdHlwZXMiOlsiY29kZSBpZF90b2tlbiJdLCJhcHBsaWNhdGl" +
            "vbl90eXBlIjoid2ViIiwiaWRfdG9rZW5fc2lnbmVkX3Jlc3BvbnNlX2FsZyI6IlBTMjU2IiwiaWRfdG9rZW5fZW5jcnlwdGVkX3Jl" +
            "c3BvbnNlX2FsZyI6IlJTQS1PQUVQIiwiaWRfdG9rZW5fZW5jcnlwdGVkX3Jlc3BvbnNlX2VuYyI6IkEyNTZHQ00iLCJyZXF1ZXN0X" +
            "29iamVjdF9zaWduaW5nX2FsZyI6IlBTMjU2Iiwic29mdHdhcmVfc3RhdGVtZW50IjoiZXlKaGJHY2lPaUpRVXpJMU5pSXNJbXRwWk" +
            "NJNkltTkpXVzh0TlhwWU5FOVVWMXB3U0hKdGJXbGFSRlo0UVVOS1RTSXNJblI1Y0NJNklrcFhWQ0o5LmV5SnBjM01pT2lKUGNHVnVR" +
            "bUZ1YTJsdVp5Qk1kR1FpTENKcFlYUWlPakUzTVRZNU5UVTJNVE1zSW1wMGFTSTZJbUpoTTJKaFpqTmpPRFEzTWpRMVptRWlMQ0p6Yj" +
            "JaMGQyRnlaVjlsYm5acGNtOXViV1Z1ZENJNkluTmhibVJpYjNnaUxDSnpiMlowZDJGeVpWOXRiMlJsSWpvaVZHVnpkQ0lzSW5Odlpu" +
            "UjNZWEpsWDJsa0lqb2liMUUwUzI5aFlYWndUM1Z2UlRkeWRsRnpXa1ZQVmlJc0luTnZablIzWVhKbFgyTnNhV1Z1ZEY5cFpDSTZJbT" +
            "lSTkV0dllXRjJjRTkxYjBVM2NuWlJjMXBGVDFZaUxDSnpiMlowZDJGeVpWOWpiR2xsYm5SZmJtRnRaU0k2SWxkVFR6SWdUM0JsYmlC" +
            "Q1lXNXJhVzVuSUZSUVVESWdLRk5oYm1SaWIzZ3BJaXdpYzI5bWRIZGhjbVZmWTJ4cFpXNTBYMlJsYzJOeWFYQjBhVzl1SWpvaVYxTl" +
            "BNaUJQY0dWdUlFSmhibXRwYm1jZ1ZGQlFNaUJtYjNJZ2RHVnpkR2x1WnlJc0luTnZablIzWVhKbFgzWmxjbk5wYjI0aU9pSXhMalVpT" +
            "ENKemIyWjBkMkZ5WlY5amJHbGxiblJmZFhKcElqb2lhSFIwY0hNNkx5OTNkM2N1WjI5dloyeGxMbU52YlNJc0luTnZablIzWVhKbFg" +
            "zSmxaR2x5WldOMFgzVnlhWE1pT2xzaWFIUjBjSE02THk5M2QzY3VaMjl2WjJ4bExtTnZiUzl5WldScGNtVmpkSE12Y21Wa2FYSmxZM" +
            "1F4SWwwc0luTnZablIzWVhKbFgzSnZiR1Z6SWpwYklsQkpVMUFpTENKQlNWTlFJaXdpUTBKUVNVa2lYU3dpYjNKbllXNXBjMkYwYVc" +
            "5dVgyTnZiWEJsZEdWdWRGOWhkWFJvYjNKcGRIbGZZMnhoYVcxeklqcDdJbUYxZEdodmNtbDBlVjlwWkNJNklrOUNSMEpTSWl3aWNtV" +
            "m5hWE4wY21GMGFXOXVYMmxrSWpvaVZXNXJibTkzYmpBd01UVTRNREF3TURGSVVWRnlXa0ZCV0NJc0luTjBZWFIxY3lJNklrRmpkR2w" +
            "yWlNJc0ltRjFkR2h2Y21sellYUnBiMjV6SWpwYmV5SnRaVzFpWlhKZmMzUmhkR1VpT2lKSFFpSXNJbkp2YkdWeklqcGJJbEJKVTFBaU" +
            "xDSkJTVk5RSWl3aVEwSlFTVWtpWFgwc2V5SnRaVzFpWlhKZmMzUmhkR1VpT2lKSlJTSXNJbkp2YkdWeklqcGJJbEJKVTFBaUxDSkRRb" +
            "EJKU1NJc0lrRkpVMUFpWFgwc2V5SnRaVzFpWlhKZmMzUmhkR1VpT2lKT1RDSXNJbkp2YkdWeklqcGJJbEJKVTFBaUxDSkJTVk5RSWl3" +
            "aVEwSlFTVWtpWFgxZGZTd2ljMjltZEhkaGNtVmZiRzluYjE5MWNta2lPaUpvZEhSd2N6b3ZMM2QzZHk1bmIyOW5iR1V1WTI5dElpd2l" +
            "iM0puWDNOMFlYUjFjeUk2SWtGamRHbDJaU0lzSW05eVoxOXBaQ0k2SWpBd01UVTRNREF3TURGSVVWRnlXa0ZCV0NJc0ltOXlaMTl1WV" +
            "cxbElqb2lWMU5QTWlBb1ZVc3BJRXhKVFVsVVJVUWlMQ0p2Y21kZlkyOXVkR0ZqZEhNaU9sdDdJbTVoYldVaU9pSlVaV05vYm1sallXd" +
            "2lMQ0psYldGcGJDSTZJbk5oWTJocGJtbHpRSGR6YnpJdVkyOXRJaXdpY0dodmJtVWlPaUlyT1RRM056UXlOelF6TnpRaUxDSjBlWEJs" +
            "SWpvaVZHVmphRzVwWTJGc0luMHNleUp1WVcxbElqb2lRblZ6YVc1bGMzTWlMQ0psYldGcGJDSTZJbk5oWTJocGJtbHpRSGR6YnpJdVk" +
            "yOXRJaXdpY0dodmJtVWlPaUlyT1RRM056UXlOelF6TnpRaUxDSjBlWEJsSWpvaVFuVnphVzVsYzNNaWZWMHNJbTl5WjE5cWQydHpYM" +
            "lZ1WkhCdmFXNTBJam9pYUhSMGNITTZMeTlyWlhsemRHOXlaUzV2Y0dWdVltRnVhMmx1WjNSbGMzUXViM0puTG5Wckx6QXdNVFU0TUR" +
            "Bd01ERklVVkZ5V2tGQldDOHdNREUxT0RBd01EQXhTRkZSY2xwQlFWZ3VhbmRyY3lJc0ltOXlaMTlxZDJ0elgzSmxkbTlyWldSZlpXN" +
            "WtjRzlwYm5RaU9pSm9kSFJ3Y3pvdkwydGxlWE4wYjNKbExtOXdaVzVpWVc1cmFXNW5kR1Z6ZEM1dmNtY3VkV3N2TURBeE5UZ3dNREF" +
            "3TVVoUlVYSmFRVUZZTDNKbGRtOXJaV1F2TURBeE5UZ3dNREF3TVVoUlVYSmFRVUZZTG1wM2EzTWlMQ0p6YjJaMGQyRnlaVjlxZDJ0e" +
            "lgyVnVaSEJ2YVc1MElqb2lhSFIwY0hNNkx5OXJaWGx6ZEc5eVpTNXZjR1Z1WW1GdWEybHVaM1JsYzNRdWIzSm5MblZyTHpBd01UVTR" +
            "NREF3TURGSVVWRnlXa0ZCV0M5dlVUUkxiMkZoZG5CUGRXOUZOM0oyVVhOYVJVOVdMbXAzYTNNaUxDSnpiMlowZDJGeVpWOXFkMnR6W" +
            "DNKbGRtOXJaV1JmWlc1a2NHOXBiblFpT2lKb2RIUndjem92TDJ0bGVYTjBiM0psTG05d1pXNWlZVzVyYVc1bmRHVnpkQzV2Y21jdWR" +
            "Xc3ZNREF4TlRnd01EQXdNVWhSVVhKYVFVRllMM0psZG05clpXUXZiMUUwUzI5aFlYWndUM1Z2UlRkeWRsRnpXa1ZQVmk1cWQydHpJa" +
            "XdpYzI5bWRIZGhjbVZmY0c5c2FXTjVYM1Z5YVNJNkltaDBkSEJ6T2k4dmQzZDNMbWR2YjJkc1pTNWpiMjBpTENKemIyWjBkMkZ5WlY" +
            "5MGIzTmZkWEpwSWpvaWFIUjBjSE02THk5M2QzY3VaMjl2WjJ4bExtTnZiU0lzSW5OdlpuUjNZWEpsWDI5dVgySmxhR0ZzWmw5dlpsO" +
            "XZjbWNpT2lKWFUwOHlJRTl3Wlc0Z1FtRnVhMmx1WnlKOS5WbmZYcnBwR21DY1lHV0xjbVQzZ0IyMnIyOTdWYzBwcGliTHJsMG12OFB" +
            "Ha2I3b1pMRWtJcWFBZHo5T0J3RmplaERpSGxJek9yQ3pOZ3pKRDVHdnlhY1pTaW9yRmtwekJwYlY4MHEtbl8tdUZUdWdFN21yQ1ZuT" +
            "mZUc2IxU0JFZG9XQ1JuX0JiekgtVDJZc3RxTFdQaGJfZkhrRFNGVEdKZVNuRkdwMUVjTVhWbXg4UC1wQ2dzb1RTLWtYRVBEWEQ3RjR" +
            "pWmpad2NGZnhEcGVfTjhGdkFVQzI4bDNUem0xYXU0YkxqckkwVDk0UFZvRUpFbUFrOUFVX3NvbUZ5X1hFdnVLdXVVTGZjY1czQ0RSN" +
            "ktHYnZYVjdNVlBOQTVYVFQ1Z19IOWJSeGNQWDRaYWFXZklhUlZmWjI4ZF9aQ1J0c1VJaUxnbV82ZVpraUQtN0VoM3F5VlEifQ.Zi-H" +
            "rNByfJBBoTAwN3Es2sq8LkA70tEfmA0MI7469eONHHtdqfflMXtbNQsw4gSyb6Gb4emB8GO3hGUKxFGF2r0D55WqLhl_fJjt4bHutk" +
            "sg0dKeqqerg1W0jm4dcUjI47RpMzAyno4rtYpuSosz50MXQfGiBLHk7C4GLjAVbDFwA8Go1MWZil_aZ7U9stoh0M8NfX4c41VxPoiB" +
            "IFzfjPIV1EA6_pQ_PQsiJfhXzdutNF79q-biAlqdckcwpDCwi-cxpXuCCjfq9oOhp3SLinqR3m_ZTbbEsdQMdHcpZ6a9mYwja6lfB7" +
            "YxhzQHa5zkVpTgvcrc77O8qkJhj6mALw";

    public static final String SSA = "eyJhbGciOiJQUzI1NiIsImtpZCI6ImNJWW8tNXpYNE9UV1pwSHJtbWlaRFZ4QUNKTSIsInR5cCI6Ik" +
            "pXVCJ9.eyJpc3MiOiJPcGVuQmFua2luZyBMdGQiLCJpYXQiOjE3MTY5NTU2MTMsImp0aSI6ImJhM2JhZjNjODQ3MjQ1ZmEiLCJzb2Z0" +
            "d2FyZV9lbnZpcm9ubWVudCI6InNhbmRib3giLCJzb2Z0d2FyZV9tb2RlIjoiVGVzdCIsInNvZnR3YXJlX2lkIjoib1E0S29hYXZwT3V" +
            "vRTdydlFzWkVPViIsInNvZnR3YXJlX2NsaWVudF9pZCI6Im9RNEtvYWF2cE91b0U3cnZRc1pFT1YiLCJzb2Z0d2FyZV9jbGllbnRfbm" +
            "FtZSI6IldTTzIgT3BlbiBCYW5raW5nIFRQUDIgKFNhbmRib3gpIiwic29mdHdhcmVfY2xpZW50X2Rlc2NyaXB0aW9uIjoiV1NPMiBPc" +
            "GVuIEJhbmtpbmcgVFBQMiBmb3IgdGVzdGluZyIsInNvZnR3YXJlX3ZlcnNpb24iOiIxLjUiLCJzb2Z0d2FyZV9jbGllbnRfdXJpIjoi" +
            "aHR0cHM6Ly93d3cuZ29vZ2xlLmNvbSIsInNvZnR3YXJlX3JlZGlyZWN0X3VyaXMiOlsiaHR0cHM6Ly93d3cuZ29vZ2xlLmNvbS9yZWR" +
            "pcmVjdHMvcmVkaXJlY3QxIl0sInNvZnR3YXJlX3JvbGVzIjpbIlBJU1AiLCJBSVNQIiwiQ0JQSUkiXSwib3JnYW5pc2F0aW9uX2NvbX" +
            "BldGVudF9hdXRob3JpdHlfY2xhaW1zIjp7ImF1dGhvcml0eV9pZCI6Ik9CR0JSIiwicmVnaXN0cmF0aW9uX2lkIjoiVW5rbm93bjAwM" +
            "TU4MDAwMDFIUVFyWkFBWCIsInN0YXR1cyI6IkFjdGl2ZSIsImF1dGhvcmlzYXRpb25zIjpbeyJtZW1iZXJfc3RhdGUiOiJHQiIsInJv" +
            "bGVzIjpbIlBJU1AiLCJBSVNQIiwiQ0JQSUkiXX0seyJtZW1iZXJfc3RhdGUiOiJJRSIsInJvbGVzIjpbIlBJU1AiLCJDQlBJSSIsIkF" +
            "JU1AiXX0seyJtZW1iZXJfc3RhdGUiOiJOTCIsInJvbGVzIjpbIlBJU1AiLCJBSVNQIiwiQ0JQSUkiXX1dfSwic29mdHdhcmVfbG9nb1" +
            "91cmkiOiJodHRwczovL3d3dy5nb29nbGUuY29tIiwib3JnX3N0YXR1cyI6IkFjdGl2ZSIsIm9yZ19pZCI6IjAwMTU4MDAwMDFIUVFyW" +
            "kFBWCIsIm9yZ19uYW1lIjoiV1NPMiAoVUspIExJTUlURUQiLCJvcmdfY29udGFjdHMiOlt7Im5hbWUiOiJUZWNobmljYWwiLCJlbWFp" +
            "bCI6InNhY2hpbmlzQHdzbzIuY29tIiwicGhvbmUiOiIrOTQ3NzQyNzQzNzQiLCJ0eXBlIjoiVGVjaG5pY2FsIn0seyJuYW1lIjoiQnV" +
            "zaW5lc3MiLCJlbWFpbCI6InNhY2hpbmlzQHdzbzIuY29tIiwicGhvbmUiOiIrOTQ3NzQyNzQzNzQiLCJ0eXBlIjoiQnVzaW5lc3MifV" +
            "0sIm9yZ19qd2tzX2VuZHBvaW50IjoiaHR0cHM6Ly9rZXlzdG9yZS5vcGVuYmFua2luZ3Rlc3Qub3JnLnVrLzAwMTU4MDAwMDFIUVFyW" +
            "kFBWC8wMDE1ODAwMDAxSFFRclpBQVguandrcyIsIm9yZ19qd2tzX3Jldm9rZWRfZW5kcG9pbnQiOiJodHRwczovL2tleXN0b3JlLm9w" +
            "ZW5iYW5raW5ndGVzdC5vcmcudWsvMDAxNTgwMDAwMUhRUXJaQUFYL3Jldm9rZWQvMDAxNTgwMDAwMUhRUXJaQUFYLmp3a3MiLCJzb2Z" +
            "0d2FyZV9qd2tzX2VuZHBvaW50IjoiaHR0cHM6Ly9rZXlzdG9yZS5vcGVuYmFua2luZ3Rlc3Qub3JnLnVrLzAwMTU4MDAwMDFIUVFyWk" +
            "FBWC9vUTRLb2FhdnBPdW9FN3J2UXNaRU9WLmp3a3MiLCJzb2Z0d2FyZV9qd2tzX3Jldm9rZWRfZW5kcG9pbnQiOiJodHRwczovL2tle" +
            "XN0b3JlLm9wZW5iYW5raW5ndGVzdC5vcmcudWsvMDAxNTgwMDAwMUhRUXJaQUFYL3Jldm9rZWQvb1E0S29hYXZwT3VvRTdydlFzWkVP" +
            "Vi5qd2tzIiwic29mdHdhcmVfcG9saWN5X3VyaSI6Imh0dHBzOi8vd3d3Lmdvb2dsZS5jb20iLCJzb2Z0d2FyZV90b3NfdXJpIjoiaHR" +
            "0cHM6Ly93d3cuZ29vZ2xlLmNvbSIsInNvZnR3YXJlX29uX2JlaGFsZl9vZl9vcmciOiJXU08yIE9wZW4gQmFua2luZyJ9.VnfXrppGm" +
            "CcYGWLcmT3gB22r297Vc0ppibLrl0mv8PGkb7oZLEkIqaAdz9OBwFjehDiHlIzOrCzNgzJD5GvyacZSiorFkpzBpbV80q-n_-uFTugE" +
            "7mrCVnNfTsb1SBEdoWCRn_BbzH-T2YstqLWPhb_fHkDSFTGJeSnFGp1EcMXVmx8P-pCgsoTS-kXEPDXD7F4iZjZwcFfxDpe_N8FvAUC" +
            "28l3Tzm1au4bLjrI0T94PVoEJEmAk9AU_somFy_XEvuKuuULfccW3CDR6KGbvXV7MVPNA5XTT5g_H9bRxcPX4ZaaWfIaRVfZ28d_ZCR" +
            "tsUIiLgm_6eZkiD-7Eh3qyVQ";

    public static final String DECODED_DCR_PAYLOAD = "{\n" +
            "  \"token_endpoint_auth_signing_alg\" : \"PS256\",\n" +
            "  \"grant_types\" : [ \"authorization_code\", \"client_credentials\", \"refresh_token\" ],\n" +
            "  \"application_type\" : \"web\",\n" +
            "  \"iss\" : \"oQ4KoaavpOuoE7rvQsZEOV\",\n" +
            "  \"software_id\": \"oQ4KoaavpOuoE7rvQsZEOV\",\n" +
            "  \"software_client_id\": \"oQ4KoaavpOuoE7rvQsZEOV\",\n" +
            "  \"software_client_name\": \"WSO2 Open Banking TPP2 (Sandbox)\",\n" +
            "  \"software_jwks_endpoint\": \"https://keystore.openbankingtest.org.uk/" +
            "                       0015800001HQQrZAAX/oQ4KoaavpOuoE7rvQsZEOV.jwks\",\n" +
            "  \"redirect_uris\" : [ \"https://www.google.com/redirects/redirect1\", " +
            "                           \"https://www.google.com/redirects/redirect2\" ],\n" +
            "  \"token_endpoint_auth_method\" : \"private_key_jwt\",\n" +
            "  \"aud\" : [ \"https://localbank.com\" ],\n" +
            "  \"software_statement\" : " + SSA + ",\n" +
            "  \"id_token_encrypted_response_alg\" : \"RSA-OAEP\",\n" +
            "  \"id_token_encrypted_response_enc\" : \"A256GCM\",\n" +
            "  \"request_object_signing_alg\" : \"PS256\",\n" +
            "  \"exp\" : 1734090972042,\n" +
            "  \"iat\" : 1733831772040,\n" +
            "  \"jti\" : \"1733831772042\",\n" +
            "  \"response_types\" : [ \"code id_token\" ],\n" +
            "  \"id_token_signed_response_alg\" : \"PS256\"\n" +
            "}\n";

    public static final String DECODED_DCR_PAYLOAD_WITHOUT_SSA = "{\n" +
            "  \"token_endpoint_auth_signing_alg\" : \"PS256\",\n" +
            "  \"grant_types\" : [ \"authorization_code\", \"client_credentials\", \"refresh_token\" ],\n" +
            "  \"application_type\" : \"web\",\n" +
            "  \"iss\" : \"oQ4KoaavpOuoE7rvQsZEOV\",\n" +
            "  \"software_id\": \"oQ4KoaavpOuoE7rvQsZEOV\",\n" +
            "  \"software_client_id\": \"oQ4KoaavpOuoE7rvQsZEOV\",\n" +
            "  \"software_client_name\": \"WSO2 Open Banking TPP2 (Sandbox)\",\n" +
            "  \"software_jwks_endpoint\": \"https://keystore.openbankingtest.org.uk/" +
            "                       0015800001HQQrZAAX/oQ4KoaavpOuoE7rvQsZEOV.jwks\",\n" +
            "  \"redirect_uris\" : [ \"https://www.google.com/redirects/redirect1\", " +
            "                           \"https://www.google.com/redirects/redirect2\" ],\n" +
            "  \"token_endpoint_auth_method\" : \"private_key_jwt\",\n" +
            "  \"aud\" : [ \"https://localbank.com\" ],\n" +
            "  \"id_token_encrypted_response_alg\" : \"RSA-OAEP\",\n" +
            "  \"id_token_encrypted_response_enc\" : \"A256GCM\",\n" +
            "  \"request_object_signing_alg\" : \"PS256\",\n" +
            "  \"exp\" : 1734090972042,\n" +
            "  \"iat\" : 1733831772040,\n" +
            "  \"jti\" : \"1733831772042\",\n" +
            "  \"response_types\" : [ \"code id_token\" ],\n" +
            "  \"id_token_signed_response_alg\" : \"PS256\"\n" +
            "}\n";

    public static final Map<String, Object> DCR_CLAIMS = Map.of("token_endpoint_auth_signing_alg", "PS256",
            "software_id", "oQ4KoaavpOuoE7rvQsZEOV",
            "software_client_name", "WSO2 Open Banking TPP2 (Sandbox)",
            "software_jwks_endpoint",
            "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/oQ4KoaavpOuoE7rvQsZEOV.jwks",
            "software_statement", SSA,
            "redirect_uris", "https://www.google.com/redirects/redirect1");

    public static final Map<String, Object> DCR_CLAIMS_WITHOUT_SSA = Map.of(
            "token_endpoint_auth_signing_alg", "PS256",
            "software_id", "oQ4KoaavpOuoE7rvQsZEOV",
            "software_client_name", "WSO2 Open Banking TPP2 (Sandbox)",
            "software_jwks_endpoint",
            "https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/oQ4KoaavpOuoE7rvQsZEOV.jwks",
            "redirect_uris", "https://www.google.com/redirects/redirect1");


    public static final String DCR_SOAP_PAYLOAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<soapenv:Body xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<text xmlns=\"http://ws.apache.org/commons/ns/payload\">" + DCR_PAYLOAD + "</text>" +
            "</soapenv:Body>";

    public static final String IS_DCR_RESPONSE = "{\n" +
            " \"client_id\": \"DdcccB3LXGz0GqAu1jjrv06Swaga\",\n" +
            " \"client_secret\": \"Ac3T9XUORQWe0NJvRhwrGlnQdRfyoei_HGIIsXwQlTwa\",\n" +
            " \"client_secret_expires_at\": 0,\n" +
            " \"redirect_uris\": [\n" +
            "       \"https://www.google.com/redirects/redirect1\"\n" +
            " ],\n" +
            " \"grant_types\": [\n" +
            "       \"authorization_code\",\n" +
            "       \"client_credentials\",\n" +
            "       \"refresh_token\"\n" +
            " ],\n" +
            " \"client_name\": \"WSO2 Open Banking TPP1\",\n" +
            " \"ext_application_display_name\": null,\n" +
            " \"ext_application_owner\": \"is_admin@wso2.com@carbon.super\",\n" +
            " \"ext_application_token_lifetime\": 3600,\n" +
            " \"ext_user_token_lifetime\": 3600,\n" +
            " \"ext_refresh_token_lifetime\": 86400,\n" +
            " \"ext_id_token_lifetime\": 3600,\n" +
            " \"ext_pkce_mandatory\": false,\n" +
            " \"ext_pkce_support_plain\": false,\n" +
            " \"ext_public_client\": false,\n" +
            " \"token_type_extension\": \"JWT\",\n" +
            " \"ext_token_type\": \"JWT\",\n" +
            " \"jwks_uri\": \"https://keystore.openbankingtest.org.uk/" +
            "           0015800001HQQrZAAX/9ZzFFBxSLGEjPZogRAbvFd.jwks\",\n" +
            " \"token_endpoint_auth_method\": \"private_key_jwt\",\n" +
            " \"token_endpoint_auth_signing_alg\": \"PS256\",\n" +
            " \"sector_identifier_uri\": null,\n" +
            " \"id_token_signed_response_alg\": \"PS256\",\n" +
            " \"id_token_encrypted_response_alg\": null,\n" +
            " \"id_token_encrypted_response_enc\": null,\n" +
            " \"request_object_signing_alg\": \"PS256\",\n" +
            " \"tls_client_auth_subject_dn\": null,\n" +
            " \"require_pushed_authorization_requests\": false,\n" +
            " \"require_signed_request_object\": true,\n" +
            " \"tls_client_certificate_bound_access_tokens\": true,\n" +
            " \"subject_type\": \"public\",\n" +
            " \"request_object_encryption_alg\": null,\n" +
            " \"request_object_encryption_enc\": null,\n" +
            " \"software_statement\":" + SSA + ",\n" +
            " \"ext_allowed_audience\": \"application\" \n" +
        "}";
}
