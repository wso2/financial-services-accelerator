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

package com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator;

import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import org.testng.annotations.DataProvider;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;

import java.text.ParseException;

/**
 * Request object test data provider.
 */
public class ReqObjectTestDataProvider {

    public static final String VALID_REQUEST = "eyJraWQiOiJEd01LZFdNbWo3UFdpbnZvcWZReVhWenlaNlEiLCJ0eXAiOiJKV1" +
            "QiLCJhbGciOiJQUzI1NiJ9.eyJhdWQiOiJodHRwczovL2xvY2FsaG9zdDo5NDQ2L29hdXRoMi90b2tlbiIsIm1heF9hZ2UiOjg2NDAw" +
            "LCJjcml0IjpbImI2NCIsImh0dHA6Ly9vcGVuYmFua2luZy5vcmcudWsvaWF0IiwiaHR0cDovL29wZW5iYW5raW5nLm9yZy51ay9pc3M" +
            "iLCJodHRwOi8vb3BlbmJhbmtpbmcub3JnLnVrL3RhbiJdLCJzY29wZSI6ImFjY291bnRzIG9wZW5pZCIsImV4cCI6MTk1NDcwODcxMC" +
            "wiY2xhaW1zIjp7ImlkX3Rva2VuIjp7ImFjciI6eyJ2YWx1ZXMiOlsidXJuOm9wZW5iYW5raW5nOnBzZDI6Y2EiLCJ1cm46b3BlbmJhb" +
            "mtpbmc6cHNkMjpzY2EiXSwiZXNzZW50aWFsIjp0cnVlfSwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjp7InZhbHVlIjoiOTRmODU3M2Mt" +
            "NjA4MC00MDI1LThkOWItMDhlM2U5MjAwZGU3IiwiZXNzZW50aWFsIjp0cnVlfX0sInVzZXJpbmZvIjp7Im9wZW5iYW5raW5nX2ludGV" +
            "udF9pZCI6eyJ2YWx1ZSI6Ijk0Zjg1NzNjLTYwODAtNDAyNS04ZDliLTA4ZTNlOTIwMGRlNyIsImVzc2VudGlhbCI6dHJ1ZX19fSwiaX" +
            "NzIjoiMlgwbjlXU05tUGlxM1hUQjhkdEMwU2hzNXI4YSIsInJlc3BvbnNlX3R5cGUiOiJjb2RlIGlkX3Rva2VuIiwicmVkaXJlY3Rfd" +
            "XJpIjoiaHR0cHM6Ly93d3cuZ29vZ2xlLmNvbS9yZWRpcmVjdHMvcmVkaXJlY3QxIiwic3RhdGUiOiIwcE4wTkJUSGN2Iiwibm9uY2Ui" +
            "OiJqQlhoT21PS0NCIiwiY2xpZW50X2lkIjoiMlgwbjlXU05tUGlxM1hUQjhkdEMwU2hzNXI4YSJ9.BjQIMnlqevJgj1zL25hmfKA9Ab" +
            "7qm2GvrkZAkxbTMNOxkxtKxQSt9QkLfycITEeM9YdGV5rh2FMdXxyex-WtO_H0G9zlKtsDyrsUw3_HWaBDd-61Hz6n65Few_f6bwtIg" +
            "HtW8oDeKpylUu01OsYtB_s4nnDw42ZCKv7zGzkTDkyoxoM2_b_AUqh-F3PNY9Arru5m1-FGDYi_zl4iQ3d3um_NYnhPhmC2wz2R9yms" +
            "flXBn9bd-d6nPKl_ftGnqmiBua7oKMd-3CMCFW9Uxig82PHbwHcuy1hYqa_7JoE58Zkr6baGur3YtgJ2381_8t5v19DJvjqhaodabfe" +
            "uNWR3GA";

