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

package org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.utils;

/**
 * Constants used in DCR tests.
 */
public class DCRTestConstants {

    public static final String SSA = "eyJhbGciOiJQUzI1NiIsImtpZCI6IkJrSHhlSUhLeU1LRjZTZ0d3cVl6TFV2VFFmayIsInR5cCI6" +
            "IkpXVCJ9.eyJpc3MiOiJPcGVuQmFua2luZyBMdGQiLCJpYXQiOjE3MzU4OTIzMDMsImp0aSI6ImY4ODQ0NTUyY2VhZTQ5YTMiLCJz" +
            "b2Z0d2FyZV9lbnZpcm9ubWVudCI6InNhbmRib3giLCJzb2Z0d2FyZV9tb2RlIjoiVGVzdCIsInNvZnR3YXJlX2lkIjoiOVp6RkZCe" +
            "FNMR0VqUFpvZ1JBYnZGZCIsInNvZnR3YXJlX2NsaWVudF9pZCI6IjlaekZGQnhTTEdFalBab2dSQWJ2RmQiLCJzb2Z0d2FyZV9jbG" +
            "llbnRfbmFtZSI6IldTTzIgT3BlbiBCYW5raW5nIFRQUCAoU2FuZGJveCkiLCJzb2Z0d2FyZV9jbGllbnRfZGVzY3JpcHRpb24iOiJ" +
            "XU08yIE9wZW4gQmFua2luZyBUUFAgZm9yIHRlc3RpbmciLCJzb2Z0d2FyZV92ZXJzaW9uIjoiMS41Iiwic29mdHdhcmVfY2xpZW50" +
            "X3VyaSI6Imh0dHBzOi8vd3d3Lmdvb2dsZS5jb20iLCJzb2Z0d2FyZV9yZWRpcmVjdF91cmlzIjpbImh0dHBzOi8vd3d3Lmdvb2dsZ" +
            "S5jb20vcmVkaXJlY3RzL3JlZGlyZWN0MSJdLCJzb2Z0d2FyZV9yb2xlcyI6WyJQSVNQIiwiQUlTUCIsIkNCUElJIl0sIm9yZ2FuaX" +
            "NhdGlvbl9jb21wZXRlbnRfYXV0aG9yaXR5X2NsYWltcyI6eyJhdXRob3JpdHlfaWQiOiJPQkdCUiIsInJlZ2lzdHJhdGlvbl9pZCI" +
            "6IlVua25vd24wMDE1ODAwMDAxSFFRclpBQVgiLCJzdGF0dXMiOiJBY3RpdmUiLCJhdXRob3Jpc2F0aW9ucyI6W3sibWVtYmVyX3N0" +
            "YXRlIjoiR0IiLCJyb2xlcyI6WyJQSVNQIiwiQUlTUCIsIkNCUElJIl19LHsibWVtYmVyX3N0YXRlIjoiSUUiLCJyb2xlcyI6WyJQS" +
            "VNQIiwiQ0JQSUkiLCJBSVNQIl19LHsibWVtYmVyX3N0YXRlIjoiTkwiLCJyb2xlcyI6WyJQSVNQIiwiQUlTUCIsIkNCUElJIl19XX" +
            "0sInNvZnR3YXJlX2xvZ29fdXJpIjoiaHR0cHM6Ly93d3cuZ29vZ2xlLmNvbSIsIm9yZ19zdGF0dXMiOiJBY3RpdmUiLCJvcmdfaWQ" +
            "iOiIwMDE1ODAwMDAxSFFRclpBQVgiLCJvcmdfbmFtZSI6IldTTzIgKFVLKSBMSU1JVEVEIiwib3JnX2NvbnRhY3RzIjpbeyJuYW1l" +
            "IjoiVGVjaG5pY2FsIiwiZW1haWwiOiJzYWNoaW5pc0B3c28yLmNvbSIsInBob25lIjoiKzk0Nzc0Mjc0Mzc0IiwidHlwZSI6IlRlY" +
            "2huaWNhbCJ9LHsibmFtZSI6IkJ1c2luZXNzIiwiZW1haWwiOiJzYWNoaW5pc0B3c28yLmNvbSIsInBob25lIjoiKzk0Nzc0Mjc0Mz" +
            "c0IiwidHlwZSI6IkJ1c2luZXNzIn1dLCJvcmdfandrc19lbmRwb2ludCI6Imh0dHBzOi8va2V5c3RvcmUub3BlbmJhbmtpbmd0ZXN" +
            "0Lm9yZy51ay8wMDE1ODAwMDAxSFFRclpBQVgvMDAxNTgwMDAwMUhRUXJaQUFYLmp3a3MiLCJvcmdfandrc19yZXZva2VkX2VuZHBv" +
            "aW50IjoiaHR0cHM6Ly9rZXlzdG9yZS5vcGVuYmFua2luZ3Rlc3Qub3JnLnVrLzAwMTU4MDAwMDFIUVFyWkFBWC9yZXZva2VkLzAwM" +
            "TU4MDAwMDFIUVFyWkFBWC5qd2tzIiwic29mdHdhcmVfandrc19lbmRwb2ludCI6Imh0dHBzOi8va2V5c3RvcmUub3BlbmJhbmtpbm" +
            "d0ZXN0Lm9yZy51ay8wMDE1ODAwMDAxSFFRclpBQVgvOVp6RkZCeFNMR0VqUFpvZ1JBYnZGZC5qd2tzIiwic29mdHdhcmVfandrc19" +
            "yZXZva2VkX2VuZHBvaW50IjoiaHR0cHM6Ly9rZXlzdG9yZS5vcGVuYmFua2luZ3Rlc3Qub3JnLnVrLzAwMTU4MDAwMDFIUVFyWkFB" +
            "WC9yZXZva2VkLzlaekZGQnhTTEdFalBab2dSQWJ2RmQuandrcyIsInNvZnR3YXJlX3BvbGljeV91cmkiOiJodHRwczovL3d3dy5nb" +
            "29nbGUuY29tIiwic29mdHdhcmVfdG9zX3VyaSI6Imh0dHBzOi8vd3d3Lmdvb2dsZS5jb20iLCJzb2Z0d2FyZV9vbl9iZWhhbGZfb2" +
            "Zfb3JnIjoiV1NPMiBPcGVuIEJhbmtpbmcifQ.qg-osLndAcSkVxgUQ6FovJjsthVgL64P-h6K1o6sEKgAfEPP138JZj7_n8T6qobR" +
            "hL9HfRikBVERHa72lsDM_NZNMvsKGmILYBXIM4mSm6BzT-EZ8LTyBZnIpfX5G8BY9BTKY_Rs1K1TgbE_kKnl0tTx9EZRoLgcgbVBm" +
            "Mqathyz7flzyIGczF-Z8bXPKKSjrr6C7W3aQEZykH-GkiyqgJ-9TBNMvMq8HFpSE_SkTPaBJ_IWMvf5zgsFyK9QwpaSfrMFeD2M5K" +
            "0kkTQEEobQ31ytjPLIayL4L_MP-QMwZMzPdzDdAr7ryfmFzNDWkfBwYiBY4B0ZVzoP2wOIe6wZAA";

