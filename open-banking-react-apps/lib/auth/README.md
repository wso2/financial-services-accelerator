# Use the API

Follow the instructions given below to implement each use case.

## Configure

`AuthProvider` will provide the session state which contains information such as the authenticated user's display name, email address etc. as well as the methods required to implement authentication in the React app.

```js
import React from "react";
import { AuthProvider } from "@bfsi-react/auth";

const config = {
  accessTokenCookieName: "access_token_cookie_name",
  refreshTokenCookieName: "refresh_token_cookie_name",
  idTokenCookieName: "id_token_cookie_name",
  baseUrl: "https://localhost:9446",
};

function App() {
  return (
    <AuthProvider config={config}>
      <div className="App">...</div>
    </AuthProvider>
  );
}

export default App;
```

`AuthProvider` takes a config object as a prop which is used to initialize the instance.

Details of the config parameters are given below

| Parameter              | Description                                             |
| ---------------------- | ------------------------------------------------------- |
| accessTokenCookieName  | Uses to fetch access token from browser cookie storage  |
| refreshTokenCookieName | Uses to fetch refresh token from browser cookie storage |
| idTokenCookieName      | Uses to fetch id token from browser cookie storage      |
| baseUrl                | Base URL of the Identity Server                         |

## Access the session state

The useAuthContext() hook provided by the module could be used to access the session state that contains information such as the email address of the authenticated user and to access the methods that are required for implementing authentication.

> Once the root component is wrapped with AuthProvider, useAuthContext() hook can be used anywhere within the application.

```js
import React from "react";
import { useAuthContext } from "@bfsi-react/auth";

export const Page = () => {
  const { state } = useAuthContext();

  return (
    <div>
      ...
      <p>{state.username}</p>
      ...
    </div>
  );
};
```

Few common methods that you can access with useAuthContext() are listed below. These will be helpful when implementing authentication capabilities in your application.

- state object - This will contain attributes such as whether a user is currently logged in, the username of the currently logged-in user etc.
- signIn - Initiate a login request to IS
- signOut - Logout the user from IS and clear any authentication data from the browser storage.
- isAuthenticated - Check whether there is an authenticated user session. Based on the result you can decide to change the application view/behaviour.
- getDecodedIDToken - Get the decoded id_token obtained in the authentication response. From there you can derive more information such as additional user-attributes.
- getIDToken - Get the id_token (JWT) obtained in the authentication response.
- getPartialAccessToken - Get the first part of the access_token obtained in the authentication response.
- getPartialRefreshToken - Get the first part of the refresh_token obtained in the authentication response.

## Add Routing

If your application needs routing, the module provides a component called SecureRoutes, which is implemented with react-router-dom@v6. This component allows you to easily secure your routes.

```js
import React from "react";
import { SecureRoutes } from "@bfsi-react/auth";

function App() {
  return (
    <div className="App">
      <Routes>
        {/* Protected Routes */}
        <Route element={<SecureRoutes redirectPath="/login" />}>
          <Route element={<ProtectedPage1 />} path="/protected-route-1" />
        </Route>

        {/* Common Routes */}
        <Route element={<Login />} path="/login" />
      </Routes>
    </div>
  );
}
```
