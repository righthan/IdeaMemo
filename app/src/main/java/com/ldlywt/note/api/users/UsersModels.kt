package com.ldlywt.note.api.users

import kotlinx.serialization.Serializable

// 用户统计信息数据模型
@Serializable
data class UserStatsResponse(
    val name: String,
    val memoDisplayTimestamps: List<String>,
    val memoTypeStats: MemoTypeStats,
    val tagCount: Map<String, Int>,
    val pinnedMemos: List<String>,
    val totalMemoCount: Int
)

@Serializable
data class MemoTypeStats(
    val linkCount: Int,
    val codeCount: Int,
    val todoCount: Int,
    val undoCount: Int
) 