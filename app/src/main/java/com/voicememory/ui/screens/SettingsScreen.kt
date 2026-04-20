package com.voicememory.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.voicememory.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settingsState by viewModel.settingsState.collectAsState()
    
    var showQualityDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    
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
                    subtitle = if (settingsState.isDarkMode) "已开启" else "已关闭",
                    onClick = { viewModel.toggleDarkMode() }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "主题颜色",
                    subtitle = when (settingsState.themeColor) {
                        "blue" -> "蓝色"
                        "purple" -> "紫色"
                        "green" -> "绿色"
                        else -> "默认"
                    },
                    onClick = { showThemeDialog = true }
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
                    subtitle = when (settingsState.audioQuality) {
                        "high" -> "高质量 (16kHz)"
                        "medium" -> "中等质量 (8kHz)"
                        else -> "低质量 (4kHz)"
                    },
                    onClick = { showQualityDialog = true }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "识别语言",
                    subtitle = when (settingsState.recognitionLanguage) {
                        "zh_cn" -> "中文普通话"
                        "en_us" -> "英语"
                        else -> "粤语"
                    },
                    onClick = { showLanguageDialog = true }
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
                    subtitle = viewModel.getStorageInfo(),
                    onClick = { 
                        Toast.makeText(context, "存储信息：${viewModel.getStorageInfo()}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.FileDownload,
                    title = "导出数据",
                    subtitle = "导出所有录音和文本",
                    onClick = {
                        viewModel.exportData(
                            onComplete = { file ->
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "分享导出文件"))
                            },
                            onError = { error ->
                                Toast.makeText(context, "导出失败: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "清除所有数据",
                    subtitle = "删除所有录音和记录",
                    onClick = { showClearDataDialog = true }
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
                    onClick = {
                        Toast.makeText(context, "VoiceMemory v1.0.0", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "隐私政策",
                    subtitle = "查看隐私政策",
                    onClick = {
                        Toast.makeText(context, "所有数据仅存储在本地，不会上传到云端", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
    
    // 录音质量选择对话框
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("选择录音质量") },
            text = {
                Column {
                    listOf(
                        "high" to "高质量 (16kHz)",
                        "medium" to "中等质量 (8kHz)",
                        "low" to "低质量 (4kHz)"
                    ).forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAudioQuality(value)
                                    showQualityDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settingsState.audioQuality == value,
                                onClick = {
                                    viewModel.setAudioQuality(value)
                                    showQualityDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 识别语言选择对话框
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("选择识别语言") },
            text = {
                Column {
                    listOf(
                        "zh_cn" to "中文普通话",
                        "en_us" to "英语",
                        "zh_yue" to "粤语"
                    ).forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setRecognitionLanguage(value)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settingsState.recognitionLanguage == value,
                                onClick = {
                                    viewModel.setRecognitionLanguage(value)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 主题颜色选择对话框
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("选择主题颜色") },
            text = {
                Column {
                    listOf(
                        "default" to "默认",
                        "blue" to "蓝色",
                        "purple" to "紫色",
                        "green" to "绿色"
                    ).forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeColor(value)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settingsState.themeColor == value,
                                onClick = {
                                    viewModel.setThemeColor(value)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 清除数据确认对话框
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("确认清除") },
            text = { Text("此操作将删除所有录音和记录，且无法恢复。确定要继续吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData(
                            onComplete = {
                                Toast.makeText(context, "已清除所有数据", Toast.LENGTH_SHORT).show()
                                showClearDataDialog = false
                            },
                            onError = { error ->
                                Toast.makeText(context, "清除失败: $error", Toast.LENGTH_SHORT).show()
                                showClearDataDialog = false
                            }
                        )
                    }
                ) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("取消")
                }
            }
        )
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
