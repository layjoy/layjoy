package com.voicememory.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voicememory.data.model.Emotion
import com.voicememory.domain.ai.EmotionAnalysisResult
import com.voicememory.ui.theme.*

@Composable
fun AIAnalysisCard(
    summary: String,
    emotionAnalysis: EmotionAnalysisResult?,
    tags: List<String>,
    isLoading: Boolean,
    onRegenerateSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "AI 分析",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Divider()
            
            if (isLoading) {
                LoadingIndicator()
            } else {
                // 智能摘要
                if (summary.isNotEmpty()) {
                    SummarySection(summary, onRegenerateSummary)
                }
                
                // 情绪分析
                if (emotionAnalysis != null) {
                    EmotionAnalysisSection(emotionAnalysis)
                }
                
                // 主题标签
                if (tags.isNotEmpty()) {
                    TagsSection(tags)
                }
            }
        }
    }
}

@Composable
private fun SummarySection(
    summary: String,
    onRegenerate: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = EmotionHappy,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "智能摘要",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            IconButton(
                onClick = onRegenerate,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "重新生成",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmotionAnalysisSection(analysis: EmotionAnalysisResult) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = EmotionExcited,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "情绪分析",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // 情绪强度条
        EmotionIntensityBar(
            emotion = analysis.emotion,
            intensity = analysis.intensity
        )
        
        // 原因分析
        InfoBox(
            icon = Icons.Default.Lightbulb,
            title = "原因",
            content = analysis.reason,
            color = Info
        )
        
        // AI 建议
        InfoBox(
            icon = Icons.Default.TipsAndUpdates,
            title = "建议",
            content = analysis.suggestion,
            color = Success
        )
    }
}

@Composable
private fun EmotionIntensityBar(
    emotion: Emotion,
    intensity: Float
) {
    val emotionColor = getEmotionColor(emotion)
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = emotion.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = emotionColor
            )
            Text(
                text = "${(intensity * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = emotionColor
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(intensity)
                    .clip(RoundedCornerShape(6.dp))
                    .background(emotionColor)
            )
        }
    }
}

@Composable
private fun InfoBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TagsSection(tags: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Label,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "主题标签",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                TagChip(tag)
            }
        }
    }
}

@Composable
private fun TagChip(tag: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Secondary.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "#$tag",
            style = MaterialTheme.typography.labelMedium,
            color = Secondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Primary
        )
        Text(
            text = "AI 正在分析中...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getEmotionColor(emotion: Emotion): androidx.compose.ui.graphics.Color {
    return when (emotion) {
        Emotion.HAPPY -> EmotionHappy
        Emotion.CALM -> EmotionCalm
        Emotion.ANXIOUS -> EmotionAnxious
        Emotion.SAD -> EmotionSad
        Emotion.EXCITED -> EmotionExcited
        Emotion.NEUTRAL -> EmotionNeutral
    }
}
