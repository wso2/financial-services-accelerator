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

import Cookies from "js-cookie";

import { AuthConfig, AuthState } from "./types";
import { decodeJWTBody, getCookieNameP1, getCookieNameP2, isTokenExpired } from "./utils";

/**
 * Contains the core authentication logic.
 */
export class AuthenticationCore {
  private _config: AuthConfig;

  public constructor (config: AuthConfig) {
    this._config = config;
  }

  /**
   * Generates the ID token from cookies.
   * @return {String} - If cookies found, return its value, Else empty value is returned
   *
   * @example
   * ```
   * const { getIDToken } = useAuthContext();
   * const idToken = getIDToken();
   * ```
   * @memberof AuthenticationCore
   *
   */
  public getIDToken = (): string => {
    const idTokenPart1 = Cookies.get(getCookieNameP1(this._config.idTokenCookieName));
    const idTokenPart2 = Cookies.get(getCookieNameP2(this._config.idTokenCookieName));

    if (!idTokenPart1 || !idTokenPart2) {
      return "";
    }
    return idTokenPart1 + idTokenPart2;
  };

  /**
   * Generates an part of the access token from cookies.
   * @return {String} - If cookies found, return its value, Else empty value is returned
   *
   * @example
   * ```
   * const { getPartialAccessToken } = useAuthContext();
   * const partialAccessToken = getPartialAccessToken();
   * ```
   * @memberof AuthenticationCore
   */
  public getPartialAccessToken = (): string => {
    return Cookies.get(getCookieNameP1(this._config.accessTokenCookieName)) ?? "";
  };

  /**
   * Generates a part of the refresh token from cookies.
   * @return {String} - If cookies found, return its value, Else empty value is returned
   *
   * @example
   * ```
   * const { getPartialRefreshToken } = useAuthContext();
   * const partialRefreshToken = getPartialRefreshToken();
   * ```
   * @memberof AuthenticationCore
   */
  public getPartialRefreshToken = (): string => {
    return Cookies.get(getCookieNameP1(this._config.refreshTokenCookieName)) ?? "";
  };

  /**
   * Generates the decoded ID token.
   * @return {Object} - If token is present, return its decoded value, Else empty object is returned
   *
   * @example
   * ```
   * const { getDecodedIDToken } = useAuthContext();
   * const decodedIDToken = getDecodedIDToken();
   * ```
   * @memberof AuthenticationCore
   */
  public getDecodedIDToken = (): { [key: string]: string } => {
    const idToken = this.getIDToken();
    return idToken ? decodeJWTBody(idToken) : {};
  };

  /**
   * Check whether there is an authenticated user session.
   * @return {Boolean} - If user is present, return true, else false is returned
   *
   * @example
   * ```
   * const { isAuthenticated } = useAuthContext();
   * const isAuthenticated = isAuthenticated();
   * ```
   * @memberof AuthenticationCore
   */
  public isAuthenticated = (): boolean => {
    const accessToken = this.getPartialAccessToken();
    const refreshToken = this.getPartialRefreshToken();
    const decodedIDToken = this.getDecodedIDToken();
    const isExpired = isTokenExpired(decodedIDToken?.exp)

    return !!(accessToken && refreshToken) && !isExpired;
  };

  /**
   * Generates the context state.
   * @return {AuthState} - Is authenticated user, return auth state, else return null
   *
   * @memberof AuthenticationCore
   */
  public generateState = (): AuthState => {
    const decodedIdToken = this.getDecodedIDToken();

    return {
      isAuthenticated: this.isAuthenticated(),
      isLoading: false,
      username: decodedIdToken?.sub,
      clientId: decodedIdToken?.aud,
      role: decodedIdToken?.user_role
    };
  };

  /**
   * Initiate a login request and process the response to obtain authentication response.
   *
   * @example
   * ```
   * const { signIn } = useAuthContext();
   * signIn();
   * ```
   * @memberof AuthenticationCore
   */
  public signIn = (): void => {
    const authorizeUrl = this._config.baseUrl + "/consentmgr/scp_oauth2_authorize";

    console.debug("redirecting to: ", authorizeUrl);
    window.location.href = authorizeUrl;
  };

  /**
   * Logout the user and clear any authentication data from the storage.
   *
   * @example
   * ```
   * const { signOut } = useAuthContext();
   * signOut();
   * ```
   * @memberof AuthenticationCore
   */
  public signOut = (): void => {
    const logoutUrl = this._config.baseUrl + "/consentmgr/scp_oauth2_logout";

    console.debug("redirecting to: ", logoutUrl);
    window.location.href = logoutUrl;
  };
}
