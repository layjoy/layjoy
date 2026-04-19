package com.voicememory.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.voicememory.ui.components.*
import com.voicememory.ui.navigation.Screen
import com.voicememory.ui.theme.*
import com.voicememory.ui.viewmodel.RecordViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordScreen(
    navController: NavController,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 权限请求
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    )
    
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // 背景粒子效果
        if (uiState.isRecording) {
            ParticleBackground(amplitude = uiState.currentAmplitude)
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部导航栏
            TopAppBar(navController)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 录音时长显示
            AnimatedRecordingTime(
                duration = uiState.recordingDuration,
                isRecording = uiState.isRecording
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 3D 波形可视化
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        ambientColor = Primary.copy(alpha = 0.3f),
                        spotColor = Primary.copy(alpha = 0.5f)
                    )
            ) {
                WaveformVisualizer3D(
                    amplitude = uiState.currentAmplitude,
                    isRecording = uiState.isRecording,
                    emotion = uiState.detectedEmotion
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 转写文本预览
            TranscriptionPreview(
                text = uiState.transcription,
                isVisible = uiState.transcription.isNotEmpty()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 录音控制按钮
            RecordControlButtons(
                isRecording = uiState.isRecording,
                isPaused = uiState.isPaused,
                onRecordClick = { viewModel.toggleRecording() },
                onPauseClick = { viewModel.togglePause() },
                onStopClick = { viewModel.stopRecording() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TopAppBar(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "声音日记",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassIconButton(
                icon = Icons.Default.SmartToy,
                onClick = { navController.navigate(Screen.AIChat.route) }
            )
            GlassIconButton(
                icon = Icons.Default.ShowChart,
                onClick = { navController.navigate(Screen.Trend.route) }
            )
            GlassIconButton(
                icon = Icons.Default.CalendarMonth,
                onClick = { navController.navigate(Screen.Calendar.route) }
            )
            GlassIconButton(
                icon = Icons.Default.Timeline,
                onClick = { navController.navigate(Screen.Timeline.route) }
            )
            GlassIconButton(
                icon = Icons.Default.Settings,
                onClick = { navController.navigate(Screen.Settings.route) }
            )
        }
    }
}

@Composable
private fun AnimatedRecordingTime(
    duration: Long,
    isRecording: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1f else 0.95f,
        animationSpec = tween(300)
    )
    
    val minutes = (duration / 60000).toInt()
    val seconds = ((duration % 60000) / 1000).toInt()
    val timeText = String.format("%02d:%02d", minutes, seconds)
    
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(
                color = if (isRecording) 
                    Error.copy(alpha = 0.1f) 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isRecording) {
                PulsingDot()
            }
            Text(
                text = timeText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isRecording) Error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(Error.copy(alpha = alpha))
    )
}

@Composable
private fun TranscriptionPreview(
    text: String,
    isVisible: Boolean
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 120.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4
            )
        }
    }
}

@Composable
private fun RecordControlButtons(
    isRecording: Boolean,
    isPaused: Boolean,
    onRecordClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 暂停按钮
        AnimatedVisibility(
            visible = isRecording,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            FloatingActionButton(
                onClick = onPauseClick,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "继续" else "暂停",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        
        // 主录音按钮
        RecordButton3D(
            isRecording = isRecording,
            onClick = onRecordClick
        )
        
        // 停止按钮
        AnimatedVisibility(
            visible = isRecording,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            FloatingActionButton(
                onClick = onStopClick,
                containerColor = Error.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "停止",
                    tint = Error
                )
            }
        }
    }
}
