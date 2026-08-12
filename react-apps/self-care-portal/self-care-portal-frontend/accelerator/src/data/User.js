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
import axios from "axios";
import Cookies from "js-cookie";
import { CONFIG } from "../config";

export default class User {
  constructor(userInfo) {
    if (userInfo && userInfo.isLogged) {
      this.isLogged = true;
      this.email = userInfo.email;
      this.role = userInfo.role;
    } else {
      this.isLogged = false;
    }
  }

  /**
   * Fetch the logged-in user's info from the backend. The id token used to derive this info is decoded
   * server-side from its HttpOnly cookies and never reaches the browser as JavaScript-readable data.
   * @returns {Promise<User>} - Resolves to a logged-in User on success, or a logged-out User otherwise
   */
  static async load() {
    try {
      const response = await axios.get(`${CONFIG.BACKEND_URL}/userinfo`, {
        withCredentials: true,
      });
      return new User(response.data);
    } catch (error) {
      return new User(null);
    }
  }
}

export const getAccessToken = () => {
  const accessTokenPart1 = Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P1);
  const accessTokenPart2 = Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P2);

  if (!accessTokenPart1 || !accessTokenPart2) {
    return null;
  }
  return accessTokenPart1 + accessTokenPart2;
};

User.CONST = {
  OB_SCP_ACC_TOKEN_P1: "OB_SCP_AT_P1",
  OB_SCP_ACC_TOKEN_P2: "OB_SCP_AT_P2",
  OB_SCP_REF_TOKEN_P1: "OB_SCP_RT_P1",
  OB_SCP_REF_TOKEN_P2: "OB_SCP_RT_P2",
};
