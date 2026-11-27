import { readConfig, saveConfig } from "../shared/config.js";
import { setPageError } from "../shared/page.js";
import { parseDate } from "../shared/calendar.js";

const source = new URLSearchParams(location.search).get("from");
const back = document.querySelector("#return");
if (source === "food-wheel") back.href = "../food-wheel/index.html";
function returnToPage() {
  document.activeElement?.blur();
  if (document.referrer === back.href) history.back();
  else location.replace(back.href);
}
back.addEventListener("click", event => {
  event.preventDefault();
  returnToPage();
});

const form = document.querySelector("form");
const status = document.querySelector("#save-status");
const profileFields = ["myName", "herName", "nickname", "startDate"];
let config;

function update(key, value) {
  try {
    const next = { ...config, [key]: value };
    saveConfig(next);
    config = next;
    setPageError("");
    status.textContent = "已保存";
    return true;
  } catch (error) {
    status.textContent = "";
    setPageError(error.message);
    return false;
  }
}

function field(label, control) {
  const wrapper = document.createElement("label");
  if (control.tagName === "SELECT") wrapper.className = "select-field";
  wrapper.append(label, control);
  return wrapper;
}

function selectRange(count, unit) {
  const select = document.createElement("select");
  for (let number = 1; number <= count; number++) {
    select.add(new Option(number + unit, String(number).padStart(2, "0")));
  }
  return select;
}

function anniversaryEditor(card, entry, commit) {
  const name = document.createElement("input");
  name.value = entry?.name ?? "";
  const frequency = document.createElement("select");
  frequency.add(new Option("每年", "yearly"));
  frequency.add(new Option("仅一次", "once"));
  frequency.value = entry?.type ?? "yearly";

  const date = document.createElement("input");
  date.type = "date";
  const now = new Date();
  const today = [now.getFullYear(), now.getMonth() + 1, now.getDate()]
    .map(part => String(part).padStart(2, "0")).join("-");
  date.value = entry?.date.length === 10 ? entry.date : today;
  const month = selectRange(12, "月");
  const day = selectRange(31, "日");
  const [initialMonth, initialDay] = (entry?.date ?? today).slice(-5).split("-");
  month.value = initialMonth;
  day.value = initialDay;

  const annual = document.createElement("div");
  annual.className = "annual-date";
  annual.append(field("月份", month), field("日期", day));
  const once = field("日期", date);
  function syncDateFields() {
    const yearly = frequency.value === "yearly";
    annual.hidden = !yearly;
    once.hidden = yearly;
    const maxDay = new Date(Date.UTC(2000, Number(month.value), 0)).getUTCDate();
    if (Number(day.value) > maxDay) day.selectedIndex = maxDay - 1;
    for (const option of day.options) option.disabled = Number(option.value) > maxDay;
  }

  card.append(field("名称", name), field("重复", frequency), annual, once);
  syncDateFields();
  card.addEventListener("change", event => {
    if (event.target === frequency && frequency.value === "yearly" && date.value) {
      [month.value, day.value] = date.value.slice(-5).split("-");
    } else if (event.target === frequency && frequency.value === "once") {
      const year = date.value.slice(0, 4) || today.slice(0, 4);
      const lastDay = new Date(Date.UTC(Number(year), Number(month.value), 0)).getUTCDate();
      date.value = year + "-" + month.value + "-" + String(Math.min(Number(day.value), lastDay)).padStart(2, "0");
    }
    syncDateFields();
    if (!name.value.trim() || (frequency.value === "once" && !date.value)) return;
    const unchangedYearlyDate = entry?.type === "yearly" && entry.date.slice(-5) === month.value + "-" + day.value;
    const selectedDate = frequency.value === "once" ? date.value
      : unchangedYearlyDate ? entry.date : month.value + "-" + day.value;
    commit({ name: name.value.trim(), type: frequency.value, date: selectedDate });
  });
  return name;
}

