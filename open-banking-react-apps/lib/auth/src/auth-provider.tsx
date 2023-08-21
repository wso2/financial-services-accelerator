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

import React, { createContext } from 'react';

import { AuthenticationCore } from './auth-core';
import { AuthContextType, AuthProviderProps, AuthState } from './types';

export const AuthContext = createContext<AuthContextType>({});

/**
 * This component is used to setup the auth context for a tree. 
 * Usually, this component will wrap an app's root component so that the entire app will be within the 
 * configured auth context. 
 * 
 * @example
 * ```js
 * import { AuthProvider } from "@bfsi-react/auth";
 * 
 * const config = {
 *  accessTokenCookieName: "access_token_cookie_name",
 *  refreshTokenCookieName: "refresh_token_cookie_name",
 *  idTokenCookieName: "id_token_cookie_name",
 *  baseUrl: "https://localhost:9446",
 * };
 * 
 * function App() {
 *  return (
 *    <AuthProvider config={config}>
 *      <div className="App">...</div>
 *    </AuthProvider>
 *  );
 * }
 * ```
 */
const AuthProvider = ({ children, config }: AuthProviderProps): React.JSX.Element => {

  const authenticationCore: AuthenticationCore = new AuthenticationCore(config);
  const signIn: () => void = authenticationCore.signIn;
  const signOut: () => void = authenticationCore.signOut;
  const isAuthenticated: boolean = authenticationCore.isAuthenticated();
  const getIDToken: () => string = authenticationCore.getIDToken;
  const getDecodedIDToken: () => { [key: string]: string } = authenticationCore.getDecodedIDToken;
  const getPartialAccessToken: () => string = authenticationCore.getPartialAccessToken;
  const getPartialRefreshToken: () => string = authenticationCore.getPartialRefreshToken;
  const state: AuthState = authenticationCore.generateState();

  return (
    <AuthContext.Provider value={{
      state,
      signIn,
      signOut,
      isAuthenticated,
      getIDToken,
      getDecodedIDToken,
      getPartialAccessToken,
      getPartialRefreshToken
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export default AuthProvider;
