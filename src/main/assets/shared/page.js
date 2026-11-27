import { readConfig } from "./config.js";

export function setPageError(message) {
  document.querySelector("#error").textContent = message;
}

export function startMainPage(render) {
  for (const tab of document.querySelectorAll("[data-tab]")) {
    tab.addEventListener("click", event => {
      if (event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
      event.preventDefault();
      if (!tab.hasAttribute("aria-current")) location.replace(tab.href);
    });
  }

  // Read settings again when returning through browser history.
  window.addEventListener("pageshow", async () => {
    try {
      const config = await readConfig();
      setPageError("");
      render(config);
    } catch (error) {
      setPageError(error.message);
    }
  });
}