    public static final String DCR_REQUEST = "eyJraWQiOiJCa0h4ZUlIS3lNS0Y2U2dHd3FZekxVdlRRZmsiLCJ0eXAiOiJKV1QiLCJh" +
            "bGciOiJQUzI1NiJ9.CiAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJpc3MiOiAiOVp6RkZCeFNMR0VqUFpvZ1JBYnZGZCIs" +
            "CiAgICAgICAgICAgICAgICAiaWF0IjogMTc0OTc5NTgxMSwKICAgICAgICAgICAgICAgICJleHAiOiAxNzQ5Nzk5NDExLAogICAgI" +
            "CAgICAgICAgICAgImp0aSI6ICIxNzQ5Nzk1ODExODMxIiwKICAgICAgICAgICAgICAgICJhdWQiOiAiaHR0cHM6Ly9sb2NhbGJhbm" +
            "suY29tIiwKICAgICAgICAgICAgICAgICJzY29wZSI6ICJhY2NvdW50cyBwYXltZW50cyBmdW5kc2NvbmZpcm1hdGlvbnMiLAogICA" +
            "gICAgICAgICAgICAgInRva2VuX2VuZHBvaW50X2F1dGhfbWV0aG9kIjogInByaXZhdGVfa2V5X2p3dCIsCiAgICAgICAgICAgICAg" +
            "ICAidG9rZW5fZW5kcG9pbnRfYXV0aF9zaWduaW5nX2FsZyI6ICJQUzI1NiIsCiAgICAgICAgICAgICAgICAiZ3JhbnRfdHlwZXMiO" +
            "iBbCiAgICAgICAgICAgICAgICAgICAgImF1dGhvcml6YXRpb25fY29kZSIsCiAgICAgICAgICAgICAgICAgICAgImNsaWVudF9jcm" +
            "VkZW50aWFscyIsCiAgICAgICAgICAgICAgICAgICAgInJlZnJlc2hfdG9rZW4iCiAgICAgICAgICAgICAgICAgICAgXSwKICAgICA" +
            "gICAgICAgICAgICJyZXNwb25zZV90eXBlcyI6IFsKICAgICAgICAgICAgICAgICAgICAiY29kZSBpZF90b2tlbiIKICAgICAgICAg" +
            "ICAgICAgICAgICBdLAogICAgICAgICAgICAgICAgImlkX3Rva2VuX3NpZ25lZF9yZXNwb25zZV9hbGciOiAiUFMyNTYiLAogICAgI" +
            "CAgICAgICAgICAgImlkX3Rva2VuX2VuY3J5cHRlZF9yZXNwb25zZV9hbGciOiAiUlNBLU9BRVAiLAogICAgICAgICAgICAgICAgIm" +
            "lkX3Rva2VuX2VuY3J5cHRlZF9yZXNwb25zZV9lbmMiOiAiQTI1NkdDTSIsCiAgICAgICAgICAgICAgICAicmVxdWVzdF9vYmplY3R" +
            "fc2lnbmluZ19hbGciOiAiUFMyNTYiLCAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICJhcHBsaWNh" +
            "dGlvbl90eXBlIjogIndlYiIsCiAgICAgICAgICAgICAgICAic29mdHdhcmVfaWQiOiAiOVp6RkZCeFNMR0VqUFpvZ1JBYnZGZCIsC" +
            "iAgICAgICAgICAgICAgICAicmVkaXJlY3RfdXJpcyI6IFsKICAgICAgICAgICAgICAgICAgICAiaHR0cHM6Ly93d3cuZ29vZ2xlLm" +
            "NvbS9yZWRpcmVjdHMvcmVkaXJlY3QxIgogICAgICAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICAic29mdHdhcmVfc3R" +
            "hdGVtZW50IjogImV5SmhiR2NpT2lKUVV6STFOaUlzSW10cFpDSTZJa0pyU0hobFNVaExlVTFMUmpaVFowZDNjVmw2VEZWMlZGRm1h" +
            "eUlzSW5SNWNDSTZJa3BYVkNKOS5leUpwYzNNaU9pSlBjR1Z1UW1GdWEybHVaeUJNZEdRaUxDSnBZWFFpT2pFM016VTRPVEl6TURNc" +
            "0ltcDBhU0k2SW1ZNE9EUTBOVFV5WTJWaFpUUTVZVE1pTENKemIyWjBkMkZ5WlY5bGJuWnBjbTl1YldWdWRDSTZJbk5oYm1SaWIzZ2" +
            "lMQ0p6YjJaMGQyRnlaVjl0YjJSbElqb2lWR1Z6ZENJc0luTnZablIzWVhKbFgybGtJam9pT1ZwNlJrWkNlRk5NUjBWcVVGcHZaMUp" +
            "CWW5aR1pDSXNJbk52Wm5SM1lYSmxYMk5zYVdWdWRGOXBaQ0k2SWpsYWVrWkdRbmhUVEVkRmFsQmFiMmRTUVdKMlJtUWlMQ0p6YjJa" +
            "MGQyRnlaVjlqYkdsbGJuUmZibUZ0WlNJNklsZFRUeklnVDNCbGJpQkNZVzVyYVc1bklGUlFVQ0FvVTJGdVpHSnZlQ2tpTENKemIyW" +
            "jBkMkZ5WlY5amJHbGxiblJmWkdWelkzSnBjSFJwYjI0aU9pSlhVMDh5SUU5d1pXNGdRbUZ1YTJsdVp5QlVVRkFnWm05eUlIUmxjM1" +
            "JwYm1jaUxDSnpiMlowZDJGeVpWOTJaWEp6YVc5dUlqb2lNUzQxSWl3aWMyOW1kSGRoY21WZlkyeHBaVzUwWDNWeWFTSTZJbWgwZEh" +
            "Cek9pOHZkM2QzTG1kdmIyZHNaUzVqYjIwaUxDSnpiMlowZDJGeVpWOXlaV1JwY21WamRGOTFjbWx6SWpwYkltaDBkSEJ6T2k4dmQz" +
            "ZDNMbWR2YjJkc1pTNWpiMjB2Y21Wa2FYSmxZM1J6TDNKbFpHbHlaV04wTVNKZExDSnpiMlowZDJGeVpWOXliMnhsY3lJNld5SlFTV" +
            "k5RSWl3aVFVbFRVQ0lzSWtOQ1VFbEpJbDBzSW05eVoyRnVhWE5oZEdsdmJsOWpiMjF3WlhSbGJuUmZZWFYwYUc5eWFYUjVYMk5zWV" +
            "dsdGN5STZleUpoZFhSb2IzSnBkSGxmYVdRaU9pSlBRa2RDVWlJc0luSmxaMmx6ZEhKaGRHbHZibDlwWkNJNklsVnVhMjV2ZDI0d01" +
            "ERTFPREF3TURBeFNGRlJjbHBCUVZnaUxDSnpkR0YwZFhNaU9pSkJZM1JwZG1VaUxDSmhkWFJvYjNKcGMyRjBhVzl1Y3lJNlczc2li" +
            "V1Z0WW1WeVgzTjBZWFJsSWpvaVIwSWlMQ0p5YjJ4bGN5STZXeUpRU1ZOUUlpd2lRVWxUVUNJc0lrTkNVRWxKSWwxOUxIc2liV1Z0W" +
            "W1WeVgzTjBZWFJsSWpvaVNVVWlMQ0p5YjJ4bGN5STZXeUpRU1ZOUUlpd2lRMEpRU1VraUxDSkJTVk5RSWwxOUxIc2liV1Z0WW1WeV" +
            "gzTjBZWFJsSWpvaVRrd2lMQ0p5YjJ4bGN5STZXeUpRU1ZOUUlpd2lRVWxUVUNJc0lrTkNVRWxKSWwxOVhYMHNJbk52Wm5SM1lYSmx" +
            "YMnh2WjI5ZmRYSnBJam9pYUhSMGNITTZMeTkzZDNjdVoyOXZaMnhsTG1OdmJTSXNJbTl5WjE5emRHRjBkWE1pT2lKQlkzUnBkbVVp" +
            "TENKdmNtZGZhV1FpT2lJd01ERTFPREF3TURBeFNGRlJjbHBCUVZnaUxDSnZjbWRmYm1GdFpTSTZJbGRUVHpJZ0tGVkxLU0JNU1UxS" +
            "lZFVkVJaXdpYjNKblgyTnZiblJoWTNSeklqcGJleUp1WVcxbElqb2lWR1ZqYUc1cFkyRnNJaXdpWlcxaGFXd2lPaUp6WVdOb2FXNX" +
            "BjMEIzYzI4eUxtTnZiU0lzSW5Cb2IyNWxJam9pS3prME56YzBNamMwTXpjMElpd2lkSGx3WlNJNklsUmxZMmh1YVdOaGJDSjlMSHN" +
            "pYm1GdFpTSTZJa0oxYzJsdVpYTnpJaXdpWlcxaGFXd2lPaUp6WVdOb2FXNXBjMEIzYzI4eUxtTnZiU0lzSW5Cb2IyNWxJam9pS3pr" +
            "ME56YzBNamMwTXpjMElpd2lkSGx3WlNJNklrSjFjMmx1WlhOekluMWRMQ0p2Y21kZmFuZHJjMTlsYm1Sd2IybHVkQ0k2SW1oMGRIQ" +
            "npPaTh2YTJWNWMzUnZjbVV1YjNCbGJtSmhibXRwYm1kMFpYTjBMbTl5Wnk1MWF5OHdNREUxT0RBd01EQXhTRkZSY2xwQlFWZ3ZNRE" +
            "F4TlRnd01EQXdNVWhSVVhKYVFVRllMbXAzYTNNaUxDSnZjbWRmYW5kcmMxOXlaWFp2YTJWa1gyVnVaSEJ2YVc1MElqb2lhSFIwY0h" +
            "NNkx5OXJaWGx6ZEc5eVpTNXZjR1Z1WW1GdWEybHVaM1JsYzNRdWIzSm5MblZyTHpBd01UVTRNREF3TURGSVVWRnlXa0ZCV0M5eVpY" +
            "WnZhMlZrTHpBd01UVTRNREF3TURGSVVWRnlXa0ZCV0M1cWQydHpJaXdpYzI5bWRIZGhjbVZmYW5kcmMxOWxibVJ3YjJsdWRDSTZJb" +
            "WgwZEhCek9pOHZhMlY1YzNSdmNtVXViM0JsYm1KaGJtdHBibWQwWlhOMExtOXlaeTUxYXk4d01ERTFPREF3TURBeFNGRlJjbHBCUV" +
            "Zndk9WcDZSa1pDZUZOTVIwVnFVRnB2WjFKQlluWkdaQzVxZDJ0eklpd2ljMjltZEhkaGNtVmZhbmRyYzE5eVpYWnZhMlZrWDJWdVp" +
            "IQnZhVzUwSWpvaWFIUjBjSE02THk5clpYbHpkRzl5WlM1dmNHVnVZbUZ1YTJsdVozUmxjM1F1YjNKbkxuVnJMekF3TVRVNE1EQXdN" +
            "REZJVVZGeVdrRkJXQzl5WlhadmEyVmtMemxhZWtaR1FuaFRURWRGYWxCYWIyZFNRV0oyUm1RdWFuZHJjeUlzSW5OdlpuUjNZWEpsW" +
            "DNCdmJHbGplVjkxY21raU9pSm9kSFJ3Y3pvdkwzZDNkeTVuYjI5bmJHVXVZMjl0SWl3aWMyOW1kSGRoY21WZmRHOXpYM1Z5YVNJNk" +
            "ltaDBkSEJ6T2k4dmQzZDNMbWR2YjJkc1pTNWpiMjBpTENKemIyWjBkMkZ5WlY5dmJsOWlaV2hoYkdaZmIyWmZiM0puSWpvaVYxTlB" +
            "NaUJQY0dWdUlFSmhibXRwYm1jaWZRLnFnLW9zTG5kQWNTa1Z4Z1VRNkZvdkpqc3RoVmdMNjRQLWg2SzFvNnNFS2dBZkVQUDEzOEpa" +
            "ajdfbjhUNnFvYlJoTDlIZlJpa0JWRVJIYTcybHNETV9OWk5NdnNLR21JTFlCWElNNG1TbTZCelQtRVo4TFR5QlpuSXBmWDVHOEJZOU" +
            "JUS1lfUnMxSzFUZ2JFX2tLbmwwdFR4OUVaUm9MZ2NnYlZCbU1xYXRoeXo3Zmx6eUlHY3pGLVo4YlhQS0tTanJyNkM3VzNhUUVaeWtI" +
            "LUdraXlxZ0otOVRCTk12TXE4SEZwU0VfU2tUUGFCSl9JV012ZjV6Z3NGeUs5UXdwYVNmck1GZUQyTTVLMGtrVFFFRW9iUTMxeXRqUE" +
            "xJYXlMNExfTVAtUU13Wk16UGR6RGRBcjdyeWZtRnpORFdrZkJ3WWlCWTRCMFpWem9QMndPSWU2d1pBQSIKICAgICAgICAgICAgfQog" +
            "ICAgICAgICA.SkdCBpPbHIYo4UAI-vlwoAOTDuTaM7gJ3srgVdBrzhIlVvKEHqOnSW4fdGRBy2KSjm3cjGSlQnuMzWV1tUGrydjqs1" +
            "CRUJ4xr19XmrzYSaM2U9AUiGjmHMOAnAneNMrSFDyBz1SKPgBJVXCZflYfE-WdeupM9el9448Msg0wH4ItM7HYCqX-rWvu6Xt0I-pz" +
            "rXCVG96wzkGGdqTmi1uQk-88w7wcsxczZEhFWdnx3AiHQRhEMMQTDZJihCT9z26bfv8-AeQb4bfbcrO4dnEiwITbtaC11m8ErPt8lX" +
            "7h6PWhS6rRo_BHwSIo0fJRHfucqrVpOBocu4ikj4DtvpqOKA";

