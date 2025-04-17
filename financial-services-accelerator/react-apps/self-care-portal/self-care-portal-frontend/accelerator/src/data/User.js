/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
import { id } from "date-fns/locale";
import Cookies from "js-cookie";

export default class User {
  constructor() {
    let idToken = getIdToken();

    if (idToken) {
      this.isLogged = true;
      this.idToken = idToken;
      this.email = decodeIdToken(idToken).sub;
      this.role = decodeIdToken(idToken).user_role;
    } else {
      this.isLogged = false;
    }
  }
}

/**
 * Concat id_token cookies and return token
 * @returns {String|null} - If cookies found, return its value, Else null value is returned
 */
const getIdToken = () => {
  const idTokenPart1 = Cookies.get(User.CONST.OB_SCP_ID_TOKEN_P1);
  const idTokenPart2 = Cookies.get(User.CONST.OB_SCP_ID_TOKEN_P2);

  if (!idTokenPart1 || !idTokenPart2) {
    return null;
  }
  return idTokenPart1 + idTokenPart2;
};

export const getAccessToken = () => {
  const accessTokenPart1 = Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P1);
  const accessTokenPart2 = Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P2);

  if (!accessTokenPart1 || !accessTokenPart2) {
    return null;
  }
  return accessTokenPart1 + accessTokenPart2;
};

export function decodeIdToken(token) {
  return JSON.parse(atob(token.split(".")[1]));
}

User.CONST = {
  OB_SCP_ACC_TOKEN_P1: "OB_SCP_AT_P1",
  OB_SCP_ACC_TOKEN_P2: "OB_SCP_AT_P2",
  OB_SCP_ID_TOKEN_P1: "OB_SCP_IT_P1",
  OB_SCP_ID_TOKEN_P2: "OB_SCP_IT_P2",
  OB_SCP_REF_TOKEN_P1: "OB_SCP_RT_P1",
  OB_SCP_REF_TOKEN_P2: "OB_SCP_RT_P2",
};