    public static final String SCOPES_INVALID_REQ = "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkR3TUtkV01tajdQ" +
            "V2ludm9xZlF5WFZ6eVo2USJ9.eyJhdWQiOiJodHRwczovL2xvY2FsaG9zdDo4MjQzL3Rva2VuIiwicmVzcG9uc2VfdHlwZSI6ImNvZG" +
            "UgaWRfdG9rZW4iLCJjbGllbnRfaWQiOiJpVEtPZnVxejQ2WTFIVlkyQkYwWjdKTTE4QXdhIiwicmVkaXJlY3RfdXJpIjoiaHR0cHM6L" +
            "y8xMC4xMTAuNS4yMzI6ODAwMC90ZXN0L2EvYXBwMS9jYWxsYmFjayIsInNjb3BlIjoib3BlbmlkIGJhbms6YWNjb3VudHMuYmFzaWM6" +
            "cmVhZCBiYW5rOmFjY291bnRzLmRldGFpbDpyZWFkIGJhbms6dHJhbnNhY3Rpb25zOnJlYWQiLCJzdGF0ZSI6ImFmMGlmanNsZGtqIiw" +
            "ibm9uY2UiOiJuLTBTNl9XekEyTWoiLCJjbGFpbXMiOnsic2hhcmluZ19kdXJhdGlvbiI6IjcyMDAiLCJpZF90b2tlbiI6eyJhY3IiOn" +
            "siZXNzZW50aWFsIjp0cnVlLCJ2YWx1ZXMiOlsidXJuOmNkcy5hdTpjZHI6MyJdfSwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjp7InZhb" +
            "HVlIjoiZDMwMmI4ODgtNzM3Zi00YzQ5LTk4ZmUtNmVkZGU4OTk2ZDZlIiwiZXNzZW50aWFsIjp0cnVlfX0sInVzZXJpbmZvIjp7Imdp" +
            "dmVuX25hbWUiOm51bGwsImZhbWlseV9uYW1lIjpudWxsfX19.dmkbejbZLg22Pe81rgt9-TS_7ynT4f1HGpUNugryL7K0-xWSwroqQL" +
            "mqLyx442nahZP1nd_1r5LScfer5-lslKid7WTh_gV-v9GvBe6U5xDIKxxgHXBepz7nAUZnOkmyZF9As6JrKxfVa36F-iKN-ZUyIfv0F" +
            "bVs5rejAWXqNQzYOlkSwMlOZWDjeCpozKwMYR4FyyecJ-l6gRF11hbRTxo-Uj8U40x3hHH7R7vDVLT6eK5VTwbmFYROXHy4iBQJNIcL" +
            "sOBF9lJnSvw8_rJEUteFBqtrlDpWVdQ8fZcHj_9mYMSCZlTKFMvh22ILXJ13EoxrhqPpuFxZMMx7oEPNjA";

    public static final String REQUEST_STRING = "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkR3TUtkV01tajdQV2ludm9x" +
            "ZlF5WFZ6eVo2USJ9.eyJhdWQiOiJodHRwczovL2xvY2FsaG9zdDo4MjQzL3Rva2VuIiwicmVzcG9uc2VfdHlwZSI6ImNvZGUgaWR" +
            "fdG9rZW4iLCJjbGllbnRfaWQiOiJpVEtPZnVxejQ2WTFIVlkyQkYwWjdKTTE4QXdhIiwicmVkaXJlY3RfdXJpIjoiaHR0cHM6Ly8" +
            "xMC4xMTAuNS4yMzI6ODAwMC90ZXN0L2EvYXBwMS9jYWxsYmFjayIsInNjb3BlIjoib3BlbmlkIGJhbms6YWNjb3VudHMuYmFzaWM6" +
            "cmVhZCBiYW5rOmFjY291bnRzLmRldGFpbDpyZWFkIGJhbms6dHJhbnNhY3Rpb25zOnJlYWQiLCJzdGF0ZSI6ImFmMGlmanNsZGtqIi" +
            "wibm9uY2UiOiJuLTBTNl9XekEyTWoiLCJjbGFpbXMiOnsic2hhcmluZ19kdXJhdGlvbiI6IjcyMDAiLCJpZF90b2tlbiI6eyJhY3Ii" +
            "OnsiZXNzZW50aWFsIjp0cnVlLCJ2YWx1ZXMiOlsidXJuOmNkcy5hdTpjZHI6MyJdfX0sInVzZXJpbmZvIjp7ImdpdmVuX25hbWUiOm" +
            "51bGwsImZhbWlseV9uYW1lIjpudWxsfX19.cnKvzjgiDWJ2JeRGL8ncTKB_pCxEynNHn6kzHSPBBXYRJ5e-WvPocTkvaDnwu1qSr" +
            "5lsJnFCNgYuNickzoIaTl9wUvl0rnK15iGVe0rSOCWIJ53eVphaV9uYtRfVHTN4HL4ecgdsREHhu6MyjYcqdgAeuv4g0robZGf" +
            "DDVCLr2Xb77f8yAr42xc6fBccAFnvZX33zVOHtFaY3S3j9RbQqRZjUxLycIgdVXGypRc2ESVKqJ9WgGxKG6fCUt2rDgqsobVj" +
            "8ekRAMyP2fGmYLoRAyycJ8JwU9uoRhGL6nqM6_uOYNG5a6xOsSs8i1Yvn4s7G6FUKQ_bmm4Gx2aJptzVA";

