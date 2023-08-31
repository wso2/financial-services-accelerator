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

import React, { useContext } from 'react'
import { Outlet, Navigate } from "react-router-dom";

import { AuthContext } from './auth-provider';
import { SecureRoutesProps } from './types';

/**
 * Custom Route component to easily secure routes.
 * 
 * @example
 * ```js
 * import React from "react";
 * import { SecureRoutes } from "@bfsi-react/auth";
 * 
 * function App() {
 *   return (
 *     <div className="App">
 *       <Routes>
 *         // Protected Routes
 *         <Route element={<SecureRoutes redirectPath="/login" />}>
 *           <Route element={<ProtectedPage1 />} path="/protected-route-1" />
 *         </Route>
 * 
 *         // Common Routes
 *         <Route element={<Login />} path="/login" />
 *       </Routes>
 *     </div>
 *   );
 * }
 * ``` 
 */
const SecureRoutes = ({ redirectPath }: SecureRoutesProps): React.JSX.Element => {
    const { isAuthenticated } = useContext(AuthContext);

    return (
        isAuthenticated ? <Outlet /> : <Navigate to={redirectPath} />
    )
}

export default SecureRoutes;
