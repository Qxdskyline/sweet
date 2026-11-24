/* =======================================================================
 *  每日自动推送 —— 每天定时给她(或给你)发:天气 + 该穿什么 + 情话 + 纪念日
 *
 *  运行:  node daily-push/push.js
 *  依赖:  无(Node 18+ 自带 fetch)
 *  发送渠道(设一个环境变量即可,两个都设就都发):
 *     SERVERCHAN_KEY   —— Server酱³ 的 SendKey(微信收,最简单)https://sct.ftqq.com
 *     WECOM_WEBHOOK    —— 企业微信群机器人的 Webhook 地址
 *  没设渠道时,只在控制台打印,方便本地预览。
 * ===================================================================== */
const cfg = require("../shared/config.js");

const DAY = 86400000;
const now = new Date();
// 用北京时间,避免 GitHub 服务器时区(UTC)算错日期
const bj = new Date(now.getTime() + (now.getTimezoneOffset() + 480) * 60000);

function daysTogether() {
  const start = new Date((cfg.startDate || "2024-01-01") + "T00:00:00");
  return Math.max(0, Math.floor((bj - start) / DAY));
}

// 纪念日提醒:今天 / 未来 7 天内有没有
function anniversaryNote() {
  const list = cfg.anniversaries || [];
  const notes = [];
  for (const a of list) {
    const [m, d] = a.date.split("-").map(Number);
    for (let off = 0; off <= 7; off++) {
      const t = new Date(bj.getFullYear(), bj.getMonth(), bj.getDate() + off);
      if (t.getMonth() + 1 === m && t.getDate() === d) {
        notes.push(off === 0 ? `🎉 **今天是${a.name}!** 别忘了给她惊喜~` :
                               `⏰ 还有 **${off}** 天就是「${a.name}」了,提前准备起来。`);
      }
    }
  }
  return notes;
}

// 天气(wttr.in,免费无需 key)。失败就跳过,不影响推送。
async function weather() {
  const city = encodeURIComponent(cfg.herCity || "北京");
  try {
    const url = `https://wttr.in/${city}?format=j1&lang=zh`;
    const ctrl = new AbortController();
    const timer = setTimeout(() => ctrl.abort(), 8000);
    const r = await fetch(url, { signal: ctrl.signal, headers: { "User-Agent": "curl" } });
    clearTimeout(timer);
    if (!r.ok) throw new Error("HTTP " + r.status);
    const j = await r.json();
    const cur = j.current_condition[0];
    const today = j.weather[0];
    const desc = (cur.lang_zh && cur.lang_zh[0] && cur.lang_zh[0].value) || cur.weatherDesc[0].value;
    const tmin = today.mintempC, tmax = today.maxtempC, feels = cur.FeelsLikeC;
    let tip;
    if (+tmax <= 10) tip = "挺冷的,记得穿厚点、戴围巾,别冻着。";
    else if (+tmax <= 18) tip = "有点凉,外套带上,早晚温差大。";
    else if (+tmax <= 26) tip = "温度刚好,舒服的一天。";
    else tip = "有点热,注意防晒、多喝水。";
    const rain = /雨|雪|阵/.test(desc) ? "☔️ 今天可能下雨,出门带把伞。" : "";
    return { line: `${cfg.herCity} 今天 ${desc},${tmin}~${tmax}℃(体感 ${feels}℃)。${tip}`, rain };
  } catch (e) {
    return null;
  }
}

function pick(arr, fallback) {
  if (!arr || !arr.length) return fallback;
  return arr[Math.floor(Math.random() * arr.length)];
}

function greeting() {
  const h = bj.getHours();
  if (h < 11) return "早安";
  if (h < 18) return "下午好";
  return "晚上好";
}

async function build() {
  const her = cfg.herName || "宝贝";
  const days = daysTogether();
  const w = await weather();
  const word = pick(cfg.loveWords, "今天也很想你。");
  const annos = anniversaryNote();

  const title = `${greeting()},${her} ☀️`;

  const lines = [];
  lines.push(`### ${greeting()},${her} 💛`);
  lines.push("");
  lines.push(`今天是我们在一起的第 **${days}** 天。`);
  if (w) { lines.push(""); lines.push(`🌤️ ${w.line}`); if (w.rain) lines.push(`> ${w.rain}`); }
  lines.push("");
  lines.push(`> 💌 ${word}`);
  if (annos.length) { lines.push(""); annos.forEach(a => lines.push(a)); }
  lines.push("");
  lines.push("——— 记得好好吃饭,想我了就打电话 📞");

  return { title, desp: lines.join("\n") };
}

async function sendServerChan(key, title, desp) {
  const r = await fetch(`https://sctapi.ftqq.com/${key}.send`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({ title, desp }),
  });
  const j = await r.json().catch(() => ({}));
  if (j.code === 0 || (j.data && j.data.pushid)) console.log("✅ Server酱 发送成功");
  else console.error("❌ Server酱 发送失败:", JSON.stringify(j));
}

async function sendWecom(url, title, desp) {
  const r = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ msgtype: "markdown", markdown: { content: `# ${title}\n${desp}` } }),
  });
  const j = await r.json().catch(() => ({}));
  if (j.errcode === 0) console.log("✅ 企业微信 发送成功");
  else console.error("❌ 企业微信 发送失败:", JSON.stringify(j));
}

(async () => {
  const { title, desp } = await build();
  console.log("—— 预览 ——\n" + title + "\n" + desp + "\n———————");

  const sck = process.env.SERVERCHAN_KEY;
  const wecom = process.env.WECOM_WEBHOOK;
  if (!sck && !wecom) {
    console.log("\n(未配置发送渠道,只做预览。设置 SERVERCHAN_KEY 或 WECOM_WEBHOOK 后即可真正发送。)");
    return;
  }
  if (sck) await sendServerChan(sck, title, desp);
  if (wecom) await sendWecom(wecom, title, desp);
})().catch(e => { console.error(e); process.exit(1); });
