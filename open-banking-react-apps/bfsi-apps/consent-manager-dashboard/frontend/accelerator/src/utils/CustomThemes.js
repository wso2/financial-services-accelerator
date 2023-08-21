import { createTheme } from "@bfsi-react/bfsi-ui";

// Theme 01 : WSO2
export const THEME_01_WSO2 = createTheme({
  palette: {
    common: {
      background: "#fff1e6",
    },
    primary: {
      main: "#ff7300",
      contrastMain: "#ffffff",
      light: "#fff1e6",
      contrastLight: "#ff7300",
    },
  },
  logo: {
    lg: {
      url: "https://wso2.cachefly.net/wso2/sites/images/brand/downloads/wso2-logo.png",
    },
  },
});
