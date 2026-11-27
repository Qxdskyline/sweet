const storageKey = "sweet_settings.config";

export async function readConfig() {
  const response = await fetch(new URL("../defaults.json", import.meta.url));
  if (!response.ok) throw new Error("无法读取默认设置");
  const defaults = await response.json();
  const raw = window.SweetSettings
    ? window.SweetSettings.read()
    : localStorage.getItem(storageKey);
  try {
    return { ...defaults, ...JSON.parse(raw) };
  } catch {
    return defaults;
  }
}

export function saveConfig(config) {
  const raw = JSON.stringify(config);
  if (window.SweetSettings) {
    if (!window.SweetSettings.save(raw)) throw new Error("无法保存设置，请重试");
  } else {
    localStorage.setItem(storageKey, raw);
  }
}
