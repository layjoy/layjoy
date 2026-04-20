package com.voicememory.ui.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicememory.domain.repository.VoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VoiceRepository
) : ViewModel() {

    // 设置键
    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val THEME_COLOR = stringPreferencesKey("theme_color")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val RECOGNITION_LANGUAGE = stringPreferencesKey("recognition_language")
    }

    // 设置状态
    data class SettingsState(
        val isDarkMode: Boolean = false,
        val themeColor: String = "default",
        val audioQuality: String = "high",
        val recognitionLanguage: String = "zh_cn",
        val storageUsed: Long = 0L,
        val totalEntries: Int = 0
    )

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSettings()
        calculateStorageUsage()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            context.dataStore.data.collect { preferences ->
                _settingsState.update { state ->
                    state.copy(
                        isDarkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
                        themeColor = preferences[PreferencesKeys.THEME_COLOR] ?: "default",
                        audioQuality = preferences[PreferencesKeys.AUDIO_QUALITY] ?: "high",
                        recognitionLanguage = preferences[PreferencesKeys.RECOGNITION_LANGUAGE] ?: "zh_cn"
                    )
                }
            }
        }
    }

    private fun calculateStorageUsage() {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                val audioDir = File(context.filesDir, "audio")
                val totalSize = if (audioDir.exists()) {
                    audioDir.listFiles()?.sumOf { it.length() } ?: 0L
                } else {
                    0L
                }
                
                _settingsState.update { it.copy(
                    storageUsed = totalSize,
                    totalEntries = entries.size
                ) }
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                val current = preferences[PreferencesKeys.DARK_MODE] ?: false
                preferences[PreferencesKeys.DARK_MODE] = !current
            }
        }
    }

    fun setThemeColor(color: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.THEME_COLOR] = color
            }
        }
    }

    fun setAudioQuality(quality: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.AUDIO_QUALITY] = quality
            }
        }
    }

    fun setRecognitionLanguage(language: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.RECOGNITION_LANGUAGE] = language
            }
        }
    }

    fun exportData(onComplete: (File) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val exportDir = File(context.getExternalFilesDir(null), "exports")
                if (!exportDir.exists()) {
                    exportDir.mkdirs()
                }
                
                val timestamp = System.currentTimeMillis()
                val exportFile = File(exportDir, "voicememory_export_$timestamp.txt")
                
                repository.getAllEntries().first().let { entries ->
                    exportFile.bufferedWriter().use { writer ->
                        writer.write("VoiceMemory 数据导出\n")
                        writer.write("导出时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}\n")
                        writer.write("总记录数: ${entries.size}\n")
                        writer.write("\n" + "=".repeat(50) + "\n\n")
                        
                        entries.forEach { entry ->
                            writer.write("【录音 #${entry.id}】\n")
                            writer.write("时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(entry.timestamp))}\n")
                            writer.write("时长: ${entry.duration / 1000}秒\n")
                            writer.write("情绪: ${entry.emotion.name}\n")
                            writer.write("内容: ${entry.transcription}\n")
                            if (entry.summary.isNotEmpty()) {
                                writer.write("摘要: ${entry.summary}\n")
                            }
                            if (entry.tags.isNotEmpty()) {
                                writer.write("标签: ${entry.tags.joinToString(", ")}\n")
                            }
                            writer.write("\n" + "-".repeat(50) + "\n\n")
                        }
                    }
                }
                
                onComplete(exportFile)
            } catch (e: Exception) {
                onError(e.message ?: "导出失败")
            }
        }
    }

    fun clearAllData(onComplete: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // 删除所有数据库记录
                repository.getAllEntries().first().forEach { entry ->
                    repository.deleteEntry(entry.id)
                }
                
                // 删除所有音频文件
                val audioDir = File(context.filesDir, "audio")
                if (audioDir.exists()) {
                    audioDir.listFiles()?.forEach { it.delete() }
                }
                
                onComplete()
            } catch (e: Exception) {
                onError(e.message ?: "清除失败")
            }
        }
    }

    fun getStorageInfo(): String {
        val state = _settingsState.value
        val sizeMB = state.storageUsed / (1024f * 1024f)
        return String.format("%.2f MB (%d 条录音)", sizeMB, state.totalEntries)
    }
}
