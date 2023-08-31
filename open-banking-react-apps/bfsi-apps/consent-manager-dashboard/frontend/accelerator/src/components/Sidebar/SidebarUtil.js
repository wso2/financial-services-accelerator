/* ----- SidebarUtil.js ----- */

export const setSidebarAnimationProps = (initialRender) => {
  return {
    initial: initialRender ? false : { opacity: 0 },
    animate: { opacity: 1 },
    exit: { opacity: 0 },
    transition: { delay: 0.5, duration: 0.25 },
  };
};
