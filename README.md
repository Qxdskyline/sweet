# Sweet for Android

Sweet 是可以离线使用的 Android 恋爱小工具。支持 Android 8.0 及以上。

- **我们**显示在一起的天数、情话和纪念日。天数按设备本地日期计算，起始日为第 0 天。
- **今天吃什么**用转盘从自定义菜单抽取一项。菜单或尺寸变化时重绘 Canvas，旋转时只更新转盘角度。
- **设置**里可以编辑两人的名字、昵称、起始日、情话、纪念日和转盘菜单。设置保存在当前设备。

## 修改和预览页面

- `src/main/assets/index.html` 是入口，打开后进入「我们的日子」。
- `src/main/assets/couple-date/`、`src/main/assets/food-wheel/` 和 `src/main/assets/settings/` 分别存放各自的 HTML、SCSS 和 JS。
- `src/main/assets/shared/` 存放公共样式、日期计算、设置读写和页面初始化逻辑。
- `src/main/java/` 存放 Android 容器、本地资源加载和设置桥接。图标和主题在 `src/main/res/`，应用清单在 `src/main/AndroidManifest.xml`。

在项目根目录安装依赖并编译 SCSS，然后启动静态服务器。

```powershell
npm ci
npm run styles
npm.cmd run preview
```

打开 [本地预览](http://127.0.0.1:8000)。编辑 SCSS 后重新运行 `npm run styles`。浏览器预览的设置保存在当前浏览器，APK 继续使用原有的 Android 设置存储。

## 构建 APK

先用 Node.js 20.19 或更高的受支持版本运行 `npm ci`。APK 构建还需要 JDK 17、Android SDK Platform 36 和包含 AAPT2、D8、zipalign、apksigner 的 Android SDK Build Tools 36。Windows PowerShell 执行以下命令。

```powershell
.\build.ps1 -Jdk 'C:\path\to\jdk-17' -BuildTools 'C:\path\to\sdk\build-tools\36.0.0' -Platform 'C:\path\to\sdk\platforms\android-36'
```

输出为 `build/Sweet-0.2.2.apk`。首次本地构建会生成调试签名。覆盖安装须使用和已安装 APK 相同的签名，保留原来的 `.signing/debug.p12`。正式分发应使用自己的发布密钥。

默认示例配置位于 `src/main/assets/defaults.json`，用户可以在 App 内修改。

[MIT License](LICENSE)
