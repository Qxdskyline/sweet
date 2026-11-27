const millisecondsPerDay = 86400000;

export function calendarDay(year, month, day) {
  const date = new Date(0);
  date.setUTCFullYear(year, month - 1, day);
  return date.getTime() / millisecondsPerDay;
}

export function today() {
  const now = new Date();
  return calendarDay(now.getFullYear(), now.getMonth() + 1, now.getDate());
}

function utcDate(day) {
  return new Date(day * millisecondsPerDay);
}

export function parseDate(value) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) throw new Error("请填写有效日期");
  const [year, month, day] = value.split("-").map(Number);
  const result = calendarDay(year, month, day);
  const date = utcDate(result);
  if (year < 1 || date.getUTCMonth() + 1 !== month || date.getUTCDate() !== day) {
    throw new Error("请填写有效日期");
  }
  return result;
}

export function formatDate(day) {
  const date = utcDate(day);
  return `${date.getUTCFullYear()} 年 ${date.getUTCMonth() + 1} 月 ${date.getUTCDate()} 日`;
}

export function nextAnniversary(event, currentDay) {
  const monthAndDay = event.type === "yearly" && /^\d{2}-\d{2}$/.test(event.date);
  const first = parseDate(monthAndDay ? `2000-${event.date}` : event.date);
  if (event.type !== "yearly") return first < currentDay ? null : first;

  const date = utcDate(first);
  const month = date.getUTCMonth() + 1;
  const day = date.getUTCDate();
  const currentYear = utcDate(currentDay).getUTCFullYear();
  const year = monthAndDay ? currentYear : Math.max(currentYear, date.getUTCFullYear());
  function occurrence(targetYear) {
    const lastDay = utcDate(calendarDay(targetYear, month + 1, 0)).getUTCDate();
    return calendarDay(targetYear, month, Math.min(day, lastDay));
  }
  const next = occurrence(year);
  return next < currentDay ? occurrence(year + 1) : next;
}
