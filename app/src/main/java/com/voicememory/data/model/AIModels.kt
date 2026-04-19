package com.voicememory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI 分析结果
 */
@Entity(tableName = "ai_analysis")
data class AIAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryId: Long, // 关联的录音 ID
    val summary: String = "", // 智能摘要
    val emotionAnalysis: String = "", // 情绪深度分析
    val emotionIntensity: Float = 0f, // 情绪强度 0-1
    val tags: List<String> = emptyList(), // 主题标签
    val suggestions: String = "", // AI 建议
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * AI 对话消息
 */
data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 趋势分析报告
 */
data class TrendReport(
    val period: String, // "week" or "month"
    val startDate: Long,
    val endDate: Long,
    val emotionDistribution: Map<Emotion, Float>, // 情绪分布
    val topTopics: List<String>, // 高频话题
    val emotionTrend: List<EmotionPoint>, // 情绪趋势点
    val insights: String, // AI 洞察
    val suggestions: String // AI 建议
)

data class EmotionPoint(
    val timestamp: Long,
    val emotion: Emotion,
    val intensity: Float
)
