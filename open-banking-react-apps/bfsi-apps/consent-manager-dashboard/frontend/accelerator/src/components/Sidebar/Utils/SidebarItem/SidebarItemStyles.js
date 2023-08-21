/* ----- SidebarItemStyles.js ----- */

export const setSidebarItemColorStyles = (current, page, theme) => {
  if (current === page) {
    return {
      backgroundColor: theme.palette.primary.light,
      color: theme.palette.primary.main,
    };
  } else {
    return {
      backgroundColor: theme.palette.common.white,
      color: theme.palette.miscellaneous.grey.main,
    };
  }
};
