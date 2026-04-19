#!/bin/bash

echo "🚀 VoiceMemory APK 编译指南"
echo "================================"
echo ""

# 检查环境
echo "📋 步骤 1: 检查环境"
echo "--------------------------------"

if [ -d "$HOME/Library/Android/sdk" ]; then
    echo "✅ Android SDK 已安装"
else
    echo "❌ Android SDK 未安装"
    echo ""
    echo "请先安装 Android Studio:"
    echo "1. 下载: https://developer.android.com/studio"
    echo "2. 安装后打开 Android Studio"
    echo "3. 进入 Settings → Appearance & Behavior → System Settings → Android SDK"
    echo "4. 安装 Android 13.0 (API 33)"
    echo ""
    exit 1
fi

# 检查 Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "✅ Java 已安装: $JAVA_VERSION"
else
    echo "❌ Java 未安装"
    echo "请安装 JDK 17: brew install openjdk@17"
    exit 1
fi

echo ""
echo "📦 步骤 2: 编译 Debug APK"
echo "--------------------------------"
cd "$(dirname "$0")"

# 赋予 gradlew 执行权限
chmod +x gradlew

# 清理旧的构建
echo "🧹 清理旧构建..."
./gradlew clean

# 编译 Debug APK
echo "🔨 开始编译..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 编译成功！"
    echo ""
    echo "📱 APK 位置:"
    echo "   app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📲 安装方法:"
    echo "   1. 通过 USB 连接手机"
    echo "   2. 运行: ./gradlew installDebug"
    echo "   或"
    echo "   3. 将 APK 文件传到手机上直接安装"
    echo ""
    
    # 显示 APK 信息
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo "📊 APK 大小: $APK_SIZE"
    fi
else
    echo ""
    echo "❌ 编译失败"
    echo "请检查错误信息"
    exit 1
fi
