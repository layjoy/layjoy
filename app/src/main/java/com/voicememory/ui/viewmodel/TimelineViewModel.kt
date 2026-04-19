package com.voicememory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicememory.data.model.VoiceEntry
import com.voicememory.domain.repository.VoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: VoiceRepository
) : ViewModel() {
    
    private val _entries = MutableStateFlow<List<VoiceEntry>>(emptyList())
    val entries: StateFlow<List<VoiceEntry>> = _entries.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        loadEntries()
    }
    
    private fun loadEntries() {
        viewModelScope.launch {
            repository.getAllEntries().collect { entryList ->
                _entries.value = entryList
            }
        }
    }
    
    fun search(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isEmpty()) {
                loadEntries()
            } else {
                repository.searchEntries(query).collect { results ->
                    _entries.value = results
                }
            }
        }
    }
    
    fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            _entries.value.find { it.id == entryId }?.let { entry ->
                repository.deleteEntry(entry)
            }
        }
    }
}
