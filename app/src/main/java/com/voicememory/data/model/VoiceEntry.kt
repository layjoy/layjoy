package com.voicememory.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_entries")
data class VoiceEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val audioFilePath: String,
    val transcription: String = "",
    val emotion: Emotion = Emotion.NEUTRAL,
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0, // 录音时长（毫秒）
    val isLocked: Boolean = false, // 时光胶囊功能
    val unlockTimestamp: Long? = null,
    val isFavorite: Boolean = false, // 收藏标记
    val summary: String = "", // AI 生成的摘要
    val tags: String = "" // 标签，逗号分隔
)

enum class Emotion(val displayName: String, val colorHex: String) {
    HAPPY("开心", "#FFBF24"),
    CALM("平静", "#60A5FA"),
    ANXIOUS("焦虑", "#F87171"),
    SAD("悲伤", "#818CF8"),
    EXCITED("兴奋", "#F472B6"),
    NEUTRAL("中性", "#94A3B8");
    
    val color: Color
        get() = Color(android.graphics.Color.parseColor(colorHex))
}
