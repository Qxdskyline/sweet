import { readConfig, saveConfig } from "../shared/config.js";
import { setPageError } from "../shared/page.js";
import { defaultParticles, particleLimits, particlePreferences, previewParticles } from "../shared/particles-effect.js";

const form = document.querySelector("form");
const controls = form.querySelector("fieldset");
const status = document.querySelector("#save-status");
const back = document.querySelector("#return");
const ranges = [...form.querySelectorAll('input[type="range"]')];
let config;

for (const input of ranges) {
  const [min, max] = particleLimits[input.name];
  input.min = min;
  input.max = max;
}

back.search = location.search;
back.addEventListener("click", event => {
  if (document.referrer !== back.href) return;
  event.preventDefault();
  history.back();
});

function render(value) {
  form.elements.enabled.checked = value.enabled;
  controls.disabled = !value.enabled;
  for (const input of ranges) {
    input.value = value[input.name];
    const isCount = input.name === "count" || input.name === "foreground";
    input.previousElementSibling.value = isCount ? input.value : input.value + "%";
  }
  previewParticles(value);
}

function currentValues() {
  return {
    enabled: form.elements.enabled.checked,
    ...Object.fromEntries(ranges.map(input => [input.name, input.valueAsNumber]))
  };
}

function save(value) {
  try {
    const updated = { ...config, particles: value };
    saveConfig(updated);
    config = updated;
    setPageError("");
    status.textContent = "\u5df2\u4fdd\u5b58";
  } catch (error) {
    status.textContent = "";
    setPageError(error.message);
    render(particlePreferences(config.particles));
  }
}

form.addEventListener("submit", event => event.preventDefault());
form.addEventListener("input", () => {
  status.textContent = "";
  render(currentValues());
});
form.addEventListener("change", () => save(currentValues()));
document.querySelector("#reset-particles").addEventListener("click", () => {
  render(defaultParticles);
  save(defaultParticles);
});

readConfig().then(value => {
  config = value;
  render(particlePreferences(config.particles));
  form.hidden = false;
}).catch(error => setPageError(error.message));
