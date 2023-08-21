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

const getCookieName = (tokenName: string, tokenPart: string): string => {
    return tokenName + tokenPart;
}

/**
 * Generates the first part of the cookie name.
 */
export const getCookieNameP1 = (tokenName: string): string => {
    return getCookieName(tokenName, "_P1")
}

/**
 * Generates the second part of the cookie name.
 */
export const getCookieNameP2 = (tokenName: string): string => {
    return getCookieName(tokenName, "_P2")
}

/**
 * Decodeds a JWT payload.
 */
export const decodeJWTBody = (jwt: string): { [key: string]: string } => {
    return jwt ? JSON.parse(atob(jwt.split(".")[1])) : "";
}

/**
 * Returns the expired status of the token.
 */
export const isTokenExpired = (expiryInSeconds: string): boolean => {
    const dateInSeconds = Date.now() / 1000;
    return Number(expiryInSeconds) < dateInSeconds;
};
