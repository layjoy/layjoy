# VoiceMemory

一个基于 Android 的语音记忆应用

## 功能特性

- 🎤 语音录制和播放
- 💾 本地存储管理
- 🔄 数据同步
- 🎨 Material Design 界面

## 技术栈

- Kotlin
- Jetpack Compose
- Room Database
- Coroutines

## 下载

在 [Releases](https://github.com/layjoy/layjoy/releases) 页面下载最新版本的 APK

或者在 [Actions](https://github.com/layjoy/layjoy/actions) 页面下载自动编译的版本

## 编译

### 使用 GitHub Actions（推荐）

每次推送代码后，GitHub Actions 会自动编译 APK，可以在 Actions 页面下载

### 本地编译

```bash
# 1. 克隆仓库
git clone https://github.com/layjoy/layjoy.git
cd layjoy

# 2. 编译 Debug APK
./gradlew assembleDebug

# 3. APK 位置
# app/build/outputs/apk/debug/app-debug.apk
```

## 安装

1. 下载 APK 文件
2. 在手机上允许安装未知来源应用
3. 打开 APK 文件进行安装

## 开发

需要安装：
- Android Studio
- JDK 17
- Android SDK API 33

## License

MIT
