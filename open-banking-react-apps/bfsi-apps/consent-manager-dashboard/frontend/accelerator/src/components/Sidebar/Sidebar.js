/* ----- Sidebar.js ----- */
import React, { useContext, useEffect, useState } from "react";
import "./Sidebar.scss";

// Constants
import {
  ACCOUNT_INFORMATION,
  CONFORMATION_OF_FUNDS,
  PAYMENTS,
} from "../../utils/constants/OpenBankingConstants";

// Context
import GlobalStateContext from "../../utils/state/GlobalStateContext";
import { ThemeContext } from "@bfsi-react/bfsi-ui";

// Components
import SidebarItem from "./Utils/SidebarItem/SidebarItem";

// Icons
import KeyboardArrowRightRoundedIcon from "@mui/icons-material/KeyboardArrowRightRounded";

// Libraries & Packages
import { AnimatePresence, motion } from "framer-motion";
import { Link } from "react-router-dom";

// Utils
import { setSidebarAnimationProps } from "./SidebarUtil";
import { setPageAttributes } from "../../utils/common/PageAttributeUtil";

function Sidebar({ type }) {
  // State
  const { globalState, setGlobalState } = useContext(GlobalStateContext);
  const { theme } = useContext(ThemeContext);

  const [initialRender, setInitialRender] = useState(true);
  const [LOGO_SM] = useState(theme.logo.sm.url);
  const [LOGO_LG] = useState(theme.logo.lg.url);

  // Effects
  useEffect(() => {
    setInitialRender(false);
  }, []);

  return (
    <div className="sidebar">
      <div className="sidebar__container">
        {/* Header */}
        {type !== "mobile" && (
          <div className="sidebar__header">
            {/* Menu */}
            <div
              className="sidebar__menu"
              onClick={() =>
                setGlobalState({
                  ...globalState,
                  isSidebarOpen: !globalState.isSidebarOpen,
                })
              }
              style={{
                borderColor: theme.palette.primary.main,
              }}
            >
              <div
                className="sidebar__menu__icon"
                style={{
                  backgroundColor: theme.palette.common.white,
                  border: `2px solid ${theme.palette.primary.main}`,
                  borderRadius: "50%",
                  transform: globalState.isSidebarOpen
                    ? "rotate(180deg)"
                    : "rotate(0deg)",
                }}
              >
                <KeyboardArrowRightRoundedIcon />
              </div>
            </div>
            {/* Logo */}
            <AnimatePresence mode="wait">
              <div className="sidebar__logo">
                <Link to="/">
                  <motion.img
                    className={`sidebar__logo__${
                      globalState.isSidebarOpen ? "lg" : "sm"
                    }`}
                    src={globalState.isSidebarOpen ? LOGO_LG : LOGO_SM}
                    alt=""
                    key={
                      globalState.isSidebarOpen
                        ? "sidebar-logo-lg"
                        : "sidebar-logo-sm"
                    }
                    {...setSidebarAnimationProps(initialRender)}
                  />
                </Link>
              </div>
            </AnimatePresence>
          </div>
        )}
        {/* Links */}
        <div
          className="sidebar__links"
          style={{ padding: type === "mobile" && "0px" }}
        >
          {/* Account Information */}
          <SidebarItem
            current={ACCOUNT_INFORMATION}
            icon={setPageAttributes(ACCOUNT_INFORMATION).icon}
            text={setPageAttributes(ACCOUNT_INFORMATION).title}
            isSidebarOpen={globalState.isSidebarOpen}
          />
          {/* Confirmation of Funds */}
          <SidebarItem
            current={CONFORMATION_OF_FUNDS}
            icon={setPageAttributes(CONFORMATION_OF_FUNDS).icon}
            text={setPageAttributes(CONFORMATION_OF_FUNDS).title}
            isSidebarOpen={globalState.isSidebarOpen}
          />
          {/* Payments */}
          <SidebarItem
            current={PAYMENTS}
            icon={setPageAttributes(PAYMENTS).icon}
            text={setPageAttributes(PAYMENTS).title}
            isSidebarOpen={globalState.isSidebarOpen}
          />
        </div>
      </div>
    </div>
  );
}

export default Sidebar;
