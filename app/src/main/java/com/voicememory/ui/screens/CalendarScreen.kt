package com.voicememory.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.voicememory.data.model.Emotion
import com.voicememory.ui.navigation.Screen
import com.voicememory.ui.theme.*
import com.voicememory.ui.viewmodel.CalendarViewModel
import com.voicememory.ui.viewmodel.DayData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日历热力图") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                    // 月份选择器
                    item {
                        MonthSelector(
                            currentMonth = uiState.currentMonth,
                            onPrevious = { viewModel.previousMonth() },
                            onNext = { viewModel.nextMonth() }
                        )
                    }
                    
                    // 统计卡片
                    item {
                        StatsCard(
                            totalEntries = uiState.totalEntries,
                            emotionStats = uiState.emotionStats
                        )
                    }
                    
                    // 日历热力图
                    item {
                        CalendarHeatmap(
                            currentMonth = uiState.currentMonth,
                            daysData = uiState.daysData,
                            onDayClick = { viewModel.selectDay(it) }
                        )
                    }
                    
                    // 选中日期的详情
                    item {
                        AnimatedVisibility(
                            visible = uiState.selectedDay != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            uiState.selectedDay?.let { dayData ->
                                DayDetailCard(
                                    dayData = dayData,
                                    onEntryClick = { entry ->
                                        navController.navigate(Screen.Player.createRoute(entry.id))
                                    },
                                    onClose = { viewModel.selectDay(null) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    currentMonth: Date,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
            }
            
            Text(
                text = SimpleDateFormat("yyyy年 MM月", Locale.CHINA).format(currentMonth),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
            }
        }
    }
}

@Composable
private fun StatsCard(
    totalEntries: Int,
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
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "本月统计",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 总录音数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "总录音数",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$totalEntries 条",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            
            Divider()
            
            // 情绪分布
            Text(
                text = "情绪分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                emotionStats.entries.sortedByDescending { it.value }.forEach { (emotion, count) ->
                    EmotionStatRow(
                        emotion = emotion,
                        count = count,
                        total = totalEntries
                    )
                }
            }
        }
    }
}

@Composable
private fun EmotionStatRow(
    emotion: Emotion,
    count: Int,
    total: Int
) {
    val percentage = if (total > 0) count.toFloat() / total else 0f
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(emotion.color)
        )
        
        Text(
            text = emotion.displayName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(60.dp)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
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
        
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = emotion.color,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun CalendarHeatmap(
    currentMonth: Date,
    daysData: Map<String, DayData>,
    onDayClick: (DayData?) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 星期标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // 日历网格
            val calendar = Calendar.getInstance().apply {
                time = currentMonth
                set(Calendar.DAY_OF_MONTH, 1)
            }
            
            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(300.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 空白占位
                items(firstDayOfWeek) {
                    Box(modifier = Modifier.aspectRatio(1f))
                }
                
                // 日期格子
                items(daysInMonth) { dayIndex ->
                    val day = dayIndex + 1
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-$day"
                    val dayData = daysData[dateKey]
                    
                    DayCell(
                        day = day,
                        dayData = dayData,
                        onClick = { onDayClick(dayData) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    dayData: DayData?,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        dayData == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else -> dayData.dominantEmotion?.color?.copy(alpha = 0.3f + dayData.intensity * 0.7f)
            ?: MaterialTheme.colorScheme.surfaceVariant
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (dayData != null) 2.dp else 0.dp,
                color = dayData?.dominantEmotion?.color ?: androidx.compose.ui.graphics.Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = dayData != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (dayData != null) FontWeight.Bold else FontWeight.Normal,
            color = if (dayData != null) 
                MaterialTheme.colorScheme.onSurface 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun DayDetailCard(
    dayData: DayData,
    onEntryClick: (com.voicememory.data.model.VoiceEntry) -> Unit,
    onClose: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("MM月dd日", Locale.CHINA).format(dayData.date),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }
            
            Text(
                text = "${dayData.entries.size} 条录音",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Divider()
            
            dayData.entries.forEach { entry ->
                EntryItem(
                    entry = entry,
                    onClick = { onEntryClick(entry) }
                )
            }
        }
    }
}

@Composable
private fun EntryItem(
    entry: com.voicememory.data.model.VoiceEntry,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(entry.emotion.color.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.PlayCircle,
            contentDescription = null,
            tint = entry.emotion.color,
            modifier = Modifier.size(32.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(entry.timestamp)),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = entry.transcription.take(30) + if (entry.transcription.length > 30) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(entry.emotion.color.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = entry.emotion.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = entry.emotion.color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
