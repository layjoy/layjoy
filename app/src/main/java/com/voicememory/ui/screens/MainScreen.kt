package com.voicememory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voicememory.ui.components.RecordButton
import com.voicememory.ui.components.WaveformVisualizer
import com.voicememory.ui.viewmodel.RecordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: RecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("声音日记") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 波形可视化
            WaveformVisualizer(
                isRecording = uiState.isRecording,
                amplitude = uiState.currentAmplitude,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 录音时长显示
            Text(
                text = formatDuration(uiState.recordingDuration),
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 录音按钮
            RecordButton(
                isRecording = uiState.isRecording,
                isPaused = uiState.isPaused,
                onRecordClick = { viewModel.toggleRecording() },
                onPauseClick = { viewModel.togglePause() },
                onStopClick = { viewModel.stopRecording() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 状态提示
            if (uiState.isRecording) {
                Text(
                    text = if (uiState.isPaused) "已暂停" else "录音中...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // 转写文本
            if (uiState.transcription.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.transcription,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}
