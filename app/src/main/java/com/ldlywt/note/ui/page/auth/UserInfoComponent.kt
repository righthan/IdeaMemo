package com.ldlywt.note.ui.page.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ldlywt.note.R
import com.ldlywt.note.utils.SharedPreferencesUtils
import kotlinx.coroutines.flow.first

@Composable
fun UserInfoComponent(
    onLogout: () -> Unit,
    viewModel: UserInfoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // 用户信息状态
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // 获取用户信息
    LaunchedEffect(Unit) {
        displayName = SharedPreferencesUtils.memosUserDisplayName.first() ?: ""
        username = SharedPreferencesUtils.memosUsername.first() ?: ""
        avatarUrl = SharedPreferencesUtils.memosAvatarUrl.first() ?: ""
        serverUrl = SharedPreferencesUtils.memosServerUrl.first() ?: ""
    }

    // 监听退出登录状态
    val logoutState by viewModel.logoutState.collectAsState()
    
    LaunchedEffect(logoutState) {
        when (logoutState) {
            is LogoutState.Success -> {
                onLogout()
            }
            else -> {}
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 用户头像
        UserAvatar(
            avatarUrl = if (avatarUrl.isNotEmpty()) "$serverUrl/$avatarUrl" else "",
            modifier = Modifier.size(60.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 用户信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = displayName.ifEmpty { username },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (displayName.isNotEmpty() && username.isNotEmpty()) {
                Text(
                    text = "@$username",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 退出登录按钮
        IconButton(
            onClick = { showLogoutDialog = true }
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "退出登录",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    // 退出登录确认对话框
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("确认退出") },
            text = { Text("确定要退出登录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun UserAvatar(
    avatarUrl: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "用户头像",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.logo),
                placeholder = painterResource(id = R.drawable.logo)
            )
        } else {
            // 没有头像URL时显示默认头像
            DefaultAvatar(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun DefaultAvatar(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "默认头像",
        modifier = modifier.clip(CircleShape),
        contentScale = ContentScale.Crop
    )
} 