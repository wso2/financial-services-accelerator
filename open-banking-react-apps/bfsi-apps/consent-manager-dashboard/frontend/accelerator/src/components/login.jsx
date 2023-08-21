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

import React from "react";
import { FormattedMessage } from "react-intl";

import { useAuthContext } from "@bfsi-react/auth";

/**
 * Renders a redirection message for a second and redirect to IS login page.
 *
 */
export const Login = () => {
  const { signIn } = useAuthContext();
  const handleSignIn = () => {
    setTimeout(() => signIn(), 1000);
  };

  return (
    <div className="container-sm text-center">
      <h2>
        <FormattedMessage
          id="app.common.login.message"
          defaultMessage="Hang Tight!"
        />
      </h2>
      <p>
        <FormattedMessage
          id="app.common.login.description"
          defaultMessage="You're being redirected to the login page..."
        />
      </p>
      {handleSignIn()}
    </div>
  );
};

export default Login;
