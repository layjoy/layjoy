package com.voicememory.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var isDarkMode by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
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
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsSection(title = "外观")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "深色模式",
                    subtitle = "自动跟随系统",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "主题颜色",
                    subtitle = "自定义应用主题",
                    onClick = { }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSection(title = "录音设置")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Mic,
                    title = "录音质量",
                    subtitle = "高质量 (16kHz)",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "识别语言",
                    subtitle = "中文普通话",
                    onClick = { }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSection(title = "数据管理")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "存储空间",
                    subtitle = "查看已使用空间",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.CloudUpload,
                    title = "云端同步",
                    subtitle = "备份到云端",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.FileDownload,
                    title = "导出数据",
                    subtitle = "导出所有录音和文本",
                    onClick = { }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSection(title = "关于")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "版本信息",
                    subtitle = "v1.0.0",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "隐私政策",
                    subtitle = "查看隐私政策",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
