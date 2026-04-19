package com.voicememory.data.model

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
    val unlockTimestamp: Long? = null
)

enum class Emotion(val displayName: String, val colorHex: String) {
    HAPPY("开心", "#FFD700"),
    CALM("平静", "#87CEEB"),
    ANXIOUS("焦虑", "#FF6347"),
    SAD("悲伤", "#4682B4"),
    EXCITED("兴奋", "#FF69B4"),
    NEUTRAL("中性", "#808080")
}