    public static final String NO_CLIENT_ID_REQUEST = "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkR3TUtkV01tajdQV" +
            "2ludm9xZlF5WFZ6eVo2USJ9.eyJhdWQiOiJodHRwczovL2xvY2FsaG9zdDo4MjQzL3Rva2VuIiwicmVzcG9uc2VfdHlwZSI6ImNvZGU" +
            "gaWRfdG9rZW4iLCJjbGllbnRfaWQiOiIiLCJyZWRpcmVjdF91cmkiOiJodHRwczovLzEwLjExMC41LjIzMjo4MDAwL3Rlc3QvYS9hcH" +
            "AxL2NhbGxiYWNrIiwic2NvcGUiOiJvcGVuaWQgYmFuazphY2NvdW50cy5iYXNpYzpyZWFkIGJhbms6YWNjb3VudHMuZGV0YWlsOnJlY" +
            "WQgYmFuazp0cmFuc2FjdGlvbnM6cmVhZCIsInN0YXRlIjoiYWYwaWZqc2xka2oiLCJub25jZSI6Im4tMFM2X1d6QTJNaiIsImNsYWlt" +
            "cyI6eyJzaGFyaW5nX2R1cmF0aW9uIjoiNzIwMCIsImlkX3Rva2VuIjp7ImFjciI6eyJlc3NlbnRpYWwiOnRydWUsInZhbHVlcyI6WyJ" +
            "1cm46Y2RzLmF1OmNkcjozIl19fSwidXNlcmluZm8iOnsiZ2l2ZW5fbmFtZSI6bnVsbCwiZmFtaWx5X25hbWUiOm51bGx9fX0.BE444R" +
            "orL9NH6THQkpr6HzylwqoxGod_OD8aIWLUgOJnS9FFQAQh6RAE-k4cXbriPS40EKC4pNIkA0CuDr3zmaZQsTrcn09W_gL9aWxIQ50cI" +
            "2RPkqXzl3W5EEtA-LBOpiqyToVvZfvfsVqZqWF8nNQW85_rToR6yiB-LWWMvlshkF0XDP6qRiFuQhsRDAa6Ro1r3hdnHBBMeoBSGRTe" +
            "0LE6jCVq_P6YrJvIbpZnMHL1wsgkJQzXoeu8HLN7kgwaYuUQesWIAWrFR26Ca7kcmRCmVqtj4Z2arhF2QQWtKUurrDYKcSxrQwZHRsQ" +
            "Zh76z6dULjhUfz7JQ5JIvtqooAA";

    public static final String VALID_FAPI_REQUEST_OBJ = "eyJraWQiOiJjSVlvLTV6WDRPVFdacEhybW1pWkRWeEFDSk0iLCJhbGciOiJ" +
            "QUzI1NiJ9.eyJhdWQiOiJodHRwczovL29iaWFtOjk0NDYvb2F1dGgyL3Rva2VuIiwibmJmIjoxNzM2MzE3NDg2LCJzY29wZSI6Im9wZ" +
            "W5pZCBhY2NvdW50cyIsImNsYWltcyI6eyJpZF90b2tlbiI6eyJvcGVuYmFua2luZ19pbnRlbnRfaWQiOnsidmFsdWUiOiJjNTkyNzRm" +
            "Yi1lZjRkLTRjNjktYTU0Yi1iM2EyYWU2MzljYjgiLCJlc3NlbnRpYWwiOnRydWV9LCJhY3IiOnsidmFsdWVzIjpbInVybjpvcGVuYmF" +
            "ua2luZzpwc2QyOnNjYSIsInVybjpvcGVuYmFua2luZzpwc2QyOmNhIl0sImVzc2VudGlhbCI6dHJ1ZX19fSwiaXNzIjoiZjhaSkZCZz" +
            "RkbXo4OGdmTFY1SGRDZnFFZnVvYSIsInJlc3BvbnNlX3R5cGUiOiJjb2RlIGlkX3Rva2VuIiwicmVkaXJlY3RfdXJpIjoiaHR0cHM6L" +
            "y9sb2NhbGhvc3QuZW1vYml4LmNvLnVrOjg0NDMvdGVzdC9hL3dzbzJjYXJib24vY2FsbGJhY2siLCJzdGF0ZSI6IkdheTMyTnBSdDAi" +
            "LCJleHAiOjE3MzYzMTc3ODYsIm5vbmNlIjoiVEtBbXFaSFEwUSIsImNsaWVudF9pZCI6ImY4WkpGQmc0ZG16ODhnZkxWNUhkQ2ZxRWZ" +
            "1b2EifQ.XFM8Gn3ZMduJGTifix-gulACNBDVEIiHUuNf52JJg1f_5cN6YrpEf8Y3-yumaZwQ4SdQJIUhMH5k9CP48fbezXKWLhIHxSy" +
            "UEziZF49OY-y_JB8HHtrtD2t3kUogyRlEzJZiQPkvK_VNzvAQuZ7pes4m5yPZp1zE_smF-UlrOhFgm5KB_VNcVDzWa2WkdDiTllk5kl" +
            "UjQ03yB4CVUlL2Y7pJMfXx6X-OZe81ovh4n-qB_Nksf_zFCV6o0r6RmXg-80-SXkYYVgmK7tqzvTM5J4bWyZ2qJi12egBThMgpZrkAZ" +
            "LDh4JbfcjB2D8n97ECH_7rr3swniUi9n5pSBuHkaw";