    public static final String DECODED_DCR_REQUEST = "{\n" +
            "  \"iss\": \"9ZzFFBxSLGEjPZogRAbvFd\",\n" +
            "  \"iat\": 1749795811,\n" +
            "  \"exp\": 1749799411,\n" +
            "  \"jti\": \"1749795811831\",\n" +
            "  \"aud\": \"https://localbank.com\",\n" +
            "  \"scope\": \"accounts payments fundsconfirmations\",\n" +
            "  \"token_endpoint_auth_method\": \"private_key_jwt\",\n" +
            "  \"token_endpoint_auth_signing_alg\": \"PS256\",\n" +
            "  \"grant_types\": [\n" +
            "    \"authorization_code\",\n" +
            "    \"client_credentials\",\n" +
            "    \"refresh_token\"\n" +
            "  ],\n" +
            "  \"response_types\": [\n" +
            "    \"code id_token\"\n" +
            "  ],\n" +
            "  \"id_token_signed_response_alg\": \"PS256\",\n" +
            "  \"id_token_encrypted_response_alg\": \"RSA-OAEP\",\n" +
            "  \"id_token_encrypted_response_enc\": \"A256GCM\",\n" +
            "  \"request_object_signing_alg\": \"PS256\",\n" +
            "  \"application_type\": \"web\",\n" +
            "  \"software_id\": \"9ZzFFBxSLGEjPZogRAbvFd\",\n" +
            "  \"redirect_uris\": [\n" +
            "    \"https://www.google.com/redirects/redirect1\"\n" +
            "  ],\n" +
            "  \"software_statement\": \"" + SSA + "\"\n" +
            "}";

