package com.ldlywt.note.ui.page.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldlywt.note.api.auth.AuthApiService
import com.ldlywt.note.utils.SharedPreferencesUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LogoutState {
    object Idle : LogoutState()
    object Loading : LogoutState()
    data class Success(val message: String) : LogoutState()
    data class Error(val message: String) : LogoutState()
}

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val authApiService: AuthApiService
) : ViewModel() {
    
    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()
    
    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.Loading
            
            try {
                val serverUrl = SharedPreferencesUtils.memosServerUrl.first() ?: ""
                val userSession = SharedPreferencesUtils.memosUserSession.first() ?: ""
                
                val (success, message) = authApiService.logout(serverUrl, userSession)
                
                if (success) {
                    _logoutState.value = LogoutState.Success(message)
                } else {
                    _logoutState.value = LogoutState.Error(message)
                }
            } catch (e: Exception) {
                // 即使出现异常，也要清除本地数据
                SharedPreferencesUtils.clearMemosConfig()
                _logoutState.value = LogoutState.Success("已清除本地登录信息")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        authApiService.close()
    }
} 