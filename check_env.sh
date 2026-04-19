#!/bin/bash

echo "🔍 检查 Android 开发环境"
echo "================================"
echo ""

# 检查 Android Studio
if [ -d "/Applications/Android Studio.app" ]; then
    echo "✅ Android Studio 已安装"
else
    echo "❌ Android Studio 未安装"
    echo "   下载地址: https://developer.android.com/studio"
fi

# 检查 Android SDK
if [ -d "$HOME/Library/Android/sdk" ]; then
    echo "✅ Android SDK 已安装"
    echo "   路径: $HOME/Library/Android/sdk"
else
    echo "❌ Android SDK 未安装"
    echo "   请先安装 Android Studio 并配置 SDK"
fi

# 检查 Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "✅ Java 已安装: $JAVA_VERSION"
else
    echo "❌ Java 未安装"
    echo "   安装命令: brew install openjdk@17"
fi

# 检查 Gradle
if [ -f "./gradlew" ]; then
    echo "✅ Gradle Wrapper 已配置"
else
    echo "❌ Gradle Wrapper 未找到"
fi

echo ""
echo "================================"
echo ""

# 给出建议
if [ ! -d "$HOME/Library/Android/sdk" ]; then
    echo "📝 下一步操作:"
    echo ""
    echo "1. 安装 Android Studio"
    echo "   brew install --cask android-studio"
    echo ""
    echo "2. 打开 Android Studio 并配置 SDK"
    echo "   Settings → Android SDK → 安装 API 33"
    echo ""
    echo "3. 在 Android Studio 中打开项目"
    echo "   File → Open → /Users/admin/Projects/voiceMemory"
    echo ""
    echo "4. 点击 Build → Build Bundle(s) / APK(s) → Build APK(s)"
    echo ""
else
    echo "✅ 环境已就绪！可以开始编译"
    echo ""
    echo "编译命令:"
    echo "  ./build_apk.sh"
    echo ""
fi
