import { startMainPage } from "../shared/page.js";
import { formatDate, nextAnniversary, parseDate, today } from "../shared/calendar.js";

const names = document.querySelector("#names-text");
const counter = document.querySelector("#count");
const quoteCard = document.querySelector("#quote-card");
const quote = document.querySelector("#quote");
const nextQuote = document.querySelector("#next-quote");
const anniversaries = document.querySelector("#anniversaries");
let config;
let renderedDay;
let quoteIndex;

function render() {
  const currentDay = today();
  const start = parseDate(config.startDate);
  const elapsed = currentDay - start;
  renderedDay = currentDay;
  names.textContent = `${config.myName} & ${config.herName}`;
  document.querySelector("#count-label").textContent = elapsed < 0 ? "离开始还有" : "在一起已经";
  counter.textContent = Math.abs(elapsed);
  document.querySelector("#start-date").textContent = formatDate(start);
  document.querySelector(".hero").hidden = false;

  const quotes = config.sweetLines;
  quoteCard.hidden = quotes.length === 0;
  if (quotes.length) {
    quoteIndex = ((currentDay % quotes.length) + quotes.length) % quotes.length;
    document.querySelector("#quote-label").textContent = config.nickname
      ? `想对${config.nickname}说` : "想对你说";
    quote.textContent = quotes[quoteIndex];
    nextQuote.hidden = quotes.length < 2;
  }

  anniversaries.textContent = "";
  for (const event of config.anniversaries) {
    const next = nextAnniversary(event, currentDay);
    const row = document.createElement("li");
    const date = next === null ? `${event.date} · 已过` : `${formatDate(next)} · 还有 ${next - currentDay} 天`;
    row.textContent = `${event.name} · ${date}`;
    anniversaries.append(row);
  }
  if (!config.anniversaries.length) {
    const empty = document.createElement("li");
    empty.className = "empty";
    empty.textContent = "还没有添加纪念日";
    anniversaries.append(empty);
  }
}

nextQuote.addEventListener("click", () => {
  quoteIndex = (quoteIndex + 1) % config.sweetLines.length;
  quote.textContent = config.sweetLines[quoteIndex];
});
document.addEventListener("visibilitychange", () => {
  if (!document.hidden && config && renderedDay !== today()) render();
});
startMainPage(value => {
  config = value;
  render();
});
