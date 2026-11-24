<div align="center">

# 💛 哄女朋友神器 · GF Kit

**一套开箱即用的「哄女朋友」小项目合集。改一个配置文件,就拥有专属于你俩的浪漫。**

*A ready-to-use collection of mini apps to make your girlfriend happy. Fork it, edit one config file, deploy — done.*

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-ff69b4.svg)](./CONTRIBUTING.md)
![Made with HTML/CSS/JS](https://img.shields.io/badge/stack-HTML%2FCSS%2FJS-ff6f8e)
![No build needed](https://img.shields.io/badge/build-none-brightgreen)

[**一键部署到 Vercel →**](https://vercel.com/new/clone?repository-url=https://github.com/wxggzz/gf)

</div>

---

## ✨ 里面有什么

| 项目 | 是什么 | 亮点 |
|---|---|---|
| 🌇 **love-home** 专属恋爱主页 | 「在一起 XXX 天」+ 萤火虫粒子 + 打字机情话,点「抱抱」满屏爱心 | 黄昏渐变 + 呼吸动画,不土 |
| 🥺 **honghong** 哄哄模拟器 | 她生气时玩的小游戏,系统判「原谅值」,满 100 通关 | **纯离线智能判分,不用 API、不烧钱** |
| 🍜 **food-wheel** 今天吃什么 | 大转盘解决「吃什么」世纪难题,选项可自定义 | Canvas 缓动,天天能用 |
| 📮 **daily-push** 每日推送 | 每天定时自动发:天气 + 该穿什么 + 情话 + 纪念日提醒 | GitHub Actions 定时,免费无 key 天气 |

外加一个 **门户首页**(`index.html`),发一个链接,四个都在里面。

> 📸 *截图 / 演示 GIF 放在这里 —— 见 [贡献指南](./CONTRIBUTING.md#截图) 帮忙补一张*

## 🚀 三步拥有

### 1. 拿到代码
点右上角绿色 **`Use this template`**(推荐),或者 **Fork**,得到你自己的一份。

### 2. 填资料(只改一个文件)
编辑 [`shared/config.js`](./shared/config.js),把名字、在一起的日期、城市、想说的话换成你们的:

```js
const COUPLE_CONFIG = {
  herName: "宝贝",          // 改成她的名字
  startDate: "2024-01-01",  // 你们在一起的第一天
  herCity: "北京",          // 她的城市(推送查天气用)
  sweetLines: [ "想对她说的话…" ],
  // …更多见文件内注释
};
```

**四个项目共用这一份配置,改完全部生效。**

### 3. 部署上线
- **Vercel(推荐):** [一键部署](https://vercel.com/new/clone?repository-url=https://github.com/wxggzz/gf),或登录后 Import 你 fork 的仓库,Framework 选 `Other`,直接 Deploy。
- **GitHub Pages:** 仓库 Settings → Pages → 选 `main` 分支 → 保存,地址形如 `https://你的用户名.github.io/gf/`。
- **本地预览:** 直接双击各文件夹里的 `index.html`。

拿到网址发给她;让她「**添加到主屏幕**」,就像装了个 App 📱

## 📮 开启每日推送(可选)

1. 注册 [Server酱³](https://sct.ftqq.com),微信扫码拿 **SendKey**。
2. 你的仓库 → **Settings → Secrets and variables → Actions → New repository secret**,名字 `SERVERCHAN_KEY`,值填 SendKey。
3. 完成!每天北京时间早 8 点自动发。改时间就编辑 [`.github/workflows/daily-push.yml`](./.github/workflows/daily-push.yml) 里的 `cron`。
4. 想立刻测:仓库 **Actions** 页 → 「每日给她推送」→ **Run workflow**。

> 也支持企业微信群机器人:把 secret 名字设为 `WECOM_WEBHOOK`,值填 Webhook 地址。

## 📁 目录结构

```
.
├── index.html              # 门户首页(菜单)
├── shared/config.js        # ⭐ 唯一要改的配置文件
├── love-home/              # 专属恋爱主页
├── honghong/               # 哄哄模拟器
├── food-wheel/             # 今天吃什么·转盘
├── daily-push/push.js      # 每日推送脚本(Node,零依赖)
└── .github/workflows/      # 定时任务
```

## 🧩 想加更多?

这是一个**会长大的集合**,欢迎把你的点子做成一个新文件夹 PR 进来。已有的愿望清单:

- [ ] 恋爱时光轴 / 相册
- [ ] 经期记录 + 关心提醒
- [ ] 情话 / 彩虹屁生成器
- [ ] 旅行存钱罐进度条
- [ ] 生日 / 节日惊喜页(烟花、弹幕表白)

怎么贡献见 **[CONTRIBUTING.md](./CONTRIBUTING.md)**。

## 🙌 支持一下

如果这个项目帮你哄好了女朋友,给个 ⭐ **Star** 让更多人看到吧~

## 📄 License

[MIT](./LICENSE) — 随便用,祝天下有情人甜甜的 🍬
