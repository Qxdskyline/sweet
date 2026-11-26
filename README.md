# Sweet for Android

Sweet 是可以离线使用的 Android 恋爱小工具。支持 Android 8.0 及以上。

- **我们**显示在一起的天数、情话和纪念日。天数按设备本地日期计算，起始日为第 0 天。
- **今天吃什么**用转盘从自定义菜单抽取一项。转盘只在菜单或尺寸变化时重新绘制，旋转使用 Android 属性动画。
- **设置**里可以编辑两人的名字、昵称、起始日、情话、纪念日和转盘菜单。设置保存在当前设备。

## 构建 APK

需要 JDK 17、Android SDK Platform 36 和包含 AAPT2、D8、zipalign、apksigner 的 Android SDK Build Tools 36。Windows PowerShell 执行：

```powershell
.\build.ps1 -Jdk 'C:\path\to\jdk-17' -BuildTools 'C:\path\to\sdk\build-tools\36.0.0' -Platform 'C:\path\to\sdk\platforms\android-36'
```

输出为 `build/Sweet-0.1.0.apk`。本地构建使用自动生成的调试签名，留存本地 `.signing/debug.p12` 可在同一签名下继续安装更新。正式分发应使用自己的发布密钥。

默认示例配置位于 `assets/defaults.json`，用户可以在 App 内修改。

[MIT License](LICENSE)
