package com.ldlywt.note.ui.page.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldlywt.note.api.auth.AuthApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val message: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authApiService: AuthApiService
) : ViewModel() {
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    // 验证服务器地址格式
    private fun validateServerUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }
    
    // 验证输入参数
    private fun validateInput(serverUrl: String, username: String, password: String): String? {
        return when {
            serverUrl.isBlank() -> "服务器地址不能为空"
            username.isBlank() -> "用户名不能为空"
            password.isBlank() -> "密码不能为空"
            !validateServerUrl(serverUrl) -> "服务器地址必须以 http:// 或 https:// 开头"
            else -> null
        }
    }
    
    fun login(serverUrl: String, username: String, password: String) {
        viewModelScope.launch {
            // 输入验证
            val validationError = validateInput(serverUrl, username, password)
            if (validationError != null) {
                _loginState.value = LoginState.Error(validationError)
                return@launch
            }
            
            _loginState.value = LoginState.Loading
            
            try {
                val (success, message) = authApiService.login(serverUrl, username, password)
                
                if (success) {
                    _loginState.value = LoginState.Success(message)
                } else {
                    _loginState.value = LoginState.Error(message)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("登录失败：${e.message ?: "未知错误"}")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        authApiService.close()
    }
} 