function addCard(key, entry) {
  const list = document.getElementById(key);
  const card = document.createElement("div");
  card.className = "entry";
  // Match the visible order, including cards that have yet to be filled in.
  let saved = entry !== undefined;
  function position() {
    const cards = Array.from(list.children);
    return cards.slice(0, cards.indexOf(card))
      .filter(item => item.dataset.saved === "true").length;
  }

  function commit(value) {
    const items = [...config[key]];
    const index = position();
    if (key === "foodOptions" && items.some((item, i) => item === value && (!saved || i !== index))) {
      status.textContent = "";
      setPageError("菜单里已有这道菜");
      input.setCustomValidity("菜单里已有这道菜");
      input.reportValidity();
      return;
    }
    items.splice(index, saved ? 1 : 0, value);
    if (update(key, items)) {
      saved = true;
      card.dataset.saved = "true";
    }
  }

  let input;
  if (key === "anniversaries") {
    input = anniversaryEditor(card, entry, commit);
  } else {
    input = document.createElement(key === "sweetLines" ? "textarea" : "input");
    if (key === "sweetLines") input.rows = 1;
    input.value = entry ?? "";
    card.append(field(key === "sweetLines" ? "情话" : "菜名", input));
    input.addEventListener("input", () => input.setCustomValidity(""));
    input.addEventListener("change", () => {
      const value = input.value.trim();
      if (value) commit(value);
      else if (saved) input.value = config[key][position()];
    });
  }

  const remove = document.createElement("button");
  remove.type = "button";
  remove.className = "remove-entry";
  remove.textContent = "删除";
  remove.addEventListener("click", () => {
    if (saved) {
      if (key === "foodOptions" && config[key].length <= 2) {
        status.textContent = "";
        setPageError("转盘至少需要保留两道菜");
        announceInline(remove, "转盘至少需要保留两道菜");
        return;
      }
      const items = [...config[key]];
      items.splice(position(), 1);
      if (!update(key, items)) return;
    }
    card.remove();
    refreshMenuLimit();
  });

  card.dataset.saved = String(saved);
  card.append(remove);
  list.append(card);
  refreshMenuLimit();
  return input;
}

function announceInline(control, message) {
  const note = document.createElement("p");
  note.className = "entry-note error";
  note.setAttribute("role", "alert");
  note.textContent = message;
  const card = control.closest(".entry");
  card.querySelector(".entry-note")?.remove();
  card.append(note);
}

const menuList = document.getElementById("foodOptions");
const addFood = document.querySelector('[data-add="foodOptions"]');
function refreshMenuLimit() {
  addFood.disabled = menuList.children.length >= 20;
  addFood.textContent = addFood.disabled ? "最多添加 20 道菜" : "添加菜品";
  for (const note of menuList.querySelectorAll(".entry-note")) note.remove();
}

form.addEventListener("submit", event => event.preventDefault());
for (const button of form.querySelectorAll("[data-add]")) {
  button.addEventListener("click", () => addCard(button.dataset.add).focus());
}

readConfig().then(value => {
  config = value;
  for (const key of profileFields) {
    const input = form.elements[key];
    input.value = config[key];
    input.addEventListener("change", () => {
      if (key === "startDate") {
        try {
          parseDate(input.value);
        } catch {
          input.value = config[key];
          return;
        }
      }
      const fallback = key === "myName" ? "我" : key === "herName" ? "她" : "";
      update(key, input.value.trim() || fallback);
      input.value = config[key];
    });
  }
  for (const key of ["anniversaries", "sweetLines", "foodOptions"]) {
    for (const entry of config[key]) addCard(key, entry);
  }
  form.hidden = false;
}).catch(error => setPageError(error.message));

const particleSettings = document.querySelector("#particle-settings");
particleSettings.search = location.search;
window.addEventListener("pageshow", async event => {
  if (!event.persisted || !config) return;
  try {
    const latest = await readConfig();
    config = { ...config, particles: latest.particles };
  } catch (error) {
    setPageError(error.message);
  }
});
