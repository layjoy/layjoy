package com.voicememory.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.voicememory.data.model.Emotion
import com.voicememory.ui.theme.*
import com.voicememory.ui.viewmodel.TrendViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendScreen(
    navController: NavController,
    viewModel: TrendViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("情绪趋势") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 时间范围选择
                    var expanded by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "选择时间范围")
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("最近 7 天") },
                            onClick = {
                                viewModel.setTimeRange(7)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("最近 30 天") },
                            onClick = {
                                viewModel.setTimeRange(30)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("最近 90 天") },
                            onClick = {
                                viewModel.setTimeRange(90)
                                expanded = false
                            }
                        )
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 时间范围标题
                    item {
                        Text(
                            text = "最近 ${uiState.timeRangeDays} 天",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 情绪趋势图
                    item {
                        EmotionTrendChart(
                            data = uiState.trendData,
                            timeRangeDays = uiState.timeRangeDays
                        )
                    }
                    
                    // 情绪分布饼图
                    item {
                        EmotionDistributionCard(
                            emotionStats = uiState.emotionStats
                        )
                    }
                    
                    // 关键洞察
                    item {
                        InsightsCard(
                            insights = uiState.insights
                        )
                    }
                    
                    // 活跃度统计
                    item {
                        ActivityCard(
                            totalEntries = uiState.totalEntries,
                            avgPerDay = uiState.avgEntriesPerDay,
                            mostActiveDay = uiState.mostActiveDay
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmotionTrendChart(
    data: Map<Long, Map<Emotion, Int>>,
    timeRangeDays: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "情绪趋势",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 图表
            if (data.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    EmotionLineChart(data = data)
                }
                
                // 图例
                EmotionLegend()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmotionLineChart(data: Map<Long, Map<Emotion, Int>>) {
    val emotions = Emotion.values()
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val padding = 40f
        
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2
        
        val sortedData = data.toSortedMap()
        val maxValue = sortedData.values.flatMap { it.values }.maxOrNull()?.toFloat() ?: 1f
        
        // 绘制每种情绪的折线
        emotions.forEach { emotion ->
            val path = Path()
            var isFirst = true
            
            sortedData.entries.forEachIndexed { index, (_, emotionCounts) ->
                val x = padding + (index.toFloat() / (sortedData.size - 1).coerceAtLeast(1)) * chartWidth
                val count = emotionCounts[emotion] ?: 0
                val y = padding + chartHeight - (count.toFloat() / maxValue) * chartHeight
                
                if (isFirst) {
                    path.moveTo(x, y)
                    isFirst = false
                } else {
                    path.lineTo(x, y)
                }
            }
            
            drawPath(
                path = path,
                color = emotion.color,
                style = Stroke(width = 4f)
            )
        }
        
        // 绘制坐标轴
        drawLine(
            color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.3f),
            start = Offset(padding, padding),
            end = Offset(padding, height - padding),
            strokeWidth = 2f
        )
        drawLine(
            color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.3f),
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2f
        )
    }
}

@Composable
private fun EmotionLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Emotion.values().forEach { emotion ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(emotion.color)
                )
                Text(
                    text = emotion.displayName,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun EmotionDistributionCard(
    emotionStats: Map<Emotion, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "情绪分布",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            val total = emotionStats.values.sum()
            
            emotionStats.entries.sortedByDescending { it.value }.forEach { (emotion, count) ->
                val percentage = if (total > 0) count.toFloat() / total else 0f
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = emotion.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(percentage * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = emotion.color
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(percentage)
                                .clip(RoundedCornerShape(4.dp))
                                .background(emotion.color)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsCard(insights: List<String>) {
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
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = EmotionHappy,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "关键洞察",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            insights.forEach { insight ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Primary
                    )
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(
    totalEntries: Int,
    avgPerDay: Float,
    mostActiveDay: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "活跃度统计",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "总录音",
                    value = "$totalEntries",
                    icon = Icons.Default.Mic
                )
                StatItem(
                    label = "日均",
                    value = String.format("%.1f", avgPerDay),
                    icon = Icons.Default.CalendarToday
                )
                StatItem(
                    label = "最活跃",
                    value = mostActiveDay,
                    icon = Icons.Default.Star
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
