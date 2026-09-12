# 自动记账 AutoLedger（Android 原生）

付款后**后台自动识别**微信/支付宝等支付通知，解析出金额、商户、支付方式、时间并**自动入账**，无需手动记录。

## 工作原理
1. `PaymentNotificationService`（通知监听服务）在系统「通知使用权」授权后，监听所有通知；
2. 付款成功时微信/支付宝会弹出支付通知，服务把通知文本交给 `BillParser` 解析；
3. `BillParser` 根据金额、商户、支付方式、时间关键词与分类词库，自动结构化；
4. 解析结果写入本地 SQLite（`DbHelper`），并通过 `KeepAliveService` 前台常驻保证后台存活；
5. 首页实时展示本月支出与自动记录列表；设置页可授权 / 清空数据。

> 防重复：同一通知文本或「相同金额 + 30 秒内」不会重复写入。

## 如何运行 / 构建
1. 用 **Android Studio** 打开本目录（`AutoLedger/`，即含 `settings.gradle` 的目录）；
2. 用数据线连接 Android 手机，开启「开发者选项 → USB 调试」；
3. 点击 ▶ Run，选择设备即可安装；
4. 首次打开后，进入「设置 → 去开启通知监听」，在系统列表中勾选「自动记账」，授予「通知使用权」。

（无 Android Studio 也可：`gradlew assembleDebug` 生成 APK 后 adb install。）

## 权限说明
- 需要「通知使用权」：用于读取支付通知（系统级能力，App 无法绕过）；
- 需要「前台服务」：用于后台保活，状态栏会常驻一个低优先级通知；
- 所有数据仅保存在手机本地 SQLite，不上传任何服务器。

## 已知限制 / 可扩展
- **iOS 不支持**：苹果系统禁止 App 后台读取其他 App 的通知，无法做自动捕获；
- 当前以「通知文本解析」为主，已覆盖微信/支付宝主流支付通知格式；
- 若需识别**屏幕内账单截图**而非通知，可额外接入「无障碍服务（AccessibilityService）」读取屏幕文本，逻辑可复用 `BillParser`；
- 分类词库在 `parse/Category.kt`，可自行增删关键词提升识别准确率。

## 目录结构
```
AutoLedger/
├── app/build.gradle
├── app/src/main/AndroidManifest.xml
├── app/src/main/java/com/autoledger/app/
│   ├── App.kt
│   ├── data/        Transaction.kt, DbHelper.kt
│   ├── parse/       BillParser.kt, Category.kt
│   ├── service/     PaymentNotificationService.kt, KeepAliveService.kt, BootReceiver.kt
│   ├── ui/          MainActivity.kt, TxAdapter.kt, SettingsActivity.kt
│   └── util/        Notify.kt
└── app/src/main/res/  layouts, values, drawable
```
