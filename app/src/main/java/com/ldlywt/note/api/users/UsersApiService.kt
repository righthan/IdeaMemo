package com.ldlywt.note.api.users

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.ldlywt.note.utils.SharedPreferencesUtils
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersApiService @Inject constructor() {
    
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
    
    /**
     * 获取用户统计信息
     * @return UserStatsResponse 用户统计信息
     */
    suspend fun getUserStats(): UserStatsResponse {
        val baseUrl = SharedPreferencesUtils.memosServerUrl.first() ?: ""
        val userSession = SharedPreferencesUtils.memosUserSession.first() ?: ""
        val userName = SharedPreferencesUtils.memosUserName.first() ?: ""
        
        return httpClient.get("$baseUrl/api/v1/$userName:getStats") {
            header("Cookie", "user_session=$userSession")
        }.body()
    }
    
    fun close() {
        httpClient.close()
    }
} 