    public static final String FAPI_REQ_OBJ_WITHOUT_CLIENT_ID = "eyJraWQiOiJjSVlvLTV6WDRPVFdacEhybW1pWkRWeEFDSk0iLCJ" +
            "hbGciOiJQUzI1NiJ9.eyJhdWQiOiJodHRwczovL29iaWFtOjk0NDYvb2F1dGgyL3Rva2VuIiwibmJmIjoxNzM2MzE3NDg2LCJzY29wZ" +
            "SI6Im9wZW5pZCBhY2NvdW50cyIsImNsYWltcyI6eyJpZF90b2tlbiI6eyJvcGVuYmFua2luZ19pbnRlbnRfaWQiOnsidmFsdWUiOiJj" +
            "NTkyNzRmYi1lZjRkLTRjNjktYTU0Yi1iM2EyYWU2MzljYjgiLCJlc3NlbnRpYWwiOnRydWV9LCJhY3IiOnsidmFsdWVzIjpbInVybjp" +
            "vcGVuYmFua2luZzpwc2QyOnNjYSIsInVybjpvcGVuYmFua2luZzpwc2QyOmNhIl0sImVzc2VudGlhbCI6dHJ1ZX19fSwiaXNzIjoiZj" +
            "haSkZCZzRkbXo4OGdmTFY1SGRDZnFFZnVvYSIsInJlc3BvbnNlX3R5cGUiOiJjb2RlIGlkX3Rva2VuIiwicmVkaXJlY3RfdXJpIjoia" +
            "HR0cHM6Ly9sb2NhbGhvc3QuZW1vYml4LmNvLnVrOjg0NDMvdGVzdC9hL3dzbzJjYXJib24vY2FsbGJhY2siLCJzdGF0ZSI6IkdheTMy" +
            "TnBSdDAiLCJleHAiOjE3MzYzMTc3ODYsIm5vbmNlIjoiVEtBbXFaSFEwUSIsImNsaWVudF9pZCI6IiJ9.L-f1tTGNS_c2qJOunM7Wq7" +
            "k51Fk9R1rR3xhRqjLjmEeQbv-Rc9MOnpxbWm1wE1_yLB3n9nKaMj9XdbGpFVCJAHrhxJSSMkhcI8RiyuXthaVFX1GymP5yxiSFUBy8G" +
            "imp34gkhT1rkhaF4wnwR_nRZv-KVccoZYPUF8ez_fKzt5K30dDQKiT1AW7t8yMLzz2K0i7dRMs0m4cZZdHBk_ixPVx0IKlpRlg2YuPh" +
            "rcFC1TlE4DCJyweebJbxiQbJeCyMvgTqURwigd5wbTanZ_FYyRSFDhkR-8PmpsH2UlytVg5zTxv_jRDqAkI3xcmIHCb6_aaajv6iGDF" +
            "uyJqy1YvoNg";

