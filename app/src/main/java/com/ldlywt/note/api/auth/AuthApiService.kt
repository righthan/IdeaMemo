package com.ldlywt.note.api.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.cookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.ldlywt.note.utils.SharedPreferencesUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthApiService @Inject constructor() {
    
    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            })
        }
        install(HttpCookies)
        install(Logging) {
            level = LogLevel.INFO
        }
        expectSuccess = false // 不要对非2xx状态码抛出异常
    }
    
    /**
     * 用户登录
     * @param serverUrl 服务器地址
     * @param username 用户名
     * @param password 密码
     * @return Pair<Boolean, String> - (是否成功, 消息)
     */
    suspend fun login(serverUrl: String, username: String, password: String): Pair<Boolean, String> {
        return try {
            val loginRequest = LoginRequest(
                passwordCredentials = PasswordCredentials(username, password)
            )
            
            val response: HttpResponse = httpClient.post("$serverUrl/api/v1/auth/sessions") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }
            
            if (response.status.isSuccess()) {
                // 登录成功，解析响应
                val successResponse: LoginSuccessResponse = response.body()
                
                // 获取Set-Cookie头并提取user_session
                val setCookieHeader = response.headers["grpc-metadata-set-cookie"]
                val userSession = setCookieHeader?.let {
                    Regex("user_session=([^;]+)").find(it)?.groupValues?.get(1)
                }
                println(userSession)
                
                // 保存登录信息到SharedPreferences
                SharedPreferencesUtils.updateMemosLoginSuccess(true)
                SharedPreferencesUtils.updateMemosServerUrl(serverUrl)
                SharedPreferencesUtils.updateMemosUserSession(userSession)
                SharedPreferencesUtils.updateMemosUserName(successResponse.user.name)
                SharedPreferencesUtils.updateMemosUsername(successResponse.user.username)
                SharedPreferencesUtils.updateMemosUserDisplayName(successResponse.user.displayName)
                SharedPreferencesUtils.updateMemosUserRole(successResponse.user.role)
                SharedPreferencesUtils.updateMemosAvatarUrl(successResponse.user.avatarUrl)
                
                Pair(true, "登录成功！用户：${successResponse.user.displayName}")
            } else {
                // 登录失败，解析错误响应
                val errorResponse: LoginErrorResponse = response.body()
                Pair(false, errorResponse.message)
            }
        } catch (e: Exception) {
            Pair(false, "登录失败：${e.message ?: "未知错误"}")
        }
    }
    
    /**
     * 退出登录
     * @param serverUrl 服务器地址
     * @param userSession 用户session
     * @return Pair<Boolean, String> - (是否成功, 消息)
     */
    suspend fun logout(serverUrl: String, userSession: String): Pair<Boolean, String> {
        return try {
            val response: HttpResponse = httpClient.delete("$serverUrl/api/v1/auth/sessions/current") {
                header("Cookie", "user_session=$userSession")
            }
            
            // 无论服务器响应如何，都清除本地登录信息
            SharedPreferencesUtils.clearMemosConfig()
            
            if (response.status.isSuccess()) {
                Pair(true, "退出登录成功")
            } else {
                // 即使服务器返回错误，本地也已清除，认为退出成功
                Pair(true, "已清除本地登录信息")
            }
        } catch (e: Exception) {
            // 出现异常时也清除本地登录信息
            SharedPreferencesUtils.clearMemosConfig()
            Pair(true, "已清除本地登录信息")
        }
    }
    
    fun close() {
        httpClient.close()
    }
} 