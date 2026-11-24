# 贡献指南 · Contributing

欢迎一起把这个「哄女朋友神器」做得更好玩!无论是修 bug、加新的小项目,还是补一张截图,都非常欢迎。

## 🧩 加一个新的小项目

这个仓库是个**小项目集合**,每个小项目就是一个顶层文件夹。加新的很简单:

1. 新建一个文件夹,名字用英文小写连字符,比如 `love-timeline/`。
2. 里面放 `index.html`(纯前端项目),或对应的脚本。
3. **复用共享配置**:在页面里引入 `../shared/config.js`,通过 `window.COUPLE_CONFIG` 读取用户资料,不要把名字/日期写死。
   ```html
   <script src="../shared/config.js"></script>
   <script>
     const c = window.COUPLE_CONFIG || {};
     // 用 c.herName / c.startDate / c.loveWords ...
   </script>
   ```
4. 如果你的项目需要新的配置项,在 `shared/config.js` 里**加带注释的字段**,并给一个合理默认值,保证别人不填也能跑。
5. 在门户首页 `index.html` 里加一张跳转卡片。
6. 更新 `README.md` 的功能表格。

### 质量小要求(让它保持「不土」)
- **纯前端、零依赖、免费**:能静态部署,不强制后端、不需要付费 key。
- **响应式**:手机上要好看(大多数女朋友是在手机上打开的)。
- **无障碍基线**:键盘能操作、`:focus-visible` 有描边、尊重 `prefers-reduced-motion`。
- **配置驱动**:所有个人信息来自 `shared/config.js`,不写死。
- **中文友好**:文案自然、不肉麻过头也不冷冰冰。

## 📸 截图

README 很需要真实截图 / 演示 GIF。欢迎:
1. 用示例配置(默认 `config.js`)本地打开各页面。
2. 截图或录制 GIF,放到 `docs/screenshots/`。
3. 在 README 对应位置引用。

录 GIF 推荐:macOS 用 [Kap](https://getkap.co/),或系统自带录屏。

## 🔧 提 PR 流程

1. Fork → 新建分支 `feat/xxx` 或 `fix/xxx`。
2. 改动尽量聚焦一件事。
3. 本地自测:页面能打开、手机端正常、`node daily-push/push.js` 能跑(如涉及)。
4. 提交 PR,说明你加了什么、附上截图。

## 💡 没想法?挑一个

README 里的「想加更多」清单就是很好的起点。认领时在 issue 里说一声,避免撞车~

谢谢你,祝天下有情人甜甜的 🍬
