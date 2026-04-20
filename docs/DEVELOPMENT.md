# VoiceMemory 项目开发指南

## 项目概述

VoiceMemory 是一款声音日记 Android 应用，已完成基础架构搭建。

## 已完成的工作

### ✅ 项目架构
- MVI 架构 + Clean Architecture
- Hilt 依赖注入配置
- Room 数据库设计
- Repository 模式实现

### ✅ 数据层
- `VoiceEntry` 数据模型（包含情绪枚举）
- `VoiceEntryDao` 数据访问接口
- `VoiceMemoryDatabase` 数据库配置
- `VoiceRepository` 仓储接口和实现

### ✅ UI 层
- Jetpack Compose + Material Design 3
- `MainScreen` 主界面
- `WaveformVisualizer` 波形可视化组件
- `RecordButton` 录音按钮组件
- `RecordViewModel` 状态管理

### ✅ 配置文件
- Gradle 构建配置
- AndroidManifest.xml（包含录音权限）
- 主题和字符串资源

## 待实现功能

### 1. 录音功能（优先级：高）

需要实现 `AudioRecorder` 类：

```kotlin
class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    
    fun startRecording(outputFile: File) {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
    }
    
    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }
    
    fun getMaxAmplitude(): Int {
        return mediaRecorder?.maxAmplitude ?: 0
    }
}
```

在 `RecordViewModel` 中集成：
- 创建 `AudioRecorder` 实例
- 在 `startRecording()` 中调用录音
- 使用协程定时更新振幅和时长
- 在 `stopRecording()` 中保存到数据库

### 2. 权限处理（优先级：高）

在 `MainScreen` 中添加：

```kotlin
val permissionState = rememberPermissionState(
    android.Manifest.permission.RECORD_AUDIO
)

LaunchedEffect(Unit) {
    if (!permissionState.status.isGranted) {
        permissionState.launchPermissionRequest()
    }
}
```

### 3. 语音识别（优先级：中）

**方案 A：Google Speech-to-Text**
```kotlin
// 添加依赖
implementation("com.google.cloud:google-cloud-speech:2.5.0")

// 实现转写
class SpeechRecognizer {
    suspend fun transcribe(audioFile: File): String {
        // 调用 Google API
    }
}
```

**方案 B：讯飞语音（国内推荐）**
```kotlin
// 添加讯飞 SDK
implementation("com.iflytek:msc:5.0.0")
```

### 4. 情绪分析（优先级：中）

**简单方案：关键词匹配**
```kotlin
object EmotionAnalyzer {
    fun analyze(text: String): Emotion {
        return when {
            text.contains("开心|高兴|快乐".toRegex()) -> Emotion.HAPPY
            text.contains("难过|悲伤|伤心".toRegex()) -> Emotion.SAD
            text.contains("焦虑|紧张|担心".toRegex()) -> Emotion.ANXIOUS
            text.contains("平静|安静|放松".toRegex()) -> Emotion.CALM
            else -> Emotion.NEUTRAL
        }
    }
}
```

**高级方案：调用 API**
- 腾讯云文本情感分析
- 阿里云情感分析

### 5. 时间轴视图（优先级：低）

创建 `TimelineScreen.kt`：
```kotlin
@Composable
fun TimelineScreen(viewModel: TimelineViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsState()
    
    LazyColumn {
        items(entries) { entry ->
            VoiceEntryCard(entry)
        }
    }
}
```

### 6. 日历视图（优先级：低）

使用第三方库：
```kotlin
implementation("com.kizitonwose.calendar:compose:2.4.0")
```

## 如何运行项目

### 1. 在 Android Studio 中打开

```bash
# 打开 Android Studio
# File -> Open -> 选择 /Users/admin/Projects/voiceMemory
```

### 2. 同步 Gradle

点击 "Sync Project with Gradle Files"

### 3. 运行应用

- 连接 Android 设备或启动模拟器
- 点击 Run 按钮（绿色三角形）

### 4. 命令行构建

```bash
cd /Users/admin/Projects/voiceMemory
./gradlew assembleDebug
```

## 开发建议

### 第一阶段：核心功能
1. 实现录音功能
2. 添加权限处理
3. 完善波形可视化
4. 实现音频播放

### 第二阶段：智能功能
1. 集成语音识别
2. 实现情绪分析
3. 添加情绪配色

### 第三阶段：体验优化
1. 时间轴视图
2. 日历热力图
3. 3D 可视化效果
4. 时光胶囊功能

## 技术要点

### 录音最佳实践
- 使用 MediaRecorder API
- 采样率：44100Hz
- 编码格式：AAC
- 文件格式：MP4

### 性能优化
- 使用协程处理异步操作
- Flow 实现响应式数据流
- Room 数据库查询优化
- Compose 重组优化

### 测试建议
- 单元测试：ViewModel 逻辑
- 集成测试：Repository 层
- UI 测试：Compose UI 测试

## 常见问题

### Q: 录音没有声音？
A: 检查权限是否授予，检查 MediaRecorder 配置

### Q: 编译失败？
A: 确保 JDK 17，Gradle 8.2，Android SDK 34

### Q: 依赖下载慢？
A: 配置国内镜像：
```gradle
repositories {
    maven { url 'https://maven.aliyun.com/repository/google' }
    maven { url 'https://maven.aliyun.com/repository/public' }
}
```

## 参考资源

- [Jetpack Compose 文档](https://developer.android.com/jetpack/compose)
- [MediaRecorder API](https://developer.android.com/reference/android/media/MediaRecorder)
- [Room 数据库](https://developer.android.com/training/data-storage/room)
- [Hilt 依赖注入](https://developer.android.com/training/dependency-injection/hilt-android)

## 联系方式

如有问题，请查看项目 README.md 或提交 Issue。