    public static final String DECODED_SSA = "{\n" +
            "  \"iss\": \"OpenBanking Ltd\",\n" +
            "  \"iat\": 1735892303,\n" +
            "  \"jti\": \"f8844552ceae49a3\",\n" +
            "  \"software_environment\": \"sandbox\",\n" +
            "  \"software_mode\": \"Test\",\n" +
            "  \"software_id\": \"9ZzFFBxSLGEjPZogRAbvFd\",\n" +
            "  \"software_client_id\": \"9ZzFFBxSLGEjPZogRAbvFd\",\n" +
            "  \"software_client_name\": \"WSO2 Open Banking TPP (Sandbox)\",\n" +
            "  \"software_client_description\": \"WSO2 Open Banking TPP for testing\",\n" +
            "  \"software_version\": \"1.5\",\n" +
            "  \"software_client_uri\": \"https://www.google.com\",\n" +
            "  \"software_redirect_uris\": [\n" +
            "    \"https://www.google.com/redirects/redirect1\"\n" +
            "  ],\n" +
            "  \"software_roles\": [\n" +
            "    \"PISP\",\n" +
            "    \"AISP\",\n" +
            "    \"CBPII\"\n" +
            "  ],\n" +
            "  \"organisation_competent_authority_claims\": {\n" +
            "    \"authority_id\": \"OBGBR\",\n" +
            "    \"registration_id\": \"Unknown0015800001HQQrZAAX\",\n" +
            "    \"status\": \"Active\",\n" +
            "    \"authorisations\": [\n" +
            "      {\n" +
            "        \"member_state\": \"GB\",\n" +
            "        \"roles\": [\n" +
            "          \"PISP\",\n" +
            "          \"AISP\",\n" +
            "          \"CBPII\"\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"member_state\": \"IE\",\n" +
            "        \"roles\": [\n" +
            "          \"PISP\",\n" +
            "          \"CBPII\",\n" +
            "          \"AISP\"\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"member_state\": \"NL\",\n" +
            "        \"roles\": [\n" +
            "          \"PISP\",\n" +
            "          \"AISP\",\n" +
            "          \"CBPII\"\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"software_logo_uri\": \"https://www.google.com\",\n" +
            "  \"org_status\": \"Active\",\n" +
            "  \"org_id\": \"0015800001HQQrZAAX\",\n" +
            "  \"org_name\": \"WSO2 (UK) LIMITED\",\n" +
            "  \"org_contacts\": [\n" +
            "    {\n" +
            "      \"name\": \"Technical\",\n" +
            "      \"email\": \"sachinis@wso2.com\",\n" +
            "      \"phone\": \"+94774274374\",\n" +
            "      \"type\": \"Technical\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"Business\",\n" +
            "      \"email\": \"sachinis@wso2.com\",\n" +
            "      \"phone\": \"+94774274374\",\n" +
            "      \"type\": \"Business\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"org_jwks_endpoint\": \"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/" +
            "0015800001HQQrZAAX.jwks\",\n" +
            "  \"org_jwks_revoked_endpoint\": \"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/" +
            "revoked/0015800001HQQrZAAX.jwks\",\n" +
            "  \"software_jwks_endpoint\": \"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/" +
            "9ZzFFBxSLGEjPZogRAbvFd.jwks\",\n" +
            "  \"software_jwks_revoked_endpoint\": \"https://keystore.openbankingtest.org.uk/0015800001HQQrZAAX/" +
            "revoked/9ZzFFBxSLGEjPZogRAbvFd.jwks\",\n" +
            "  \"software_policy_uri\": \"https://www.google.com\",\n" +
            "  \"software_tos_uri\": \"https://www.google.com\",\n" +
            "  \"software_on_behalf_of_org\": \"WSO2 Open Banking\"\n" +
            "}";

