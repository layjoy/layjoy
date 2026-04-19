package com.voicememory.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.voicememory.data.model.Emotion
import com.voicememory.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

@Composable
fun WaveformVisualizer3D(
    amplitude: Float,
    isRecording: Boolean,
    emotion: Emotion?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1f + amplitude * 0.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val emotionColor = emotion?.let { getEmotionColor(it) } ?: Primary
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // 外层光晕
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(40.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            emotionColor.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // 3D 圆环
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2 * 0.8f
            
            // 绘制多层圆环
            for (i in 0..5) {
                val layerRadius = radius * (1f - i * 0.12f)
                val layerAlpha = 1f - i * 0.15f
                val layerAmplitude = amplitude * (1f - i * 0.2f)
                
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            emotionColor.copy(alpha = layerAlpha * 0.3f),
                            emotionColor.copy(alpha = layerAlpha * 0.6f),
                            emotionColor.copy(alpha = layerAlpha * 0.3f)
                        ),
                        center = Offset(centerX, centerY)
                    ),
                    radius = layerRadius + layerAmplitude * 20f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 3f)
                )
            }
            
            // 绘制波形点
            if (isRecording) {
                val points = 60
                for (i in 0 until points) {
                    val angle = (i * 360f / points + rotation) * PI.toFloat() / 180f
                    val waveAmplitude = sin(angle * 3f + rotation * 0.1f) * amplitude * 30f
                    val pointRadius = radius + waveAmplitude
                    
                    val x = centerX + cos(angle) * pointRadius
                    val y = centerY + sin(angle) * pointRadius
                    
                    drawCircle(
                        color = emotionColor.copy(alpha = 0.8f),
                        radius = 4f + amplitude * 6f,
                        center = Offset(x, y)
                    )
                }
            }
        }
        
        // 中心图标
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            emotionColor.copy(alpha = 0.2f),
                            emotionColor.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = emotionColor
            )
        }
    }
}

@Composable
fun RecordButton3D(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // 脉冲效果
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Error.copy(alpha = 0.2f))
            )
        }
        
        // 主按钮
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = if (isRecording) Error else Primary,
                    spotColor = if (isRecording) Error else Primary
                ),
            containerColor = if (isRecording) Error else Primary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                imageVector = if (isRecording) 
                    Icons.Default.Stop 
                else 
                    Icons.Default.Mic,
                contentDescription = if (isRecording) "停止录音" else "开始录音",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                )
            )
            .padding(20.dp),
        content = content
    )
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ParticleBackground(
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        List(30) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = Random.nextFloat() * 0.5f + 0.2f,
                size = Random.nextFloat() * 4f + 2f
            )
        }
    }
    
    var time by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis {
                time += 0.016f
            }
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val x = (particle.x + time * particle.speed * 0.1f) % 1f
            val y = particle.y
            val alpha = (sin(time * particle.speed * 2f) * 0.5f + 0.5f) * amplitude
            
            drawCircle(
                color = Primary.copy(alpha = alpha * 0.3f),
                radius = particle.size * (1f + amplitude * 2f),
                center = Offset(
                    x = x * size.width,
                    y = y * size.height
                )
            )
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float
)

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
