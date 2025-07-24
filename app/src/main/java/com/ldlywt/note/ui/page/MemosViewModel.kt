package com.ldlywt.note.ui.page

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldlywt.note.api.memos.Memo
import com.ldlywt.note.api.memos.MemosApiService
import com.ldlywt.note.api.users.UsersApiService
import com.ldlywt.note.ui.page.settings.Level
import com.ldlywt.note.utils.SharedPreferencesUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MemosViewModel @Inject constructor(
    private val memosApiService: MemosApiService,
    private val usersApiService: UsersApiService
) : ViewModel() {
    
    private val _memos = MutableStateFlow<List<Memo>>(emptyList())
    val memos: StateFlow<List<Memo>> = _memos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _nextPageToken = MutableStateFlow("")
    val nextPageToken: StateFlow<String> = _nextPageToken.asStateFlow()
    
    // 用户统计信息状态
    private val _userStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val userStats: StateFlow<Map<String, Int>> = _userStats.asStateFlow()
    
    private val _totalMemoCount = MutableStateFlow(0)
    val totalMemoCount: StateFlow<Int> = _totalMemoCount.asStateFlow()
    
    private val _memoTimestamps = MutableStateFlow<List<String>>(emptyList())
    val memoTimestamps: StateFlow<List<String>> = _memoTimestamps.asStateFlow()
    
    // 热力图数据
    var levelMemosMap = mutableStateMapOf<LocalDate, Level>()
        private set
    
    // 天数统计（去重后的天数）
    private val _uniqueDaysCount = MutableStateFlow(0)
    val uniqueDaysCount: StateFlow<Int> = _uniqueDaysCount.asStateFlow()
    
    init {
        // 只有在配置完成且登录成功时才加载数据
        viewModelScope.launch {
            checkConfigAndLoadMemos()
        }
    }
    
    fun loadMemos(pageToken: String = "", isRefreshOperation: Boolean = false, isLoadMoreOperation: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            if (isRefreshOperation) {
                _isRefreshing.value = true
            }
            if (isLoadMoreOperation) {
                _isLoadingMore.value = true
            }
            _error.value = null
            
            try {
                val response = memosApiService.getMemos(
                    pageSize = 5,
                    pageToken = pageToken
                )
                
                if (pageToken.isEmpty()) {
                    // 首次加载，替换数据
                    _memos.value = response.memos
                } else {
                    // 分页加载，追加数据
                    _memos.value = _memos.value + response.memos
                }
                
                _nextPageToken.value = response.nextPageToken
                
            } catch (e: Exception) {
                _error.value = e.message ?: "加载笔记失败"
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
                _isLoadingMore.value = false
            }
        }
    }
    
    fun loadMoreMemos() {
        if (_nextPageToken.value.isNotEmpty() && !_isLoading.value) {
            loadMemos(_nextPageToken.value, isLoadMoreOperation = true)
        }
    }
    
    fun refreshMemos() {
        loadMemos(isRefreshOperation = true)
    }
    
    /**
     * 加载用户统计信息
     */
    private fun loadUserStats() {
        viewModelScope.launch {
            try {
                val response = usersApiService.getUserStats()
                
                // 更新状态
                _userStats.value = response.tagCount
                _totalMemoCount.value = response.totalMemoCount
                _memoTimestamps.value = response.memoDisplayTimestamps
                
                // 计算热力图数据和天数统计
                updateHeatMapData(response.memoDisplayTimestamps)
                
                // 保存到 SharedPreferences
                val tagCountJson = Json.encodeToString(response.tagCount)
                SharedPreferencesUtils.updateMemosTagCount(tagCountJson)
                SharedPreferencesUtils.updateMemosTotalMemoCount(response.totalMemoCount)
                val timestampsJson = Json.encodeToString(response.memoDisplayTimestamps)
                SharedPreferencesUtils.updateMemosDisplayTimestamps(timestampsJson)
                
            } catch (e: Exception) {
                // 如果加载失败，尝试从本地缓存读取
                loadUserStatsFromCache()
            }
        }
    }
    
    /**
     * 从本地缓存加载用户统计信息
     */
    private suspend fun loadUserStatsFromCache() {
        try {
            val tagCountJson = SharedPreferencesUtils.memosTagCount.first()
            val totalCountStr = SharedPreferencesUtils.memosTotalMemoCount.first()
            val timestampsJson = SharedPreferencesUtils.memosDisplayTimestamps.first()
            
            if (!tagCountJson.isNullOrEmpty()) {
                val tagCount = Json.decodeFromString<Map<String, Int>>(tagCountJson)
                _userStats.value = tagCount
            }
            
            if (!totalCountStr.isNullOrEmpty()) {
                _totalMemoCount.value = totalCountStr.toIntOrNull() ?: 0
            }
            
            if (!timestampsJson.isNullOrEmpty()) {
                val timestamps = Json.decodeFromString<List<String>>(timestampsJson)
                _memoTimestamps.value = timestamps
                // 从缓存数据计算热力图
                updateHeatMapData(timestamps)
            }
            
        } catch (e: Exception) {
            // 忽略解析错误
        }
    }
    
    /**
     * 更新热力图数据
     */
    private suspend fun updateHeatMapData(timestamps: List<String>) = withContext(Dispatchers.IO) {
        val map: MutableMap<LocalDate, Int> = mutableMapOf()
        
        timestamps.forEach { timestamp ->
            try {
                // 解析 ISO 8601 格式的时间戳（如：2025-07-16T07:56:53Z）
                val instant = Instant.parse(timestamp)
                val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                map[localDate] = map.getOrElse(localDate) { 0 } + 1
            } catch (e: Exception) {
                // 忽略无法解析的时间戳
            }
        }
        
        // 更新热力图数据
        levelMemosMap.clear()
        levelMemosMap.putAll(convertToLevelMap(map))
        
        // 更新去重后的天数统计
        _uniqueDaysCount.value = map.keys.size
    }
    
    /**
     * 将数量映射转换为级别映射
     */
    private fun convertToLevelMap(inputMap: Map<LocalDate, Int>): Map<LocalDate, Level> {
        return inputMap.mapValues { (_, value) ->
            when (value) {
                in 0 until 1 -> Level.Zero
                in 1 until 3 -> Level.One
                in 3 until 5 -> Level.Two
                in 5 until 8 -> Level.Three
                else -> Level.Four
            }
        }
    }
    
    /**
     * 检查配置并加载数据，用于配置完成后手动触发加载
     */
    fun checkConfigAndLoadMemos() {
        viewModelScope.launch {
            val isLoginSuccess = SharedPreferencesUtils.memosLoginSuccess.first()
            val serverUrl = SharedPreferencesUtils.memosServerUrl.first()
            val userSession = SharedPreferencesUtils.memosUserSession.first()
            val userName = SharedPreferencesUtils.memosUserName.first()
            
            if (isLoginSuccess && 
                !serverUrl.isNullOrBlank() && 
                !userSession.isNullOrBlank() &&
                !userName.isNullOrBlank()) {
                loadMemos()
                loadUserStats()
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        memosApiService.close()
        usersApiService.close()
    }
} 