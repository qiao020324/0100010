# 云端构建 APK（无需 Android Studio）

本项目已配置好 GitHub Actions 工作流（`.github/workflows/build-apk.yml`）。
只要把代码推送到 GitHub，云端就会自动编译出可安装的 `AutoLedger-debug.apk`。

## 前置条件
- 一个 GitHub 账号（https://github.com）
- 本机已安装 Git（若未安装：https://git-scm.com/downloads ，或用 `winget install Git.Git`）

## 步骤一：在 GitHub 新建空仓库
1. 打开 https://github.com/new
2. 仓库名随便取，例如 `AutoLedger`
3. **不要**勾选 "Add a README file" / ".gitignore" / "License"（保持空仓库）
4. 点击 **Create repository**，进入后复制仓库地址（形如 `https://github.com/<你的用户名>/AutoLedger.git`）

## 步骤二：把本工程推上去（任选一种）

### 方式 A：网页直接拖拽上传（推荐，免装任何软件）
1. 打开刚才新建的空仓库页面；
2. **打开本机 `AutoLedger` 文件夹**，选中里面的**所有内容**
   （`app/`、`build.gradle`、`settings.gradle`、`gradle.properties`、
   `README.md`、`CLOUD_BUILD.md`、`.github/`、`.gitignore`）；
3. 把这些**直接拖到 GitHub 网页的「把文件拖到此处上传」区域**；
   > ⚠️ 注意：拖的是 `AutoLedger` **里面的东西**，不要连 `AutoLedger` 这层一起拖，
   > 否则仓库里会多一层目录，导致构建路径错误。上传后仓库根应直接看到 `app/` 和 `.github/`。
4. 页面底部填写提交说明（如 `init AutoLedger`），点击 **Commit changes**。

### 方式 B：用 Git 命令行（需先安装 Git）
```bash
cd AutoLedger
git init
git add .
git commit -m "init AutoLedger"
git branch -M main
git remote add origin https://github.com/<你的用户名>/AutoLedger.git
git push -u origin main
```
> 推送密码处请填 **Personal Access Token（PAT）** 而非网页登录密码：
> GitHub → Settings → Developer settings → Personal access tokens → 勾选 `repo`。

## 步骤三：下载 APK
1. 进入仓库的 **Actions** 页面，会看到 `Build Debug APK` 正在运行；
2. 等待约 3~6 分钟，显示绿色 ✅ 即构建成功；
3. 点击该次构建 → 右侧 **Artifacts** → 下载 `AutoLedger-debug.apk`；
4. 把 APK 传到安卓手机安装即可。

## 手机上首次使用
1. 打开 App → 「设置」→「去开启通知监听」；
2. 在系统列表勾选「自动记账」，授予「通知使用权」；
3. 之后微信/支付宝付款成功，通知会自动被识别并入账。

## 用本地 adb 安装到手机（可选，免 Android Studio）
如果已在 `D:\AndroidTools` 装好 Android SDK，可直接用里面的 `adb` 把云端出的 APK 推到手机：
1. 手机开启「开发者选项 → USB 调试」，用数据线连电脑；
2. 把云端下载的 `AutoLedger-debug.apk` 放到本机（如 `D:\AutoLedger-debug.apk`）；
3. 执行：
   ```bash
   D:\AndroidTools\platform-tools\adb.exe install D:\AutoLedger-debug.apk
   ```
4. 手机上确认安装即可（重装时加 `-r` 参数：`adb install -r ...`）。

> 说明：本机 JDK 为 24、且缺少 Gradle，无法直接在本机 `assembleDebug`；
> 因此「构建」走云端 GitHub Actions（自动用 JDK17+Gradle+标准 SDK），
> 「安装/调试」用本地 `D:\AndroidTools` 的 `adb`。两者结合即可全程不依赖 Android Studio。

## 构建失败排查
- 若 Actions 报红：进入日志查看具体错误，通常是依赖下载或 SDK 版本问题，
  把报错贴回给 AI 助理即可定位修复；
- 如需更换 compileSdk / 依赖版本，改 `app/build.gradle` 与根 `build.gradle` 后重新推送。