    public static final String DECODED_ACCESS_TOKEN = "{\n" +
            "  \"sub\": \"j4Bzin1ok2D4woQRo2xPcCbwBFUa\",\n" +
            "  \"aut\": \"APPLICATION\",\n" +
            "  \"binding_type\": \"certificate\",\n" +
            "  \"iss\": \"https://localhost:9446/oauth2/token\",\n" +
            "  \"client_id\": \"j4Bzin1ok2D4woQRo2xPcCbwBFUa\",\n" +
            "  \"aud\": \"j4Bzin1ok2D4woQRo2xPcCbwBFUa\",\n" +
            "  \"nbf\": 1749795813,\n" +
            "  \"azp\": \"j4Bzin1ok2D4woQRo2xPcCbwBFUa\",\n" +
            "  \"org_id\": \"10084a8d-113f-4211-a0d5-efe36b082211\",\n" +
            "  \"scope\": \"accounts\",\n" +
            "  \"cnf\": {\n" +
            "    \"x5t#S256\": \"sN2eQi7jz914eg8eqayvUXX4LzaYsLD8jkPEJFajrzo\"\n" +
            "  },\n" +
            "  \"exp\": 1749799413,\n" +
            "  \"org_name\": \"Super\",\n" +
            "  \"iat\": 1749795813,\n" +
            "  \"binding_ref\": \"a8e4d7e54851add477e484683a12223f\",\n" +
            "  \"jti\": \"81850121-1639-4b5d-84ef-932a88bfff83\"\n" +
            "}";
}
