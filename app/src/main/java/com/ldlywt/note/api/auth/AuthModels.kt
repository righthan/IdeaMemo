package com.ldlywt.note.api.auth

import kotlinx.serialization.Serializable

// 登录请求数据模型
@Serializable
data class LoginRequest(
    val passwordCredentials: PasswordCredentials
)

@Serializable
data class PasswordCredentials(
    val username: String,
    val password: String
)

// 登录成功响应数据模型
@Serializable
data class LoginSuccessResponse(
    val user: UserInfo,
    val lastAccessedAt: String? = null
)

@Serializable
data class UserInfo(
    val name: String,
    val role: String,
    val username: String,
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val description: String = "",
    val password: String = "",
    val state: String = "NORMAL",
    val createTime: String,
    val updateTime: String
)

// 登录失败响应数据模型
@Serializable
data class LoginErrorResponse(
    val code: Int,
    val message: String,
    val details: List<String> = emptyList()
) 