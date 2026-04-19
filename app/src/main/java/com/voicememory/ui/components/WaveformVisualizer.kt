package com.voicememory.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isRecording: Boolean,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(0f) }
    
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (true) {
                phase += 0.1f
                kotlinx.coroutines.delay(16) // ~60fps
            }
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        val path = Path()
        val points = 100
        
        for (i in 0..points) {
            val x = (i.toFloat() / points) * width
            val normalizedAmplitude = if (isRecording) amplitude else 0.1f
            val y = centerY + sin((i.toFloat() / points) * 4 * Math.PI + phase).toFloat() * 
                    normalizedAmplitude * height * 0.3f
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = if (isRecording) Color(0xFF6200EE) else Color.Gray,
            style = Stroke(width = 4f)
        )
    }
}
