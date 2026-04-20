package com.voicememory.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.voicememory.data.model.VoiceEntry
import com.voicememory.ui.components.AIAnalysisCard
import com.voicememory.ui.components.WaveformVisualizer
import com.voicememory.ui.theme.*
import com.voicememory.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    navController: NavController,
    entryId: Long,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("播放") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (uiState.entry?.isFavorite == true) 
                                Icons.Default.Favorite 
                            else 
                                Icons.Default.FavoriteBorder,
                            contentDescription = "收藏",
                            tint = if (uiState.entry?.isFavorite == true) 
                                EmotionHappy 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { 
                        viewModel.shareEntry { shareText, audioFilePath ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                
                                // 如果有音频文件，尝试附加
                                audioFilePath?.let { path ->
                                    val file = File(path)
                                    if (file.exists()) {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        type = "audio/*"
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                }
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "分享语音记录")
                            context.startActivity(shareIntent)
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "分享")
                    }
                    IconButton(onClick = { viewModel.deleteEntry() }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
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
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.entry != null) {
                PlayerContent(
                    entry = uiState.entry!!,
                    isPlaying = uiState.isPlaying,
                    currentPosition = uiState.currentPosition,
                    duration = uiState.duration,
                    playbackSpeed = uiState.playbackSpeed,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onSeek = { viewModel.seekTo(it) },
                    onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                    onRewind = { viewModel.rewind() },
                    onForward = { viewModel.forward() },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun PlayerContent(
    entry: VoiceEntry,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    viewModel: PlayerViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 日期和时长
        EntryHeader(entry, duration)
        
        // 波形可视化
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = entry.emotion.color.copy(alpha = 0.1f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                WaveformVisualizer(
                    isRecording = isPlaying,
                    amplitude = if (isPlaying) 0.7f else 0f,
                    color = entry.emotion.color
                )
            }
        }
        
        // 进度条
        ProgressSection(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = onSeek
        )
        
        // 播放控制
        PlaybackControls(
            isPlaying = isPlaying,
            playbackSpeed = playbackSpeed,
            onPlayPause = onPlayPause,
            onRewind = onRewind,
            onForward = onForward,
            onSpeedChange = onSpeedChange,
            emotionColor = entry.emotion.color
        )
        
        // 转写文本
        TranscriptionCard(entry.transcription)
        
        // AI 分析
        val aiState by viewModel.aiAnalysisState.collectAsState()
        AIAnalysisCard(
            summary = aiState.summary,
            emotionAnalysis = aiState.emotionAnalysis,
            tags = aiState.tags,
            isLoading = aiState.isLoading,
            onRegenerateSummary = { viewModel.regenerateAnalysis() }
        )
    }
}

@Composable
private fun EntryHeader(entry: VoiceEntry, duration: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
                    .format(Date(entry.timestamp)),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = SimpleDateFormat("HH:mm", Locale.CHINA)
                    .format(Date(entry.timestamp)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        EmotionBadge(entry.emotion)
    }
}

@Composable
private fun EmotionBadge(emotion: com.voicememory.data.model.Emotion) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(emotion.color.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = emotion.displayName,
            style = MaterialTheme.typography.labelLarge,
            color = emotion.color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProgressSection(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..duration.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Primary,
                activeTrackColor = Primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    emotionColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 倍速按钮
        SpeedButton(
            speed = playbackSpeed,
            onClick = {
                val nextSpeed = when (playbackSpeed) {
                    0.5f -> 0.75f
                    0.75f -> 1.0f
                    1.0f -> 1.25f
                    1.25f -> 1.5f
                    1.5f -> 2.0f
                    else -> 0.5f
                }
                onSpeedChange(nextSpeed)
            }
        )
        
        // 后退 15 秒
        IconButton(
            onClick = onRewind,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Replay,
                contentDescription = "后退 15 秒",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // 播放/暂停
        FloatingActionButton(
            onClick = onPlayPause,
            modifier = Modifier.size(72.dp),
            containerColor = emotionColor,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        // 前进 15 秒
        IconButton(
            onClick = onForward,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Forward30,
                contentDescription = "前进 15 秒",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // 循环播放
        IconButton(
            onClick = { viewModel.toggleLoop() },
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = "循环播放",
                modifier = Modifier.size(28.dp),
                tint = if (uiState.isLooping) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SpeedButton(
    speed: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${speed}x",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TranscriptionCard(transcription: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Subtitles,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "转写文本",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = transcription,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}
