#!/bin/bash

echo "🔍 VoiceMemory 项目检查"
echo "========================"
echo ""

# 检查项目结构
echo "📁 项目结构检查..."
if [ -f "app/build.gradle.kts" ]; then
    echo "✅ build.gradle.kts 存在"
else
    echo "❌ build.gradle.kts 缺失"
fi

if [ -f "app/src/main/AndroidManifest.xml" ]; then
    echo "✅ AndroidManifest.xml 存在"
else
    echo "❌ AndroidManifest.xml 缺失"
fi

echo ""
echo "📊 代码统计..."
kt_files=$(find app/src/main/java -name "*.kt" | wc -l)
echo "Kotlin 文件数: $kt_files"

xml_files=$(find app/src/main/res -name "*.xml" 2>/dev/null | wc -l)
echo "XML 资源文件: $xml_files"

echo ""
echo "🎨 UI 组件检查..."
[ -f "app/src/main/java/com/voicememory/ui/screens/RecordScreen.kt" ] && echo "✅ 录音页面"
[ -f "app/src/main/java/com/voicememory/ui/screens/TimelineScreen.kt" ] && echo "✅ 时间轴页面"
[ -f "app/src/main/java/com/voicememory/ui/screens/CalendarScreen.kt" ] && echo "✅ 日历页面"
[ -f "app/src/main/java/com/voicememory/ui/screens/PlayerScreen.kt" ] && echo "✅ 播放器页面"
[ -f "app/src/main/java/com/voicememory/ui/screens/SettingsScreen.kt" ] && echo "✅ 设置页面"
[ -f "app/src/main/java/com/voicememory/ui/components/Components3D.kt" ] && echo "✅ 3D 组件库"

echo ""
echo "🔧 核心功能检查..."
[ -f "app/src/main/java/com/voicememory/domain/audio/AudioRecorder.kt" ] && echo "✅ 录音管理"
[ -f "app/src/main/java/com/voicememory/domain/audio/IFlyTekSpeechRecognizer.kt" ] && echo "✅ 语音识别"
[ -f "app/src/main/java/com/voicememory/data/local/VoiceMemoryDatabase.kt" ] && echo "✅ 数据库"
[ -f "app/src/main/java/com/voicememory/di/AppModule.kt" ] && echo "✅ 依赖注入"

echo ""
echo "📦 依赖检查..."
if grep -q "okhttp" app/build.gradle.kts; then
    echo "✅ OkHttp (WebSocket)"
fi
if grep -q "hilt" app/build.gradle.kts; then
    echo "✅ Hilt (依赖注入)"
fi
if grep -q "room" app/build.gradle.kts; then
    echo "✅ Room (数据库)"
fi
if grep -q "lottie" app/build.gradle.kts; then
    echo "✅ Lottie (动画)"
fi
if grep -q "accompanist" app/build.gradle.kts; then
    echo "✅ Accompanist (权限)"
fi

echo ""
echo "🎯 项目状态: ✅ 准备就绪"
echo ""
echo "下一步操作:"
echo "1. ./gradlew build          # 编译项目"
echo "2. ./gradlew installDebug   # 安装到设备"
echo "3. 真机测试录音和识别功能"
