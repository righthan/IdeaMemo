package com.ldlywt.note.ui.page.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldlywt.note.api.memos.MemosApiService
import com.ldlywt.note.api.memos.Memo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val memosApiService: MemosApiService
) : ViewModel() {

    private val _query: MutableStateFlow<String> = MutableStateFlow(value = "")
    val query: StateFlow<String>
        get() = _query

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean>
        get() = _isLoading

    private val _error: MutableStateFlow<String?> = MutableStateFlow(null)
    val error: StateFlow<String?>
        get() = _error

    // Memos搜索结果
    val memosFlow: MutableStateFlow<List<Memo>> = MutableStateFlow(value = emptyList())

    fun clearSearchQuery() {
        _query.value = ""
        memosFlow.value = emptyList()
        _error.value = null
    }

    fun onQuery(query: String) {
        _query.value = query
    }

    fun onSearch(str: String) {
        searchMemos(str)
    }

    private fun searchMemos(query: String) {
        if (query.isBlank()) {
            memosFlow.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = memosApiService.getMemos(
                    pageSize = 50,
                    searchQuery = query
                )
                memosFlow.value = response.memos
            } catch (e: Exception) {
                _error.value = e.message ?: "搜索失败"
                memosFlow.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}