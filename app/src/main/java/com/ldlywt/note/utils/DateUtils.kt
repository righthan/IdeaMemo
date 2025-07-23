package com.ldlywt.note.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toMinute(): String {
    val dateTime = Date(this)
    val format = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    return format.format(dateTime)
}

fun Long.toDD(): String {
    val dateTime = Date(this)
    val format = SimpleDateFormat("dd", Locale.ENGLISH)
    return format.format(dateTime)
}

fun Long.toMM(): String {
    val dateTime = Date(this)
    val format = SimpleDateFormat("MM", Locale.ENGLISH)
    return format.format(dateTime)
}

fun Long.toYYMMDD(): String {
    val dateTime = Date(this)
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    return format.format(dateTime)
}

fun Long.toDate(): String {
    val dateTime = Date(this)
    val format = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
    return format.format(dateTime)
}

fun Long.toMYYMM(): String {
    val dateTime = Date(this)
    val format = SimpleDateFormat("yyyy/MM", Locale.ENGLISH)
    return format.format(dateTime)
}

fun Long.toBackUpFileName(): String {
    val dateTime = Date(this)
    val format = SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH)
    return format.format(dateTime)
}

fun Long.toTime(): String {
    val dateTime = Date(this)
    val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH)
    return format.format(dateTime)
}

/**
 * 将 Memos API 返回的时间字符串转换为显示格式
 * Memos API 返回的时间格式通常是 ISO 8601 格式: 2025-07-23T07:32:52Z
 */
fun String.formatMemoTime(): String {
    return try {
        val instant = java.time.Instant.parse(this)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        this
    }
}