    public static final String FAP_REQ_OBJ_WITHOUT_EXP = "eyJraWQiOiJjSVlvLTV6WDRPVFdacEhybW1pWkRWeEFDSk0iLCJhbGciO" +
            "iJQUzI1NiJ9.eyJhdWQiOiJodHRwczovL29iaWFtOjk0NDYvb2F1dGgyL3Rva2VuIiwibmJmIjoxNzM2MzEzMjQzLCJzY29wZSI6Im" +
            "9wZW5pZCBhY2NvdW50cyIsImNsYWltcyI6eyJpZF90b2tlbiI6eyJvcGVuYmFua2luZ19pbnRlbnRfaWQiOnsidmFsdWUiOiIzYThj" +
            "ZWViYS04ZmJkLTQ2ZDYtOWVhZi0wZjgzNTI5MjRmMDkiLCJlc3NlbnRpYWwiOnRydWV9LCJhY3IiOnsidmFsdWVzIjpbInVybjpvcG" +
            "VuYmFua2luZzpwc2QyOnNjYSIsInVybjpvcGVuYmFua2luZzpwc2QyOmNhIl0sImVzc2VudGlhbCI6dHJ1ZX19fSwiaXNzIjoidmpu" +
            "QzlxX19Ha1BJYmpOU3pwbnNDMGRPa3U4YSIsInJlc3BvbnNlX3R5cGUiOiJjb2RlIGlkX3Rva2VuIiwicmVkaXJlY3RfdXJpIjoiaH" +
            "R0cHM6Ly9sb2NhbGhvc3QuZW1vYml4LmNvLnVrOjg0NDMvdGVzdC9hL3dzbzJjYXJib24vY2FsbGJhY2siLCJzdGF0ZSI6InN4UUtw" +
            "ckRNWDEiLCJub25jZSI6IkNNRkVhUlZ6eUkiLCJjbGllbnRfaWQiOiJ2am5DOXFfX0drUEliak5TenBuc0MwZE9rdThhIn0.KchTgE" +
            "eElHTYpM_1bHTqB-tZQXANSSS7A4zIQ3GYdnN_8MP_RxaCZ17HgQmZ8MxOpvIk2tzdRmo0OODmd21uEciC4j9Isf6vhQEZ1Tlnc0R1" +
            "jtmEExjiX-OYkogan1_VjW5npdwhumDwN-GPSqutl0vQ8144x2XTXT0HnnkpE1TUT2lMtV019StXWNfgegSJ6Dnn7e8_aAvNPPAopA" +
            "o9FBYG5nbxfetJRUH6-fbcaSwb4JOMhsb_DLFunYWgzBz1QYFCSOLEbj8zN7dIFEu7IG0ivGll1EkZMC_he99XHiL234RWX-NPI_dx" +
            "Jpci0Wcn-SOyLwYGDU0ZpQVV48inSQ";


    @DataProvider(name = "dp-checkValidRequestObject")
    public Object[][] dpCheckValidRequestObject() throws ParseException, RequestObjectException {

        RequestObject requestObject = new RequestObject();
        JOSEObject jwt = JOSEObject.parse(REQUEST_STRING);
        if (jwt.getHeader().getAlgorithm() == null || jwt.getHeader().getAlgorithm().equals(JWSAlgorithm.NONE)) {
            requestObject.setPlainJWT(PlainJWT.parse(REQUEST_STRING));
        } else {
            requestObject.setSignedJWT(SignedJWT.parse(REQUEST_STRING));
        }

        return new Object[][]{
                {requestObject, new OAuth2Parameters()},
                {requestObject, null},
        };
    }

    @DataProvider(name = "dp-checkIncorrectRequestObject")
    public Object[][] dpCheckIncorrectRequestObject() throws ParseException, RequestObjectException {

        RequestObject requestObject = new RequestObject();
        JOSEObject jwt = JOSEObject.parse(REQUEST_STRING);
        if (jwt.getHeader().getAlgorithm() == null || jwt.getHeader().getAlgorithm().equals(JWSAlgorithm.NONE)) {
            requestObject.setPlainJWT(PlainJWT.parse(REQUEST_STRING));
        } else {
            requestObject.setSignedJWT(SignedJWT.parse(REQUEST_STRING));
        }

        return new Object[][]{
                {new RequestObject(), new OAuth2Parameters()},
        };
    }

    @DataProvider(name = "dp-checkInValidRequestObject")
    public Object[][] dpCheckInValidRequestObject() {

        return new Object[][]{
                {null, new OAuth2Parameters()},
                {null, null},
        };
    }

}
