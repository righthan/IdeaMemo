package com.ldlywt.note.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.ldlywt.note.utils.SharedPreferencesUtils
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemosApiService @Inject constructor() {
    
    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    
    suspend fun getMemos(
        pageSize: Int = 10,
        pageToken: String = "",
        state: String = "NORMAL"
    ): MemosResponse {
        val baseUrl = SharedPreferencesUtils.memosServerUrl.first() ?: ""
        val authToken = SharedPreferencesUtils.memosAuthToken.first() ?: ""
        val userName = SharedPreferencesUtils.memosUserName.first() ?: ""
        
        return httpClient.get("$baseUrl/api/v1/memos") {
            url {
                parameters.append("pageSize", pageSize.toString())
                parameters.append("pageToken", pageToken)
                parameters.append("state", state)
                parameters.append("parent", userName)
            }
            header("Authorization", "Bearer $authToken")
        }.body()
    }
    
    /**
     * 验证Memos服务器连接和Token有效性
     * @param serverUrl 服务器地址
     * @param authToken 认证Token (不包含Bearer前缀)
     * @return Pair<Boolean, String> - (是否成功, 消息)
     */
    suspend fun validateConnection(serverUrl: String, authToken: String): Pair<Boolean, String> {
        return try {
            // 创建一个临时客户端，不抛出HTTP错误异常
            val tempClient = HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = false
                    })
                }
                expectSuccess = false // 不要对非2xx状态码抛出异常
            }
            
            val response: HttpResponse = tempClient.get("$serverUrl/api/v1/auth/sessions/current") {
                header("Authorization", "Bearer $authToken")
            }
            
            if (response.status.isSuccess()) {
                // 尝试解析成功响应
                val successResponse: AuthValidationSuccessResponse = response.body()
                tempClient.close()
                
                // 保存用户信息到SharedPreferences
                SharedPreferencesUtils.updateMemosUserName(successResponse.user.name)
                SharedPreferencesUtils.updateMemosUserDisplayName(successResponse.user.displayName)
                SharedPreferencesUtils.updateMemosUserRole(successResponse.user.role)
                
                Pair(true, "连接成功！用户：${successResponse.user.displayName}")
            } else {
                // 尝试解析错误响应
                val errorResponse: AuthValidationErrorResponse = response.body()
                tempClient.close()
                Pair(false, errorResponse.message)
            }
        } catch (e: Exception) {
            Pair(false, "连接失败：${e.message ?: "未知错误"}")
        }
    }
    
    /**
     * 获取用户统计信息
     * @return UserStatsResponse 用户统计信息
     */
    suspend fun getUserStats(): UserStatsResponse {
        val baseUrl = SharedPreferencesUtils.memosServerUrl.first() ?: ""
        val authToken = SharedPreferencesUtils.memosAuthToken.first() ?: ""
        val userName = SharedPreferencesUtils.memosUserName.first() ?: ""
        
        return httpClient.get("$baseUrl/api/v1/$userName:getStats") {
            header("Authorization", "Bearer $authToken")
        }.body()
    }
    
    fun close() {
        httpClient.close()
    }
} 