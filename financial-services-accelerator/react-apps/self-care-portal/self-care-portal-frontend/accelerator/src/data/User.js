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
import Cookies from "js-cookie";
import axios from "axios";
import { CONFIG } from "../config";

export default class User {
  constructor(isLogged, email, role) {
    this.isLogged = !!isLogged;
    this.email = email;
    this.role = role;
  }

  /**
   * Builds a User by asking the backend to decode the id_token (kept in httpOnly
   * cookies) and return only the email/role claims needed by the UI.
   * @returns {Promise<User>}
   */
  static async load() {
    const accessTokenPart1 = Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P1);
    if (!accessTokenPart1) {
      return new User(false);
    }

    try {
      const response = await axios.get(`${CONFIG.BACKEND_URL}/userinfo`, {
        headers: {
          Authorization: `Bearer ${accessTokenPart1}`,
        },
      });
      return new User(true, response.data.email, response.data.role);
    } catch (error) {
      return new User(false);
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
};
