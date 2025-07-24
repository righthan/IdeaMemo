package com.ldlywt.note.ui.page.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ldlywt.note.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var serverUrlError by remember { mutableStateOf("") }
    
    // 监听登录状态
    val loginState by viewModel.loginState.collectAsState()
    val isLoading = loginState is LoginState.Loading
    
    // 处理登录状态变化
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                onLoginSuccess()
            }
            is LoginState.Error -> {
                errorMessage = state.message
            }
            is LoginState.Loading -> {
                errorMessage = ""
            }
            else -> {
                // 初始状态，不做处理
            }
        }
    }
    
    // 验证服务器地址格式
    fun validateServerUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // 添加IME padding避免键盘遮挡
            .verticalScroll(rememberScrollState()) // 添加滚动支持
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "登录到 Memos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 服务器地址输入框
        OutlinedTextField(
            value = serverUrl,
            onValueChange = { 
                serverUrl = it
                // 实时验证服务器地址格式
                serverUrlError = if (it.isNotBlank() && !validateServerUrl(it)) {
                    "服务器地址必须以 http:// 或 https:// 开头"
                } else {
                    ""
                }
            },
            label = { Text("服务器地址") },
            placeholder = { Text("https://example.com:5230", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            singleLine = true,
            isError = serverUrlError.isNotEmpty(),
            supportingText = if (serverUrlError.isNotEmpty()) {
                { Text(serverUrlError, color = MaterialTheme.colorScheme.error) }
            } else null
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 用户名输入框
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 密码输入框
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 登录按钮
        Button(
            onClick = {
                when {
                    serverUrl.isBlank() || username.isBlank() || password.isBlank() -> {
                        errorMessage = "请填写完整的登录信息"
                    }
                    !validateServerUrl(serverUrl.trim()) -> {
                        errorMessage = "请输入正确的服务器地址格式（以 http:// 或 https:// 开头）"
                    }
                    else -> {
                        viewModel.login(serverUrl.trim(), username.trim(), password)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && serverUrlError.isEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("登录")
            }
        }
        
        // 错误提示
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // 添加底部空间，确保在键盘弹出时内容不被遮挡
        Spacer(modifier = Modifier.height(32.dp))
    }
} 