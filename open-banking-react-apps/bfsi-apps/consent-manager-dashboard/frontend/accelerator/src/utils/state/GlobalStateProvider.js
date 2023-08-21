/* ----- GlobalStateProvider.js ----- */
import React, { useState } from "react";
import GlobalStateContext from "./GlobalStateContext";

function GlobalStateProvider({ children }) {
  const [globalState, setGlobalState] = useState({
    isSidebarOpen: true,
    initialRender: true,
    page: null,
  });

  return (
    <GlobalStateContext.Provider value={{ globalState, setGlobalState }}>
      {children}
    </GlobalStateContext.Provider>
  );
}

export default GlobalStateProvider;
