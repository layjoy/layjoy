package com.voicememory.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecordButton(
    isRecording: Boolean,
    isPaused: Boolean,
    onRecordClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRecording) {
            // 暂停按钮
            FloatingActionButton(
                onClick = onPauseClick,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "继续" else "暂停"
                )
            }
            
            // 停止按钮
            FloatingActionButton(
                onClick = onStopClick,
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "停止"
                )
            }
        } else {
            // 录音按钮
            FloatingActionButton(
                onClick = onRecordClick,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "开始录音",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
