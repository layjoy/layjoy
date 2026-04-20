# VoiceMemory - 编译和安装指南

## 🚀 快速开始

### 方案 1: 使用 Android Studio（推荐）

#### 1. 安装 Android Studio
```bash
# 下载地址
https://developer.android.com/studio

# 或使用 Homebrew 安装
brew install --cask android-studio
```

#### 2. 配置 Android SDK
1. 打开 Android Studio
2. 进入 `Settings → Appearance & Behavior → System Settings → Android SDK`
3. 安装以下组件：
   - ✅ Android 13.0 (API 33)
   - ✅ Android SDK Build-Tools 33.0.0
   - ✅ Android SDK Platform-Tools
   - ✅ Android SDK Tools

#### 3. 打开项目
```bash
# 在 Android Studio 中
File → Open → 选择 /Users/admin/Projects/voiceMemory
```

#### 4. 编译 APK
```bash
# 方法 1: 使用 Android Studio
Build → Build Bundle(s) / APK(s) → Build APK(s)

# 方法 2: 使用命令行
cd /Users/admin/Projects/voiceMemory
./gradlew assembleDebug
```

#### 5. 安装到手机
```bash
# 方法 1: USB 连接手机后
./gradlew installDebug

# 方法 2: 手动安装
# APK 位置: app/build/outputs/apk/debug/app-debug.apk
# 将文件传到手机上直接安装
```

---

### 方案 2: 使用命令行工具

#### 1. 安装依赖
```bash
# 安装 Java 17
brew install openjdk@17

# 设置环境变量
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@17' >> ~/.zshrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# 安装 Android Command Line Tools
brew install --cask android-commandlinetools
```

#### 2. 配置 Android SDK
```bash
# 设置环境变量
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

# 安装必要的 SDK 组件
sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.0"
```

#### 3. 编译 APK
```bash
cd /Users/admin/Projects/voiceMemory

# 赋予执行权限
chmod +x gradlew

# 编译 Debug APK
./gradlew assembleDebug

# 或使用自动化脚本
chmod +x build_apk.sh
./build_apk.sh
```

---

### 方案 3: 在线编译服务（最简单）

如果不想安装 Android Studio，可以使用在线编译服务：

#### 1. GitHub Actions（免费）
```yaml
# 将项目推送到 GitHub
# 配置 GitHub Actions 自动编译
# 下载编译好的 APK
```

#### 2. AppCenter（免费）
```bash
# 注册 Microsoft AppCenter
# 连接 GitHub 仓库
# 自动编译和分发
```

---

## 📱 安装到手机

### 方法 1: USB 连接
```bash
# 1. 手机开启开发者选项和 USB 调试
# 2. 连接手机到电脑
# 3. 运行安装命令
./gradlew installDebug
```

### 方法 2: 无线安装
```bash
# 1. 手机和电脑连接同一 WiFi
# 2. 手机开启无线调试
# 3. 配对设备
adb pair <手机 IP>:<配对端口>

# 4. 连接设备
adb connect <手机 IP>:<调试端口>

# 5. 安装 APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 方法 3: 直接传输
```bash
# 1. 将 APK 文件传到手机
# 可以使用：
#   - AirDrop
#   - 微信/QQ 文件传输
#   - USB 数据线
#   - 云盘（百度网盘/阿里云盘）

# 2. 在手机上打开 APK 文件
# 3. 允许安装未知来源应用
# 4. 点击安装
```

---

## 🔧 常见问题

### Q1: gradlew 权限不足
```bash
chmod +x gradlew
```

### Q2: SDK 未找到
```bash
# 创建 local.properties 文件
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```

### Q3: 编译失败 - 内存不足
```bash
# 编辑 gradle.properties
echo "org.gradle.jvmargs=-Xmx4096m" >> gradle.properties
```

### Q4: 依赖下载失败
```bash
# 使用国内镜像
# 编辑 build.gradle.kts，添加阿里云镜像
maven { url = uri("https://maven.aliyun.com/repository/public") }
maven { url = uri("https://maven.aliyun.com/repository/google") }
```

### Q5: 手机无法安装
```bash
# 检查手机设置
# 1. 允许安装未知来源应用
# 2. 关闭 MIUI 优化（小米手机）
# 3. 检查存储空间
```

---

## 📊 APK 信息

### Debug APK
- **位置**: `app/build/outputs/apk/debug/app-debug.apk`
- **大小**: 约 15-20 MB
- **签名**: Debug 签名（仅用于测试）
- **最低版本**: Android 8.0 (API 26)
- **目标版本**: Android 13 (API 33)

### Release APK（生产版本）
```bash
# 1. 生成签名密钥
keytool -genkey -v -keystore voicememory.keystore \
  -alias voicememory -keyalg RSA -keysize 2048 -validity 10000

# 2. 配置签名
# 编辑 app/build.gradle.kts，添加 signingConfigs

# 3. 编译 Release APK
./gradlew assembleRelease

# 4. APK 位置
# app/build/outputs/apk/release/app-release.apk
```

---

## 🎯 推荐流程

### 对于开发者
1. 安装 Android Studio
2. 打开项目
3. 点击 Run 按钮
4. 选择设备或模拟器

### 对于普通用户
1. 让开发者编译好 APK
2. 通过微信/QQ 接收 APK 文件
3. 在手机上直接安装

---

## 📞 需要帮助？

如果遇到问题，可以：
1. 查看编译日志
2. 检查 Android Studio 的 Build 窗口
3. 运行 `./gradlew build --stacktrace` 查看详细错误

---

## 🎉 编译成功后

APK 文件位置：
```
app/build/outputs/apk/debug/app-debug.apk
```

可以通过以下方式分享：
- 📧 Email
- 💬 微信/QQ
- ☁️ 云盘链接
- 📱 AirDrop
- 🔗 文件传输工具

---

**注意**: Debug APK 仅用于测试，正式发布需要使用 Release APK 并签名。
