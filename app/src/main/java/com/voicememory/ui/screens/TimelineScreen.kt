package com.voicememory.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.voicememory.data.model.Emotion
import com.voicememory.data.model.VoiceEntry
import com.voicememory.ui.components.GlassCard
import com.voicememory.ui.components.GlassIconButton
import com.voicememory.ui.navigation.Screen
import com.voicememory.ui.theme.*
import com.voicememory.ui.viewmodel.TimelineViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    navController: NavController,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val entries by viewModel.entries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "时间轴",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: 搜索 */ }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            if (entries.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        TimelineCard(
                            entry = entry,
                            onClick = {
                                navController.navigate(Screen.Player.createRoute(entry.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineCard(
    entry: VoiceEntry,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val emotionColor = getEmotionColor(entry.emotion)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = emotionColor.copy(alpha = 0.2f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 情绪标签
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(emotionColor.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = entry.emotion.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = emotionColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // 时间
                Text(
                    text = formatTime(entry.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 转写文本
            Text(
                text = entry.transcription,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )
            
            if (entry.transcription.length > 100) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Text(if (isExpanded) "收起" else "展开")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 底部信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(entry.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { /* TODO: 分享 */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "分享",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { /* TODO: 删除 */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            modifier = Modifier.size(18.dp),
                            tint = Error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MicNone,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "还没有录音",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "开始记录你的声音日记吧",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        diff < 604800000 -> "${diff / 86400000}天前"
        else -> SimpleDateFormat("MM月dd日", Locale.CHINA).format(Date(timestamp))
    }
}

private fun formatDuration(duration: Long): String {
    val minutes = (duration / 60000).toInt()
    val seconds = ((duration % 60000) / 1000).toInt()
    return String.format("%d:%02d", minutes, seconds)
}

private fun getEmotionColor(emotion: Emotion): Color {
    return when (emotion) {
        Emotion.HAPPY -> EmotionHappy
        Emotion.CALM -> EmotionCalm
        Emotion.ANXIOUS -> EmotionAnxious
        Emotion.SAD -> EmotionSad
        Emotion.EXCITED -> EmotionExcited
        Emotion.NEUTRAL -> EmotionNeutral
    }
}
