/* ----- SidebarItem.js ----- */
import React, { useContext, useEffect, useState } from "react";
import "./SidebarItem.scss";

// Context
import GlobalStateContext from "../../../../utils/state/GlobalStateContext";
import { ThemeContext } from "@bfsi-react/bfsi-ui";

// Libraries & Packages
import { motion } from "framer-motion";
import { Link } from "react-router-dom";

// Util
import { setSidebarAnimationProps } from "../../SidebarUtil";
import { setSidebarItemColorStyles } from "./SidebarItemStyles";

function SidebarItem({ current, icon, text }) {
  // State
  const { globalState } = useContext(GlobalStateContext);
  const { theme } = useContext(ThemeContext);

  const [initialRender, setInitialRender] = useState(true);

  // Effects
  useEffect(() => {
    setInitialRender(false);
  }, []);
  return (
    <div
      className="sidebarItem"
      style={{
        backgroundColor: setSidebarItemColorStyles(
          current,
          globalState.page,
          theme
        ).backgroundColor,
      }}
    >
      <Link to={`/${current}`}>
        <div
          className="sidebarItem__icon"
          style={{ minWidth: globalState.isSidebarOpen ? "28px" : "100%" }}
        >
          <div
            className="sidebarItem__icon__container"
            style={{
              color: setSidebarItemColorStyles(current, globalState.page, theme)
                .color,
            }}
          >
            {icon}
          </div>
        </div>
        <div
          className="sidebarItem__text"
          style={{
            display: globalState.isSidebarOpen ? "flex" : "none",
            width: globalState.isSidebarOpen && "calc(100% - 28px)",
          }}
        >
          <motion.p
            key={globalState.isSidebarOpen}
            style={{
              color: setSidebarItemColorStyles(current, globalState.page, theme)
                .color,
            }}
            {...setSidebarAnimationProps(initialRender)}
          >
            {text}
          </motion.p>
        </div>
      </Link>
    </div>
  );
}

export default SidebarItem;
